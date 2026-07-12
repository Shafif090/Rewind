# Rewind Manual Testing Checklist

Use a fresh test world with cheats enabled. Test Fabric first, then Forge. After each scenario, confirm the action bar message and inventory state before moving on.

## Start Dev Clients

Fabric:

```powershell
.\gradlew.bat :fabric:runClient
```

Forge:

```powershell
.\gradlew.bat :forge:runClient
```

## Setup

1. Create or open a creative test world.
2. Put these items in the player inventory: diamonds, stone, dirt, and one diamond pickaxe.
3. Switch to survival for drop and pickup tests if creative behavior looks different:

```text
/gamemode survival
```

## Expected Success Cases

- Move stack: move part of a diamond stack from one slot to an empty slot, then press `Ctrl + Z`. The source and destination slots should return to their previous counts.
- Swap slots: swap diamonds and stone between two inventory slots, then press `Ctrl + Z`. Expected message: `Undid: Swapped Slots`.
- Shift-click transfer: shift-click a stack between player inventory sections, then press `Ctrl + Z`. The stack should return to the original slot if the snapshot is unchanged.
- Hotbar number swap: hover a slot and press a hotbar number key to swap, then press `Ctrl + Z`.
- Pickup: drop an item, pick it back up, then press `Ctrl + Z`. The gained inventory stack should be removed only if the current inventory still matches the recorded pickup.
- Drop: drop one diamond pickaxe or diamond stack, wait for it to appear in the world, then press `Ctrl + Z`. Expected message shape: `Undid: Dropped Item`; the dropped entity should disappear and the item should return to inventory.

## Expected Refusal Cases

Each refusal should show:

```text
Cannot undo: world has changed.
```

- Dropped item burned: drop an item into lava/fire, then press `Ctrl + Z`.
- Dropped item despawned: hard to wait out manually; simulate by dropping an item, using `/kill @e[type=item,limit=1,sort=nearest]`, then pressing `Ctrl + Z`.
- Dropped item picked up by another entity/player: use a second player if available, or let another inventory action consume/change the dropped entity before undo.
- Inventory slot changed unexpectedly: after a move/drop, manually place a different item into the affected slot before pressing `Ctrl + Z`.
- Inventory has no room to restore item: fill all inventory slots with full stacks, occupy the original drop slot with a different full stack, then press `Ctrl + Z`.

## What To Report Back

For each loader, tell me:

- Which success cases passed or failed.
- Which refusal cases passed or failed.
- The exact action bar message for failures.
- Any console error or crash text.
