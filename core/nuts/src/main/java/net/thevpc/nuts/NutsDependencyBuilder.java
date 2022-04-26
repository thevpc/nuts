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
public interface NutsDependencyBuilder extends NutsDependency {

    static NutsDependencyBuilder of(String groupId, String artifactId) {
        return new DefaultNutsDependencyBuilder(groupId, artifactId);
    }

    static NutsDependencyBuilder of() {
        return new DefaultNutsDependencyBuilder();
    }

    /**
     * set id value
     *
     * @param id new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setId(NutsId id);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setDependency(NutsDependencyBuilder value);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setAll(NutsDependencyBuilder value);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setAll(NutsDependency value);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setDependency(NutsDependency value);

    /**
     * reset this instance
     *
     * @return {@code this} instance
     */
    NutsDependencyBuilder clear();

    /**
     * set type value
     *
     * @param type new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setType(String type);

    /**
     * set optional value
     *
     * @param optional new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setOptional(String optional);

    /**
     * set scope value
     *
     * @param scope new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setScope(NutsDependencyScope scope);

    /**
     * set scope value
     *
     * @param scope new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setScope(String scope);

    /**
     * set repository value
     *
     * @param repository new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setRepository(String repository);

    /**
     * set group value
     *
     * @param groupId new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setGroupId(String groupId);

    /**
     * set name value
     *
     * @param artifactId new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setArtifactId(String artifactId);

    /**
     * set classifier value
     *
     * @param classifier new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setClassifier(String classifier);

    /**
     * set version value
     *
     * @param version new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setVersion(NutsVersion version);

    /**
     * set version value
     *
     * @param version new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setVersion(String version);
    /**
     * set exclusions value
     *
     * @param exclusions new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setExclusions(List<NutsId> exclusions);

    /**
     * build new instance of NutsDependencies
     *
     * @return new instance of NutsDependencies
     */
    NutsDependency build();

    NutsDependencyBuilder setProperty(String property, String value);

    NutsDependencyBuilder setPropertiesQuery(String propertiesQuery);

    NutsDependencyBuilder setProperties(Map<String, String> queryMap);

    NutsDependencyBuilder addPropertiesQuery(String propertiesQuery);

    NutsDependencyBuilder addProperties(Map<String, String> queryMap);

    /**
     * set condition
     *
     * @param condition condition
     * @return {@code this} instance
     */
    NutsDependencyBuilder setCondition(NutsEnvCondition condition);

    NutsDependency copy();

}
