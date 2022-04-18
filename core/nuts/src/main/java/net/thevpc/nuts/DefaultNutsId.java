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

import net.thevpc.nuts.boot.PrivateNutsUtilMaps;
import net.thevpc.nuts.boot.PrivateNutsQueryStringParser;
import net.thevpc.nuts.boot.PrivateNutsUtilStrings;

import java.util.*;


/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsId implements NutsId {
    public static final long serialVersionUID = 1L;
    private final String groupId;
    private final String artifactId;
    private final String classifier;
    private final NutsVersion version;
    private final String properties;
    private final NutsEnvCondition condition;

    public DefaultNutsId() {
        this.groupId = null;
        this.artifactId = null;
        this.version = NutsVersion.BLANK;
        this.classifier = null;
        this.condition = NutsEnvCondition.BLANK;
        this.properties = "";
    }

    public DefaultNutsId(String groupId, String artifactId, NutsVersion version) {
        this.groupId = NutsUtilStrings.trimToNull(groupId);
        this.artifactId = NutsUtilStrings.trimToNull(artifactId);
        this.version = version == null ? NutsVersion.BLANK : version;
        this.classifier = null;
        this.condition = NutsEnvCondition.BLANK;
        this.properties = "";
    }

    public DefaultNutsId(String groupId, String artifactId, NutsVersion version, String classifier, Map<String, String> properties, NutsEnvCondition condition) {
        this.groupId = NutsUtilStrings.trimToNull(groupId);
        this.artifactId = NutsUtilStrings.trimToNull(artifactId);
        this.version = version == null ? NutsVersion.BLANK : version;
        String c0 = NutsUtilStrings.trimToNull(classifier);
        String c1 = null;
        if (properties != null) {
            c1 = properties.remove(NutsConstants.IdProperties.CLASSIFIER);
        }
        if (c0 == null) {
            if (c1 != null) {
                c0 = NutsUtilStrings.trimToNull(c1);
            }
        }
        this.classifier = c0;
        this.condition = condition == null ? NutsEnvCondition.BLANK : condition;
        this.properties = PrivateNutsQueryStringParser.formatSortedPropertiesQuery(properties);
    }

    public DefaultNutsId(String groupId, String artifactId, NutsVersion version, String classifier, String properties, NutsEnvCondition condition) {
        this(groupId, artifactId, version, classifier, new PrivateNutsQueryStringParser(true, null).setProperties(properties).getProperties(), condition);
    }

    @Override
    public NutsFormat formatter(NutsSession session) {
        return NutsIdFormat.of(session).setValue(this);
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
    public boolean equalsShortId(NutsId other) {
        if (other == null) {
            return false;
        }
        return NutsUtilStrings.trim(artifactId).equals(NutsUtilStrings.trim(other.getArtifactId()))
                && NutsUtilStrings.trim(groupId).equals(NutsUtilStrings.trim(other.getGroupId()));
    }

    @Override
    public boolean isLongId() {
        if (NutsBlankable.isBlank(properties)) {
            return true;
        }
        Map<String, String> m = new HashMap<>(getProperties());
        m.remove(NutsConstants.IdProperties.CLASSIFIER);
        return m.isEmpty();
    }

    @Override
    public boolean isShortId() {
        return NutsBlankable.isBlank(properties)
                && NutsBlankable.isBlank(version)
                && NutsBlankable.isBlank(classifier)
                ;
    }

    @Override
    public boolean equalsLongId(NutsId other) {
        if (other == null) {
            return false;
        }
        return NutsUtilStrings.trim(artifactId).equals(NutsUtilStrings.trim(other.getArtifactId()))
                && NutsUtilStrings.trim(groupId).equals(NutsUtilStrings.trim(other.getGroupId()))
                && Objects.equals((version == null || version.isBlank()) ? null : version,
                (other.getVersion() == null || other.getVersion().isBlank()) ? null : other.getVersion())
                && Objects.equals(getClassifier(), other.getClassifier())
                ;
    }

    @Override
    public String getFace() {
        String s = getProperties().get(NutsConstants.IdProperties.FACE);
        return NutsUtilStrings.trimToNull(s);
    }

    @Override
    public String getClassifier() {
        return NutsUtilStrings.trimToNull(classifier);
    }

    @Override
    public String getPackaging() {
        String s = getProperties().get(NutsConstants.IdProperties.PACKAGING);
        return NutsUtilStrings.trimToNull(s);
    }

    @Override
    public NutsEnvCondition getCondition() {
        return condition;
    }

    @Override
    public String getPropertiesQuery() {
        return properties;
    }

    @Override
    public Map<String, String> getProperties() {
        return PrivateNutsQueryStringParser.parseMap(properties);
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
    public NutsId getShortId() {
        return new DefaultNutsId(groupId, artifactId, (NutsVersion) null, null, "",
                NutsEnvCondition.BLANK);
    }

    @Override
    public NutsId getLongId() {
        return new DefaultNutsId(groupId, artifactId, version, classifier, "", NutsEnvCondition.BLANK);
    }

    @Override
    public String getShortName() {
        if (NutsBlankable.isBlank(groupId)) {
            return NutsUtilStrings.trim(artifactId);
        }
        return NutsUtilStrings.trim(groupId) + ":" + NutsUtilStrings.trim(artifactId);
    }

    @Override
    public String getLongName() {
        StringBuilder sb = new StringBuilder();
        if (!NutsBlankable.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NutsUtilStrings.trim(artifactId));
        NutsVersion v = getVersion();
        if (!v.isBlank()) {
            sb.append("#");
            sb.append(v);
        }
        if (!NutsBlankable.isBlank(classifier)) {
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
    public NutsVersion getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!NutsBlankable.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NutsUtilStrings.trim(artifactId));
        NutsVersion v = getVersion();
        if (!v.isBlank()) {
            sb.append("#");
            sb.append(v);
        }
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        if (!NutsBlankable.isBlank(classifier)) {
            m.put(NutsConstants.IdProperties.CLASSIFIER, classifier);
        }
        m.putAll(PrivateNutsUtilMaps.toMap(condition));
        for (Map.Entry<String, String> e : PrivateNutsQueryStringParser.parseMap(properties).entrySet()) {
            if (!m.containsKey(e.getKey())) {
                m.put(e.getKey(), e.getValue());
            }
        }
        if (!m.isEmpty()) {
            sb.append("?").append(PrivateNutsQueryStringParser.formatPropertiesQuery(m));
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
    public NutsDependency toDependency() {
        Map<String, String> properties = getProperties();
        //CoreStringUtils.join(",", ex)
        String exc = properties.get(NutsConstants.IdProperties.EXCLUSIONS);
        if (exc == null) {
            exc = "";
        }
        List<NutsId> a = new ArrayList<>();
        for (String s : PrivateNutsUtilStrings.splitDefault(exc)) {
            NutsId n = NutsId.of(s).get();
            if (n != null) {
                a.add(n);
            }
        }
        return new DefaultNutsDependencyBuilder()
                .setRepository(getRepository())
                .setArtifactId(getArtifactId())
                .setGroupId(getGroupId())
                .setClassifier(getClassifier())
                .setVersion(getVersion())
                .setScope(properties.get(NutsConstants.IdProperties.SCOPE))
                .setOptional(properties.get(NutsConstants.IdProperties.OPTIONAL))
                .setExclusions(a)
                .setCondition(getCondition())
                .setProperties(properties)
                .build()
                ;
    }

    @Override
    public NutsIdBuilder builder() {
        return new DefaultNutsIdBuilder(this);
    }

    @Override
    public int compareTo(NutsId o2) {
        int x;
        x = NutsUtilStrings.trim(this.getGroupId()).compareTo(NutsUtilStrings.trim(o2.getGroupId()));
        if (x != 0) {
            return x;
        }
        x = NutsUtilStrings.trim(this.getArtifactId()).compareTo(NutsUtilStrings.trim(o2.getArtifactId()));
        if (x != 0) {
            return x;
        }
        x = NutsUtilStrings.trim(this.getClassifier()).compareTo(NutsUtilStrings.trim(o2.getClassifier()));
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
    public NutsIdFilter filter(NutsSession session) {
        return NutsIdFilters.of(session).byValue(this);
    }

    @Override
    public NutsId compatNewer() {
        return builder().setVersion(getVersion().compatNewer()).build();
    }

    @Override
    public NutsId compatOlder() {
        return builder().setVersion(getVersion().compatOlder()).build();
    }
}
