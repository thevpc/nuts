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
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.DefaultNutsId;
import net.thevpc.nuts.runtime.DefaultNutsVersion;
import net.thevpc.nuts.runtime.util.QueryStringMap;

import java.util.*;

import net.thevpc.nuts.runtime.util.NutsDependencyScopes;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsDependency implements NutsDependency {
    public static final long serialVersionUID = 1L;
    private final String namespace;
    private final String groupId;
    private final String artifactId;
    private final NutsVersion version;
    private final String scope;
    private final String classifier;
    private final String optional;
    private final NutsId[] exclusions;
    private final String properties;

    public DefaultNutsDependency(String namespace, String groupId, String artifactId, String classifier, NutsVersion version, String scope, String optional, NutsId[] exclusions,Map<String,String> properties) {
        this(namespace, groupId, artifactId, classifier, version, scope, optional, exclusions,QueryStringMap.formatSortedPropertiesQuery(properties));
    }
    public DefaultNutsDependency(String namespace, String groupId, String artifactId, String classifier, NutsVersion version, String scope, String optional, NutsId[] exclusions,String properties) {
        this.namespace = CoreStringUtils.trimToNull(namespace);
        this.groupId = CoreStringUtils.trimToNull(groupId);
        this.artifactId = CoreStringUtils.trimToNull(artifactId);
        this.version = version == null ? DefaultNutsVersion.EMPTY : version;
        this.classifier = CoreStringUtils.trimToNull(classifier);
        this.scope = NutsDependencyScopes.normalizeScope(scope);
        this.optional = CoreStringUtils.isBlank(optional) ? "false" : CoreStringUtils.trim(optional);
        this.exclusions = exclusions == null ? new NutsId[0] : Arrays.copyOf(exclusions, exclusions.length);
        this.properties = QueryStringMap.formatSortedPropertiesQuery(properties);
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public boolean isOptional() {
        return Boolean.parseBoolean(getOptional());
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
    public NutsId getId() {
        Map<String, String> m = new LinkedHashMap<>();
        if (!NutsDependencyScopes.isDefaultScope(scope)) {
            m.put(NutsConstants.IdProperties.SCOPE, scope);
        }
        if (!CoreStringUtils.isBlank(optional) && !"false".equals(optional)) {
            m.put(NutsConstants.IdProperties.OPTIONAL, optional);
        }
        if (!CoreStringUtils.isBlank(classifier)) {
            m.put(NutsConstants.IdProperties.CLASSIFIER, classifier);
        }
        if (exclusions.length > 0) {
            TreeSet<String> ex = new TreeSet<>();
            for (NutsId exclusion : exclusions) {
                ex.add(exclusion.getShortName());
            }
            m.put(NutsConstants.IdProperties.EXCLUSIONS, String.join(",", ex));
        }
        return new DefaultNutsId(
                getNamespace(),
                getGroupId(),
                getArtifactId(),
                getVersion().getValue(),
                m
        );
    }


    @Override
    public NutsId toId() {
        Map<String, String> m = new LinkedHashMap<>();
//        if (!NutsDependencyScopes.isDefaultScope(scope)) {
//            m.put(NutsConstants.IdProperties.SCOPE, scope);
//        }
//        if (!CoreStringUtils.isBlank(optional) && !"false".equals(optional)) {
//            m.put(NutsConstants.IdProperties.OPTIONAL, optional);
//        }
//        if (!CoreStringUtils.isBlank(classifier)) {
//            m.put(NutsConstants.IdProperties.CLASSIFIER, classifier);
//        }
//        if (exclusions.length > 0) {
//            TreeSet<String> ex = new TreeSet<>();
//            for (NutsId exclusion : exclusions) {
//                ex.add(exclusion.getShortName());
//            }
//            m.put(NutsConstants.IdProperties.EXCLUSIONS, CoreStringUtils.join(",", ex));
//        }
        return new DefaultNutsId(
                getNamespace(),
                getGroupId(),
                getArtifactId(),
                getVersion().getValue(),
                m
        );
    }

    @Override
    public String getNamespace() {
        return namespace;
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
        if (CoreStringUtils.isBlank(groupId)) {
            return CoreStringUtils.trim(artifactId);
        }
        return CoreStringUtils.trim(groupId) + ":" + CoreStringUtils.trim(artifactId);
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

    //    public void setNamespace(String namespace) {
//        this.namespace = namespace;
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
        if (!CoreStringUtils.isBlank(namespace)) {
            sb.append(namespace).append("://");
        }
        if (!CoreStringUtils.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(artifactId);
        if (!CoreStringUtils.isBlank(version.getValue())) {
            sb.append("#").append(version);
        }
        Map<String, String> p = new HashMap<>();
        if (!CoreStringUtils.isBlank(scope)) {
            if (!scope.equals(NutsDependencyScope.API.id())) {
                p.put(NutsConstants.IdProperties.SCOPE, scope);
            }
        }
        if (!CoreStringUtils.isBlank(optional)) {
            if (!optional.equals("false")) {
                p.put(NutsConstants.IdProperties.OPTIONAL, optional);
            }
        }
        if (!p.isEmpty()) {
            sb.append("?");
            sb.append(QueryStringMap.formatPropertiesQuery(p));
        }
        return sb.toString();
    }

    @Override
    public NutsId[] getExclusions() {
        return Arrays.copyOf(exclusions, exclusions.length);
    }

    @Override
    public NutsDependencyBuilder builder() {
        return new DefaultNutsDependencyBuilder().set(this);
    }

    @Override
    public String getPropertiesQuery() {
        return properties;
    }

    @Override
    public Map<String, String> getProperties() {
        return QueryStringMap.parseMap(properties);
    }
}
