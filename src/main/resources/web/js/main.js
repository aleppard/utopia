// Prototype code (i.e. it's not meant to be good).

//The path to the image that we want to add.
var imgPath3 = '/utopia/images/heroic_spirits__vx_ace__by_kiradu60_d7hpnuy.png'

//Create a new Image object.
var imgObj3 = new Image();
 
//Set the src of this Image object.
imgObj3.src = imgPath3;

var tile_size = 32;
var tile_scale = 1; // TODO: Remove

// Offset of view from start of map.
var view_x = null
var view_y = null

var view_width;
var view_height;

var avatar_x;
var avatar_y;

const NORTH = "north"
const EAST = "east"
const SOUTH = "south"
const WEST = "west"

var last_avatar_direction;
var avatar_margin = 5;

var map;
var is_traverseable;
var images = new Map();

var visibility;

function update_visibility() {
    var made_visible = [];
    // @todo This could be made much faster!
    const RADIUS = 6;

    var centre_x = avatar_x + 1
    var centre_y = avatar_y + 1

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
        x = tile[0] - 1
        y = tile[1] - 1
        tile = get_tile(x + 1, y + 1);

        // TODO What about partial tiles?
        draw_tile(ctx, tile,
                  left_margin + tile_size * (x - view_x),
                  top_margin + tile_size * (y - view_y),
                  0, 0,
                  tile_size, tile_size);
    }
}

var left_margin;
var top_margin;

