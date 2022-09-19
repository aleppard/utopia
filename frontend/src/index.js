////////////////////////////////////////////////////////////////////////////////
import {Grid, Astar} from 'fast-astar';

import * as Configuration from './configuration';
import * as Direction from './direction';
import * as Key from './key';
import {AvatarImages} from './avatar-images';
import {Session} from './session';
import {TileImages} from './tile-images';

// Offset of view from start of map.
var viewPixelX = null;
var viewPixelY = null;

var view_width;
var view_height;
var screen_width;
var screen_height;

// @todo use "tile_x" vs. "pixel_x".
var avatarPixelX;
var avatarPixelY;

var lastAvatarDirection;
var avatar_margin = 5;

var session;
var avatarImages = new AvatarImages();
var tileImages = new TileImages();

// The current direction to move towards.
var targetMoveDirection = null;

// The current position and path to move towards.
var targetMovePosition = null;
var targetMovePath = null;

var move_interval = null;

var isMapMissingTiles = false;

function isTraverseable(pixel_x, pixel_y) {
    var x_floor = Math.floor(pixel_x / Configuration.TILE_SIZE);
    var x_ceil = Math.ceil(pixel_x / Configuration.TILE_SIZE);
    var y_floor = Math.floor(pixel_y / Configuration.TILE_SIZE);
    var y_ceil = Math.ceil(pixel_y / Configuration.TILE_SIZE);     

    return (session.map.isTileTraverseable(x_floor, y_floor) &&
            session.map.isTileTraverseable(x_floor, y_ceil) &&
            session.map.isTileTraverseable(x_ceil, y_floor) &&
            session.map.isTileTraverseable(x_ceil, y_ceil));
}

function draw_over_avatar(ctx, oldAvatarPixelX, oldAvatarPixelY) {
    // @todo This is excessive. At most we need to redraw two tiles and
    // not even the entire two tiles.
    var x_floor = Math.floor(oldAvatarPixelX / Configuration.TILE_SIZE);
    var x_ceil = Math.ceil(oldAvatarPixelX / Configuration.TILE_SIZE);
    var y_floor = Math.floor(oldAvatarPixelY / Configuration.TILE_SIZE);
    var y_ceil = Math.ceil(oldAvatarPixelY / Configuration.TILE_SIZE);     

    const coordinates = [[x_floor, y_floor],
                         [x_floor, y_ceil],
                         [x_ceil, y_floor],
                         [x_ceil, y_ceil]];
    coordinates.forEach(coordinate => {
        var tile = session.map.getTileIds(coordinate[0], coordinate[1])
        draw_tile(ctx, tile,
                  coordinate[0] * Configuration.TILE_SIZE - viewPixelX,
                  coordinate[1] * Configuration.TILE_SIZE - viewPixelY,
                  0, 0,
                  Configuration.TILE_SIZE, Configuration.TILE_SIZE);
    });
}

function draw_tile(ctx, tile, x, y, tile_x_offset, tile_y_offset, tile_width, tile_height) {
    let isMissingTile = false;
    
    // @todo forEach
    for (var i = 0; i < tile.length; i++) {
        var tileId = tile[i]
        var tileImage = tileImages.get(tileId);

        if (!tileImage) {
            isMissingTile = true;
            continue;
        }
        
        // @todo Do we need the other arguments here?
        ctx.drawImage(tileImage,
                      tile_x_offset,
                      tile_y_offset,
                      tile_width, tile_height, x, y,
                      tile_width, tile_height);
    }

    return isMissingTile;
}

function draw_tiles(ctx, tiles) {
    let isMissingTile = false;
    
    // @todo forEach
    for (var i = 0; i < tiles.length; i++) {
        var tile = tiles[i]
        var x = tile[0]
        var y = tile[1]
        var tile = session.map.getTileIds(x, y);

        if (tile == null) {
            // The tile has not been loaded yet. Draw a black square.
            ctx.fillStyle = "rgb(0,0,0)";
            ctx.fillRect(x * Configuration.TILE_SIZE - viewPixelX,
                         y * Configuration.TILE_SIZE - viewPixelY,
                         x * Configuration.TILE_SIZE - viewPixelX + Configuration.TILE_SIZE,
                         y * Configuration.TILE_SIZE - viewPixelY + Configuration.TILE_SIZE);
            isMissingTile = true;
        }
        else {
            if (draw_tile(ctx, tile,
                          x * Configuration.TILE_SIZE - viewPixelX,
                          y * Configuration.TILE_SIZE - viewPixelY,
                          0, 0,
                          Configuration.TILE_SIZE, Configuration.TILE_SIZE)) {
                isMissingTile = true;
            }
        }
    }

    return isMissingTile;
}

