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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.reserved.util.NBootPlatformHome;
import net.thevpc.nuts.boot.reserved.util.NBootUtils;
import net.thevpc.nuts.boot.reserved.util.NBootStringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Nuts environment condition builder, used to check against the current environment
 *
 * @app.category Descriptor
 * @since 0.8.3
 */
public class NBootEnvCondition implements Serializable {
    public static final NBootEnvCondition BLANK=new NBootEnvCondition();
    private static final long serialVersionUID = 1L;

    private List<String> arch = new ArrayList<>(); //defaults to empty
    private List<String> os = new ArrayList<>(); //defaults to empty;
    private List<String> osDist = new ArrayList<>(); //defaults to empty;
    private List<String> platform = new ArrayList<>(); //defaults to empty;
    private List<String> desktopEnvironment = new ArrayList<>(); //defaults to empty;
    private List<String> profiles = new ArrayList<>(); //defaults to empty;
    private Map<String, String> properties = new HashMap<>();

    public NBootEnvCondition() {
    }

    public NBootEnvCondition(NBootEnvCondition other) {
        addAll(other);
    }

    public NBootEnvCondition(List<String> arch, List<String> os, List<String> osDist,
                             List<String> platform,
                             List<String> desktopEnvironment,
                             List<String> profile,
                             Map<String, String> properties) {
        this.arch = arch == null ? Collections.emptyList() : Collections.unmodifiableList(arch);
        this.os = os == null ? Collections.emptyList() : Collections.unmodifiableList(os);
        this.osDist = osDist == null ? Collections.emptyList() : Collections.unmodifiableList(osDist);
        this.platform = platform == null ? Collections.emptyList() : Collections.unmodifiableList(platform);
        this.desktopEnvironment = desktopEnvironment == null ? Collections.emptyList() : Collections.unmodifiableList(desktopEnvironment);
        this.profiles = profile == null ? Collections.emptyList() : Collections.unmodifiableList(profile);
        this.properties = properties == null ? new HashMap<>() : new HashMap<>(properties);
    }

    public static boolean isAcceptCondition(NBootEnvCondition cond) {
        List<String> oss = NBootUtils.uniqueNonBlankStringList(cond.getOs());
        List<String> archs = NBootUtils.uniqueNonBlankStringList(cond.getArch());
        if (!oss.isEmpty()) {
            String eos = NBootPlatformHome.currentOsFamily();
            boolean osOk = false;
            for (String e : oss) {
                NBootId ee = NBootId.of(e);
                if (ee.getShortName().equalsIgnoreCase(eos)) {
                    if (NBootUtils.acceptVersion(ee.getVersion(), System.getProperty("os.version"))) {
                        osOk = true;
                    }
                    break;
                }
            }
            if (!osOk) {
                return false;
            }
        }
        if (!archs.isEmpty()) {
            String earch = System.getProperty("os.arch");
            if (earch != null) {
                boolean archOk = false;
                for (String e : archs) {
                    if (!e.isEmpty()) {
                        if (e.equalsIgnoreCase(earch)) {
                            archOk = true;
                            break;
                        }
                    }
                }
                return archOk;
            }
        }
        return true;
    }


    public List<String> getArch() {
        return arch;
    }

    
    public NBootEnvCondition setArch(List<String> arch) {
        this.arch = NBootUtils.uniqueNonBlankStringList(arch);
        return this;
    }

    public List<String> getOs() {
        return os;
    }

    
    public NBootEnvCondition setOs(List<String> os) {
        this.os = NBootUtils.uniqueNonBlankStringList(os);
        return this;
    }

    public List<String> getOsDist() {
        return osDist;
    }

    
    public NBootEnvCondition setOsDist(List<String> osDist) {
        this.osDist = NBootUtils.uniqueNonBlankStringList(osDist);
        return this;
    }

    public List<String> getPlatform() {
        return platform;
    }

    
    public NBootEnvCondition setPlatform(List<String> platform) {
        this.platform = NBootUtils.uniqueNonBlankStringList(platform);
        return this;
    }

