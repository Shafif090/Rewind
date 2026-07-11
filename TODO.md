# Rewind Implementation Plan

This plan is meant to be executed one step at a time. Fabric and Forge are required targets. NeoForge is optional and should only be added after the Fabric + Forge build is stable.

## Phase 1 - Project Foundation

- [x] Confirm exact Minecraft 26.x target version and matching mappings/toolchain.
- [x] Create Gradle multi-module project.
- [x] Add Stonecutter configuration for shared common code.
- [x] Add modules:
  - [x] `common`
  - [x] `fabric`
  - [x] `forge`
  - [x] Optional later: `neoforge`
- [x] Configure Java 25 toolchain.
- [x] Add basic README with build commands.
- [x] Verify empty project compiles for Fabric and Forge.

## Phase 2 - Common Architecture

- [x] Create common package structure.
- [x] Add `InventoryAction` interface.
- [x] Add `UndoResult` value type.
- [x] Add `UndoContext` for server-side world/player access.
- [x] Add `InventoryHistory` with max size of 100 actions.
- [x] Add `UndoService` to handle peek, validate, undo, pop, and messages.
- [x] Add platform boundary interfaces:
  - [x] `PlayerMessenger`
  - [x] `EntityLookup`
  - [x] `InventoryAccess`
- [x] Add TODO comments for future features:
  - [x] Crafting undo
  - [x] Creative mode support
  - [x] Chest interactions
  - [x] Undo timeline GUI
  - [x] Config screen

## Phase 3 - Server-Side Undo Flow

- [x] Ensure undo requests execute on the server.
- [x] Add client-to-server undo request packet for Fabric.
- [x] Add client-to-server undo request packet for Forge.
- [x] Register `Ctrl + Z` keybinding on Fabric client.
- [x] Register `Ctrl + Z` keybinding on Forge client.
- [x] Show action bar messages from server result.
- [x] Refuse undo without popping history when `canUndo()` fails.

## Phase 4 - Action Recording

- [x] Implement inventory transaction recorder in common.
- [x] Capture relevant before/after slot state without saving full inventory snapshots.
- [x] Detect moving items between slots.
- [x] Detect swapping slots.
- [x] Detect shift-click transfers.
- [x] Detect number-key hotbar swaps.
- [x] Detect item pickup actions.
- [x] Detect item drop actions.
- [x] Normalize detected changes into `InventoryAction` implementations.
- [x] Add strict validation for unexpected inventory changes.

## Phase 5 - Action Implementations

- [x] Implement `MoveAction`.
- [x] Implement `SwapAction`.
- [x] Implement `ShiftClickAction`.
- [x] Implement `HotbarSwapAction`.
- [x] Implement `PickupAction`.
- [x] Implement `DropAction`.
- [x] Ensure every action implements:
  - [x] `canUndo()`
  - [x] `undo()`
  - [x] `description()`
- [x] Ensure every action refuses with `Cannot undo: world has changed.` when state validation fails.
- [x] Ensure no action can duplicate items.

## Phase 6 - Drop Action Strict Mode

- [ ] Record dropped `ItemEntity` UUID.
- [ ] Record original slot.
- [ ] Record original stack identity and count.
- [ ] On undo, find the dropped entity by UUID.
- [ ] Verify entity still exists.
- [ ] Verify entity is not removed or dead.
- [ ] Verify entity stack still matches recorded stack.
- [ ] Remove entity only after destination inventory validation succeeds.
- [ ] Return stack to original slot if possible.
- [ ] Attempt merge if original slot is occupied.
- [ ] Fail gracefully if merge is impossible.
- [ ] Never recreate burned, despawned, or stolen dropped items.

## Phase 7 - Loader Integration

- [ ] Fabric:
  - [ ] Add mod metadata.
  - [ ] Add initializer.
  - [ ] Register client keybinding.
  - [ ] Register networking.
  - [ ] Register inventory/drop/pickup event hooks.
- [ ] Forge:
  - [ ] Add mod metadata.
  - [ ] Add main mod class.
  - [ ] Register client keybinding.
  - [ ] Register networking.
  - [ ] Register inventory/drop/pickup event hooks.
- [ ] Keep loader-specific classes thin.
- [ ] Confirm no Fabric APIs appear in `common`.
- [ ] Confirm no Forge APIs appear in `common`.

## Phase 8 - Testing And Verification

- [ ] Add unit tests for history behavior.
- [ ] Add unit tests for action validation rules.
- [ ] Add test cases for each supported action.
- [ ] Manually test Fabric build in dev client.
- [ ] Manually test Forge build in dev client.
- [ ] Test undo refusal cases:
  - [ ] Dropped item burned.
  - [ ] Dropped item despawned.
  - [ ] Dropped item picked up by another entity.
  - [ ] Inventory slot changed unexpectedly.
  - [ ] Inventory has no room to restore item.
- [ ] Verify messages:
  - [ ] `Undid: Dropped Diamond Pickaxe`
  - [ ] `Undid: Swapped Slots`
  - [ ] `Cannot undo: world has changed.`

## Phase 9 - Documentation And Release Readiness

- [ ] Update README with feature list.
- [ ] Document supported Minecraft version.
- [ ] Document Fabric build command.
- [ ] Document Forge build command.
- [ ] Document strict-mode limitations.
- [ ] Document unsupported features.
- [ ] Confirm license choice.
- [ ] Produce release artifacts for Fabric and Forge.

## Optional Phase - NeoForge

- [ ] Add `neoforge` Stonecutter module.
- [ ] Add NeoForge metadata.
- [ ] Add NeoForge initializer.
- [ ] Register keybinding and networking.
- [ ] Register event hooks.
- [ ] Verify NeoForge build and dev client.
