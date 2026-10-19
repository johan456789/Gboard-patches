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
        Double stock = Double.valueOf(0.2d);
        Assert.assertSame(stock, GboardSpaceBarSwipeFixPolicy.resolve(
                "key_edge_distance_threshold", stock));
    }

    @Test
    public void otherFlagNameStaysUntouched() {
        Double stock = Double.valueOf(0.0d);
        Assert.assertSame(stock, GboardSpaceBarSwipeFixPolicy.resolve(
                "free_cursor", stock));
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
