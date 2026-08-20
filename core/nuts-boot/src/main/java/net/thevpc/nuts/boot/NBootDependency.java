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
 * <br> ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.internal.NBootVersion;
import net.thevpc.nuts.boot.internal.util.NBootConstants;
import net.thevpc.nuts.boot.internal.util.NBootStringMapFormat;
import net.thevpc.nuts.boot.internal.util.NBootUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/5/17.
 */
public class NBootDependency {

    public static NBootDependency API_ID = new NBootDependency(NBootConstants.Ids.NUTS_GROUP_ID, NBootConstants.Ids.NUTS_API_ARTIFACT_ID, "");
    public static NBootDependency RUNTIME_ID = new NBootDependency(NBootConstants.Ids.NUTS_GROUP_ID, NBootConstants.Ids.NUTS_RUNTIME_ARTIFACT_ID, "");
    public static Pattern PATTERN = Pattern.compile("^(?<group>[a-zA-Z0-9_.${}*-]+)(:(?<artifact>[a-zA-Z0-9_.${}*-]+))?(#(?<version>[^?]+))?(\\?(?<query>.+))?$");
    public static NBootDependency BLANK = new NBootDependency(null, null, "");

    private String repository;
    private String groupId;
    private String artifactId;
    private String version;
    private String scope;
    private String optional;
    private String type;
    private String classifier;
    private List<NBootDependency> exclusions = new ArrayList<>();
    private Map<String, String> properties = new LinkedHashMap<>();
    private List<String> conditionArch = new ArrayList<>(); //defaults to empty
    private List<String> conditionOs = new ArrayList<>(); //defaults to empty;
    private List<String> conditionOsDist = new ArrayList<>(); //defaults to empty;
    private List<String> conditionPlatform = new ArrayList<>(); //defaults to empty;
    private List<String> conditionDesktopEnvironment = new ArrayList<>(); //defaults to empty;
    private List<String> conditionProfiles = new ArrayList<>(); //defaults to empty;
    private Map<String, String> conditionProperties = new HashMap<>();


    public static NBootDependency ofApi(String version) {
        if (version == null || version.isEmpty()) {
            return API_ID;
        }
        return new NBootDependency(NBootConstants.Ids.NUTS_GROUP_ID, NBootConstants.Ids.NUTS_API_ARTIFACT_ID, version);
    }

    public static NBootDependency ofRuntime(String version) {
        if (version == null || version.isEmpty()) {
            return RUNTIME_ID;
        }
        return new NBootDependency(NBootConstants.Ids.NUTS_GROUP_ID, NBootConstants.Ids.NUTS_RUNTIME_ARTIFACT_ID, version);
    }

    public static NBootDependency ofApi(NBootVersion version) {
        if (version == null || version.isBlank()) {
            return API_ID;
        }
        return of(NBootConstants.Ids.NUTS_GROUP_ID, NBootConstants.Ids.NUTS_API_ARTIFACT_ID, version.getValue());
    }

    public static List<NBootDependency> ofList(String value) {
        return NBootUtils.parseIdList(value);
    }

    public static Set<NBootDependency> ofSet(String value) {
        List<NBootDependency> nBootIds = ofList(value);
        return nBootIds == null ? null : new LinkedHashSet(nBootIds);
    }


    public static NBootDependency of(String groupId, String artifactId, String version) {
        return new NBootDependency(groupId, artifactId, version);
    }

    public static NBootDependency of(String value) {
        return NBootUtils.parseId(value);
    }

    public NBootDependency() {

    }

    public NBootDependency(String groupId, String artifactId, String version) {
        this.groupId = NBootUtils.trimToNull(groupId);
        this.artifactId = NBootUtils.trimToNull(artifactId);
        this.version = version == null ? "" : version;
    }

    public NBootDependency(NBootDependency other) {
        this(
                other.getRepository(), other.getGroupId(), other.getArtifactId(), other.getClassifier(),
                other.getVersion(),
                other.getScope(),
                other.getOptional(),
                other.getExclusions(),
                other.getType(), other.getPropertiesQuery(),
                other.getConditionArch(),
                other.getConditionOs(),
                other.getConditionOsDist(),
                other.getConditionPlatform(),
                other.getConditionDesktopEnvironment(),
                other.getConditionProfiles(),
                other.getConditionProperties()
        );
    }

