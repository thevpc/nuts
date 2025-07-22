/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author thevpc
 */
public class MethodReflectProperty3 extends AbstractReflectProperty {

    private Field read;
    private Method write;
    private boolean readAccessible;
    private boolean writeAccessible;

    public MethodReflectProperty3(String name, Field read, Method write, Object cleanInstance, NReflectType type, NReflectPropertyDefaultValueStrategy defaultValueStrategy) {
        if (read != null) {
            this.read = read;
            try {
                this.read.setAccessible(true);
                readAccessible = true;
            } catch (Exception e) {
                //
            }
        }
        if (write != null) {
            this.write = write;
            try {
                this.write.setAccessible(true);
                writeAccessible=true;
            }catch (Exception e){
                //
            }
        }
        init(name,type, cleanInstance, read.getGenericType(),defaultValueStrategy);
    }


    @Override
    public boolean isRead() {
        return readAccessible;
    }

    @Override
    public boolean isWrite() {
        return writeAccessible;
    }

    @Override
    public Object read(Object instance) {
        if(!readAccessible){
            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC("illegal-access in read mode (%s)",toString()));
        }
        try {
            return read.get(instance);
        } catch (IllegalAccessException ex) {
            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC("illegal-access in read mode (%s) %s",toString(),NExceptions.getErrorMessage(ex)), ex);
        }
    }

    @Override
    public void write(Object instance, Object value) {
        if(!writeAccessible){
            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC("illegal-access in write mode (%s)",toString()));
        }
        try {
            write.invoke(instance, value);
        } catch (IllegalAccessException ex) {
            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC("illegal-access in write mode (%s) %s",toString(),NExceptions.getErrorMessage(ex)), ex);
        } catch (InvocationTargetException ex) {
            throw NExceptions.ofSafeIllegalArgumentException(NMsg.ofC("illegal-invocation in write mode (%s) %s",toString(),NExceptions.getErrorMessage(ex)), ex);
        }
    }

}
