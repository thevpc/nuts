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
import java.util.function.Consumer;

/**
 * Array element Builder is a mutable NutsArrayElement that helps
 * manipulating arrays.
 *
 * @author thevpc
 * @app.category Elements
 */
public interface NArrayElementBuilder extends NElementBuilder {

    static NArrayElementBuilder of() {
        return NElements.of().ofArrayBuilder();
    }

    NArrayElementBuilder doWith(Consumer<NArrayElementBuilder> con);
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
    NArrayElementBuilder addAll(NElement[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(Collection<NElement> value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(String[] value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(int value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(long value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(double value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(float value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(byte value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(boolean value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(char value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(Number value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder add(String value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(int[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(double[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(long[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(float[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(boolean[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(char[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(byte[] value);

    /**
     * all all elements in the given array builder
     *
     * @param value value
     * @return {@code this} instance
     */
    NArrayElementBuilder addAll(NArrayElementBuilder value);

    /**
     * add new element to the end of the array.
     *
     * @param element element to add, should no be null
     * @return {@code this} instance
     * @throws NullPointerException if {@code element==null}
     */
    NArrayElementBuilder add(NElement element);

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
    NArrayElementBuilder insert(int index, NElement element);

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
    NArrayElementBuilder set(int index, NElement element);

    /**
     * remove all elements from this array.
     *
     * @return {@code this} instance
     */
    NArrayElementBuilder clear();

    /**
     * add new element to the end of the array.
     *
     * @param index index to remove
     * @return {@code this} instance
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index > size()})
     */
    NArrayElementBuilder remove(int index);

    /**
     * reset this instance with the given array
     *
     * @param other array
     * @return {@code this} instance
     */
    NArrayElementBuilder copyFrom(NArrayElementBuilder other);

    /**
     * reset this instance with the given array
     *
     * @param other array builder
     * @return {@code this} instance
     */
    NArrayElementBuilder copyFrom(NArrayElement other);

    /**
     * create array with this instance elements
     *
     * @return new array instance
     */
    NArrayElement build();


    NArrayElementBuilder addAnnotations(List<NElementAnnotation> annotations);
    NArrayElementBuilder addAnnotation(NElementAnnotation annotation);
    NArrayElementBuilder addAnnotation(String name,NElement ...args);
    NArrayElementBuilder addAnnotationAt(int index,NElementAnnotation annotation);
    NArrayElementBuilder removeAnnotationAt(int index);
    NArrayElementBuilder clearAnnotations();

    NArrayElementBuilder addLeadingComment(NElementCommentType type, String text);
    NArrayElementBuilder addTrailingComment(NElementCommentType type, String text);
    NArrayElementBuilder addLeadingComment(NElementComment comment);
    NArrayElementBuilder addLeadingComments(NElementComment... comments);
    NArrayElementBuilder addTrailingComment(NElementComment comment);
    NArrayElementBuilder addTrailingComments(NElementComment... comments);
    NArrayElementBuilder removeLeadingComment(NElementComment comment);
    NArrayElementBuilder removeTrailingComment(NElementComment comment);
    NArrayElementBuilder removeLeadingCommentAt(int index);
    NArrayElementBuilder removeTrailingCommentAt(int index);
    NArrayElementBuilder clearComments();
    NArrayElementBuilder addComments(NElementComments comments);


    NArrayElementBuilder addParams(List<NElement> params) ;
    NArrayElementBuilder addParam(NElement param) ;
    NArrayElementBuilder addParamAt(int index, NElement param) ;
    NArrayElementBuilder removeParamAt(int index) ;
    NArrayElementBuilder clearParams() ;
    List<NElement> params();
    String name() ;
    NArrayElementBuilder name(String name) ;
    boolean isParametrized() ;
    NArrayElementBuilder setParametrized(boolean parametrized);

}