    public NBootDependency(String groupId, String artifactId) {
        this.groupId = NBootUtils.trimToNull(groupId);
        this.artifactId = NBootUtils.trimToNull(artifactId);
    }

    public NBootDependency(String repository, String groupId, String artifactId, String classifier, String version, String scope, String optional, List<NBootDependency> exclusions,
                           String type, String properties,
                           List<String> conditionArch, List<String> conditionOs, List<String> conditionOsDist,
                           List<String> conditionPlatform,
                           List<String> conditionDesktopEnvironment,
                           List<String> profile,
                           Map<String, String> conditionProperties
    ) {
        this.repository = NBootUtils.trimToNull(repository);
        this.groupId = NBootUtils.trimToNull(groupId);
        this.artifactId = NBootUtils.trimToNull(artifactId);
        this.version = version == null ? "" : version;
        this.classifier = NBootUtils.trimToNull(classifier);
        this.scope = NBootUtils.firstNonNull(scope, "API");

        String o = NBootUtils.trimToNull(optional);
        if ("false".equalsIgnoreCase(o)) {
            o = null;
        } else if ("true".equalsIgnoreCase(o)) {
            o = "true";//remove case and formatting
        }
        this.optional = o;
        this.exclusions = NBootUtils.unmodifiableList(exclusions);
        for (NBootDependency exclusion : this.exclusions) {
            if (exclusion == null) {
                throw new NullPointerException();
            }
        }
        this.type = NBootUtils.trimToNull(type);
        this.properties = NBootStringMapFormat.DEFAULT.parse(properties);
        this.conditionArch = conditionArch == null ? Collections.emptyList() : Collections.unmodifiableList(conditionArch);
        this.conditionOs = conditionOs == null ? Collections.emptyList() : Collections.unmodifiableList(conditionOs);
        this.conditionOsDist = conditionOsDist == null ? Collections.emptyList() : Collections.unmodifiableList(conditionOsDist);
        this.conditionPlatform = conditionPlatform == null ? Collections.emptyList() : Collections.unmodifiableList(conditionPlatform);
        this.conditionDesktopEnvironment = conditionDesktopEnvironment == null ? Collections.emptyList() : Collections.unmodifiableList(conditionDesktopEnvironment);
        this.conditionProfiles = profile == null ? Collections.emptyList() : Collections.unmodifiableList(profile);
        this.conditionProperties = conditionProperties == null ? new HashMap<>() : new HashMap<>(conditionProperties);
    }

    public NBootDependency(String groupId, String artifactId, String classifier, String version, String propertiesQuery) {
        this.groupId = NBootUtils.trimToNull(groupId);
        this.artifactId = NBootUtils.trimToNull(artifactId);
        this.classifier = NBootUtils.trimToNull(classifier);
        this.version = version == null ? "" : version;
        setPropertiesQuery(propertiesQuery);
    }


    public NBootDependency setId(NBootDependency id) {
        if (id == null) {
            setRepository(null);
            setGroupId(null);
            setArtifactId(null);
            setVersion(null);
        } else {
            setRepository(id.getRepository());
            setGroupId(id.getGroupId());
            setArtifactId(id.getArtifactId());
            setVersion(id.getVersion());
            this.setProperties(id.getProperties());
        }
        return this;
    }


    public NBootDependency setDependency(NBootDependency value) {
        return copyFrom(value);
    }


    public NBootDependency copyFrom(NBootDependency value) {
        if (value != null) {
            setRepository(value.getRepository());
            setGroupId(value.getGroupId());
            setArtifactId(value.getArtifactId());
            setVersion(value.getVersion());
            setScope(value.getScope());
            setOptional(value.getOptional());
            setExclusions(value.getExclusions());
            setClassifier(value.getClassifier());
            setType(value.getType());
            setProperties(value.getProperties());
            this.setConditionOs(value.getConditionOs())
                    .setConditionOsDist(value.getConditionOsDist())
                    .setConditionArch(value.getConditionArch())
                    .setConditionPlatform(value.getConditionPlatform())
                    .setConditionDesktopEnvironment(value.getConditionDesktopEnvironment())
                    .setConditionProfile(value.getConditionProfiles())
                    .setConditionProperties(value.getConditionProperties());
        } else {
            clear();
        }
        return this;
    }


