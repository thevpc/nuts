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

import net.thevpc.nuts.boot.NutsApiUtils;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * this class represents an <strong>immutable</strong> string representation of a version parsed as a suite of alternating numbers and words.
 * Parsing algorithm is simply to split whenever word type changes.
 * Examples:
 * <ul>
 *     <li>1 = [1]</li>
 *     <li>1.2 = [1,'.',2]</li>
 *     <li>10.20update3 = [10,'.',20,'update',3]</li>
 * </ul>
 * @author thevpc
 * @since 0.5.4
 * @app.category Descriptor
 */
public interface NutsVersion extends Serializable, NutsTokenFilter, NutsFormattable, Comparable<NutsVersion> {

    static NutsVersion parse(String str, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().version().parse(str);
    }

    /**
     * return string representation of the version
     * @return string representation of the version (never null)
     */
    String getValue();

    /**
     * compare this version to the other version
     * @param other other version
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    int compareTo(String other);

    @Override
    int compareTo(NutsVersion other);

    /**
     * parse the current version as new instance of {@link NutsVersionFilter}
     * @return new instance of {@link NutsVersionFilter}
     */
    NutsVersionFilter filter();

    NutsVersionFilter filterCompat();

    /**
     * parse the current version as an interval array
     * @return new interval array
     */
    NutsVersionInterval[] intervals();

    /**
     * return true if this version denotes as single value and does not match an interval.
     * @return true if this version denotes as single value and does not match an interval.
     */
    boolean isSingleValue();

    /**
     * return true if this is a filter
     * @return true if this is a filter
     */
    boolean isFilter();


    /**
     * increment the last number in the version with 1
     * @return new version incrementing the last number
     */
    NutsVersion inc();

    /**
     * increment the number at {@code position}  in the version with 1
     * @param position number position
     * @return new version incrementing the last number
     */
    NutsVersion inc(int position);

    /**
     * increment the last number in the version with the given {@code amount}
     * @param position number position
     * @param amount amount of the increment
     * @return new version incrementing the last number
     */
    NutsVersion inc(int position, long amount);

    NutsVersion inc(int position, BigInteger amount);

    /**
     * number of elements in the version.
     * <ul>
     *     <li>size(1.22)=3 {'1','.','22'}</li>
     *     <li>size(1.22_u1)=5 {'1','.','22','_u','1'}</li>
     * </ul>
     * @return number of elements in the version.
     */
    int size();

    /**
     * number of elements in the version.
     * <ul>
     *     <li>numberSize(1.22)=2 {1,22}</li>
     *     <li>numberSize(1.22_u1)=3 {1,22,1}</li>
     * </ul>
     * @return number of elements in the version.
     */
    int numberSize();

    /**
     * element at given index. if the index is negative will return from right.
     * <ul>
     *     <li>(1.a22).get(0)=1</li>
     *     <li>(1.a22).get(1)=a</li>
     *     <li>(1.a22).get(-1)=22</li>
     * </ul>
     * @param index version part index
     * @return element at given index.
     */
    String get(int index);

    /**
     * number element at given index. if the index is negative will return from right (-1 is the first starting from the right).
     * The version is first split (as a suite of number and words) then all words are discarded.
     * <ul>
     *     <li>size(1.22)=3 {'1','.','22'}</li>
     *     <li>size(1.22_u1)=5 {'1','.','22','_u','1'}</li>
     * </ul>
     * <ul>
     *     <li>(1.a22).getNumber(0)=1</li>
     *     <li>(1.a22).getNumber(1)=22</li>
     *     <li>(1.a22).getNumber(-1)=22</li>
     * </ul>
     * @param index version part index
     * @return element at given index.
     */
    BigInteger getNumber(int index);

    /**
     * return number element at position or default value. if the index is negative will return from right (-1 is the first starting from the right).
     * The version is first split (as a suite of number and words) then all words are discarded.
     * @param index position
     * @param defaultValue default value
     * @return number element at position or default value
     */

    BigInteger getNumber(int index, BigInteger defaultValue);

    /**
     * return number element at position or default value. if the index is negative will return from right (-1 is the first starting from the right).
     * The version is first split (as a suite of number and words) then all words are discarded.
     * @param index position
     * @param defaultValue default value
     * @return number element at position or default value
     * @since 0.8.3
     */
    int getInt(int index, int defaultValue);

    /**
     * return number element at position or default value. if the index is negative will return from right (-1 is the first starting from the right).
     * The version is first split (as a suite of number and words) then all words are discarded.
     * @param index position
     * @param defaultValue default value
     * @return number element at position or default value
     * @since 0.8.3
     */
    long getLong(int index, long defaultValue);
}
