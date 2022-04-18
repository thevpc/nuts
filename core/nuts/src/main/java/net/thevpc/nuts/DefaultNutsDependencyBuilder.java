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

import net.thevpc.nuts.boot.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsDependencyBuilder implements NutsDependencyBuilder {

    private String repo;
    private String groupId;
    private String artifactId;
    private NutsVersion version;
    private String scope;
    private String optional;
    private String type;
    private NutsEnvConditionBuilder condition;
    private String classifier;
    private List<NutsId> exclusions = new ArrayList<>();
    private final transient PrivateNutsQueryStringParser propertiesQuery = new PrivateNutsQueryStringParser(true, (name, value) -> {
        if (name != null) {
            switch (name) {
                case NutsConstants.IdProperties.SCOPE: {
                    setScope(value);
                    return true;
                }
                case NutsConstants.IdProperties.VERSION: {
                    setVersion(value);
                    return true;
                }
                case NutsConstants.IdProperties.OPTIONAL: {
                    setOptional(value);
                    return true;
                }
                case NutsConstants.IdProperties.CLASSIFIER: {
                    setClassifier(value);
                    return true;
                }
                case NutsConstants.IdProperties.REPO: {
                    setRepository(value);
                    return true;
                }
                case NutsConstants.IdProperties.EXCLUSIONS: {
                    setExclusions(value);
                    return true;
                }
                case NutsConstants.IdProperties.OS: {
                    condition.setOs(PrivateNutsUtilStrings.parseAndTrimToDistinctList(value));
                    return true;
                }
                case NutsConstants.IdProperties.ARCH: {
                    condition.setArch(PrivateNutsUtilStrings.parseAndTrimToDistinctList(value));
                    return true;
                }
                case NutsConstants.IdProperties.PLATFORM: {
                    condition.setPlatform(NutsId.ofList(value).map(x->x.stream().map(Object::toString)).get().collect(Collectors.toList()));
                    return true;
                }
                case NutsConstants.IdProperties.OS_DIST: {
                    condition.setOsDist(PrivateNutsUtilStrings.parseAndTrimToDistinctList(value));
                    return true;
                }
                case NutsConstants.IdProperties.DESKTOP: {
                    condition.setDesktopEnvironment(PrivateNutsUtilStrings.parseAndTrimToDistinctList(value));
                    return true;
                }
                case NutsConstants.IdProperties.TYPE: {
                    setType(value);
                    return true;
                }
            }
        }
        return false;
    });

    public DefaultNutsDependencyBuilder(NutsDependency d) {
        setAll(d);
    }

    public DefaultNutsDependencyBuilder() {
        //for serialization
    }

    public DefaultNutsDependencyBuilder(NutsSession session) {
        condition = new DefaultNutsEnvConditionBuilder();
    }

    @Override
    public NutsDependencyBuilder setId(NutsId id) {
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
    public NutsDependencyBuilder setDependency(NutsDependencyBuilder value) {
        return setAll(value);
    }

    @Override
    public NutsDependencyBuilder setAll(NutsDependencyBuilder value) {
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
    public NutsDependencyBuilder setAll(NutsDependency value) {
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
    public NutsDependencyBuilder setDependency(NutsDependency value) {
        return setAll(value);
    }

    @Override
    public NutsDependencyBuilder clear() {
        setRepository(null);
        setGroupId(null);
        setArtifactId(null);
        setVersion((NutsVersion) null);
        setScope((String) null);
        setOptional(null);
        setExclusions((List<NutsId>) null);
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
    public NutsDependencyBuilder setType(String type) {
        this.type = NutsUtilStrings.trimToNull(type);
        return this;
    }

    @Override
    public String getOptional() {
        return optional;
    }

    @Override
    public NutsDependencyBuilder setOptional(String optional) {
        String o = NutsUtilStrings.trimToNull(optional);
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
    public NutsDependencyBuilder setScope(NutsDependencyScope scope) {
        this.scope = scope == null ? "" : scope.toString();
        return this;
    }

    @Override
    public NutsDependencyBuilder setScope(String scope) {
        this.scope = NutsDependencyScope.parse(scope).orElse(NutsDependencyScope.API).id();
        return this;
    }

    @Override
    public NutsId toId() {
        Map<String, String> m = new LinkedHashMap<>();
        if (!PrivateNutsUtilDescriptors.isDefaultScope(scope)) {
            m.put(NutsConstants.IdProperties.SCOPE, scope);
        }
        if (!NutsBlankable.isBlank(optional) && !"false".equals(optional)) {
            m.put(NutsConstants.IdProperties.OPTIONAL, optional);
        }
        if (!NutsBlankable.isBlank(classifier)) {
            m.put(NutsConstants.IdProperties.CLASSIFIER, classifier);
        }
        if (!NutsBlankable.isBlank(type)) {
            m.put(NutsConstants.IdProperties.TYPE, type);
        }
        if (exclusions.size() > 0) {
            m.put(NutsConstants.IdProperties.EXCLUSIONS, PrivateNutsUtilDescriptors.toExclusionListString(exclusions));
        }
        return new DefaultNutsIdBuilder()
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
    public NutsDependencyBuilder setRepository(String repository) {
        this.repo = NutsUtilStrings.trimToNull(repository);
        return this;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public NutsDependencyBuilder setGroupId(String groupId) {
        this.groupId = NutsUtilStrings.trimToNull(groupId);
        return this;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public NutsDependencyBuilder setArtifactId(String artifactId) {
        this.artifactId = NutsUtilStrings.trimToNull(artifactId);
        return this;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public NutsDependencyBuilder setClassifier(String classifier) {
        this.classifier = NutsUtilStrings.trimToNull(classifier);
        return this;
    }

    @Override
    public String getFullName() {
        if (NutsBlankable.isBlank(groupId)) {
            return NutsUtilStrings.trim(artifactId);
        }
        return NutsUtilStrings.trim(groupId) + ":" + NutsUtilStrings.trim(artifactId);
    }

    @Override
    public NutsVersion getVersion() {
        return version;
    }

    @Override
    public NutsDependencyBuilder setVersion(NutsVersion version) {
        this.version = version == null ? NutsVersion.BLANK : version;
        return this;
    }

    @Override
    public NutsDependencyBuilder setVersion(String version) {
        this.version = NutsVersion.of(version).get();
        return this;
    }

    @Override
    public List<NutsId> getExclusions() {
        return exclusions;
    }

    @Override
    public NutsDependencyBuilder setExclusions(List<NutsId> exclusions) {
        this.exclusions = PrivateNutsUtilCollections.nonNullList(exclusions);
        return this;
    }

    @Override
    public NutsDependency build() {
        return new DefaultNutsDependency(
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
    public NutsDependency copy() {
        return builder();
    }

    @Override
    public NutsDependencyBuilder setProperty(String property, String value) {
        this.propertiesQuery.setProperty(property, value);
        return this;
    }

    @Override
    public NutsDependencyBuilder setProperties(Map<String, String> queryMap) {
        this.propertiesQuery.addProperties(queryMap);
        return this;
    }

    @Override
    public NutsDependencyBuilder setProperties(String propertiesQuery) {
        this.propertiesQuery.addProperties(propertiesQuery);
        return this;
    }

    @Override
    public String getPropertiesQuery() {
        return propertiesQuery.getPropertiesQuery();
    }

    @Override
    public Map<String, String> getProperties() {
        return propertiesQuery.getProperties();
    }


    public NutsEnvConditionBuilder getCondition() {
        return condition;
    }

    @Override
    public NutsDependencyBuilder setCondition(NutsEnvCondition condition) {
        this.condition.setAll(condition);
        return this;
    }

    public NutsDependencyBuilder setExclusions(String exclusions) {
        if (exclusions == null) {
            exclusions = "";
        }
        List<NutsId> ids = new ArrayList<>();
        for (String s : PrivateNutsUtilStrings.splitDefault(exclusions)) {
            NutsId ii = NutsId.of(s).orElse(null);
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
    public NutsDependencyBuilder builder() {
        return new DefaultNutsDependencyBuilder().setAll(this);
    }

    @Override
    public boolean isBlank() {
        return build().isBlank();
    }

    @Override
    public String getSimpleName() {
        return build().getSimpleName();
    }

    @Override
    public String getLongName() {
        return build().getLongName();
    }

    @Override
    public NutsDependencyFormat formatter(NutsSession session) {
        return build().formatter(session);
    }
}
