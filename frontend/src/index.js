////////////////////////////////////////////////////////////////////////////////
import {Grid, Astar} from 'fast-astar';
import {GameMap} from './game-map';

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
var images = new Map();

var visibility;

// Frame rate when moving avatar
const MOVE_FRAME_COUNT_PER_SECOND = 60;

// Number of pixels to move avatar per frame. Should be a divisor of
// the tile size.
const MOVE_PIXEL_COUNT = 4;

// The current direction to move towards.
var targetMoveDirection = null;

// The current position and path to move towards.
var targetMovePosition = null;
var targetMovePath = null;

var move_interval = null;

function isTraverseable(pixel_x, pixel_y) {
    var x_floor = Math.floor(pixel_x / tile_size);
    var x_ceil = Math.ceil(pixel_x / tile_size);
    var y_floor = Math.floor(pixel_y / tile_size);
    var y_ceil = Math.ceil(pixel_y / tile_size);     

    return (map.isTileTraverseable(x_floor, y_floor) &&
            map.isTileTraverseable(x_floor, y_ceil) &&
            map.isTileTraverseable(x_ceil, y_floor) &&
            map.isTileTraverseable(x_ceil, y_ceil));
}

function draw_over_avatar(ctx, old_avatar_pixel_x, old_avatar_pixel_y) {
    // @todo This is excessive. At most we need to redraw two tiles and
    // not even the entire two tiles.
    var x_floor = Math.floor(old_avatar_pixel_x / tile_size);
    var x_ceil = Math.ceil(old_avatar_pixel_x / tile_size);
    var y_floor = Math.floor(old_avatar_pixel_y / tile_size);
    var y_ceil = Math.ceil(old_avatar_pixel_y / tile_size);     

    const coordinates = [[x_floor, y_floor],
                         [x_floor, y_ceil],
                         [x_ceil, y_floor],
                         [x_ceil, y_ceil]];
    coordinates.forEach(coordinate => {
        var tile = map.getTileIds(coordinate[0], coordinate[1])
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

function is_tile_visible(x, y) {
    return visibility[y][x]
}

function draw_tile(ctx, tile, x, y, tile_x_offset, tile_y_offset, tile_width, tile_height) {
    // @todo forEach
    for (var i = 0; i < tile.length; i++) {
        var tileId = tile[i]

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
        var tile = tiles[i]
        var x = tile[0]
        var y = tile[1]
        var tile = map.getTileIds(x, y);

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

        // Position the initial view such that the avatar is in the middle
        // of the screen.
        view_pixel_x = Math.floor(avatar_pixel_x / tile_size - view_width / 2) * tile_size
        view_pixel_y = Math.floor(avatar_pixel_y / tile_size - view_height / 2) * tile_size

        // Make sure that the view is not positioned before the start of the
        // map.
        view_pixel_x = Math.max(view_pixel_x, 0)
        view_pixel_y = Math.max(view_pixel_y, 0)

        // Make sure that the view is not positioned such that the view extends
        // past the edge of the map.
        view_pixel_x = Math.min(view_pixel_x, map.width * tile_size - screen_width)        
        view_pixel_y = Math.min(view_pixel_y, map.height * tile_size - screen_height)

        // Make sure that the view is positioned modulo the pixel movement
        // amount so that the view is properly aligned to avatar movement.
        view_pixel_x = view_pixel_x - view_pixel_x % MOVE_PIXEL_COUNT
        view_pixel_y = view_pixel_y - view_pixel_y % MOVE_PIXEL_COUNT        
    }

    var view_x = Math.floor(view_pixel_x / tile_size)
    var view_y = Math.floor(view_pixel_y / tile_size)
    
    var tile_y_offset = 0

    for (var i = 0; tile_y_offset < height; i++) {

        var source_tile_y_offset
        var tile_height
        
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
        
        var tile_x_offset = 0

        for (var j = 0; tile_x_offset < width; j++) {

            var source_tile_x_offset
            var tile_width
            
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

            var tile = map.getTileIds(view_x + j, view_y + i)
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
    var element = document.getElementById('main')
    var ctx = element.getContext('2d');
    var img_x_offset
    var img_y_offset    
    
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

function moveAvatar(avatar_direction) {
    last_avatar_direction = avatar_direction;

    var element = document.getElementById('main')
    var ctx = element.getContext('2d');
    var move_view = false;
    
    var new_avatar_pixel_x = avatar_pixel_x
    var new_avatar_pixel_y = avatar_pixel_y

    var new_view_pixel_x = view_pixel_x
    var new_view_pixel_y = view_pixel_y
    
    if (avatar_direction == NORTH) {
        if (avatar_pixel_y > 0) {
            new_avatar_pixel_y = new_avatar_pixel_y - MOVE_PIXEL_COUNT
            
            if (view_pixel_y > 0 && Math.round((avatar_pixel_y - view_pixel_y) / tile_size) < avatar_margin) {
                new_view_pixel_y -= MOVE_PIXEL_COUNT;
                move_view = true;
            }
        }
    }
    else if (avatar_direction == WEST) {
        if (avatar_pixel_x > 0) {
            new_avatar_pixel_x = new_avatar_pixel_x - MOVE_PIXEL_COUNT
            
            if (view_pixel_x > 0 && Math.round((avatar_pixel_x - view_pixel_x) / tile_size) < avatar_margin) {
                new_view_pixel_x -= MOVE_PIXEL_COUNT;
                move_view = true;
            }
        }
    }
    else if (avatar_direction == SOUTH) {
        if (avatar_pixel_y < map.height * tile_size) {
            new_avatar_pixel_y = new_avatar_pixel_y + MOVE_PIXEL_COUNT

            if (view_pixel_y < (map.height * tile_size - screen_height)) {
                if ((view_height - Math.round((avatar_pixel_y - view_pixel_y) / tile_size)) < avatar_margin) {
                    new_view_pixel_y += MOVE_PIXEL_COUNT;
                    move_view = true;
                }
            }
        }
    }
    else {
        if (avatar_pixel_x < map.width * tile_size) {
            new_avatar_pixel_x = new_avatar_pixel_x + MOVE_PIXEL_COUNT

            if (view_pixel_x < (map.width * tile_size - screen_width)) {
                if ((view_width - Math.round((avatar_pixel_x - view_pixel_x) / tile_size)) < avatar_margin) {
                    new_view_pixel_x += MOVE_PIXEL_COUNT;
                    move_view = true;
                }
            }
        }
    }

    if (isTraverseable(new_avatar_pixel_x, new_avatar_pixel_y)) {
        var old_avatar_pixel_x = avatar_pixel_x
        var old_avatar_pixel_y = avatar_pixel_y
        
        avatar_pixel_x = new_avatar_pixel_x
        avatar_pixel_y = new_avatar_pixel_y
        
        view_pixel_x = new_view_pixel_x
        view_pixel_y = new_view_pixel_y

        var newly_visible_tiles = update_visibility()
        
        if (move_view) {
            draw_map()
        }
        else {
            // Cover up old avatar position.
            draw_over_avatar(ctx, old_avatar_pixel_x, old_avatar_pixel_y)

            // Draw newly visible tiles
            draw_tiles(ctx, newly_visible_tiles);
        }

        var offset_newly_visible_tiles =
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
    canvas.addEventListener("touchstart", screenTouched);    
    update_visibility();
    draw();
}

var resize_timeout;

function windowResized() {
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
        map = new GameMap(json.map.startX,
                          json.map.startY,
                          json.map.width,
                          json.map.height,
                          json.map.tiles,
                          json.map.isTraverseable);
        avatar_pixel_x = (json.user.x - map.startX) * tile_size;
        avatar_pixel_y = (json.user.y - map.startY) * tile_size;
        last_avatar_direction = json.user.direction;
        visibility = json.traversal.hasSeen;

        var tileIds = map.getUniqueTileIds()
        tileIds.forEach(tileId => {
            images.set(tileId, null);
        });
        
        // Load tiles.
        var promises = [];
        
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
        
        document.onkeydown = keyPressed;
        document.onkeyup = keyReleased;
        document.addEventListener("click", mouseButtonClicked);
        window.addEventListener("resize", windowResized);
})

/**
 * Move towards the current target which could either be a direction
 * (e.g. North), or a position with a set of waypoints to follow.
 */
function moveAvatarTowardsTarget() {
    if (targetMoveDirection != null) {
        moveAvatar(targetMoveDirection);
    }
    else {
        // Remove the next waypoint on the path if we've reached it.
        var nextWaypoint = targetMovePath[0];
        if (avatar_pixel_x == nextWaypoint[0] &&
            avatar_pixel_y == nextWaypoint[1]) {
            targetMovePath.shift();
        }
        
        if (targetMovePath.length > 0) {
            // Move towards the next if there is any.
            nextWaypoint = targetMovePath[0];
            moveAvatar(findDirection([avatar_pixel_x, avatar_pixel_y],
                                     nextWaypoint))
        }
        else {
            // We can stop if we are already there.            
            stopMovingAvatar();
        }
    }
}

function startMovingAvatarTowardsPosition(x, y) {
    // @todo Ignore if we are already moving towards the given position.
    
    // @todo Cache grid.
    var grid = new Grid({
        col:view_width,
        row:view_height
    });

    // @todo We should include partial tiles at the edges of the screen
    // if they are mostly visible.
    var startX = Math.ceil(view_pixel_x / tile_size);
    var startY = Math.ceil(view_pixel_y / tile_size);
    
    for (var viewX = startX; viewX < startX + view_width; viewX++) {
        for (var viewY = startY; viewY < startY + view_height; viewY++) {
            // Any tile that is not visible we assume is blocked. 
            if (!visibility[viewY][viewX] || !map.isTileTraverseable(viewX, viewY)) {
                grid.set([viewX - startX, viewY - startY],'value',1);
            }
        }
    }

    var avatarStartX = Math.round(avatar_pixel_x / tile_size) - startX;
    var avatarStartY = Math.round(avatar_pixel_y / tile_size) - startY;
    var avatarEndX = Math.floor((view_pixel_x + x) / tile_size) - startX;
    var avatarEndY = Math.floor((view_pixel_y + y) / tile_size) - startY;

    // @todo There is a bug here where avatarEndX/Y may be past local search
    // window. We should expand the window and/or ignore the request.
    
    // Use the A-Star algorithm to find a path between the avatar's
    // current position and the given position.
    var astar = new Astar(grid);
    var waypoints = astar.search(
        [avatarStartX, avatarStartY],
        [avatarEndX, avatarEndY],
        {
            rightAngle:true,
            optimalResult:false
        }
    );

    if (!waypoints) {
        // Return if we can't find a path.
        // @todo A better approach would be to move in the right direction
        // and re-try as more tiles become visible.
        return;
    }

    targetMovePosition = [x, y];

    // Convert coordinates from local path finding map to actual map.
    targetMovePath = waypoints.map(function(waypoint) {
        return [(waypoint[0] + startX) * tile_size,
                (waypoint[1] + startY) * tile_size];
    });

    // @todo We should do the first move now.
    
    startAvatarMovingTimerr();
}

function startMovingAvatarTowardsDirection(direction) {
    // Nothing more to do if we are already moving in the same direction.
    if (direction == targetMoveDirection) return;
    targetMoveDirection = direction;
    targetMovePosition = null;
    targetMovePath = null;
    
    moveAvatarTowardsTarget();
    startAvatarMovingTimerr();
}

function startAvatarMovingTimerr() {
    if (move_interval == null) {
        move_interval =
            setInterval(moveAvatarTowardsTarget, 1000 / MOVE_FRAME_COUNT_PER_SECOND);
    }
}

function stopMovingAvatar() {
    if (move_interval != null) {
        clearInterval(move_interval);
        move_interval = null;
        targetMoveDirection = null;
        targetMovePosition = null;
        targetMovePath = null;
    }
}

/**
 * Given a start coordindate and an end coordinate, return a direction
 * we would need to travel to go from the start to the end coordinate.
 */
function findDirection(start, end) {
    // @todo Pick the largest difference.
    if (start[0] < end[0]) {
        return EAST;
    }
    else if (start[0] > end[0]) {
        return WEST;
    }
    else if (start[1] < end[1]) {
        return SOUTH;
    }
    else {
        return NORTH;
    }
}

/**
 * Convert an array key code (i.e. up, down, left, right) to a direction
 * that we should move in (e.g. north, south, east, west).
 */
function arrowKeyCodeToDirection(keyCode) {
    if (keyCode == ARROW_UP) {    
        return NORTH;
    }
    else if (keyCode == ARROW_DOWN) {
        return SOUTH;
    }
    else if (keyCode == ARROW_LEFT) {
        return WEST;
    }
    else {
        return EAST;
    }
}

function keyPressed(event) {
    event = event || window.event;

    if (event.keyCode == ARROW_UP ||
        event.keyCode == ARROW_DOWN ||
        event.keyCode == ARROW_LEFT ||
        event.keyCode == ARROW_RIGHT) {
        startMovingAvatarTowardsDirection(arrowKeyCodeToDirection(event.keyCode));
    }
    else {
        stopMovingAvatar();
    }
}

function keyReleased(event) {
    stopMovingAvatar();
}

function screenTouched(event) {
    // @todo If the user drags their fingers across the screen we could follow
    // that path.
    var touch = event.touches[0];
    startMovingAvatarTowardsPosition(touch.clientX, touch.clientY);
}

function mouseButtonClicked(event) {
    startMovingAvatarTowardsPosition(event.clientX, event.clientY)
}
