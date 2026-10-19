package dev.jason.gboardpatches.extension.spacebarswipefix;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;

import dev.jason.gboardpatches.extension.settings.GboardPatchesSettingsContract;

@RunWith(RobolectricTestRunner.class)
public final class GboardSpaceBarSwipeFixContractTest {
    @Test
    public void exactFlagDefaultsOnAndPreservesStockWhenDisabledOrPreferencesFail() {
        Context context = RuntimeEnvironment.getApplication();
        SharedPreferences preferences = context.getSharedPreferences(
                "space-bar-swipe-fix-test",
                Context.MODE_PRIVATE);
        preferences.edit().clear().commit();

        Assert.assertEquals(
                "key_edge_distance_threshold",
                GboardSpaceBarSwipeFixPolicy.FLAG_NAME);
        Assert.assertTrue(GboardSpaceBarSwipeFixSettings.DEFAULT_ENABLED);
        Assert.assertEquals(Double.valueOf(0.05d),
                GboardSpaceBarSwipeFixRuntime.applyFlagValue(
                        GboardSpaceBarSwipeFixPolicy.FLAG_NAME,
                        Double.valueOf(0.0d),
                        preferences));

        preferences.edit().putBoolean(
                GboardSpaceBarSwipeFixSettings.PREF_KEY_ENABLED,
                false).commit();
        Double disabledStock = Double.valueOf(0.0d);
        Assert.assertSame(disabledStock,
                GboardSpaceBarSwipeFixRuntime.applyFlagValue(
                        GboardSpaceBarSwipeFixPolicy.FLAG_NAME,
                        disabledStock,
                        preferences));
        Double serverLikeStock = Double.valueOf(0.2d);
        Assert.assertSame(serverLikeStock,
                GboardSpaceBarSwipeFixRuntime.applyFlagValue(
                        GboardSpaceBarSwipeFixPolicy.FLAG_NAME,
                        serverLikeStock,
                        preferences));
        Double unrelatedStock = Double.valueOf(0.0d);
        Assert.assertSame(unrelatedStock,
                GboardSpaceBarSwipeFixRuntime.applyFlagValue(
                        "unrelated",
                        unrelatedStock,
                        preferences));
        Assert.assertSame(Boolean.FALSE,
                GboardSpaceBarSwipeFixRuntime.applyFlagValue(
                        GboardSpaceBarSwipeFixPolicy.FLAG_NAME,
                        Boolean.FALSE,
                        preferences));
        Double throwingStock = Double.valueOf(0.0d);
        Assert.assertSame(throwingStock,
                GboardSpaceBarSwipeFixRuntime.applyFlagValue(
                        GboardSpaceBarSwipeFixPolicy.FLAG_NAME,
                        throwingStock,
                        throwingPreferences()));
    }

    @Test
    public void patchesScreenHasOneSummarylessDefaultOnToggleAndOnePreviewImage() {
        GboardSpaceBarSwipeFixSettingsFeature feature =
                new GboardSpaceBarSwipeFixSettingsFeature(
                        RuntimeEnvironment.getApplication());
        GboardPatchesSettingsContract.Screen screen = feature.buildScreenForState(
                true,
                enabled -> { });

        Assert.assertEquals("Space Bar Swipe Fix", feature.getEntryTitle());
        Assert.assertEquals("", feature.getEntrySummary());
        Assert.assertEquals(1, screen.getRows().size());
        GboardPatchesSettingsContract.ToggleRow row =
                (GboardPatchesSettingsContract.ToggleRow) screen.getRows().get(0);
        Assert.assertTrue(row.isChecked());
        Assert.assertEquals("", row.getSummary());
        Assert.assertEquals(1, row.getPreviewSpec().getMediaItems().size());
        GboardPatchesSettingsContract.PreviewImage preview =
                (GboardPatchesSettingsContract.PreviewImage)
                        row.getPreviewSpec().getMediaItems().get(0);
        Assert.assertEquals(
                "settings-previews/spacebarswipefix/01_space_bar_swipe_fix.png",
                preview.getAssetPath());
        Assert.assertTrue(Files.exists(Path.of(
                "..", "..", "patches", "src", "main", "resources",
                preview.getAssetPath())));
    }

    private static SharedPreferences throwingPreferences() {
        return (SharedPreferences) Proxy.newProxyInstance(
                SharedPreferences.class.getClassLoader(),
                new Class<?>[]{SharedPreferences.class},
                (proxy, method, arguments) -> {
                    throw new AssertionError("preferences failure");
                });
    }
}
