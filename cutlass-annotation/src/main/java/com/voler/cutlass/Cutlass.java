package com.voler.cutlass;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 三尺春光驱我寒，一生戎马为长安
 * Created by voler on 17/8/5.
 */

public final class Cutlass {
    private Cutlass() {
        throw new AssertionError("No instances.");
    }

    private static final String TAG = "Cutlass";
    private static boolean debug = false;

    static final Map<Class<?>, FieldInject> BINDERS = new LinkedHashMap<>();
    static final FieldInject NOP_VIEW_BINDER = new FieldInject() {

        @Override
        public void inject(Object target) {

        }
    };

    /**
     * Control whether debug logging is enabled.
     */
    public static void setDebug(boolean debug) {
        Cutlass.debug = debug;
    }


    public static void inject(Object target) {
        Class<?> targetClass = target.getClass();
        try {
            FieldInject viewBinder = findViewBinderForClass(targetClass);
            viewBinder.inject(target);
        } catch (Exception e) {
            throw new RuntimeException("Unable to bind views for " + targetClass.getName(), e);
        }
    }

    private static FieldInject findViewBinderForClass(Class<?> cls)
            throws IllegalAccessException, InstantiationException {
        FieldInject viewBinder = BINDERS.get(cls);
        if (viewBinder != null) {
            return viewBinder;
        }
        String clsName = cls.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
            return NOP_VIEW_BINDER;
        }
        try {
            Class<?> viewBindingClass = Class.forName(clsName + "$$InjectField");
            //noinspection unchecked
            viewBinder = (FieldInject) viewBindingClass.newInstance();
        } catch (ClassNotFoundException e) {
            viewBinder = findViewBinderForClass(cls.getSuperclass());
        }
        BINDERS.put(cls, viewBinder);
        return viewBinder;
    }

}