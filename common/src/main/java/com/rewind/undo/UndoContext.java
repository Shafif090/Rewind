package com.rewind.undo;

import com.rewind.platform.EntityLookup;
import com.rewind.platform.InventoryAccess;
import com.rewind.platform.PlayerMessenger;

import java.util.Objects;
import java.util.UUID;

/**
 * Server-side data and services required to validate and apply an undo.
 */
public record UndoContext(
    UUID playerId,
    Object server,
    Object level,
    Object player,
    PlayerMessenger messenger,
    EntityLookup entityLookup,
    InventoryAccess inventoryAccess
) {
    public UndoContext {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(server, "server");
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(messenger, "messenger");
        Objects.requireNonNull(entityLookup, "entityLookup");
        Objects.requireNonNull(inventoryAccess, "inventoryAccess");
    }
}
