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
import net.thevpc.nuts.util.NutsUtilStrings;

import java.util.*;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsDependency implements NutsDependency {

    public static final long serialVersionUID = 1L;
    private final String repository;
    private final String groupId;
    private final String artifactId;
    private final NutsVersion version;
    private final String scope;
    private final String classifier;
    private final String optional;
    private final String type;
    private final List<NutsId> exclusions;
    private final String properties;
    private final NutsEnvCondition condition;

    public DefaultNutsDependency(String repository, String groupId, String artifactId, String classifier, NutsVersion version, String scope, String optional, List<NutsId> exclusions,
                                 NutsEnvCondition condition, String type,
                                 Map<String, String> properties) {
        this(repository, groupId, artifactId, classifier, version, scope, optional, exclusions, condition, type, NutsUtilStrings.formatDefaultMap(properties));
    }

    public DefaultNutsDependency(String repository, String groupId, String artifactId, String classifier, NutsVersion version, String scope, String optional, List<NutsId> exclusions,
                                 NutsEnvCondition condition, String type,
                                 String properties) {
        this.repository = NutsUtilStrings.trimToNull(repository);
        this.groupId = NutsUtilStrings.trimToNull(groupId);
        this.artifactId = NutsUtilStrings.trimToNull(artifactId);
        this.version = version == null ? NutsVersion.BLANK : version;
        this.classifier = NutsUtilStrings.trimToNull(classifier);
        this.scope = NutsDependencyScope.parse(scope).orElse(NutsDependencyScope.API).id();

        String o = NutsUtilStrings.trimToNull(optional);
        if ("false".equalsIgnoreCase(o)) {
            o = null;
        } else if ("true".equalsIgnoreCase(o)) {
            o = "true";//remove case and formatting
        }
        this.optional = o;
        this.exclusions = PrivateNutsUtilCollections.unmodifiableList(exclusions);
        for (NutsId exclusion : this.exclusions) {
            if (exclusion == null) {
                throw new NullPointerException();
            }
        }
        this.condition = condition == null ? NutsEnvCondition.BLANK : condition;
        this.type = NutsUtilStrings.trimToNull(type);
        this.properties = NutsUtilStrings.formatDefaultMap(NutsUtilStrings.parseDefaultMap(properties).get());
    }

    @Override
    public boolean isBlank() {
        return toId().isBlank();
    }

    @Override
    public NutsDependencyBuilder builder() {
        return new DefaultNutsDependencyBuilder().setAll(this);
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
    public NutsId toId() {
        Map<String, String> m = new LinkedHashMap<>();
        if (!PrivateNutsUtilDescriptors.isDefaultScope(scope)) {
            m.put(NutsConstants.IdProperties.SCOPE, scope);
        }
        if (!NutsBlankable.isBlank(optional) && !"false".equals(optional)) {
            m.put(NutsConstants.IdProperties.OPTIONAL, optional);
        }
        if (!NutsBlankable.isBlank(type)) {
            m.put(NutsConstants.IdProperties.TYPE, type);
        }
        if (!NutsBlankable.isBlank(repository)) {
            m.put(NutsConstants.IdProperties.REPO, repository);
        }
        if (exclusions.size() > 0) {
            m.put(NutsConstants.IdProperties.EXCLUSIONS, PrivateNutsUtilDescriptors.toExclusionListString(exclusions));
        }
        NutsId ii = NutsIdBuilder.of()
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
        return PrivateNutsUtilIds.getIdShortName(groupId, artifactId);
    }

    @Override
    public String getLongName() {
        return PrivateNutsUtilIds.getIdLongName(groupId, artifactId, version, classifier);
    }

    @Override
    public String getFullName() {
        return toString();
    }

    @Override
    public NutsVersion getVersion() {
        return version;
    }

    @Override
    public NutsEnvCondition getCondition() {
        return condition;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<NutsId> getExclusions() {
        return exclusions;
    }

    @Override
    public String getPropertiesQuery() {
        return properties;
    }

    @Override
    public Map<String, String> getProperties() {
        return NutsUtilStrings.parseDefaultMap(properties).get();
    }

    @Override
    public NutsDependencyFormat formatter(NutsSession session) {
        return NutsDependencyFormat.of(session).setValue(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repository, groupId, artifactId, version, scope, classifier, optional, type, properties, condition, exclusions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsDependency that = (DefaultNutsDependency) o;
        return Objects.equals(repository, that.repository) && Objects.equals(groupId, that.groupId) && Objects.equals(artifactId, that.artifactId) && Objects.equals(version, that.version) && Objects.equals(scope, that.scope) && Objects.equals(classifier, that.classifier) && Objects.equals(optional, that.optional) && Objects.equals(type, that.type) && Objects.equals(exclusions, that.exclusions) && Objects.equals(properties, that.properties) && Objects.equals(condition, that.condition);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!NutsBlankable.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(artifactId);
        if (!NutsBlankable.isBlank(version.getValue())) {
            sb.append("#").append(version);
        }
        Map<String, String> p = new HashMap<>();
        if (!NutsBlankable.isBlank(classifier)) {
            p.put(NutsConstants.IdProperties.CLASSIFIER, classifier);
        }
        if (!NutsBlankable.isBlank(repository)) {
            p.put(NutsConstants.IdProperties.REPO, repository);
        }
        if (!NutsBlankable.isBlank(scope)) {
            if (!scope.equals(NutsDependencyScope.API.id())) {
                p.put(NutsConstants.IdProperties.SCOPE, scope);
            }
        }
        if (!NutsBlankable.isBlank(optional)) {
            if (!optional.equals("false")) {
                p.put(NutsConstants.IdProperties.OPTIONAL, optional);
            }
        }
        if (!NutsBlankable.isBlank(type)) {
            p.put(NutsConstants.IdProperties.TYPE, type);
        }
        if (condition != null && !condition.isBlank()) {
            if (condition.getOs().size() > 0) {
                p.put(NutsConstants.IdProperties.OS, String.join(",", condition.getOs()));
            }
            if (condition.getOsDist().size() > 0) {
                p.put(NutsConstants.IdProperties.OS_DIST, String.join(",", condition.getOsDist()));
            }
            if (condition.getDesktopEnvironment().size() > 0) {
                p.put(NutsConstants.IdProperties.DESKTOP, String.join(",", condition.getDesktopEnvironment()));
            }
            if (condition.getArch().size() > 0) {
                p.put(NutsConstants.IdProperties.ARCH, String.join(",", condition.getArch()));
            }
            if (condition.getPlatform().size() > 0) {
                p.put(NutsConstants.IdProperties.PLATFORM, PrivateNutsIdListParser.formatStringIdList(condition.getPlatform()));
            }
            if (condition.getProfile().size() > 0) {
                p.put(NutsConstants.IdProperties.PROFILE, String.join(",", condition.getProfile()));
            }
            if (!condition.getProperties().isEmpty()) {
                p.put(NutsConstants.IdProperties.CONDITIONAL_PROPERTIES,
                        NutsUtilStrings.formatMap(condition.getProperties(), "=", ",", "&", true)
                );
            }
        }
        if (exclusions.size() > 0) {
            p.put(NutsConstants.IdProperties.EXCLUSIONS,
                    PrivateNutsUtilDescriptors.toExclusionListString(exclusions)
            );
        }
        if (!p.isEmpty()) {
            sb.append("?");
            sb.append(NutsUtilStrings.formatDefaultMap(p));
        }
        return sb.toString();
    }
}
