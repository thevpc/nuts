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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.artifact;

import net.thevpc.nuts.internal.artifact.NVersionImpl;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.*;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.regex.Pattern;

/**
 * this class represents an <strong>immutable</strong> string representation of a version parsed as a suite of alternating numbers and words.
 * Parsing algorithm is simply to split whenever word type changes.
 * Examples:
 * <ul>
 *     <li>1 = [1]</li>
 *     <li>1.2 = [1,'.',2]</li>
 *     <li>10.20update3 = [10,'.',20,'update',3]</li>
 * </ul>
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public interface NVersion extends Serializable, Comparable<NVersion>, NBlankable {
    Pattern PATTERN = Pattern.compile("[A-Za-z0-9._*,()\\[\\] ${}+-]+");
    NVersion BLANK = new NVersionImpl("");

    /**
     * parses the version or create error
     *
     * @param version string value
     * @return parsed value
     */
    static NOptional<NVersion> get(String version) {
        if (NBlankable.isBlank(version)) {
            return NOptional.of(BLANK);
        }
        String version2 = NStringUtils.strip(version);
        if (PATTERN.matcher(version2).matches()) {
            return NOptional.of(new NVersionImpl(version2));
        }
        return NOptional.ofError(() -> NMsg.ofC("invalid version format : %s", version));
    }

    /**
     * Creates a new instance of.
     *
     * @param version version
     * @return of result
     */
    static NVersion of(String version) {
        /**
         * Returns the get.
         *
         * @param version).get( version).get(
         * @return get result
         */
        return get(version).get();
    }

    /**
     * return true the version value is a null string
     *
     * @return true the version value is a null string
     */
    boolean isNull();

    /**
     * return string representation of the version
     *
     * @return string representation of the version (never null)
     */
    String value();

    /**
     * compare this version to the other version
     *
     * @param other other version
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    int compareTo(String other);

    /**
     * compare this version to the other version
     *
     * @param other other version
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    @Override
    int compareTo(NVersion other);

    /**
     * Compare to.
     *
     * @param other other
     * @param comparator comparator
     * @return compare to result
     */
    int compareTo(NVersion other, NVersionComparator comparator);

    /**
     * Compare to.
     *
     * @param other other
     * @param comparator comparator
     * @return compare to result
     */
    int compareTo(String other, NVersionComparator comparator);

    /**
     * remove qualifier from version
     * important to check against version intervals
     *
     * @return new version without qualifier
     */
    NVersion toCanonical();

    /**
     * remove trailing zeros
     *
     * @return
     */
    NVersion toNormalized();

    /**
     * parse the current version as new instance of {@link NVersionFilter}
     *
     * @return new instance of {@link NVersionFilter}
     */
    NVersionFilter toFilter();

    /**
     * Converts to filter.
     *
     * @param comparator comparator
     * @return to filter result
     */
    NVersionFilter toFilter(NVersionComparator comparator);

    /**
     * when the current version is a single value version X , returns ],X] version that guarantees backward compatibility
     * in all other cases returns the current version
     *
     * @return when the current version is a single value version X , returns ],X] version that guarantees backward compatibility in all other cases returns the current version
     * @since 0.8.3
     */
    NVersion toAtMost();

    /**
     * when the current version is a single value version X , returns [X,[ version that guarantees forward compatibility
     * in all other cases returns the current version
     *
     * @return when the current version is a single value version X , returns [X,[ version that guarantees forward compatibility in all other cases returns the current version
     * @since 0.8.3
     */
    NVersion toAtLeast();

    /**
     * parse the current version as an interval array
     *
     * @return new interval array
     */
    NOptional<List<NVersionInterval>> toIntervals();

    /**
     * Converts to intervals.
     *
     * @param comparator comparator
     * @return to intervals result
     */
    NOptional<List<NVersionInterval>> toIntervals(NVersionComparator comparator);

    /**
     * return true if this version denotes as single value and does not match an interval.
     *
     * @return true if this version denotes as single value and does not match an interval.
     */
    boolean isSingleValue();

    /**
     * As single value.
     *
     * @return as single value result
     */
    NOptional<String> singleValue();

    /**
     * return true if this is a filter
     *
     * @return true if this is a filter
     */
    boolean isFilter();


    /**
     * increment the last number in the version with 1
     *
     * @return new version incrementing the last number
     */
    NVersion inc();

    /**
     * increment the number at {@code index}  in the version with 1
     *
     * @param index number index
     * @return new version incrementing the last number
     */
    NVersion inc(int index);

    /**
     * increment the last number in the version with the given {@code amount}
     *
     * @param index  number index
     * @param amount amount of the increment
     * @return new version incrementing the last number
     */
    NVersion inc(int index, long amount);

    /**
     * increment the last number in the version with the given {@code amount}
     *
     * @param index  number index
     * @param amount amount of the increment
     * @return new version incrementing the last number
     */
    NVersion inc(int index, BigInteger amount);

    /**
     * number of parts in the version.
     * <ul>
     *     <li>size(1.22)=3 {'1','.','22'}</li>
     *     <li>size(1.22_u1)=5 {'1','.','22','_u','1'}</li>
     * </ul>
     *
     * @return number of elements in the version.
     */
    int partCount();

    /**
     * number of numeric parts in the version.
     * <ul>
     *     <li>numberSize(1.22)=2 {1,22}</li>
     *     <li>numberSize(1.22_u1)=3 {1,22,1}</li>
     * </ul>
     *
     * @return number of numeric parts in the version.
     */
    int numberCount();

    /**
     * element at given index. if the index is negative will return from right.
     * <ul>
     *     <li>(1.a22).get(0)=1</li>
     *     <li>(1.a22).get(1)=a</li>
     *     <li>(1.a22).get(-1)=22</li>
     * </ul>
     *
     * @param index version part index
     * @return element at given index.
     */
    NOptional<NVersionPart> getPartAt(int index);


    /**
     * number element at given index. if the index is negative will return from right (-1 is the first starting from the right).
     * The version is first split (as a suite of numbers and words) then all words are discarded.
     * <ul>
     *     <li>size(1.22)=3 {'1','.','22'}</li>
     *     <li>size(1.22_u1)=5 {'1','.','22','_u','1'}</li>
     * </ul>
     * <ul>
     *     <li>(1.a22).getNumberLiteralAt(0)=1</li>
     *     <li>(1.a22).getNumberLiteralAt(1)=22</li>
     *     <li>(1.a22).getNumberLiteralAt(-1)=22</li>
     * </ul>
     *
     * @param index version part index
     * @return element at given index.
     */
    NOptional<Number> getNumberAt(int index);

    /**
     * Returns the int at.
     *
     * @param index index
     * @return get int at result
     */
    NOptional<Integer> getIntAt(int index);

    /**
     * Returns the long at.
     *
     * @param index index
     * @return get long at result
     */
    NOptional<Long> getLongAt(int index);

    /**
     * Returns the big int at.
     *
     * @param level level
     * @return get big int at result
     */
    NOptional<BigInteger> getBigIntAt(int level);

    /**
     * Checks if is latest.
     *
     * @return is latest result
     */
    boolean isLatest();

    /**
     * Checks if is release.
     *
     * @return is release result
     */
    boolean isRelease();

    /**
     * Checks if is snapshot.
     *
     * @return is snapshot result
     */
    boolean isSnapshot();

    /**
     * Parts.
     *
     * @return parts result
     */
    List<NVersionPart> parts();
}
