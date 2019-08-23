/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsVersion extends Serializable, NutsTokenFilter, Comparable<NutsVersion> {

    String getValue();

    int compareTo(String other);

    @Override
    int compareTo(NutsVersion other);

    boolean ge(String other);

    boolean gt(String other);

    boolean le(String other);

    boolean lt(String other);

    boolean eq(String other);

    boolean ne(String other);

    NutsVersionFilter filter();

    NutsVersionInterval[] intervals();

    boolean isSingleValue();

    NutsVersion inc();

    NutsVersion inc(int level);

    NutsVersion inc(int level, int count);

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
     *     <li>size(1.22)=3 {'1','.','22'}</li>
     *     <li>size(1.22_u1)=5 {'1','.','22','_u','1'}</li>
     * </ul>
     * @return element at given index.
     */
    String get(int index);

    /**
     * number element at given index. if the index is negative will return from right.
     * <ul>
     *     <li>size(1.22)=3 {'1','.','22'}</li>
     *     <li>size(1.22_u1)=5 {'1','.','22','_u','1'}</li>
     * </ul>
     * @return element at given index.
     */
    int getNumber(int index);

    /**
     * return number element at position or default value
     * @param index position
     * @param defaultValue default value
     * @return number element at position or default value
     */

    int getNumber(int index,int defaultValue);
}
