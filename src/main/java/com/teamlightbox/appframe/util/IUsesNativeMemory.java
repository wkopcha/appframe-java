package com.teamlightbox.appframe.util;

/**
 * Interface indicating that this object uses non-garbage collected memory and has to be manually cleaned up
 */
public interface IUsesNativeMemory {

    /**
     * Cleans up all non-garbage collected memory
     * Must be called at end of object lifetime
     */
    void cleanup();
}
