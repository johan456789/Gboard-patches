package dev.jason.gboardpatches.extension.rambler;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Method;

import dev.jason.gboardpatches.extension.settings.GboardPatchesSettings;

/** Keeps Agentic capability exposure aligned with Gboard's official selector. */
public final class GboardRambler1803OfficialSelectionRuntime {
    /** Persistent user toggle that inverts the effective voice backend. */
    public static final String PREF_KEY_VOICE_MODE_INVERTED = "pref_voice_mode_inverted";
    /** R.string id of Gboard's own agentic-dictation selection pref (the store behind mqk.a). */
    private static final int AGENTIC_SELECTION_RES_ID = 0x7f140a0d;

    private static final ThreadLocal<Integer> VOICE_SETTINGS_SCOPE_DEPTH =
            new ThreadLocal<Integer>();
    private static final ThreadLocal<Integer> DEFAULT_SELECTION_SUPPRESSION_DEPTH =
            new ThreadLocal<Integer>();
    private static final ThreadLocal<Integer> BACKEND_INVERSION_DEPTH =
            new ThreadLocal<Integer>();

    private static volatile Boolean officialRamblerSelected;
    private static volatile Boolean invertedOverride;

    private GboardRambler1803OfficialSelectionRuntime() {
    }

    public static boolean shouldEnableAgenticDictation() {
        if (isDefaultSelectionSuppressed()) {
            return false;
        }
        if (isVoiceSettingsScopeActive()) {
            return true;
        }
        Boolean selected = officialRamblerSelected;
        if (selected == null) {
            selected = readOfficialSelection();
        }
        return Boolean.TRUE.equals(selected);
    }

    public static void enterVoiceSettingsScope() {
        VOICE_SETTINGS_SCOPE_DEPTH.set(Integer.valueOf(depth(VOICE_SETTINGS_SCOPE_DEPTH) + 1));
    }

    public static void exitVoiceSettingsScope() {
        decrement(VOICE_SETTINGS_SCOPE_DEPTH);
    }

    public static void updateOfficialSelection(boolean selected) {
        officialRamblerSelected = Boolean.valueOf(selected);
    }

    public static void enterDefaultSelectionSuppression() {
        DEFAULT_SELECTION_SUPPRESSION_DEPTH.set(Integer.valueOf(
                depth(DEFAULT_SELECTION_SUPPRESSION_DEPTH) + 1));
    }

    public static void exitDefaultSelectionSuppression() {
        decrement(DEFAULT_SELECTION_SUPPRESSION_DEPTH);
    }

    /**
     * Arms a single-invocation inversion of the effective voice backend. While this scope is
     * active, {@link #applyOfficialSelectionOverride(boolean)} returns the inverted official
     * selection so a mic long-press dictates with the other backend for that invocation.
     */
    public static void enterBackendInversionScope() {
        BACKEND_INVERSION_DEPTH.set(Integer.valueOf(depth(BACKEND_INVERSION_DEPTH) + 1));
    }

    public static void exitBackendInversionScope() {
        decrement(BACKEND_INVERSION_DEPTH);
    }

    /**
     * Substitutes the official selector result. Records the stock selection (as the previous
     * read observer did) and returns the inverted value while a backend inversion is active.
     * The inversion is active when either the persistent user toggle is on or the one-invocation
     * scope is armed; the two compose with XOR. Never inverts inside the voice settings UI so the
     * official toggle keeps working.
     */
    public static boolean applyOfficialSelectionOverride(boolean stockResult) {
        updateOfficialSelection(stockResult);
        if (isVoiceSettingsScopeActive()) {
            return stockResult;
        }
        boolean invert = isInvertedOverride() ^ isBackendInversionScopeActive();
        return invert ? !stockResult : stockResult;
    }

    /** Whether the persistent user toggle currently inverts the effective voice backend. */
    public static boolean isInvertedOverride() {
        Boolean cached = invertedOverride;
        if (cached != null) {
            return cached.booleanValue();
        }
        boolean value = readInvertedOverrideFromPreferences(resolveApplicationContext());
        invertedOverride = Boolean.valueOf(value);
        return value;
    }

    /** Flips the persistent user toggle, persists it, and returns the new value. */
    public static boolean toggleInvertedOverride(Context context) {
        boolean next = !isInvertedOverride();
        setInvertedOverride(context, next);
        return next;
    }

    /** Writes the persistent user toggle and refreshes the in-memory cache. */
    public static void setInvertedOverride(Context context, boolean inverted) {
        invertedOverride = Boolean.valueOf(inverted);
        if (context == null) {
            return;
        }
        try {
            GboardPatchesSettings.preferences(context)
                    .edit()
                    .putBoolean(PREF_KEY_VOICE_MODE_INVERTED, inverted)
                    .commit();
        } catch (Throwable ignored) {
            // Persisting the toggle must never affect the keyboard path.
        }
    }

