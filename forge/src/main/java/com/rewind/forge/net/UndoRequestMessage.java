package com.rewind.forge.net;

/**
 * Empty Forge client-to-server message requesting the latest inventory undo.
 */
public record UndoRequestMessage() {
    public static final UndoRequestMessage INSTANCE = new UndoRequestMessage();
}
