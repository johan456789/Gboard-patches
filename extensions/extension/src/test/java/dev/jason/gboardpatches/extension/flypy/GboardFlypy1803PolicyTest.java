package dev.jason.gboardpatches.extension.flypy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GboardFlypy1803PolicyTest {
    @Test
    public void recognizesShuangpinFlypySchemeValue() {
        assertTrue(GboardFlypy1803Policy.isShuangpinFlypy("shuangpin_flypy"));
        assertFalse(GboardFlypy1803Policy.isShuangpinFlypy("quanpin"));
        assertFalse(GboardFlypy1803Policy.isShuangpinFlypy("shuangpin_abc"));
        assertFalse(GboardFlypy1803Policy.isShuangpinFlypy(null));
        assertFalse(GboardFlypy1803Policy.isShuangpinFlypy(""));
    }

    @Test
    public void onlySwapsThePinyinEngineWhenFlypyIsSelected() {
        assertTrue(GboardFlypy1803Policy.shouldUseFlypyDictionary(1, "shuangpin_flypy"));
        assertFalse(GboardFlypy1803Policy.shouldUseFlypyDictionary(0, "shuangpin_flypy"));
        assertFalse(GboardFlypy1803Policy.shouldUseFlypyDictionary(2, "shuangpin_flypy"));
        assertFalse(GboardFlypy1803Policy.shouldUseFlypyDictionary(1, "quanpin"));
        assertFalse(GboardFlypy1803Policy.shouldUseFlypyDictionary(1, null));
    }
}
