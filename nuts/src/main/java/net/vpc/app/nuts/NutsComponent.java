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

/**
 * Top Level extension Point in Nuts. 
 * Extension mechanism in nuts is based on a factory thats select the best 
 * implementation for a given predefined interface (named Extension Point).
 * Such interfaces must extend this {@code NutsComponent} interface.
 * Implementations must implement these extension points by providing their 
 * best support level (when method {@link #getSupportLevel(net.vpc.app.nuts.NutsSupportLevelContext)} is invoked).
 * Only implementations with positive support level are considered.
 * Implementations with higher support level are selected first.
 * 
 *
 * @param <CriteriaType> support criteria type
 * @since 0.5.4
 * @category SPI Base
 */
public interface NutsComponent<CriteriaType> {

    /**
     * minimum support level for user defined implementations.
     */
    int CUSTOM_SUPPORT = 1000;
    /**
     * this is the default support level for runtime implementation (nuts-core).
     */
    int DEFAULT_SUPPORT = 10;
    /**
     * when getSupportLevel(...)==NO_SUPPORT the component is discarded.
     */
    int NO_SUPPORT = -1;

    /**
     * evaluate support level (who much this instance should be considered convenient, acceptable)
     * for the given arguments (provided in context).
     * @param context evaluation context
     * @return support level value
     */
    int getSupportLevel(NutsSupportLevelContext<CriteriaType> context);
}
