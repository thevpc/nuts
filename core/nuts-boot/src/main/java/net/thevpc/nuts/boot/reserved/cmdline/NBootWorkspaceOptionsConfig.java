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
 * <br> ====================================================================
 */
package net.thevpc.nuts.boot.reserved.cmdline;

import java.util.Objects;

/**
 * @author thevpc
 */
public class NBootWorkspaceOptionsConfig {

    private boolean shortOptions;
    private boolean singleArgOptions;
    private boolean omitDefaults;
    private String apiVersion;


    public NBootWorkspaceOptionsConfig setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public NBootWorkspaceOptionsConfig setCompact(boolean compact) {
        if (compact) {
            shortOptions = true;
            singleArgOptions = true;
            omitDefaults = true;
        } else {
            shortOptions = false;
            singleArgOptions = false;
            omitDefaults = false;
        }
        return this;
    }

    public boolean isShortOptions() {
        return shortOptions;
    }

    public boolean isSingleArgOptions() {
        return singleArgOptions;
    }

    public boolean isOmitDefaults() {
        return omitDefaults;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortOptions, singleArgOptions, omitDefaults, apiVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NBootWorkspaceOptionsConfig that = (NBootWorkspaceOptionsConfig) o;
        return shortOptions == that.shortOptions
                && singleArgOptions == that.singleArgOptions
                && omitDefaults == that.omitDefaults
                && Objects.equals(apiVersion, that.apiVersion);
    }

    @Override
    public String toString() {
        return "NutsWorkspaceOptionsConfig{" +
                ", shortOptions=" + shortOptions +
                ", singleArgOptions=" + singleArgOptions +
                ", omitDefaults=" + omitDefaults +
                ", apiVersion=" + apiVersion +
                '}';
    }
}