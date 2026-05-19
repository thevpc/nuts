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

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.io.Serializable;
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
public interface NDependencyBuilder extends NComponent, Serializable, NBlankable {

    static NDependencyBuilder of(String groupId, String artifactId) {
        return of().groupId(groupId).artifactId(artifactId);
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
    @NSetter
    NDependencyBuilder id(NId id);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NDependencyBuilder dependency(NDependencyBuilder value);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NDependencyBuilder copyFrom(NDependencyBuilder value);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    NDependencyBuilder copyFrom(NDependency value);

    /**
     * reset this instance with value
     *
     * @param value new value
     * @return {@code this} instance
     */
    @NSetter
    NDependencyBuilder dependency(NDependency value);

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
    @NSetter
    NDependencyBuilder type(String type);

    /**
     * set optional value
     *
     * @param optional new value
     * @return {@code this} instance
     */
    NDependencyBuilder optional(String optional);

    /**
     * set scope value
     *
     * @param scope new value
     * @return {@code this} instance
     */
    @NSetter
    NDependencyBuilder scope(NDependencyScope scope);

    /**
     * set scope value
     *
     * @param scope new value
     * @return {@code this} instance
     */
    NDependencyBuilder scope(String scope);

    /**
     * set repository value
     *
     * @param repository new value
     * @return {@code this} instance
     */
    @NSetter
    NDependencyBuilder repository(String repository);

    /**
     * set group value
     *
     * @param groupId new value
     * @return {@code this} instance
     */
    @NSetter
    NDependencyBuilder groupId(String groupId);

    /**
     * set name value
     *
     * @param artifactId new value
     * @return {@code this} instance
     */
    @NSetter
    NDependencyBuilder artifactId(String artifactId);

    /**
     * set classifier value
     *
     * @param classifier new value
     * @return {@code this} instance
     */
    @NSetter
    NDependencyBuilder classifier(String classifier);

    /**
     * set version value
     *
     * @param version new value
     * @return {@code this} instance
     */
    @NSetter
    NDependencyBuilder version(NVersion version);

    /**
     * set version value
     *
     * @param version new value
     * @return {@code this} instance
     */
    NDependencyBuilder version(String version);

    /**
     * set exclusions value
     *
     * @param exclusions new value
     * @return {@code this} instance
     */
    NDependencyBuilder exclusions(List<NId> exclusions);

    /**
     * build new instance of NutsDependencies
     *
     * @return new instance of NutsDependencies
     */
    NDependency build();

    NDependencyBuilder property(String property, String value);

    NDependencyBuilder propertiesQuery(String propertiesQuery);

    NDependencyBuilder properties(Map<String, String> queryMap);

    NDependencyBuilder addPropertiesQuery(String propertiesQuery);

    NDependencyBuilder addProperties(Map<String, String> queryMap);

    /**
     * set condition
     *
     * @param condition condition
     * @return {@code this} instance
     */
    NDependencyBuilder condition(NEnvCondition condition);

    NDependencyBuilder condition(NEnvConditionBuilder condition);

    NDependencyBuilder removeCondition();

    NDependencyBuilder copy();


    /**
     * true if this dependency is optional.
     * equivalent to {@code Boolean.parseBoolean(getOptional())}
     *
     * @return true if this dependency is optional.
     */
    boolean isOptional();

    /**
     * Indicates the dependency is optional for use of this library.
     *
     * @return string representation (or $ var) that can be evaluated as 'true'
     */
    @NGetter
    String optional();

    /**
     * get scope string value (may be $ var).
     *
     * @return scope string value (may be $ var)
     */
    String scope();

    /**
     * get classifier string value (may be $ var)
     *
     * @return classifier string
     */
    @NGetter
    String classifier();

    /**
     * convert to NutsId
     *
     * @return converted to NutsId
     */
    NId toId();

    /**
     * return repository
     *
     * @return repository
     */
    @NGetter
    String repository();


    /**
     * return artifact group id (aka groupId in maven)
     *
     * @return artifact group id (aka groupId in maven)
     */
    @NGetter
    String groupId();

    /**
     * return artifact id (aka artifactId)
     *
     * @return artifact id (aka artifactId in maven)
     */
    @NGetter
    String artifactId();

    /**
     * return dependency full name in the form
     * group:name
     *
     * @return return dependency short name
     */
    @NGetter
    String shortName();

    /**
     * return dependency full name in the form
     * group:name#version
     *
     * @return return dependency long name
     */
    @NGetter
    String longName();

    /**
     * return dependency full name in the form
     * group:name#version?scope=&lt;scope&gt;{@code &}optional=&lt;optional&gt;
     *
     * @return return dependency full name
     */
    @NGetter
    String fullName();

    /**
     * return dependency version
     *
     * @return return dependency version
     */
    @NGetter
    NVersion version();

    @NGetter
    NEnvConditionBuilder condition();

    @NGetter
    String type();

    /**
     * dependency exclusions
     *
     * @return dependency exclusions
     */
    @NGetter
    List<NId> exclusions();

    /**
     * properties in the URL query form
     *
     * @return properties in the URL query form.
     * @since 0.5.7
     */
    @NGetter
    String propertiesQuery();

    /**
     * properties in the URL query form
     *
     * @return properties in the URL query form.
     * @since 0.5.7
     */
    @NGetter
    Map<String, String> properties();

}
