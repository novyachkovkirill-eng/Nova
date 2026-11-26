package org.telegram.utils.ghost;

import org.telegram.messenger.MessagesController;

/**
 * NOVA ghost mode. Off by default. With it on, this account sends no read receipts
 * (read history, read voice/video marks, secret chat reads) and does not report itself as online.
 * Statuses and messages of other people are received as before.
 */
public final class NovaGhostMode {
    public static final String PREF = "nova_ghost_mode";

    private NovaGhostMode() {}

    public static boolean isEnabled() {
        try {
            return MessagesController.getGlobalMainSettings().getBoolean(PREF, false);
        } catch (Throwable e) {
            return false;
        }
    }

    public static void setEnabled(boolean enabled) {
        try {
            MessagesController.getGlobalMainSettings().edit().putBoolean(PREF, enabled).apply();
        } catch (Throwable ignore) {
        }
    }
}
