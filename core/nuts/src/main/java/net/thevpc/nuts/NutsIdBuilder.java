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

import net.thevpc.nuts.boot.NutsApiUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

/**
 * Mutable Artifact id information used to create instance of {@link NutsId}
 * @author thevpc
 * @since 0.5.4
 * @app.category Descriptor
 */
public interface NutsIdBuilder extends Serializable {
    static NutsIdBuilder of(NutsSession session){
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().id().builder();
    }


    
    /**
     * update groupId
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setGroupId(String value);


    /**
     * update repository
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setRepository(String value);


    /**
     * update version
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setVersion(String value);


    /**
     * update setVersion
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setVersion(NutsVersion value);


    /**
     * update artifactId
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setArtifactId(String value);

    /**
     * id face define is a release file type selector of the id.
     * It helps discriminating content (jar) from descriptor, from other (hash,...)
     * files released for the very same  artifact.
     * @return id face selector
     */
    String getFace();

    /**
     * os and env supported by the artifact
     * @return os supported by the artifact
     */
    NutsEnvConditionBuilder getCondition();

    NutsIdBuilder setCondition(NutsEnvCondition c);

    NutsIdBuilder setCondition(NutsEnvConditionBuilder c);

    /**
     * tag used to distinguish between different artifacts that were built from the same source code
     * @return tag used to distinguish between different artifacts that were built from the same source code
     */
    String getClassifier();

    /**
     * packaging
     * @return packaging
     */
    String getPackaging();

    /**
     * equivalent to {@code setFace(NutsConstants.QueryFaces.CONTENT)}
     * @return this instance
     */
    NutsIdBuilder setFaceContent();

    /**
     * equivalent to {@code setFace(NutsConstants.QueryFaces.DESCRIPTOR)}
     * @return {@code this} instance
     */
    NutsIdBuilder setFaceDescriptor();

    /**
     * update id face which defines is a release file type selector
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setFace(String value);

    /**
     * update classifier
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setClassifier(String value);


    /**
     * update property.
     * When {@code value} is null, property will be removed.
     * @param property name
     * @param value new value
     * @return {@code this} instance
     */
    NutsIdBuilder setProperty(String property, String value);

    /**
     * update all properties property.
     * @param queryMap new value
     * @return {@code this} instance
     */
    NutsIdBuilder setProperties(Map<String, String> queryMap);

    /**
     * update all properties property while retaining old,
     * non overridden properties.
     * @param queryMap new value
     * @return {@code this} instance
     */
    NutsIdBuilder addProperties(Map<String, String> queryMap);

    
    /**
     * update all properties property.
     * @param query new value
     * @return {@code this} instance
     */
    NutsIdBuilder setProperties(String query);

    /**
     * update all properties property while retaining old,
     * non overridden properties.
     * @param query new value
     * @return {@code this} instance
     */
    NutsIdBuilder addProperties(String query);


    /**
     * update packaging
     * @param packaging new value
     * @return {@code this} instance
     */
    NutsIdBuilder setPackaging(String packaging);

    /**
     * properties in the url query form
     * @return properties in the url query form.
     */
    String getPropertiesQuery();

    /**
     * properties as map.
     * @return properties as map.
     */
    Map<String, String> getProperties();

    /**
     * artifact repository (usually repository name or id)
     * @return artifact repository (usually repository name or id)
     */
    String getRepository();

    /**
     * artifact group which identifies uniquely projects and group of projects.
     * @return artifact group which identifies uniquely projects and group of projects.
     */
    String getGroupId();

    /**
     * return a string concatenation of group, name and version,
     * ignoring repository, and queryMap values. An example of long name is
     * <code>my-group:my-artifact#my-version?alt</code>
     *
     * @return group id, artifact id and version only Id instance
     */
    String getLongName();

    /**
     * returns a string concatenation of group and name (':' separated) ignoring
     * version,repository, and queryMap values. In group is empty or null, name
     * is returned. Ann null values are trimmed to "" An example of simple name
     * is <code>my-group:my-artifact</code>
     *
     * @return group id and artifact id
     */
    String getShortName();

    /**
     * return a string representation of this id. All of group, name, version,
     * repository, queryMap values are printed. This method is equivalent to
     * {@link Object#toString()}
     *
     * @return string representation of this id
     */
    String getFullName();

    /**
     * return name part of this id
     *
     * @return return name part of this id
     */
    String getArtifactId();

    /**
     * artifact version (never null)
     * @return artifact version (never null)
     */
    NutsVersion getVersion();


    /**
     * update all arguments
     * @param id new value
     * @return {@code this} instance
     */
    NutsIdBuilder setAll(NutsId id);


    /**
     * update all arguments
     * @param id new value
     * @return {@code this} instance
     */
    NutsIdBuilder setAll(NutsIdBuilder id);

    /**
     * replace dollar based variables with the given properties
     * @param properties to replace
     * @return {@code this} instance
     */
    NutsIdBuilder apply(Function<String, String> properties);

    /**
     * clear this instance (set null/default all properties)
     *
     * @return {@code this instance}
     */
    NutsIdBuilder clear();

    /**
     * create new instance of {@link NutsId} initialized with this builder values.
     * @return new instance of {@link NutsId} initialized with this builder values.
     */
    NutsId build();

    NutsIdBuilder omitImportedGroupId();
}
