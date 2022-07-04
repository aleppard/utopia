// Prototype code (i.e. it's not meant to be good yet).

//The path to the image that we want to add.
var imgPath3 = '/images/heroic_spirits__vx_ace__by_kiradu60_d7hpnuy.png'

//Create a new Image object.
var imgObj3 = new Image();
 
//Set the src of this Image object.
imgObj3.src = imgPath3;

// @todo Get from back-end.
var tile_size = 32;

// Offset of view from start of map.
var view_pixel_x = null;
var view_pixel_y = null;

var view_width;
var view_height;
var screen_width;
var screen_height;

// @todo use "tile_x" vs. "pixel_x".
var avatar_pixel_x;
var avatar_pixel_y;

const NORTH = "north"
const EAST = "east"
const SOUTH = "south"
const WEST = "west"

const ARROW_UP = '38';
const ARROW_DOWN = '40';
const ARROW_LEFT = '37';
const ARROW_RIGHT = '39';

var last_avatar_direction;
var avatar_margin = 5;

var map;
var is_traverseable;
var images = new Map();

var visibility;

// Frame rate when moving avatar
const MOVE_FRAME_COUNT_PER_SECOND = 60;

// Number of pixels to move avatar per frame.
const MOVE_PIXELS_PER_FRAME = 4;

var move_key_down = null;

function isTraverseable(pixel_x, pixel_y) {
    x_floor = Math.floor(pixel_x / tile_size);
    x_ceil = Math.ceil(pixel_x / tile_size);
    y_floor = Math.floor(pixel_y / tile_size);
    y_ceil = Math.ceil(pixel_y / tile_size);     

    return (is_traverseable[y_floor][x_floor] &&
            is_traverseable[y_floor][x_ceil] &&
            is_traverseable[y_ceil][x_floor] &&
            is_traverseable[y_ceil][x_ceil]);
}

function draw_over_avatar(ctx, old_avatar_pixel_x, old_avatar_pixel_y) {
    // @todo This is excessive. At most we need to redraw two tiles and
    // not even the entire two tiles.
    x_floor = Math.floor(old_avatar_pixel_x / tile_size);
    x_ceil = Math.ceil(old_avatar_pixel_x / tile_size);
    y_floor = Math.floor(old_avatar_pixel_y / tile_size);
    y_ceil = Math.ceil(old_avatar_pixel_y / tile_size);     

    const coordinates = [[x_floor, y_floor],
                         [x_floor, y_ceil],
                         [x_ceil, y_floor],
                         [x_ceil, y_ceil]];
    coordinates.forEach(coordinate => {
        tile = get_tile(coordinate[0], coordinate[1])
        draw_tile(ctx, tile,
                  coordinate[0] * tile_size - view_pixel_x,
                  coordinate[1] * tile_size - view_pixel_y,
                  0, 0,
                  tile_size, tile_size);
    });
}
                       
function update_visibility() {
    var made_visible = [];
    // @todo This could be made much faster!
    const RADIUS = 6;

    var centre_x = Math.round(avatar_pixel_x / tile_size)
    var centre_y = Math.round(avatar_pixel_y / tile_size)

    for (var x = centre_x - RADIUS; x <= centre_x + RADIUS; x++) {
        for (var y = centre_y - RADIUS; y <= centre_y + RADIUS; y++) {
            if (x >= 0 && y >= 0 && x < map.width && y < map.height) {
                if (!visibility[y][x]) {                
                    if (Math.sqrt(Math.pow(centre_x - x, 2) +
                                  Math.pow(centre_y - y, 2)) < RADIUS) {
                        // @todo Use line of sight calculations.
                        // @todo Standard x,y ordering.
                        visibility[y][x] = true
                        made_visible.push([x, y])
                    }
                }
            }
        }
    }

    return made_visible
}

function get_tile(x, y) {
    return map.tiles[y][x]
}

function is_tile_visible(x, y) {
    return visibility[y][x]
}

