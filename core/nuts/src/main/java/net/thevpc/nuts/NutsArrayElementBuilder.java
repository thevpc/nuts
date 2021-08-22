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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts;

import java.util.List;

/**
 * Array element Builder is a mutable NutsArrayElement that helps 
 * manipulating arrays.
 * @author thevpc
 * @app.category Elements
 */
public interface NutsArrayElementBuilder extends NutsElementBuilder{

    static NutsArrayElementBuilder of(NutsSession session){
        PrivateNutsUtils.checkSession(session);
        return session.getWorkspace().elem().forArray();
    }

    /**
     * array items
     * @return array items
     */
    List<NutsElement> children();

    /**
     * element count
     * @return element count
     */
    int size();


    /**
     * element at index
     * @param index index
     * @return element at index
     */
    NutsElement get(int index);

    /**
     * all all elements in the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder addAll(NutsArrayElement value);

    /**
     * all all elements in the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder addAll(NutsElement[] value);

    /**
     * all all elements in the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder addAll(String[] value);

    /**
     * add element to the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder add(int value);

    /**
     * add element to the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder add(long value);

    /**
     * add element to the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder add(double value);

    /**
     * add element to the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder add(float value);

    /**
     * add element to the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder add(byte value);

    /**
     * add element to the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder add(boolean value);

    /**
     * add element to the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder add(char value);

    /**
     * add element to the given array
     * @param value value
     * @return {@code this} instance
     */
    public NutsArrayElementBuilder add(Number value);

    /**
     * add element to the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder add(String value);

    /**
     * all all elements in the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder addAll(int[] value);

    /**
     * all all elements in the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder addAll(double[] value);

    /**
     * all all elements in the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder addAll(long[] value);

    /**
     * all all elements in the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder addAll(float[] value);

    /**
     * all all elements in the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder addAll(boolean[] value);

    /**
     * all all elements in the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder addAll(char[] value);

    /**
     * all all elements in the given array
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder addAll(byte[] value);

    /**
     * all all elements in the given array builder
     * @param value value
     * @return {@code this} instance
     */
    NutsArrayElementBuilder addAll(NutsArrayElementBuilder value);

    /**
     * add new element to the end of the array. 
     *
     * @param element element to add, should no be null
     * @return {@code this} instance
     * @throws NullPointerException if {@code element==null}
     */
    NutsArrayElementBuilder add(NutsElement element);

    /**
     * insert new element at the given index.
     *
     * @param index index to insert into
     * @param element element to add, should no be null
     * @return {@code this} instance
     * @throws NullPointerException if {@code element==null}
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index > size()})
     */
    NutsArrayElementBuilder insert(int index, NutsElement element);

    /**
     * update element at the given index.
     *
     * @param index index to update
     * @param element element to add, should no be null
     * @return {@code this} instance
     * @throws NullPointerException if {@code element==null}
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     */
    NutsArrayElementBuilder set(int index, NutsElement element);

    /**
     * remove all elements from this array.
     *
     * @return {@code this} instance
     */
    NutsArrayElementBuilder clear();

    /**
     * add new element to the end of the array. 
     *
     * @param index index to remove
     * @return {@code this} instance
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index > size()})
     */
    NutsArrayElementBuilder remove(int index);

    /**
     * reset this instance with the given array
     * @param other array
     * @return {@code this} instance
     */
    NutsArrayElementBuilder set(NutsArrayElementBuilder other);

    /**
     * reset this instance with the given array
     * @param other array builder
     * @return {@code this} instance
     */
    NutsArrayElementBuilder set(NutsArrayElement other);

    /**
     * create array with this instance elements
     * @return new array instance
     */
    NutsArrayElement build();
}
