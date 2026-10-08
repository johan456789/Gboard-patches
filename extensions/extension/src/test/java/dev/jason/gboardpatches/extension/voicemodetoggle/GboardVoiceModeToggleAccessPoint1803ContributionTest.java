package dev.jason.gboardpatches.extension.voicemodetoggle;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public final class GboardVoiceModeToggleAccessPoint1803ContributionTest {
    @Test
    public void copyStringsDeduplicatesAndSkipsNonStrings() {
        List<String> result = GboardVoiceModeToggleAccessPoint1803Contribution.copyStrings(
                Arrays.asList("a", "a", 5, null, "b"));
        Assert.assertEquals(Arrays.asList("a", "b"), result);
    }

    @Test
    public void modeLabelDescribesBackend() {
        Assert.assertEquals("Standard voice typing",
                GboardVoiceModeToggleAccessPoint1803Contribution.modeLabel(true));
        Assert.assertEquals("Rambler (agentic) voice typing",
                GboardVoiceModeToggleAccessPoint1803Contribution.modeLabel(false));
    }

    @Test
    public void tokenIsStable() {
        Assert.assertEquals("voice_mode_toggle",
                GboardVoiceModeToggleAccessPoint1803Contribution.TOKEN);
    }
}
