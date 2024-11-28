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

import java.util.List;
import java.util.Map;

/**
 * Dependency Builder (mutable).
 * User should use available 'set' method and finally call {@link #build()}
 * to get an instance of immutable NutsDependency
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.4
 */
public interface NDependencyBuilder extends NDependency, NComponent {

    static NDependencyBuilder of(String groupId, String artifactId) {
        return of().setGroupId(groupId).setArtifactId(artifactId);
    }

    static NDependencyBuilder of() {
        return NExtensions.of(NDependencyBuilder.class);
    }

    /**
     * set id value
     *
     * @param id new value
     * @return {@code this} instance
     */
    NDependencyBuilder setId(NId id);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NDependencyBuilder setDependency(NDependencyBuilder value);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NDependencyBuilder setAll(NDependencyBuilder value);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NDependencyBuilder setAll(NDependency value);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NDependencyBuilder setDependency(NDependency value);

    /**
     * reset this instance
     *
     * @return {@code this} instance
     */
    NDependencyBuilder clear();

    /**
     * set type value
     *
     * @param type new value
     * @return {@code this} instance
     */
    NDependencyBuilder setType(String type);

    /**
     * set optional value
     *
     * @param optional new value
     * @return {@code this} instance
     */
    NDependencyBuilder setOptional(String optional);

    /**
     * set scope value
     *
     * @param scope new value
     * @return {@code this} instance
     */
    NDependencyBuilder setScope(NDependencyScope scope);

    /**
     * set scope value
     *
     * @param scope new value
     * @return {@code this} instance
     */
    NDependencyBuilder setScope(String scope);

    /**
     * set repository value
     *
     * @param repository new value
     * @return {@code this} instance
     */
    NDependencyBuilder setRepository(String repository);

    /**
     * set group value
     *
     * @param groupId new value
     * @return {@code this} instance
     */
    NDependencyBuilder setGroupId(String groupId);

    /**
     * set name value
     *
     * @param artifactId new value
     * @return {@code this} instance
     */
    NDependencyBuilder setArtifactId(String artifactId);

    /**
     * set classifier value
     *
     * @param classifier new value
     * @return {@code this} instance
     */
    NDependencyBuilder setClassifier(String classifier);

    /**
     * set version value
     *
     * @param version new value
     * @return {@code this} instance
     */
    NDependencyBuilder setVersion(NVersion version);

    /**
     * set version value
     *
     * @param version new value
     * @return {@code this} instance
     */
    NDependencyBuilder setVersion(String version);
    /**
     * set exclusions value
     *
     * @param exclusions new value
     * @return {@code this} instance
     */
    NDependencyBuilder setExclusions(List<NId> exclusions);

    /**
     * build new instance of NutsDependencies
     *
     * @return new instance of NutsDependencies
     */
    NDependency build();

    NDependencyBuilder setProperty(String property, String value);

    NDependencyBuilder setPropertiesQuery(String propertiesQuery);

    NDependencyBuilder setProperties(Map<String, String> queryMap);

    NDependencyBuilder addPropertiesQuery(String propertiesQuery);

    NDependencyBuilder addProperties(Map<String, String> queryMap);

    /**
     * set condition
     *
     * @param condition condition
     * @return {@code this} instance
     */
    NDependencyBuilder setCondition(NEnvCondition condition);

    NDependency copy();

}
