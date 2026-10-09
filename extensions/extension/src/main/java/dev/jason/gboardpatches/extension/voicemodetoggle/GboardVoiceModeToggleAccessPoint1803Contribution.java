package dev.jason.gboardpatches.extension.voicemodetoggle;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.jason.gboardpatches.extension.rambler.GboardRambler1803OfficialSelectionRuntime;
import dev.jason.gboardpatches.extension.settings.GboardPatchesFeatureAvailability;

/**
 * Adds a single draggable Access Point button that flips the effective voice backend between
 * agentic dictation ("Rambler") and standard voice typing. The toggle is persistent; the real
 * mic key then dictates in whichever backend is selected.
 */
public final class GboardVoiceModeToggleAccessPoint1803Contribution {
    public static final GboardVoiceModeToggleAccessPoint1803Contribution INSTANCE =
            new GboardVoiceModeToggleAccessPoint1803Contribution();
    public static final String TOKEN = "voice_mode_toggle";
    static final int MIC_DRAWABLE_ID = 0x7f0805ee;
    /** R.string id Gboard uses as the key for the persistent access-point order (mjz). */
    private static final int ORDER_RES_ID = 0x7f1409b0;

    private static volatile Handles handles;

    private GboardVoiceModeToggleAccessPoint1803Contribution() {
    }

    public Object extendOrderCatalog(Context context, Object original) {
        try {
            if (!isAvailable(context) || !(original instanceof Collection<?> collection)) {
                return original;
            }
            List<String> values = copyStrings(collection);
            if (!values.contains(TOKEN)) {
                values.add(TOKEN);
            }
            Class<?> immutableCollection = Class.forName(
                    "vxe", false, original.getClass().getClassLoader());
            Method copy = immutableCollection.getDeclaredMethod("n", Collection.class);
            copy.setAccessible(true);
            return copy.invoke(null, values);
        } catch (Throwable ignored) {
            return original;
        }
    }

    public void register(Object controller, Context context) {
        try {
            if (controller == null || context == null || !isAvailable(context)) {
                return;
            }
            Context application = context.getApplicationContext();
            Context safeContext = application != null ? application : context;
            Handles active = handles(controller.getClass().getClassLoader());
            Object builder = active.descriptorBuilderFactory.invoke(null);
            active.builderTokenMethod.invoke(builder, TOKEN);
            active.builderIconResourceMethod.invoke(builder, MIC_DRAWABLE_ID);
            active.builderLabelTextField.set(builder, "Voice mode");
            active.builderContentDescriptionTextField.set(
                    builder, "Toggle voice typing mode");
            active.builderRunnableMethod.invoke(builder, new ToggleAction(safeContext));
            Object descriptor = active.builderBuildMethod.invoke(builder);
            active.controllerRegisterMethod.invoke(controller, descriptor, false);
            ensureShownInToolbar(safeContext);
        } catch (Throwable ignored) {
            // A synthetic Access Point must fail closed.
        }
    }

    /**
     * Adds the Voice mode token to Gboard's toolbar showing order once, so the button is visible
     * without the user having to drag it in. A one-time flag means removing it stays removed.
     */
    /**
     * Writes the persistent access-point order so the Voice mode button is visible. Gboard merges
     * the default order into the stored order by inserting each missing default before the first
     * stored entry it has not seen yet, which would push a lone token past the visible slots.
     * Prepending the token to the full default list means every default is already present, so
     * nothing is inserted and the token stays first.
     */
    public static void ensureShownInToolbar(Context context) {
        if (context == null) {
            return;
        }
        try {
            ClassLoader loader = context.getClassLoader();
            Class<?> qhyClass = Class.forName("qhy", false, loader);
            Object qhy = qhyClass.getMethod("I", Context.class).invoke(null, context);
            Method read = qhyClass.getMethod("o", int.class, String.class);
            Method write = qhyClass.getMethod("T", int.class, Object.class);

            Object raw = read.invoke(qhy, ORDER_RES_ID, "");
            String current = raw instanceof String ? (String) raw : "";
            String defaults = defaultOrder(loader);
            List<String> desired = new ArrayList<>();
            desired.add(TOKEN);
            appendTokens(desired, current);
            appendTokens(desired, defaults);
            String updated = String.join(";", desired);
            if (updated.equals(current)) {
                // Order already contains the token in first position; nothing to write.
                return;
            }
            write.invoke(qhy, ORDER_RES_ID, updated);
            Log.i("GboardPatches", "[voice-mode] persistent order updated: " + updated);
        } catch (Throwable throwable) {
            Log.i("GboardPatches", "[voice-mode] ensureShownInToolbar failed: " + throwable);
        }
    }

    private static void appendTokens(List<String> target, String order) {
        if (order == null || order.isEmpty()) {
            return;
        }
        for (String part : order.split(";")) {
            if (!part.isEmpty() && !target.contains(part)) {
                target.add(part);
            }
        }
    }

    /** Reads Gboard's default access-point order flag (mid.a -> access_points_order). */
    private static String defaultOrder(ClassLoader loader) {
        try {
            Class<?> midClass = Class.forName("mid", false, loader);
            java.lang.reflect.Field flagField = midClass.getDeclaredField("a");
            flagField.setAccessible(true);
            Object flag = flagField.get(null);
            Object value = flag.getClass().getMethod("g").invoke(flag);
            return value instanceof String ? (String) value : "";
        } catch (Throwable throwable) {
            return "";
        }
    }

