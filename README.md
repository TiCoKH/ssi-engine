About
=====

This project tries to create an engine that can play all
SSI Goldbox game series including content created by FRUA.

At the moment of this writing only Buck Rogers: Countdown to Doomsday
will show something.

Most information of the inner workings of these games has been retrieved from

[Goldbox Explorer](https://github.com/simeonpilgrim/goldboxexplorer)

[Curse of the Azure Bonds](https://github.com/simeonpilgrim/coab)

Building
========

Requirements:

* Maven 3
* Java Development Kit 8

Run
`mvn clean package -Dmaven.test.skip=true`
in the project directory.

Running
=======

From the project directory run the following command:
`java -jar ./target/engine-SNAPSHOT.jar <directory> [-noShowTitle]`
where `<directory>` is the one in which the game data for
Buck Rogers 1 resides and noShowTitle argument makes the engine
skip the screens before the title menu.

Make sure all game data filenames are uppercase.

In the title menu use

* D to start the demo
* G to start the game

In a dungeon use

* WSAD to move
* Ctrl-S to save

Ctrl-L will load the game,
Ctrl-Q will quit anywhere.
