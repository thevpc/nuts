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

import net.thevpc.nuts.util.NReflectConfiguration;
import net.thevpc.nuts.util.NReflectPropertyAccessStrategy;
import net.thevpc.nuts.util.NReflectPropertyDefaultValueStrategy;

import java.util.function.Function;

/**
 *
 * @author thevpc
 */
public class DefaultNReflectConfiguration implements NReflectConfiguration {

    private Function<Class, NReflectPropertyAccessStrategy> propertyAccessStrategy;
    private Function<Class, NReflectPropertyDefaultValueStrategy> propertyDefaultValueStrategy;

    public DefaultNReflectConfiguration(Function<Class, NReflectPropertyAccessStrategy> propertyAccessStrategy, Function<Class, NReflectPropertyDefaultValueStrategy> propertyDefaultValueStrategy) {
        this.propertyAccessStrategy = propertyAccessStrategy;
        this.propertyDefaultValueStrategy = propertyDefaultValueStrategy;
    }

    @Override
    public NReflectPropertyAccessStrategy getAccessStrategy(Class clz) {
        if (propertyAccessStrategy == null) {
            return NReflectPropertyAccessStrategy.FIELD;
        }
        NReflectPropertyAccessStrategy v = propertyAccessStrategy.apply(clz);
        return v != null ? v : NReflectPropertyAccessStrategy.FIELD;
    }

    @Override
    public NReflectPropertyDefaultValueStrategy getDefaultValueStrategy(Class clz) {
        if (propertyAccessStrategy == null) {
            return NReflectPropertyDefaultValueStrategy.TYPE_DEFAULT;
        }
        NReflectPropertyDefaultValueStrategy v = propertyDefaultValueStrategy.apply(clz);
        return v != null ? v : NReflectPropertyDefaultValueStrategy.PROPERTY_DEFAULT;
    }

}