    private static boolean isAvailable(Context context) {
        try {
            return GboardPatchesFeatureAvailability.hasFeature(
                    context,
                    GboardPatchesFeatureAvailability.FEATURE_VOICE_MODE_TOGGLE);
        } catch (Throwable ignored) {
            return false;
        }
    }

    static List<String> copyStrings(Collection<?> values) {
        List<String> result = new ArrayList<>();
        if (values != null) {
            for (Object value : values) {
                if (value instanceof String stringValue && !result.contains(stringValue)) {
                    result.add(stringValue);
                }
            }
        }
        return result;
    }

    static String modeLabel(boolean inverted) {
        return inverted
                ? "Standard voice typing"
                : "Rambler (agentic) voice typing";
    }

    private static Handles handles(ClassLoader classLoader) throws Throwable {
        Handles current = handles;
        if (current != null && current.classLoader == classLoader) {
            return current;
        }
        synchronized (GboardVoiceModeToggleAccessPoint1803Contribution.class) {
            current = handles;
            if (current == null || current.classLoader != classLoader) {
                current = Handles.resolve(classLoader);
                handles = current;
            }
            return current;
        }
    }

    private static final class ToggleAction implements Runnable {
        /** Strong ref: the application context is a process singleton and never leaks. */
        private final Context context;

        ToggleAction(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            Context context = this.context;
            boolean inverted;
            try {
                inverted = GboardRambler1803OfficialSelectionRuntime
                        .toggleInvertedOverride(context);
            } catch (Throwable ignored) {
                inverted = GboardRambler1803OfficialSelectionRuntime.isInvertedOverride();
            }
            Log.i("GboardPatches", "[voice-mode] toggle tapped inverted=" + inverted);
            showToast(context, modeLabel(inverted));
        }

        private static void showToast(Context context, String message) {
            if (context == null) {
                return;
            }
            try {
                new Handler(Looper.getMainLooper()).post(() -> {
                    try {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    } catch (Throwable throwable) {
                        Log.i("GboardPatches", "[voice-mode] toast failed: " + throwable);
                    }
                });
            } catch (Throwable throwable) {
                Log.i("GboardPatches", "[voice-mode] toast post failed: " + throwable);
            }
        }
    }

    private static final class Handles {
        final ClassLoader classLoader;
        final Method descriptorBuilderFactory;
        final Method builderTokenMethod;
        final Method builderRunnableMethod;
        final Method builderBuildMethod;
        final Method builderIconResourceMethod;
        final Field builderLabelTextField;
        final Field builderContentDescriptionTextField;
        final Method controllerRegisterMethod;

        Handles(ClassLoader classLoader, Method descriptorBuilderFactory,
                Method builderTokenMethod, Method builderRunnableMethod,
                Method builderBuildMethod, Method builderIconResourceMethod,
                Field builderLabelTextField, Field builderContentDescriptionTextField,
                Method controllerRegisterMethod) {
            this.classLoader = classLoader;
            this.descriptorBuilderFactory = descriptorBuilderFactory;
            this.builderTokenMethod = builderTokenMethod;
            this.builderRunnableMethod = builderRunnableMethod;
            this.builderBuildMethod = builderBuildMethod;
            this.builderIconResourceMethod = builderIconResourceMethod;
            this.builderLabelTextField = builderLabelTextField;
            this.builderContentDescriptionTextField = builderContentDescriptionTextField;
            this.controllerRegisterMethod = controllerRegisterMethod;
        }

        static Handles resolve(ClassLoader classLoader) throws Throwable {
            Class<?> descriptor = Class.forName("mic", false, classLoader);
            Class<?> builder = Class.forName("mhx", false, classLoader);
            Class<?> controller = Class.forName("mlh", false, classLoader);
            Method descriptorBuilderFactory = descriptor.getDeclaredMethod("c");
            Method builderTokenMethod = builder.getDeclaredMethod("l", String.class);
            Method builderRunnableMethod = builder.getDeclaredMethod("q", Runnable.class);
            Method builderBuildMethod = builder.getDeclaredMethod("a");
            Method builderIconResourceMethod = builder.getDeclaredMethod("i", int.class);
            Field builderLabelTextField = builder.getDeclaredField("d");
            Field builderContentDescriptionTextField = builder.getDeclaredField("e");
            Method controllerRegisterMethod = controller.getDeclaredMethod(
                    "g", descriptor, boolean.class);
            descriptorBuilderFactory.setAccessible(true);
            builderTokenMethod.setAccessible(true);
            builderRunnableMethod.setAccessible(true);
            builderBuildMethod.setAccessible(true);
            builderIconResourceMethod.setAccessible(true);
            builderLabelTextField.setAccessible(true);
            builderContentDescriptionTextField.setAccessible(true);
            controllerRegisterMethod.setAccessible(true);
            return new Handles(classLoader, descriptorBuilderFactory, builderTokenMethod,
                    builderRunnableMethod, builderBuildMethod, builderIconResourceMethod,
                    builderLabelTextField, builderContentDescriptionTextField,
                    controllerRegisterMethod);
        }
    }
}
