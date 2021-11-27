package net.thevpc.nuts.runtime.standalone.util.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * ==================================================================== Nuts :
 * Network Updatable Things Service (universal package manager)
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
/**
 *
 * @author thevpc
 */
public class SimpleParametrizedType implements ParameterizedType {

    private String typeName;
    private Type[] actualTypeArguments;
    private Type rawType;
    private Type ownerType;

    public SimpleParametrizedType(Type rawType, Type... actualTypeArguments) {
        this(rawType, actualTypeArguments, null);
    }

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

}
