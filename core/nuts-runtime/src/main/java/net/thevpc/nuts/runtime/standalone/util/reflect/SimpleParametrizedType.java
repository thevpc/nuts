package net.thevpc.nuts.runtime.standalone.util.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

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

/**
 * @author thevpc
 */
public class SimpleParametrizedType implements ParameterizedType {

    private String typeName;
    private Type[] actualTypeArguments;
    private Type rawType;
    private Type ownerType;

    public SimpleParametrizedType(Type rawType, Type[] actualTypeArguments, Type ownerType) {
        this.actualTypeArguments = actualTypeArguments;
        this.rawType = rawType;
        this.ownerType = ownerType;
        StringBuilder sb = new StringBuilder();
        sb.append(rawType);
        sb.append("<");
        for (int i = 0; i < actualTypeArguments.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            Type actualTypeArgument = actualTypeArguments[i];
            sb.append(actualTypeArgument);
        }
        sb.append(">");
        typeName = sb.toString();
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ParameterizedType) {
            // Check that information is equivalent
            ParameterizedType that = (ParameterizedType) o;

            if (this == that)
                return true;

            Type thatOwner = that.getOwnerType();
            Type thatRawType = that.getRawType();

//            if (false) { // Debugging
//                boolean ownerEquality = (ownerType == null ?
//                        thatOwner == null :
//                        ownerType.equals(thatOwner));
//                boolean rawEquality = (rawType == null ?
//                        thatRawType == null :
//                        rawType.equals(thatRawType));
//
//                boolean typeArgEquality = Arrays.equals(actualTypeArguments, // avoid clone
//                        that.getActualTypeArguments());
//                for (Type t : actualTypeArguments) {
//                    System.out.printf("\t\t%s%s%n", t, t.getClass());
//                }
//
//                System.out.printf("\towner %s\traw %s\ttypeArg %s%n",
//                        ownerEquality, rawEquality, typeArgEquality);
//                return ownerEquality && rawEquality && typeArgEquality;
//            }

            return
                    Objects.equals(ownerType, thatOwner) &&
                            Objects.equals(rawType, thatRawType) &&
                            Arrays.equals(actualTypeArguments, // avoid clone
                                    that.getActualTypeArguments());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return
                Arrays.hashCode(actualTypeArguments) ^
                        Objects.hashCode(ownerType) ^
                        Objects.hashCode(rawType);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (ownerType != null) {
            if (ownerType instanceof Class) {
                sb.append(((Class) ownerType).getName());
            } else {
                sb.append(ownerType.toString());
            }
            sb.append("$");
            sb.append(rawType.getTypeName());
        } else {
            sb.append(rawType.getTypeName());
        }

        if (actualTypeArguments != null &&
                actualTypeArguments.length > 0) {
            sb.append("<");
            boolean first = true;
            for (Type t : actualTypeArguments) {
                if (!first)
                    sb.append(", ");
                sb.append(t.getTypeName());
                first = false;
            }
            sb.append(">");
        }

        return sb.toString();
    }

}
