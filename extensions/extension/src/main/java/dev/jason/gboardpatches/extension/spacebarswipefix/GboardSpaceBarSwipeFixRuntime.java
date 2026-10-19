package dev.jason.gboardpatches.extension.spacebarswipefix;

import android.content.SharedPreferences;

import dev.jason.gboardpatches.extension.flagsettings.GboardFlagRuntimeContext;

public final class GboardSpaceBarSwipeFixRuntime {
    private GboardSpaceBarSwipeFixRuntime() {
    }

    public static Object applyFlagValue(String flagName, Object stockResult) {
        try {
            return applyFlagValue(
                    flagName,
                    stockResult,
                    GboardFlagRuntimeContext.preferencesOrNull());
        } catch (Throwable ignored) {
            return stockResult;
        }
    }

    static Object applyFlagValue(
            String flagName,
            Object stockResult,
            SharedPreferences preferences) {
        try {
            if (!GboardSpaceBarSwipeFixPolicy.isTargetFlagName(flagName)
                    || preferences == null
                    || !GboardSpaceBarSwipeFixSettings.readEnabled(preferences)) {
                return stockResult;
            }
            return GboardSpaceBarSwipeFixPolicy.resolve(flagName, stockResult);
        } catch (Throwable ignored) {
            return stockResult;
        }
    }
}
