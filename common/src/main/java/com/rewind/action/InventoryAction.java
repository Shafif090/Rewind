package com.rewind.action;

import com.rewind.undo.UndoContext;
import com.rewind.undo.UndoResult;

/**
 * Represents one reversible inventory action recorded by Rewind.
 */
public interface InventoryAction {
    /**
     * Returns whether this action can still be safely undone in the current world state.
     *
     * @param context server-side context and platform services
     * @return true only when undoing cannot duplicate or recreate items
     */
    boolean canUndo(UndoContext context);

    /**
     * Applies the undo operation.
     *
     * @param context server-side context and platform services
     * @return the result to show to the player and use for history mutation
     */
    UndoResult undo(UndoContext context);

    /**
     * Human-readable description used in action bar messages.
     *
     * @return a short action description, such as "Dropped Diamond Pickaxe"
     */
    String description();
}
