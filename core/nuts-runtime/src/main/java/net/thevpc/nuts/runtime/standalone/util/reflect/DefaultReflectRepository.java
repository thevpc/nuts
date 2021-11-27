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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class DefaultReflectRepository implements ReflectRepository {

    private final Map<Type, ReflectType> beans = new HashMap<>();
    private final ReflectConfiguration configuration;

    public DefaultReflectRepository(ReflectConfiguration configurer) {
        this.configuration = configurer == null ? ReflectConfigurationBuilder.create().build() : configurer;
    }

    @Override
    public ReflectConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public ReflectType getType(Type clz) {
        ReflectType v = beans.get(clz);
        if (v == null) {
            v = create(clz);
            beans.put(clz, v);
        }
        return v;
    }

    private ReflectType create(Type clz) {
        Class raw = ReflectUtils.getRawClass(clz);
        ReflectPropertyAccessStrategy a = configuration.getAccessStrategy(raw);
        ReflectPropertyDefaultValueStrategy d = configuration.getDefaultValueStrategy(raw);
        return new ClassReflectType(clz, a, d, this);
    }
}
