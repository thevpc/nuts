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
 *
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
package net.thevpc.nuts.runtime.standalone.dependency;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.QueryStringParser;

import java.util.*;
import java.util.stream.Collectors;

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
    private final NutsId[] exclusions;
    private final String properties;
    private final NutsEnvCondition condition;
    private transient final NutsSession session;

    public DefaultNutsDependency(String repository, String groupId, String artifactId, String classifier, NutsVersion version, String scope, String optional, NutsId[] exclusions,
                                 NutsEnvCondition condition, String type,
                                 Map<String, String> properties, NutsSession session) {
        this(repository, groupId, artifactId, classifier, version, scope, optional, exclusions, condition, type, QueryStringParser.formatSortedPropertiesQuery(properties,session), session);
    }

    public DefaultNutsDependency(String repository, String groupId, String artifactId, String classifier, NutsVersion version, String scope, String optional, NutsId[] exclusions,
                                 NutsEnvCondition condition, String type,
                                 String properties, NutsSession session) {
        this.repository = NutsUtilStrings.trimToNull(repository);
        this.groupId = NutsUtilStrings.trimToNull(groupId);
        this.artifactId = NutsUtilStrings.trimToNull(artifactId);
        this.version = version == null ? NutsVersion.of("",session) : version;
        this.classifier = NutsUtilStrings.trimToNull(classifier);
        this.scope = NutsDependencyScope.parseLenient(scope,NutsDependencyScope.API,NutsDependencyScope.OTHER).id();

        String o = NutsUtilStrings.trimToNull(optional);
        if ("false".equalsIgnoreCase(o)) {
            o = null;
        } else if ("true".equalsIgnoreCase(o)) {
            o = "true";//remove case and formatting
        }
        this.optional = o;
        this.exclusions = exclusions == null ? new NutsId[0] : Arrays.copyOf(exclusions, exclusions.length);
        for (NutsId exclusion : exclusions) {
            if (exclusion == null) {
                throw new NullPointerException();
            }
        }
        this.condition = CoreFilterUtils.trimToBlank(condition,session);
        this.type = NutsUtilStrings.trimToNull(type);
        this.properties = QueryStringParser.formatSortedPropertiesQuery(properties,session);
        this.session = session;
//        if (toString().contains("jai_imageio")) {
//            System.out.print("");
//        }
    }

    @Override
    public boolean isBlank() {
        return toId().isBlank();
    }

    @Override
    public String getClassifier() {
        return classifier;
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
    public String getType() {
        return type;
    }

    @Override
    public NutsId toId() {
        Map<String, String> m = new LinkedHashMap<>();
        if (!NutsDependencyScopes.isDefaultScope(scope)) {
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
        if (!NutsBlankable.isBlank(repository)) {
            m.put(NutsConstants.IdProperties.REPO, repository);
        }
        if (exclusions.length > 0) {
            TreeSet<String> ex = new TreeSet<>();
            for (NutsId exclusion : exclusions) {
                ex.add(exclusion.getShortName());
            }
            m.put(NutsConstants.IdProperties.EXCLUSIONS, String.join(",", ex));
        }
        NutsId ii = NutsIdBuilder.of(session)
                .setGroupId(getGroupId())
                .setArtifactId(getArtifactId())
                .setVersion(getVersion())
                .setCondition(getCondition())
                .setProperties(m).build();
        String ss=ii.toString();
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
        if (NutsBlankable.isBlank(groupId)) {
            return NutsUtilStrings.trim(artifactId);
        }
        return NutsUtilStrings.trim(groupId) + ":" + NutsUtilStrings.trim(artifactId);
    }

    @Override
    public String getLongName() {
        StringBuilder sb=new StringBuilder();
        if (!NutsBlankable.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NutsUtilStrings.trim(artifactId));
        NutsVersion v = getVersion();
        if (!v.isBlank()) {
            sb.append("#");
            sb.append(v);
        }
        if(!NutsBlankable.isBlank(classifier)){
            sb.append("?");
            sb.append("classifier=");
            sb.append(classifier);
        }
        return sb.toString();
    }

    @Override
    public String getFullName() {
        return toString();
    }

    @Override
    public NutsVersion getVersion() {
        return version;
    }

    //    public void setRepository(String repository) {
//        this.repository = repository;
//    }
//    public void setGroup(String group) {
//        this.group = group;
//    }
//
//    public void setArtifactId(String artifactId) {
//        this.name = artifactId;
//    }
//
//    public void setVersion(String version) {
//        this.version = version;
//    }
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
        if (condition!=null && !condition.isBlank()) {
            if(condition.getOs().length>0) {
                p.put(NutsConstants.IdProperties.OS, String.join(",",condition.getOs()));
            }
            if(condition.getOsDist().length>0) {
                p.put(NutsConstants.IdProperties.OS_DIST, String.join(",",condition.getOsDist()));
            }
            if(condition.getDesktopEnvironment().length>0) {
                p.put(NutsConstants.IdProperties.DESKTOP_ENVIRONMENT, String.join(",",condition.getDesktopEnvironment()));
            }
            if(condition.getArch().length>0) {
                p.put(NutsConstants.IdProperties.ARCH, String.join(",",condition.getArch()));
            }
            if(condition.getPlatform().length>0) {
                p.put(NutsConstants.IdProperties.PLATFORM, String.join(",",condition.getPlatform()));
            }
            if(condition.getProfile().length>0) {
                p.put(NutsConstants.IdProperties.PROFILE, String.join(",",condition.getProfile()));
            }
        }
        if (exclusions.length>0) {
            p.put(NutsConstants.IdProperties.EXCLUSIONS, Arrays.stream(exclusions)
                            .map(NutsId::getShortName)
                    .collect(Collectors.joining(","))
            );
        }
        if (!p.isEmpty()) {
            sb.append("?");
            sb.append(QueryStringParser.formatPropertiesQuery(p));
        }
        return sb.toString();
    }

    @Override
    public NutsId[] getExclusions() {
        return Arrays.copyOf(exclusions, exclusions.length);
    }

    @Override
    public NutsDependencyBuilder builder() {
        return new DefaultNutsDependencyBuilder(session).set(this);
    }

    @Override
    public String getPropertiesQuery() {
        return properties;
    }

    @Override
    public Map<String, String> getProperties() {
        return QueryStringParser.parseMap(properties,session);
    }

    @Override
    public NutsDependencyFormat formatter() {
        return NutsDependencyFormat.of(session).setValue(this);
    }

    @Override
    public NutsEnvCondition getCondition() {
        return condition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsDependency that = (DefaultNutsDependency) o;
        return Objects.equals(repository, that.repository) && Objects.equals(groupId, that.groupId) && Objects.equals(artifactId, that.artifactId) && Objects.equals(version, that.version) && Objects.equals(scope, that.scope) && Objects.equals(classifier, that.classifier) && Objects.equals(optional, that.optional) && Objects.equals(type, that.type) && Arrays.equals(exclusions, that.exclusions) && Objects.equals(properties, that.properties) && Objects.equals(condition, that.condition) && Objects.equals(session, that.session);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(repository, groupId, artifactId, version, scope, classifier, optional, type, properties, condition, session);
        result = 31 * result + Arrays.hashCode(exclusions);
        return result;
    }
}
