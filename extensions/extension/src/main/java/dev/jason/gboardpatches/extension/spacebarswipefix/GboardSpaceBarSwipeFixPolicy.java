package dev.jason.gboardpatches.extension.spacebarswipefix;

public final class GboardSpaceBarSwipeFixPolicy {
    public static final String FLAG_NAME = "key_edge_distance_threshold";
    public static final double SERVER_VALUE = 0.05d;
    public static final double COMPILED_DEFAULT = 0.0d;

    private GboardSpaceBarSwipeFixPolicy() {
    }

    public static boolean isTargetFlagName(String flagName) {
        return FLAG_NAME.equals(flagName);
    }

    public static Object resolve(String flagName, Object stockResult) {
        if (!isTargetFlagName(flagName) || !(stockResult instanceof Double)) {
            return stockResult;
        }
        return ((Double) stockResult) == COMPILED_DEFAULT ? Double.valueOf(SERVER_VALUE) : stockResult;
    }
}