    public NBootDependency clear() {
        setRepository(null);
        setGroupId(null);
        setArtifactId(null);
        setVersion(null);
        setScope(null);
        setOptional(null);
        setExclusions((List<NBootDependency>) null);
        setClassifier(null);
        setType(null);
        setProperties(null);
        setConditionArch(new ArrayList<>());
        setConditionOs(new ArrayList<>());
        setConditionOsDist(new ArrayList<>());
        setConditionPlatform(new ArrayList<>());
        setConditionDesktopEnvironment(new ArrayList<>());
        setConditionProfile(new ArrayList<>());
        setConditionProperties(new LinkedHashMap<>());
        return this;
    }


    public boolean isOptional() {
        return Boolean.parseBoolean(optional);
    }

    public String getType() {
        return type;
    }


    public NBootDependency setType(String type) {
        this.type = NBootUtils.trimToNull(type);
        return this;
    }


    public String getOptional() {
        return optional;
    }


    public NBootDependency setOptional(String optional) {
        String o = NBootUtils.trimToNull(optional);
        if ("false".equals(o)) {
            o = null;
        } else if ("true".equalsIgnoreCase(o)) {
            o = "true";//remove case and formatting
        }
        this.optional = o;
        return this;
    }


    public String getScope() {
        return scope;
    }


    public NBootDependency setScope(String scope) {
        this.scope = scope == null ? "" : scope;
        return this;
    }


    public String getRepository() {
        return repository;
    }


    public NBootDependency setRepository(String repository) {
        this.repository = NBootUtils.trimToNull(repository);
        return this;
    }


    public String getGroupId() {
        return groupId;
    }


    public NBootDependency setGroupId(String groupId) {
        this.groupId = NBootUtils.trimToNull(groupId);
        return this;
    }


    public String getArtifactId() {
        return artifactId;
    }


    public NBootDependency setArtifactId(String artifactId) {
        this.artifactId = NBootUtils.trimToNull(artifactId);
        return this;
    }


    public String getClassifier() {
        return classifier;
    }


    public NBootDependency setClassifier(String classifier) {
        this.classifier = NBootUtils.trimToNull(classifier);
        return this;
    }


    public String getFullName() {
        if (NBootUtils.isBlank(groupId)) {
            return NBootUtils.trim(artifactId);
        }
        return NBootUtils.trim(groupId) + ":" + NBootUtils.trim(artifactId);
    }


    public String getVersion() {
        return version;
    }


    public NBootDependency setVersion(String version) {
        this.version = version == null ? "" : version;
        return this;
    }


    public List<NBootDependency> getExclusions() {
        return exclusions;
    }


    public NBootDependency setExclusions(List<NBootDependency> exclusions) {
        this.exclusions = NBootUtils.nonNullList(exclusions);
        return this;
    }


    public NBootDependency copy() {
        return new NBootDependency(
                getRepository(), getGroupId(), getArtifactId(), getClassifier(),
                getVersion(),
                getScope(),
                getOptional(),
                getExclusions(),
                getType(), getPropertiesQuery(),
                getConditionArch(),
                getConditionOs(),
                getConditionOsDist(),
                getConditionPlatform(),
                getConditionDesktopEnvironment(),
                getConditionProfiles(),
                getConditionProperties()
        );
    }


