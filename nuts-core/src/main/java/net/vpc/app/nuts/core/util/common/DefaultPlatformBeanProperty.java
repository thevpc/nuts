/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.util.common;

import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.core.JsonTransient;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DefaultPlatformBeanProperty extends AbstractPlatformBeanProperty {

    private final Method getter;
    private final Method setter;
    private final Field javaField;
    private boolean transientProperty;
    private boolean deprecatedProperty;

    public DefaultPlatformBeanProperty(String field, Class fieldType, Field jfield, Method getter, Method setter) {
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
            throw new NutsIllegalArgumentException("Field inaccessible : no getter found for field " + getName());
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
            throw new NutsException(e);
        }
    }

    @Override
    public void setValue(Object o, Object value) {
        if (setter == null) {
            throw new NutsException("Field readonly : no setter found for " + getName() + " in class " + o.getClass());
        }
        try {
            setter.invoke(o, value);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                e = (Exception) ((InvocationTargetException) e).getTargetException();
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new NutsIllegalArgumentException(e);
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
