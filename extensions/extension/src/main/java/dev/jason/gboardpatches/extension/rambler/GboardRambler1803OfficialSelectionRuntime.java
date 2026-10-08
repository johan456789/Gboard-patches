package dev.jason.gboardpatches.extension.rambler;

import android.content.Context;

import java.lang.reflect.Method;

/** Keeps Agentic capability exposure aligned with Gboard's official selector. */
public final class GboardRambler1803OfficialSelectionRuntime {
    private static final ThreadLocal<Integer> VOICE_SETTINGS_SCOPE_DEPTH =
            new ThreadLocal<Integer>();
    private static final ThreadLocal<Integer> DEFAULT_SELECTION_SUPPRESSION_DEPTH =
            new ThreadLocal<Integer>();
    private static final ThreadLocal<Integer> BACKEND_INVERSION_DEPTH =
            new ThreadLocal<Integer>();

    private static volatile Boolean officialRamblerSelected;

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
     * read observer did) and returns the inverted value while the backend inversion scope is
     * active. Never inverts inside the voice settings UI so the official toggle keeps working.
     */
    public static boolean applyOfficialSelectionOverride(boolean stockResult) {
        updateOfficialSelection(stockResult);
        if (isVoiceSettingsScopeActive()) {
            return stockResult;
        }
        return isBackendInversionScopeActive() ? !stockResult : stockResult;
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
    }
}
