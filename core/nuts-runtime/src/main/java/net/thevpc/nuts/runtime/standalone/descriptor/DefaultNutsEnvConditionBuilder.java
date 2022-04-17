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
package net.thevpc.nuts.runtime.standalone.descriptor;

import net.thevpc.nuts.NutsEnvCondition;
import net.thevpc.nuts.NutsEnvConditionBuilder;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.MapToFunction;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreArrayUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultNutsEnvConditionBuilder implements NutsEnvConditionBuilder {

    private static final long serialVersionUID = 1L;

    private List<String> arch = new ArrayList<>(); //defaults to empty
    private List<String> os = new ArrayList<>(); //defaults to empty;
    private List<String> osDist = new ArrayList<>(); //defaults to empty;
    private List<String> platform = new ArrayList<>(); //defaults to empty;
    private List<String> desktopEnvironment = new ArrayList<>(); //defaults to empty;
    private List<String> profiles = new ArrayList<>(); //defaults to empty;
    private Map<String,String> properties=new HashMap<>();
    private transient NutsSession session;

    public DefaultNutsEnvConditionBuilder() {
    }

    public DefaultNutsEnvConditionBuilder(NutsSession session) {
        this.session = session;
    }

    public DefaultNutsEnvConditionBuilder(NutsEnvCondition other, NutsSession session) {
        this.session = session;
        setAll(other);
    }

    public NutsEnvConditionBuilder addDesktopEnvironment(String desktopEnvironment) {
        if (desktopEnvironment != null) {
            if (this.desktopEnvironment == null) {
                this.desktopEnvironment = new ArrayList<>();
            }
            this.desktopEnvironment.add(desktopEnvironment);
        }
        return this;
    }

    @Override
    public String[] getArch() {
        return CoreArrayUtils.toDistinctTrimmedNonEmptyArray(arch.toArray(new String[0]));
    }

    public NutsEnvConditionBuilder setArch(String[] arch) {
        this.arch = new ArrayList<>(Arrays.asList(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(arch)));
        return this;
    }

    public String[] getOs() {
        return CoreArrayUtils.toDistinctTrimmedNonEmptyArray(os.toArray(new String[0]));
    }

    public NutsEnvConditionBuilder setOs(String[] os) {
        this.os = new ArrayList<>(Arrays.asList(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(os)));
        return this;
    }

    public String[] getOsDist() {
        return CoreArrayUtils.toDistinctTrimmedNonEmptyArray(osDist.toArray(new String[0]));
    }

    public NutsEnvConditionBuilder setOsDist(String[] osDist) {
        this.osDist = new ArrayList<>(Arrays.asList(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(osDist)));
        return this;
    }

    public String[] getPlatform() {
        return CoreArrayUtils.toDistinctTrimmedNonEmptyArray(platform.toArray(new String[0]));
    }

    public NutsEnvConditionBuilder setPlatform(String[] platform) {
        this.platform = new ArrayList<>(Arrays.asList(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(platform)));
        return this;
    }

    public String[] getDesktopEnvironment() {
        return CoreArrayUtils.toDistinctTrimmedNonEmptyArray(desktopEnvironment.toArray(new String[0]));
    }

    public NutsEnvConditionBuilder setDesktopEnvironment(String[] desktopEnvironment) {
        this.desktopEnvironment = new ArrayList<>(Arrays.asList(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(desktopEnvironment)));
        return this;
    }

    public String[] getProfile() {
        return CoreArrayUtils.toDistinctTrimmedNonEmptyArray(profiles.toArray(new String[0]));
    }

    public NutsEnvConditionBuilder setProfile(String[] profiles) {
        this.profiles = new ArrayList<>(Arrays.asList(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(profiles)));
        return this;
    }

    @Override
    public NutsEnvConditionBuilder addProfile(String profile) {
        if (this.profiles == null) {
            this.profiles = new ArrayList<>();
        }
        this.profiles.add(profile);
        return this;
    }

    @Override
    public NutsEnvConditionBuilder addOs(String os) {
        if (this.os == null) {
            this.os = new ArrayList<>();
        }
        this.os.add(os);
        return this;
    }

    @Override
    public NutsEnvConditionBuilder addOsDist(String osDist) {
        if (this.osDist == null) {
            this.osDist = new ArrayList<>();
        }
        this.osDist.add(osDist);
        return this;
    }

    @Override
    public NutsEnvConditionBuilder addArch(String arch) {
        if (this.arch == null) {
            this.arch = new ArrayList<>();
        }
        this.arch.add(arch);
        return this;
    }

    //    @Override
//    public NutsDescriptorBuilder setExt(String ext) {
//        this.ext = NutsUtilStrings.trim(ext);
//        return this;
//    }
    public NutsEnvConditionBuilder addPlatform(String platform) {
        if (platform != null) {
            if (this.platform == null) {
                this.platform = new ArrayList<>();
            }
            this.platform.add(platform);
        }
        return this;
    }

    @Override
    public NutsEnvConditionBuilder removeOs(String os) {
        if (this.os != null) {
            this.os.remove(os);
        }
        return this;
    }

    @Override
    public NutsEnvConditionBuilder removeOsDist(String osDist) {
        if (this.osDist != null) {
            this.osDist.remove(osDist);
        }
        return this;
    }

    @Override
    public NutsEnvConditionBuilder removeArch(String arch) {
        if (this.arch != null) {
            this.arch.remove(arch);
        }
        return this;
    }

    @Override
    public NutsEnvConditionBuilder removePlatform(String platform) {
        if (this.platform != null) {
            this.platform.remove(platform);
        }
        return this;
    }

    @Override
    public NutsEnvConditionBuilder setAll(NutsEnvConditionBuilder other) {
        if (other != null) {
            setArch(other.getArch());
            setOs(other.getOs());
            setOsDist(other.getOsDist());
            setPlatform(other.getPlatform());
            setDesktopEnvironment(other.getDesktopEnvironment());
            setProfile(other.getProfile());
            setProperties(other.getProperties());
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NutsEnvConditionBuilder setAll(NutsEnvCondition other) {
        if (other != null) {
            setArch(other.getArch());
            setOs(other.getOs());
            setOsDist(other.getOsDist());
            setPlatform(other.getPlatform());
            setDesktopEnvironment(other.getDesktopEnvironment());
            setProfile(other.getProfile());
            setProperties(other.getProperties());
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NutsEnvConditionBuilder addAll(NutsEnvCondition other) {
        if (other != null) {
            setArch(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(getArch(), other.getArch()));
            setOs(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(getOs(), other.getOs()));
            setOsDist(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(getOsDist(), other.getOsDist()));
            setPlatform(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(getPlatform(), other.getPlatform()));
            setDesktopEnvironment(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(getDesktopEnvironment(), other.getDesktopEnvironment()));
            setProfile(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(getProfile(), other.getProfile()));
            Map<String,String> a=new HashMap<>(properties);
            Map<String, String> b = other.getProperties();
            if(b!=null) {
                a.putAll(b);
            }
            setProperties(a);
        }
        return this;
    }

    @Override
    public NutsEnvConditionBuilder addAll(NutsEnvConditionBuilder other) {
        if (other != null) {
            setArch(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(getArch(), other.getArch()));
            setOs(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(getOs(), other.getOs()));
            setOsDist(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(getOsDist(), other.getOsDist()));
            setPlatform(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(getPlatform(), other.getPlatform()));
            setDesktopEnvironment(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(getDesktopEnvironment(), other.getDesktopEnvironment()));
            setProfile(CoreArrayUtils.toDistinctTrimmedNonEmptyArray(getProfile(), other.getProfile()));
            Map<String,String> a=new HashMap<>(properties);
            Map<String, String> b = other.getProperties();
            if(b!=null) {
                a.putAll(b);
            }
            setProperties(a);
        }
        return this;
    }

    @Override
    public NutsEnvConditionBuilder clear() {
        setArch(null);
        setOs(null);
        setOsDist(null);
        setPlatform(null);
        setDesktopEnvironment(null);
        setProfile(null);
        setProperties(null);
        return this;
    }

    @Override
    public NutsEnvConditionBuilder removeDesktopEnvironment(String desktopEnvironment) {
        if (this.desktopEnvironment != null) {
            this.desktopEnvironment.remove(desktopEnvironment);
        }
        return this;
    }

    @Override
    public NutsEnvCondition build() {
        return new DefaultNutsEnvCondition(
                getArch(), getOs(), getOsDist(), getPlatform(),
                getDesktopEnvironment(),
                getProfile(),
                properties,
                session
        );
    }

    @Override
    public NutsEnvConditionBuilder copy() {
        return new DefaultNutsEnvConditionBuilder(session).setAll(this);
    }

    @Override
    public NutsEnvConditionBuilder applyProperties(Map<String, String> properties) {
        Function<String, String> map = new MapToFunction<>(properties);

        this.setArch(CoreNutsUtils.applyStringProperties(getArch(), map));
        this.setOs(CoreNutsUtils.applyStringProperties(getOs(), map));
        this.setOsDist(CoreNutsUtils.applyStringProperties(getOsDist(), map));
        this.setPlatform(CoreNutsUtils.applyStringProperties(getPlatform(), map));
        this.setDesktopEnvironment(CoreNutsUtils.applyStringProperties(getDesktopEnvironment(), map));
        this.setProfile(CoreNutsUtils.applyStringProperties(getProfile(), map));
        return this;
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
                                ts("props",properties)
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
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public NutsEnvConditionBuilder setProperties(Map<String, String> properties) {
        this.properties = properties==null?null:new HashMap<>(properties);
        return this;
    }
    private String ts(String n,Map<String,String> properties){
        if(properties.isEmpty()){
            return "";
        }
        return n+"={"+ properties.entrySet().stream().map(x->{
            String k = x.getKey();
            String v = x.getValue();
            if(v==null){
                return k;
            }
            return k+"="+v;
        }).collect(Collectors.joining(","))+"}";
    }
}
