# Grazer (Meteor Client addon, Fabric 1.21.11)

- Anchor ESP: Respawn Anchors, colored by charged / uncharged, optional tracers
- Storage ESP: chests, trapped chests, barrels, ender chests, shulker boxes
- Delete opens the Grazer menu. Right-click a section title for the full Meteor settings.

## Build the jar
Option A (local): install JDK 21 + Gradle, run `gradle build` in this folder. Jar: build/libs/grazer-1.0.0.jar
Option B (no setup): push this folder to a GitHub repo. The included workflow builds it;
download the `grazer-jar` artifact from the Actions tab.

Install: put the jar in your mods folder next to Fabric API (if required) and Meteor Client for 1.21.11.