function drawMap() {
    isMapMissingTiles = false;
    
    var element = document.getElementById('main')
    var ctx = element.getContext('2d');
    element.width = window.innerWidth;
    element.height = window.innerHeight;

    var positionInfo = element.getBoundingClientRect();

    var height = positionInfo.height;
    var width = positionInfo.width;
    let isNewView = !screen_width;
    let hasViewChanged = false;

    if (screen_width != width || screen_height != height) {
        screen_width = width;
        screen_height = height;
        view_width = Math.floor(width / Configuration.TILE_SIZE);
        view_height = Math.floor(height / Configuration.TILE_SIZE);
        hasViewChanged = true;
    }

    // Position the initial view such that the avatar is in the middle
    // of the screen.
    if (isNewView) {
        viewPixelX = Math.floor(avatarPixelX / Configuration.TILE_SIZE - view_width / 2) * Configuration.TILE_SIZE
        viewPixelY = Math.floor(avatarPixelY / Configuration.TILE_SIZE - view_height / 2) * Configuration.TILE_SIZE
    }
    
    if (isNewView || hasViewChanged) {
        // Make sure that the view is not positioned before the start of the
        // map.
        viewPixelX = Math.max(viewPixelX, 0)
        viewPixelY = Math.max(viewPixelY, 0)

        // Make sure that the view is not positioned such that the view extends
        // past the edge of the map.
        viewPixelX = Math.min(viewPixelX,
                              session.map.width * Configuration.TILE_SIZE - screen_width)        
        viewPixelY = Math.min(viewPixelY,
                              session.map.height * Configuration.TILE_SIZE - screen_height)

        // Make sure that the view is positioned modulo the pixel movement
        // amount so that the view is properly aligned to avatar movement.
        viewPixelX = viewPixelX - viewPixelX % Configuration.MOVE_PIXEL_COUNT
        viewPixelY = viewPixelY - viewPixelY % Configuration.MOVE_PIXEL_COUNT
    }

    var view_x = Math.floor(viewPixelX / Configuration.TILE_SIZE)
    var view_y = Math.floor(viewPixelY / Configuration.TILE_SIZE)
    
    var tile_y_offset = 0

    for (var i = 0; tile_y_offset < height; i++) {

        var source_tile_y_offset
        var tile_height
        
        if (i == 0) {
            source_tile_y_offset = viewPixelY % Configuration.TILE_SIZE
            tile_height = Configuration.TILE_SIZE - source_tile_y_offset
        }
        else if (tile_y_offset + Configuration.TILE_SIZE >= height) {
            source_tile_y_offset = 0            
            tile_height = height - tile_y_offset
        }
        else {
            source_tile_y_offset = 0
            tile_height = Configuration.TILE_SIZE
        }
        
        var tile_x_offset = 0

        for (var j = 0; tile_x_offset < width; j++) {

            var source_tile_x_offset
            var tile_width
            
            if (j == 0) {
                source_tile_x_offset = viewPixelX % Configuration.TILE_SIZE
                tile_width = Configuration.TILE_SIZE - source_tile_x_offset
            }
            else if (tile_x_offset + Configuration.TILE_SIZE >= width) {
                source_tile_x_offset = 0                
                tile_width = width - tile_x_offset
            }
            else {
                source_tile_x_offset = 0
                tile_width = Configuration.TILE_SIZE
            }

            var tile = session.map.getTileIds(view_x + j, view_y + i)
            const hasSeenTile =
                  session.traversal.hasSeenTile(view_x + j, view_y + i);
            if (hasSeenTile == null) {
                isMapMissingTiles = true;
            }
            else if (hasSeenTile) {
                if (draw_tile(ctx, tile, tile_x_offset, tile_y_offset,
                              source_tile_x_offset, source_tile_y_offset,
                              tile_width, tile_height)) {
                    isMapMissingTiles = true;
                }
            }

            tile_x_offset += tile_width
        }

        tile_y_offset += tile_height
    }
}

function drawAvatar() {
    let element = document.getElementById('main')
    let ctx = element.getContext('2d');
    const avatarRegion = avatarImages.getRegion(lastAvatarDirection);
    
    ctx.drawImage(avatarImages.get(session.avatarId),
                  avatarRegion.startX,
                  avatarRegion.startY,
                  avatarRegion.width,
                  avatarRegion.height,
                  avatarPixelX - viewPixelX,
                  avatarPixelY - viewPixelY,
                  Configuration.TILE_SIZE,
                  Configuration.TILE_SIZE);    
}

