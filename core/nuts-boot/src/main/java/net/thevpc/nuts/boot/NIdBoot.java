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

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.boot.reserved.util.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/5/17.
 */
public class NIdBoot {

    public static NIdBoot API_ID = new NIdBoot(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_API_ARTIFACT_ID, "");
    public static NIdBoot RUNTIME_ID = new NIdBoot(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_RUNTIME_ARTIFACT_ID, "");
    public static Pattern PATTERN = Pattern.compile("^(?<group>[a-zA-Z0-9_.${}*-]+)(:(?<artifact>[a-zA-Z0-9_.${}*-]+))?(#(?<version>[^?]+))?(\\?(?<query>.+))?$");
    public static NIdBoot BLANK = new NIdBoot(null, null, "");

    private String groupId;
    private String artifactId;
    private String classifier;
    private String version;
    private NEnvConditionBoot condition = new NEnvConditionBoot();
    private Map<String, String> properties = new LinkedHashMap<>();

    public static NIdBoot ofApi(String version) {
        if (version == null || version.isEmpty()) {
            return API_ID;
        }
        return new NIdBoot(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_API_ARTIFACT_ID, version);
    }
    public static NIdBoot ofRuntime(String version) {
        if (version == null || version.isEmpty()) {
            return RUNTIME_ID;
        }
        return new NIdBoot(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_RUNTIME_ARTIFACT_ID, version);
    }

    public static NIdBoot ofApi(NVersionBoot version) {
        if (version == null || version.isBlank()) {
            return API_ID;
        }
        return of(NConstants.Ids.NUTS_GROUP_ID, NConstants.Ids.NUTS_API_ARTIFACT_ID, version.getValue());
    }
    public static List<NIdBoot> ofList(String value) {
        return NReservedUtilsBoot.parseIdList(value);
    }

    public static Set<NIdBoot> ofSet(String value) {
        List<NIdBoot> nIdBoots = ofList(value);
        return nIdBoots==null?null:new LinkedHashSet(nIdBoots);
    }

    public NIdBoot() {
        this.condition = new NEnvConditionBoot();
    }

    public NIdBoot(NIdBoot id) {
        setAll(id);
    }

    public NIdBoot(String groupId, String artifactId) {
        this.groupId = NStringUtilsBoot.trimToNull(groupId);
        this.artifactId = NStringUtilsBoot.trimToNull(artifactId);
    }

    public static NIdBoot of(String groupId, String artifactId, String version) {
        return new NIdBoot(groupId, artifactId, version);
    }

    public NIdBoot(String groupId, String artifactId, String version) {
        this.groupId = NStringUtilsBoot.trimToNull(groupId);
        this.artifactId = NStringUtilsBoot.trimToNull(artifactId);
        this.version = version == null ? "" : version;
    }

    public NIdBoot(String groupId, String artifactId, String version, String classifier, String propertiesQuery, NEnvConditionBoot condition) {
        this.groupId = NStringUtilsBoot.trimToNull(groupId);
        this.artifactId = NStringUtilsBoot.trimToNull(artifactId);
        this.version = version == null ? "" : version;

        setCondition(condition);
        String c0 = NStringUtilsBoot.trimToNull(classifier);
        String c1 = null;
        Map<String, String> properties = propertiesQuery == null ? new LinkedHashMap<>() : NStringMapFormatBoot.DEFAULT.parse(propertiesQuery);
        if (!properties.isEmpty()) {
            c1 = properties.remove(NConstants.IdProperties.CLASSIFIER);
        }
        if (c0 == null) {
            if (c1 != null) {
                c0 = NStringUtilsBoot.trimToNull(c1);
            }
        }
        this.classifier = c0;
        setProperties(properties);
    }

    public static NIdBoot of(String value) {
        return NUtilsBoot.parseId(value);
    }


