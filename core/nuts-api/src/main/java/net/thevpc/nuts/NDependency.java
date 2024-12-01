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

import net.thevpc.nuts.util.NBlankable;
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

    static NOptional<NDependency> of(String value) {
        return NId.of(value).map(NId::toDependency);
    }
    static NOptional<NDependency> of(NId value) {
        if(value==null){
            return NOptional.ofNamedEmpty("id");
        }
        return NOptional.of(value.toDependency());
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

    /**
     * Indicates the dependency is optional for use of this library.
     *
     * @return string representation (or $ var) that can be evaluated as 'true'
     */
    String getOptional();

    /**
     * get scope string value (may be $ var).
     *
     * @return scope string value (may be $ var)
     */
    String getScope();

    /**
     * get classifier string value (may be $ var)
     *
     * @return classifier string
     */
    String getClassifier();

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
    String getRepository();


    /**
     * return artifact group id (aka groupId in maven)
     *
     * @return artifact group id (aka groupId in maven)
     */
    String getGroupId();

    /**
     * return artifact id (aka artifactId)
     *
     * @return artifact id (aka artifactId in maven)
     */
    String getArtifactId();

    /**
     * return dependency full name in the form
     * group:name
     *
     * @return return dependency short name
     */
    String getSimpleName();

    /**
     * return dependency full name in the form
     * group:name#version
     *
     * @return return dependency long name
     */
    String getLongName();

    /**
     * return dependency full name in the form
     * group:name#version?scope=&lt;scope&gt;{@code &}optional=&lt;optional&gt;
     *
     * @return return dependency full name
     */
    String getFullName();

    /**
     * return dependency version
     *
     * @return return dependency version
     */
    NVersion getVersion();

    NEnvCondition getCondition();

    String getType();

    /**
     * dependency exclusions
     *
     * @return dependency exclusions
     */
    List<NId> getExclusions();

    /**
     * properties in the URL query form
     *
     * @return properties in the URL query form.
     * @since 0.5.7
     */
    String getPropertiesQuery();

    /**
     * properties in the URL query form
     *
     * @return properties in the URL query form.
     * @since 0.5.7
     */
    Map<String, String> getProperties();

}
