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
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.reserved.util.NBootConstants;
import net.thevpc.nuts.boot.reserved.util.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/5/17.
 */
public class NBootId {

    public static NBootId API_ID = new NBootId(NBootConstants.Ids.NUTS_GROUP_ID, NBootConstants.Ids.NUTS_API_ARTIFACT_ID, "");
    public static NBootId RUNTIME_ID = new NBootId(NBootConstants.Ids.NUTS_GROUP_ID, NBootConstants.Ids.NUTS_RUNTIME_ARTIFACT_ID, "");
    public static Pattern PATTERN = Pattern.compile("^(?<group>[a-zA-Z0-9_.${}*-]+)(:(?<artifact>[a-zA-Z0-9_.${}*-]+))?(#(?<version>[^?]+))?(\\?(?<query>.+))?$");
    public static NBootId BLANK = new NBootId(null, null, "");

    private String groupId;
    private String artifactId;
    private String classifier;
    private String version;
    private NBootEnvCondition condition = new NBootEnvCondition();
    private Map<String, String> properties = new LinkedHashMap<>();

    public static NBootId ofApi(String version) {
        if (version == null || version.isEmpty()) {
            return API_ID;
        }
        return new NBootId(NBootConstants.Ids.NUTS_GROUP_ID, NBootConstants.Ids.NUTS_API_ARTIFACT_ID, version);
    }
    public static NBootId ofRuntime(String version) {
        if (version == null || version.isEmpty()) {
            return RUNTIME_ID;
        }
        return new NBootId(NBootConstants.Ids.NUTS_GROUP_ID, NBootConstants.Ids.NUTS_RUNTIME_ARTIFACT_ID, version);
    }

    public static NBootId ofApi(NBootVersion version) {
        if (version == null || version.isBlank()) {
            return API_ID;
        }
        return of(NBootConstants.Ids.NUTS_GROUP_ID, NBootConstants.Ids.NUTS_API_ARTIFACT_ID, version.getValue());
    }
    public static List<NBootId> ofList(String value) {
        return NBootUtils.parseIdList(value);
    }

    public static Set<NBootId> ofSet(String value) {
        List<NBootId> nBootIds = ofList(value);
        return nBootIds ==null?null:new LinkedHashSet(nBootIds);
    }

    public NBootId() {
        this.condition = new NBootEnvCondition();
    }

    public NBootId(NBootId id) {
        copyFrom(id);
    }

    public NBootId(String groupId, String artifactId) {
        this.groupId = NBootUtils.trimToNull(groupId);
        this.artifactId = NBootUtils.trimToNull(artifactId);
    }

    public static NBootId of(String groupId, String artifactId, String version) {
        return new NBootId(groupId, artifactId, version);
    }

    public NBootId(String groupId, String artifactId, String version) {
        this.groupId = NBootUtils.trimToNull(groupId);
        this.artifactId = NBootUtils.trimToNull(artifactId);
        this.version = version == null ? "" : version;
    }

    public NBootId(String groupId, String artifactId, String classifier, String version, String propertiesQuery, NBootEnvCondition condition) {
        this.groupId = NBootUtils.trimToNull(groupId);
        this.artifactId = NBootUtils.trimToNull(artifactId);
        this.classifier = NBootUtils.trimToNull(classifier);
        this.version = version == null ? "" : version;

        setCondition(condition);
        setProperties(properties);
    }

    public static NBootId of(String value) {
        return NBootUtils.parseId(value);
    }