    /** Writes the persistent user toggle into the supplied preferences and refreshes the cache. */
    public static void writeInvertedOverride(SharedPreferences preferences, boolean inverted) {
        invertedOverride = Boolean.valueOf(inverted);
        if (preferences == null) {
            return;
        }
        try {
            preferences.edit()
                    .putBoolean(PREF_KEY_VOICE_MODE_INVERTED, inverted)
                    .commit();
        } catch (Throwable ignored) {
            // Persisting the toggle must never affect the keyboard path.
        }
    }

    private static boolean readInvertedOverrideFromPreferences(Context context) {
        if (context == null) {
            return false;
        }
        try {
            return GboardPatchesSettings.preferences(context)
                    .getBoolean(PREF_KEY_VOICE_MODE_INVERTED, false);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Context resolveApplicationContext() {
        try {
            Object application = Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null);
            if (application instanceof Context) {
                Context context = (Context) application;
                Context app = context.getApplicationContext();
                return app != null ? app : context;
            }
        } catch (Throwable ignored) {
            // Application may not be ready yet.
        }
        return null;
    }

    private static boolean isVoiceSettingsScopeActive() {
        return depth(VOICE_SETTINGS_SCOPE_DEPTH) > 0;
    }

    private static boolean isDefaultSelectionSuppressed() {
        return depth(DEFAULT_SELECTION_SUPPRESSION_DEPTH) > 0;
    }

    private static boolean isBackendInversionScopeActive() {
        return depth(BACKEND_INVERSION_DEPTH) > 0;
    }

    /**
     * Reads Gboard's own agentic-dictation selection pref (the store behind mqk.a). Writing it is
     * what actually switches the backend: Gboard's preference listeners re-route voice typing and
     * flip the mic icon, whereas inverting only the mqk.a return value notifies nobody.
     */
    public static boolean readAgenticSelection(Context context) {
        if (context == null) {
            return false;
        }
        try {
            Class<?> qhyClass = Class.forName("qhy", false, context.getClassLoader());
            Object qhy = qhyClass.getMethod("I", Context.class).invoke(null, context);
            Object value = qhyClass.getMethod("x", int.class, boolean.class)
                    .invoke(qhy, AGENTIC_SELECTION_RES_ID, Boolean.FALSE);
            return Boolean.TRUE.equals(value);
        } catch (Throwable throwable) {
            return false;
        }
    }

    /** Persists Gboard's agentic-dictation selection so its preference listeners fire. */
    public static void writeAgenticSelection(Context context, boolean value) {
        if (context == null) {
            return;
        }
        try {
            Class<?> qhyClass = Class.forName("qhy", false, context.getClassLoader());
            Object qhy = qhyClass.getMethod("I", Context.class).invoke(null, context);
            qhyClass.getMethod("q", int.class, boolean.class)
                    .invoke(qhy, AGENTIC_SELECTION_RES_ID, value);
        } catch (Throwable ignored) {
            // Switching backends must never affect the keyboard path.
        }
    }

    /**
     * Flips the voice backend. Returns true when agentic dictation (Rambler) is now selected.
     * The selection override is cleared so the mqk.a read hook passes the stored value through and
     * both mechanisms agree.
     */
    public static boolean toggleVoiceBackend(Context context) {
        boolean next = !readAgenticSelection(context);
        writeAgenticSelection(context, next);
        officialRamblerSelected = Boolean.valueOf(next);
        invertedOverride = Boolean.FALSE;
        return next;
    }

    private static int depth(ThreadLocal<Integer> scope) {
        Integer value = scope.get();
        return value == null ? 0 : value.intValue();
    }

    private static void decrement(ThreadLocal<Integer> scope) {
        int next = depth(scope) - 1;
        if (next <= 0) {
            scope.remove();
        } else {
            scope.set(Integer.valueOf(next));
        }
    }

    private static Boolean readOfficialSelection() {
        try {
            Object application = Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null);
            if (!(application instanceof Context)) {
                return null;
            }
            ClassLoader loader = application.getClass().getClassLoader();
            Class<?> support = Class.forName("mqk", false, loader);
            Method selection = support.getDeclaredMethod("a", Context.class);
            selection.setAccessible(true);
            Object value = selection.invoke(null, application);
            if (value instanceof Boolean) {
                officialRamblerSelected = (Boolean) value;
                return (Boolean) value;
            }
        } catch (Throwable ignored) {
            // Application or the exact formal selector may not be ready yet.
        }
        return null;
    }

    static void resetForTests() {
        VOICE_SETTINGS_SCOPE_DEPTH.remove();
        DEFAULT_SELECTION_SUPPRESSION_DEPTH.remove();
        BACKEND_INVERSION_DEPTH.remove();
        officialRamblerSelected = null;
        invertedOverride = null;
    }
}
