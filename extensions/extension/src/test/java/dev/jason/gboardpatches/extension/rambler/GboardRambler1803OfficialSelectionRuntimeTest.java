package dev.jason.gboardpatches.extension.rambler;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public final class GboardRambler1803OfficialSelectionRuntimeTest {
    @After
    public void tearDown() {
        GboardRambler1803OfficialSelectionRuntime.resetForTests();
    }

    @Test
    public void officialSelectorStateControlsAgenticCapabilityOutsideSettings() {
        GboardRambler1803OfficialSelectionRuntime.updateOfficialSelection(false);
        Assert.assertFalse(
                GboardRambler1803OfficialSelectionRuntime.shouldEnableAgenticDictation());

        GboardRambler1803OfficialSelectionRuntime.updateOfficialSelection(true);
        Assert.assertTrue(
                GboardRambler1803OfficialSelectionRuntime.shouldEnableAgenticDictation());
    }

    @Test
    public void voiceSettingsScopeTemporarilyExposesBothOfficialChoices() {
        GboardRambler1803OfficialSelectionRuntime.updateOfficialSelection(false);
        GboardRambler1803OfficialSelectionRuntime.enterVoiceSettingsScope();
        GboardRambler1803OfficialSelectionRuntime.enterVoiceSettingsScope();

        Assert.assertTrue(
                GboardRambler1803OfficialSelectionRuntime.shouldEnableAgenticDictation());
        GboardRambler1803OfficialSelectionRuntime.exitVoiceSettingsScope();
        Assert.assertTrue(
                GboardRambler1803OfficialSelectionRuntime.shouldEnableAgenticDictation());
        GboardRambler1803OfficialSelectionRuntime.exitVoiceSettingsScope();
        Assert.assertFalse(
                GboardRambler1803OfficialSelectionRuntime.shouldEnableAgenticDictation());
    }

    @Test
    public void defaultSelectionSuppressionWinsAndBalancedExitRestoresOfficialState() {
        GboardRambler1803OfficialSelectionRuntime.updateOfficialSelection(true);
        GboardRambler1803OfficialSelectionRuntime.enterVoiceSettingsScope();
        GboardRambler1803OfficialSelectionRuntime.enterDefaultSelectionSuppression();
        GboardRambler1803OfficialSelectionRuntime.enterDefaultSelectionSuppression();

        Assert.assertFalse(
                GboardRambler1803OfficialSelectionRuntime.shouldEnableAgenticDictation());
        GboardRambler1803OfficialSelectionRuntime.exitDefaultSelectionSuppression();
        Assert.assertFalse(
                GboardRambler1803OfficialSelectionRuntime.shouldEnableAgenticDictation());
        GboardRambler1803OfficialSelectionRuntime.exitDefaultSelectionSuppression();
        Assert.assertTrue(
                GboardRambler1803OfficialSelectionRuntime.shouldEnableAgenticDictation());

        GboardRambler1803OfficialSelectionRuntime.exitVoiceSettingsScope();
        Assert.assertTrue(
                GboardRambler1803OfficialSelectionRuntime.shouldEnableAgenticDictation());
    }

    @Test
    public void backendInversionScopeInvertsOfficialSelectionForOneInvocation() {
        GboardRambler1803OfficialSelectionRuntime.updateOfficialSelection(true);
        Assert.assertTrue(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(true));

        GboardRambler1803OfficialSelectionRuntime.enterBackendInversionScope();
        Assert.assertFalse(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(true));

        GboardRambler1803OfficialSelectionRuntime.exitBackendInversionScope();
        Assert.assertTrue(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(true));
    }

    @Test
    public void backendInversionScopeIsNestedAndBalanced() {
        GboardRambler1803OfficialSelectionRuntime.enterBackendInversionScope();
        GboardRambler1803OfficialSelectionRuntime.enterBackendInversionScope();
        Assert.assertFalse(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(true));
        GboardRambler1803OfficialSelectionRuntime.exitBackendInversionScope();
        Assert.assertFalse(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(true));
        GboardRambler1803OfficialSelectionRuntime.exitBackendInversionScope();
        Assert.assertTrue(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(true));
    }

    @Test
    public void voiceSettingsScopeIsNeverInverted() {
        GboardRambler1803OfficialSelectionRuntime.enterVoiceSettingsScope();
        GboardRambler1803OfficialSelectionRuntime.enterBackendInversionScope();

        Assert.assertTrue(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(true));
        Assert.assertFalse(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(false));

        GboardRambler1803OfficialSelectionRuntime.exitBackendInversionScope();
        GboardRambler1803OfficialSelectionRuntime.exitVoiceSettingsScope();
    }

    @Test
    public void applyOfficialSelectionOverrideKeepsOfficialCacheInSync() {
        GboardRambler1803OfficialSelectionRuntime.enterBackendInversionScope();
        Assert.assertFalse(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(true));
        GboardRambler1803OfficialSelectionRuntime.exitBackendInversionScope();
        Assert.assertTrue(
                GboardRambler1803OfficialSelectionRuntime.shouldEnableAgenticDictation());
    }

    @Test
    public void persistentInversionOverrideInvertsOfficialSelection() {
        Assert.assertTrue(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(true));

        GboardRambler1803OfficialSelectionRuntime.setInvertedOverride(null, true);
        Assert.assertFalse(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(true));

        GboardRambler1803OfficialSelectionRuntime.setInvertedOverride(null, false);
        Assert.assertTrue(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(true));
    }

    @Test
    public void toggleFlipsThePersistentOverride() {
        Assert.assertFalse(GboardRambler1803OfficialSelectionRuntime.isInvertedOverride());
        Assert.assertTrue(GboardRambler1803OfficialSelectionRuntime.toggleInvertedOverride(null));
        Assert.assertTrue(GboardRambler1803OfficialSelectionRuntime.isInvertedOverride());
        Assert.assertFalse(GboardRambler1803OfficialSelectionRuntime.toggleInvertedOverride(null));
    }

    @Test
    public void persistentOverrideAndScopeComposeWithXor() {
        GboardRambler1803OfficialSelectionRuntime.setInvertedOverride(null, true);
        GboardRambler1803OfficialSelectionRuntime.enterBackendInversionScope();
        Assert.assertTrue(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(true));
        GboardRambler1803OfficialSelectionRuntime.exitBackendInversionScope();
        Assert.assertFalse(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(true));
    }

    @Test
    public void voiceSettingsScopeIgnoresPersistentOverride() {
        GboardRambler1803OfficialSelectionRuntime.setInvertedOverride(null, true);
        GboardRambler1803OfficialSelectionRuntime.enterVoiceSettingsScope();
        Assert.assertTrue(
                GboardRambler1803OfficialSelectionRuntime.applyOfficialSelectionOverride(true));
        GboardRambler1803OfficialSelectionRuntime.exitVoiceSettingsScope();
    }
}
