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
 *
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

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Class responsible for manipulating {@link NElement} type. It helps parsing
 * from, converting to and formatting such types.
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
public interface NElements extends NComponent {


    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NElements of() {
        return NExtensions.of(NElements.class);
    }


    /**
     * Checks if is ntf.
     *
     * @return is ntf result
     */
    boolean isNtf();

    /**
     * Sets the ntf.
     *
     * @param ntf ntf
     * @return set ntf result
     */
    NElements setNtf(boolean ntf);

    /**
     *
     * convert element to the specified object if applicable or throw an
     * exception.
     *
     * @param <T> return type
     * @param any element to convert
     * @param to  class type
     * @return instance of type {@code T} converted from {@code element}
     */
    <T> T convert(Object any, Class<T> to);

    /**
     * simplify an object is to convert it to a simple object composed only of :
     * <ul>
     * <li>boxed primitives</li>
     * <li>simple objects like String,Date,Instant and Path</li>
     * <li>Map</li>
     * <li>Map.Entry</li>
     * <li>List</li>
     * </ul>
     *
     * @param any object
     * @return atomized object
     */
    Object toSimple(Object any);

    /**
     * Converts to element.
     *
     * @param any any
     * @return to element result
     */
    NElement toElement(Object any);


    /**
     * From element.
     *
     * @param o o
     * @param to to
     * @return from element result
     */
    <T> T fromElement(NElement o, Class<T> to);


    /**
     * Do with mapper store.
     *
     * @param doWith do with
     * @return do with mapper store result
     */
    NElements doWithMapperStore(Consumer<NElementMapperStore> doWith);

    /**
     * Mapper store.
     *
     * @return mapper store result
     */
    NElementMapperStore mapperStore();
}
