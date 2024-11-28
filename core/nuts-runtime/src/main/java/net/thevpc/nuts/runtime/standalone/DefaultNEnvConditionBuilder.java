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
package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.DefaultNEnvCondition;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NEnvCondition;
import net.thevpc.nuts.NEnvConditionBuilder;
import net.thevpc.nuts.boot.NEnvConditionBoot;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NStringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Nuts environment condition builder, used to check against the current environment
 *
 * @app.category Descriptor
 * @since 0.8.3
 */
public class DefaultNEnvConditionBuilder implements Serializable, NEnvConditionBuilder {
    private static final long serialVersionUID = 1L;

    private List<String> arch = new ArrayList<>(); //defaults to empty
    private List<String> os = new ArrayList<>(); //defaults to empty;
    private List<String> osDist = new ArrayList<>(); //defaults to empty;
    private List<String> platform = new ArrayList<>(); //defaults to empty;
    private List<String> desktopEnvironment = new ArrayList<>(); //defaults to empty;
    private List<String> profiles = new ArrayList<>(); //defaults to empty;
    private Map<String, String> properties = new HashMap<>();

    public DefaultNEnvConditionBuilder() {
    }

    public DefaultNEnvConditionBuilder(NEnvCondition other) {
        addAll(other);
    }

    public List<String> getArch() {
        return arch;
    }

    @Override
    public NEnvConditionBuilder setArch(List<String> arch) {
        this.arch = NReservedLangUtils.uniqueNonBlankList(arch);
        return this;
    }

    public List<String> getOs() {
        return os;
    }

    @Override
    public NEnvConditionBuilder setOs(List<String> os) {
        this.os = NReservedLangUtils.uniqueNonBlankList(os);
        return this;
    }

    public List<String> getOsDist() {
        return osDist;
    }

    @Override
    public NEnvConditionBuilder setOsDist(List<String> osDist) {
        this.osDist = NReservedLangUtils.uniqueNonBlankList(osDist);
        return this;
    }

    public List<String> getPlatform() {
        return platform;
    }

    @Override
    public NEnvConditionBuilder setPlatform(List<String> platform) {
        this.platform = NReservedLangUtils.uniqueNonBlankList(platform);
        return this;
    }

    public List<String> getDesktopEnvironment() {
        return desktopEnvironment;
    }

