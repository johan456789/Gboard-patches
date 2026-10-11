package dev.jason.gboardpatches.extension.flypy;

import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

final class GboardFlypy1803RuntimeSupport {
    private static final String ZTA_CLASS = "defpackage.zta";
    private static final String XVY_CLASS = "defpackage.xvy";
    private static final String XVZ_CLASS = "defpackage.xvz";
    private static final String QHY_CLASS = "defpackage.qhy";
    private static final String CDL_CLASS = "defpackage.cdl";

    private static final int XVY_DICTIONARY_FIELD_PRESENT_BIT = 2;

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
        Class<?> cdlClass = resolve(classLoader, CDL_CLASS);

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
                xvyDictionaryField,
                xvyBitField,
                ztaMessageField,
                xvzNewBuilderMethod,
                ztaSetIterableMethod,
                ztaBuildMethod,
                preferenceFactoryMethod,
                preferenceReadStringMethod);
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
