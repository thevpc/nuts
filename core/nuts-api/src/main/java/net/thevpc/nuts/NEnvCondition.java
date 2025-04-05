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

import net.thevpc.nuts.util.NBlankable;

import java.util.List;
import java.util.Map;

/**
 * Nuts environment condition, used to check against the current environment
 *
 * @app.category Descriptor
 * @since 0.8.3
 */
public interface NEnvCondition extends NBlankable {
    NEnvCondition BLANK=new DefaultNEnvCondition();
    /**
     * supported profiles (such as maven profiles)
     *
     * @return supported supported profiles
     */
    List<String> getProfiles();

    /**
     * supported arch list. if empty, all arch are supported (for example for java, all arch are supported).
     *
     * @return supported arch list
     */
    List<String> getArch();

    /**
     * supported operating systems. if empty, all oses are supported (for example for java, all arch are supported).
     *
     * @return supported oses
     */
    List<String> getOs();

    /**
     * supported operating system distributions (mostly for linux systems). if empty, all distributions are supported.
     *
     * @return supported operating system distributions
     */
    List<String> getOsDist();

    /**
     * supported platforms (java, dotnet, ...). if empty platform is not relevant.
     * This is helpful to bind application to a jdk version for instance (in that case platform may be in the form java#8 for instance)
     *
     * @return supported platforms
     */
    List<String> getPlatform();

    /**
     * supported desktop environments (gnome, kde, none, ...). if empty desktop environment is not relevant.
     * This is helpful to bind application to a specific environment
     *
     * @return supported platforms
     */
    List<String> getDesktopEnvironment();

    /**
     * create builder from this instance
     *
     * @return builder copy of this instance
     */
    NEnvConditionBuilder builder();

    NEnvCondition readOnly();

    /**
     * return env properties
     * @since 0.8.4
     * @return env properties
     */
    Map<String, String> getProperties();

    Map<String, String> toMap();
}
