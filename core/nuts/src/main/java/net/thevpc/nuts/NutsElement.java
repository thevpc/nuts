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
package net.thevpc.nuts;

/**
 * Nuts Element types are generic JSON like parsable objects. elements are a superset of JSON actually
 * that support multiple structured elements including json, xml, etc...
 * Elements are used to provide a convenient way to manipulate structured elements regardless of the underlying
 * format. Hence it is used for converting from json to xml as an example among many other use cases in the NAF
 * (Nuts Application Framework)
 *
 * @author thevpc
 * @app.category Elements
 * @since 0.5.6
 */
public interface NutsElement extends NutsDescribable, NutsBlankable,NutsValue {

    /**
     * element type
     *
     * @return element type
     */
    NutsElementType type();

    /**
     * convert this element to {@link NutsPrimitiveElement} or throw
     * ClassCastException
     *
     * @return {@link NutsPrimitiveElement}
     */
    NutsOptional<NutsPrimitiveElement> asPrimitive();

    /**
     * cast this element to {@link NutsObjectElement} or throw
     * ClassCastException
     *
     * @return {@link NutsObjectElement}
     */
    NutsOptional<NutsObjectElement> asObject();

    /**
     * cast this element to {@link NutsObjectElement} or throw
     * ClassCastException
     *
     * @return {@link NutsObjectElement}
     */
    NutsOptional<NutsNavigatableElement> asNavigatable();

    /**
     * cast this element to {@link NutsCustomElement} or throw
     * ClassCastException
     *
     * @return {@link NutsObjectElement}
     */
    NutsOptional<NutsCustomElement> asCustom();

    /**
     * true if can be cast to a custom element
     * @return true if can be cast to a custom element
     */
    boolean isCustom();

    /**
     * convert this element to {@link NutsArrayElement} or throw
     * ClassCastException
     *
     * @return {@link NutsArrayElement}
     */
    NutsOptional<NutsArrayElement> asArray();

    /**
     * return true if this element can be cast to {@link NutsPrimitiveElement}
     * @return true if this element can be cast to {@link NutsPrimitiveElement}
     */
    boolean isPrimitive();

    /**
     * return true if this element can be cast to {@link NutsObjectElement}
     * @return true if this element can be cast to {@link NutsObjectElement}
     */
    boolean isObject();

    /**
     * return true if this element can be cast to {@link NutsArrayElement}
     * @return true if this element can be cast to {@link NutsArrayElement}
     */
    boolean isArray();


    /**
     * return true if this element is empty:
     * <ul>
     *     <li>primitives are empty only if they are null or an empty string</li>
     *     <li>objects are empty if they do not have any field</li>
     *     <li>arrays are empty if they do not have any item</li>
     *     <li>customs are NEVER empty</li>
     * </ul>
     *
     * @return return true if this element is empty
     */
    boolean isEmpty();

    /**
     * return true if this element is blank:
     * <ul>
     *     <li>primitives are blank only if they are null or a blank string</li>
     *     <li>objects are blank if they do not have any field</li>
     *     <li>arrays are blank if they do not have any item</li>
     *     <li>customs are NEVER blank</li>
     * </ul>
     *
     * @return return true if this element is blank
     */
    @Override
    boolean isBlank();
}
