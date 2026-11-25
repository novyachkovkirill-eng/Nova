package org.telegram.utils.glass;

import org.telegram.messenger.MessagesController;

/**
 * NOVA interface style for list sections and cards. Set from NOVA settings.
 * Classic keeps the Telegram look; glass makes section cards translucent with a light rim.
 */
public final class NovaUiStyle {
    public static final String PREF = "nova_ui_style";
    public static final int CLASSIC = 0;
    public static final int GLASS = 1;
    public static final int DEFAULT = GLASS;

    private static Integer cached;

    private NovaUiStyle() {}

    public static synchronized int get() {
        if (cached == null) {
            try {
                cached = MessagesController.getGlobalMainSettings().getInt(PREF, DEFAULT);
            } catch (Throwable e) {
                return DEFAULT;
            }
        }
        return cached == GLASS ? GLASS : CLASSIC;
    }

    public static synchronized void set(int style) {
        cached = style == GLASS ? GLASS : CLASSIC;
        try {
            MessagesController.getGlobalMainSettings().edit().putInt(PREF, cached).apply();
        } catch (Throwable ignore) {}
    }

    public static boolean isGlass() {
        return get() == GLASS;
    }
}
