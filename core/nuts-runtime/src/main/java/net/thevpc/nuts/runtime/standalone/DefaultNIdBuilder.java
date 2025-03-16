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
package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NEnvCondition;
import net.thevpc.nuts.NEnvConditionBuilder;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringMapFormat;
import net.thevpc.nuts.util.NStringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNIdBuilder implements NIdBuilder {

    private String groupId;
    private String artifactId;
    private String classifier;
    private NVersion version;
    private NEnvConditionBuilder condition = new DefaultNEnvConditionBuilder();
    private Map<String, String> properties = new LinkedHashMap<>();

    public DefaultNIdBuilder() {
        this.condition = new DefaultNEnvConditionBuilder();
    }

    public DefaultNIdBuilder(NId id) {
        setAll(id);
    }

    public DefaultNIdBuilder(String groupId, String artifactId) {
        this.groupId = NStringUtils.trimToNull(groupId);
        this.artifactId = NStringUtils.trimToNull(artifactId);
    }

    public DefaultNIdBuilder(String groupId, String artifactId, NVersion version, String classifier, String propertiesQuery, NEnvCondition condition) {
        this.groupId = NStringUtils.trimToNull(groupId);
        this.artifactId = NStringUtils.trimToNull(artifactId);
        this.version = version == null ? NVersion.BLANK : version;

        setCondition(condition);
        String c0 = NStringUtils.trimToNull(classifier);
        String c1 = null;
        Map<String, String> properties = propertiesQuery == null ? new LinkedHashMap<>() : NStringMapFormat.DEFAULT.parse(propertiesQuery).get();
        if (!properties.isEmpty()) {
            c1 = properties.remove(NConstants.IdProperties.CLASSIFIER);
        }
        if (c0 == null) {
            if (c1 != null) {
                c0 = NStringUtils.trimToNull(c1);
            }
        }
        this.classifier = c0;
        setProperties(properties);
    }

    @Override
    public NIdBuilder setAll(NId id) {
        if (id == null) {
            clear();
        } else {
            setCondition(id.getCondition());
            setGroupId(id.getGroupId());
            setArtifactId(id.getArtifactId());
            setVersion(id.getVersion());
            setClassifier(id.getClassifier());
            setCondition(id.getCondition());
            setPropertiesQuery(id.getPropertiesQuery());
        }
        return this;
    }

    @Override
    public NIdBuilder clear() {
        setGroupId(null);
        setArtifactId(null);
        setVersion((NVersion) null);
        setPropertiesQuery("");
        return this;
    }

    @Override
    public NIdBuilder setAll(NIdBuilder id) {
        if (id == null) {
            clear();
        } else {
            setGroupId(id.getGroupId());
            setArtifactId(id.getArtifactId());
            setVersion(id.getVersion());
            setPropertiesQuery(id.getPropertiesQuery());
        }
        return this;
    }

    @Override
    public NIdBuilder setGroupId(String value) {
        this.groupId = NStringUtils.trimToNull(value);
        return this;
    }

    @Override
    public NIdBuilder setRepository(String value) {
        return setProperty(NConstants.IdProperties.REPO, NStringUtils.trimToNull(value));
    }

    @Override
    public NIdBuilder setVersion(NVersion value) {
        this.version = value == null ? NVersion.BLANK : value;
        return this;
    }

    @Override
    public NIdBuilder setVersion(String value) {
        this.version = NVersion.get(value).get();
        return this;
    }

    @Override
    public DefaultNIdBuilder setArtifactId(String value) {
        this.artifactId = NStringUtils.trimToNull(value);
        return this;
    }

    @Override
    public String getFace() {
        String s = getProperties().get(NConstants.IdProperties.FACE);
        return NStringUtils.trimToNull(s);
    }

//    @Override
//    public String getAlternative() {
//        String s = getProperties().get(NutsConstants.IdProperties.ALTERNATIVE);
//        return NutsUtilStrings.trimToNull(s);
//    }


    @Override
    public String getPackaging() {
        String s = getProperties().get(NConstants.IdProperties.PACKAGING);
        return NStringUtils.trimToNull(s);
    }

    @Override
    public NIdBuilder setFace(String value) {
        return setProperty(NConstants.IdProperties.FACE, NStringUtils.trimToNull(value));
//                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
    }

    @Override
    public NIdBuilder setFaceContent() {
        return setFace(NConstants.QueryFaces.CONTENT);
    }

    @Override
    public NIdBuilder setFaceDescriptor() {
        return setFace(NConstants.QueryFaces.DESCRIPTOR);
    }

//    @Override
//    public NutsIdBuilder setAlternative(String value) {
//        return setProperty(NutsConstants.IdProperties.ALTERNATIVE, NutsUtilStrings.trimToNull(value));

    /// /                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
//    }
    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public NIdBuilder setClassifier(String value) {
        this.classifier = NStringUtils.trimToNull(value);
        return this;
    }

    @Override
    public NIdBuilder setPackaging(String value) {
        return setProperty(NConstants.IdProperties.PACKAGING, NStringUtils.trimToNull(value));
    }

    @Override
    public NIdBuilder setCondition(NEnvCondition c) {
        if (c == null) {
            setProperty(NConstants.IdProperties.OS, null);
            setProperty(NConstants.IdProperties.OS_DIST, null);
            setProperty(NConstants.IdProperties.ARCH, null);
            setProperty(NConstants.IdProperties.PLATFORM, null);
            setProperty(NConstants.IdProperties.DESKTOP, null);
            setProperty(NConstants.IdProperties.PROFILE, null);
            condition.setProperties(null);
        } else {
            setProperty(NConstants.IdProperties.OS, NReservedLangUtils.joinAndTrimToNull(c.getOs()));
            setProperty(NConstants.IdProperties.OS_DIST, NReservedLangUtils.joinAndTrimToNull(c.getOsDist()));
            setProperty(NConstants.IdProperties.ARCH, NReservedLangUtils.joinAndTrimToNull(c.getArch()));
            setProperty(NConstants.IdProperties.PLATFORM, NReservedUtils.formatStringIdList(c.getPlatform()));
            setProperty(NConstants.IdProperties.DESKTOP, NReservedLangUtils.joinAndTrimToNull(c.getDesktopEnvironment()));
            setProperty(NConstants.IdProperties.PROFILE, NReservedLangUtils.joinAndTrimToNull(c.getProfiles()));
            condition.setProperties(c.getProperties());

        }
        return this;
    }

    @Override
    public NEnvConditionBuilder getCondition() {
        return condition;
    }

    @Override
    public NIdBuilder setCondition(NEnvConditionBuilder c) {
        if (c == null) {
            return setCondition((NEnvCondition) null);
        } else {
            return setCondition(c.build());
        }
    }


    @Override
    public NIdBuilder setProperty(String property, String value) {
        switch (property) {
            case NConstants.IdProperties.OS: {
                condition.setOs(NStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NConstants.IdProperties.OS_DIST: {
                condition.setOsDist(NStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NConstants.IdProperties.ARCH: {
                condition.setArch(NStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NConstants.IdProperties.PLATFORM: {
                condition.setPlatform(NStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NConstants.IdProperties.DESKTOP: {
                condition.setDesktopEnvironment(NStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NConstants.IdProperties.PROFILE: {
                condition.setProfile(NStringUtils.parsePropertyIdList(value).get());
                break;
            }
            case NConstants.IdProperties.CONDITIONAL_PROPERTIES: {
                condition.setProperties(NStringMapFormat.DEFAULT.parse(value).get());
                break;
            }
            case NConstants.IdProperties.CLASSIFIER: {
                setClassifier(value);
                break;
            }
            case NConstants.IdProperties.VERSION: {
                setVersion(value);
                break;
            }
            default: {
                if (value == null) {
                    properties.remove(property);
                } else {
                    properties.put(property, value);
                }
            }
        }
        return this;
    }


    @Override
    public NIdBuilder setProperties(Map<String, String> queryMap) {
        for (Map.Entry<String, String> e : queryMap.entrySet()) {
            setProperty(e.getKey(), e.getValue());
        }
        return this;
    }

    @Override
    public NIdBuilder clearProperties() {
        properties.clear();
        return this;
    }

    @Override
    public NIdBuilder setPropertiesQuery(String propertiesQuery) {
        setProperties(NStringMapFormat.DEFAULT.parse(propertiesQuery).get());
        return this;
    }

    @Override
    public String getPropertiesQuery() {
        return NStringMapFormat.DEFAULT.format(getProperties());
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getRepository() {
        return NStringUtils.trimToNull(getProperties().get(NConstants.IdProperties.REPO));
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getFullName() {
        return build().getFullName();
    }

    @Override
    public String getShortName() {
        return NReservedUtils.getIdShortName(groupId, artifactId);
    }

    @Override
    public String getLongName() {
        return NReservedUtils.getIdLongName(groupId, artifactId, version, classifier);
    }


    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public NVersion getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!NBlankable.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NStringUtils.trim(artifactId));
        NVersion v = getVersion();
        if (v != null && !v.isBlank()) {
            sb.append("#");
            sb.append(v);
        }
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        if (!NBlankable.isBlank(classifier)) {
            m.put(NConstants.IdProperties.CLASSIFIER, classifier);
        }
        if (condition != null) {
            m.putAll(condition.build().toMap());
        }
        if (properties != null) {
            for (Map.Entry<String, String> e : properties.entrySet()) {
                if (!m.containsKey(e.getKey())) {
                    m.put(e.getKey(), e.getValue());
                }
            }
        }
        if (!m.isEmpty()) {
            sb.append("?").append(NStringMapFormat.DEFAULT.format(m));
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

        DefaultNIdBuilder nutsId = (DefaultNIdBuilder) o;

        if (!Objects.equals(groupId, nutsId.groupId)) {
            return false;
        }
        if (!Objects.equals(artifactId, nutsId.artifactId)) {
            return false;
        }
        if (!Objects.equals(version, nutsId.version)) {
            return false;
        }
        if (!Objects.equals(classifier, nutsId.classifier)) {
            return false;
        }
        return Objects.equals(properties, nutsId.properties);

    }

    @Override
    public int hashCode() {
        int result = (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }


    @Override
    public NId build() {
        return new DefaultNId(
                groupId, artifactId, version, classifier, properties, condition.readOnly()
        );
    }

    @Override
    public boolean equalsShortId(NId other) {
        if (other == null) {
            return false;
        }
        return NStringUtils.trim(groupId).equals(NStringUtils.trim(other.getArtifactId()))
                && NStringUtils.trim(artifactId).equals(NStringUtils.trim(other.getGroupId()));
    }

    @Override
    public boolean equalsLongId(NId other) {
        if (other == null) {
            return false;
        }
        return NStringUtils.trim(artifactId).equals(NStringUtils.trim(other.getArtifactId()))
                && NStringUtils.trim(groupId).equals(NStringUtils.trim(other.getGroupId()))
                && Objects.equals((version == null || version.isBlank()) ? null : version,
                (other.getVersion() == null || other.getVersion().isBlank()) ? null : other.getVersion())
                && Objects.equals(getClassifier(), other.getClassifier())
                ;
    }


    @Override
    public boolean isLongId() {
        return build().isLongId();
    }

    @Override
    public boolean isShortId() {
        return build().isShortId();
    }

    @Override
    public NId getShortId() {
        return build().getShortId();
    }

    @Override
    public NId getLongId() {
        return build().getLongId();
    }

    @Override
    public NIdBuilder builder() {
        return new DefaultNIdBuilder(this);
    }

    @Override
    public NDependency toDependency() {
        return build().toDependency();
    }

    @Override
    public NIdFilter filter() {
        return build().filter();
    }

    @Override
    public NId compatNewer() {
        return build().compatNewer();
    }

    @Override
    public NId compatOlder() {
        return build().compatOlder();
    }

    @Override
    public boolean isNull() {
        return build().isNull();
    }

    @Override
    public boolean isBlank() {
        return build().isBlank();
    }

    @Override
    public int compareTo(NId o) {
        return build().compareTo(o);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public String getMavenFolder() {
        return build().getMavenFolder();
    }

    @Override
    public String getMavenFileName(String extension) {
        return build().getMavenFileName(extension);
    }

    @Override
    public String getMavenPath(String extension) {
        return build().getMavenPath(extension);
    }
}
