package net.thevpc.nuts.runtime.standalone.util.reflect;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * ==================================================================== Nuts :
 * Network Updatable Things Service (universal package manager)
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

/**
 *
 * @author thevpc
 */
public class SimpleGenericArrayType implements GenericArrayType {
    private Type genericComponentType;

    public SimpleGenericArrayType(Type genericComponentType) {
        this.genericComponentType = genericComponentType;
    }

    @Override
    public Type getGenericComponentType() {
        return genericComponentType;
    }

    public String toString() {
        Type componentType = this.getGenericComponentType();
        StringBuilder sb = new StringBuilder();
        if (componentType instanceof Class) {
            sb.append(((Class)componentType).getName());
        } else {
            sb.append(componentType.toString());
        }

        sb.append("[]");
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (o instanceof GenericArrayType) {
            GenericArrayType that = (GenericArrayType)o;
            Type thatComponentType = that.getGenericComponentType();
            return this.genericComponentType.equals(thatComponentType);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return this.genericComponentType.hashCode();
    }
}
