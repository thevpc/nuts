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
 * classifier selector builder class.
 * Nuts can select artifact classifier according to filters based on arch, os, os dist and platform.
 * This class defines the mapping to classifier to consider if all the filters.
 * When multiple selectors match, the first on prevails.
 *
 * @since 0.5.7
 * @category Descriptor
 */
public interface NutsClassifierMappingBuilder {
    /**
     * classifier to select
     * @return classifier to select
     */
    String getClassifier();

    /**
     * set classifier
     * @param value classifier
     * @return {@code this} instance
     */
    NutsClassifierMappingBuilder setClassifier(String value);

    /**
     * packaging to select
     * @return packaging to select
     */
    String getPackaging();

    /**
     * set packaging
     * @param value packaging
     * @return {@code this} instance
     */
    NutsClassifierMappingBuilder setPackaging(String value);

    /**
     * arch list filter.
     * al least one of the list must match.
     * @return arch list filter
     */
    String[] getArch();

    /**
     * set archs
     * @param value archs
     * @return {@code this} instance
     */
    NutsClassifierMappingBuilder setArch(String... value);

    /**
     * os list filter.
     * al least one of the list must match.
     * @return os list filter
     */
    String[] getOs();

    /**
     * set oses
     * @param value oses
     * @return {@code this} instance
     */
    NutsClassifierMappingBuilder setOs(String... value);

    /**
     * os distribution list filter.
     * al least one of the list must match.
     * @return os distribution list filter
     */
    String[] getOsdist();

    /**
     * set os dists
     * @param value os dists
     * @return {@code this} instance
     */
    NutsClassifierMappingBuilder setOsdist(String... value);

    /**
     * platform list filter.
     * al least one of the list must match.
     * @return platform list filter.
     */
    String[] getPlatform();

    /**
     * set platforms
     * @param value platforms
     * @return {@code this} instance
     */
    NutsClassifierMappingBuilder setPlatform(String... value);

    /**
     * copy all values from the given builder
     * @param value builder to copy from
     * @return {@code this} instance
     */
    NutsClassifierMappingBuilder set(NutsClassifierMappingBuilder value);

    /**
     * copy all values from the given instance
     * @param value instance to copy from
     * @return {@code this} instance
     */
    NutsClassifierMappingBuilder set(NutsClassifierMapping value);

    /**
     * clear all values / reset builder
     * @return {@code this} instance
     */
    NutsClassifierMappingBuilder clear();

    /**
     * create new instance of {@link NutsClassifierMapping} initialized with this builder's values.
     * @return new instance of {@link NutsClassifierMapping} initialized with this builder's values.
     */
    NutsClassifierMapping build();
}
