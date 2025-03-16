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
package net.thevpc.nuts.reflect;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.util.function.Function;

/**
 *
 * @author thevpc
 * @since 0.8.4
 */
public interface NReflectConfigurationBuilder extends NComponent {

    static NReflectConfigurationBuilder of() {
        return NExtensions.of(NReflectConfigurationBuilder.class);
    }

    NReflectConfiguration build();

    Function<Class, NReflectPropertyAccessStrategy> getPropertyAccessStrategy();

    Function<Class, NReflectPropertyDefaultValueStrategy> getPropertyDefaultValueStrategy();

    NReflectConfigurationBuilder setPropertyAccessStrategy(Function<Class, NReflectPropertyAccessStrategy> propertyAccessStrategy);

    NReflectConfigurationBuilder setPropertyAccessStrategy(NReflectPropertyAccessStrategy propertyAccessStrategy);

    NReflectConfigurationBuilder setPropertyDefaultValueStrategy(NReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy);
    
    NReflectConfigurationBuilder setPropertyDefaultValueStrategy(Function<Class, NReflectPropertyDefaultValueStrategy> propertyDefaultValueStrategy);

    NReflectConfigurationBuilder unsetPropertyAccessStrategy();

    NReflectConfigurationBuilder unsetPropertyDefaultValueStrategy();

}
