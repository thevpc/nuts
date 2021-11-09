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
import net.thevpc.nuts.spi.NutsComponent;

import java.io.Serializable;
import java.util.Map;

/**
 * Nuts environment condition builder, used to check against the current environment
 *
 * @app.category Descriptor
 * @since 0.8.3
 */
public interface NutsEnvConditionBuilder extends Serializable, NutsComponent {

    static NutsEnvConditionBuilder of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsEnvConditionBuilder.class, true, null);
    }

    /**
     * supported archs. if empty, all arch are supported (for example for java, all arch are supported).
     *
     * @return supported archs
     */
    String[] getArch();

    /**
     * set archs
     *
     * @param archs value to set
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder setArch(String... archs);

    /**
     * supported operating systems. if empty, all oses are supported (for example for java, all arch are supported).
     *
     * @return supported oses
     */
    String[] getOs();

    /**
     * set os
     *
     * @param os value to set
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder setOs(String... os);

    /**
     * supported operating system distributions (mostly for linux systems). if empty, all distributions are supported.
     *
     * @return supported operating system distributions
     */
    String[] getOsDist();

    /**
     * set os dist
     *
     * @param osDist value to set
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder setOsDist(String... osDist);

    /**
     * supported platforms (java, dotnet, ...). if empty platform is not relevant.
     * This is helpful to bind application to a jdk version for instance (in that case platform may be in the form java#8 for instance)
     *
     * @return supported platforms
     */
    String[] getPlatform();

    /**
     * set platform
     *
     * @param platform value to set
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder setPlatform(String... platform);

    /**
     * supported desktop environment (kde, gnome, none, ...). if empty desktop environment is not relevant.
     *
     * @return supported environment list
     */
    String[] getDesktopEnvironment();

    /**
     * set desktopEnvironment
     *
     * @param desktopEnvironment value to set
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder setDesktopEnvironment(String... desktopEnvironment);

    /**
     * add os
     *
     * @param os new value to add
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder addOs(String os);

    /**
     * add os dist
     *
     * @param osDist new value to add
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder addOsDist(String osDist);

    /**
     * add arch
     *
     * @param arch new value to add
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder addArch(String arch);

    /**
     * add platform
     *
     * @param platform new value to add
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder addPlatform(String platform);

    /**
     * remove os
     *
     * @param os value to remove
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder removeOs(String os);

    /**
     * remove os dist
     *
     * @param osDist value to remove
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder removeOsDist(String osDist);

    /**
     * remove arch
     *
     * @param arch value to remove
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder removeArch(String arch);

    /**
     * remove platform
     *
     * @param platform value to remove
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder removePlatform(String platform);

    /**
     * set all fields from {@code other}
     *
     * @param other builder to copy from
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder setAll(NutsEnvConditionBuilder other);

    /**
     * set all fields from {@code other}
     *
     * @param other descriptor to copy from
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder setAll(NutsEnvCondition other);

    /**
     * add all fields from {@code other}
     *
     * @param other descriptor to copy from
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder addAll(NutsEnvCondition other);

    /**
     * add all fields from {@code other}
     *
     * @param other descriptor to copy from
     * @return {@code this} instance
     */
    NutsEnvConditionBuilder addAll(NutsEnvConditionBuilder other);

    /**
     * clear this instance (set null/default all properties)
     *
     * @return {@code this instance}
     */
    NutsEnvConditionBuilder clear();

    NutsEnvConditionBuilder removeDesktopEnvironment(String desktopEnvironment);

    /**
     * create new Descriptor filled with this builder fields.
     *
     * @return {@code this} instance
     */
    NutsEnvCondition build();

    NutsEnvConditionBuilder copy();

    NutsEnvConditionBuilder applyProperties(Map<String, String> properties);
}
