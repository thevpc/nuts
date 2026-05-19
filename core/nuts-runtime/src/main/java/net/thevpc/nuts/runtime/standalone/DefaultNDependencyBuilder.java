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
package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.boot.NBootDependency;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.internal.NReservedLangUtils;
import net.thevpc.nuts.internal.NReservedUtils;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/5/17.
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
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
        copyFrom(d);
    }

    public DefaultNDependencyBuilder() {

    }

    public DefaultNDependencyBuilder(String groupId, String artifactId) {
        this.groupId = NStringUtils.trimToNull(groupId);
        this.artifactId = NStringUtils.trimToNull(artifactId);
    }

    @Override
    public NDependencyBuilder id(NId id) {
        if (id == null) {
            repository(null);
            groupId(null);
            artifactId(null);
            version((String) null);
        } else {
            repository(id.repository());
            groupId(id.groupId());
            artifactId(id.artifactId());
            version(id.version());
            this.properties(id.properties());
        }
        return this;
    }

    @Override
    public NDependencyBuilder dependency(NDependencyBuilder value) {
        return copyFrom(value);
    }

    @Override
    public NDependencyBuilder copyFrom(NDependencyBuilder value) {
        if (value != null) {
            repository(value.repository());
            groupId(value.groupId());
            artifactId(value.artifactId());
            version(value.version());
            scope(value.scope());
            optional(value.optional());
            exclusions(value.exclusions());
            classifier(value.classifier());
            condition().copyFrom(value.condition());
            type(value.type());
            properties(value.properties());
        } else {
            clear();
        }
        return this;
    }


    public NDependencyBuilder copyFrom(NBootDependency value) {
        if (value != null) {
            repository(value.getRepository());
            groupId(value.getGroupId());
            artifactId(value.getArtifactId());
            version(value.getVersion());
            scope(value.getScope());
            optional(value.getOptional());
            exclusions(value.getExclusions()==null?null:value.getExclusions().stream().map(x->x==null?null:NId.get(x.toString()).get()).collect(Collectors.toList()));
            classifier(value.getClassifier());
            condition().copyFrom(value.getCondition()==null?null:new DefaultNEnvConditionBuilder().copyFrom(value.getCondition()));
            type(value.getType());
            properties(value.getProperties());
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NDependencyBuilder copyFrom(NDependency value) {
        if (value != null) {
            repository(value.repository());
            groupId(value.groupId());
            artifactId(value.artifactId());
            version(value.version());
            scope(value.scope());
            optional(value.optional());
            exclusions(value.exclusions());
            classifier(value.classifier());
            condition().copyFrom(value.condition());
            type(value.type());
            properties(value.properties());
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NDependencyBuilder dependency(NDependency value) {
        return copyFrom(value);
    }

    @Override
    public NDependencyBuilder clear() {
        repository(null);
        groupId(null);
        artifactId(null);
        version((NVersion) null);
        scope((String) null);
        optional(null);
        exclusions((List<NId>) null);
        classifier(null);
        condition().clear();
        type(null);
        properties((Map<String, String>) null);
        return this;
    }

    @Override
    public boolean isOptional() {
        return optional != null && Boolean.parseBoolean(optional);
    }

    public String type() {
        return type;
    }

    @Override
    public NDependencyBuilder type(String type) {
        this.type = NStringUtils.trimToNull(type);
        return this;
    }

    @Override
    public String optional() {
        return optional;
    }

    @Override
    public NDependencyBuilder optional(String optional) {
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
    public String scope() {
        return scope;
    }

    @Override
    public NDependencyBuilder scope(NDependencyScope scope) {
        this.scope = scope == null ? "" : scope.toString();
        return this;
    }

    @Override
    public NDependencyBuilder scope(String scope) {
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
        if (!NBlankable.isBlank(type)) {
            m.put(NConstants.IdProperties.TYPE, type);
        }
        if (exclusions.size() > 0) {
            m.put(NConstants.IdProperties.EXCLUSIONS, NReservedUtils.toDependencyExclusionListString(exclusions));
        }
        return NIdBuilder.of()
                .repository(repository())
                .groupId(groupId())
                .classifier(classifier())
                .artifactId(artifactId())
                .version(version())
                .condition(condition())
                .setProperties(m).build()
                ;
    }

    @Override
    public String repository() {
        return repo;
    }

    @Override
    public NDependencyBuilder repository(String repository) {
        this.repo = NStringUtils.trimToNull(repository);
        return this;
    }

    @Override
    public String groupId() {
        return groupId;
    }

    @Override
    public NDependencyBuilder groupId(String groupId) {
        this.groupId = NStringUtils.trimToNull(groupId);
        return this;
    }

    @Override
    public String artifactId() {
        return artifactId;
    }

    @Override
    public NDependencyBuilder artifactId(String artifactId) {
        this.artifactId = NStringUtils.trimToNull(artifactId);
        return this;
    }

    @Override
    public String classifier() {
        return classifier;
    }

    @Override
    public NDependencyBuilder classifier(String classifier) {
        this.classifier = NStringUtils.trimToNull(classifier);
        return this;
    }

    @Override
    public String fullName() {
        if (NBlankable.isBlank(groupId)) {
            return NStringUtils.trim(artifactId);
        }
        return NStringUtils.trim(groupId) + ":" + NStringUtils.trim(artifactId);
    }

    @Override
    public NVersion version() {
        return version;
    }

    @Override
    public NDependencyBuilder version(NVersion version) {
        this.version = version == null ? NVersion.BLANK : version;
        return this;
    }

    @Override
    public NDependencyBuilder version(String version) {
        this.version = NVersion.get(version).get();
        return this;
    }

    @Override
    public List<NId> exclusions() {
        return exclusions;
    }

    @Override
    public NDependencyBuilder exclusions(List<NId> exclusions) {
        this.exclusions = NReservedLangUtils.nonNullList(exclusions);
        return this;
    }

    @Override
    public NDependency build() {
        return new DefaultNDependency(
                repository(), groupId(), artifactId(), classifier(),
                version(),
                scope(),
                optional(),
                exclusions(),
                condition().build(),
                type(),
                propertiesQuery()
        );
    }


    @Override
    public NDependencyBuilder property(String property, String value) {
        if (property != null) {
            switch (property) {
                case NConstants.IdProperties.SCOPE: {
                    scope(value);
                    break;
                }
                case NConstants.IdProperties.VERSION: {
                    version(value);
                    break;
                }
                case NConstants.IdProperties.OPTIONAL: {
                    optional(value);
                    break;
                }
                case NConstants.IdProperties.REPO: {
                    repository(value);
                    break;
                }
                case NConstants.IdProperties.EXCLUSIONS: {
                    setExclusions(value);
                    break;
                }
                case NConstants.IdProperties.OS: {
                    condition.os(NStringUtils.parsePropertyIdList(value).get());
                    break;
                }
                case NConstants.IdProperties.ARCH: {
                    condition.arch(NStringUtils.parsePropertyIdList(value).get());
                    break;
                }
                case NConstants.IdProperties.PLATFORM: {
                    condition.platform(NStringUtils.parsePropertyIdList(value).get());
                    break;
                }
                case NConstants.IdProperties.OS_DIST: {
                    condition.osDist(NStringUtils.parsePropertyIdList(value).get());
                    break;
                }
                case NConstants.IdProperties.DESKTOP: {
                    condition.desktopEnvironment(NStringUtils.parsePropertyIdList(value).get());
                    break;
                }
                case NConstants.IdProperties.TYPE: {
                    type(value);
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
                property(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    @Override
    public NDependencyBuilder properties(Map<String, String> queryMap) {
        properties.clear();
        if (queryMap != null) {
            for (Map.Entry<String, String> e : queryMap.entrySet()) {
                property(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    @Override
    public NDependencyBuilder propertiesQuery(String propertiesQuery) {
        properties(NStringMapFormat.DEFAULT.parse(propertiesQuery).get());
        return this;
    }

    @Override
    public String propertiesQuery() {
        return NStringMapFormat.DEFAULT.format(properties);
    }

    @Override
    public Map<String, String> properties() {
        return properties;
    }


    public NEnvConditionBuilder condition() {
        return condition;
    }

    @Override
    public NDependencyBuilder condition(NEnvCondition condition) {
        this.condition.clear();
        this.condition.copyFrom(condition);
        return this;
    }

    @Override
    public NDependencyBuilder condition(NEnvConditionBuilder condition) {
        this.condition.clear();
        this.condition.copyFrom(condition);
        return this;
    }

    @Override
    public NDependencyBuilder removeCondition() {
        this.condition.clear();
        return this;
    }

    public NDependencyBuilder setExclusions(String exclusions) {
        if (exclusions == null) {
            exclusions = "";
        }
        List<NId> ids = new ArrayList<>();
        for (String s : NReservedLangUtils.splitDefault(exclusions)) {
            NId ii = NId.get(s).orNull();
            if (ii != null) {
                ids.add(ii);
            }
        }
        exclusions(ids);
        return this;
    }

    @Override
    public String toString() {
        return build().toString();
    }

    @Override
    public NDependencyBuilder copy() {
        return new DefaultNDependencyBuilder().copyFrom(this);
    }

    @Override
    public boolean isBlank() {
        return build().isBlank();
    }

    @Override
    public String shortName() {
        return NReservedUtils.getIdShortName(groupId,artifactId, classifier);
    }

    @Override
    public String longName() {
        return NReservedUtils.getIdLongName(groupId,artifactId, version, classifier);
    }

}
