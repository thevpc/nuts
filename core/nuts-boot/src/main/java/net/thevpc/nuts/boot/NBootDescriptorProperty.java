/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.internal.util.NBootUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Descriptor Property Builder
 *
 * @author thevpc
 * @app.category Descriptor
 * @since 0.8.3
 */
public class NBootDescriptorProperty {

    private static final long serialVersionUID = 1L;

    private String name;
    private String value = null;
    private List<String> conditionArch = new ArrayList<>(); //defaults to empty
    private List<String> conditionOs = new ArrayList<>(); //defaults to empty;
    private List<String> conditionOsDist = new ArrayList<>(); //defaults to empty;
    private List<String> conditionPlatform = new ArrayList<>(); //defaults to empty;
    private List<String> conditionDesktopEnvironment = new ArrayList<>(); //defaults to empty;
    private List<String> conditionProfiles = new ArrayList<>(); //defaults to empty;
    private Map<String, String> conditionProperties = new HashMap<>();

    public NBootDescriptorProperty() {
    }

    public NBootDescriptorProperty(NBootDescriptorProperty other) {
        copyFrom(other);
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }


    public NBootDescriptorProperty name(String name) {
        this.name = name;
        return this;
    }


    public NBootDescriptorProperty value(String value) {
        this.value = value;
        return this;
    }


    public NBootDescriptorProperty copyFrom(NBootDescriptorProperty value) {
        if (value == null) {
            this.name(null);
            this.value(null);
            this.conditionOs(null)
                    .conditionOsDist(null)
                    .conditionArch(null)
                    .conditionPlatform(null)
                    .conditionDesktopEnvironment(null)
                    .conditionProfile(null)
                    .conditionProperties(null);

        } else {
            this.name(value.name());
            this.value(value.value());
            this.conditionOs(value.conditionOs())
                    .conditionOsDist(value.conditionOsDist())
                    .conditionArch(value.conditionArch())
                    .conditionPlatform(value.conditionPlatform())
                    .conditionDesktopEnvironment(value.conditionDesktopEnvironment())
                    .conditionProfile(value.conditionProfiles())
                    .conditionProperties(value.conditionProperties());
        }
        return this;
    }

    public boolean isBlank() {
        if (!NBootUtils.isBlank(name)) {
            return false;
        }
        if (!NBootUtils.isBlank(value)) {
            return false;
        }
        if (conditionArch != null && !conditionArch.isEmpty()) return false;
        if (conditionOs != null && !conditionOs.isEmpty()) return false;
        if (conditionOsDist != null && !conditionOsDist.isEmpty()) return false;
        if (conditionPlatform != null && !conditionPlatform.isEmpty()) return false;
        if (conditionDesktopEnvironment != null && !conditionDesktopEnvironment.isEmpty()) return false;
        if (conditionProfiles != null && !conditionProfiles.isEmpty()) return false;
        if (conditionProperties != null && !conditionProperties.isEmpty()) return false;
        return true;
    }

    public NBootDescriptorProperty copy() {
        return new NBootDescriptorProperty(this);
    }

    public List<String> conditionArch() {
        return conditionArch;
    }

    public List<String> conditionOs() {
        return conditionOs;
    }

    public List<String> conditionOsDist() {
        return conditionOsDist;
    }

    public List<String> conditionPlatform() {
        return conditionPlatform;
    }

    public List<String> conditionDesktopEnvironment() {
        return conditionDesktopEnvironment;
    }

    public List<String> conditionProfiles() {
        return conditionProfiles;
    }

    public Map<String, String> conditionProperties() {
        return conditionProperties;
    }

    public NBootDescriptorProperty conditionProperties(Map<String, String> conditionProperties) {
        this.conditionProperties = conditionProperties == null ? null : new HashMap<>(conditionProperties);
        return this;
    }

    public NBootDescriptorProperty conditionDesktopEnvironment(List<String> conditionDesktopEnvironment) {
        this.conditionDesktopEnvironment = NBootUtils.uniqueNonBlankStringList(conditionDesktopEnvironment);
        return this;
    }

    public NBootDescriptorProperty conditionProfile(List<String> profiles) {
        this.conditionProfiles = profiles;
        return this;
    }

    public NBootDescriptorProperty conditionPlatform(List<String> conditionPlatform) {
        this.conditionPlatform = NBootUtils.uniqueNonBlankStringList(conditionPlatform);
        return this;
    }

    public NBootDescriptorProperty conditionOsDist(List<String> conditionOsDist) {
        this.conditionOsDist = NBootUtils.uniqueNonBlankStringList(conditionOsDist);
        return this;
    }

    public NBootDescriptorProperty conditionOs(List<String> conditionOs) {
        this.conditionOs = NBootUtils.uniqueNonBlankStringList(conditionOs);
        return this;
    }

    public NBootDescriptorProperty conditionArch(List<String> conditionArch) {
        this.conditionArch = NBootUtils.uniqueNonBlankStringList(conditionArch);
        return this;
    }
}
