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
        return of(NBootConstants.Ids.NUTS_GROUP_ID, NBootConstants.Ids.NUTS_API_ARTIFACT_ID, version.value());
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
                other.repository(), other.groupId(), other.artifactId(), other.classifier(),
                other.version(),
                other.scope(),
                other.optional(),
                other.exclusions(),
                other.type(), other.propertiesQuery(),
                other.conditionArch(),
                other.conditionOs(),
                other.conditionOsDist(),
                other.conditionPlatform(),
                other.conditionDesktopEnvironment(),
                other.conditionProfiles(),
                other.conditionProperties()
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
        propertiesQuery(propertiesQuery);
    }


    public NBootDependency id(NBootDependency id) {
        if (id == null) {
            repository(null);
            groupId(null);
            artifactId(null);
            version(null);
        } else {
            repository(id.repository());
            groupId(id.groupId());
            artifactId(id.artifactId());
            version(id.version());
            this.properties(id.properties());
        }
        return this;
    }


    public NBootDependency dependency(NBootDependency value) {
        return copyFrom(value);
    }


    public NBootDependency copyFrom(NBootDependency value) {
        if (value != null) {
            repository(value.repository());
            groupId(value.groupId());
            artifactId(value.artifactId());
            version(value.version());
            scope(value.scope());
            optional(value.optional());
            exclusions(value.exclusions());
            classifier(value.classifier());
            type(value.type());
            properties(value.properties());
            this.conditionOs(value.conditionOs())
                    .conditionOsDist(value.conditionOsDist())
                    .conditionArch(value.conditionArch())
                    .conditionPlatform(value.conditionPlatform())
                    .conditionDesktopEnvironment(value.conditionDesktopEnvironment())
                    .conditionProfile(value.conditionProfiles())
                    .conditionProperties(value.conditionProperties());
        } else {
            clear();
        }
        return this;
    }


    public NBootDependency clear() {
        repository(null);
        groupId(null);
        artifactId(null);
        version(null);
        scope(null);
        optional(null);
        exclusions((List<NBootDependency>) null);
        classifier(null);
        type(null);
        properties(null);
        conditionArch(new ArrayList<>());
        conditionOs(new ArrayList<>());
        conditionOsDist(new ArrayList<>());
        conditionPlatform(new ArrayList<>());
        conditionDesktopEnvironment(new ArrayList<>());
        conditionProfile(new ArrayList<>());
        conditionProperties(new LinkedHashMap<>());
        return this;
    }


    public boolean isOptional() {
        return Boolean.parseBoolean(optional);
    }

    public String type() {
        return type;
    }


    public NBootDependency type(String type) {
        this.type = NBootUtils.trimToNull(type);
        return this;
    }


    public String optional() {
        return optional;
    }


    public NBootDependency optional(String optional) {
        String o = NBootUtils.trimToNull(optional);
        if ("false".equals(o)) {
            o = null;
        } else if ("true".equalsIgnoreCase(o)) {
            o = "true";//remove case and formatting
        }
        this.optional = o;
        return this;
    }


    public String scope() {
        return scope;
    }


    public NBootDependency scope(String scope) {
        this.scope = scope == null ? "" : scope;
        return this;
    }


    public String repository() {
        return repository;
    }


    public NBootDependency repository(String repository) {
        this.repository = NBootUtils.trimToNull(repository);
        return this;
    }


    public String groupId() {
        return groupId;
    }


    public NBootDependency groupId(String groupId) {
        this.groupId = NBootUtils.trimToNull(groupId);
        return this;
    }


    public String artifactId() {
        return artifactId;
    }


    public NBootDependency artifactId(String artifactId) {
        this.artifactId = NBootUtils.trimToNull(artifactId);
        return this;
    }


    public String classifier() {
        return classifier;
    }


    public NBootDependency classifier(String classifier) {
        this.classifier = NBootUtils.trimToNull(classifier);
        return this;
    }


    public String fullName() {
        if (NBootUtils.isBlank(groupId)) {
            return NBootUtils.trim(artifactId);
        }
        return NBootUtils.trim(groupId) + ":" + NBootUtils.trim(artifactId);
    }


    public String version() {
        return version;
    }


    public NBootDependency version(String version) {
        this.version = version == null ? "" : version;
        return this;
    }


    public List<NBootDependency> exclusions() {
        return exclusions;
    }


    public NBootDependency exclusions(List<NBootDependency> exclusions) {
        this.exclusions = NBootUtils.nonNullList(exclusions);
        return this;
    }


    public NBootDependency copy() {
        return new NBootDependency(
                repository(), groupId(), artifactId(), classifier(),
                version(),
                scope(),
                optional(),
                exclusions(),
                type(), propertiesQuery(),
                conditionArch(),
                conditionOs(),
                conditionOsDist(),
                conditionPlatform(),
                conditionDesktopEnvironment(),
                conditionProfiles(),
                conditionProperties()
        );
    }


    public NBootDependency setProperty(String property, String value) {
        if (property != null) {
            switch (property) {
                case NBootConstants.IdProperties.SCOPE: {
                    scope(value);
                    break;
                }
                case NBootConstants.IdProperties.VERSION: {
                    version(value);
                    break;
                }
                case NBootConstants.IdProperties.OPTIONAL: {
                    optional(value);
                    break;
                }
                case NBootConstants.IdProperties.REPO: {
                    repository(value);
                    break;
                }
                case NBootConstants.IdProperties.EXCLUSIONS: {
                    exclusions(value);
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
                    type(value);
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


    public NBootDependency properties(Map<String, String> queryMap) {
        properties.clear();
        if (queryMap != null) {
            for (Map.Entry<String, String> e : queryMap.entrySet()) {
                setProperty(e.getKey(), e.getValue());
            }
        }
        return this;
    }


    public NBootDependency propertiesQuery(String propertiesQuery) {
        properties(NBootStringMapFormat.DEFAULT.parse(propertiesQuery));
        return this;
    }


    public String propertiesQuery() {
        return NBootStringMapFormat.DEFAULT.format(properties);
    }


    public Map<String, String> properties() {
        return properties;
    }




    public List<String> conditionArch() {
        return conditionArch;
    }

    public List<String> conditionOs() {
        return conditionOs;
    }

    public List<String> conditionOsDist() {
        return conditionOsDist;
    }

    public List<String> conditionPlatform() {
        return conditionPlatform;
    }

    public List<String> conditionDesktopEnvironment() {
        return conditionDesktopEnvironment;
    }

    public List<String> conditionProfiles() {
        return conditionProfiles;
    }

    public Map<String, String> conditionProperties() {
        return conditionProperties;
    }

    public NBootDependency exclusions(String exclusions) {
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
        exclusions(ids);
        return this;
    }


    public String simpleName() {
        return NBootUtils.getIdShortName(groupId, artifactId);
    }

    public String longName() {
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

    public String face() {
        String s = properties().get(NBootConstants.IdProperties.FACE);
        return NBootUtils.trimToNull(s);
    }

    public String packaging() {
        String s = properties().get(NBootConstants.IdProperties.PACKAGING);
        return NBootUtils.trimToNull(s);
    }


    public NBootDependency face(String value) {
        return setProperty(NBootConstants.IdProperties.FACE, NBootUtils.trimToNull(value));
    }


    public NBootDependency packaging(String value) {
        return setProperty(NBootConstants.IdProperties.PACKAGING, NBootUtils.trimToNull(value));
    }


    public NBootDependency clearProperties() {
        properties.clear();
        return this;
    }

    public String shortName() {
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
        String v = version();
        if (!NBootUtils.isBlank(v)) {
            sb.append("#");
            sb.append(v);
        }
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        String s;

        if (conditionArch() != null) {
            s = conditionArch().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBootUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.ARCH, s);
            }
        }
        if (conditionOs() != null) {
            s = conditionOs().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBootUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.OS, s);
            }
        }
        if (conditionOsDist() != null) {
            s = conditionOsDist().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBootUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.OS_DIST, s);
            }
        }
        if (conditionPlatform() != null) {
            s = NBootUtils.formatStringIdList(conditionPlatform());
            if (!NBootUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.PLATFORM, s);
            }
        }
        if (conditionDesktopEnvironment() != null) {
            s = conditionDesktopEnvironment().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBootUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.DESKTOP, s);
            }
        }
        if (conditionProfiles() != null) {
            s = conditionProfiles().stream().map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
            if (!NBootUtils.isBlank(s)) {
                m.put(NBootConstants.IdProperties.PROFILE, s);
            }
        }
        if (conditionProperties() != null) {
            Map<String, String> properties = conditionProperties();
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
        return NBootUtils.trim(groupId).equals(NBootUtils.trim(other.artifactId()))
                && NBootUtils.trim(artifactId).equals(NBootUtils.trim(other.groupId()));
    }


    public boolean equalsLongId(NBootDependency other) {
        if (other == null) {
            return false;
        }
        return NBootUtils.trim(artifactId).equals(NBootUtils.trim(other.artifactId()))
                && NBootUtils.trim(groupId).equals(NBootUtils.trim(other.groupId()))
                && Objects.equals((version == null || NBootUtils.isBlank(version)) ? null : version,
                (other.version() == null || NBootUtils.isBlank(other.version())) ? null : other.version())
                && Objects.equals(classifier(), other.classifier())
                ;
    }


    public NBootDependency shortId() {
        return new NBootDependency(groupId, artifactId, classifier, null, "");
    }


    public NBootDependency longId() {
        return new NBootDependency(groupId, artifactId, classifier, version, "");
    }

    public NBootDependency toDependency() {
        Map<String, String> properties = properties();
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
                .repository(repository())
                .artifactId(artifactId())
                .groupId(groupId())
                .classifier(classifier())
                .version(version())
                .scope(properties.get(NBootConstants.IdProperties.SCOPE))
                .optional(properties.get(NBootConstants.IdProperties.OPTIONAL))
                .exclusions(a)
                .properties(properties)
                .conditionOs(conditionOs())
                .conditionOsDist(conditionOsDist())
                .conditionArch(conditionArch())
                .conditionPlatform(conditionPlatform())
                .conditionDesktopEnvironment(conditionDesktopEnvironment())
                .conditionProfile(conditionProfiles())
                .conditionProperties(conditionProperties())
                ;
    }

    public NBootDependency conditionProperties(Map<String, String> conditionProperties) {
        this.conditionProperties = conditionProperties == null ? null : new HashMap<>(conditionProperties);
        return this;
    }

    public NBootDependency conditionDesktopEnvironment(List<String> conditionDesktopEnvironment) {
        this.conditionDesktopEnvironment = NBootUtils.uniqueNonBlankStringList(conditionDesktopEnvironment);
        return this;
    }

    public NBootDependency conditionProfile(List<String> profiles) {
        this.conditionProfiles = profiles;
        return this;
    }

    public NBootDependency conditionPlatform(List<String> conditionPlatform) {
        this.conditionPlatform = NBootUtils.uniqueNonBlankStringList(conditionPlatform);
        return this;
    }

    public NBootDependency conditionOsDist(List<String> conditionOsDist) {
        this.conditionOsDist = NBootUtils.uniqueNonBlankStringList(conditionOsDist);
        return this;
    }

    public NBootDependency conditionOs(List<String> conditionOs) {
        this.conditionOs = NBootUtils.uniqueNonBlankStringList(conditionOs);
        return this;
    }

    public NBootDependency conditionArch(List<String> conditionArch) {
        this.conditionArch = NBootUtils.uniqueNonBlankStringList(conditionArch);
        return this;
    }

    public boolean isNull() {
        return false;
    }

    public int compareTo(NBootDependency o2) {
        int x;
        x = NBootUtils.trim(this.groupId()).compareTo(NBootUtils.trim(o2.groupId()));
        if (x != 0) {
            return x;
        }
        x = NBootUtils.trim(this.artifactId()).compareTo(NBootUtils.trim(o2.artifactId()));
        if (x != 0) {
            return x;
        }
        x = NBootUtils.trim(this.classifier()).compareTo(NBootUtils.trim(o2.classifier()));
        if (x != 0) {
            return x;
        }
        x = NBootVersion.of(this.version()).compareTo(NBootVersion.of(o2.version()));
        return x;
    }
}
