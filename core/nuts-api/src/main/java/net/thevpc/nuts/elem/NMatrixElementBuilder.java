/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.elem;

import java.util.Collection;
import java.util.List;

/**
 * Array element Builder is a mutable NutsArrayElement that helps
 * manipulating arrays.
 *
 * @author thevpc
 * @app.category Elements
 */
public interface NMatrixElementBuilder extends NElementBuilder {

    static NMatrixElementBuilder of() {
        return NElements.of().ofMatrixBuilder();
    }

    /**
     * array items
     *
     * @return array items
     */
    List<NElement> items();

    /**
     * element count
     *
     * @return element count
     */
    int size();


    /**
     * element at index
     *
     * @param index index
     * @return element at index
     */
    NElement get(int index);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder addAll(NArrayElement value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder addAll(NElement[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder addAll(Collection<NElement> value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder addAll(String[] value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder add(int value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder add(long value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder add(double value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder add(float value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder add(byte value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder add(boolean value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder add(char value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder add(Number value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder add(String value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder addAll(int[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder addAll(double[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder addAll(long[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder addAll(float[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder addAll(boolean[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder addAll(char[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder addAll(byte[] value);

    /**
     * all all elements in the given array builder
     *
     * @param value value
     * @return {@code this} instance
     */
    NMatrixElementBuilder addAll(NMatrixElementBuilder value);

    /**
     * add new element to the end of the array.
     *
     * @param element element to add, should no be null
     * @return {@code this} instance
     * @throws NullPointerException if {@code element==null}
     */
    NMatrixElementBuilder add(NElement element);

    /**
     * insert new element at the given index.
     *
     * @param index   index to insert into
     * @param element element to add, should no be null
     * @return {@code this} instance
     * @throws NullPointerException      if {@code element==null}
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index > size()})
     */
    NMatrixElementBuilder insert(int index, NElement element);

    /**
     * update element at the given index.
     *
     * @param index   index to update
     * @param element element to add, should no be null
     * @return {@code this} instance
     * @throws NullPointerException      if {@code element==null}
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    NMatrixElementBuilder set(int index, NElement element);

    /**
     * remove all elements from this array.
     *
     * @return {@code this} instance
     */
    NMatrixElementBuilder clear();

    /**
     * add new element to the end of the array.
     *
     * @param index index to remove
     * @return {@code this} instance
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index > size()})
     */
    NMatrixElementBuilder remove(int index);

    /**
     * reset this instance with the given array
     *
     * @param other array
     * @return {@code this} instance
     */
    NMatrixElementBuilder set(NMatrixElementBuilder other);

    /**
     * reset this instance with the given array
     *
     * @param other array builder
     * @return {@code this} instance
     */
    NMatrixElementBuilder set(NArrayElement other);

    /**
     * create array with this instance elements
     *
     * @return new array instance
     */
    NArrayElement build();


    NMatrixElementBuilder addAnnotations(List<NElementAnnotation> annotations);
    NMatrixElementBuilder addAnnotation(NElementAnnotation annotation);
    NMatrixElementBuilder addAnnotationAt(int index, NElementAnnotation annotation);
    NMatrixElementBuilder removeAnnotationAt(int index);
    NMatrixElementBuilder clearAnnotations();

    NMatrixElementBuilder addArgs(List<NElement> args) ;
    NMatrixElementBuilder addArg(NElement arg) ;
    NMatrixElementBuilder addArgAt(int index, NElement arg) ;
    NMatrixElementBuilder removeArgAt(int index) ;
    NMatrixElementBuilder clearArgs() ;
    List<NElement> getArgs();
    String getName() ;
    NMatrixElementBuilder setName(String name) ;
    boolean isWithArgs() ;
    NMatrixElementBuilder setHasArgs(boolean hasArgs);

}
