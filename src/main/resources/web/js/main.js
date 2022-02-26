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
var view_x = 40;
var view_y = 40;

var view_width;
var view_height;

// Offset of avatar from start of map.
var avatar_x = view_x + 7;
var avatar_y = view_y + 7;

// 1 - North
// 2 - East
// 3 - South
// 4 - West
var last_avatar_direction = 3; // South
var avatar_margin = 5;

var map_width = 100
var map_height = 100
var MAP;
var is_traverseable;
var images = new Map();

var visibility = Array(map_width * map_height).fill(0);

function update_visibility() {
    var made_visible = [];
    // TODO: This could be made much faster!
    var MARGIN = 5;

    // TODO: These +1s and -1s everywhere indicate an off-by-one error somewhere.
    var centre_x = avatar_x + 1
    var centre_y = avatar_y + 1

    for (var x = centre_x - MARGIN; x <= centre_x + MARGIN; x++) {
        for (var y = centre_y - MARGIN; y <= centre_y + MARGIN; y++) {
            if (x > 0 && y > 0 && x < map_width && y < map_height) {
                if (!visibility[x + y * map_width]) {                
                    if (Math.sqrt(Math.pow(centre_x - x, 2) +
                                  Math.pow(centre_y - y, 2)) < MARGIN) {
                        // TODO: Use line of sight calculations.
                        visibility[x + y * map_width] = 1
                        made_visible.push([x, y])
                    }
                }
            }
        }
    }

    return made_visible
}

function get_tile(x, y) {
    return MAP[y][x]
}

function is_tile_visible(x, y) {
    return visibility[x + y * map_width];
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

    if (last_avatar_direction == 1) {
        img_x_offset = 9 * tile_size
        img_y_offset = 7 * tile_size
    }
    else if (last_avatar_direction == 2) {
        img_x_offset = 9 * tile_size
        img_y_offset = 5 * tile_size
    }
    else if (last_avatar_direction == 3) {
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
    
    if (avatar_direction == 1) {
        if (avatar_y > 0) {
            new_avatar_y--;
            
            if (view_y > 0 && ((avatar_y - view_y) < avatar_margin)) {
                new_view_y--;
                move_view = true;
            }
        }
    }
    else if (avatar_direction == 2) {
        if (avatar_x > 0) {
            new_avatar_x--;
            
            if (view_x > 0 && ((avatar_x - view_x) < avatar_margin)) {
                new_view_x--;
                move_view = true;
            }
        }
    }
    else if (avatar_direction == 3) {
        if (avatar_y < map_height - 3) {
            new_avatar_y++;

            if (view_y < (map_height - view_height)) {
                if ((view_y + view_height - avatar_y) < avatar_margin) {
                    new_view_y++;
                    move_view = true;
                }
            }
        }
    }
    else {
        if (avatar_x < map_width - 3) {
            new_avatar_x++;

            if (view_x < (map_width - view_width)) {
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
                move_avatar(2);
            }
            else {
                move_avatar(4);
            }
        }
        else {
            if (y < (avatar_y - view_y)) {
                move_avatar(1);
            }
            else {
                move_avatar(3);
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

const url = '/utopia/api/v0/map.json'
fetch(url)
    .then(data => data.json())
    .then((json) => {
        MAP = json.tiles
        is_traverseable = json.isTraverseable
        
        // Find unique tiles.
        for (var y = 0; y < map_height; y++) {
            for (var x = 0; x < map_width; x++) {
                tile_list = MAP[y][x]
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
        move_avatar(1);
    }
    else if (e.keyCode == '40') {
        move_avatar(3);            
    }
    else if (e.keyCode == '37') {
        move_avatar(2);           
    }
    else if (e.keyCode == '39') {
        move_avatar(4);
    }
}
