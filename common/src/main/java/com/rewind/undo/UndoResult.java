package com.rewind.undo;

import com.rewind.Rewind;

import java.util.Objects;

/**
 * Result of an undo validation or execution attempt.
 */
public record UndoResult(Status status, String message) {
    public UndoResult {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(message, "message");
    }

    public static UndoResult success(String description) {
        return new UndoResult(Status.SUCCESS, "Undid: " + Objects.requireNonNull(description, "description"));
    }

    public static UndoResult refusedWorldChanged() {
        return new UndoResult(Status.REFUSED, Rewind.CANNOT_UNDO_WORLD_CHANGED);
    }

    public static UndoResult refused(String message) {
        return new UndoResult(Status.REFUSED, message);
    }

    public static UndoResult failed(String message) {
        return new UndoResult(Status.FAILED, message);
    }

    public boolean shouldPopHistory() {
        return status == Status.SUCCESS;
    }

    public enum Status {
        SUCCESS,
        REFUSED,
        FAILED
    }
}
