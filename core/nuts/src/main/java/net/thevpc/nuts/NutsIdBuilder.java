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

import java.io.Serializable;
import java.util.Map;

/**
 * Mutable Artifact id information used to create instance of {@link NutsId}
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public interface NutsIdBuilder extends NutsId, Serializable {

    /**
     * update id face which defines is a release file type selector
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setFace(String value);

    NutsIdBuilder setCondition(NutsEnvCondition c);

    NutsIdBuilder setCondition(NutsEnvConditionBuilder c);

    /**
     * update classifier
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setClassifier(String value);

    /**
     * update packaging
     *
     * @param packaging new value
     * @return {@code this} instance
     */
    NutsIdBuilder setPackaging(String packaging);

    /**
     * equivalent to {@code setFace(NutsConstants.QueryFaces.CONTENT)}
     *
     * @return this instance
     */
    NutsIdBuilder setFaceContent();

    /**
     * equivalent to {@code setFace(NutsConstants.QueryFaces.DESCRIPTOR)}
     *
     * @return {@code this} instance
     */
    NutsIdBuilder setFaceDescriptor();

    /**
     * update property.
     * When {@code value} is null, property will be removed.
     *
     * @param property name
     * @param value    new value
     * @return {@code this} instance
     */
    NutsIdBuilder setProperty(String property, String value);

    /**
     * update all properties property.
     *
     * @param queryMap new value
     * @return {@code this} instance
     */
    NutsIdBuilder setProperties(Map<String, String> queryMap);

    /**
     * update all properties property.
     *
     * @param query new value
     * @return {@code this} instance
     */
    NutsIdBuilder setPropertiesQuery(String query);

    /**
     * clear all properties
     * @return {@code this} instance
     */
    NutsIdBuilder clearProperties();

    /**
     * update repository
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setRepository(String value);

    /**
     * update groupId
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setGroupId(String value);

    /**
     * update artifactId
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setArtifactId(String value);

    /**
     * update version
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setVersion(String value);

    /**
     * update setVersion
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setVersion(NutsVersion value);

    /**
     * update all arguments
     *
     * @param id new value
     * @return {@code this} instance
     */
    NutsIdBuilder setAll(NutsId id);


    /**
     * update all arguments
     *
     * @param id new value
     * @return {@code this} instance
     */
    NutsIdBuilder setAll(NutsIdBuilder id);

    /**
     * clear this instance (set null/default all properties)
     *
     * @return {@code this instance}
     */
    NutsIdBuilder clear();

    /**
     * create new instance of {@link NutsId} initialized with this builder values.
     *
     * @return new instance of {@link NutsId} initialized with this builder values.
     */
    NutsId build();
}
