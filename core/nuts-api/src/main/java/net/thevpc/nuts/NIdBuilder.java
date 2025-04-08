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
import net.thevpc.nuts.util.NBlankable;

import java.io.Serializable;
import java.util.Map;

/**
 * Mutable Artifact id information used to create instance of {@link NId}
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public interface NIdBuilder extends NBlankable, NComponent, Serializable {

    static NIdBuilder of(NId id) {
        return of().copyFrom(id);
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
    NIdBuilder copyFrom(NId id);


    /**
     * update all arguments
     *
     * @param id new value
     * @return {@code this} instance
     */
    NIdBuilder copyFrom(NIdBuilder id);

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


    /**
     * true if other has exact short name than {@code this}
     *
     * @param other other id
     * @return true if other has exact short name than {@code this}
     */
    boolean equalsShortId(NId other);

    /**
     * true if other has exact long name than {@code this}
     *
     * @param other other id
     * @return true if other has exact long name than {@code this}
     */
    boolean equalsLongId(NId other);

    /**
     * true if this id is a long name
     *
     * @return true if this id is a long name
     */
    boolean isLongId();

    boolean isShortId();

    /**
     * id face define is a release file type selector of the id.
     * It helps discriminating content (jar) from descriptor, from other (hash,...)
     * files released for the very same  artifact.
     *
     * @return id face selector
     */
    String getFace();

    /**
     * os supported by the artifact
     *
     * @return os supported by the artifact
     */
    NEnvConditionBuilder getCondition();

    /**
     * properties in the url query form
     *
     * @return properties in the url query form.
     */
    String getPropertiesQuery();

    /**
     * properties as map.
     *
     * @return properties as map.
     */
    Map<String, String> getProperties();

    /**
     * artifact repository (usually repository name or id)
     *
     * @return artifact repository (usually repository name or id)
     */
    String getRepository();

    /**
     * artifact group which identifies uniquely projects and group of projects.
     *
     * @return artifact group which identifies uniquely projects and group of projects.
     */
    String getGroupId();

    /**
     * return a string representation of this id. All of group, name, version,
     * repository, queryMap values are printed. This method is equivalent to
     * {@link Object#toString()}
     *
     * @return string representation of this id
     */
    String getFullName();

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
     * return a new instance of NutsId defining only group and name ignoring
     * version,repository, and queryMap values.
     *
     * @return group and name only Id instance
     */
    NId getShortId();

    /**
     * return a new instance of NutsId defining only group, name, version and classifier if available,
     * ignoring repository, and queryMap values.
     *
     * @return group, name and version only Id instance
     */
    NId getLongId();

    /**
     * return name part of this id
     *
     * @return return name part of this id
     */
    String getArtifactId();

    /**
     * tag used to distinguish between different artifacts that were built from the same source code
     *
     * @return tag used to distinguish between different artifacts that were built from the same source code
     */
    String getClassifier();

    /**
     * package packaging type
     *
     * @return packaging
     */
    String getPackaging();

    /**
     * artifact version (never null)
     *
     * @return artifact version (never null)
     */
    NVersion getVersion();

    /**
     * create a builder (mutable id) based on this id
     *
     * @return a new instance of builder (mutable id) based on this id
     */
    NIdBuilder builder();

    NDependency toDependency();

    NIdFilter filter();

    /**
     * filter accepted any id with the defined version or greater
     * @return filter accepted any id with the defined version or greater
     */
    /**
     * when the current version is a single value version X , returns ],X] version that guarantees backward compatibility
     * in all other cases returns the current version
     *
     * @return when the current version is a single value version X , returns ],X] version that guarantees backward compatibility in all other cases returns the current version
     * @since 0.8.3
     */
    NId compatNewer();

    /**
     * when the current version is a single value version X , returns [X,[ version that guarantees forward compatibility
     * in all other cases returns the current version
     *
     * @return when the current version is a single value version X , returns [X,[ version that guarantees forward compatibility in all other cases returns the current version
     * @since 0.8.3
     */
    NId compatOlder();

    boolean isNull();

    boolean isBlank();
}
