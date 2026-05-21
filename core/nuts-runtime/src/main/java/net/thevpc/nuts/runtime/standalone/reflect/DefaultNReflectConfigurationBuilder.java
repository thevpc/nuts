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
package net.thevpc.nuts.runtime.standalone.reflect;

import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.reflect.NReflectConfiguration;
import net.thevpc.nuts.reflect.NReflectConfigurationBuilder;
import net.thevpc.nuts.reflect.NReflectPropertyAccessStrategy;
import net.thevpc.nuts.reflect.NReflectPropertyDefaultValueStrategy;

import java.util.Set;
import java.util.function.Function;

/**
 *
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNReflectConfigurationBuilder implements NReflectConfigurationBuilder {

    private Function<Class, Set<NReflectPropertyAccessStrategy>> propertyAccessStrategy;
    private Function<Class, NReflectPropertyDefaultValueStrategy> propertyDefaultValueStrategy;

    @Override
    public Function<Class, Set<NReflectPropertyAccessStrategy>> propertyAccessStrategy() {
        return propertyAccessStrategy;
    }

    @Override
    public NReflectConfigurationBuilder unsetPropertyAccessStrategy() {
        this.propertyAccessStrategy = null;
        return this;
    }

    @Override
    public NReflectConfigurationBuilder propertyAccessStrategy(Function<Class, Set<NReflectPropertyAccessStrategy>> propertyAccessStrategy) {
        this.propertyAccessStrategy = propertyAccessStrategy;
        return this;
    }

    @Override
    public NReflectConfigurationBuilder propertyAccessStrategy(Set<NReflectPropertyAccessStrategy> propertyAccessStrategy) {
        this.propertyAccessStrategy = propertyAccessStrategy == null ? null : x -> propertyAccessStrategy;
        return this;
    }

    @Override
    public NReflectConfigurationBuilder unsetPropertyDefaultValueStrategy() {
        this.propertyDefaultValueStrategy = null;
        return this;
    }

    @Override
    public Function<Class, NReflectPropertyDefaultValueStrategy> propertyDefaultValueStrategy() {
        return propertyDefaultValueStrategy;
    }

    @Override
    public NReflectConfigurationBuilder propertyDefaultValueStrategy(Function<Class, NReflectPropertyDefaultValueStrategy> propertyDefaultValueStrategy) {
        this.propertyDefaultValueStrategy = propertyDefaultValueStrategy;
        return this;
    }

    @Override
    public NReflectConfigurationBuilder propertyDefaultValueStrategy(NReflectPropertyDefaultValueStrategy propertyDefaultValueStrategy) {
        this.propertyDefaultValueStrategy = propertyDefaultValueStrategy == null ? null : x -> propertyDefaultValueStrategy;
        return this;
    }

    @Override
    public NReflectConfiguration build() {
        return new DefaultNReflectConfiguration(propertyAccessStrategy, propertyDefaultValueStrategy);
    }

}
