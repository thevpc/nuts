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
package net.thevpc.nuts.runtime.core.model;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.parsers.QueryStringParser;

import java.util.*;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.standalone.util.NutsDependencyScopes;

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
    private final String os;
    private final String arch;
    private final NutsId[] exclusions;
    private final String properties;
    private transient final NutsSession session;

    public DefaultNutsDependency(String repository, String groupId, String artifactId, String classifier, NutsVersion version, String scope, String optional, NutsId[] exclusions,
                                 String os, String arch, String type,
                                 Map<String, String> properties, NutsSession ws) {
        this(repository, groupId, artifactId, classifier, version, scope, optional, exclusions, os, arch, type, QueryStringParser.formatSortedPropertiesQuery(properties), ws);
    }

    public DefaultNutsDependency(String repository, String groupId, String artifactId, String classifier, NutsVersion version, String scope, String optional, NutsId[] exclusions,
                                 String os, String arch, String type,
                                 String properties, NutsSession session) {
        this.repository = NutsUtilStrings.trimToNull(repository);
        this.groupId = NutsUtilStrings.trimToNull(groupId);
        this.artifactId = NutsUtilStrings.trimToNull(artifactId);
        this.version = version == null ? session.getWorkspace().version().parser().parse("") : version;
        this.classifier = NutsUtilStrings.trimToNull(classifier);
        this.scope = NutsDependencyScopes.normalizeScope(scope);

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
        this.os = NutsUtilStrings.trimToNull(os);
        this.arch = NutsUtilStrings.trimToNull(arch);
        this.type = NutsUtilStrings.trimToNull(type);
        this.properties = QueryStringParser.formatSortedPropertiesQuery(properties);
        this.session = session;
//        if (toString().contains("jai_imageio")) {
//            System.out.print("");
//        }
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
        if (!NutsUtilStrings.isBlank(optional) && !"false".equals(optional)) {
            m.put(NutsConstants.IdProperties.OPTIONAL, optional);
        }
        if (!NutsUtilStrings.isBlank(classifier)) {
            m.put(NutsConstants.IdProperties.CLASSIFIER, classifier);
        }
        if (!NutsUtilStrings.isBlank(type)) {
            m.put(NutsConstants.IdProperties.TYPE, type);
        }
        if (!NutsUtilStrings.isBlank(repository)) {
            m.put(NutsConstants.IdProperties.REPO, repository);
        }
        if (exclusions.length > 0) {
            TreeSet<String> ex = new TreeSet<>();
            for (NutsId exclusion : exclusions) {
                ex.add(exclusion.getShortName());
            }
            m.put(NutsConstants.IdProperties.EXCLUSIONS, String.join(",", ex));
        }
        return session.getWorkspace().id().builder()
                .setGroupId(getGroupId())
                .setArtifactId(getArtifactId())
                .setVersion(getVersion())
                .setOs(getOs())
                .setArch(getArch())
                .setProperties(m).build();
    }

    @Override
    public String getOs() {
        return os;
    }

    @Override
    public String getArch() {
        return arch;
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
        if (NutsUtilStrings.isBlank(groupId)) {
            return NutsUtilStrings.trim(artifactId);
        }
        return NutsUtilStrings.trim(groupId) + ":" + NutsUtilStrings.trim(artifactId);
    }

    @Override
    public String getLongName() {
        String s = getSimpleName();
        NutsVersion v = getVersion();
        if (v.isBlank()) {
            return s;
        }
        return s + "#" + v;
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
        if (!NutsUtilStrings.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(artifactId);
        if (!NutsUtilStrings.isBlank(version.getValue())) {
            sb.append("#").append(version);
        }
        Map<String, String> p = new HashMap<>();
        if (!NutsUtilStrings.isBlank(repository)) {
            p.put(NutsConstants.IdProperties.REPO, repository);
        }
        if (!NutsUtilStrings.isBlank(scope)) {
            if (!scope.equals(NutsDependencyScope.API.id())) {
                p.put(NutsConstants.IdProperties.SCOPE, scope);
            }
        }
        if (!NutsUtilStrings.isBlank(optional)) {
            if (!optional.equals("false")) {
                p.put(NutsConstants.IdProperties.OPTIONAL, optional);
            }
        }
        if (!NutsUtilStrings.isBlank(type)) {
            p.put(NutsConstants.IdProperties.TYPE, type);
        }
        if (!NutsUtilStrings.isBlank(os)) {
            p.put(NutsConstants.IdProperties.OS, os);
        }
        if (!NutsUtilStrings.isBlank(arch)) {
            p.put(NutsConstants.IdProperties.ARCH, arch);
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
        return QueryStringParser.parseMap(properties);
    }

    @Override
    public NutsDependencyFormat formatter() {
        return session.getWorkspace().dependency().formatter().setValue(this);
    }
}
