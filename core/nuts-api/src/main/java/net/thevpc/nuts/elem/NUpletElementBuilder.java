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
import java.util.Map;
import java.util.function.Consumer;

/**
 * Array element Builder is a mutable NutsArrayElement that helps
 * manipulating arrays.
 *
 * @author thevpc
 * @app.category Elements
 */
public interface NUpletElementBuilder extends NElementBuilder {
    NUpletElementBuilder doWith(Consumer<NUpletElementBuilder> con);

    static NUpletElementBuilder of() {
        return NElements.of().ofUpletBuilder();
    }

    String name();

    NUpletElementBuilder name(String name);

    NUpletElementBuilder removeAt(int index);

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
    NUpletElementBuilder copyFrom(NUpletElement value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder addAll(NElement[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder addAll(Collection<NElement> value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder addAll(String[] value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder add(int value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder add(long value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder add(double value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder add(float value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder add(byte value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder add(boolean value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder add(char value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder add(Number value);

    /**
     * add element to the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder add(String value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder addAll(int[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder addAll(double[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder addAll(long[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder addAll(float[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder addAll(boolean[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder addAll(char[] value);

    /**
     * all all elements in the given array
     *
     * @param value value
     * @return {@code this} instance
     */
    NUpletElementBuilder addAll(byte[] value);

    /**
     * add new element to the end of the array.
     *
     * @param element element to add, should no be null
     * @return {@code this} instance
     * @throws NullPointerException if {@code element==null}
     */
    NUpletElementBuilder add(NElement element);

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
    NUpletElementBuilder insert(int index, NElement element);

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
    NUpletElementBuilder set(int index, NElement element);

    /**
     * remove all elements from this array.
     *
     * @return {@code this} instance
     */
    NUpletElementBuilder clear();

    /**
     * add new element to the end of the array.
     *
     * @param index index to remove
     * @return {@code this} instance
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index > size()})
     */
    NUpletElementBuilder remove(int index);

    /**
     * reset this instance with the given array
     *
     * @param other array
     * @return {@code this} instance
     */
    NUpletElementBuilder copyFrom(NUpletElementBuilder other);

    /**
     * create array with this instance elements
     *
     * @return new array instance
     */
    NUpletElement build();


    NUpletElementBuilder addAnnotations(List<NElementAnnotation> annotations);

    NUpletElementBuilder addAnnotation(NElementAnnotation annotation);

    NUpletElementBuilder addAnnotationAt(int index, NElementAnnotation annotation);

    NUpletElementBuilder removeAnnotationAt(int index);

    NUpletElementBuilder clearAnnotations();

    NUpletElementBuilder addLeadingComment(NElementCommentType type, String text);
    NUpletElementBuilder addTrailingComment(NElementCommentType type, String text);
    NUpletElementBuilder addLeadingComment(NElementComment comment);
    NUpletElementBuilder addLeadingComments(NElementComment... comments);
    NUpletElementBuilder addTrailingComment(NElementComment comment);
    NUpletElementBuilder addTrailingComments(NElementComment... comments);
    NUpletElementBuilder removeLeadingComment(NElementComment comment);
    NUpletElementBuilder removeTrailingComment(NElementComment comment);
    NUpletElementBuilder removeLeadingCommentAt(int index);
    NUpletElementBuilder removeTrailingCommentAt(int index);
    NUpletElementBuilder clearComments();
    NUpletElementBuilder addComments(NElementComments comments);


    NUpletElementBuilder set(String name, NElement value);

    NUpletElementBuilder set(String name, boolean value);

    NUpletElementBuilder set(String name, int value);

    NUpletElementBuilder set(String name, double value);

    NUpletElementBuilder set(String name, String value);

    NUpletElementBuilder set(NElement name, NElement value);

    NUpletElementBuilder add(NElement name, NElement value);

    NUpletElementBuilder add(String name, NElement value);

    NUpletElementBuilder set(NElement name, boolean value);

    NUpletElementBuilder set(NElement name, int value);

    NUpletElementBuilder set(NElement name, double value);

    NUpletElementBuilder set(NElement name, String value);

    NUpletElementBuilder set(NPairElement entry);

    NUpletElementBuilder add(String name, boolean value);

    NUpletElementBuilder add(String name, int value);

    NUpletElementBuilder add(String name, double value);

    NUpletElementBuilder add(String name, String value);

    NUpletElementBuilder addAll(Map<NElement, NElement> other);

}
