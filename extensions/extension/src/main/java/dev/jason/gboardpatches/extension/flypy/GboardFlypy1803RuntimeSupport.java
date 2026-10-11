package dev.jason.gboardpatches.extension.flypy;

import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

final class GboardFlypy1803RuntimeSupport {
    private static final String ZTA_CLASS = "zta";
    private static final String XVY_CLASS = "xvy";
    private static final String XVZ_CLASS = "xvz";
    private static final String QHY_CLASS = "qhy";
    private static final String QHX_CLASS = "qhx";
    private static final String CDL_CLASS = "cdl";
    private static final String HBD_CLASS = "hbd";
    private static final String KKB_CLASS = "kkb";

    private static final int XVY_DICTIONARY_FIELD_PRESENT_BIT = 2;

    private final Field applicationContextField;
    private final Field preferenceManagerField;
    private final Field preferenceListenerField;
    private final Method dataManagerMethod;
    private final Method preferenceRegisterMethod;
    private final Field xvyDictionaryField;
    private final Field xvyBitField;
    private final Field ztaMessageField;
    private final Method xvzNewBuilderMethod;
    private final Method ztaSetIterableMethod;
    private final Method ztaBuildMethod;
    private final Object xvzDefaultInstance;
    private final Method preferenceFactoryMethod;
    private final Method preferenceReadStringMethod;

    GboardFlypy1803RuntimeSupport(ClassLoader classLoader) throws ReflectiveOperationException {
        Class<?> ztaClass = resolve(classLoader, ZTA_CLASS);
        Class<?> xvyClass = resolve(classLoader, XVY_CLASS);
        Class<?> xvzClass = resolve(classLoader, XVZ_CLASS);
        Class<?> qhyClass = resolve(classLoader, QHY_CLASS);
        Class<?> qhxClass = resolve(classLoader, QHX_CLASS);
        Class<?> cdlClass = resolve(classLoader, CDL_CLASS);
        Class<?> hbdClass = resolve(classLoader, HBD_CLASS);
        Class<?> kkbClass = resolve(classLoader, KKB_CLASS);

        applicationContextField = findField(hbdClass, "j");
        preferenceManagerField = findField(hbdClass, "g");
        preferenceListenerField = findField(kkbClass, "t");
        dataManagerMethod = hbdClass.getMethod("M");
        preferenceRegisterMethod = qhyClass.getMethod("Z", qhxClass, int.class);

        xvyDictionaryField = xvyClass.getField("d");
        xvyBitField = xvyClass.getField("b");
        ztaMessageField = ztaClass.getField("b");
        xvzNewBuilderMethod = xvzClass.getMethod("l");
        ztaSetIterableMethod = ztaClass.getMethod("bf", Iterable.class);
        ztaBuildMethod = ztaClass.getMethod("q");
        xvzDefaultInstance = xvzClass.getField("a").get(null);
        preferenceFactoryMethod = qhyClass.getMethod("I", Context.class);
        preferenceReadStringMethod = cdlClass.getMethod("o", int.class, String.class);

        setAccessible(
                applicationContextField,
                preferenceManagerField,
                preferenceListenerField,
                dataManagerMethod,
                preferenceRegisterMethod,
                xvyDictionaryField,
                xvyBitField,
                ztaMessageField,
                xvzNewBuilderMethod,
                ztaSetIterableMethod,
                ztaBuildMethod,
                preferenceFactoryMethod,
                preferenceReadStringMethod);
    }

    Context applicationContext(Object engineFactory) throws ReflectiveOperationException {
        return (Context) applicationContextField.get(engineFactory);
    }

    void registerPinyinSchemeListener(Object engineFactory) throws ReflectiveOperationException {
        Object preferences = preferenceManagerField.get(engineFactory);
        Object listener = preferenceListenerField.get(engineFactory);
        preferenceRegisterMethod.invoke(
                preferences,
                listener,
                GboardFlypy1803Policy.PINYIN_SCHEME_PREF_RES_ID);
    }

    Object dataManager(Object engineFactory) throws ReflectiveOperationException {
        return dataManagerMethod.invoke(engineFactory);
    }

    String readPinyinScheme(Context context) throws ReflectiveOperationException {
        Object preferences = preferenceFactoryMethod.invoke(null, context);
        if (preferences == null) {
            return null;
        }
        return (String) preferenceReadStringMethod.invoke(
                preferences,
                GboardFlypy1803Policy.PINYIN_SCHEME_PREF_RES_ID,
                "");
    }

    void selectFlypyTokenDictionary(Object xvyBuilder) throws ReflectiveOperationException {
        Object xvyMessage = ztaMessageField.get(xvyBuilder);
        if (xvyMessage == null) {
            return;
        }
        Object emptyXvzBuilder = xvzNewBuilderMethod.invoke(xvzDefaultInstance);
        ztaSetIterableMethod.invoke(
                emptyXvzBuilder,
                Collections.singletonList(GboardFlypy1803Policy.FLYPY_DICTIONARY_ID));
        Object flypyXvz = ztaBuildMethod.invoke(emptyXvzBuilder);
        xvyDictionaryField.set(xvyMessage, flypyXvz);
        xvyBitField.setInt(
                xvyMessage,
                xvyBitField.getInt(xvyMessage) | XVY_DICTIONARY_FIELD_PRESENT_BIT);
    }

    private static Class<?> resolve(ClassLoader classLoader, String name)
            throws ClassNotFoundException {
        if (classLoader == null) {
            throw new ClassNotFoundException(name);
        }
        return Class.forName(name, false, classLoader);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // Continue walking up the hierarchy.
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static void setAccessible(Object... members) {
        for (Object member : members) {
            try {
                if (member instanceof Field) {
                    ((Field) member).setAccessible(true);
                } else if (member instanceof Method) {
                    ((Method) member).setAccessible(true);
                }
            } catch (Throwable ignored) {
                // Accessible state is best-effort; the members are public in practice.
            }
        }
    }
}
