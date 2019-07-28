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
 * Copyright (C) 2016-2019 Taha BEN SALAH
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

import java.time.Instant;
import java.util.Date;

/**
 * Nuts Element builder that helps creating element instances.
 * @author vpc
 */
public interface NutsElementBuilder {

    /**
     * create primitive boolean element
     * @param value value
     * @return primitive boolean element
     */
    NutsPrimitiveElement forBoolean(String value);

    /**
     * create primitive boolean element
     * @param value value
     * @return primitive boolean element
     */
    NutsPrimitiveElement forBoolean(boolean value);

    /**
     * create primitive date element
     * @param value value
     * @return primitive date element
     */
    NutsPrimitiveElement forDate(Date value);

    /**
     * create primitive date element
     * @param value value
     * @return primitive date element
     */
    NutsPrimitiveElement forDate(Instant value);

    /**
     * create primitive date element
     * @param value value
     * @return primitive date element
     */
    NutsPrimitiveElement forDate(String value);

    /**
     * create primitive null element
     * @return primitive null element
     */
    NutsPrimitiveElement forNull();

    /**
     * create primitive number element
     * @param value value
     * @return primitive number element
     */
    NutsPrimitiveElement forNumber(Number value);

    /**
     * create primitive number element
     * @param value value
     * @return primitive number element
     */
    NutsPrimitiveElement forNumber(String value);

    /**
     * create primitive string element
     * @param value value
     * @return primitive string element
     */
    NutsPrimitiveElement forString(String value);

    /**
     * create object element builder (mutable)
     * @return object element
     */
    NutsObjectElementBuilder forObject();

    /**
     * create array element builder (mutable)
     * @return array element
     */
    NutsArrayElementBuilder forArray();

    
}