    public NBootId copyFrom(NBootId id) {
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


    public NBootId clear() {
        setGroupId(null);
        setArtifactId(null);
        setVersion(null);
        setPropertiesQuery("");
        return this;
    }

    public NBootId setGroupId(String value) {
        this.groupId = NBootUtils.trimToNull(value);
        return this;
    }


    public NBootId setRepository(String value) {
        return setProperty(NBootConstants.IdProperties.REPO, NBootUtils.trimToNull(value));
    }


    public NBootId setVersion(String value) {
        this.version = value == null ? "" : value;
        return this;
    }


    public NBootId setArtifactId(String value) {
        this.artifactId = NBootUtils.trimToNull(value);
        return this;
    }


    public String getFace() {
        String s = getProperties().get(NBootConstants.IdProperties.FACE);
        return NBootUtils.trimToNull(s);
    }

//    
//    public String getAlternative() {
//        String s = getProperties().get(NutsConstants.IdProperties.ALTERNATIVE);
//        return NutsUtilStrings.trimToNull(s);
//    }


    public String getPackaging() {
        String s = getProperties().get(NBootConstants.IdProperties.PACKAGING);
        return NBootUtils.trimToNull(s);
    }


    public NBootId setFace(String value) {
        return setProperty(NBootConstants.IdProperties.FACE, NBootUtils.trimToNull(value));
//                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
    }


    public NBootId setFaceContent() {
        return setFace(NBootConstants.QueryFaces.CONTENT);
    }


    public NBootId setFaceDescriptor() {
        return setFace(NBootConstants.QueryFaces.DESCRIPTOR);
    }

//    
//    public NutsIdBuilder setAlternative(String value) {
//        return setProperty(NutsConstants.IdProperties.ALTERNATIVE, NutsUtilStrings.trimToNull(value));

    /// /                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
//    }
    public String getClassifier() {
        return classifier;
    }


    public NBootId setClassifier(String value) {
        this.classifier = NBootUtils.trimToNull(value);
        return this;
    }


    public NBootId setPackaging(String value) {
        return setProperty(NBootConstants.IdProperties.PACKAGING, NBootUtils.trimToNull(value));
    }


    public NBootId setCondition(NBootEnvCondition c) {
        if (c == null) {
            setProperty(NBootConstants.IdProperties.OS, null);
            setProperty(NBootConstants.IdProperties.OS_DIST, null);
            setProperty(NBootConstants.IdProperties.ARCH, null);
            setProperty(NBootConstants.IdProperties.PLATFORM, null);
            setProperty(NBootConstants.IdProperties.DESKTOP, null);
            setProperty(NBootConstants.IdProperties.PROFILE, null);
            condition.setProperties(null);
        } else {
            setProperty(NBootConstants.IdProperties.OS, NBootUtils.joinAndTrimToNull(c.getOs()));
            setProperty(NBootConstants.IdProperties.OS_DIST, NBootUtils.joinAndTrimToNull(c.getOsDist()));
            setProperty(NBootConstants.IdProperties.ARCH, NBootUtils.joinAndTrimToNull(c.getArch()));
            setProperty(NBootConstants.IdProperties.PLATFORM, NBootUtils.formatStringIdList(c.getPlatform()));
            setProperty(NBootConstants.IdProperties.DESKTOP, NBootUtils.joinAndTrimToNull(c.getDesktopEnvironment()));
            setProperty(NBootConstants.IdProperties.PROFILE, NBootUtils.joinAndTrimToNull(c.getProfiles()));
            condition.setProperties(c.getProperties());

        }
        return this;
    }


    public NBootEnvCondition getCondition() {
        return condition;
    }

    public NBootId setProperty(String property, String value) {
        switch (property) {
            case NBootConstants.IdProperties.OS: {
                condition.setOs(NBootUtils.parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.OS_DIST: {
                condition.setOsDist(NBootUtils.parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.ARCH: {
                condition.setArch(NBootUtils.parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.PLATFORM: {
                condition.setPlatform(NBootUtils.parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.DESKTOP: {
                condition.setDesktopEnvironment(NBootUtils.parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.PROFILE: {
                condition.setProfile(NBootUtils.parsePropertyIdList(value));
                break;
            }
            case NBootConstants.IdProperties.CONDITIONAL_PROPERTIES: {
                condition.setProperties(NBootStringMapFormat.DEFAULT.parse(value));
                break;
            }
            case NBootConstants.IdProperties.VERSION: {
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


    public NBootId setProperties(Map<String, String> queryMap) {
        for (Map.Entry<String, String> e : queryMap.entrySet()) {
            setProperty(e.getKey(), e.getValue());
        }
        return this;
    }


    public NBootId clearProperties() {
        properties.clear();
        return this;
    }


    public NBootId setPropertiesQuery(String propertiesQuery) {
        setProperties(NBootStringMapFormat.DEFAULT.parse(propertiesQuery));
        return this;
    }


    public String getPropertiesQuery() {
        return NBootStringMapFormat.DEFAULT.format(getProperties());
    }


    public Map<String, String> getProperties() {
        return properties;
    }


    public String getRepository() {
        return NBootUtils.trimToNull(getProperties().get(NBootConstants.IdProperties.REPO));
    }


    public String getGroupId() {
        return groupId;
    }


    public String getFullName() {
        return toString();
    }


    public String getShortName() {
        return NBootUtils.getIdShortName(groupId, artifactId);
    }


    public String getLongName() {
        return NBootUtils.getIdLongName(groupId, artifactId, version, classifier);
    }


    public String getArtifactId() {
        return artifactId;
    }


    public String getVersion() {
        return version;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (NBootUtils.isBlank(classifier)) {
            if (!NBootUtils.isBlank(groupId)) {
                sb.append(groupId).append(":");
            }
            sb.append(NBootUtils.trim(artifactId));
        }else {
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
        m.putAll(NBootUtils.toMap(condition));
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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NBootId nutsId = (NBootId) o;

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


    public int hashCode() {
        int result = (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }

    public boolean equalsShortId(NBootId other) {
        if (other == null) {
            return false;
        }
        return NBootUtils.trim(groupId).equals(NBootUtils.trim(other.getArtifactId()))
                && NBootUtils.trim(artifactId).equals(NBootUtils.trim(other.getGroupId()));
    }


    public boolean equalsLongId(NBootId other) {
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


    public NBootId getShortId() {
        return new NBootId(groupId, artifactId, classifier, null, "",
                NBootEnvCondition.BLANK);
    }


    public NBootId getLongId() {
        return new NBootId(groupId, artifactId, classifier, version, "", NBootEnvCondition.BLANK);
    }


    public NBootId copy() {
        return new NBootId(this);
    }


    public NBootDependency toDependency() {
        Map<String, String> properties = getProperties();
        //CoreStringUtils.join(",", ex)
        String exc = properties.get(NBootConstants.IdProperties.EXCLUSIONS);
        if (exc == null) {
            exc = "";
        }
        List<NBootId> a = new ArrayList<>();
        for (String s : NBootUtils.splitDefault(exc)) {
            NBootId n = NBootId.of(s);
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
                .setCondition(getCondition())
                .setProperties(properties)
                ;
    }

//    public NIdFilter filter() {
//        return build().filter();
//    }


//    public NIdBoot compatNewer() {
//        return build().compatNewer();
//    }


//    public NIdBoot compatOlder() {
//        return build().compatOlder();
//    }


    public boolean isNull() {
        return false;
    }

    public boolean isBlank() {
        return toString().isEmpty();
    }


    public int compareTo(NBootId o2) {
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
        if (x != 0) {
            return x;
        }
        return 0;
    }


}
