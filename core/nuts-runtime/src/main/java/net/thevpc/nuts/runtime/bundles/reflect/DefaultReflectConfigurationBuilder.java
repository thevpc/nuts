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
package net.thevpc.nuts.runtime.bundles.reflect;

import java.util.function.Function;

/**
 *
 * @author thevpc
 */
public class DefaultReflectConfigurationBuilder implements ReflectConfigurationBuilder {

    private Function<Class, ReflectPropertyAccessStrategy> propertyAccessStrategy;
    private Function<Class, ReflectPropertyDefaultValueStrategy> propertyDefaultValueStrategy;

    @Override
    public Function<Class, ReflectPropertyAccessStrategy> getPropertyAccessStrategy() {
        return propertyAccessStrategy;
    }

    @Override
    public ReflectConfigurationBuilder unsetPropertyAccessStrategy() {
        this.propertyAccessStrategy = null;
        return this;
    }

    @Override
    public ReflectConfigurationBuilder setPropertyAccessStrategy(Function<Class, ReflectPropertyAccessStrategy> propertyAccessStrategy) {
        this.propertyAccessStrategy = propertyAccessStrategy;
        return this;
    }

    @Override
    public ReflectConfigurationBuilder setPropertyAccessStrategy(ReflectPropertyAccessStrategy propertyAccessStrategy) {
        this.propertyAccessStrategy = propertyAccessStrategy == null ? null : x -> propertyAccessStrategy;
        return this;
    }

    @Override
    public ReflectConfigurationBuilder unsetPropertyDefaultValueStrategy() {
        this.propertyDefaultValueStrategy = null;
        return this;
    }

    @Override
    public Function<Class, ReflectPropertyDefaultValueStrategy> getPropertyDefaultValueStrategy() {
        return propertyDefaultValueStrategy;
    }

    @Override
    public ReflectConfigurationBuilder setPropertyDefaultValueStrategy(Function<Class, ReflectPropertyDefaultValueStrategy> propertyDefaultValueStrategy) {
        this.propertyDefaultValueStrategy = propertyDefaultValueStrategy;
        return this;
    }

    @Override
    public ReflectConfigurationBuilder setPropertyDefaultValueStrategy(ReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy) {
        this.propertyDefaultValueStrategy = propertyDefaultValueStrategy == null ? null : x -> propertyDefaultValueStrategy;
        return this;
    }

    @Override
    public ReflectConfiguration build() {
        return new DefaultReflectConfiguration(propertyAccessStrategy, propertyDefaultValueStrategy);
    }

}
