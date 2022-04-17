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

import java.util.Map;

/**
 * Nuts environment condition, used to check against the current environment
 *
 * @app.category Descriptor
 * @since 0.8.3
 */
public interface NutsEnvCondition extends NutsBlankable {
    /**
     * supported profiles (such as maven profiles)
     *
     * @return supported supported profiles
     */
    String[] getProfile();

    /**
     * supported arch list. if empty, all arch are supported (for example for java, all arch are supported).
     *
     * @return supported arch list
     */
    String[] getArch();

    /**
     * supported operating systems. if empty, all oses are supported (for example for java, all arch are supported).
     *
     * @return supported oses
     */
    String[] getOs();

    /**
     * supported operating system distributions (mostly for linux systems). if empty, all distributions are supported.
     *
     * @return supported operating system distributions
     */
    String[] getOsDist();

    /**
     * supported platforms (java, dotnet, ...). if empty platform is not relevant.
     * This is helpful to bind application to a jdk version for instance (in that case platform may be in the form java#8 for instance)
     *
     * @return supported platforms
     */
    String[] getPlatform();

    /**
     * supported desktop environments (gnome, kde, none, ...). if empty desktop environment is not relevant.
     * This is helpful to bind application to a specific environment
     *
     * @return supported platforms
     */
    String[] getDesktopEnvironment();

    /**
     * create builder from this instance
     *
     * @return builder copy of this instance
     */
    NutsEnvConditionBuilder builder();

    /**
     * true if no condition
     *
     * @return true if no condition
     */
    boolean isBlank();

    /**
     * return env properties
     * @since 0.8.4
     * @return env properties
     */
    Map<String, String> getProperties();
}
