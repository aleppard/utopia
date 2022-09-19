////////////////////////////////////////////////////////////////////////////////

import {calculate} from './load-region';
import {GameMap} from './game-map';
import {Region} from './region';
import {Traversal} from './traversal';

const SESSION_URL = "/api/v0/session.json";

/**
 * Manages the user's session with the remote server including:
 *
 * - Creating a new game or resuming an existing game.
 * - Loading the user's location traversal map from the server, and updating it.
 * - Loading the part of the game map that is required based on the user's
 * - location.
 */
export class Session {
    constructor() {
        this.mapStartX = null;
        this.mapStartY = null;
        this.mapWidth = null;
        this.mapHeight = null;
        this.map = null;
        this.traversal = null;
        this.avatarX = null;
        this.avatarY = null;
        this.avatarDirection = null;
        this.avatarId = null;

        /** Set of regions we are currently loading from the server. */
        this.regionsLoading = new Set();
    }

    /**
     * Create a new game or resume an existing game.
     *
     * @return the list of unique tile IDs required to load. 
     */
    initialise(screenWidth, screenHeight) {
        return fetch(SESSION_URL + "?" + new URLSearchParams({
            screen_width: screenWidth,
            screen_height: screenHeight}), {
                method:'POST'
            })
            .then(data => data.json())
            .then((json) => {
                this.mapStartX = json.mapBounds.startX;
                this.mapStartY = json.mapBounds.startY;        
                this.mapWidth = json.mapBounds.width;
                this.mapHeight = json.mapBounds.height;
            
                this.map = new GameMap(json.mapBounds.width,
                                       json.mapBounds.height);
                this.map.setTiles(json.map.startX - this.mapStartX,
                                  json.map.startY - this.mapStartY,
                                  json.map.tiles);
                this.map.setTileTraversability(json.map.startX - this.mapStartX,
                                               json.map.startY - this.mapStartY,
                                               json.map.isTraverseable);
                this.traversal = new Traversal(json.mapBounds.width,
                                               json.mapBounds.height);
                this.traversal.setHasSeen(json.map.startX - this.mapStartX,
                                          json.map.startY - this.mapStartY,
                                          json.traversal.hasSeen);
                this.avatarX = json.user.x - this.mapStartX;
                this.avatarY = json.user.y - this.mapStartY;
                this.avatarDirection = json.user.direction;
                this.avatarId = json.user.avatarId;
                
                return this.map.getUniqueTileIds
                (json.map.startX - this.mapStartX,
                 json.map.startY - this.mapStartY,
                 json.map.width,
                 json.map.height);
            });
    }

    /**
     * Given the location of the avatar and the view's width and height
     * load any nearby regions that aren't already loaded. This way the
     * user can roam a large map and we can load regions we need just before
     * we need them.
     */
    loadNearbyUnloadedRegions(avatarX, avatarY, viewWidth, viewHeight) {
        let region = calculate(new Region(0,
                                          0,
                                          this.mapWidth,
                                          this.mapHeight),
                               10, // MYTODO quad size
                               avatarX,
                               avatarY,
                               viewWidth,
                               viewHeight);

        // @todo Pass Region here.
        let unloadedRegions = this.map.tiles.getNotPresentRegions(region.startX,
                                                                  region.startY,
                                                                  region.width,
                                                                  region.height);

        let promises = []
        unloadedRegions.forEach((region) => {
            if (!(region in this.regionsLoading)) {            
                promises.push(this.loadRegion(region));
            }
        });

        return Promise.allSettled(promises);
    }

    loadRegion(region) {
        this.regionsLoading.add(region);

        return fetch(SESSION_URL + "?" + new URLSearchParams({
            start_x: region.startX + this.mapStartX,
            start_y: region.startY + this.mapStartY,
            width: region.width,
            height: region.height
        }))
            .then(data => data.json())
            .then((json) => {
                this.map.setTiles(region.startX, region.startY,
                                  json.map.tiles);
                this.map.setTileTraversability(region.startX, region.startY,
                                               json.map.isTraverseable);
                this.traversal.setHasSeen(region.startX, region.startY,
                                          json.traversal.hasSeen);
                this.regionsLoading.delete(region);
                return region;
            });
    }

    /** 
     * Save newly visible tiles and the avatar's current location and direction
     * with the server.
     */
    saveTraversal(newTilesSeen, avatarX, avatarY, avatarDirection) {
        this.avatarX = avatarX;
        this.avatarY = avatarY;
        this.avatarDirection = avatarDirection;
        
        var offsetNewTilesSeen =
            newTilesSeen.map(coordinate => [coordinate[0] + this.mapStartX,
                                            coordinate[1] + this.mapStartY])
        
        // @todo Pass pixel offset to server
        let request = { user: { x: avatarX + this.mapStartX,
                                y: avatarY + this.mapStartY,
                                direction: avatarDirection
                              },
                        traversal: { seen: offsetNewTilesSeen}}
        
        fetch(SESSION_URL,
              {
                  method:'PUT',
                  body:JSON.stringify(request)
              }).then();
    }
}