function draw_tile(ctx, tile, x, y, tile_x_offset, tile_y_offset, tile_width, tile_height) {
    // @todo forEach
    for (var i = 0; i < tile.length; i++) {
        tileId = tile[i]

        // @todo Do we need the other arguments here?
        ctx.drawImage(images.get(tileId),
                      tile_x_offset,
                      tile_y_offset,
                      tile_width, tile_height, x, y,
                      tile_width, tile_height);
    }
}

function draw_tiles(ctx, tiles) {
    for (var i = 0; i < tiles.length; i++) {
        tile = tiles[i]
        x = tile[0]
        y = tile[1]
        tile = get_tile(x, y);

        // TODO What about partial tiles?
        draw_tile(ctx, tile,
                  x * tile_size - view_pixel_x,
                  y * tile_size - view_pixel_y,
                  0, 0,
                  tile_size, tile_size);
    }
}

function draw_map() {
    var element = document.getElementById('main')
    var ctx = element.getContext('2d');
    element.width = window.innerWidth;
    element.height = window.innerHeight;

    var positionInfo = element.getBoundingClientRect();

    var height = positionInfo.height;
    var width = positionInfo.width;

    if (view_pixel_x == null) {
        screen_width = width;
        screen_height = height

        // @todo Remove these two
        view_width = Math.floor(width / tile_size);
        view_height = Math.floor(height / tile_size);

        // @todo Don't position these such that the screen extends past the
        // map.
        view_pixel_x = Math.max(Math.floor(avatar_pixel_x / tile_size - view_width / 2) * tile_size, 0)
        view_pixel_y = Math.max(Math.floor(avatar_pixel_y / tile_size - view_height / 2) * tile_size, 0)
    }

    view_x = Math.floor(view_pixel_x / tile_size)
    view_y = Math.floor(view_pixel_y / tile_size)
    
    tile_y_offset = 0
    
    for (var i = 0; tile_y_offset < height; i++) {

        if (i == 0) {
            source_tile_y_offset = view_pixel_y % tile_size
            tile_height = tile_size - source_tile_y_offset
        }
        else if (tile_y_offset + tile_size >= height) {
            source_tile_y_offset = 0            
            tile_height = height - tile_y_offset
        }
        else {
            source_tile_y_offset = 0
            tile_height = tile_size
        }
        
        tile_x_offset = 0

        for (var j = 0; tile_x_offset < width; j++) {

            if (j == 0) {
                source_tile_x_offset = view_pixel_x % tile_size
                tile_width = tile_size - source_tile_x_offset
            }
            else if (tile_x_offset + tile_size >= width) {
                source_tile_x_offset = 0                
                tile_width = width - tile_x_offset
            }
            else {
                source_tile_x_offset = 0
                tile_width = tile_size
            }

            tile = get_tile(view_x + j, view_y + i)
            if (is_tile_visible(view_x + j, view_y + i)) {
                draw_tile(ctx, tile, tile_x_offset, tile_y_offset,
                          source_tile_x_offset, source_tile_y_offset,
                          tile_width, tile_height)
            }

            tile_x_offset += tile_width
        }

        tile_y_offset += tile_height
    }
}

function draw_avatar() {
    // TODO: Avatar should go one more to the left and up?
    var element = document.getElementById('main')
    var ctx = element.getContext('2d');

    if (last_avatar_direction == NORTH) {
        img_x_offset = 9 * tile_size
        img_y_offset = 7 * tile_size
    }
    else if (last_avatar_direction == WEST) {
        img_x_offset = 9 * tile_size
        img_y_offset = 5 * tile_size
    }
    else if (last_avatar_direction == SOUTH) {
        img_x_offset = 9 * tile_size
        img_y_offset = 4 * tile_size
    }
    else {
        img_x_offset = 9 * tile_size
        img_y_offset = 6 * tile_size
    }

    ctx.drawImage(imgObj3,
                  img_x_offset,
                  img_y_offset,
                  tile_size,
                  tile_size,
                  avatar_pixel_x - view_pixel_x,
                  avatar_pixel_y - view_pixel_y,
                  tile_size, tile_size);    
}

