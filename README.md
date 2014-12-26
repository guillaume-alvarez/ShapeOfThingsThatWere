Shape Of Things That Were
=========================

Open source strategy game

To start the game from sources:
 * checkout github repository
 * execute `gradlew.bat :desktop:run`

To create an executable jar:
 * checkout github repository
 * execute `gradlew.bat :desktop:dist`
 * jar is built in directory `desktop/build/libs/`

To start the game in Eclipse:
 * checkout github repository
 * execute `gradlew.bat eclipse` and import projects
 * or use Eclipse Gradle plugin to import projects
 * launch `desktop/ImagePacker.launch`
 * launch `desktop/Things That Were.launch`

## Design objectives

The goal is to produce a strategy video game. It should emphasize macro over micro management and chaos over stability.

### Macro management and Influence
The player should not be bothered with such trivial details as the construction on an aqueduct or granary. He should not have ot move its regiments around the map. He should not chose where his workers will exploit terrain.

To achieve this goal the central concept is Influence: the area that is (more or less) controlled by each empire. The different empires do fight certainly, but this fight consist in improving the influence on the surrounding terrain, or directing it on certain tiles.

### Chaotic system
Too many games produce stable and predictible situations. Stable situations are no fun. Predictible ones are so boring. The player should be forced to act, be it to ride the history or to support it, and be surprised by whatever the outcome is.

That is not to mean anything should happen. When anything can happen you do not know what may happen and cannot take decision.

Thus the game should always be moving, in short and long term directions that are clear to the player. It should be complex enough so that the player cannot predict it and micro-manage his way to victory, but it should provide clear clues as to which direction it is heading.

In few words: it must be a chaotic system, logical but impredictible.

## Current status

PRE-ALPHA.

 * Map display is nice.
 * Menu API is working.
 * Influence system basics are working but far to stable.
 * Diplomacy is all but a stub.
 * Discovery is all but a stub.
