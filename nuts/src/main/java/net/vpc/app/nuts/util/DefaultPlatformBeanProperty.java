package net.vpc.app.nuts.util;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

class DefaultPlatformBeanProperty extends AbstractPlatformBeanProperty {

    private Method getter;
    private Method setter;
    private Field javaField;
    private boolean transientProperty;
    private boolean deprecatedProperty;

    DefaultPlatformBeanProperty(String field, Class fieldType, Field jfield, Method getter, Method setter) {
        super(field, fieldType);
        this.javaField = jfield;
        if (javaField != null) {
            if (Modifier.isTransient(javaField.getModifiers())) {
                transientProperty = true;
            }
            JsonTransient jTransient = javaField.getAnnotation(JsonTransient.class);
            if (jTransient != null) {
                transientProperty = true;
            }
            Deprecated deprecated = javaField.getAnnotation(Deprecated.class);
            if (deprecated != null) {
                deprecatedProperty = true;
            }
        }
        this.getter = getter;
        if (getter != null) {
            getter.setAccessible(true);
            Transient aTransient = getter.getAnnotation(Transient.class);
            if (aTransient != null) {
                transientProperty = true;
            }
            JsonTransient jTransient = getter.getAnnotation(JsonTransient.class);
            if (jTransient != null) {
                transientProperty = true;
            }
            Deprecated deprecated = getter.getAnnotation(Deprecated.class);
            if (deprecated != null) {
                deprecatedProperty = true;
            }
        }

        this.setter = setter;
        if (setter != null) {
            setter.setAccessible(true);
            Transient aTransient = setter.getAnnotation(Transient.class);
            if (aTransient != null) {
                transientProperty = true;
            }
            JsonTransient jTransient = setter.getAnnotation(JsonTransient.class);
            if (jTransient != null) {
                transientProperty = true;
            }
            Deprecated deprecated = setter.getAnnotation(Deprecated.class);
            if (deprecated != null) {
                deprecatedProperty = true;
            }
        }

    }

    @Override
    public boolean isTransient() {
        return transientProperty;
    }

    @Override
    public boolean isDeprecated() {
        return deprecatedProperty;
    }

    public Method getGetter() {
        return getter;
    }

    public Method getSetter() {
        return setter;
    }

    @Override
    public Object getValue(Object o) {
        if (getter == null) {
            throw new RuntimeException("Field inaccessible : no getter found for field " + getName());
        }
        try {
            return getter.invoke(o);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                e = (Exception) ((InvocationTargetException) e).getTargetException();
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setValue(Object o, Object value) {
        if (setter == null) {
            throw new RuntimeException("Field readonly : no setter found for " + getName() + " in class " + o.getClass());
        }
        try {
            setter.invoke(o, value);
        } catch (Exception e) {
            //throw new IllegalArgumentException("Unable to set value " + (value == null ? "null" : value.getClass()) + " for property " + getName() + ". Expected Type is " + getPlatformType(), e);
            if (e instanceof InvocationTargetException) {
                e = (Exception) ((InvocationTargetException) e).getTargetException();
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isWriteSupported() {
        return setter != null;
    }

    @Override
    public boolean isReadSupported() {
        return getter != null;
    }
}