    public NBootDependency setProperty(String property, String value) {
        if (property != null) {
            switch (property) {
                case NBootConstants.IdProperties.SCOPE: {
                    setScope(value);
                    break;
                }
                case NBootConstants.IdProperties.VERSION: {
                    setVersion(value);
                    break;
                }
                case NBootConstants.IdProperties.OPTIONAL: {
                    setOptional(value);
                    break;
                }
                case NBootConstants.IdProperties.REPO: {
                    setRepository(value);
                    break;
                }
                case NBootConstants.IdProperties.EXCLUSIONS: {
                    setExclusions(value);
                    break;
                }
                case NBootConstants.IdProperties.OS: {
                    this.conditionOs = NBootUtils.uniqueNonBlankStringList(NBootUtils.parsePropertyIdList(value));
                    break;
                }
                case NBootConstants.IdProperties.ARCH: {
                    this.conditionArch = NBootUtils.uniqueNonBlankStringList(NBootUtils.parsePropertyIdList(value));
                    break;
                }
                case NBootConstants.IdProperties.PLATFORM: {
                    this.conditionPlatform = NBootUtils.uniqueNonBlankStringList(NBootUtils.parsePropertyIdList(value));
                    break;
                }
                case NBootConstants.IdProperties.OS_DIST: {
                    this.conditionOsDist = NBootUtils.uniqueNonBlankStringList(NBootUtils.parsePropertyIdList(value));
                    break;
                }
                case NBootConstants.IdProperties.DESKTOP: {
                    this.conditionDesktopEnvironment = NBootUtils.uniqueNonBlankStringList(NBootUtils.parsePropertyIdList(value));
                    break;
                }
                case NBootConstants.IdProperties.TYPE: {
                    setType(value);
                    break;
                }
                case NBootConstants.IdProperties.PROFILE: {
                    this.conditionProfiles = NBootUtils.uniqueNonBlankStringList(NBootUtils.parsePropertyIdList(value));
                    break;
                }
                case NBootConstants.IdProperties.CONDITIONAL_PROPERTIES: {
                    this.conditionProperties = NBootStringMapFormat.DEFAULT.parse(value);
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
        }
        return this;
    }


    public NBootDependency addPropertiesQuery(String propertiesQuery) {
        return addProperties(NBootStringMapFormat.DEFAULT.parse(propertiesQuery));
    }


    public NBootDependency addProperties(Map<String, String> queryMap) {
        if (queryMap != null) {
            for (Map.Entry<String, String> e : queryMap.entrySet()) {
                setProperty(e.getKey(), e.getValue());
            }
        }
        return this;
    }


    public NBootDependency setProperties(Map<String, String> queryMap) {
        properties.clear();
        if (queryMap != null) {
            for (Map.Entry<String, String> e : queryMap.entrySet()) {
                setProperty(e.getKey(), e.getValue());
            }
        }
        return this;
    }


    public NBootDependency setPropertiesQuery(String propertiesQuery) {
        setProperties(NBootStringMapFormat.DEFAULT.parse(propertiesQuery));
        return this;
    }


    public String getPropertiesQuery() {
        return NBootStringMapFormat.DEFAULT.format(properties);
    }


    public Map<String, String> getProperties() {
        return properties;
    }




    public List<String> getConditionArch() {
        return conditionArch;
    }

    public List<String> getConditionOs() {
        return conditionOs;
    }

    public List<String> getConditionOsDist() {
        return conditionOsDist;
    }

    public List<String> getConditionPlatform() {
        return conditionPlatform;
    }

    public List<String> getConditionDesktopEnvironment() {
        return conditionDesktopEnvironment;
    }

    public List<String> getConditionProfiles() {
        return conditionProfiles;
    }

    public Map<String, String> getConditionProperties() {
        return conditionProperties;
    }

    public NBootDependency setExclusions(String exclusions) {
        if (exclusions == null) {
            exclusions = "";
        }
        List<NBootDependency> ids = new ArrayList<>();
        for (String s : NBootUtils.splitDefault(exclusions)) {
            NBootDependency ii = NBootDependency.of(s);
            if (ii != null) {
                ids.add(ii);
            }
        }
        setExclusions(ids);
        return this;
    }


    public String getSimpleName() {
        return NBootUtils.getIdShortName(groupId, artifactId);
    }

    public String getLongName() {
        return NBootUtils.getIdLongName(groupId, artifactId, version, classifier);
    }

    public boolean isBlank() {
        if (conditionArch != null && !conditionArch.isEmpty()) return false;
        if (conditionOs != null && !conditionOs.isEmpty()) return false;
        if (conditionOsDist != null && !conditionOsDist.isEmpty()) return false;
        if (conditionPlatform != null && !conditionPlatform.isEmpty()) return false;
        if (conditionDesktopEnvironment != null && !conditionDesktopEnvironment.isEmpty()) return false;
        if (conditionProfiles != null && !conditionProfiles.isEmpty()) return false;
        if (conditionProperties != null && !conditionProperties.isEmpty()) return false;
        return toString().isEmpty();
    }

    public String getFace() {
        String s = getProperties().get(NBootConstants.IdProperties.FACE);
        return NBootUtils.trimToNull(s);
    }

    public String getPackaging() {
        String s = getProperties().get(NBootConstants.IdProperties.PACKAGING);
        return NBootUtils.trimToNull(s);
    }


    public NBootDependency setFace(String value) {
        return setProperty(NBootConstants.IdProperties.FACE, NBootUtils.trimToNull(value));
//                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
    }


    public NBootDependency setFaceContent() {
        return setFace(NBootConstants.QueryFaces.CONTENT);
    }


    public NBootDependency setFaceDescriptor() {
        return setFace(NBootConstants.QueryFaces.DESCRIPTOR);
    }

    public NBootDependency setPackaging(String value) {
        return setProperty(NBootConstants.IdProperties.PACKAGING, NBootUtils.trimToNull(value));
    }


    public NBootDependency clearProperties() {
        properties.clear();
        return this;
    }

    public String getShortName() {
        return NBootUtils.getIdShortName(groupId, artifactId);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (NBootUtils.isBlank(classifier)) {
            if (!NBootUtils.isBlank(groupId)) {
                sb.append(groupId).append(":");
            }
            sb.append(NBootUtils.trim(artifactId));
        } else {
            sb.append(NBootUtils.trim(groupId));
            sb.append(":").append(NBootUtils.trim(artifactId));
            sb.append(":").append(NBootUtils.trim(classifier));
        }
        String v = getVersion();
        if (!NBootUtils.isBlank(v)) {
            sb.append("#");
            sb.append(v);
        }
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        String s;

        if (getConditionArch() != null) {
            s = getConditionArch().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBootUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.ARCH, s);
            }
        }
        if (getConditionOs() != null) {
            s = getConditionOs().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBootUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.OS, s);
            }
        }
        if (getConditionOsDist() != null) {
            s = getConditionOsDist().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBootUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.OS_DIST, s);
            }
        }
        if (getConditionPlatform() != null) {
            s = NBootUtils.formatStringIdList(getConditionPlatform());
            if (!NBootUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.PLATFORM, s);
            }
        }
        if (getConditionDesktopEnvironment() != null) {
            s = getConditionDesktopEnvironment().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBootUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.DESKTOP, s);
            }
        }
        if (getConditionProfiles() != null) {
            s = getConditionProfiles().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBootUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.PROFILE, s);
            }
        }
        if (getConditionProperties() != null) {
            Map<String, String> properties = getConditionProperties();
            if (!properties.isEmpty()) {
                m.put(NBootConstants.IdProperties.CONDITIONAL_PROPERTIES, NBootStringMapFormat.DEFAULT.format(properties));
            }
        }

        if (properties != null) {
            for (Map.Entry<String, String> e : properties.entrySet()) {
                if (!m.containsKey(e.getKey())) {
                    m.put(e.getKey(), e.getValue());
                }
            }
        }
        if (!m.isEmpty()) {
            sb.append("?").append(NBootStringMapFormat.DEFAULT.format(m));
        }
        return sb.toString();
    }


    public boolean equalsShortId(NBootDependency other) {
        if (other == null) {
            return false;
        }
        return NBootUtils.trim(groupId).equals(NBootUtils.trim(other.getArtifactId()))
                && NBootUtils.trim(artifactId).equals(NBootUtils.trim(other.getGroupId()));
    }


    public boolean equalsLongId(NBootDependency other) {
        if (other == null) {
            return false;
        }
        return NBootUtils.trim(artifactId).equals(NBootUtils.trim(other.getArtifactId()))
                && NBootUtils.trim(groupId).equals(NBootUtils.trim(other.getGroupId()))
                && Objects.equals((version == null || NBootUtils.isBlank(version)) ? null : version,
                (other.getVersion() == null || NBootUtils.isBlank(other.getVersion())) ? null : other.getVersion())
                && Objects.equals(getClassifier(), other.getClassifier())
                ;
    }


    public NBootDependency getShortId() {
        return new NBootDependency(groupId, artifactId, classifier, null, "");
    }


    public NBootDependency getLongId() {
        return new NBootDependency(groupId, artifactId, classifier, version, "");
    }

    public NBootDependency toDependency() {
        Map<String, String> properties = getProperties();
        //CoreStringUtils.join(",", ex)
        String exc = properties.get(NBootConstants.IdProperties.EXCLUSIONS);
        if (exc == null) {
            exc = "";
        }
        List<NBootDependency> a = new ArrayList<>();
        for (String s : NBootUtils.splitDefault(exc)) {
            NBootDependency n = NBootDependency.of(s);
            if (n != null) {
                a.add(n);
            }
        }
        return new NBootDependency()
                .setRepository(getRepository())
                .setArtifactId(getArtifactId())
                .setGroupId(getGroupId())
                .setClassifier(getClassifier())
                .setVersion(getVersion())
                .setScope(properties.get(NBootConstants.IdProperties.SCOPE))
                .setOptional(properties.get(NBootConstants.IdProperties.OPTIONAL))
                .setExclusions(a)
                .setProperties(properties)
                .setConditionOs(getConditionOs())
                .setConditionOsDist(getConditionOsDist())
                .setConditionArch(getConditionArch())
                .setConditionPlatform(getConditionPlatform())
                .setConditionDesktopEnvironment(getConditionDesktopEnvironment())
                .setConditionProfile(getConditionProfiles())
                .setConditionProperties(getConditionProperties())
                ;
    }

    public NBootDependency setConditionProperties(Map<String, String> conditionProperties) {
        this.conditionProperties = conditionProperties == null ? null : new HashMap<>(conditionProperties);
        return this;
    }

    public NBootDependency setConditionDesktopEnvironment(List<String> conditionDesktopEnvironment) {
        this.conditionDesktopEnvironment = NBootUtils.uniqueNonBlankStringList(conditionDesktopEnvironment);
        return this;
    }

    public NBootDependency setConditionProfile(List<String> profiles) {
        this.conditionProfiles = profiles;
        return this;
    }

    public NBootDependency setConditionPlatform(List<String> conditionPlatform) {
        this.conditionPlatform = NBootUtils.uniqueNonBlankStringList(conditionPlatform);
        return this;
    }

    public NBootDependency setConditionOsDist(List<String> conditionOsDist) {
        this.conditionOsDist = NBootUtils.uniqueNonBlankStringList(conditionOsDist);
        return this;
    }

    public NBootDependency setConditionOs(List<String> conditionOs) {
        this.conditionOs = NBootUtils.uniqueNonBlankStringList(conditionOs);
        return this;
    }

    public NBootDependency setConditionArch(List<String> conditionArch) {
        this.conditionArch = NBootUtils.uniqueNonBlankStringList(conditionArch);
        return this;
    }

    public boolean isNull() {
        return false;
    }

    public int compareTo(NBootDependency o2) {
        int x;
        x = NBootUtils.trim(this.getGroupId()).compareTo(NBootUtils.trim(o2.getGroupId()));
        if (x != 0) {
            return x;
        }
        x = NBootUtils.trim(this.getArtifactId()).compareTo(NBootUtils.trim(o2.getArtifactId()));
        if (x != 0) {
            return x;
        }
        x = NBootUtils.trim(this.getClassifier()).compareTo(NBootUtils.trim(o2.getClassifier()));
        if (x != 0) {
            return x;
        }
        x = NBootVersion.of(this.getVersion()).compareTo(NBootVersion.of(o2.getVersion()));
        return x;
    }
}
