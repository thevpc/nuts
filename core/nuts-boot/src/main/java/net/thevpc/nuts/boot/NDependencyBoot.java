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
 * <br> ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.boot.reserved.util.NReservedLangUtilsBoot;
import net.thevpc.nuts.boot.reserved.util.NReservedUtilsBoot;
import net.thevpc.nuts.boot.reserved.util.NStringMapFormatBoot;
import net.thevpc.nuts.boot.reserved.util.NUtilsBoot;
import net.thevpc.nuts.boot.reserved.util.NStringUtilsBoot;

import java.util.*;

/**
 * Created by vpc on 1/5/17.
 */
public class NDependencyBoot {
    private static NStringMapFormatBoot COMMA_MAP = NStringMapFormatBoot.COMMA_FORMAT.copy(null,null,"&",null);

    private String repository;
    private String groupId;
    private String artifactId;
    private String version;
    private String scope;
    private String optional;
    private String type;
    private NEnvConditionBoot condition = new NEnvConditionBoot();
    private String classifier;
    private List<NIdBoot> exclusions = new ArrayList<>();
    private Map<String, String> properties = new LinkedHashMap<>();


    public NDependencyBoot() {

    }

    public NDependencyBoot(String groupId, String artifactId) {
        this.groupId = NStringUtilsBoot.trimToNull(groupId);
        this.artifactId = NStringUtilsBoot.trimToNull(artifactId);
    }
    public NDependencyBoot(String repository, String groupId, String artifactId, String classifier, String version, String scope, String optional, List<NIdBoot> exclusions,
                              NEnvConditionBoot condition, String type,
                              String properties) {
        this.repository = NStringUtilsBoot.trimToNull(repository);
        this.groupId = NStringUtilsBoot.trimToNull(groupId);
        this.artifactId = NStringUtilsBoot.trimToNull(artifactId);
        this.version = version == null ? "" : version;
        this.classifier = NStringUtilsBoot.trimToNull(classifier);
        this.scope = NUtilsBoot.firstNonNull(scope,"API");

        String o = NStringUtilsBoot.trimToNull(optional);
        if ("false".equalsIgnoreCase(o)) {
            o = null;
        } else if ("true".equalsIgnoreCase(o)) {
            o = "true";//remove case and formatting
        }
        this.optional = o;
        this.exclusions = NReservedLangUtilsBoot.unmodifiableList(exclusions);
        for (NIdBoot exclusion : this.exclusions) {
            if (exclusion == null) {
                throw new NullPointerException();
            }
        }
        this.condition = condition == null ? NEnvConditionBoot.BLANK : condition;
        this.type = NStringUtilsBoot.trimToNull(type);
        this.properties = NStringMapFormatBoot.DEFAULT.parse(properties);
    }

    public NDependencyBoot setId(NIdBoot id) {
        if (id == null) {
            setRepository(null);
            setGroupId(null);
            setArtifactId(null);
            setVersion((String) null);
        } else {
            setRepository(id.getRepository());
            setGroupId(id.getGroupId());
            setArtifactId(id.getArtifactId());
            setVersion(id.getVersion());
            this.setProperties(id.getProperties());
        }
        return this;
    }

    
    public NDependencyBoot setDependency(NDependencyBoot value) {
        return setAll(value);
    }

    
    public NDependencyBoot setAll(NDependencyBoot value) {
        if (value != null) {
            setRepository(value.getRepository());
            setGroupId(value.getGroupId());
            setArtifactId(value.getArtifactId());
            setVersion(value.getVersion());
            setScope(value.getScope());
            setOptional(value.getOptional());
            setExclusions(value.getExclusions());
            setClassifier(value.getClassifier());
            getCondition().setAll(value.getCondition());
            setType(value.getType());
            setProperties(value.getProperties());
        } else {
            clear();
        }
        return this;
    }


    public NDependencyBoot clear() {
        setRepository(null);
        setGroupId(null);
        setArtifactId(null);
        setVersion( null);
        setScope(null);
        setOptional(null);
        setExclusions((List<NIdBoot>) null);
        setClassifier(null);
        getCondition().clear();
        setType(null);
        setProperties((Map<String, String>) null);
        return this;
    }

    
    public boolean isOptional() {
        return optional != null && Boolean.parseBoolean(optional);
    }

