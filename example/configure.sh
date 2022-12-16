# Script to configure avatars, tiles & the map with example assets.
# sh -x configure.sh <tomcat url> <utopia authorisation token>
curl -X POST -H "Authorization: Bearer $2" $1/api/v0/avatar.json -d @avatars.json

curl -X POST -H "Authorization: Bearer $2" -H "Content-Type: image/png" "$1/api/v0/avatar.png?id=1" --data-binary @assets/avatars.png

curl -X POST -H "Authorization: Bearer $2" $1/api/v0/tile_connector.json -d @tile_connectors.json
curl -X POST -H "Authorization: Bearer $2" $1/api/v0/tile.json -d @tiles.json

curl -X POST -H "Authorization: Bearer $2" -H "Content-Type: image/png" "$1/api/v0/tile.png?id=grass,small-mushrooms,mushroom,little-mushroom,clump,medium-clump,large-clump,lots-of-pebbles,small-pebble,medium-pebble,tree-top-left,tree-top-right,big-pebble,lillies,tree-bottom-left,tree-bottom-right&x=0&y=0&width=128&height=128" --data-binary @assets/tiles.png

curl -X POST -H "Authorization: Bearer $2" $1/api/v0/map.json -d @map.json
