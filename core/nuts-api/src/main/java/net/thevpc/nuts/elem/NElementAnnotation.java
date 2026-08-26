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

import net.thevpc.nuts.internal.rpi.NElementRPI;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 * Nuts Element types are generic JSON like parsable objects. elements are a superset of JSON actually
 * that support multiple structured elements including json, xml, etc...
 * Elements are used to provide a convenient way to manipulate structured elements regardless of the underlying
 * format. Hence It's used for converting from json to xml as an example among many other use cases in the NAF
 * (Nuts Application Framework)
 *
 * @author thevpc
 * @app.category Elements
 * @since 0.5.6
 */
public interface NElementAnnotation extends NAffix {
    /**
     * Creates a new instance of of.
     *
     * @param name name
     * @param values values
     * @return of result
     */
    static NElementAnnotation of(String name, NElement... values) {
        return NElementRPI.of().createAnnotation(name, values);
    }

    /**
     * Creates a new instance of of.
     *
     * @param name name
     * @return of result
     */
    static NElementAnnotation of(String name) {
        return NElementRPI.of().createAnnotation(name);
    }


    /**
     * Name.
     *
     * @return name result
     */
    String name();

    /**
     * Affixes.
     *
     * @return affixes result
     */
    List<NBoundAffix> affixes();

    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * Returns the get.
     *
     * @param s s
     * @return get result
     */
    NOptional<NElement> get(String s);

    /**
     * Param.
     *
     * @param index index
     * @return param result
     */
    NOptional<NElement> param(int index);

    /**
     * Param.
     *
     * @param name name
     * @return param result
     */
    NOptional<NElement> param(String name);

    /**
     * Params.
     *
     * @return params result
     */
    NOptional<List<NElement>> params();

    /**
     * Checks if is parametrized.
     *
     * @return is parametrized result
     */
    boolean isParametrized();

    /**
     * Checks if is named.
     *
     * @return is named result
     */
    boolean isNamed();

    /**
     * Builder.
     *
     * @return builder result
     */
    NElementAnnotationBuilder builder();
}
