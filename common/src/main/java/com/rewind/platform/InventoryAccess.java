package com.rewind.platform;

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
}
