package com.rewind.undo;

import com.rewind.history.InventoryHistory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared server-side entrypoint used by loader networking handlers.
 */
public final class UndoController {
    private static final Map<UUID, UndoService> SERVICES = new ConcurrentHashMap<>();

    private UndoController() {
    }

    /**
     * Runs the latest undo action for a player on the server thread.
     *
     * @param context server-side undo context
     * @return result returned by the undo service
     */
    public static UndoResult undoLatest(UndoContext context) {
        return serviceFor(context.playerId()).undoLatest(context);
    }

    /**
     * Returns the player's current undo service.
     *
     * @param playerId server player UUID
     * @return undo service backed by that player's history
     */
    public static UndoService serviceFor(UUID playerId) {
        return SERVICES.computeIfAbsent(playerId, ignored -> new UndoService(new InventoryHistory()));
    }

    /**
     * Removes server-side history for a player, normally when they disconnect.
     *
     * @param playerId server player UUID
     */
    public static void clear(UUID playerId) {
        SERVICES.remove(playerId);
    }
}
