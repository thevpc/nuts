/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.List;
import java.util.stream.Stream;

/**
 * Find Result items from find command
 *
 * @see NutsSearchCommand#getResultIds()
 * @author vpc
 * @param <T> Result Type
 * @since 0.5.4
 */
public interface NutsResultList<T> extends Iterable<T> {

    /**
     * return result as a  java.util.List .
     *
     * consumes the result and returns a list Calling this method twice will
     * result in unexpected behavior (may return an empty list as the result is
     * already consumed or throw an Exception)
     *
     * @return result as a  java.util.List
     */
    List<T> list();

    /**
     * return the first value or null if none found.
     *
     * Calling this method twice will result in unexpected behavior (may return
     * an incorrect value such as null as the result is already consumed or
     * throw an Exception)
     *
     * @return the first value or null if none found
     */
    T first();

    /**
     * return the first value or NutsNotFoundException if not found.
     *
     * Calling this method twice will result in unexpected behavior (may return
     * an incorrect value such as null as the result is already consumed or
     * throw an Exception)
     *
     * @return the first value or NutsNotFoundException if not found
     */
    T required() throws NutsNotFoundException;

    /**
     * return the first value while checking that there are no more elements.
     *
     * Calling this method twice will result in unexpected behavior (may return
     * an incorrect value such as null as the result is already consumed or
     * throw an Exception)
     *
     * @return the first value while checking that there are no more elements to
     * consume. An IllegalArgumentException is thrown if there are no elements
     * to consume. An IllegalArgumentException is also thrown if the are more
     * than one element consumed
     */
    T singleton() throws NutsTooManyElementsException, NutsNotFoundException;

    /**
     * return result as a  java.util.stream.Stream .
     *
     * Calling this method twice will result in unexpected behavior (may return
     * 0 as the result is already consumed or throw an Exception)
     *
     * @return result as a  java.util.stream.Stream
     */
    Stream<T> stream();

    /**
     * return elements count of this result.
     *
     * consumes the result and returns the number of elements consumed. Calling
     * this method twice will result in unexpected behavior (may return 0 as the
     * result is already consumed or throw an Exception)
     *
     * @return elements count of this result.
     */
    long count();

}
