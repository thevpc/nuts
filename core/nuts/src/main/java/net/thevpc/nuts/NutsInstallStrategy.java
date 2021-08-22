/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
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

/**
 * Install strategy defines the strategy used by installer

 * @app.category Base
 */
public enum NutsInstallStrategy implements NutsEnum{
    /**
     * the default strategy points to 'INSTALL' but this can be configured.
     */
    DEFAULT,

    /**
     * Install the artifact as 'required'.
     */
    REQUIRE,

    /**
     * Install the artifact if not already installed. All dependencies will
     * be fetched and marked as 'required'.
     * If the artifact is 'required', it will be promoted to 'installed'.
     */
    INSTALL,

    /**
     * reinstall the artifact if already installed. re-fetch the artifact if already required.
     * If wont promote 'required' to 'installed'. All dependencies will
     * be fetched and marked as 'required'.
     */
    REINSTALL,

    /**
     * reinstall the artifact if already installed. re-fetch the artifact if already required.
     * If wont promote 'required' to 'installed'.
     * No dependency will be fetched.
     */
    REPAIR,

    /**
     * switch default version. This is applicable only if the artifact is already installed.
     * No dependency will be fetched.
     */
    SWITCH_VERSION;

    private String id;

    NutsInstallStrategy() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    @Override
    public String id() {
        return id;
    }

    public static NutsInstallStrategy parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsInstallStrategy parseLenient(String value, NutsInstallStrategy emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsInstallStrategy parseLenient(String value, NutsInstallStrategy emptyValue, NutsInstallStrategy errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsInstallStrategy.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }
}
