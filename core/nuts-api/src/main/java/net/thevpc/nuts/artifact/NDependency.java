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

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NOptional;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * NutsDependency is an <strong>immutable</strong> object that contains all information about a package's dependency.
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.5.3
 */
public interface NDependency extends Serializable, NBlankable {

    static NOptional<NDependency> get(String value) {
        return NId.get(value).map(NId::toDependency);
    }

    static NOptional<NDependency> get(NId value) {
        if (value == null) {
            return NOptional.ofNamedEmpty("id");
        }
        return NOptional.of(value.toDependency());
    }

    static NDependency of(String value) {
        return get(value).get();
    }

    static NDependency of(NId value) {
        return get(value).get();
    }

    /**
     * return mutable id builder instance initialized with {@code this} instance.
     *
     * @return mutable id builder instance initialized with {@code this} instance
     */
    NDependencyBuilder builder();

    /**
     * true if this dependency is optional.
     * equivalent to {@code Boolean.parseBoolean(getOptional())}
     *
     * @return true if this dependency is optional.
     */
    boolean isOptional();

    boolean isAnyProvided();

    boolean isAnyRuntime();

    boolean isProvided();

    boolean isRuntime();

    boolean isAnyTest();

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
    @NGetter
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
    NEnvCondition condition();

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
    Map<String, String> properties();

}