function move_avatar(avatar_direction) {
    last_avatar_direction = avatar_direction;

    var element = document.getElementById('main')
    var ctx = element.getContext('2d');
    var move_view = false;
    
    new_avatar_pixel_x = avatar_pixel_x
    new_avatar_pixel_y = avatar_pixel_y

    new_view_pixel_x = view_pixel_x
    new_view_pixel_y = view_pixel_y
    
    if (avatar_direction == NORTH) {
        if (avatar_pixel_y > 0) {
            new_avatar_pixel_y = new_avatar_pixel_y - MOVE_PIXELS_PER_FRAME
            
            if (view_pixel_y > 0 && Math.round((avatar_pixel_y - view_pixel_y) / tile_size) < avatar_margin) {
                new_view_pixel_y -= MOVE_PIXELS_PER_FRAME;
                move_view = true;
            }
        }
    }
    else if (avatar_direction == WEST) {
        if (avatar_pixel_x > 0) {
            new_avatar_pixel_x = new_avatar_pixel_x - MOVE_PIXELS_PER_FRAME
            
            if (view_pixel_x > 0 && Math.round((avatar_pixel_x - view_pixel_x) / tile_size) < avatar_margin) {
                new_view_pixel_x -= MOVE_PIXELS_PER_FRAME;
                move_view = true;
            }
        }
    }
    else if (avatar_direction == SOUTH) {
        if (avatar_pixel_y < map.height * tile_size) {
            new_avatar_pixel_y = new_avatar_pixel_y + MOVE_PIXELS_PER_FRAME

            if (view_pixel_y < (map.height * tile_size - screen_height)) {
                if ((view_height - Math.round((avatar_pixel_y - view_pixel_y) / tile_size)) < avatar_margin) {
                    new_view_pixel_y += MOVE_PIXELS_PER_FRAME;
                    move_view = true;
                }
            }
        }
    }
    else {
        if (avatar_pixel_x < map.width * tile_size) {
            new_avatar_pixel_x = new_avatar_pixel_x + MOVE_PIXELS_PER_FRAME

            if (view_pixel_x < (map.width * tile_size - screen_width)) {
                if ((view_width - Math.round((avatar_pixel_x - view_pixel_x) / tile_size)) < avatar_margin) {
                    new_view_pixel_x += MOVE_PIXELS_PER_FRAME;
                    move_view = true;
                }
            }
        }
    }

    if (isTraverseable(new_avatar_pixel_x, new_avatar_pixel_y)) {
        old_avatar_pixel_x = avatar_pixel_x
        old_avatar_pixel_y = avatar_pixel_y
        
        avatar_pixel_x = new_avatar_pixel_x
        avatar_pixel_y = new_avatar_pixel_y
        
        view_pixel_x = new_view_pixel_x
        view_pixel_y = new_view_pixel_y

        newly_visible_tiles = update_visibility()
        
        if (move_view) {
            draw_map()
        }
        else {
            // Cover up old avatar position.
            draw_over_avatar(ctx, old_avatar_pixel_x, old_avatar_pixel_y)

            // Draw newly visible tiles
            draw_tiles(ctx, newly_visible_tiles);
        }

        offset_newly_visible_tiles =
            newly_visible_tiles.map(tile => [tile[0] + map.startX,
                                             tile[1] + map.startY])

        // @todo Pass pixel offset to server
        var session = { user: { x: Math.round(avatar_pixel_x / tile_size) + map.startX,
                                y: Math.round(avatar_pixel_y / tile_size) + map.startY,
                                direction: last_avatar_direction
                              },
                        traversal: { seen: offset_newly_visible_tiles}}
    
        fetch('/api/v0/session.json',
              {
                  method:'PUT',
                  body:JSON.stringify(session)
              }).then();
    }
    
    draw_avatar()
}

