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

import net.thevpc.nuts.boot.NutsApiUtils;

import java.util.Map;

/**
 * Dependency Builder (mutable).
 * User should use available 'set' method and finally call {@link #build()}
 * to get an instance of immutable NutsDependency
 *
 * @author thevpc
 * @since 0.5.4
 * @app.category Descriptor
 */
public interface NutsDependencyBuilder {

    public static NutsDependencyBuilder of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().dependency().builder();
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
    NutsDependencyBuilder set(NutsDependencyBuilder value);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder set(NutsDependency value);

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
     * true if this dependency is optional.
     * equivalent to {@code Boolean.parseBoolean(getOptional())}
     *
     * @return true if this dependency is optional.
     */
    boolean isOptional();

    /**
     * return dependency type
     *
     * @return dependency type
     */
    String getType();

    /**
     * set type value
     *
     * @param type new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setType(String type);

    /**
     * return optional
     *
     * @return optional
     */
    String getOptional();

    /**
     * set optional value
     *
     * @param optional new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setOptional(String optional);

    /**
     * return scope
     *
     * @return scope
     */
    String getScope();

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
     * return id
     *
     * @return id
     */
    NutsId toId();

    /**
     * return repository
     *
     * @return repository
     */
    String getRepository();

    /**
     * set repository value
     *
     * @param repository new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setRepository(String repository);

    /**
     * return group
     *
     * @return group
     */
    String getGroupId();

    /**
     * set group value
     *
     * @param groupId new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setGroupId(String groupId);

    /**
     * return name
     *
     * @return name
     */
    String getArtifactId();

    /**
     * set name value
     *
     * @param artifactId new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setArtifactId(String artifactId);

    /**
     * return classifier
     *
     * @return classifier
     */
    String getClassifier();

    /**
     * set classifier value
     *
     * @param classifier new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setClassifier(String classifier);

    /**
     * set condition
     *
     * @param condition condition
     * @return {@code this} instance
     */
    NutsDependencyBuilder setCondition(NutsEnvCondition condition);

    NutsDependencyBuilder setCondition(NutsEnvConditionBuilder condition);

    /**
     * return full name
     *
     * @return full name
     */
    String getFullName();

    /**
     * return version
     *
     * @return version
     */
    NutsVersion getVersion();

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
     * return exclusions
     *
     * @return exclusions
     */
    NutsId[] getExclusions();

    /**
     * set exclusions value
     *
     * @param exclusions new value
     * @return {@code this} instance
     */
    NutsDependencyBuilder setExclusions(NutsId[] exclusions);

    /**
     * build new instance of NutsDependencies
     *
     * @return new instance of NutsDependencies
     */
    NutsDependency build();

    NutsDependencyBuilder setProperty(String property, String value);

    NutsDependencyBuilder addProperties(Map<String, String> queryMap);

    NutsDependencyBuilder addProperties(String propertiesQuery);

    String getPropertiesQuery();

    Map<String, String> getProperties();

    NutsDependencyBuilder setProperties(Map<String, String> queryMap);

    NutsDependencyBuilder setProperties(String propertiesQuery);

    /**
     * return condition builder
     *
     * @return condition condition builder
     */
    NutsEnvConditionBuilder getCondition();

}