    @Override
    public NEnvConditionBuilder setDesktopEnvironment(List<String> desktopEnvironment) {
        this.desktopEnvironment = NReservedLangUtils.uniqueNonBlankList(desktopEnvironment);
        return this;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    @Override
    public NEnvConditionBuilder setProfile(List<String> profiles) {
        this.profiles = profiles;
        return this;
    }

    @Override
    public NEnvConditionBuilder setAll(NEnvCondition other) {
        clear();
        addAll(other);
        return this;
    }

    public NEnvConditionBuilder setAll(NEnvConditionBoot other) {
        clear();
        addAll(other);
        return this;
    }

    @Override
    public NEnvConditionBuilder addAll(NEnvCondition other) {
        if (other != null) {
            setArch(mergeLists(getArch(), other.getArch()));
            setOs(mergeLists(getOs(), other.getOs()));
            setOsDist(mergeLists(getOsDist(), other.getOsDist()));
            setPlatform(mergeLists(getPlatform(), other.getPlatform()));
            setDesktopEnvironment(mergeLists(getDesktopEnvironment(), other.getDesktopEnvironment()));
            setProfile(mergeLists(getProfiles(), other.getProfiles()));
            setProperties(mergeMaps(getProperties(), other.getProperties()));
        }
        return this;
    }

    public NEnvConditionBuilder addAll(NEnvConditionBoot other) {
        if (other != null) {
            setArch(mergeLists(getArch(), other.getArch()));
            setOs(mergeLists(getOs(), other.getOs()));
            setOsDist(mergeLists(getOsDist(), other.getOsDist()));
            setPlatform(mergeLists(getPlatform(), other.getPlatform()));
            setDesktopEnvironment(mergeLists(getDesktopEnvironment(), other.getDesktopEnvironment()));
            setProfile(mergeLists(getProfiles(), other.getProfiles()));
            setProperties(mergeMaps(getProperties(), other.getProperties()));
        }
        return this;
    }

    private Map<String, String> mergeMaps(Map<String, String> a, Map<String, String> b) {
        LinkedHashMap<String, String> n = new LinkedHashMap<>();
        if (a != null) {
            n.putAll(a);
        }
        if (a != null) {
            n.putAll(b);
        }
        return n;
    }

    private List<String> mergeLists(List<String> a, List<String> b) {
        LinkedHashSet<String> n = new LinkedHashSet<>();
        if (a != null) {
            n.addAll(a);
        }
        if (a != null) {
            n.addAll(b);
        }
        return new ArrayList<>(n);
    }


    @Override
    public NEnvConditionBuilder clear() {
        setArch(new ArrayList<>());
        setOs(new ArrayList<>());
        setOsDist(new ArrayList<>());
        setPlatform(new ArrayList<>());
        setDesktopEnvironment(new ArrayList<>());
        setProfile(new ArrayList<>());
        setProperties(new LinkedHashMap<>());
        return this;
    }

    @Override
    public NEnvCondition build() {
        return readOnly();
    }

    @Override
    public NEnvCondition copy() {
        return builder();
    }

    @Override
    public NEnvCondition readOnly() {
        return new DefaultNEnvCondition(
                getArch(), getOs(), getOsDist(), getPlatform(),
                getDesktopEnvironment(),
                getProfiles(),
                properties
        );
    }

    @Override
    public NEnvConditionBuilder builder() {
        return new DefaultNEnvConditionBuilder(this);
    }

    @Override
    public String toString() {
        String s = String.join(" & ",
                Arrays.stream(new String[]{
                                ts("arch", arch.toArray(new String[0])),
                                ts("os", os.toArray(new String[0])),
                                ts("osDist", osDist.toArray(new String[0])),
                                ts("platform", platform.toArray(new String[0])),
                                ts("desktop", desktopEnvironment.toArray(new String[0])),
                                ts("profile", profiles.toArray(new String[0])),
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

    private String ts(String n, String[] vs) {
        if (vs.length == 0) {
            return "";
        }
        return n + "=" + String.join(",", vs[0]);
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public NEnvConditionBuilder setProperties(Map<String, String> properties) {
        this.properties = properties == null ? null : new HashMap<>(properties);
        return this;
    }

    @Override
    public NEnvConditionBuilder addProperties(Map<String, String> properties) {
        if(properties!=null){
            for (Map.Entry<String, String> e : properties.entrySet()) {
                addProperty(e.getKey(),e.getValue());
            }
        }
        return this;
    }

    @Override
    public NEnvConditionBuilder addProperty(String key, String value) {
        key= NStringUtils.trimToNull(key);
        if(key!=null) {
            if (this.properties == null) {
                this.properties = new HashMap<>();
            }
            if(value==null){
                this.properties.remove(key);
            }else{
                this.properties.put(key,value);
            }
        }
        return this;
    }

    @Override
    public NEnvConditionBuilder addDesktopEnvironment(String value) {
        this.desktopEnvironment= NReservedLangUtils.addUniqueNonBlankList(this.desktopEnvironment,value);
        return this;
    }

    @Override
    public NEnvConditionBuilder addDesktopEnvironments(String... values) {
        this.desktopEnvironment= NReservedLangUtils.addUniqueNonBlankList(this.desktopEnvironment,values);
        return this;
    }

    @Override
    public NEnvConditionBuilder addArchs(String value) {
        this.arch= NReservedLangUtils.addUniqueNonBlankList(this.arch,value);
        return this;
    }

    @Override
    public NEnvConditionBuilder addArchs(String... values) {
        this.arch= NReservedLangUtils.addUniqueNonBlankList(this.arch,values);
        return this;
    }

    @Override
    public NEnvConditionBuilder addOs(String value) {
        this.os= NReservedLangUtils.addUniqueNonBlankList(this.os,value);
        return this;
    }

    @Override
    public NEnvConditionBuilder addOses(String... values) {
        this.os= NReservedLangUtils.addUniqueNonBlankList(this.os,values);
        return this;
    }

    @Override
    public NEnvConditionBuilder addOsDist(String value) {
        this.osDist= NReservedLangUtils.addUniqueNonBlankList(this.osDist,value);
        return this;
    }

    @Override
    public NEnvConditionBuilder addOsDists(String... values) {
        this.osDist= NReservedLangUtils.addUniqueNonBlankList(this.osDist,values);
        return this;
    }

    @Override
    public NEnvConditionBuilder addPlatform(String value) {
        this.platform= NReservedLangUtils.addUniqueNonBlankList(this.platform,value);
        return this;
    }

    @Override
    public NEnvConditionBuilder addPlatforms(String... values) {
        this.platform= NReservedLangUtils.addUniqueNonBlankList(this.platform,values);
        return this;
    }

    @Override
    public NEnvConditionBuilder addProfile(String value) {
        this.profiles= NReservedLangUtils.addUniqueNonBlankList(this.profiles,value);
        return this;
    }

    @Override
    public NEnvConditionBuilder addProfiles(String... values) {
        this.profiles= NReservedLangUtils.addUniqueNonBlankList(this.profiles,values);
        return this;
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
    public boolean isBlank() {
        if (arch != null && !arch.isEmpty()) return false;
        if (os != null && !os.isEmpty()) return false;
        if (osDist != null && !osDist.isEmpty()) return false;
        if (platform != null && !platform.isEmpty()) return false;
        if (desktopEnvironment != null && !desktopEnvironment.isEmpty()) return false;
        if (profiles != null && !profiles.isEmpty()) return false;
        if (properties != null && !properties.isEmpty()) return false;
        return true;
    }

    public Map<String, String> toMap() {
        return NReservedUtils.toMap(this);
    }

    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