function draw() {
    draw_map();
    draw_avatar();
}

function on_load() {
    // Listen for touch events. Touching screen will move avatar.
    var canvas = document.getElementById('main')
    canvas.addEventListener("touchstart", function (e) {
        var touch = e.touches[0];

        x = touch.clientX / tile_size
        y = touch.clientY / tile_size
        
        if (Math.abs(Math.round((avatar_pixel_x - view_pixel_x) / tile_size) - x) >
            Math.abs(Math.round((avatar_pixel_y - view_pixel_y) / tile_size) - y)) {
            
            if (x < Math.round(avatar_pixel_x - view_pixel_x / tile_size)) {
                move_avatar(WEST);
            }
            else {
                move_avatar(EAST);
            }
        }
        else {
            if (y < Math.round(avatar_pixel_y - view_pixel_y / tile_size)) {
                move_avatar(NORTH);
            }
            else {
                move_avatar(SOUTH);
            }
        }
    });
    
    update_visibility();
    draw();
}

var resize_timeout;

function resize_map() {
    clearTimeout(resize_timeout);
    resize_timeout = setTimeout(draw, 100);    
}

function loadImage(url) {
    return new Promise(resolve => {
        const image = new Image();
        image.addEventListener('load', () => {
            resolve(image);
        });
        image.src = url;
    });
}

const url = '/api/v0/session.json'
fetch(url)
    .then(data => data.json())
    .then((json) => {
        map = json.map
        is_traverseable = json.map.isTraverseable;
        avatar_pixel_x = (json.user.x - map.startX) * tile_size;
        avatar_pixel_y = (json.user.y - map.startY) * tile_size;
        last_avatar_direction = json.user.direction;
        visibility = json.traversal.hasSeen;

        // Find unique tiles.
        for (var y = 0; y < map.height; y++) {
            for (var x = 0; x < map.width; x++) {
                tile_list = map.tiles[y][x]
                for (var tileIndex = 0; tileIndex < tile_list.length; tileIndex++) {
                    images.set(tile_list[tileIndex], null);
                }
            }
        }

        // Load tiles.
        var promises = [];
        var tileIds = Array.from(images.keys());
        
        tileIds.forEach(tileId => {
            if (tileId != 0) {
                promises.push(loadImage('/api/v0/tile.png?id=' + tileId));
            }
        });
        
        Promise.allSettled(promises).then(results => {
            // @todo Error handling. See promise.status.
            results.forEach((promise, index) => {
                images.set(tileIds[index], promise.value);
            });
            
            on_load();
        })
        
        document.onkeydown = keyDown;
        document.onkeyup = keyUp;
        window.addEventListener("resize", resize_map);
})

var move_interval = null;

function move() {
    if (move_key_down == ARROW_UP) {    
        move_avatar(NORTH);
    }
    else if (move_key_down == ARROW_DOWN) {
        move_avatar(SOUTH);            
    }
    else if (move_key_down == ARROW_LEFT) {
        move_avatar(WEST);           
    }
    else {
        move_avatar(EAST);
    }
}

function startMove(keyCode) {
    // Nothing more to do if we are already moving in the right direction.
    if (move_key_down == keyCode) return;
    
    move_key_down = keyCode;
    move();

    if (move_interval == null) {
        move_interval = setInterval(move, 1000 / MOVE_FRAME_COUNT_PER_SECOND);
    }
}

function stopMove() {
    if (move_interval != null) {
        clearInterval(move_interval);
        move_interval = null;
        move_key_down = null;
    }
}

function keyDown(e) {
    e = e || window.event;

    if (e.keyCode == ARROW_UP ||
        e.keyCode == ARROW_DOWN ||
        e.keyCode == ARROW_LEFT ||
        e.keyCode == ARROW_RIGHT) {
        startMove(e.keyCode);
    }
    else {
        move_key_down = null;
    }
}

function keyUp(e) {
    stopMove();
}
