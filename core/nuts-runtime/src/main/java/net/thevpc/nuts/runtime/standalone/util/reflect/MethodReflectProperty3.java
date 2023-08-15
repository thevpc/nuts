/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.util.reflect;

import net.thevpc.nuts.reflect.NReflectPropertyDefaultValueStrategy;
import net.thevpc.nuts.reflect.NReflectType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author thevpc
 */
public class MethodReflectProperty3 extends AbstractReflectProperty {

    private Field read;
    private Method write;

    public MethodReflectProperty3(String name, Field read, Method write, Object cleanInstance, NReflectType type, NReflectPropertyDefaultValueStrategy defaultValueStrategy) {
        this.read = read;
        this.read.setAccessible(true);
        if (write != null) {
            this.write = write;
            this.write.setAccessible(true);
        }
        init(name,type, cleanInstance, read.getGenericType(),defaultValueStrategy);
    }


    @Override
    public boolean isRead() {
        return true;
    }

    @Override
    public boolean isWrite() {
        return write != null;
    }

    @Override
    public Object read(Object instance) {
        try {
            return read.get(instance);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("illegal-access", ex);
        }
    }

    @Override
    public void write(Object instance, Object value) {
        try {
            write.invoke(instance, value);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("illegal-access", ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalArgumentException("illegal-invocation", ex);
        }
    }

}
