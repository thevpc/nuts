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

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class DefaultNReflectRepository implements NReflectRepository {

    private final Map<Type, NReflectType> beans = new HashMap<>();
    private NReflectConfiguration configuration;

    public DefaultNReflectRepository(NSession session) {
        this(NReflectConfigurationBuilder.of(session).build());
    }

    public DefaultNReflectRepository(NReflectConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public DefaultNReflectRepository setConfiguration(NReflectConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    @Override
    public NReflectConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public NReflectType getType(Type clz) {
        NReflectType v = beans.get(clz);
        if (v == null) {
            v = create(clz);
            beans.put(clz, v);
        }
        return v;
    }

    @Override
    public NReflectType getParametrizedType(Type type, Type owner, Type[] params) {
        SimpleParametrizedType s = new SimpleParametrizedType(
                type,
                params,
                owner
        );
        return getType(s);
    }

    private NReflectType create(Type clz) {
        return new DefaultNReflectType(clz, this);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }



}
