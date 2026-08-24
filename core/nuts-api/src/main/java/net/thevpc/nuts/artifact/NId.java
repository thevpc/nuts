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
package net.thevpc.nuts.artifact;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.internal.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NGetter;
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
public interface NId extends Comparable<NId>, NBlankable {
    NId API_ID = get(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_API_ARTIFACT_ID, "").get();
    NId RUNTIME_ID = get(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_RUNTIME_ARTIFACT_ID, "").get();
    Pattern PATTERN = Pattern.compile("^((?<group>[a-zA-Z0-9_.${}*-]*):)?(?<artifact>[a-zA-Z0-9_.${}*-]*)(:(?<classifier>[a-zA-Z0-9_.${}*-]*))?(#(?<version>[^?]+))?(\\?(?<query>.+))?$");
    Pattern GROUP_ID_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z][A-Za-z0-9_]*)*$");
    Pattern ARTIFACT_ID_PATTERN = Pattern.compile("^[A-Za-z0-9]+(?:[._-][A-Za-z0-9]+)*$");
    NId BLANK = new DefaultNId();

    /**
     * Returns the list.
     *
     * @param value value
     * @return get list result
     */
    static NOptional<List<NId>> getList(String value) {
        return NReservedUtils.parseIdList(value);
    }

    /**
     * Returns the set.
     *
     * @param value value
     * @return get set result
     */
    static NOptional<Set<NId>> getSet(String value) {
        /**
         * Returns the list.
         *
         * @param value).map(LinkedHashSet::new value).map( linked hash set::new
         * @return get list result
         */
        return getList(value).map(LinkedHashSet::new);
    }

    /**
     * Returns the get.
     *
     * @param groupId group id
     * @param artifactId artifact id
     * @return get result
     */
    static NOptional<NId> get(String groupId, String artifactId) {
        return NOptional.of(new DefaultNId(groupId, artifactId, null));
    }

    /**
     * Returns the get.
     *
     * @param groupId group id
     * @param artifactId artifact id
     * @param version version
     * @return get result
     */
    static NOptional<NId> get(String groupId, String artifactId, NVersion version) {
        return NOptional.of(new DefaultNId(groupId, artifactId, version));
    }

    /**
     * Returns the get.
     *
     * @param groupId group id
     * @param artifactId artifact id
     * @param version version
     * @return get result
     */
    static NOptional<NId> get(String groupId, String artifactId, String version) {
        return NVersion.get(version).map(x -> new DefaultNId(groupId, artifactId, x));
    }

    /**
     * Returns the api.
     *
     * @param version version
     * @return get api result
     */
    static NOptional<NId> getApi(NVersion version) {
        if (NBlankable.isBlank(version)) {
            return NOptional.of(API_ID);
        }
        /**
         * Returns the get.
         *
         * @param NConstants.Ids.NUTS_GROUP_ID n constants. ids.nuts_group_id
         * @param NConstants.Ids.NUTS_API_ARTIFACT_ID n constants. ids.nuts_api_artifact_id
         * @param version version
         * @return get result
         */
        return get(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_API_ARTIFACT_ID, version);
    }

    /**
     * Returns the runtime.
     *
     * @param version version
     * @return get runtime result
     */
    static NOptional<NId> getRuntime(NVersion version) {
        if (NBlankable.isBlank(version)) {
            return NOptional.of(RUNTIME_ID);
        }
        /**
         * Returns the get.
         *
         * @param NConstants.Ids.NUTS_GROUP_ID n constants. ids.nuts_group_id
         * @param NConstants.Ids.NUTS_RUNTIME n constants. ids.nuts_runtime
         * @param version version
         * @return get result
         */
        return get(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_RUNTIME, version);
    }

    /**
     * Returns the api.
     *
     * @param version version
     * @return get api result
     */
    static NOptional<NId> getApi(String version) {
        if (NBlankable.isBlank(version)) {
            return NOptional.of(API_ID);
        }
        /**
         * Returns the get.
         *
         * @param NConstants.Ids.NUTS_GROUP_ID n constants. ids.nuts_group_id
         * @param NConstants.Ids.NUTS_API_ARTIFACT_ID n constants. ids.nuts_api_artifact_id
         * @param version version
         * @return get result
         */
        return get(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_API_ARTIFACT_ID, version);
    }

    /**
     * Returns the runtime.
     *
     * @param version version
     * @return get runtime result
     */
    static NOptional<NId> getRuntime(String version) {
        if (NBlankable.isBlank(version)) {
            return NOptional.of(RUNTIME_ID);
        }
        /**
         * Returns the get.
         *
         * @param NConstants.Ids.NUTS_GROUP_ID n constants. ids.nuts_group_id
         * @param NConstants.Ids.NUTS_RUNTIME_ARTIFACT_ID n constants. ids.nuts_runtime_artifact_id
         * @param version version
         * @return get result
         */
        return get(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_RUNTIME_ARTIFACT_ID, version);
    }

    /**
     * Returns the get.
     *
     * @param value value
     * @return get result
     */
    static NOptional<NId> get(String value) {
        return NReservedUtils.parseId(value);
    }

    /**
     * Returns the for class.
     *
     * @param value value
     * @return get for class result
     */
    static NOptional<NId> getForClass(Class<?> value) {
        return NIORPI.of().resolveId(value);
    }

    /**
     * Returns the for path.
     *
     * @param value value
     * @return get for path result
     */
    static NOptional<NId> getForPath(NPath value) {
        return NIORPI.of().resolveId(value);
    }

    /**
     * Creates a new instance of of list.
     *
     * @param value value
     * @return of list result
     */
    static List<NId> ofList(String value) {
        /**
         * Returns the list.
         *
         * @param value).get( value).get(
         * @return get list result
         */
        return getList(value).get();
    }

    /**
     * Creates a new instance of of set.
     *
     * @param value value
     * @return of set result
     */
    static Set<NId> ofSet(String value) {
        /**
         * Returns the set.
         *
         * @param value).get( value).get(
         * @return get set result
         */
        return getSet(value).get();
    }

    /**
     * Creates a new instance of of.
     *
     * @param groupId group id
     * @param artifactId artifact id
     * @return of result
     */
    static NId of(String groupId, String artifactId) {
        /**
         * Returns the get.
         *
         * @param groupId group id
         * @param artifactId).get( artifact id).get(
         * @return get result
         */
        return get(groupId, artifactId).get();
    }

    /**
     * Creates a new instance of of.
     *
     * @param groupId group id
     * @param artifactId artifact id
     * @param version version
     * @return of result
     */
    static NId of(String groupId, String artifactId, NVersion version) {
        /**
         * Returns the get.
         *
         * @param groupId group id
         * @param artifactId artifact id
         * @param version).get( version).get(
         * @return get result
         */
        return get(groupId, artifactId, version).get();
    }

    /**
     * Creates a new instance of of.
     *
     * @param groupId group id
     * @param artifactId artifact id
     * @param version version
     * @return of result
     */
    static NId of(String groupId, String artifactId, String version) {
        /**
         * Returns the get.
         *
         * @param groupId group id
         * @param artifactId artifact id
         * @param version).get( version).get(
         * @return get result
         */
        return get(groupId, artifactId, version).get();
    }

    /**
     * Creates a new instance of of api.
     *
     * @param version version
     * @return of api result
     */
    static NId ofApi(NVersion version) {
        /**
         * Returns the api.
         *
         * @param version).get( version).get(
         * @return get api result
         */
        return getApi(version).get();
    }

    /**
     * Creates a new instance of of runtime.
     *
     * @param version version
     * @return of runtime result
     */
    static NId ofRuntime(NVersion version) {
        /**
         * Returns the runtime.
         *
         * @param version).get( version).get(
         * @return get runtime result
         */
        return getRuntime(version).get();
    }

    /**
     * Creates a new instance of of api.
     *
     * @param version version
     * @return of api result
     */
    static NId ofApi(String version) {
        /**
         * Returns the api.
         *
         * @param version).get( version).get(
         * @return get api result
         */
        return getApi(version).get();
    }

    /**
     * Creates a new instance of of runtime.
     *
     * @param version version
     * @return of runtime result
     */
    static NId ofRuntime(String version) {
        /**
         * Returns the runtime.
         *
         * @param version).get( version).get(
         * @return get runtime result
         */
        return getRuntime(version).get();
    }

    /**
     * Creates a new instance of of.
     *
     * @param value value
     * @return of result
     */
    static NId of(String value) {
        /**
         * Returns the get.
         *
         * @param value).get( value).get(
         * @return get result
         */
        return get(value).get();
    }

    /**
     * Creates a new instance of of class.
     *
     * @param value value
     * @return of class result
     */
    static NId ofClass(Class<?> value) {
        /**
         * Returns the for class.
         *
         * @param value).get( value).get(
         * @return get for class result
         */
        return getForClass(value).get();
    }

    /**
     * Creates a new instance of of path.
     *
     * @param value value
     * @return of path result
     */
    static NId ofPath(NPath value) {
        /**
         * Returns the for path.
         *
         * @param value).get( value).get(
         * @return get for path result
         */
        return getForPath(value).get();
    }


    /**
     * Finds the find by class.
     *
     * @param value value
     * @return find by class result
     */
    static List<NId> findByClass(Class<?> value) {
        return NIORPI.of().resolveIds(value);
    }

    /**
     * Finds the find by path.
     *
     * @param value value
     * @return find by path result
     */
    static List<NId> findByPath(NPath value) {
        return NIORPI.of().resolveIds(value);
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
    @NGetter
    boolean isLongId();

    /**
     * Checks if is short id.
     *
     * @return is short id result
     */
    @NGetter
    boolean isShortId();

    /**
     * id face define is a release file type selector of the id.
     * It helps discriminating content (jar) from descriptor, from other (hash,...)
     * files released for the very same  artifact.
     *
     * @return id face selector
     */
    @NGetter
    String face();

    /**
     * os supported by the artifact
     *
     * @return os supported by the artifact
     */
    @NGetter
    NEnvCondition condition();

    /**
     * properties in the url query form
     *
     * @return properties in the url query form.
     */
    @NGetter
    String propertiesQuery();

    /**
     * properties as map.
     *
     * @return properties as map.
     */
    @NGetter
    Map<String, String> properties();

    /**
     * artifact repository (usually repository name or id)
     *
     * @return artifact repository (usually repository name or id)
     */
    @NGetter
    String repository();

    /**
     * artifact group which identifies uniquely projects and group of projects.
     *
     * @return artifact group which identifies uniquely projects and group of projects.
     */
    @NGetter
    String groupId();

    /**
     * return a string representation of this id. All of group, name, version,
     * repository, queryMap values are printed. This method is equivalent to
     * {@link Object#toString()}
     *
     * @return string representation of this id
     */
    @NGetter
    String fullName();

    /**
     * return a string concatenation of group, name and version,
     * ignoring repository, and queryMap values. An example of long name is
     * <code>my-group:my-artifact:classifier#my-version</code>
     *
     * @return group id, artifact id and version only id instance
     */
    @NGetter
    String longName();

    /**
     * returns a string concatenation of group and name (':' separated) ignoring
     * version,repository, and queryMap values. In group is empty or null, name
     * is returned. Ann null values are stripped to "" An example of simple name
     * is <code>my-group:my-artifact</code>
     *
     * @return group id and artifact id
     */
    @NGetter
    String shortName();

    /**
     * return a new instance of NutsId defining only group and name ignoring
     * version,repository, and queryMap values.
     *
     * @return group and name only Id instance
     */
    @NGetter
    NId shortId();

    /**
     * Shared id.
     *
     * @return shared id result
     */
    @NGetter
    NId sharedId();

    /**
     * return a new instance of NutsId defining only group, name, version and classifier if available,
     * ignoring repository, and queryMap values.
     *
     * @return group, name and version only Id instance
     */
    @NGetter
    NId longId();

    /**
     * return name part of this id
     *
     * @return return name part of this id
     */
    @NGetter
    String artifactId();

    /**
     * tag used to distinguish between different artifacts that were built from the same source code
     *
     * @return tag used to distinguish between different artifacts that were built from the same source code
     */
    @NGetter
    String classifier();

    /**
     * package packaging type
     *
     * @return packaging
     */
    @NGetter
    String packaging();

    /**
     * artifact version (never null)
     *
     * @return artifact version (never null)
     */
    @NGetter
    NVersion version();

    /**
     * create a builder (mutable id) based on this id
     *
     * @return a new instance of builder (mutable id) based on this id
     */
    NIdBuilder builder();

    /**
     * Converts to dependency.
     *
     * @return to dependency result
     */
    NDependency toDependency();

    /**
     * filter accepted any id with the defined version or greater
     * @return filter accepted any id with the defined version or greater
     */
    NIdFilter toFilter();


    /**
     * when the current version is a single value version X , returns ],X] version that guarantees backward compatibility
     * in all other cases returns the current version
     *
     * @return when the current version is a single value version X , returns ],X] version that guarantees backward compatibility in all other cases returns the current version
     * @since 0.8.3
     */
    NId toAtLeast();

    /**
     * when the current version is a single value version X , returns [X,[ version that guarantees forward compatibility
     * in all other cases returns the current version
     *
     * @return when the current version is a single value version X , returns [X,[ version that guarantees forward compatibility in all other cases returns the current version
     * @since 0.8.3
     */
    NId toAtMost();

    /**
     * Checks if is null.
     *
     * @return is null result
     */
    boolean isNull();

    /**
     * Checks if is blank.
     *
     * @return is blank result
     */
    boolean isBlank();

    /**
     * maven path as [groupId]/[artifactId]/[version]
     *
     * @return maven path
     */
    @NGetter
    String mavenFolder();

    /**
     * Returns the maven file name.
     *
     * @param extension extension
     * @return get maven file name result
     */
    String getMavenFileName(String extension);

    /**
     * Returns the maven path.
     *
     * @param extension extension
     * @return get maven path result
     */
    String getMavenPath(String extension);
}
