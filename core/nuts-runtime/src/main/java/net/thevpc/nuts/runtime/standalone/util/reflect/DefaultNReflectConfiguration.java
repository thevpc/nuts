/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
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

import net.thevpc.nuts.reflect.NReflectConfiguration;
import net.thevpc.nuts.reflect.NReflectPropertyAccessStrategy;
import net.thevpc.nuts.reflect.NReflectPropertyDefaultValueStrategy;

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
        if (clz == null || propertyAccessStrategy == null) {
            return NReflectPropertyAccessStrategy.FIELD;
        }
        NReflectPropertyAccessStrategy v = propertyAccessStrategy.apply(clz);
        return v != null ? v : NReflectPropertyAccessStrategy.FIELD;
    }

    @Override
    public NReflectPropertyDefaultValueStrategy getDefaultValueStrategy(Class clz) {
        if (clz == null || propertyAccessStrategy == null) {
            return NReflectPropertyDefaultValueStrategy.TYPE_DEFAULT;
        }
        NReflectPropertyDefaultValueStrategy v = propertyDefaultValueStrategy.apply(clz);
        return v != null ? v : NReflectPropertyDefaultValueStrategy.PROPERTY_DEFAULT;
    }

}
