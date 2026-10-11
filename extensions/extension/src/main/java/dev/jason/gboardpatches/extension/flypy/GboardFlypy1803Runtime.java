package dev.jason.gboardpatches.extension.flypy;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings("unused")
public final class GboardFlypy1803Runtime {
    private static final String DATA_MANAGER_CLASS =
            "com.google.android.apps.inputmethod.libs.hmm.DataManagerImpl";
    private static final String ENROLL_METHOD = "nativeEnrollDataFile";
    private static final String ASSET_PATH = "gboard-flypy/tokens_shuangpin_flypy";
    private static final String DATA_DIRECTORY = "gboard-flypy";
    private static final String DATA_FILE_NAME = "tokens_shuangpin_flypy";

    private static final Map<ClassLoader, GboardFlypy1803RuntimeSupport> HANDLES_BY_CLASS_LOADER =
            Collections.synchronizedMap(
                    new WeakHashMap<ClassLoader, GboardFlypy1803RuntimeSupport>());

    private static volatile Context applicationContext;

    private GboardFlypy1803Runtime() {
    }

    public static void onEngineFactoryCreated(Object engineFactory) {
        if (engineFactory == null) {
            return;
        }
        try {
            GboardFlypy1803RuntimeSupport handles = handles(
                    engineFactory.getClass().getClassLoader());
            seedApplicationContext(handles.applicationContext(engineFactory));
            handles.registerPinyinSchemeListener(engineFactory);
        } catch (Throwable ignored) {
            // A failed registration must not disturb the stock engine factory.
        }
    }

    public static void onEngineFactoryInitialized(Object engineFactory) {
        if (engineFactory == null) {
            return;
        }
        try {
            GboardFlypy1803RuntimeSupport handles = handles(
                    engineFactory.getClass().getClassLoader());
            Context context = handles.applicationContext(engineFactory);
            seedApplicationContext(context);
            ensureDictionaryEnrolled(context, handles.dataManager(engineFactory));
        } catch (Throwable ignored) {
            // Enrollment failure keeps the keyboard on the stock full-pinyin dictionary.
        }
    }

    public static void maybeSelectTokenDictionary(Object xvyBuilder, int engineIndex) {
        if (xvyBuilder == null || engineIndex != GboardFlypy1803Policy.PINYIN_ENGINE_INDEX) {
            return;
        }
        Context context = applicationContext;
        if (context == null) {
            return;
        }
        try {
            GboardFlypy1803RuntimeSupport handles = handles(
                    xvyBuilder.getClass().getClassLoader());
            String scheme = handles.readPinyinScheme(context);
            if (!GboardFlypy1803Policy.shouldUseFlypyDictionary(engineIndex, scheme)) {
                return;
            }
            handles.selectFlypyTokenDictionary(xvyBuilder);
        } catch (Throwable ignored) {
            // A failed swap must leave the stock full-pinyin dictionary in place.
        }
    }

    private static void seedApplicationContext(Context context) {
        if (context == null) {
            return;
        }
        Context application = context.getApplicationContext();
        applicationContext = application != null ? application : context;
    }

    private static void ensureDictionaryEnrolled(Context context, Object dataManager) {
        if (context == null || dataManager == null) {
            return;
        }
        try {
            long handle = dataManager.getClass().getField("a").getLong(dataManager);
            File target = copyBundledDictionary(context);
            if (target == null) {
                return;
            }
            Class<?> dataManagerClass = Class.forName(
                    DATA_MANAGER_CLASS, true, context.getClassLoader());
            Method enrollMethod = dataManagerClass.getMethod(
                    ENROLL_METHOD, long.class, String.class, int.class, String.class);
            enrollMethod.invoke(
                    null,
                    handle,
                    GboardFlypy1803Policy.FLYPY_DICTIONARY_ID,
                    GboardFlypy1803Policy.TOKEN_DICTIONARY_TYPE,
                    target.getAbsolutePath());
        } catch (Throwable ignored) {
            // Enrollment failure keeps the keyboard on the stock full-pinyin dictionary.
        }
    }

    private static File copyBundledDictionary(Context context) {
        try {
            File directory = new File(context.getFilesDir(), DATA_DIRECTORY);
            if (!directory.isDirectory() && !directory.mkdirs()) {
                return null;
            }
            File target = new File(directory, DATA_FILE_NAME);
            InputStream input = context.getAssets().open(ASSET_PATH);
            try {
                OutputStream output = new FileOutputStream(target);
                try {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                } finally {
                    output.close();
                }
            } finally {
                input.close();
            }
            return target;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static GboardFlypy1803RuntimeSupport handles(ClassLoader classLoader)
            throws ReflectiveOperationException {
        if (classLoader == null) {
            throw new ClassNotFoundException("Gboard flypy host has no class loader");
        }
        synchronized (HANDLES_BY_CLASS_LOADER) {
            GboardFlypy1803RuntimeSupport cached = HANDLES_BY_CLASS_LOADER.get(classLoader);
            if (cached != null) {
                return cached;
            }
            GboardFlypy1803RuntimeSupport created =
                    new GboardFlypy1803RuntimeSupport(classLoader);
            HANDLES_BY_CLASS_LOADER.put(classLoader, created);
            return created;
        }
    }
}
