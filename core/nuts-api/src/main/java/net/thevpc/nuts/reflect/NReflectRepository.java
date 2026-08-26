/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
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
package net.thevpc.nuts.reflect;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.internal.rpi.NReflectRPI;
import net.thevpc.nuts.spi.NComponent;

import java.lang.reflect.Type;

/**
 * @author thevpc
 * @since 0.8.4
 */
public interface NReflectRepository extends NComponent {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NReflectRepository of() {
        return NExtensions.of(NReflectRPI.class).getDefaultReflectRepository();
    }

    /**
     * Creates a new instance of of.
     *
     * @param config config
     * @return of result
     */
    static NReflectRepository of(NReflectConfiguration config) {
        return NExtensions.of().createSupported(NReflectRepository.class,config).get();
    }

    /**
     * Returns the parametrized type.
     *
     * @param clazz clazz
     * @param owner owner
     * @param params params
     * @return get parametrized type result
     */
    NReflectType getParametrizedType(Type clazz, Type owner, Type[] params);

    /**
     * Returns the type.
     *
     * @param clazz clazz
     * @return get type result
     */
    NReflectType getType(Type clazz);

    /**
     * Configuration.
     *
     * @return configuration result
     */
    NReflectConfiguration configuration();
}
