package com.rewind.platform;

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
}
