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
package net.thevpc.nuts;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.io.Serializable;
import java.util.Map;

/**
 * Mutable Artifact id information used to create instance of {@link NId}
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public interface NIdBuilder extends NId, NComponent, Serializable {

    static NIdBuilder of(NId id) {
        return of().setAll(id);
    }
    static NIdBuilder of(String groupId, String artifactId) {
        return of().setGroupId(groupId).setArtifactId(artifactId);
    }

    static NIdBuilder of() {
        return NExtensions.of(NIdBuilder.class);
    }

    /**
     * update id face which defines is a release file type selector
     *
     * @param value new value
     * @return {@code this} instance
     */
    NIdBuilder setFace(String value);

    NIdBuilder setCondition(NEnvCondition c);

    NIdBuilder setCondition(NEnvConditionBuilder c);

    /**
     * update classifier
     *
     * @param value new value
     * @return {@code this} instance
     */
    NIdBuilder setClassifier(String value);

    /**
     * update packaging
     *
     * @param packaging new value
     * @return {@code this} instance
     */
    NIdBuilder setPackaging(String packaging);

    /**
     * equivalent to {@code setFace(NutsConstants.QueryFaces.CONTENT)}
     *
     * @return this instance
     */
    NIdBuilder setFaceContent();

    /**
     * equivalent to {@code setFace(NutsConstants.QueryFaces.DESCRIPTOR)}
     *
     * @return {@code this} instance
     */
    NIdBuilder setFaceDescriptor();

    /**
     * update property.
     * When {@code value} is null, property will be removed.
     *
     * @param property name
     * @param value    new value
     * @return {@code this} instance
     */
    NIdBuilder setProperty(String property, String value);

    /**
     * update all properties property.
     *
     * @param queryMap new value
     * @return {@code this} instance
     */
    NIdBuilder setProperties(Map<String, String> queryMap);

    /**
     * update all properties property.
     *
     * @param query new value
     * @return {@code this} instance
     */
    NIdBuilder setPropertiesQuery(String query);

    /**
     * clear all properties
     *
     * @return {@code this} instance
     */
    NIdBuilder clearProperties();

    /**
     * update repository
     *
     * @param value new value
     * @return {@code this} instance
     */
    NIdBuilder setRepository(String value);

    /**
     * update groupId
     *
     * @param value new value
     * @return {@code this} instance
     */
    NIdBuilder setGroupId(String value);

    /**
     * update artifactId
     *
     * @param value new value
     * @return {@code this} instance
     */
    NIdBuilder setArtifactId(String value);

    /**
     * update version
     *
     * @param value new value
     * @return {@code this} instance
     */
    NIdBuilder setVersion(String value);

    /**
     * update setVersion
     *
     * @param value new value
     * @return {@code this} instance
     */
    NIdBuilder setVersion(NVersion value);

    /**
     * update all arguments
     *
     * @param id new value
     * @return {@code this} instance
     */
    NIdBuilder setAll(NId id);


    /**
     * update all arguments
     *
     * @param id new value
     * @return {@code this} instance
     */
    NIdBuilder setAll(NIdBuilder id);

    /**
     * clear this instance (set null/default all properties)
     *
     * @return {@code this instance}
     */
    NIdBuilder clear();

    /**
     * create new instance of {@link NId} initialized with this builder values.
     *
     * @return new instance of {@link NId} initialized with this builder values.
     */
    NId build();
}
