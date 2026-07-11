package com.rewind.platform;

/**
 * Loader-provided player notification channel.
 */
public interface PlayerMessenger {
    /**
     * Sends a small action bar message to the active player.
     *
     * @param message text to show
     */
    void sendActionBar(String message);
}
