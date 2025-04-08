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

import net.thevpc.nuts.boot.reserved.util.NBootConstants;
import net.thevpc.nuts.boot.reserved.util.NBootStringMapFormat;
import net.thevpc.nuts.boot.reserved.util.NBootUtils;

import java.util.*;

/**
 * Created by vpc on 1/5/17.
 */
public class NBootDependency {
    private static NBootStringMapFormat COMMA_MAP = NBootStringMapFormat.COMMA_FORMAT.copy(null,null,"&",null);

    private String repository;
    private String groupId;
    private String artifactId;
    private String version;
    private String scope;
    private String optional;
    private String type;
    private NBootEnvCondition condition = new NBootEnvCondition();
    private String classifier;
    private List<NBootId> exclusions = new ArrayList<>();
    private Map<String, String> properties = new LinkedHashMap<>();


    public NBootDependency() {

    }

    public NBootDependency(String groupId, String artifactId) {
        this.groupId = NBootUtils.trimToNull(groupId);
        this.artifactId = NBootUtils.trimToNull(artifactId);
    }
    public NBootDependency(String repository, String groupId, String artifactId, String classifier, String version, String scope, String optional, List<NBootId> exclusions,
                           NBootEnvCondition condition, String type,
                           String properties) {
        this.repository = NBootUtils.trimToNull(repository);
        this.groupId = NBootUtils.trimToNull(groupId);
        this.artifactId = NBootUtils.trimToNull(artifactId);
        this.version = version == null ? "" : version;
        this.classifier = NBootUtils.trimToNull(classifier);
        this.scope = NBootUtils.firstNonNull(scope,"API");

        String o = NBootUtils.trimToNull(optional);
        if ("false".equalsIgnoreCase(o)) {
            o = null;
        } else if ("true".equalsIgnoreCase(o)) {
            o = "true";//remove case and formatting
        }
        this.optional = o;
        this.exclusions = NBootUtils.unmodifiableList(exclusions);
        for (NBootId exclusion : this.exclusions) {
            if (exclusion == null) {
                throw new NullPointerException();
            }
        }
        this.condition = condition == null ? NBootEnvCondition.BLANK : condition;
        this.type = NBootUtils.trimToNull(type);
        this.properties = NBootStringMapFormat.DEFAULT.parse(properties);
    }