function moveAvatar(avatar_direction) {
    lastAvatarDirection = avatar_direction;

    var element = document.getElementById('main')
    var ctx = element.getContext('2d');
    var move_view = false;
    
    var new_avatarPixelX = avatarPixelX
    var new_avatarPixelY = avatarPixelY

    var new_viewPixelX = viewPixelX
    var new_viewPixelY = viewPixelY
    
    if (avatar_direction == Direction.NORTH) {
        if (avatarPixelY > 0) {
            new_avatarPixelY = new_avatarPixelY - Configuration.MOVE_PIXEL_COUNT
            
            if (viewPixelY > 0 && Math.round((avatarPixelY - viewPixelY) / Configuration.TILE_SIZE) < avatar_margin) {
                new_viewPixelY -= Configuration.MOVE_PIXEL_COUNT;
                move_view = true;
            }
        }
    }
    else if (avatar_direction == Direction.WEST) {
        if (avatarPixelX > 0) {
            new_avatarPixelX = new_avatarPixelX - Configuration.MOVE_PIXEL_COUNT
            
            if (viewPixelX > 0 && Math.round((avatarPixelX - viewPixelX) / Configuration.TILE_SIZE) < avatar_margin) {
                new_viewPixelX -= Configuration.MOVE_PIXEL_COUNT;
                move_view = true;
            }
        }
    }
    else if (avatar_direction == Direction.SOUTH) {
        if (avatarPixelY < session.map.height * Configuration.TILE_SIZE) {
            new_avatarPixelY = new_avatarPixelY + Configuration.MOVE_PIXEL_COUNT

            if (viewPixelY < (session.map.height * Configuration.TILE_SIZE - screen_height)) {
                if ((view_height - Math.round((avatarPixelY - viewPixelY) / Configuration.TILE_SIZE)) < avatar_margin) {
                    new_viewPixelY += Configuration.MOVE_PIXEL_COUNT;
                    move_view = true;
                }
            }
        }
    }
    else {
        if (avatarPixelX < session.map.width * Configuration.TILE_SIZE) {
            new_avatarPixelX = new_avatarPixelX + Configuration.MOVE_PIXEL_COUNT

            if (viewPixelX < (session.map.width * Configuration.TILE_SIZE - screen_width)) {
                if ((view_width - Math.round((avatarPixelX - viewPixelX) / Configuration.TILE_SIZE)) < avatar_margin) {
                    new_viewPixelX += Configuration.MOVE_PIXEL_COUNT;
                    move_view = true;
                }
            }
        }
    }

    if (isTraverseable(new_avatarPixelX, new_avatarPixelY)) {
        var oldAvatarPixelX = avatarPixelX
        var oldAvatarPixelY = avatarPixelY

        avatarPixelX = new_avatarPixelX
        avatarPixelY = new_avatarPixelY

        let avatarX = Math.round(avatarPixelX / Configuration.TILE_SIZE);
        let avatarY = Math.round(avatarPixelY / Configuration.TILE_SIZE);
        
        viewPixelX = new_viewPixelX
        viewPixelY = new_viewPixelY

        var newTilesSeen =
            session.traversal.updateTilesSeen(avatarX, avatarY);
        
        if (move_view) {
            drawMap();
        }
        else {
            // Cover up old avatar position.
            draw_over_avatar(ctx, oldAvatarPixelX, oldAvatarPixelY)
            
            // Draw newly visible tiles
            draw_tiles(ctx, newTilesSeen);
        }
        
        // If the avatar has crossed a tile boundary then we need to send the
        // new location to the server and potential load new parts of the map
        // that might soon become visible.
        if (Math.round(oldAvatarPixelX / Configuration.TILE_SIZE) != avatarX ||
            Math.round(oldAvatarPixelY / Configuration.TILE_SIZE) != avatarY) {
            // @todo We perform these two calls to the server together.
            // Can we combine them?
            session.saveTraversal(newTilesSeen, avatarX, avatarY, lastAvatarDirection)
            session.loadNearbyUnloadedRegions(avatarX,
                                              avatarY,
                                              view_width,
                                              view_height)
                .then(() => {
                    // @todo Load any new tiles.

                    // If the view is missing tiles because they haven't
                    // been loaded yet redraw the view.
                    if (isMapMissingTiles) {
                        // @todo Only need to draw new tiles (if any).
                        draw();
                    }
                })
        }
    }
    
    drawAvatar()
}

function draw() {
    drawMap();
    drawAvatar();
}

var resize_timeout;

