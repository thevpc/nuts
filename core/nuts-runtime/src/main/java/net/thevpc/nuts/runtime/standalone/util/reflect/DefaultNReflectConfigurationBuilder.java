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

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.reflect.NReflectConfiguration;
import net.thevpc.nuts.reflect.NReflectConfigurationBuilder;
import net.thevpc.nuts.reflect.NReflectPropertyAccessStrategy;
import net.thevpc.nuts.reflect.NReflectPropertyDefaultValueStrategy;

import java.util.function.Function;

/**
 *
 * @author thevpc
 */
public class DefaultNReflectConfigurationBuilder implements NReflectConfigurationBuilder {

    private Function<Class, NReflectPropertyAccessStrategy> propertyAccessStrategy;
    private Function<Class, NReflectPropertyDefaultValueStrategy> propertyDefaultValueStrategy;

    @Override
    public Function<Class, NReflectPropertyAccessStrategy> getPropertyAccessStrategy() {
        return propertyAccessStrategy;
    }

    @Override
    public NReflectConfigurationBuilder unsetPropertyAccessStrategy() {
        this.propertyAccessStrategy = null;
        return this;
    }

    @Override
    public NReflectConfigurationBuilder setPropertyAccessStrategy(Function<Class, NReflectPropertyAccessStrategy> propertyAccessStrategy) {
        this.propertyAccessStrategy = propertyAccessStrategy;
        return this;
    }

    @Override
    public NReflectConfigurationBuilder setPropertyAccessStrategy(NReflectPropertyAccessStrategy propertyAccessStrategy) {
        this.propertyAccessStrategy = propertyAccessStrategy == null ? null : x -> propertyAccessStrategy;
        return this;
    }

    @Override
    public NReflectConfigurationBuilder unsetPropertyDefaultValueStrategy() {
        this.propertyDefaultValueStrategy = null;
        return this;
    }

    @Override
    public Function<Class, NReflectPropertyDefaultValueStrategy> getPropertyDefaultValueStrategy() {
        return propertyDefaultValueStrategy;
    }

    @Override
    public NReflectConfigurationBuilder setPropertyDefaultValueStrategy(Function<Class, NReflectPropertyDefaultValueStrategy> propertyDefaultValueStrategy) {
        this.propertyDefaultValueStrategy = propertyDefaultValueStrategy;
        return this;
    }

    @Override
    public NReflectConfigurationBuilder setPropertyDefaultValueStrategy(NReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy) {
        this.propertyDefaultValueStrategy = propertyDefaultValueStrategy == null ? null : x -> propertyDefaultValueStrategy;
        return this;
    }

    @Override
    public NReflectConfiguration build() {
        return new DefaultNReflectConfiguration(propertyAccessStrategy, propertyDefaultValueStrategy);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
