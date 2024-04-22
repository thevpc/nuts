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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringMapFormat;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNDependencyBuilder implements NDependencyBuilder {

    private String repo;
    private String groupId;
    private String artifactId;
    private NVersion version;
    private String scope;
    private String optional;
    private String type;
    private NEnvConditionBuilder condition = new DefaultNEnvConditionBuilder();
    private String classifier;
    private List<NId> exclusions = new ArrayList<>();
    private Map<String, String> properties = new LinkedHashMap<>();


    public DefaultNDependencyBuilder(NDependency d) {
        setAll(d);
    }

    public DefaultNDependencyBuilder() {

    }

    public DefaultNDependencyBuilder(String groupId, String artifactId) {
        this.groupId = NStringUtils.trimToNull(groupId);
        this.artifactId = NStringUtils.trimToNull(artifactId);
    }

    @Override
    public NDependencyBuilder setId(NId id) {
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

    @Override
    public NDependencyBuilder setDependency(NDependencyBuilder value) {
        return setAll(value);
    }

    @Override
    public NDependencyBuilder setAll(NDependencyBuilder value) {
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

    @Override
    public NDependencyBuilder setAll(NDependency value) {
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

    @Override
    public NDependencyBuilder setDependency(NDependency value) {
        return setAll(value);
    }

    @Override
    public NDependencyBuilder clear() {
        setRepository(null);
        setGroupId(null);
        setArtifactId(null);
        setVersion((NVersion) null);
        setScope((String) null);
        setOptional(null);
        setExclusions((List<NId>) null);
        setClassifier(null);
        getCondition().clear();
        setType(null);
        setProperties((Map<String, String>) null);
        return this;
    }

    @Override
    public boolean isOptional() {
        return optional != null && Boolean.parseBoolean(optional);
    }

    public String getType() {
        return type;
    }

    @Override
    public NDependencyBuilder setType(String type) {
        this.type = NStringUtils.trimToNull(type);
        return this;
    }

    @Override
    public String getOptional() {
        return optional;
    }

    @Override
    public NDependencyBuilder setOptional(String optional) {
        String o = NStringUtils.trimToNull(optional);
        if ("false".equals(o)) {
            o = null;
        } else if ("true".equalsIgnoreCase(o)) {
            o = "true";//remove case and formatting
        }
        this.optional = o;
        return this;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public NDependencyBuilder setScope(NDependencyScope scope) {
        this.scope = scope == null ? "" : scope.toString();
        return this;
    }

    @Override
    public NDependencyBuilder setScope(String scope) {
        this.scope = NDependencyScope.parse(scope).orElse(NDependencyScope.API).id();
        return this;
    }

    @Override
    public NId toId() {
        Map<String, String> m = new LinkedHashMap<>();
        if (!NReservedUtils.isDependencyDefaultScope(scope)) {
            m.put(NConstants.IdProperties.SCOPE, scope);
        }
        if (!NBlankable.isBlank(optional) && !"false".equals(optional)) {
            m.put(NConstants.IdProperties.OPTIONAL, optional);
        }
        if (!NBlankable.isBlank(classifier)) {
            m.put(NConstants.IdProperties.CLASSIFIER, classifier);
        }
        if (!NBlankable.isBlank(type)) {
            m.put(NConstants.IdProperties.TYPE, type);
        }
        if (exclusions.size() > 0) {
            m.put(NConstants.IdProperties.EXCLUSIONS, NReservedUtils.toDependencyExclusionListString(exclusions));
        }
        return NIdBuilder.of()
                .setRepository(getRepository())
                .setGroupId(getGroupId())
                .setArtifactId(getArtifactId())
                .setVersion(getVersion())
                .setCondition(getCondition())
                .setProperties(m).build()
                ;
    }

    @Override
    public String getRepository() {
        return repo;
    }

    @Override
    public NDependencyBuilder setRepository(String repository) {
        this.repo = NStringUtils.trimToNull(repository);
        return this;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public NDependencyBuilder setGroupId(String groupId) {
        this.groupId = NStringUtils.trimToNull(groupId);
        return this;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public NDependencyBuilder setArtifactId(String artifactId) {
        this.artifactId = NStringUtils.trimToNull(artifactId);
        return this;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public NDependencyBuilder setClassifier(String classifier) {
        this.classifier = NStringUtils.trimToNull(classifier);
        return this;
    }

    @Override
    public String getFullName() {
        if (NBlankable.isBlank(groupId)) {
            return NStringUtils.trim(artifactId);
        }
        return NStringUtils.trim(groupId) + ":" + NStringUtils.trim(artifactId);
    }

    @Override
    public NVersion getVersion() {
        return version;
    }

    @Override
    public NDependencyBuilder setVersion(NVersion version) {
        this.version = version == null ? NVersion.BLANK : version;
        return this;
    }

    @Override
    public NDependencyBuilder setVersion(String version) {
        this.version = NVersion.of(version).get();
        return this;
    }

    @Override
    public List<NId> getExclusions() {
        return exclusions;
    }

    @Override
    public NDependencyBuilder setExclusions(List<NId> exclusions) {
        this.exclusions = NReservedLangUtils.nonNullList(exclusions);
        return this;
    }

    @Override
    public NDependency build() {
        return new DefaultNDependency(
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

    @Override
    public NDependency copy() {
        return builder();
    }

    @Override
    public NDependencyBuilder setProperty(String property, String value) {
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
                    condition.setOs(NStringUtils.parsePropertyIdList(value).get());
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
                case NConstants.IdProperties.OS_DIST: {
                    condition.setOsDist(NStringUtils.parsePropertyIdList(value).get());
                    break;
                }
                case NConstants.IdProperties.DESKTOP: {
                    condition.setDesktopEnvironment(NStringUtils.parsePropertyIdList(value).get());
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

    @Override
    public NDependencyBuilder addPropertiesQuery(String propertiesQuery) {
        return addProperties(NStringMapFormat.DEFAULT.parse(propertiesQuery).get());
    }

    @Override
    public NDependencyBuilder addProperties(Map<String, String> queryMap) {
        if (queryMap != null) {
            for (Map.Entry<String, String> e : queryMap.entrySet()) {
                setProperty(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    @Override
    public NDependencyBuilder setProperties(Map<String, String> queryMap) {
        properties.clear();
        if (queryMap != null) {
            for (Map.Entry<String, String> e : queryMap.entrySet()) {
                setProperty(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    @Override
    public NDependencyBuilder setPropertiesQuery(String propertiesQuery) {
        setProperties(NStringMapFormat.DEFAULT.parse(propertiesQuery).get());
        return this;
    }

    @Override
    public String getPropertiesQuery() {
        return NStringMapFormat.DEFAULT.format(properties);
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }


    public NEnvConditionBuilder getCondition() {
        return condition;
    }

    @Override
    public NDependencyBuilder setCondition(NEnvCondition condition) {
        this.condition.setAll(condition);
        return this;
    }

    public NDependencyBuilder setExclusions(String exclusions) {
        if (exclusions == null) {
            exclusions = "";
        }
        List<NId> ids = new ArrayList<>();
        for (String s : NReservedLangUtils.splitDefault(exclusions)) {
            NId ii = NId.of(s).orNull();
            if (ii != null) {
                ids.add(ii);
            }
        }
        setExclusions(ids);
        return this;
    }

    @Override
    public String toString() {
        return build().toString();
    }

    @Override
    public NDependencyBuilder builder() {
        return new DefaultNDependencyBuilder().setAll(this);
    }

    @Override
    public boolean isBlank() {
        return build().isBlank();
    }

    @Override
    public String getSimpleName() {
        return NReservedUtils.getIdShortName(groupId, artifactId);
    }

    @Override
    public String getLongName() {
        return NReservedUtils.getIdLongName(groupId, artifactId, version, classifier);
    }

    @Override
    public NDependencyFormat formatter(NSession session) {
        return build().formatter(session);
    }

}
