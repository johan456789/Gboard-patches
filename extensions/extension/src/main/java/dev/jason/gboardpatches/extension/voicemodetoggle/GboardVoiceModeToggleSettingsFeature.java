package dev.jason.gboardpatches.extension.voicemodetoggle;

import android.content.Context;
import android.content.SharedPreferences;

import dev.jason.gboardpatches.extension.R;
import dev.jason.gboardpatches.extension.flagsettings.GboardBooleanFlagSettingsFeature;
import dev.jason.gboardpatches.extension.rambler.GboardRambler1803OfficialSelectionRuntime;
import dev.jason.gboardpatches.extension.settings.GboardPatchesFeatureAvailability;
import dev.jason.gboardpatches.extension.settings.GboardPatchesSettingsContract;
import dev.jason.gboardpatches.extension.settings.GboardSettingsText;

/**
 * Settings screen toggle that inverts the effective voice typing backend. Mirrors the persistent
 * override driven by the Access Point button.
 */
public final class GboardVoiceModeToggleSettingsFeature
        extends GboardBooleanFlagSettingsFeature {
    public GboardVoiceModeToggleSettingsFeature(Context context) {
        super(
                GboardPatchesFeatureAvailability.FEATURE_VOICE_MODE_TOGGLE,
                text(context, R.string.gboard_patches_voice_mode_toggle_title),
                text(context, R.string.gboard_patches_voice_mode_toggle_summary),
                text(context, R.string.gboard_patches_voice_mode_toggle_toggle_title),
                text(context, R.string.gboard_patches_header_badge),
                text(context, R.string.gboard_patches_flag_patch_error_title),
                text(context, R.string.gboard_patches_flag_patch_error_summary),
                text(context, R.string.gboard_patches_flag_patch_section_feature),
                new GboardPatchesSettingsContract.PreviewSpec(
                        "", "", new GboardPatchesSettingsContract.PreviewMedia[0]),
                new SettingsStore() {
                    @Override
                    public void ensureDefault(SharedPreferences preferences) {
                        // Absence of the key already means "not inverted"; nothing to seed.
                    }

                    @Override
                    public boolean readEnabled(SharedPreferences preferences) {
                        return GboardRambler1803OfficialSelectionRuntime.isInvertedOverride();
                    }

                    @Override
                    public boolean writeEnabled(
                            SharedPreferences preferences,
                            boolean enabled) {
                        GboardRambler1803OfficialSelectionRuntime.writeInvertedOverride(
                                preferences, enabled);
                        return true;
                    }
                });
    }

    private static String text(Context context, int resourceId) {
        return GboardSettingsText.get(context, resourceId);
    }
}
