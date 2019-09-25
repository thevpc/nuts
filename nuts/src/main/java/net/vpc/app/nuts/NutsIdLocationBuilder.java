/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * Mutable IdLocation class that helps creating instance of immutable {@link NutsIdLocation}.
 * Instances of {@link NutsIdLocation} are used in {@link NutsDescriptor} (see {@link NutsDescriptor#getLocations()})
 *
 */
public interface NutsIdLocationBuilder {

    /**
     * return location url
     * @return location url
     */
    String getUrl();

    /**
     * update location url
     * @param value location url
     * @return {@code this} instance
     */
    NutsIdLocationBuilder url(String value);

    /**
     * update location url
     * @param value location url
     * @return {@code this} instance
     */
    NutsIdLocationBuilder setUrl(String value);

    /**
     * return location classifier
     * @return location classifier
     */
    String getClassifier();

    /**
     * update location classifier
     * @param value location classifier
     * @return {@code this} instance
     */
    NutsIdLocationBuilder classifier(String value);

    /**
     * update location classifier
     * @param value location classifier
     * @return {@code this} instance
     */
    NutsIdLocationBuilder setClassifier(String value);

    /**
     * return location region
     * @return location region
     */
    String getRegion();

    /**
     * update location region
     * @param value location region
     * @return {@code this} instance
     */
    NutsIdLocationBuilder region(String value);

    /**
     * update location region
     * @param value location region
     * @return {@code this} instance
     */
    NutsIdLocationBuilder setRegion(String value);

    /**
     * update all attributes, copy from {@code value} instance
     * @param value instance to copy from
     * @return {@code this} instance
     */
    NutsIdLocationBuilder set(NutsIdLocationBuilder value);

    /**
     * update all attributes, copy from {@code value} instance
     * @param value instance to copy from
     * @return {@code this} instance
     */
    NutsIdLocationBuilder set(NutsIdLocation value);

    /**
     * clear this instance (set null/default all properties)
     *
     * @return {@code this instance}
     */
    NutsIdLocationBuilder clear();


    /**
     * create new instance of {@link NutsIdLocation} initialized with this builder values.
     * @return new instance of {@link NutsIdLocation} initialized with this builder values.
     */
    NutsIdLocation build();
}