    public NIdBoot setAll(NIdBoot id) {
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


    public NIdBoot clear() {
        setGroupId(null);
        setArtifactId(null);
        setVersion(null);
        setPropertiesQuery("");
        return this;
    }

    public NIdBoot setGroupId(String value) {
        this.groupId = NStringUtilsBoot.trimToNull(value);
        return this;
    }


    public NIdBoot setRepository(String value) {
        return setProperty(NConstants.IdProperties.REPO, NStringUtilsBoot.trimToNull(value));
    }


    public NIdBoot setVersion(String value) {
        this.version = value == null ? "" : value;
        return this;
    }


    public NIdBoot setArtifactId(String value) {
        this.artifactId = NStringUtilsBoot.trimToNull(value);
        return this;
    }


    public String getFace() {
        String s = getProperties().get(NConstants.IdProperties.FACE);
        return NStringUtilsBoot.trimToNull(s);
    }

//    
//    public String getAlternative() {
//        String s = getProperties().get(NutsConstants.IdProperties.ALTERNATIVE);
//        return NutsUtilStrings.trimToNull(s);
//    }


    public String getPackaging() {
        String s = getProperties().get(NConstants.IdProperties.PACKAGING);
        return NStringUtilsBoot.trimToNull(s);
    }


    public NIdBoot setFace(String value) {
        return setProperty(NConstants.IdProperties.FACE, NStringUtilsBoot.trimToNull(value));
//                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
    }


    public NIdBoot setFaceContent() {
        return setFace(NConstants.QueryFaces.CONTENT);
    }


    public NIdBoot setFaceDescriptor() {
        return setFace(NConstants.QueryFaces.DESCRIPTOR);
    }

//    
//    public NutsIdBuilder setAlternative(String value) {
//        return setProperty(NutsConstants.IdProperties.ALTERNATIVE, NutsUtilStrings.trimToNull(value));

    /// /                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
//    }
    public String getClassifier() {
        return classifier;
    }


    public NIdBoot setClassifier(String value) {
        this.classifier = NStringUtilsBoot.trimToNull(value);
        return this;
    }


    public NIdBoot setPackaging(String value) {
        return setProperty(NConstants.IdProperties.PACKAGING, NStringUtilsBoot.trimToNull(value));
    }


    public NIdBoot setCondition(NEnvConditionBoot c) {
        if (c == null) {
            setProperty(NConstants.IdProperties.OS, null);
            setProperty(NConstants.IdProperties.OS_DIST, null);
            setProperty(NConstants.IdProperties.ARCH, null);
            setProperty(NConstants.IdProperties.PLATFORM, null);
            setProperty(NConstants.IdProperties.DESKTOP, null);
            setProperty(NConstants.IdProperties.PROFILE, null);
            condition.setProperties(null);
        } else {
            setProperty(NConstants.IdProperties.OS, NReservedLangUtilsBoot.joinAndTrimToNull(c.getOs()));
            setProperty(NConstants.IdProperties.OS_DIST, NReservedLangUtilsBoot.joinAndTrimToNull(c.getOsDist()));
            setProperty(NConstants.IdProperties.ARCH, NReservedLangUtilsBoot.joinAndTrimToNull(c.getArch()));
            setProperty(NConstants.IdProperties.PLATFORM, NUtilsBoot.formatStringIdList(c.getPlatform()));
            setProperty(NConstants.IdProperties.DESKTOP, NReservedLangUtilsBoot.joinAndTrimToNull(c.getDesktopEnvironment()));
            setProperty(NConstants.IdProperties.PROFILE, NReservedLangUtilsBoot.joinAndTrimToNull(c.getProfiles()));
            condition.setProperties(c.getProperties());

        }
        return this;
    }


    public NEnvConditionBoot getCondition() {
        return condition;
    }

    public NIdBoot setProperty(String property, String value) {
        switch (property) {
            case NConstants.IdProperties.OS: {
                condition.setOs(NStringUtilsBoot.parsePropertyIdList(value));
                break;
            }
            case NConstants.IdProperties.OS_DIST: {
                condition.setOsDist(NStringUtilsBoot.parsePropertyIdList(value));
                break;
            }
            case NConstants.IdProperties.ARCH: {
                condition.setArch(NStringUtilsBoot.parsePropertyIdList(value));
                break;
            }
            case NConstants.IdProperties.PLATFORM: {
                condition.setPlatform(NStringUtilsBoot.parsePropertyIdList(value));
                break;
            }
            case NConstants.IdProperties.DESKTOP: {
                condition.setDesktopEnvironment(NStringUtilsBoot.parsePropertyIdList(value));
                break;
            }
            case NConstants.IdProperties.PROFILE: {
                condition.setProfile(NStringUtilsBoot.parsePropertyIdList(value));
                break;
            }
            case NConstants.IdProperties.CONDITIONAL_PROPERTIES: {
                condition.setProperties(NStringMapFormatBoot.DEFAULT.parse(value));
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


    public NIdBoot setProperties(Map<String, String> queryMap) {
        for (Map.Entry<String, String> e : queryMap.entrySet()) {
            setProperty(e.getKey(), e.getValue());
        }
        return this;
    }


    public NIdBoot clearProperties() {
        properties.clear();
        return this;
    }


    public NIdBoot setPropertiesQuery(String propertiesQuery) {
        setProperties(NStringMapFormatBoot.DEFAULT.parse(propertiesQuery));
        return this;
    }


    public String getPropertiesQuery() {
        return NStringMapFormatBoot.DEFAULT.format(getProperties());
    }


    public Map<String, String> getProperties() {
        return properties;
    }


    public String getRepository() {
        return NStringUtilsBoot.trimToNull(getProperties().get(NConstants.IdProperties.REPO));
    }


    public String getGroupId() {
        return groupId;
    }


    public String getFullName() {
        return toString();
    }


    public String getShortName() {
        return NUtilsBoot.getIdShortName(groupId, artifactId);
    }


    public String getLongName() {
        return NUtilsBoot.getIdLongName(groupId, artifactId, version, classifier);
    }


    public String getArtifactId() {
        return artifactId;
    }


    public String getVersion() {
        return version;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!NStringUtilsBoot.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NStringUtilsBoot.trim(artifactId));
        String v = getVersion();
        if (!NStringUtilsBoot.isBlank(v)) {
            sb.append("#");
            sb.append(v);
        }
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        if (!NStringUtilsBoot.isBlank(classifier)) {
            m.put(NConstants.IdProperties.CLASSIFIER, classifier);
        }
        m.putAll(NUtilsBoot.toMap(condition));
        if (properties != null) {
            for (Map.Entry<String, String> e : properties.entrySet()) {
                if (!m.containsKey(e.getKey())) {
                    m.put(e.getKey(), e.getValue());
                }
            }
        }
        if (!m.isEmpty()) {
            sb.append("?").append(NStringMapFormatBoot.DEFAULT.format(m));
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

        NIdBoot nutsId = (NIdBoot) o;

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

    public boolean equalsShortId(NIdBoot other) {
        if (other == null) {
            return false;
        }
        return NStringUtilsBoot.trim(groupId).equals(NStringUtilsBoot.trim(other.getArtifactId()))
                && NStringUtilsBoot.trim(artifactId).equals(NStringUtilsBoot.trim(other.getGroupId()));
    }


    public boolean equalsLongId(NIdBoot other) {
        if (other == null) {
            return false;
        }
        return NStringUtilsBoot.trim(artifactId).equals(NStringUtilsBoot.trim(other.getArtifactId()))
                && NStringUtilsBoot.trim(groupId).equals(NStringUtilsBoot.trim(other.getGroupId()))
                && Objects.equals((version == null || NStringUtilsBoot.isBlank(version)) ? null : version,
                (other.getVersion() == null || NStringUtilsBoot.isBlank(other.getVersion())) ? null : other.getVersion())
                && Objects.equals(getClassifier(), other.getClassifier())
                ;
    }


    public boolean isLongId() {
        if (properties == null || properties.isEmpty()) {
            return true;
        }
        Map<String, String> m = new HashMap<>(getProperties());
        m.remove(NConstants.IdProperties.CLASSIFIER);
        return m.isEmpty();
    }


    public boolean isShortId() {
        return (properties == null || properties.isEmpty())
                && NStringUtilsBoot.isBlank(version)
                && NStringUtilsBoot.isBlank(classifier)
                ;
    }


    public NIdBoot getShortId() {
        return new NIdBoot(groupId, artifactId, null, null, "",
                NEnvConditionBoot.BLANK);
    }


    public NIdBoot getLongId() {
        return new NIdBoot(groupId, artifactId, version, classifier, "", NEnvConditionBoot.BLANK);
    }


    public NIdBoot copy() {
        return new NIdBoot(this);
    }


    public NDependencyBoot toDependency() {
        Map<String, String> properties = getProperties();
        //CoreStringUtils.join(",", ex)
        String exc = properties.get(NConstants.IdProperties.EXCLUSIONS);
        if (exc == null) {
            exc = "";
        }
        List<NIdBoot> a = new ArrayList<>();
        for (String s : NReservedLangUtilsBoot.splitDefault(exc)) {
            NIdBoot n = NIdBoot.of(s);
            if (n != null) {
                a.add(n);
            }
        }
        return new NDependencyBoot()
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


    public int compareTo(NIdBoot o2) {
        int x;
        x = NStringUtilsBoot.trim(this.getGroupId()).compareTo(NStringUtilsBoot.trim(o2.getGroupId()));
        if (x != 0) {
            return x;
        }
        x = NStringUtilsBoot.trim(this.getArtifactId()).compareTo(NStringUtilsBoot.trim(o2.getArtifactId()));
        if (x != 0) {
            return x;
        }
        x = NStringUtilsBoot.trim(this.getClassifier()).compareTo(NStringUtilsBoot.trim(o2.getClassifier()));
        if (x != 0) {
            return x;
        }
        x = NVersionBoot.of(this.getVersion()).compareTo(NVersionBoot.of(o2.getVersion()));
        if (x != 0) {
            return x;
        }
        return 0;
    }


}
