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

/**
 * classifier selector builder class.
 * Nuts can select artifact classifier according to filters based on arch, os, os dist and platform.
 * This class defines the mapping to classifier to consider if all the filters.
 * When multiple selectors match, the first on prevails.
 *
 * @since 0.5.7
 * @app.category Descriptor
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
