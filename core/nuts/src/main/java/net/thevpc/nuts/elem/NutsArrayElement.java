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
package net.thevpc.nuts.elem;

import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.NutsSession;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Array implementation of Nuts Element type. Nuts Element types are generic
 * JSON like parsable objects.
 *
 * @author thevpc
 * @app.category Elements
 * @since 0.5.6
 */
public interface NutsArrayElement extends NutsNavigatableElement, Iterable<NutsElement> {
    static NutsArrayElement ofEmpty(NutsSession session) {
        return NutsElements.of(session).ofEmptyArray();
    }

    /**
     * array items
     *
     * @return array items
     */
    Collection<NutsElement> items();

    Stream<NutsElement> stream();

    /**
     * element at index
     *
     * @param index index
     * @return element at index
     */
    NutsOptional<NutsElement> get(int index);

    NutsOptional<String> getString(int index);

    NutsOptional<Boolean> getBoolean(int index);

    NutsOptional<Byte> getByte(int index);

    NutsOptional<Short> getShort(int index);

    NutsOptional<Integer> getInt(int index);

    NutsOptional<Long> getLong(int index);

    NutsOptional<Float> getFloat(int index);

    NutsOptional<Double> getDouble(int index);

    NutsOptional<Instant> getInstant(int index);

    NutsOptional<NutsArrayElement> getArray(int index);

    NutsOptional<NutsObjectElement> getObject(int index);

    /**
     * return new builder initialized with this instance
     *
     * @return new builder initialized with this instance
     */
    NutsArrayElementBuilder builder();
}