function windowResized() {
    clearTimeout(resize_timeout);
    resize_timeout = setTimeout(draw, 100);    
}

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
        if (avatarPixelX == nextWaypoint[0] &&
            avatarPixelY == nextWaypoint[1]) {
            targetMovePath.shift();
        }
        
        if (targetMovePath.length > 0) {
            // Move towards the next if there is any.
            nextWaypoint = targetMovePath[0];
            moveAvatar(Direction.find([avatarPixelX, avatarPixelY],
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
    var startX = Math.ceil(viewPixelX / Configuration.TILE_SIZE);
    var startY = Math.ceil(viewPixelY / Configuration.TILE_SIZE);
    
    for (var viewX = startX; viewX < startX + view_width; viewX++) {
        for (var viewY = startY; viewY < startY + view_height; viewY++) {
            // Any tile that is not visible we assume is blocked. 
            if (!session.traversal.hasSeenTile(viewX, viewY) ||
                !session.map.isTileTraverseable(viewX, viewY)) {
                grid.set([viewX - startX, viewY - startY],'value',1);
            }
        }
    }

    var avatarStartX = Math.round(avatarPixelX / Configuration.TILE_SIZE) - startX;
    var avatarStartY = Math.round(avatarPixelY / Configuration.TILE_SIZE) - startY;
    var avatarEndX = Math.floor((viewPixelX + x) / Configuration.TILE_SIZE) - startX;
    var avatarEndY = Math.floor((viewPixelY + y) / Configuration.TILE_SIZE) - startY;

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
        return [(waypoint[0] + startX) * Configuration.TILE_SIZE,
                (waypoint[1] + startY) * Configuration.TILE_SIZE];
    });

    // @todo We should do the first move now.
    
    startAvatarMovingTimer();
}

function startMovingAvatarTowardsDirection(direction) {
    // Nothing more to do if we are already moving in the same direction.
    if (direction == targetMoveDirection) return;
    targetMoveDirection = direction;
    targetMovePosition = null;
    targetMovePath = null;
    
    moveAvatarTowardsTarget();
    startAvatarMovingTimer();
}

function startAvatarMovingTimer() {
    if (move_interval == null) {
        move_interval =
            setInterval(moveAvatarTowardsTarget,
                        1000 / Configuration.MOVE_FRAME_COUNT_PER_SECOND);
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
 * Convert an array key code (i.e. up, down, left, right) to a direction
 * that we should move in (e.g. north, south, east, west).
 */
function arrowKeyToDirection(keyCode) {
    if (keyCode == Key.ARROW_UP) {    
        return Direction.NORTH;
    }
    else if (keyCode == Key.ARROW_DOWN) {
        return Direction.SOUTH;
    }
    else if (keyCode == Key.ARROW_LEFT) {
        return Direction.WEST;
    }
    else {
        return Direction.EAST;
    }
}

function keyPressed(event) {   
    event = event || window.event;

    if (Key.isArrowKey(event.keyCode)) {
        startMovingAvatarTowardsDirection(arrowKeyToDirection(event.keyCode));
    }
    else {
        stopMovingAvatar();
    }
}

function keyReleased(event) {
    stopMovingAvatar();
}

function screenTouched(event) {
    if (requestFullScreen()) return;
    
    // @todo If the user drags their fingers across the screen we could follow
    // that path.
    var touch = event.touches[0];
    startMovingAvatarTowardsPosition(touch.clientX, touch.clientY);
}

function mouseButtonClicked(event) {
    if (requestFullScreen()) return;
    
    startMovingAvatarTowardsPosition(event.clientX, event.clientY)
}

var hasRequestedFullScreen = false;
function requestFullScreen() {
    if (!hasRequestedFullScreen) {
        hasRequestedFullScreen = true;
        
        var element = document.documentElement;
        if (element.requestFullscreen) {
            element.requestFullscreen();
            return true;
        }
        else if (element.webkitRequestFullscreen) {
            element.webkitRequestFullscreen();
            return true;
        }
        else if (elemente.mozRequestFullScreen) {
            element.mozRequestFullScreen();
            return true;
        }
        else if (element.msRequestFullscreen) {
            element.msRequestFullscreen();
            return true;
        }
    }

    return false;
}

async function load() {
    const screenWidth = Math.floor(window.screen.availWidth / Configuration.TILE_SIZE);
    const screenHeight = Math.floor(window.screen.availHeight / Configuration.TILE_SIZE);    

    session = new Session();
    const tileIds = await session.initialise(screenWidth, screenHeight);
    
    avatarPixelX = session.avatarX * Configuration.TILE_SIZE;
    avatarPixelY = session.avatarY * Configuration.TILE_SIZE;
    lastAvatarDirection = session.avatarDirection;

    let promises = tileImages.loadAll(tileIds);
    promises.push(avatarImages.load(session.avatarId));
    return Promise.allSettled(promises);
}

load().then(() => {
    // Listen for touch events. Touching screen will move avatar.
    var canvas = document.getElementById('main')
    canvas.addEventListener("touchstart", screenTouched);

    // Also listen for keyboard, mouse and re-size events.
    document.onkeydown = keyPressed;
    document.onkeyup = keyReleased;
    document.addEventListener("click", mouseButtonClicked);
    
    window.addEventListener("resize", windowResized);

    // @todo This shouldn't be necessary.
    session.traversal.updateTilesSeen(Math.round(avatarPixelX / Configuration.TILE_SIZE),
                                      Math.round(avatarPixelY / Configuration.TILE_SIZE));
    draw();
});
