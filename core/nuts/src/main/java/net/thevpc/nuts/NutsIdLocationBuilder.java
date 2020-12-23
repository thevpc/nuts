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
 * Mutable IdLocation class that helps creating instance of immutable {@link NutsIdLocation}.
 * Instances of {@link NutsIdLocation} are used in {@link NutsDescriptor} (see {@link NutsDescriptor#getLocations()})
 *
 * @category Base
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
