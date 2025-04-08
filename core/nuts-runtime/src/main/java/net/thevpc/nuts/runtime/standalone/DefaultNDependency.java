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

import net.thevpc.nuts.*;
import net.thevpc.nuts.NEnvCondition;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringMapFormat;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNDependency implements NDependency {
    private static NStringMapFormat COMMA_MAP = NStringMapFormat.COMMA_FORMAT.builder().setEscapeChars("&").build();

    public static final long serialVersionUID = 1L;
    private final String repository;
    private final String groupId;
    private final String artifactId;
    private final NVersion version;
    private final String scope;
    private final String classifier;
    private final String optional;
    private final String type;
    private final List<NId> exclusions;
    private final String properties;
    private final NEnvCondition condition;

    public DefaultNDependency(String repository, String groupId, String artifactId, String classifier, NVersion version, String scope, String optional, List<NId> exclusions,
                              NEnvCondition condition, String type,
                              Map<String, String> properties) {
        this(repository, groupId, artifactId, classifier, version, scope, optional, exclusions, condition, type, NStringMapFormat.DEFAULT.format(properties));
    }

    public DefaultNDependency(String repository, String groupId, String artifactId, String classifier, NVersion version, String scope, String optional, List<NId> exclusions,
                              NEnvCondition condition, String type,
                              String properties) {
        this.repository = NStringUtils.trimToNull(repository);
        this.groupId = NStringUtils.trimToNull(groupId);
        this.artifactId = NStringUtils.trimToNull(artifactId);
        this.version = version == null ? NVersion.BLANK : version;
        this.classifier = NStringUtils.trimToNull(classifier);
        this.scope = NDependencyScope.parse(scope).orElse(NDependencyScope.API).id();

        String o = NStringUtils.trimToNull(optional);
        if ("false".equalsIgnoreCase(o)) {
            o = null;
        } else if ("true".equalsIgnoreCase(o)) {
            o = "true";//remove case and formatting
        }
        this.optional = o;
        this.exclusions = NReservedLangUtils.unmodifiableList(exclusions);
        for (NId exclusion : this.exclusions) {
            if (exclusion == null) {
                throw new NullPointerException();
            }
        }
        this.condition = condition == null ? NEnvCondition.BLANK : condition;
        this.type = NStringUtils.trimToNull(type);
        this.properties = NStringUtils.trim(NStringMapFormat.DEFAULT.format(NStringMapFormat.DEFAULT.parse(properties).get()));
    }

    @Override
    public boolean isBlank() {
        return toId().isBlank();
    }

    @Override
    public NDependencyBuilder builder() {
        return NDependencyBuilder.of().copyFrom(this);
    }

    @Override
    public boolean isOptional() {
        final String o = getOptional();
        return o != null && Boolean.parseBoolean(o);
    }

    @Override
    public String getOptional() {
        return optional;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public String getClassifier() {
        return classifier;
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
        if (!NBlankable.isBlank(repository)) {
            m.put(NConstants.IdProperties.REPO, repository);
        }
        if (exclusions.size() > 0) {
            m.put(NConstants.IdProperties.EXCLUSIONS, NReservedUtils.toDependencyExclusionListString(exclusions));
        }
        NId ii = NIdBuilder.of()
                .setGroupId(getGroupId())
                .setArtifactId(getArtifactId())
                .setClassifier(getClassifier())
                .setVersion(getVersion())
                .setCondition(getCondition())
                .setProperties(m).build();
        return ii;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
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
    public String getFullName() {
        return toString();
    }

    @Override
    public NVersion getVersion() {
        return version;
    }

    @Override
    public NEnvCondition getCondition() {
        return condition;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<NId> getExclusions() {
        return exclusions;
    }

    @Override
    public String getPropertiesQuery() {
        return properties;
    }

    @Override
    public Map<String, String> getProperties() {
        return NStringMapFormat.DEFAULT.parse(properties).get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(repository, groupId, artifactId, version, scope, classifier, optional, type, properties, condition, exclusions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNDependency that = (DefaultNDependency) o;
        return Objects.equals(repository, that.repository) && Objects.equals(groupId, that.groupId) && Objects.equals(artifactId, that.artifactId) && Objects.equals(version, that.version) && Objects.equals(scope, that.scope) && Objects.equals(classifier, that.classifier) && Objects.equals(optional, that.optional) && Objects.equals(type, that.type) && Objects.equals(exclusions, that.exclusions) && Objects.equals(properties, that.properties) && Objects.equals(condition, that.condition);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!NBlankable.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(artifactId);
        if (!NBlankable.isBlank(version.getValue())) {
            sb.append("#").append(version);
        }
        Map<String, String> p = new HashMap<>();
        if (!NBlankable.isBlank(classifier)) {
            p.put(NConstants.IdProperties.CLASSIFIER, classifier);
        }
        if (!NBlankable.isBlank(repository)) {
            p.put(NConstants.IdProperties.REPO, repository);
        }
        if (!NBlankable.isBlank(scope)) {
            if (!scope.equals(NDependencyScope.API.id())) {
                p.put(NConstants.IdProperties.SCOPE, scope);
            }
        }
        if (!NBlankable.isBlank(optional)) {
            if (!optional.equals("false")) {
                p.put(NConstants.IdProperties.OPTIONAL, optional);
            }
        }
        if (!NBlankable.isBlank(type)) {
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
                p.put(NConstants.IdProperties.PLATFORM, NReservedUtils.formatStringIdList(condition.getPlatform()));
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
                    NReservedUtils.toDependencyExclusionListString(exclusions)
            );
        }
        if (!p.isEmpty()) {
            sb.append("?");
            sb.append(NStringMapFormat.DEFAULT.format(p));
        }
        return sb.toString();
    }
}