    public String getType() {
        return type;
    }

    
    public NDependencyBoot setType(String type) {
        this.type = NStringUtilsBoot.trimToNull(type);
        return this;
    }

    
    public String getOptional() {
        return optional;
    }

    
    public NDependencyBoot setOptional(String optional) {
        String o = NStringUtilsBoot.trimToNull(optional);
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

    
    public NDependencyBoot setScope(String scope) {
        this.scope = scope == null ? "" : scope;
        return this;
    }

    
    public NIdBoot toId() {
        Map<String, String> m = new LinkedHashMap<>();
        if (!NReservedUtilsBoot.isDependencyDefaultScope(scope)) {
            m.put(NConstants.IdProperties.SCOPE, scope);
        }
        if (!NStringUtilsBoot.isBlank(optional) && !"false".equals(optional)) {
            m.put(NConstants.IdProperties.OPTIONAL, optional);
        }
        if (!NStringUtilsBoot.isBlank(classifier)) {
            m.put(NConstants.IdProperties.CLASSIFIER, classifier);
        }
        if (!NStringUtilsBoot.isBlank(type)) {
            m.put(NConstants.IdProperties.TYPE, type);
        }
        if (exclusions.size() > 0) {
            m.put(NConstants.IdProperties.EXCLUSIONS, NReservedUtilsBoot.toDependencyExclusionListString(exclusions));
        }
        return new NIdBoot()
                .setRepository(getRepository())
                .setGroupId(getGroupId())
                .setArtifactId(getArtifactId())
                .setVersion(getVersion())
                .setCondition(getCondition())
                .setProperties(m)
                ;
    }

    
    public String getRepository() {
        return repository;
    }

    
    public NDependencyBoot setRepository(String repository) {
        this.repository = NStringUtilsBoot.trimToNull(repository);
        return this;
    }

    
    public String getGroupId() {
        return groupId;
    }

    
    public NDependencyBoot setGroupId(String groupId) {
        this.groupId = NStringUtilsBoot.trimToNull(groupId);
        return this;
    }

    
    public String getArtifactId() {
        return artifactId;
    }

    
    public NDependencyBoot setArtifactId(String artifactId) {
        this.artifactId = NStringUtilsBoot.trimToNull(artifactId);
        return this;
    }

    
    public String getClassifier() {
        return classifier;
    }

    
    public NDependencyBoot setClassifier(String classifier) {
        this.classifier = NStringUtilsBoot.trimToNull(classifier);
        return this;
    }

    
    public String getFullName() {
        if (NStringUtilsBoot.isBlank(groupId)) {
            return NStringUtilsBoot.trim(artifactId);
        }
        return NStringUtilsBoot.trim(groupId) + ":" + NStringUtilsBoot.trim(artifactId);
    }

    
    public String getVersion() {
        return version;
    }

    
    public NDependencyBoot setVersion(String version) {
        this.version = version == null ? "" : version;
        return this;
    }

    
    public List<NIdBoot> getExclusions() {
        return exclusions;
    }

    
    public NDependencyBoot setExclusions(List<NIdBoot> exclusions) {
        this.exclusions = NReservedLangUtilsBoot.nonNullList(exclusions);
        return this;
    }

    
    public NDependencyBoot copy() {
        return new NDependencyBoot(
                getRepository(), getGroupId(), getArtifactId(), getClassifier(),
                getVersion(),
                getScope(),
                getOptional(),
                getExclusions(),
                getCondition(),
                getType(),
                getPropertiesQuery()
        );
    }

    
    public NDependencyBoot setProperty(String property, String value) {
        if (property != null) {
            switch (property) {
                case NConstants.IdProperties.SCOPE: {
                    setScope(value);
                    break;
                }
                case NConstants.IdProperties.VERSION: {
                    setVersion(value);
                    break;
                }
                case NConstants.IdProperties.OPTIONAL: {
                    setOptional(value);
                    break;
                }
                case NConstants.IdProperties.CLASSIFIER: {
                    setClassifier(value);
                    break;
                }
                case NConstants.IdProperties.REPO: {
                    setRepository(value);
                    break;
                }
                case NConstants.IdProperties.EXCLUSIONS: {
                    setExclusions(value);
                    break;
                }
                case NConstants.IdProperties.OS: {
                    condition.setOs(NStringUtilsBoot.parsePropertyIdList(value));
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
                case NConstants.IdProperties.OS_DIST: {
                    condition.setOsDist(NStringUtilsBoot.parsePropertyIdList(value));
                    break;
                }
                case NConstants.IdProperties.DESKTOP: {
                    condition.setDesktopEnvironment(NStringUtilsBoot.parsePropertyIdList(value));
                    break;
                }
                case NConstants.IdProperties.TYPE: {
                    setType(value);
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

    
    public NDependencyBoot addPropertiesQuery(String propertiesQuery) {
        return addProperties(NStringMapFormatBoot.DEFAULT.parse(propertiesQuery));
    }

    
    public NDependencyBoot addProperties(Map<String, String> queryMap) {
        if (queryMap != null) {
            for (Map.Entry<String, String> e : queryMap.entrySet()) {
                setProperty(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    
    public NDependencyBoot setProperties(Map<String, String> queryMap) {
        properties.clear();
        if (queryMap != null) {
            for (Map.Entry<String, String> e : queryMap.entrySet()) {
                setProperty(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    
    public NDependencyBoot setPropertiesQuery(String propertiesQuery) {
        setProperties(NStringMapFormatBoot.DEFAULT.parse(propertiesQuery));
        return this;
    }

    
    public String getPropertiesQuery() {
        return NStringMapFormatBoot.DEFAULT.format(properties);
    }

    
    public Map<String, String> getProperties() {
        return properties;
    }


    public NEnvConditionBoot getCondition() {
        return condition;
    }

    
    public NDependencyBoot setCondition(NEnvConditionBoot condition) {
        this.condition.setAll(condition);
        return this;
    }

    public NDependencyBoot setExclusions(String exclusions) {
        if (exclusions == null) {
            exclusions = "";
        }
        List<NIdBoot> ids = new ArrayList<>();
        for (String s : NReservedLangUtilsBoot.splitDefault(exclusions)) {
            NIdBoot ii = NIdBoot.of(s);
            if (ii != null) {
                ids.add(ii);
            }
        }
        setExclusions(ids);
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!NStringUtilsBoot.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(artifactId);
        if (!NStringUtilsBoot.isBlank(version)) {
            sb.append("#").append(version);
        }
        Map<String, String> p = new HashMap<>();
        if (!NStringUtilsBoot.isBlank(classifier)) {
            p.put(NConstants.IdProperties.CLASSIFIER, classifier);
        }
        if (!NStringUtilsBoot.isBlank(repository)) {
            p.put(NConstants.IdProperties.REPO, repository);
        }
        if (!NStringUtilsBoot.isBlank(scope)) {
            if (!scope.equals("api")) {
                p.put(NConstants.IdProperties.SCOPE, scope);
            }
        }
        if (!NStringUtilsBoot.isBlank(optional)) {
            if (!optional.equals("false")) {
                p.put(NConstants.IdProperties.OPTIONAL, optional);
            }
        }
        if (!NStringUtilsBoot.isBlank(type)) {
            p.put(NConstants.IdProperties.TYPE, type);
        }
        if (condition != null && !condition.isBlank()) {
            if (condition.getOs().size() > 0) {
                p.put(NConstants.IdProperties.OS, String.join(",", condition.getOs()));
            }
            if (condition.getOsDist().size() > 0) {
                p.put(NConstants.IdProperties.OS_DIST, String.join(",", condition.getOsDist()));
            }
            if (condition.getDesktopEnvironment().size() > 0) {
                p.put(NConstants.IdProperties.DESKTOP, String.join(",", condition.getDesktopEnvironment()));
            }
            if (condition.getArch().size() > 0) {
                p.put(NConstants.IdProperties.ARCH, String.join(",", condition.getArch()));
            }
            if (condition.getPlatform().size() > 0) {
                p.put(NConstants.IdProperties.PLATFORM, NReservedUtilsBoot.formatStringIdList(condition.getPlatform()));
            }
            if (condition.getProfiles().size() > 0) {
                p.put(NConstants.IdProperties.PROFILE, String.join(",", condition.getProfiles()));
            }
            if (!condition.getProperties().isEmpty()) {
                p.put(NConstants.IdProperties.CONDITIONAL_PROPERTIES,
                        COMMA_MAP.format(condition.getProperties())
                );
            }
        }
        if (exclusions.size() > 0) {
            p.put(NConstants.IdProperties.EXCLUSIONS,
                    NReservedUtilsBoot.toDependencyExclusionListString(exclusions)
            );
        }
        if (!p.isEmpty()) {
            sb.append("?");
            sb.append(NStringMapFormatBoot.DEFAULT.format(p));
        }
        return sb.toString();
    }

    public String getSimpleName() {
        return NUtilsBoot.getIdShortName(groupId, artifactId);
    }

    public String getLongName() {
        return NUtilsBoot.getIdLongName(groupId, artifactId, version, classifier);
    }

    public boolean isBlank(){
        return toString().isEmpty();
    }

}