    public List<String> getDesktopEnvironment() {
        return desktopEnvironment;
    }

    
    public NBootEnvCondition setDesktopEnvironment(List<String> desktopEnvironment) {
        this.desktopEnvironment = NBootUtils.uniqueNonBlankStringList(desktopEnvironment);
        return this;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    
    public NBootEnvCondition setProfile(List<String> profiles) {
        this.profiles = profiles;
        return this;
    }

    
    public NBootEnvCondition setAll(NBootEnvCondition other) {
        clear();
        addAll(other);
        return this;
    }

    
    public NBootEnvCondition addAll(NBootEnvCondition other) {
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


    
    public NBootEnvCondition clear() {
        setArch(new ArrayList<>());
        setOs(new ArrayList<>());
        setOsDist(new ArrayList<>());
        setPlatform(new ArrayList<>());
        setDesktopEnvironment(new ArrayList<>());
        setProfile(new ArrayList<>());
        setProperties(new LinkedHashMap<>());
        return this;
    }

    
    public NBootEnvCondition build() {
        return readOnly();
    }

    
    public NBootEnvCondition copy() {
        return builder();
    }

    
    public NBootEnvCondition readOnly() {
        return new NBootEnvCondition(
                getArch(), getOs(), getOsDist(), getPlatform(),
                getDesktopEnvironment(),
                getProfiles(),
                properties
        );
    }

    
    public NBootEnvCondition builder() {
        return new NBootEnvCondition(this);
    }

    
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

    
    public Map<String, String> getProperties() {
        return properties;
    }

    
    public NBootEnvCondition setProperties(Map<String, String> properties) {
        this.properties = properties == null ? null : new HashMap<>(properties);
        return this;
    }

    
    public NBootEnvCondition addProperties(Map<String, String> properties) {
        if(properties!=null){
            for (Map.Entry<String, String> e : properties.entrySet()) {
                addProperty(e.getKey(),e.getValue());
            }
        }
        return this;
    }

    
    public NBootEnvCondition addProperty(String key, String value) {
        key= NBootStringUtils.trimToNull(key);
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

    
    public NBootEnvCondition addDesktopEnvironment(String value) {
        this.desktopEnvironment= NBootUtils.addUniqueNonBlankList(this.desktopEnvironment,value);
        return this;
    }

    
    public NBootEnvCondition addDesktopEnvironments(String... values) {
        this.desktopEnvironment= NBootUtils.addUniqueNonBlankList(this.desktopEnvironment,values);
        return this;
    }

    
    public NBootEnvCondition addArchs(String value) {
        this.arch= NBootUtils.addUniqueNonBlankList(this.arch,value);
        return this;
    }

    
    public NBootEnvCondition addArchs(String... values) {
        this.arch= NBootUtils.addUniqueNonBlankList(this.arch,values);
        return this;
    }

    
    public NBootEnvCondition addOs(String value) {
        this.os= NBootUtils.addUniqueNonBlankList(this.os,value);
        return this;
    }

    
    public NBootEnvCondition addOses(String... values) {
        this.os= NBootUtils.addUniqueNonBlankList(this.os,values);
        return this;
    }

    
    public NBootEnvCondition addOsDist(String value) {
        this.osDist= NBootUtils.addUniqueNonBlankList(this.osDist,value);
        return this;
    }

    
    public NBootEnvCondition addOsDists(String... values) {
        this.osDist= NBootUtils.addUniqueNonBlankList(this.osDist,values);
        return this;
    }

    
    public NBootEnvCondition addPlatform(String value) {
        this.platform= NBootUtils.addUniqueNonBlankList(this.platform,value);
        return this;
    }

    
    public NBootEnvCondition addPlatforms(String... values) {
        this.platform= NBootUtils.addUniqueNonBlankList(this.platform,values);
        return this;
    }

    
    public NBootEnvCondition addProfile(String value) {
        this.profiles= NBootUtils.addUniqueNonBlankList(this.profiles,value);
        return this;
    }

    
    public NBootEnvCondition addProfiles(String... values) {
        this.profiles= NBootUtils.addUniqueNonBlankList(this.profiles,values);
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
        return NBootUtils.toMap(this);
    }
}
