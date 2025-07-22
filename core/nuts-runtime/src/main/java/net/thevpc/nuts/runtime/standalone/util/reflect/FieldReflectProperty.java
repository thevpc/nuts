/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.util.reflect;

import net.thevpc.nuts.NExceptions;
import net.thevpc.nuts.reflect.NReflectPropertyDefaultValueStrategy;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.util.NMsg;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author thevpc
 */
public class FieldReflectProperty extends AbstractReflectProperty {

    private Field field;
    private boolean accessible;

    public FieldReflectProperty(Field field, Object cleanInstance, NReflectType declaringType, NReflectPropertyDefaultValueStrategy defaultValueStrategy) {
        if (field != null) {
            this.field = field;
            try {
                field.setAccessible(true);
                accessible = true;
            } catch (Exception e) {
                //ignore
            }
            init(field.getName(), declaringType, cleanInstance, field.getGenericType(), defaultValueStrategy);
        }
    }


    @Override
    public boolean isRead() {
        return accessible;
    }

    @Override
    public boolean isWrite() {
        return accessible && !Modifier.isFinal(field.getModifiers());
    }

    @Override
    public Object read(Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException ex) {
            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC("illegal-access (%s) %s", toString(), NExceptions.getErrorMessage(ex)), ex);
        }
    }

    @Override
    public void write(Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException ex) {
            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC("illegal-access (%s) %s", toString(), NExceptions.getErrorMessage(ex)), ex);
        } catch (IllegalArgumentException ex) {
            throw ex;
        }
    }

}
