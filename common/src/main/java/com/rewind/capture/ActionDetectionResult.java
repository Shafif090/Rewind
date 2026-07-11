package com.rewind.capture;

import com.rewind.action.InventoryAction;

import java.util.Objects;
import java.util.Optional;

/**
 * Result of trying to convert slot changes into an undoable action record.
 */
public record ActionDetectionResult(Optional<InventoryAction> action, String reason) {
    public ActionDetectionResult {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(reason, "reason");
    }

    public static ActionDetectionResult detected(InventoryAction action) {
        return new ActionDetectionResult(Optional.of(Objects.requireNonNull(action, "action")), "");
    }

    public static ActionDetectionResult ignored(String reason) {
        return new ActionDetectionResult(Optional.empty(), reason);
    }

    public boolean isDetected() {
        return action.isPresent();
    }
}
