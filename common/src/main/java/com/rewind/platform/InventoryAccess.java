package com.rewind.platform;

import com.rewind.capture.SlotState;

/**
 * Loader-provided access to the server player's inventory.
 */
public interface InventoryAccess {
    /**
     * Returns the current stack object in a player inventory slot.
     *
     * @param slotIndex server inventory slot index
     * @return loader-native stack object, or an empty-stack representation
     */
    Object stackAt(int slotIndex);

    /**
     * Returns whether the current slot exactly matches a recorded loader-neutral state.
     *
     * @param slotIndex server inventory slot index
     * @param expected recorded expected state
     * @return true only when item identity, components, and count match
     */
    boolean matches(int slotIndex, SlotState expected);

    /**
     * Creates an empty loader-native stack object.
     *
     * @return loader-native empty stack representation
     */
    Object emptyStack();

    /**
     * Copies a loader-native stack with a new count.
     *
     * @param stack loader-native stack object to copy
     * @param count desired item count
     * @return copied loader-native stack object, or null when the stack cannot be copied
     */
    Object copyStackWithCount(Object stack, int count);

    /**
     * Attempts to place a stack into a slot without violating vanilla stack rules.
     *
     * @param slotIndex server inventory slot index
     * @param stack loader-native stack object
     * @return true when the stack was accepted
     */
    boolean tryPlace(int slotIndex, Object stack);

    /**
     * Attempts to merge a stack into the player's inventory.
     *
     * @param stack loader-native stack object
     * @return true when the full stack was merged
     */
    boolean tryMerge(Object stack);

    /**
     * Returns whether a full stack could currently merge into the player's inventory.
     *
     * @param stack loader-native stack object
     * @return true when a later {@link #tryMerge(Object)} should be able to merge the full stack
     */
    boolean canMerge(Object stack);
}
