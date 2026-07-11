package com.rewind.capture;

import java.util.Objects;

/**
 * Loader-neutral identity for an item stack, excluding stack count.
 */
public record StackFingerprint(String itemKey, String componentsKey) {
    private static final StackFingerprint EMPTY = new StackFingerprint("", "");

    public StackFingerprint {
        Objects.requireNonNull(itemKey, "itemKey");
        Objects.requireNonNull(componentsKey, "componentsKey");
        if (itemKey.isBlank() != componentsKey.isBlank()) {
            throw new IllegalArgumentException("empty stack fingerprint must have empty item and components keys");
        }
    }

    public static StackFingerprint empty() {
        return EMPTY;
    }

    public static StackFingerprint of(String itemKey, String componentsKey) {
        return new StackFingerprint(itemKey, componentsKey);
    }

    public boolean isEmpty() {
        return itemKey.isBlank();
    }
}
