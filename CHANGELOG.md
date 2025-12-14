# Changelog

## 2.1.0
- Added Fabric/Quilt mod support for Minecraft 1.21.11
- Added Modrinth update checker (configurable via `check-for-updates`)
- Migrated build system from Maven to Gradle
- Paper plugin now supports all 1.21.x versions (1.21 - 1.21.11)
- Separate platform jars: `-Paper.jar` and `-Fabric.jar`

## 2.0.1
- Updated for Minecraft 1.21
- Added Folia support with region-aware scheduler
- Added automatic config updater
- Improved double door redstone synchronization

## 1.3.1
- Removed forgotten debug messages

## 1.3.0
- Added option to allow opening iron doors with hands
- Requires permission `doorsreloaded.irondoors`
- Works with double doors

## 1.1.2
- Fixed `allow-doubledoors` not working

## 1.0.4
- Fixed update checker message shown to OPs when disabled

## 1.0.3
- Fixed knocking when `knocking-requires-empty-hand` is true

## 1.0.2
- Fixed possible NullPointerException

## 1.0.1
- Added download and changelog link to UpdateChecker
- Fixed exception on startup