/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsIdBuilder implements NutsIdBuilder {

    private String namespace;
    private String groupId;
    private String artifactId;
    private NutsVersion version;
    private String propertiesQuery;

    public DefaultNutsIdBuilder() {
    }

    public DefaultNutsIdBuilder(NutsId id) {
        setNamespace(id.getNamespace());
        setGroupId(id.getGroupId());
        setArtifactId(id.getArtifactId());
        setVersion(id.getVersion());
        setProperties(id.getPropertiesQuery());
    }

    public DefaultNutsIdBuilder(String namespace, String groupId, String artifactId, NutsVersion version, String propertiesQuery) {
        this.namespace = CoreStringUtils.trimToNull(namespace);
        this.groupId = CoreStringUtils.trimToNull(groupId);
        this.artifactId = CoreStringUtils.trimToNull(artifactId);
        this.version = version==null?DefaultNutsVersion.EMPTY:version;
        this.propertiesQuery = CoreStringUtils.trimToNull(propertiesQuery);
    }

    @Override
    public NutsIdBuilder set(NutsId id) {
        if(id==null) {
            clear();
        }else{
            setNamespace(id.getNamespace());
            setGroupId(id.getGroupId());
            setArtifactId(id.getArtifactId());
            setVersion(id.getVersion());
            setProperties(id.getPropertiesQuery());
        }
        return this;
    }

    @Override
    public NutsIdBuilder clear() {
        setNamespace(null);
        setGroupId(null);
        setArtifactId(null);
        setVersion((NutsVersion)null);
        setProperties("");
        return this;
    }

    @Override
    public NutsIdBuilder set(NutsIdBuilder id) {
        if(id==null) {
            clear();
        }else{
            setNamespace(id.getNamespace());
            setGroupId(id.getGroupId());
            setArtifactId(id.getArtifactId());
            setVersion(id.getVersion());
            setProperties(id.getPropertiesQuery());
        }
        return this;
    }

    @Override
    public NutsIdBuilder setGroupId(String group) {
        this.groupId = CoreStringUtils.trimToNull(group);
        return this;
    }

    @Override
    public NutsIdBuilder setNamespace(String namespace) {
        this.namespace = CoreStringUtils.trimToNull(namespace);
        return this;
    }

    @Override
    public NutsIdBuilder setVersion(NutsVersion version) {
        this.version = version==null?DefaultNutsVersion.EMPTY:version;
        return this;
    }

    @Override
    public NutsIdBuilder setVersion(String version) {
        this.version = DefaultNutsVersion.valueOf(version);
        return this;
    }

    @Override
    public DefaultNutsIdBuilder setArtifactId(String name) {
        this.artifactId = CoreStringUtils.trimToNull(name);
        return this;
    }

    @Override
    public String getFace() {
        String s = getProperties().get(NutsConstants.IdProperties.FACE);
        return CoreStringUtils.trimToNull(s);
    }

//    @Override
//    public String getAlternative() {
//        String s = getProperties().get(NutsConstants.IdProperties.ALTERNATIVE);
//        return CoreStringUtils.trimToNull(s);
//    }

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
    public NutsIdBuilder setFace(String value) {
        return setProperty(NutsConstants.IdProperties.FACE, CoreStringUtils.trimToNull(value));
//                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
    }

    @Override
    public NutsIdBuilder setFaceContent() {
        return setFace(NutsConstants.QueryFaces.CONTENT);
    }

    @Override
    public NutsIdBuilder setFaceDescriptor() {
        return setFace(NutsConstants.QueryFaces.DESCRIPTOR);
    }

//    @Override
//    public NutsIdBuilder setAlternative(String value) {
//        return setProperty(NutsConstants.IdProperties.ALTERNATIVE, CoreStringUtils.trimToNull(value));
////                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
//    }

    @Override
    public String getClassifier() {
        String s = getProperties().get(NutsConstants.IdProperties.CLASSIFIER);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public NutsIdBuilder setClassifier(String value) {
        return setProperty(NutsConstants.IdProperties.CLASSIFIER, CoreStringUtils.trimToNull(value));
//                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
    }

    @Override
    public NutsIdBuilder setScope(String value) {
        return setProperty(NutsConstants.IdProperties.SCOPE, CoreStringUtils.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setOptional(String value) {
        return setProperty(NutsConstants.IdProperties.OPTIONAL, CoreStringUtils.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setPackaging(String value) {
        return setProperty(NutsConstants.IdProperties.PACKAGING, CoreStringUtils.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setPlatform(String value) {
        return setProperty(NutsConstants.IdProperties.PLATFORM, CoreStringUtils.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setArch(String value) {
        return setProperty(NutsConstants.IdProperties.ARCH, CoreStringUtils.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setOs(String value) {
        return setProperty(NutsConstants.IdProperties.OSDIST, CoreStringUtils.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setOsdist(String value) {
        return setProperty(NutsConstants.IdProperties.OS, CoreStringUtils.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setProperty(String property, String value) {
        Map<String, String> m = getProperties();
        if (value == null) {
            m.remove(property);
        } else {
            m.put(property, value);
        }
        return setProperties(m);
    }

    private NutsIdBuilder _setProperties(Map<String, String> queryMap, boolean merge) {
        Map<String, String> m = null;
        if (merge) {
            m = getProperties();
            if (queryMap != null) {
                for (Map.Entry<String, String> e : queryMap.entrySet()) {
                    String property = e.getKey();
                    String value = e.getValue();
                    if (value == null) {
                        m.remove(property);
                    } else {
                        m.put(property, value);
                    }
                }
            }
        } else {
            m = new HashMap<>();
            if (queryMap != null) {
                m.putAll(queryMap);
            }
        }
        this.propertiesQuery = DefaultNutsId.formatPropertiesQuery(m);
        return this;
    }

    @Override
    public NutsIdBuilder setProperties(Map<String, String> queryMap) {
        return _setProperties(queryMap, false);
    }

    @Override
    public NutsIdBuilder addProperties(Map<String, String> queryMap) {
        return _setProperties(queryMap, true);
    }

    @Override
    public NutsIdBuilder setProperties(String propertiesQuery) {
        this.propertiesQuery=CoreStringUtils.trimToNull(propertiesQuery);
        return this;
    }

    @Override
    public NutsIdBuilder addProperties(String propertiesQuery) {
        Map<String, String> m1 = getProperties();
        Map<String, String> m2 = CoreStringUtils.parseMap(propertiesQuery, "&");
        m1.putAll(m2);
        this.propertiesQuery=DefaultNutsId.formatPropertiesQuery(m1);
        return this;
    }

    @Override
    public String getPropertiesQuery() {
        return propertiesQuery;
    }

    @Override
    public Map<String, String> getProperties() {
        return CoreStringUtils.parseMap(getPropertiesQuery(), "&");
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
    public String getFullName() {
        if (CoreStringUtils.isBlank(groupId)) {
            return CoreStringUtils.trim(artifactId);
        }
        return CoreStringUtils.trim(groupId) + ":" + CoreStringUtils.trim(artifactId);
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
        if (!CoreStringUtils.isBlank(version.getValue())) {
            sb.append("#").append(version);
        }
        if (!CoreStringUtils.isBlank(propertiesQuery)) {
            sb.append("?");
            sb.append(propertiesQuery);
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

        DefaultNutsIdBuilder nutsId = (DefaultNutsIdBuilder) o;

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
        return propertiesQuery != null ? propertiesQuery.equals(nutsId.propertiesQuery) : nutsId.propertiesQuery == null;

    }

    @Override
    public int hashCode() {
        int result = namespace != null ? namespace.hashCode() : 0;
        result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (propertiesQuery != null ? propertiesQuery.hashCode() : 0);
        return result;
    }

    @Override
    public NutsIdBuilder apply(Function<String, String> properties) {
        setNamespace(CoreNutsUtils.applyStringProperties(this.getNamespace(), properties));
        setGroupId(CoreNutsUtils.applyStringProperties(this.getGroupId(), properties));
        setArtifactId(CoreNutsUtils.applyStringProperties(this.getArtifactId(), properties));
        setVersion(CoreNutsUtils.applyStringProperties(this.getVersion().getValue(), properties));
        setProperties(CoreNutsUtils.applyMapProperties(this.getProperties(), properties));
        return this;
    }

    @Override
    public NutsId build() {
        return new DefaultNutsId(
                namespace, groupId, artifactId, version == null ? null : version.getValue(), propertiesQuery
        );
    }
}
