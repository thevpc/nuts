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
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.spi.NComponent;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * @author thevpc
 * @app.category Base
 */
public interface NEnvs extends NComponent {
    static NEnvs of(NSession session) {
        return NExtensions.of(session).createSupported(NEnvs.class);
    }

    /**
     * @return properties
     * @since 0.8.1
     */
    Map<String, Object> getProperties();

    /**
     * return property raw value
     *
     * @param property property name
     * @return property raw value
     * @since 0.8.1
     */
    NOptional<NValue> getProperty(String property);

    /**
     * @param property property
     * @param value    value
     * @return {@code this} instance
     * @since 0.8.1
     */
    NEnvs setProperty(String property, Object value);

    NOsFamily getOsFamily();

    Set<NShellFamily> getShellFamilies();

    NShellFamily getShellFamily();

    NId getDesktopEnvironment();

    Set<NId> getDesktopEnvironments();

    NDesktopEnvironmentFamily getDesktopEnvironmentFamily();

    Set<NDesktopEnvironmentFamily> getDesktopEnvironmentFamilies();

    NPlatformManager platforms();

    NId getPlatform();

    NId getOs();

    NId getOsDist();

    NId getArch();

    NArchFamily getArchFamily();

    NSession getSession();

    NEnvs setSession(NSession session);

    boolean isGraphicalDesktopEnvironment();

    NSupportMode getDesktopIntegrationSupport(NDesktopIntegrationItem target);

    Path getDesktopPath();

    void addLauncher(NLauncherOptions launcher);

}
