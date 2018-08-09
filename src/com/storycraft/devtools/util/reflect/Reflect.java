package com.storycraft.devtools.util.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class Reflect {

    private static final WrappedField<Integer, Field> modifiersField;

    static {
        modifiersField = getField(Field.class, "modifiers");
    }

    public static <T, C>WrappedField<T, C> getField(Object obj, String name) {
        return (WrappedField<T, C>) getField(obj.getClass(), obj, name);
    }

    public static <T, C>WrappedField<T, C> getField(Class<?> c, String name) {
        return (WrappedField<T, C>) getField(c, null, name);
    }

    public static <T, C>WrappedField<T, C> getField(Class<C> c, Object obj, String name) {
        try {
            Field field = getDeclaredField(c, name);

            return new WrappedField<>(field);
        } catch (NullPointerException e) {
            System.out.println("Error to get " + name + " : " + e.getMessage());
        }

        return null;
    }

    private static Field getDeclaredField(Class<?> c, String name) {
        try {
            Field field = c.getDeclaredField(name);
            field.setAccessible(true);

            if (Modifier.isFinal(field.getModifiers()))
                modifiersField.set(field, field.getModifiers() & ~Modifier.FINAL);

            return field;
        } catch (NoSuchFieldException e) {
            System.out.println(name + " field in " + c.getName() + " not found");
        }

        return null;
    }

    public static <T, C>WrappedMethod<T, C> getMethod(Object obj, String name, Class... params) {
        return (WrappedMethod<T, C>) getMethod(obj.getClass(), obj, name, params);
    }

    public static <T, C>WrappedMethod<T, C> getMethod(Class<?> c, String name, Class... params) {
        return (WrappedMethod<T, C>) getMethod(c, null, name, params);
    }

    public static <T, C>WrappedMethod<T, C> getMethod(Class<C> c, Object obj, String name, Class... params) {
        try {
            Method method = getDeclaredMethod(c, name, params);

            return new WrappedMethod<>(method);

        } catch (NullPointerException e) {
            System.out.println("Error to get " + name + " : " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private static Method getDeclaredMethod(Class<?> c, String name, Class<?>... classes) {
        try {
            Method method = c.getDeclaredMethod(name, classes);
            method.setAccessible(true);

            return method;
        } catch (NoSuchMethodException e) {
            System.out.println(name + " method in " + c.getName() + " not found");
        }

        return null;
    }

    public static class WrappedField<T, C> {

        private Field field;

        public WrappedField(Field field){
            this.field = field;
        }

        public Field getField() {
            return field;
        }

        public String getName(){
            return field.getName();
        }

        public T get(C object){
            try {
                return (T) field.get(object);
            } catch (IllegalAccessException e) {
                System.out.println("Error to get " + getName() + " : " + e.getMessage());
            }

            return null;
        }

        public void set(C object, T value){
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                System.out.println("Error to set " + getName() + " : " + e.getMessage());
            }
        }
    }

    public static class WrappedMethod<T, C> {

        private Method method;

        public WrappedMethod(Method method){
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }

        public String getName(){
            return method.getName();
        }

        public T invoke(C object, Object... objects){
            try {
                return (T) method.invoke(object, objects);
            } catch (InvocationTargetException | IllegalAccessException e) {
                System.out.println("Error to invoke " + getName() + " : " + e.getMessage());
            }

            return null;
        }
    }
}
