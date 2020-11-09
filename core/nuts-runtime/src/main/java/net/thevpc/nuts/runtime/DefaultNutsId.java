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
package net.thevpc.nuts.runtime;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.config.DefaultNutsDependency;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.util.QueryStringMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsId implements NutsId {
    public static final long serialVersionUID = 1L;
    private final String namespace;
    private final String groupId;
    private final String artifactId;
    private final NutsVersion version;
    private final String properties;

    public DefaultNutsId(String namespace, String groupId, String artifactId, String version, Map<String, String> properties) {
        this(namespace, groupId, artifactId, DefaultNutsVersion.valueOf(version), properties);
    }

    protected DefaultNutsId(String namespace, String groupId, String artifactId, NutsVersion version, Map<String, String> properties) {
        this.namespace = CoreStringUtils.trimToNull(namespace);
        this.groupId = CoreStringUtils.trimToNull(groupId);
        this.artifactId = CoreStringUtils.trimToNull(artifactId);
        this.version = version == null ? DefaultNutsVersion.EMPTY : version;
        this.properties = QueryStringMap.formatSortedPropertiesQuery(properties);
    }

    protected DefaultNutsId(String namespace, String groupId, String artifactId, NutsVersion version, String properties) {
        this.namespace = CoreStringUtils.trimToNull(namespace);
        this.groupId = CoreStringUtils.trimToNull(groupId);
        this.artifactId = CoreStringUtils.trimToNull(artifactId);
        this.version = version == null ? DefaultNutsVersion.EMPTY : version;
        this.properties = QueryStringMap.formatSortedPropertiesQuery(properties);
    }

    public DefaultNutsId(String groupId, String artifactId, String version) {
        this(null, groupId, artifactId, version, (String) null);
    }

    public DefaultNutsId(String namespace, String groupId, String artifactId, String version, String properties) {
        this.namespace = CoreStringUtils.trimToNull(namespace);
        this.groupId = CoreStringUtils.trimToNull(groupId);
        this.artifactId = CoreStringUtils.trimToNull(artifactId);
        this.version = DefaultNutsVersion.valueOf(version);
        this.properties = QueryStringMap.formatSortedPropertiesQuery(properties);
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
    public NutsTokenFilter namespaceToken() {
        return new DefaultNutsTokenFilter(getNamespace());
    }

    @Override
    public NutsTokenFilter anyToken() {
        NutsTokenFilter[] oo = {groupIdToken(), propertiesToken(), versionToken(), artifactIdToken(), namespaceToken()};
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
        return CoreStringUtils.trim(artifactId).equals(CoreStringUtils.trim(other.getArtifactId()))
                && CoreStringUtils.trim(groupId).equals(CoreStringUtils.trim(other.getGroupId()));
    }

    @Override
    public boolean like(String pattern) {
        if (pattern == null) {
            return true;
        }
        return toString().matches(CoreStringUtils.simpexpToRegexp(pattern));
    }

    @Override
    public String getFace() {
        String s = getProperties().get(NutsConstants.IdProperties.FACE);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public String getClassifier() {
        String s = getProperties().get(NutsConstants.IdProperties.CLASSIFIER);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public String getOs() {
        String s = getProperties().get(NutsConstants.IdProperties.OS);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public String getOsdist() {
        String s = getProperties().get(NutsConstants.IdProperties.OSDIST);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public String getPlatform() {
        String s = getProperties().get(NutsConstants.IdProperties.PLATFORM);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public String getArch() {
        String s = getProperties().get(NutsConstants.IdProperties.ARCH);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public String getPropertiesQuery() {
        return properties;
    }

    @Override
    public Map<String, String> getProperties() {
        return QueryStringMap.parseMap(properties);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public NutsId getShortNameId() {
        return new DefaultNutsId(null, groupId, artifactId, (NutsVersion) null, "");
    }

    @Override
    public NutsId getLongNameId() {
        return new DefaultNutsId(null, groupId, artifactId, version, "");
    }

    @Override
    public String getShortName() {
        if (CoreStringUtils.isBlank(groupId)) {
            return CoreStringUtils.trim(artifactId);
        }
        return CoreStringUtils.trim(groupId) + ":" + CoreStringUtils.trim(artifactId);
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
        if (!CoreStringUtils.isBlank(namespace)) {
            sb.append(namespace).append("://");
        }
        if (!CoreStringUtils.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(artifactId);
        if (!version.isBlank()) {
            sb.append("#").append(version);
        }
        if (!CoreStringUtils.isBlank(properties)) {
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

        if (namespace != null ? !namespace.equals(nutsId.namespace) : nutsId.namespace != null) {
            return false;
        }
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
        int result = namespace != null ? namespace.hashCode() : 0;
        result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
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
        for (String s : exc.split(";")) {
            NutsId n = CoreNutsUtils.parseNutsId(s);
            if(n!=null){
                a.add(n);
            }
        }
        return new DefaultNutsDependency(
                getNamespace(),getGroupId(),getGroupId(),getClassifier(),getVersion(),
                properties.get(NutsConstants.IdProperties.SCOPE),
                properties.get(NutsConstants.IdProperties.OPTIONAL),
                a.toArray(new NutsId[0]),
                properties
        );
    }

    @Override
    public NutsIdBuilder builder() {
        return new DefaultNutsIdBuilder(this);
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
}
