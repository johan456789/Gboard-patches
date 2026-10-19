package dev.jason.gboardpatches.extension.spacebarswipefix;

import android.content.Context;
import android.content.SharedPreferences;

import dev.jason.gboardpatches.extension.R;
import dev.jason.gboardpatches.extension.flagsettings.GboardBooleanFlagSettingsFeature;
import dev.jason.gboardpatches.extension.settings.GboardPatchesFeatureAvailability;
import dev.jason.gboardpatches.extension.settings.GboardPatchesSettingsContract;
import dev.jason.gboardpatches.extension.settings.GboardSettingsText;

public final class GboardSpaceBarSwipeFixSettingsFeature
        extends GboardBooleanFlagSettingsFeature {
    private static final String PREVIEW_IMAGE =
            "settings-previews/spacebarswipefix/01_space_bar_swipe_fix.png";

    public GboardSpaceBarSwipeFixSettingsFeature(Context context) {
        super(
                GboardPatchesFeatureAvailability.FEATURE_SPACE_BAR_SWIPE_FIX,
                text(context, R.string.gboard_patches_space_bar_swipe_fix_title),
                "",
                text(context, R.string.gboard_patches_space_bar_swipe_fix_title),
                text(context, R.string.gboard_patches_header_badge),
                text(context,
                        R.string.gboard_patches_space_bar_swipe_fix_error_title),
                text(context,
                        R.string.gboard_patches_space_bar_swipe_fix_error_summary),
                text(context,
                        R.string.gboard_patches_space_bar_swipe_fix_section_feature),
                new GboardPatchesSettingsContract.PreviewSpec(
                        text(context,
                                R.string.gboard_patches_space_bar_swipe_fix_title),
                        "",
                        GboardPatchesSettingsContract.PreviewLayout.STACKED,
                        new GboardPatchesSettingsContract.PreviewImage(PREVIEW_IMAGE, "")),
                new SettingsStore() {
                    @Override
                    public void ensureDefault(SharedPreferences preferences) {
                        GboardSpaceBarSwipeFixSettings.ensureDefault(preferences);
                    }

                    @Override
                    public boolean readEnabled(SharedPreferences preferences) {
                        return GboardSpaceBarSwipeFixSettings.readEnabled(preferences);
                    }

                    @Override
                    public boolean writeEnabled(
                            SharedPreferences preferences,
                            boolean enabled) {
                        return GboardSpaceBarSwipeFixSettings
                                .writeEnabled(preferences, enabled);
                    }
                });
    }

    private static String text(Context context, int resourceId) {
        return GboardSettingsText.get(context, resourceId);
    }
}
