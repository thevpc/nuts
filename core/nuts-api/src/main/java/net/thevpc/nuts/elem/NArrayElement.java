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
package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Array implementation of Nuts Element type. Nuts Element types are generic
 * JSON like parsable objects.
 *
 * @author thevpc
 * @app.category Elements
 * @since 0.5.6
 */
public interface NArrayElement extends NNavigatableElement, Iterable<NElement> {
    static NArrayElement ofEmpty() {
        return NElements.of().ofEmptyArray();
    }

    /**
     * array items
     *
     * @return array items
     */
    Collection<NElement> items();

    Stream<NElement> stream();

    /**
     * element at index
     *
     * @param index index
     * @return element at index
     */
    NOptional<NElement> get(int index);

    NOptional<String> getString(int index);

    NOptional<Boolean> getBoolean(int index);

    NOptional<Byte> getByte(int index);

    NOptional<Short> getShort(int index);

    NOptional<Integer> getInt(int index);

    NOptional<Long> getLong(int index);

    NOptional<Float> getFloat(int index);

    NOptional<Double> getDouble(int index);

    NOptional<Instant> getInstant(int index);

    NOptional<NArrayElement> getArray(int index);

    NOptional<NObjectElement> getObject(int index);

    String name() ;

    boolean isNamed() ;

    boolean isParametrized() ;

    List<NElement> params() ;

    int paramsCount() ;

    NElement param(int index) ;

    /**
     * return new builder initialized with this instance
     *
     * @return new builder initialized with this instance
     */
    NArrayElementBuilder builder();
}
