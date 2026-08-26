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

import java.util.List;
import java.util.stream.Stream;

/**
 * Array implementation of Nuts Element type. Nuts Element types are generic
 * JSON like parsable objects.
 *
 * @author thevpc
 * @app.category Elements
 * @since 0.8.9
 */
public interface NFlatExprElement extends NElement, Iterable<NElement> {
    /**
     * Returns the get.
     *
     * @param index index
     * @return get result
     */
    NOptional<NElement> get(int index);

    /**
     * Children.
     *
     * @return children result
     */
    List<NElement> children();

    /**
     * Stream.
     *
     * @return stream result
     */
    Stream<NElement> stream();

    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    boolean isEmpty();

    /**
     * Builder.
     *
     * @return builder result
     */
    NFlatExprElementBuilder builder();

    /**
     * Reshape.
     *
     * @param reshaper reshaper
     * @return reshape result
     */
    default NElement reshape(NExprElementReshaper reshaper) {
        return reshaper == null ? this : reshaper.reshape(this);
    }

    /**
     * Reshape.
     *
     * @return reshape result
     */
    default NElement reshape() {
        /**
         * Reshape.
         *
         * @param NExprElementReshaperType.DEFAULT n expr element reshaper type.default
         * @return reshape result
         */
        return reshape(NExprElementReshaperType.DEFAULT);
    }

    /**
     * Reshape.
     *
     * @param reshaper reshaper
     * @return reshape result
     */
    default NElement reshape(NExprElementReshaperType reshaper) {
        return reshaper == null ? this : NExprElementReshaper.of(reshaper).reshape(this);
    }
}
