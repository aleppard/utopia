# Utopia 2D web tile engine

Utopia is a 2D tile engine for the web that can be used as a platform for web game development or AI exploration. Utopia is written in Javascript for the front-end, and in Java for the back-end. The back-end runs on [Apache Tomcat](https://tomcat.apache.org/). Utopia's philosophy is:

1. To provide an instant game experience for the visitor with little to no wait time, and no need to create an account or sign-up.
2. To leverage AI to create an extremely immersive and flexible game experience.

![Screenshot!](/screenshot.png)

## Features

* Supports smooth scrolling of a very large map. The map is loaded on-demand  to remove the need to wait for the entire map to be loaded.
* Supports only showing the part of the map that a visitor has explored.
* Supports remembering visitors' map traversal and last location and direction.
* Supports multiple avatar images.
* Supports building up the map by overlaying multiple tiles.
* Supports movement by arrow keys, touch or mouse click. The latter two uses a path finding algorithm to negotiate to the selected point.
* Supports full screen mode.

## Future Improvements

Future improvements could include:

* Better API support to integrate and train AI agents.
* AI model to translate commands into acions.
* AI model to translate map into scene descriptions.
* Support for NPCs and animals.
* Map creator.
* Freely available, high quality game assets such as avatars and tiles.
* Multi-player
* ...

## How To Build

To build you will need maven and jdk8+.

You can build with the following command:

    mvn package

## How To Run

### Docker

The simplest way to run is using Docker. Install [Docker](https://www.docker.com/), build as above, and run the following command:

    docker-compose -f docker-compose.yml up
  
or

    sudo docker-compose -f docker-compose.yml up

To start in the background, and to stop you can run respectively:

    docker-compose -f docker-compose.yml up -d
    docker-compose -f docker-compose.yml down

Once running you can configure it (e.g. uploading the map) using the following example configuration script (you will need to install [curl](https://curl.se/) to run):

     cd example
     sh -x configure.sh localhost:80 b2c401cdf2236c1762c173fab87c33a3

You can then visit <http://localhost> in your browser.

### Deploy to Tomcat

The best way for development is to deploy to a local Tomcat server.

Build as above.
Install and configure [Apache Tomcat](https://tomcat.apache.org/).
Install and configure [Postgres](https://www.postgresql.org/).
Create ~/.m2/settings.xml and set Tomcat and Postgres credentials. See example/settings.xml for an example file.

The first build and deployment can be made by running:

    mvn package
    mvn cargo:deploy 
    
Then to re-deploy:
    
    mvn package
    mvn cargo:redeploy 

Pass -P argument to the deploy command to deploy to a production instance, otherwise it will deploy to the development instance. See example_settings.xml for more information.

Once running you can configure it (e.g. uploading the map) using the following example configuration script (you will need to install [curl](https://curl.se/) to run):

     cd example
     sh -x configure.sh <tomcat url> <utopia authorisation token>