    public NBootDependency setId(NBootId id) {
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
            getCondition().clear().copyFrom(value.getCondition());
            setType(value.getType());
            setProperties(value.getProperties());
        } else {
            clear();
        }
        return this;
    }


    public NBootDependency clear() {
        setRepository(null);
        setGroupId(null);
        setArtifactId(null);
        setVersion( null);
        setScope(null);
        setOptional(null);
        setExclusions((List<NBootId>) null);
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

    
    public NBootId toId() {
        Map<String, String> m = new LinkedHashMap<>();
        if (!NBootUtils.isDependencyDefaultScope(scope)) {
            m.put(NBootConstants.IdProperties.SCOPE, scope);
        }
        if (!NBootUtils.isBlank(optional) && !"false".equals(optional)) {
            m.put(NBootConstants.IdProperties.OPTIONAL, optional);
        }
        if (!NBootUtils.isBlank(classifier)) {
            m.put(NBootConstants.IdProperties.CLASSIFIER, classifier);
        }
        if (!NBootUtils.isBlank(type)) {
            m.put(NBootConstants.IdProperties.TYPE, type);
        }
        if (exclusions.size() > 0) {
            m.put(NBootConstants.IdProperties.EXCLUSIONS, NBootUtils.toDependencyExclusionListString(exclusions));
        }
        return new NBootId()
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

    
    public List<NBootId> getExclusions() {
        return exclusions;
    }

    
    public NBootDependency setExclusions(List<NBootId> exclusions) {
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
                getCondition(),
                getType(),
                getPropertiesQuery()
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
                case NBootConstants.IdProperties.CLASSIFIER: {
                    setClassifier(value);
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
                    condition.setOs(NBootUtils.parsePropertyIdList(value));
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
                case NBootConstants.IdProperties.OS_DIST: {
                    condition.setOsDist(NBootUtils.parsePropertyIdList(value));
                    break;
                }
                case NBootConstants.IdProperties.DESKTOP: {
                    condition.setDesktopEnvironment(NBootUtils.parsePropertyIdList(value));
                    break;
                }
                case NBootConstants.IdProperties.TYPE: {
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


    public NBootEnvCondition getCondition() {
        return condition;
    }

    
    public NBootDependency setCondition(NBootEnvCondition condition) {
        this.condition.clear().copyFrom(condition);
        return this;
    }

    public NBootDependency setExclusions(String exclusions) {
        if (exclusions == null) {
            exclusions = "";
        }
        List<NBootId> ids = new ArrayList<>();
        for (String s : NBootUtils.splitDefault(exclusions)) {
            NBootId ii = NBootId.of(s);
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
        if (!NBootUtils.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(artifactId);
        if (!NBootUtils.isBlank(version)) {
            sb.append("#").append(version);
        }
        Map<String, String> p = new HashMap<>();
        if (!NBootUtils.isBlank(classifier)) {
            p.put(NBootConstants.IdProperties.CLASSIFIER, classifier);
        }
        if (!NBootUtils.isBlank(repository)) {
            p.put(NBootConstants.IdProperties.REPO, repository);
        }
        if (!NBootUtils.isBlank(scope)) {
            if (!scope.equals("api")) {
                p.put(NBootConstants.IdProperties.SCOPE, scope);
            }
        }
        if (!NBootUtils.isBlank(optional)) {
            if (!optional.equals("false")) {
                p.put(NBootConstants.IdProperties.OPTIONAL, optional);
            }
        }
        if (!NBootUtils.isBlank(type)) {
            p.put(NBootConstants.IdProperties.TYPE, type);
        }
        if (condition != null && !condition.isBlank()) {
            if (condition.getOs().size() > 0) {
                p.put(NBootConstants.IdProperties.OS, String.join(",", condition.getOs()));
            }
            if (condition.getOsDist().size() > 0) {
                p.put(NBootConstants.IdProperties.OS_DIST, String.join(",", condition.getOsDist()));
            }
            if (condition.getDesktopEnvironment().size() > 0) {
                p.put(NBootConstants.IdProperties.DESKTOP, String.join(",", condition.getDesktopEnvironment()));
            }
            if (condition.getArch().size() > 0) {
                p.put(NBootConstants.IdProperties.ARCH, String.join(",", condition.getArch()));
            }
            if (condition.getPlatform().size() > 0) {
                p.put(NBootConstants.IdProperties.PLATFORM, NBootUtils.formatStringIdList(condition.getPlatform()));
            }
            if (condition.getProfiles().size() > 0) {
                p.put(NBootConstants.IdProperties.PROFILE, String.join(",", condition.getProfiles()));
            }
            if (!condition.getProperties().isEmpty()) {
                p.put(NBootConstants.IdProperties.CONDITIONAL_PROPERTIES,
                        COMMA_MAP.format(condition.getProperties())
                );
            }
        }
        if (exclusions.size() > 0) {
            p.put(NBootConstants.IdProperties.EXCLUSIONS,
                    NBootUtils.toDependencyExclusionListString(exclusions)
            );
        }
        if (!p.isEmpty()) {
            sb.append("?");
            sb.append(NBootStringMapFormat.DEFAULT.format(p));
        }
        return sb.toString();
    }

    public String getSimpleName() {
        return NBootUtils.getIdShortName(groupId, artifactId);
    }

    public String getLongName() {
        return NBootUtils.getIdLongName(groupId, artifactId, version, classifier);
    }

    public boolean isBlank(){
        return toString().isEmpty();
    }

}
