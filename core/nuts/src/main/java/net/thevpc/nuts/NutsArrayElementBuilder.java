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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts;

import java.util.List;

/**
 * Array element Builder is a mutable NutsArrayElement that helps 
 * manipulating arrays.
 * @author vpc
 * @category Elements
 */
public interface NutsArrayElementBuilder {

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
