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

import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNEnvCondition implements NEnvCondition {

    private static final long serialVersionUID = 1L;
    private List<String> arch;
    private List<String> profiles;
    private List<String> os;
    private List<String> osDist;
    private List<String> platform;
    private List<String> desktopEnvironment;
    private Map<String, String> properties;

    public DefaultNEnvCondition(NEnvCondition d) {
        this(
                d.getArch(),
                d.getOs(),
                d.getOsDist(),
                d.getPlatform(),
                d.getDesktopEnvironment(),
                d.getProfiles(),
                d.getProperties()
        );
    }

    public DefaultNEnvCondition() {
        this(null, null, null, null, null, null, null);
    }

    public DefaultNEnvCondition(List<String> arch, List<String> os, List<String> osDist,
                                List<String> platform,
                                List<String> desktopEnvironment,
                                List<String> profiles,
                                Map<String, String> properties) {
        this.arch = arch == null ? Collections.emptyList() : Collections.unmodifiableList(arch);
        this.os = os == null ? Collections.emptyList() : Collections.unmodifiableList(os);
        this.osDist = osDist == null ? Collections.emptyList() : Collections.unmodifiableList(osDist);
        this.platform = platform == null ? Collections.emptyList() : Collections.unmodifiableList(platform);
        this.desktopEnvironment = desktopEnvironment == null ? Collections.emptyList() : Collections.unmodifiableList(desktopEnvironment);
        this.profiles = profiles == null ? Collections.emptyList() : Collections.unmodifiableList(profiles);
        this.properties = properties == null ? new HashMap<>() : new HashMap<>(properties);
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public boolean isBlank() {
        for (String s : arch) {
            if (!NBlankable.isBlank(s)) {
                return false;
            }
        }
        for (String s : os) {
            if (!NBlankable.isBlank(s)) {
                return false;
            }
        }
        for (String s : osDist) {
            if (!NBlankable.isBlank(s)) {
                return false;
            }
        }
        for (String s : platform) {
            if (!NBlankable.isBlank(s)) {
                return false;
            }
        }
        for (String s : desktopEnvironment) {
            if (!NBlankable.isBlank(s)) {
                return false;
            }
        }
        for (String s : profiles) {
            if (!NBlankable.isBlank(s)) {
                return false;
            }
        }
        if (!properties.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public List<String> getProfiles() {
        return profiles;
    }

    @Override
    public List<String> getArch() {
        return arch;
    }

    @Override
    public List<String> getOs() {
        return os;
    }

    @Override
    public List<String> getOsDist() {
        return osDist;
    }

    @Override
    public List<String> getPlatform() {
        return platform;
    }

    @Override
    public List<String> getDesktopEnvironment() {
        return desktopEnvironment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNEnvCondition that = (DefaultNEnvCondition) o;
        return Objects.equals(properties, that.properties)
                && Objects.equals(arch, that.arch)
                && Objects.equals(os, that.os)
                && Objects.equals(osDist, that.osDist)
                && Objects.equals(platform, that.platform)
                && Objects.equals(profiles, that.profiles)
                && Objects.equals(desktopEnvironment, that.desktopEnvironment);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Objects.hashCode(properties);
        result = 31 * result + Objects.hashCode(arch);
        result = 31 * result + Objects.hashCode(os);
        result = 31 * result + Objects.hashCode(osDist);
        result = 31 * result + Objects.hashCode(platform);
        result = 31 * result + Objects.hashCode(profiles);
        result = 31 * result + Objects.hashCode(desktopEnvironment);
        return result;
    }

    @Override
    public String toString() {
        String s = String.join(" & ",
                Arrays.stream(new String[]{
                                ts("profile", profiles),
                                ts("arch", arch),
                                ts("os", os),
                                ts("osDist", osDist),
                                ts("platform", platform),
                                ts("desktop", desktopEnvironment),
                                ts("props", properties)
                        })
                        .filter(x -> x.length() > 0)
                        .toArray(String[]::new)
        );
        if (s.isEmpty()) {
            return "blank";
        }
        return s;
    }

    private String ts(String n, List<String> vs) {
        if (vs == null || vs.size() == 0) {
            return "";
        }
        return n + "=" + String.join(",", vs);
    }

    private String ts(String n, Map<String, String> properties) {
        if (properties.isEmpty()) {
            return "";
        }
        return n + "={" + properties.entrySet().stream().map(x -> {
            String k = x.getKey();
            String v = x.getValue();
            if (v == null) {
                return k;
            }
            return k + "=" + v;
        }).collect(Collectors.joining(",")) + "}";
    }

    @Override
    public NEnvConditionBuilder builder() {
        return NEnvConditionBuilder.of().copyFrom(this);
    }

    @Override
    public Map<String, String> toMap() {
        return NReservedUtils.toMap(this);
    }
}
