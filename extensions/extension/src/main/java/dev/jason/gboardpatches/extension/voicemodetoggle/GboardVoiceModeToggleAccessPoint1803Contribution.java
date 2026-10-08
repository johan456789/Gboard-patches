package dev.jason.gboardpatches.extension.voicemodetoggle;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.lang.ref.WeakReference;
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
        } catch (Throwable ignored) {
            // A synthetic Access Point must fail closed.
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
        private final WeakReference<Context> contextReference;

        ToggleAction(Context context) {
            contextReference = new WeakReference<>(context);
        }

        @Override
        public void run() {
            Context context = contextReference.get();
            boolean inverted;
            try {
                inverted = GboardRambler1803OfficialSelectionRuntime
                        .toggleInvertedOverride(context);
            } catch (Throwable ignored) {
                inverted = GboardRambler1803OfficialSelectionRuntime.isInvertedOverride();
            }
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
                    } catch (Throwable ignored) {
                        // Toast is best effort.
                    }
                });
            } catch (Throwable ignored) {
                // Toast is best effort.
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