function draw_map() {
    var element = document.getElementById('main')
    var ctx = element.getContext('2d');
    element.width = window.innerWidth;
    element.height = window.innerHeight;

    var positionInfo = element.getBoundingClientRect();

    var height = positionInfo.height;
    var width = positionInfo.width;

    view_width = Math.floor(width / tile_size);
    view_height = Math.floor(height / tile_size);
    if (view_x == null) {
        view_x = Math.max(avatar_x - Math.floor(view_width / 2), 0)
        view_y = Math.max(avatar_y - Math.floor(view_height / 2), 0)
    }
    
    left_margin = Math.floor((width - (view_width * (tile_size * tile_scale))) / 2)
    top_margin = Math.floor((height - (view_height * (tile_size * tile_scale))) / 2)

    var right_margin = width - view_width * tile_size * tile_scale - left_margin;
    var bottom_margin = height - view_height * tile_size * tile_scale - top_margin;
    
    if (left_margin  > 0) {
        view_width += 2;
    }

    if (top_margin > 0) {
        view_height += 2;
    }

    tile_y_offset = 0
    
    for (var i = 0; i < view_height; i++) {

        if (i == 0) {
            source_tile_y_offset = (tile_size - top_margin / tile_scale)
            tile_height = top_margin / tile_scale
        }
        else if (i == view_height - 1) {
            source_tile_y_offset = 0            
            tile_height = bottom_margin / tile_scale
        }
        else {
            source_tile_y_offset = 0
            tile_height = tile_size
        }
        
        tile_x_offset = 0

        for (var j = 0; j < view_width; j++) {

            if (j == 0) {
                source_tile_x_offset = (tile_size - left_margin / tile_scale)
                tile_width = left_margin / tile_scale;
            }
            else if (j == view_width - 1) {
                source_tile_x_offset = 0                
                tile_width = right_margin / tile_scale
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

            tile_x_offset += tile_width * tile_scale
        }

        tile_y_offset += tile_height * tile_scale
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
                  left_margin + tile_size * (avatar_x - view_x),
                  top_margin + tile_size * (avatar_y - view_y),
                  tile_size, tile_size);    
}

function move_avatar(avatar_direction) {
    last_avatar_direction = avatar_direction;

    var element = document.getElementById('main')
    var ctx = element.getContext('2d');
    var move_view = false;
    
    new_avatar_x = avatar_x
    new_avatar_y = avatar_y

    new_view_x = view_x
    new_view_y = view_y
    
    if (avatar_direction == NORTH) {
        if (avatar_y > 0) {
            new_avatar_y--;
            
            if (view_y > 0 && ((avatar_y - view_y) < avatar_margin)) {
                new_view_y--;
                move_view = true;
            }
        }
    }
    else if (avatar_direction == WEST) {
        if (avatar_x > 0) {
            new_avatar_x--;
            
            if (view_x > 0 && ((avatar_x - view_x) < avatar_margin)) {
                new_view_x--;
                move_view = true;
            }
        }
    }
    else if (avatar_direction == SOUTH) {
        if (avatar_y < map.height - 3) {
            new_avatar_y++;

            if (view_y < (map.height - view_height)) {
                if ((view_y + view_height - avatar_y) < avatar_margin) {
                    new_view_y++;
                    move_view = true;
                }
            }
        }
    }
    else {
        if (avatar_x < map.width - 3) {
            new_avatar_x++;

            if (view_x < (map.width - view_width)) {
                if ((view_x + view_width - avatar_x) < avatar_margin) {
                    new_view_x++;
                    move_view = true;
                }
            }
        }
    }

    if (is_traverseable[new_avatar_y + 1][new_avatar_x + 1]) {    
        old_avatar_x = avatar_x
        old_avatar_y = avatar_y
        
        avatar_x = new_avatar_x
        avatar_y = new_avatar_y
        
        view_x = new_view_x
        view_y = new_view_y

        newly_visible_tiles = update_visibility()
        
        if (move_view) {
            draw_map()
        }
        else {
            // Cover up old avatar position.
            tile = get_tile(old_avatar_x + 1, old_avatar_y + 1);            
            draw_tile(ctx, tile,
                      left_margin + tile_size * (old_avatar_x - view_x),
                      top_margin + tile_size * (old_avatar_y - view_y),
                      0, 0,
                      tile_size, tile_size);

            // Draw newly visible tiles
            draw_tiles(ctx, newly_visible_tiles);
        }


        offset_newly_visible_tiles =
            newly_visible_tiles.map(tile => [tile[0] + map.startX,
                                             tile[1] + map.startY])
        
        var session = { user: { x: avatar_x + map.startX,
                                y: avatar_y + map.startY,
                                direction: last_avatar_direction
                              },
                        traversal: { seen: offset_newly_visible_tiles}}
    
        fetch('/utopia/api/v0/session.json',
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

        // TODO: Take into account margin
        x = touch.clientX / tile_size
        y = touch.clientY / tile_size
        
        if (Math.abs((avatar_x - view_x) - x) >
            Math.abs((avatar_y - view_y) - y)) {
            
            if (x < (avatar_x - view_x)) {
                move_avatar(WEST);
            }
            else {
                move_avatar(EAST);
            }
        }
        else {
            if (y < (avatar_y - view_y)) {
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
    // TODO: Why is this sometimes black?
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

const url = '/utopia/api/v0/session.json'
fetch(url)
    .then(data => data.json())
    .then((json) => {
        map = json.map
        is_traverseable = json.map.isTraverseable;
        avatar_x = json.user.x - map.startX;
        avatar_y = json.user.y - map.startY;
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
                promises.push(loadImage('/utopia/api/v0/tile.png?id=' + tileId));
            }
        });
        
        Promise.allSettled(promises).then(results => {
            // @todo Error handling. See promise.status.
            results.forEach((promise, index) => {
                images.set(tileIds[index], promise.value);
            });
            
            on_load();
        })
        
        document.onkeydown = checkKey;
        window.addEventListener("resize", resize_map);
})


//document.addEventListener('touchstart', function (e) {
//    e.preventDefault();
//});

function checkKey(e) {
    e = e || window.event;

    if (e.keyCode == '38') {
        move_avatar(NORTH);
    }
    else if (e.keyCode == '40') {
        move_avatar(SOUTH);            
    }
    else if (e.keyCode == '37') {
        move_avatar(WEST);           
    }
    else if (e.keyCode == '39') {
        move_avatar(EAST);
    }
}
