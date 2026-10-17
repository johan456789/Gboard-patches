package dev.jason.gboardpatches.extension.longpressquickactions;

public final class GboardLongPressQuickActions1803Policy {
    private static final QuickAction SELECT_ALL = new QuickAction(
            -0x2766, 0x7f0804b1, GboardEditingShortcutPolicy.Shortcut.SELECT_ALL, true);
    private static final QuickAction UNDO = new QuickAction(
            -0x273d, 0x7f0804ed, GboardEditingShortcutPolicy.Shortcut.UNDO, false);
    private static final QuickAction COPY = new QuickAction(
            -0x2767, 0x7f08041b, GboardEditingShortcutPolicy.Shortcut.COPY, true);
    private static final QuickAction CUT = new QuickAction(
            -0x2769, 0x7f08041c, GboardEditingShortcutPolicy.Shortcut.CUT, true);
    private static final QuickAction PASTE = new QuickAction(
            -0x2768, 0x7f08041e, GboardEditingShortcutPolicy.Shortcut.PASTE, true);
    private static final QuickAction REDO = new QuickAction(
            -0x27a3, 0x7f0804ad, GboardEditingShortcutPolicy.Shortcut.REDO, false);

    private GboardLongPressQuickActions1803Policy() {
    }

    public static QuickAction plan(int keyId, String pressText, int[] existingCodes) {
        QuickAction action = resolve(keyId, pressText);
        if (action == null || contains(existingCodes, action.actionCode)) {
            return null;
        }
        return action;
    }

    public static Integer contextMenuActionFor(int keyId, String pressText,
            String actionTypeName, int selectedCode) {
        if (!isQuickActionEvent(keyId, pressText, actionTypeName, selectedCode)) {
            return null;
        }
        QuickAction action = resolve(keyId, pressText);
        if (action == null || action.contextMenuActionId == 0) {
            return null;
        }
        return Integer.valueOf(action.contextMenuActionId);
    }

    public static boolean isQuickActionEvent(int keyId, String pressText,
            String actionTypeName, int selectedCode) {
        if (!"LONG_PRESS".equals(actionTypeName)) {
            return false;
        }
        QuickAction action = resolve(keyId, pressText);
        return action != null && action.actionCode == selectedCode;
    }

    public static boolean containsAssignedAction(int keyId, String pressText,
            int[] existingCodes) {
        QuickAction action = resolve(keyId, pressText);
        return action != null && contains(existingCodes, action.actionCode);
    }

    public static GboardEditingShortcutPolicy.Shortcut shortcutForChord(
            int keyId, String pressText, int selectedCode, int flowModeTypingPulseCode,
            int plainTextCode) {
        QuickAction action = resolve(keyId, pressText);
        if (action == null) {
            return null;
        }
        GboardEditingShortcutPolicy.Shortcut latinCarrier =
                GboardEditingShortcutPolicy.resolveLatinChord(
                        pressText, selectedCode, flowModeTypingPulseCode);
        return selectedCode == action.actionCode
                || selectedCode == plainTextCode
                || latinCarrier == action.shortcut
                ? action.shortcut : null;
    }

    public static GboardEditingShortcutPolicy.Shortcut shortcutForPointerTarget(
            int keyId, String pressText, int pressCarrierCode,
            int flowModeTypingPulseCode) {
        QuickAction action = resolve(keyId, pressText);
        if (action == null) {
            return null;
        }
        return GboardEditingShortcutPolicy.resolveLatinChord(
                pressText, pressCarrierCode, flowModeTypingPulseCode) == action.shortcut
                ? action.shortcut : null;
    }

    public static boolean isZhuyinKeyId(int keyId) {
        return keyId >= 0x7f0b28b7 && keyId <= 0x7f0b28e3;
    }

    private static QuickAction resolve(int keyId, String pressText) {
        if (pressText == null) {
            return null;
        }
        return switch (keyId) {
            case 0x7f0b18d2 -> "a".equals(pressText) ? SELECT_ALL : null;
            case 0x7f0b17f5 -> "A".equals(pressText) ? SELECT_ALL : null;
            case 0x7f0b1a0b -> "z".equals(pressText) ? UNDO : null;
            case 0x7f0b18ab -> "Z".equals(pressText) ? UNDO : null;
            case 0x7f0b18e7 -> "c".equals(pressText) ? COPY : null;
            case 0x7f0b1806 -> "C".equals(pressText) ? COPY : null;
            case 0x7f0b1a01 -> "x".equals(pressText) ? CUT : null;
            case 0x7f0b18a6 -> "X".equals(pressText) ? CUT : null;
            case 0x7f0b19f9 -> "v".equals(pressText) ? PASTE : null;
            case 0x7f0b18a1 -> "V".equals(pressText) ? PASTE : null;
            case 0x7f0b1a04 -> "y".equals(pressText) ? REDO : null;
            case 0x7f0b18a7 -> "Y".equals(pressText) ? REDO : null;
            case 0x7f0b28cf -> "a".equals(pressText) ? SELECT_ALL : null;
            case 0x7f0b28c6 -> "z".equals(pressText) ? UNDO : null;
            case 0x7f0b28c9 -> "c".equals(pressText) ? COPY : null;
            case 0x7f0b28ce -> "x".equals(pressText) ? CUT : null;
            case 0x7f0b28e0 -> "v".equals(pressText) ? PASTE : null;
            case 0x7f0b28e2 -> "y".equals(pressText) ? REDO : null;
            default -> null;
        };
    }

    private static boolean contains(int[] values, int expected) {
        if (values == null) {
            return false;
        }
        for (int value : values) {
            if (value == expected) {
                return true;
            }
        }
        return false;
    }

    public static final class QuickAction {
        public final int actionCode;
        public final int iconResId;
        public final String debugName;
        public final int contextMenuActionId;
        public final GboardEditingShortcutPolicy.Shortcut shortcut;

        private QuickAction(int actionCode, int iconResId,
                GboardEditingShortcutPolicy.Shortcut shortcut,
                boolean dispatchThroughInputConnection) {
            this.actionCode = actionCode;
            this.iconResId = iconResId;
            this.shortcut = shortcut;
            this.debugName = shortcut.debugName;
            this.contextMenuActionId = dispatchThroughInputConnection
                    ? shortcut.contextMenuActionId : 0;
        }
    }
}
