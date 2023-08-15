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
package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NReservedStringUtils;
import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringMapFormat;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;


/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNId implements NId {
    public static final long serialVersionUID = 1L;
    private final String groupId;
    private final String artifactId;
    private final String classifier;
    private final NVersion version;
    private final String properties;
    private final NEnvCondition condition;

    public DefaultNId() {
        this.groupId = null;
        this.artifactId = null;
        this.version = NVersion.BLANK;
        this.classifier = null;
        this.condition = NEnvCondition.BLANK;
        this.properties = "";
    }

    public DefaultNId(String groupId, String artifactId, NVersion version) {
        this.groupId = NStringUtils.trimToNull(groupId);
        this.artifactId = NStringUtils.trimToNull(artifactId);
        this.version = version == null ? NVersion.BLANK : version;
        this.classifier = null;
        this.condition = NEnvCondition.BLANK;
        this.properties = "";
    }

    public DefaultNId(String groupId, String artifactId, NVersion version, String classifier, Map<String, String> properties, NEnvCondition condition) {
        this.groupId = NStringUtils.trimToNull(groupId);
        this.artifactId = NStringUtils.trimToNull(artifactId);
        this.version = version == null ? NVersion.BLANK : version;
        String c0 = NStringUtils.trimToNull(classifier);
        String c1 = null;
        if (properties != null) {
            c1 = properties.remove(NConstants.IdProperties.CLASSIFIER);
        }
        if (c0 == null) {
            if (c1 != null) {
                c0 = NStringUtils.trimToNull(c1);
            }
        }
        this.classifier = c0;
        this.condition = condition == null ? NEnvCondition.BLANK : condition.readOnly();
        this.properties = NStringUtils.trim(NStringMapFormat.DEFAULT.format(properties));
    }

    public DefaultNId(String groupId, String artifactId, NVersion version, String classifier, String properties, NEnvCondition condition) {
        this(groupId, artifactId, version, classifier, NStringMapFormat.DEFAULT.parse(properties).get(), condition);
    }

    @Override
    public NFormat formatter(NSession session) {
        return NIdFormat.of(session).setValue(this);
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
    public boolean equalsShortId(NId other) {
        if (other == null) {
            return false;
        }
        return NStringUtils.trim(groupId).equals(NStringUtils.trim(other.getGroupId()))
                && NStringUtils.trim(artifactId).equals(NStringUtils.trim(other.getArtifactId()));
    }

    @Override
    public boolean isLongId() {
        if (NBlankable.isBlank(properties)) {
            return true;
        }
        Map<String, String> m = new HashMap<>(getProperties());
        m.remove(NConstants.IdProperties.CLASSIFIER);
        return m.isEmpty();
    }

    @Override
    public boolean isShortId() {
        return NBlankable.isBlank(properties)
                && NBlankable.isBlank(version)
                && NBlankable.isBlank(classifier)
                ;
    }

    @Override
    public boolean equalsLongId(NId other) {
        if (other == null) {
            return false;
        }
        return NStringUtils.trim(groupId).equals(NStringUtils.trim(other.getGroupId()))
                && NStringUtils.trim(artifactId).equals(NStringUtils.trim(other.getArtifactId()))
                && Objects.equals((version == null || version.isBlank()) ? null : version,
                (other.getVersion() == null || other.getVersion().isBlank()) ? null : other.getVersion())
                && Objects.equals(getClassifier(), other.getClassifier())
                ;
    }

    @Override
    public String getFace() {
        String s = getProperties().get(NConstants.IdProperties.FACE);
        return NStringUtils.trimToNull(s);
    }

    @Override
    public String getClassifier() {
        return NStringUtils.trimToNull(classifier);
    }

    @Override
    public String getPackaging() {
        String s = getProperties().get(NConstants.IdProperties.PACKAGING);
        return NStringUtils.trimToNull(s);
    }

    @Override
    public NEnvCondition getCondition() {
        return condition;
    }

    @Override
    public String getPropertiesQuery() {
        return properties;
    }

    @Override
    public Map<String, String> getProperties() {
        return NStringMapFormat.DEFAULT.parse(properties).get();
    }

    @Override
    public String getRepository() {
        String s = getProperties().get(NConstants.IdProperties.REPO);
        return NStringUtils.trimToNull(s);
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public NId getShortId() {
        return new DefaultNId(groupId, artifactId, (NVersion) null, null, "",
                NEnvCondition.BLANK);
    }

    @Override
    public NId getLongId() {
        return new DefaultNId(groupId, artifactId, version, classifier, "", NEnvCondition.BLANK);
    }

    @Override
    public String getShortName() {
        if (NBlankable.isBlank(groupId)) {
            return NStringUtils.trim(artifactId);
        }
        return NStringUtils.trim(groupId) + ":" + NStringUtils.trim(artifactId);
    }

    @Override
    public String getLongName() {
        StringBuilder sb = new StringBuilder();
        if (!NBlankable.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NStringUtils.trim(artifactId));
        NVersion v = getVersion();
        if (!v.isBlank()) {
            sb.append("#");
            sb.append(v);
        }
        if (!NBlankable.isBlank(classifier)) {
            sb.append("?");
            sb.append("classifier=");
            sb.append(classifier);
        }
        return sb.toString();
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
        if (!v.isBlank()) {
            sb.append("#");
            sb.append(v);
        }
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        if (!NBlankable.isBlank(classifier)) {
            m.put(NConstants.IdProperties.CLASSIFIER, classifier);
        }
        m.putAll(NReservedUtils.toMap(condition));
        for (Map.Entry<String, String> e : NStringMapFormat.DEFAULT.parse(properties).get().entrySet()) {
            if (!m.containsKey(e.getKey())) {
                m.put(e.getKey(), e.getValue());
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

        DefaultNId nutsId = (DefaultNId) o;

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
    public NDependency toDependency() {
        Map<String, String> properties = getProperties();
        //CoreStringUtils.join(",", ex)
        String exc = properties.get(NConstants.IdProperties.EXCLUSIONS);
        if (exc == null) {
            exc = "";
        }
        List<NId> a = new ArrayList<>();
        for (String s : NReservedStringUtils.splitDefault(exc)) {
            NId n = NId.of(s).get();
            if (n != null) {
                a.add(n);
            }
        }
        return new DefaultNDependencyBuilder()
                .setRepository(getRepository())
                .setArtifactId(getArtifactId())
                .setGroupId(getGroupId())
                .setClassifier(getClassifier())
                .setVersion(getVersion())
                .setScope(properties.get(NConstants.IdProperties.SCOPE))
                .setOptional(properties.get(NConstants.IdProperties.OPTIONAL))
                .setExclusions(a)
                .setCondition(getCondition())
                .setProperties(properties)
                .build()
                ;
    }

    @Override
    public NIdBuilder builder() {
        return new DefaultNIdBuilder(this);
    }

    @Override
    public int compareTo(NId o2) {
        int x;
        x = NStringUtils.trim(this.getGroupId()).compareTo(NStringUtils.trim(o2.getGroupId()));
        if (x != 0) {
            return x;
        }
        x = NStringUtils.trim(this.getArtifactId()).compareTo(NStringUtils.trim(o2.getArtifactId()));
        if (x != 0) {
            return x;
        }
        x = NStringUtils.trim(this.getClassifier()).compareTo(NStringUtils.trim(o2.getClassifier()));
        if (x != 0) {
            return x;
        }
        x = this.getVersion().compareTo(o2.getVersion());
        if (x != 0) {
            return x;
        }
        return 0;
    }

    @Override
    public NIdFilter filter(NSession session) {
        return NIdFilters.of(session).byValue(this);
    }

    @Override
    public NId compatNewer() {
        return builder().setVersion(getVersion().compatNewer()).build();
    }

    @Override
    public NId compatOlder() {
        return builder().setVersion(getVersion().compatOlder()).build();
    }
}
