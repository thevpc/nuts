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
package net.thevpc.nuts.runtime.standalone.descriptor;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreArrayUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsEnvCondition implements NutsEnvCondition {

    private static final long serialVersionUID = 1L;

    protected transient NutsSession session;

    @Override
    public NutsEnvConditionBuilder builder() {
        return NutsEnvConditionBuilder.of(session).setAll(this);
    }

    private String[] arch;
    private String[] profile;
    private String[] os;
    private String[] osDist;
    private String[] platform;
    private String[] desktopEnvironment;
    private Map<String,String> properties;

    public DefaultNutsEnvCondition(NutsEnvCondition d, NutsSession session) {
        this(
                d.getArch(),
                d.getOs(),
                d.getOsDist(),
                d.getPlatform(),
                d.getDesktopEnvironment(),
                d.getProfile(),
                ((DefaultNutsEnvCondition)d).getProperties(),
                session
        );
    }

    public DefaultNutsEnvCondition(NutsSession session){
        this(null,null,null,null,null,null,null,session);
    }

    public DefaultNutsEnvCondition(String[] arch, String[] os, String[] osDist,
                                   String[] platform,
                                   String[] desktopEnvironment,
                                   String[] profile,
                                   Map<String,String> properties,
                                   NutsSession session) {
        this.session=session;
        this.arch = CoreArrayUtils.toDistinctTrimmedNonEmptyArray(arch);
        this.os = CoreArrayUtils.toDistinctTrimmedNonEmptyArray(os);
        this.osDist = CoreArrayUtils.toDistinctTrimmedNonEmptyArray(osDist);
        this.platform = CoreArrayUtils.toDistinctTrimmedNonEmptyArray(platform);
        this.desktopEnvironment = CoreArrayUtils.toDistinctTrimmedNonEmptyArray(desktopEnvironment);
        this.profile = CoreArrayUtils.toDistinctTrimmedNonEmptyArray(profile);
        this.properties = properties==null?new HashMap<>() : new HashMap<>(properties);
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public boolean isBlank() {
        for (String s : arch) {
            if(!NutsBlankable.isBlank(s)){
                return false;
            }
        }
        for (String s : os) {
            if(!NutsBlankable.isBlank(s)){
                return false;
            }
        }
        for (String s : osDist) {
            if(!NutsBlankable.isBlank(s)){
                return false;
            }
        }
        for (String s : platform) {
            if(!NutsBlankable.isBlank(s)){
                return false;
            }
        }
        for (String s : desktopEnvironment) {
            if(!NutsBlankable.isBlank(s)){
                return false;
            }
        }
        for (String s : profile) {
            if(!NutsBlankable.isBlank(s)){
                return false;
            }
        }
        if(!properties.isEmpty()){
            return false;
        }
        return true;
    }

    @Override
    public String[] getProfile() {
        return profile;
    }

    @Override
    public String[] getArch() {
        return arch;
    }

    @Override
    public String[] getOs() {
        return os;
    }

    @Override
    public String[] getOsDist() {
        return osDist;
    }

    @Override
    public String[] getPlatform() {
        return platform;
    }

    @Override
    public String[] getDesktopEnvironment() {
        return desktopEnvironment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsEnvCondition that = (DefaultNutsEnvCondition) o;
        return Objects.equals(session, that.session)
                && Objects.equals(properties, that.properties)
                && Arrays.equals(arch, that.arch)
                && Arrays.equals(os, that.os)
                && Arrays.equals(osDist, that.osDist)
                && Arrays.equals(platform, that.platform)
                && Arrays.equals(profile, that.profile)
                && Arrays.equals(desktopEnvironment, that.desktopEnvironment);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(session);
        result = 31 * result + Objects.hashCode(properties);
        result = 31 * result + Arrays.hashCode(arch);
        result = 31 * result + Arrays.hashCode(os);
        result = 31 * result + Arrays.hashCode(osDist);
        result = 31 * result + Arrays.hashCode(platform);
        result = 31 * result + Arrays.hashCode(profile);
        result = 31 * result + Arrays.hashCode(desktopEnvironment);
        return result;
    }

    @Override
    public String toString() {
        String s= String.join(" & ",
                Arrays.stream(new String[]{
                                ts("profile", profile),
                                ts("arch",arch),
                                ts("os",os),
                                ts("osDist", osDist),
                                ts("platform",platform),
                                ts("desktop",desktopEnvironment),
                                ts("props",properties)
                        })
                        .filter(x->x.length()>0)
                        .toArray(String[]::new)
                );
        if(s.isEmpty()){
            return "blank";
        }
        return s;
    }

    private String ts(String n,String[] vs){
        if(vs.length==0){
            return "";
        }
        return n+"="+String.join(",",vs[0]);
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
