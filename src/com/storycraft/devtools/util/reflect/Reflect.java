package com.storycraft.devtools.util.reflect;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

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

    public static <T, C>WrappedField<T, C> getField(C obj, String... nameList) {
        return (WrappedField<T, C>) getField(obj.getClass(), nameList);
    }

    public static <T, C>WrappedField<T, C> getField(Class<C> c, String... nameList) {
        try {
            Field field = getDeclaredField(c, nameList);

            return new WrappedField<>(field);
        } catch (NullPointerException e) {
            System.out.println("Error to get " + nameList + " : " + e.getMessage());
        }

        return null;
    }

    private static Field getDeclaredField(Class<?> c, String... nameList) {
        Field field = ReflectionHelper.findField(c, nameList);

        if (Modifier.isFinal(field.getModifiers()))
            modifiersField.set(field, field.getModifiers() & ~Modifier.FINAL);

        return field;
    }

    public static <T, C>WrappedMethod<T, C> getMethod(Class<C> c, String[] nameList, Class... params) {
        return (WrappedMethod<T, C>) getMethod(c, null, nameList, params);
    }

    public static <T, C>WrappedMethod<T, C> getMethod(Class<C> c, C obj, String[] nameList, Class... params) {
        Method method = ReflectionHelper.findMethod(c, obj, nameList, params);

        return new WrappedMethod<>(method);
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
