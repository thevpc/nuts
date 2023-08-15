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

import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Immutable Artifact id information.
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.1.0
 */
public interface NId extends Comparable<NId>, NFormattable, NBlankable {
    NId API_ID = of(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_ARTIFACT_ID, "").get();
    NId RUNTIME_ID = of(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_RUNTIME_ARTIFACT_ID, "").get();
    Pattern PATTERN = Pattern.compile("^(?<group>[a-zA-Z0-9_.${}*-]+)(:(?<artifact>[a-zA-Z0-9_.${}*-]+))?(#(?<version>[^?]+))?(\\?(?<query>.+))?$");
    NId BLANK = new DefaultNId();

    static NOptional<List<NId>> ofList(String value) {
        return NReservedUtils.parseIdList(value);
    }

    static NOptional<Set<NId>> ofSet(String value) {
        return ofList(value).map(LinkedHashSet::new);
    }

    static NOptional<NId> of(String groupId, String artifactId) {
        return NOptional.of(new DefaultNId(groupId, artifactId, null));
    }

    static NOptional<NId> of(String groupId, String artifactId, NVersion version) {
        return NOptional.of(new DefaultNId(groupId, artifactId, version));
    }

    static NOptional<NId> of(String groupId, String artifactId, String version) {
        return NVersion.of(version).map(x -> new DefaultNId(groupId, artifactId, x));
    }

    static NOptional<NId> ofApi(NVersion version) {
        if (NBlankable.isBlank(version)) {
            return NOptional.of(API_ID);
        }
        return of(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_ARTIFACT_ID, version);
    }

    static NOptional<NId> ofRuntime(NVersion version) {
        if (NBlankable.isBlank(version)) {
            return NOptional.of(RUNTIME_ID);
        }
        return of(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_RUNTIME, version);
    }

    static NOptional<NId> ofApi(String version) {
        if (NBlankable.isBlank(version)) {
            return NOptional.of(API_ID);
        }
        return of(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_ARTIFACT_ID, version);
    }

    static NOptional<NId> ofRuntime(String version) {
        if (NBlankable.isBlank(version)) {
            return NOptional.of(RUNTIME_ID);
        }
        return of(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_RUNTIME_ARTIFACT_ID, version);
    }

    static NOptional<NId> of(String value) {
        return NReservedUtils.parseId(value);
    }

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
    NEnvCondition getCondition();

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

    NIdFilter filter(NSession session);

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
