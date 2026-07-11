# Rewind

Rewind is a Minecraft quality-of-life mod that will let players undo safe inventory actions with `Ctrl + Z`.

The project is intentionally split into shared logic and loader integrations:

- `common` contains inventory history, undo validation, and action logic.
- `fabric` contains Fabric-only bootstrap, keybinding, networking, and event hooks.
- `forge` contains Forge-only bootstrap, keybinding, networking, and event hooks.
- `stonecutter.gradle` records the required Stonecutter target matrix.

Fabric and Forge are required targets. NeoForge is optional and can be added later.

## Target

- Minecraft Java `26.2`
- Java `25`
- Fabric loader `0.18.2`
- Forge `65.0.3`
- Stonecutter Gradle plugin `0.9.6`

## Build

Generate or use the Gradle wrapper, then run:

```powershell
.\gradlew.bat build
```

Useful module checks:

```powershell
.\gradlew.bat :common:build
.\gradlew.bat :fabric:build
.\gradlew.bat :forge:build
```

Minecraft loader plugins and mod metadata are added in later phases.
