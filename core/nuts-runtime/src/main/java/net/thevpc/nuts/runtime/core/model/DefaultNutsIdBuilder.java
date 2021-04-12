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
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.bundles.parsers.QueryStringParser;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.Map;
import java.util.function.Function;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsIdBuilder implements NutsIdBuilder {

    private transient NutsSession session;
    private String namespace;
    private String groupId;
    private String artifactId;
    private NutsVersion version;
    private transient QueryStringParser propertiesQuery = new QueryStringParser(true, (name, value) -> {
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

    public DefaultNutsIdBuilder(NutsSession session) {
        this.session=session;
    }

    public DefaultNutsIdBuilder(NutsId id,NutsSession session) {
        this.session=session;
        setNamespace(id.getNamespace());
        setGroupId(id.getGroupId());
        setArtifactId(id.getArtifactId());
        setVersion(id.getVersion());
        setProperties(id.getPropertiesQuery());
    }

    public DefaultNutsIdBuilder(String namespace, String groupId, String artifactId, NutsVersion version, String propertiesQuery,NutsSession session) {
        this.session=session;
        this.namespace = CoreStringUtils.trimToNull(namespace);
        this.groupId = CoreStringUtils.trimToNull(groupId);
        this.artifactId = CoreStringUtils.trimToNull(artifactId);
        this.version = version == null ? session.getWorkspace().version().parser().parse("") : version;
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
        this.version = value == null ? session.getWorkspace().version().parser().parse("") : value;
        return this;
    }

    @Override
    public NutsIdBuilder setVersion(String value) {
        this.version = session.getWorkspace().version().parser().parse(value);
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
    public String getPackaging() {
        String s = getProperties().get(NutsConstants.IdProperties.PACKAGING);
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
    public NutsIdBuilder omitImportedGroupId() {
        String g = getGroupId();
        if(g!=null && g.length()>0){
            if(session.getWorkspace().imports().isImportedGroupId(g)){
                setGroupId(null);
            }
        }
        return this;
    }

    @Override
    public NutsId build() {
        return new DefaultNutsId(
                namespace, groupId, artifactId, version == null ? null : version.getValue(), propertiesQuery.getPropertiesQuery(),session
        );
    }

}
