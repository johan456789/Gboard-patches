package dev.jason.gboardpatches.extension.spacebarswipefix;

import org.junit.Assert;
import org.junit.Test;

public final class GboardSpaceBarSwipeFixPolicyTest {
    @Test
    public void compiledDefaultRestoresServerValue() {
        Assert.assertEquals(
                "key_edge_distance_threshold",
                GboardSpaceBarSwipeFixPolicy.FLAG_NAME);
        Assert.assertEquals(Double.valueOf(0.05d), GboardSpaceBarSwipeFixPolicy.resolve(
                "key_edge_distance_threshold", Double.valueOf(0.0d)));
    }

    @Test
    public void serverLikeValueStaysUntouched() {
        Assert.assertSame(Double.valueOf(0.2d), GboardSpaceBarSwipeFixPolicy.resolve(
                "key_edge_distance_threshold", Double.valueOf(0.2d)));
    }

    @Test
    public void otherFlagNameStaysUntouched() {
        Assert.assertSame(Double.valueOf(0.0d), GboardSpaceBarSwipeFixPolicy.resolve(
                "free_cursor", Double.valueOf(0.0d)));
    }

    @Test
    public void wrongTypeStaysUntouched() {
        Assert.assertSame(Boolean.FALSE, GboardSpaceBarSwipeFixPolicy.resolve(
                "key_edge_distance_threshold", Boolean.FALSE));
    }

    @Test
    public void nullStockResultStaysUntouched() {
        Assert.assertNull(GboardSpaceBarSwipeFixPolicy.resolve(
                "key_edge_distance_threshold", null));
    }
}
