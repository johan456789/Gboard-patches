package dev.jason.gboardpatches.extension.flypy;

public final class GboardFlypy1803Policy {
    public static final int PINYIN_ENGINE_INDEX = 1;
    public static final String SHUANGPIN_FLYPY_VALUE = "shuangpin_flypy";
    public static final String FLYPY_DICTIONARY_ID =
            "zh_hant_t_i0_shuangpin_flypy_android_token_dictionary";
    public static final String PINYIN_DICTIONARY_ID =
            "zh_hant_t_i0_pinyin_android_token_dictionary";
    public static final int PINYIN_SCHEME_PREF_RES_ID = 0x7f140a4a;
    public static final int TOKEN_DICTIONARY_TYPE = 4;

    private GboardFlypy1803Policy() {
    }

    public static boolean isShuangpinFlypy(String schemeValue) {
        return SHUANGPIN_FLYPY_VALUE.equals(schemeValue);
    }

    public static boolean shouldUseFlypyDictionary(int engineIndex, String schemeValue) {
        return engineIndex == PINYIN_ENGINE_INDEX && isShuangpinFlypy(schemeValue);
    }
}
