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
package net.vpc.app.nuts.runtime;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;

import java.util.Map;
import java.util.function.Function;

import net.vpc.app.nuts.runtime.util.QueryStringMap;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsIdBuilder implements NutsIdBuilder {

    private String namespace;
    private String groupId;
    private String artifactId;
    private NutsVersion version;
    private QueryStringMap propertiesQuery = new QueryStringMap(true, (name, value) -> {
        if (name != null) {
            switch (name) {
                case NutsConstants.IdProperties.VERSION: {
                    setVersion(value);
                    return true;
                }
                case NutsConstants.IdProperties.NAMESPACE: {
                    setNamespace(value);
                    return true;
                }
            }
        }
        return false;
    });

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
        this.version = version == null ? DefaultNutsVersion.EMPTY : version;
        this.propertiesQuery.setProperties(propertiesQuery);
    }

    @Override
    public NutsIdBuilder set(NutsId id) {
        if (id == null) {
            clear();
        } else {
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
        setVersion((NutsVersion) null);
        setProperties("");
        return this;
    }

    @Override
    public NutsIdBuilder set(NutsIdBuilder id) {
        if (id == null) {
            clear();
        } else {
            setNamespace(id.getNamespace());
            setGroupId(id.getGroupId());
            setArtifactId(id.getArtifactId());
            setVersion(id.getVersion());
            setProperties(id.getPropertiesQuery());
        }
        return this;
    }

    @Override
    public NutsIdBuilder setGroupId(String value) {
        this.groupId = CoreStringUtils.trimToNull(value);
        return this;
    }

    @Override
    public NutsIdBuilder setNamespace(String value) {
        this.namespace = CoreStringUtils.trimToNull(value);
        return this;
    }

    @Override
    public NutsIdBuilder setVersion(NutsVersion value) {
        this.version = value == null ? DefaultNutsVersion.EMPTY : value;
        return this;
    }

    @Override
    public NutsIdBuilder setVersion(String value) {
        this.version = DefaultNutsVersion.valueOf(value);
        return this;
    }

    @Override
    public DefaultNutsIdBuilder setArtifactId(String value) {
        this.artifactId = CoreStringUtils.trimToNull(value);
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
        propertiesQuery.setProperty(property, value);
        return this;
    }


    @Override
    public NutsIdBuilder setProperties(Map<String, String> queryMap) {
        propertiesQuery.setProperties(queryMap);
        return this;
    }

    @Override
    public NutsIdBuilder addProperties(Map<String, String> queryMap) {
        propertiesQuery.addProperties(queryMap);
        return this;
    }

    @Override
    public NutsIdBuilder setProperties(String propertiesQuery) {
        this.propertiesQuery.setProperties(propertiesQuery);
        return this;
    }

    @Override
    public NutsIdBuilder addProperties(String propertiesQuery) {
        this.propertiesQuery.addProperties(propertiesQuery);
        return this;
    }

    @Override
    public String getPropertiesQuery() {
        return propertiesQuery.getPropertiesQuery();
    }

    @Override
    public Map<String, String> getProperties() {
        return propertiesQuery.getProperties();
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
        if (!propertiesQuery.isEmpty()) {
            sb.append("?");
            sb.append(propertiesQuery.getPropertiesQuery());
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
                namespace, groupId, artifactId, version == null ? null : version.getValue(), propertiesQuery.getPropertiesQuery()
        );
    }

    @Override
    public NutsIdBuilder groupId(String value) {
        return setGroupId(value);
    }

    @Override
    public NutsIdBuilder namespace(String value) {
        return setNamespace(value);
    }

    @Override
    public NutsIdBuilder version(String value) {
        return setVersion(value);
    }

    @Override
    public NutsIdBuilder version(NutsVersion value) {
        return setVersion(value);
    }

    @Override
    public NutsIdBuilder artifactId(String value) {
        return setArtifactId(value);
    }

    @Override
    public NutsIdBuilder faceContent() {
        return setFaceContent();
    }

    @Override
    public NutsIdBuilder faceDescriptor() {
        return setFaceDescriptor();
    }

    @Override
    public NutsIdBuilder face(String value) {
        return setFace(value);
    }

    @Override
    public NutsIdBuilder classifier(String value) {
        return setClassifier(value);
    }

    @Override
    public NutsIdBuilder platform(String value) {
        return setPlatform(value);
    }

    @Override
    public NutsIdBuilder arch(String value) {
        return setArch(value);
    }

    @Override
    public NutsIdBuilder os(String value) {
        return setOs(value);
    }

    @Override
    public NutsIdBuilder osdist(String value) {
        return setOsdist(value);
    }

    @Override
    public NutsIdBuilder property(String property, String value) {
        return setProperty(property, value);
    }

    @Override
    public NutsIdBuilder packaging(String packaging) {
        return setPackaging(packaging);
    }

    @Override
    public NutsIdBuilder id(NutsId id) {
        return set(id);
    }

    @Override
    public NutsIdBuilder id(NutsIdBuilder id) {
        return set(id);
    }

    @Override
    public NutsIdBuilder properties(Map<String, String> queryMap) {
        return setProperties(queryMap);
    }

    @Override
    public NutsIdBuilder properties(String query) {
        return setProperties(query);
    }
}
