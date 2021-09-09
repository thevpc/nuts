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
package net.thevpc.nuts.runtime.core.model;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.QueryStringParser;
import net.thevpc.nuts.runtime.core.filters.DefaultNutsTokenFilter;

import java.util.*;

import net.thevpc.nuts.runtime.bundles.string.GlobUtils;
import net.thevpc.nuts.runtime.core.filters.id.NutsIdIdFilter;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsId implements NutsId {
    public static final long serialVersionUID = 1L;
    private final String groupId;
    private final String artifactId;
    private final NutsVersion version;
    private final String properties;
    private transient NutsWorkspace ws;
    private transient NutsSession session;

    public DefaultNutsId(String groupId, String artifactId, String version, Map<String, String> properties,NutsSession session) {
        this( groupId, artifactId, session.getWorkspace().version().parser().parse(version), properties,session);
    }

    protected DefaultNutsId(String groupId, String artifactId, NutsVersion version, Map<String, String> properties,NutsSession session) {
        this.ws = session.getWorkspace();
        this.session = session;
        this.groupId = NutsUtilStrings.trimToNull(groupId);
        this.artifactId = NutsUtilStrings.trimToNull(artifactId);
        this.version = version == null ? ws.version().parser().parse("") : version;
        this.properties = QueryStringParser.formatSortedPropertiesQuery(properties);
    }

    protected DefaultNutsId(String groupId, String artifactId, NutsVersion version, String properties, NutsSession session) {
        this.ws = session.getWorkspace();
        this.session = session;
        this.groupId = NutsUtilStrings.trimToNull(groupId);
        this.artifactId = NutsUtilStrings.trimToNull(artifactId);
        this.version = version == null ? ws.version().parser().parse("") : version;
        this.properties = QueryStringParser.formatSortedPropertiesQuery(properties);
    }

    public DefaultNutsId(String groupId, String artifactId, String version,NutsSession session) {
        this(groupId, artifactId, version, (String) null,session);
    }

    public DefaultNutsId(String groupId, String artifactId, String version, String properties, NutsSession session) {
        this.ws = session.getWorkspace();
        this.session = session;
        this.groupId = NutsUtilStrings.trimToNull(groupId);
        this.artifactId = NutsUtilStrings.trimToNull(artifactId);
        this.version = ws.version().parser().parse(version);
        this.properties = QueryStringParser.formatSortedPropertiesQuery(properties);
    }

    @Override
    public NutsFormat formatter() {
        return ws.id().formatter().setValue(this);
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public boolean isBlank() {
        return toString().isEmpty();
    }

    @Override
    public boolean matches(String pattern) {
        if (pattern == null) {
            return true;
        }
        return toString().matches(pattern);
    }

    @Override
    public boolean contains(String substring) {
        return toString().contains(substring);
    }

    @Override
    public NutsTokenFilter groupIdToken() {
        return new DefaultNutsTokenFilter(getGroupId());
    }

    @Override
    public NutsTokenFilter propertiesToken() {
        return new DefaultNutsTokenFilter(getPropertiesQuery());
    }

    @Override
    public NutsTokenFilter versionToken() {
        return new DefaultNutsTokenFilter(getVersion().getValue());
    }

    @Override
    public NutsTokenFilter artifactIdToken() {
        return new DefaultNutsTokenFilter(getArtifactId());
    }

    @Override
    public NutsTokenFilter repositoryToken() {
        return new DefaultNutsTokenFilter(getRepository());
    }

    @Override
    public NutsTokenFilter anyToken() {
        NutsTokenFilter[] oo = {groupIdToken(), propertiesToken(), versionToken(), artifactIdToken(), repositoryToken()};
        return new NutsTokenFilter() {
            @Override
            public boolean isNull() {
                for (NutsTokenFilter t : oo) {
                    if (t.isNull()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean isBlank() {
                for (NutsTokenFilter t : oo) {
                    if (t.isBlank()) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean like(String pattern) {
                for (NutsTokenFilter t : oo) {
                    if (t.like(pattern)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean matches(String pattern) {
                for (NutsTokenFilter t : oo) {
                    if (t.matches(pattern)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean contains(String pattern) {
                for (NutsTokenFilter t : oo) {
                    if (t.contains(pattern)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    @Override
    public boolean equalsShortName(NutsId other) {
        if (other == null) {
            return false;
        }
        return NutsUtilStrings.trim(artifactId).equals(NutsUtilStrings.trim(other.getArtifactId()))
                && NutsUtilStrings.trim(groupId).equals(NutsUtilStrings.trim(other.getGroupId()));
    }

    @Override
    public boolean like(String pattern) {
        if (pattern == null) {
            return true;
        }
        return GlobUtils.ofExact(pattern).matcher(toString()).matches();
    }

    @Override
    public String getFace() {
        String s = getProperties().get(NutsConstants.IdProperties.FACE);
        return NutsUtilStrings.trimToNull(s);
    }

    @Override
    public String getClassifier() {
        String s = getProperties().get(NutsConstants.IdProperties.CLASSIFIER);
        return NutsUtilStrings.trimToNull(s);
    }

    @Override
    public String getPackaging() {
        String s = getProperties().get(NutsConstants.IdProperties.PACKAGING);
        return NutsUtilStrings.trimToNull(s);
    }

    @Override
    public NutsEnvCondition getCondition() {
        NutsEnvConditionBuilder c = session.getWorkspace().descriptor().envConditionBuilder();
        Map<String, String> properties = getProperties();
        c.setOs(
                Arrays.stream(
                        NutsUtilStrings.trim(properties.get(NutsConstants.IdProperties.OS)).split(",")
                ).map(String::trim).filter(x -> x.length() > 0).distinct().toArray(String[]::new)
        );
        c.setOsDist(
                Arrays.stream(
                        NutsUtilStrings.trim(properties.get(NutsConstants.IdProperties.OS_DIST)).split(",")
                ).map(String::trim).filter(x -> x.length() > 0).distinct().toArray(String[]::new)
        );
        c.setPlatform(
                Arrays.stream(
                        NutsUtilStrings.trim(properties.get(NutsConstants.IdProperties.PLATFORM)).split(",")
                ).map(String::trim).filter(x -> x.length() > 0).distinct().toArray(String[]::new)
        );
        c.setArch(
                Arrays.stream(
                        NutsUtilStrings.trim(properties.get(NutsConstants.IdProperties.ARCH)).split(",")
                ).map(String::trim).filter(x -> x.length() > 0).distinct().toArray(String[]::new)
        );
        c.setDesktopEnvironment(
                Arrays.stream(
                        NutsUtilStrings.trim(properties.get(NutsConstants.IdProperties.DESKTOP_ENVIRONMENT)).split(",")
                ).map(String::trim).filter(x -> x.length() > 0).distinct().toArray(String[]::new)
        );

        return c.build();
    }

    @Override
    public String getPropertiesQuery() {
        return properties;
    }

    @Override
    public Map<String, String> getProperties() {
        return QueryStringParser.parseMap(properties);
    }

    @Override
    public String getRepository() {
        String s = getProperties().get(NutsConstants.IdProperties.REPO);
        return NutsUtilStrings.trimToNull(s);
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public NutsId getShortNameId() {
        return new DefaultNutsId( groupId, artifactId, (NutsVersion) null, "",session);
    }

    @Override
    public NutsId getLongNameId() {
        return new DefaultNutsId( groupId, artifactId, version, "",session);
    }

    @Override
    public String getShortName() {
        if (NutsUtilStrings.isBlank(groupId)) {
            return NutsUtilStrings.trim(artifactId);
        }
        return NutsUtilStrings.trim(groupId) + ":" + NutsUtilStrings.trim(artifactId);
    }

    @Override
    public String getLongName() {
        String s = getShortName();
        NutsVersion v = getVersion();
        if (v.isBlank()) {
            return s;
        }
        return s + "#" + v;
    }

    @Override
    public String getFullName() {
        return toString();
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public NutsVersion getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!NutsUtilStrings.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(artifactId);
        if (!version.isBlank()) {
            sb.append("#").append(version);
        }
        if (!NutsUtilStrings.isBlank(properties)) {
            sb.append("?");
            sb.append(properties);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultNutsId nutsId = (DefaultNutsId) o;

        if (groupId != null ? !groupId.equals(nutsId.groupId) : nutsId.groupId != null) {
            return false;
        }
        if (artifactId != null ? !artifactId.equals(nutsId.artifactId) : nutsId.artifactId != null) {
            return false;
        }
        if (version != null ? !version.equals(nutsId.version) : nutsId.version != null) {
            return false;
        }
        return properties != null ? properties.equals(nutsId.properties) : nutsId.properties == null;

    }

    @Override
    public int hashCode() {
        int result =  (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

    @Override
    public NutsDependency toDependency() {
        Map<String, String> properties = getProperties();
        //CoreStringUtils.join(",", ex)
        String exc=properties.get(NutsConstants.IdProperties.EXCLUSIONS);
        if(exc==null){
            exc="";
        }
        List<NutsId> a=new ArrayList<>();
        for (String s : exc.split("[;,]")) {
            NutsId n = ws.id().parser().parse(s);
            if(n!=null){
                a.add(n);
            }
        }
        return ws.dependency().builder()
                .setRepository(getRepository())
                .setArtifactId(getArtifactId())
                .setGroupId(getGroupId())
                .setClassifier(getClassifier())
                .setVersion(getVersion())
                .setScope(properties.get(NutsConstants.IdProperties.SCOPE))
                .setOptional(properties.get(NutsConstants.IdProperties.OPTIONAL))
                .setExclusions(a.toArray(new NutsId[0]))
                .setProperties(properties).build()
        ;
    }

    @Override
    public NutsIdBuilder builder() {
        return new DefaultNutsIdBuilder(this,session);
    }

    @Override
    public int compareTo(NutsId o2) {
        int x = this.getShortName().compareTo(o2.getShortName());
        if (x != 0) {
            return x;
        }
        //latest versions first
        x = this.getVersion().compareTo(o2.getVersion());
        return -x;
    }

    @Override
    public NutsIdFilter filter() {
        return new NutsIdIdFilter(this,false,session);
    }

    @Override
    public NutsIdFilter filterCompat() {
        return new NutsIdIdFilter(this,true,session);
    }
}
