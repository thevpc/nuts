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

import net.thevpc.nuts.util.NReflectPropertyDefaultValueStrategy;
import net.thevpc.nuts.util.NReflectType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 *
 * @author thevpc
 */
public class FieldReflectProperty extends AbstractReflectProperty {

    private Field field;

    public FieldReflectProperty(Field field, Object cleanInstance, NReflectType type, NReflectPropertyDefaultValueStrategy defaultValueStrategy) {
        this.field = field;
        field.setAccessible(true);
        init(field.getName(),type, cleanInstance, field.getGenericType(),defaultValueStrategy);
    }



    @Override
    public boolean isRead() {
        return true;
    }

    @Override
    public boolean isWrite() {
        return !Modifier.isFinal(field.getModifiers());
    }

    @Override
    public Object read(Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("illegal-access", ex);
        }
    }

    @Override
    public void write(Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("illegal-access", ex);
        } catch (IllegalArgumentException ex) {
            throw ex;
        }
    }

}
