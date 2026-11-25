package org.telegram.utils.glass;

import android.content.SharedPreferences;

import org.telegram.messenger.MessagesController;

/**
 * NOVA liquid glass settings: on/off and strength. Set from NOVA settings.
 * Values are cached, because the glass is read on every frame.
 */
public final class NovaGlassSettings {
    public static final String PREF_ENABLED = "nova_glass_enabled";
    public static final String PREF_INTENSITY = "nova_glass_intensity";
    public static final int DEFAULT_INTENSITY = 50;
    public static final int MAX_INTENSITY = 100;

    private static Boolean cachedEnabled;
    private static Integer cachedIntensity;

    private NovaGlassSettings() {}

    private static SharedPreferences prefs() {
        return MessagesController.getGlobalMainSettings();
    }

    /** Liquid glass is on by default. It replaces the LiteMode liquid glass flag. */
    public static synchronized boolean isEnabled() {
        if (cachedEnabled == null) {
            try {
                cachedEnabled = prefs().getBoolean(PREF_ENABLED, true);
            } catch (Throwable e) {
                return true;
            }
        }
        return cachedEnabled;
    }

    public static synchronized void setEnabled(boolean enabled) {
        cachedEnabled = enabled;
        try {
            prefs().edit().putBoolean(PREF_ENABLED, enabled).apply();
        } catch (Throwable ignore) {}
    }

    /** 0..MAX_INTENSITY, where DEFAULT_INTENSITY is the stock look. */
    public static synchronized int getIntensity() {
        if (cachedIntensity == null) {
            try {
                cachedIntensity = clamp(prefs().getInt(PREF_INTENSITY, DEFAULT_INTENSITY));
            } catch (Throwable e) {
                return DEFAULT_INTENSITY;
            }
        }
        return cachedIntensity;
    }

    public static synchronized void setIntensity(int intensity) {
        cachedIntensity = clamp(intensity);
        try {
            prefs().edit().putInt(PREF_INTENSITY, cachedIntensity).apply();
        } catch (Throwable ignore) {}
    }

    /** Multiplier for the shader's refraction strength: 1.0 at DEFAULT_INTENSITY, 0 at 0, 2.0 at the maximum. */
    public static float intensityScale() {
        return getIntensity() / (float) DEFAULT_INTENSITY;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(MAX_INTENSITY, value));
    }
}
