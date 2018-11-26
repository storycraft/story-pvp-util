package com.storycraft.pvputil.util.reflect;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Reflect {

    private static final WrappedField<Integer, Field> modifiersField;

    static {
        modifiersField = getField(Field.class, "modifiers", "modifiers");
    }

    public static <T, C>WrappedField<T, C> getField(C obj, String fieldName, String fieldObfName) {
        return (WrappedField<T, C>) getField(obj.getClass(), fieldName, fieldObfName);
    }

    public static <T, C>WrappedField<T, C> getField(Class<C> c, String fieldName, String fieldObfName) {
        try {
            Field field = getDeclaredField(c, fieldName, fieldObfName);

            return new WrappedField<>(field);
        } catch (NullPointerException e) {
            System.out.println("Error to get " + fieldName + " : " + e.getMessage());
        }

        return null;
    }

    private static Field getDeclaredField(Class<?> c, String fieldName, String fieldObfName) {
        Field field = ReflectionHelper.findField(c, fieldName, fieldObfName);

        if (Modifier.isFinal(field.getModifiers()))
            modifiersField.set(field, field.getModifiers() & ~Modifier.FINAL);

        return field;
    }

    public static <T, C>WrappedMethod<T, C> getMethod(Class<C> c, String methodName, String methodObfName, Class... params) {
        Method method = ReflectionHelper.findMethod(c, null, new String[] {methodName, methodObfName}, params);

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