About
=====

This project tries to create an engine that can play all
SSI Goldbox game series including content created by FRUA.

All games except FRUA are playable to a varying degree. All games are still
missing party handling and combat mechanics as big features.

Most information of the inner workings of these games has been retrieved from

[Goldbox Explorer](https://github.com/simeonpilgrim/goldboxexplorer)

[Curse of the Azure Bonds](https://github.com/simeonpilgrim/coab)

Building
========

Requirements:

* Maven 3
* Java Development Kit 17

Run
`mvn clean package -Dmaven.test.skip=true`
in the project directory.

Running
=======

From the project directory run the following command:
`java -jar ./target/engine-SNAPSHOT.jar [<directory> [--no-title]]`
where `<directory>` contains the game data from any of the SSI goldbox games
and the --no-title argument skips the screens before the title menu.

In the title menu use

* D to start the demo
* G to start the game

In a dungeon use

* WSAD to move
* Ctrl-S to save

Ctrl-L will load the game,
Ctrl-Q will quit anywhere.
