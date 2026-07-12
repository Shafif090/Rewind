package com.rewind.platform;

import com.rewind.capture.StackFingerprint;

import java.util.Optional;
import java.util.UUID;

/**
 * Loader-provided access to server-side entities.
 */
public interface EntityLookup {
    /**
     * Finds an entity by UUID in the relevant server level.
     *
     * @param entityId entity UUID recorded with an inventory action
     * @return the entity object from the active loader runtime, if it still exists
     */
    Optional<Object> findEntity(UUID entityId);

    /**
     * Returns whether the entity is a live dropped item entity that can still be consumed by undo.
     *
     * @param entity loader-native entity object
     * @return true only for an existing, alive, not-removed dropped item entity
     */
    default boolean isLiveDroppedItem(Object entity) {
        return false;
    }

    /**
     * Returns whether the dropped entity stack still matches the recorded identity and count.
     *
     * @param entity loader-native dropped item entity object
     * @param stack expected item identity and components
     * @param count expected stack count
     * @return true only when the dropped item stack is unchanged
     */
    default boolean droppedItemMatches(Object entity, StackFingerprint stack, int count) {
        return false;
    }

    /**
     * Copies the stack currently held by a dropped item entity.
     *
     * @param entity loader-native dropped item entity object
     * @return copied loader-native stack, or empty when unavailable
     */
    default Optional<Object> copyDroppedItemStack(Object entity) {
        return Optional.empty();
    }

    /**
     * Removes the dropped item entity after undo destination validation succeeds.
     *
     * @param entity loader-native dropped item entity object
     * @return true when the entity was removed or marked for removal
     */
    default boolean removeDroppedItem(Object entity) {
        return false;
    }
}
