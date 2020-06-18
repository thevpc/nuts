/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.config;

import net.vpc.app.nuts.*;

import java.util.*;

import net.vpc.app.nuts.runtime.DefaultNutsId;
import net.vpc.app.nuts.runtime.DefaultNutsVersion;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.NutsDependencyScopes;
import net.vpc.app.nuts.runtime.util.QueryStringMap;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsDependencyBuilder implements NutsDependencyBuilder {

    private String namespace;
    private String groupId;
    private String artifactId;
    private NutsVersion version;
    private String scope;
    private String optional;
    private String classifier;
    private NutsId[] exclusions;
    private QueryStringMap propertiesQuery = new QueryStringMap(true, (name, value) -> {
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
                case NutsConstants.IdProperties.NAMESPACE: {
                    setNamespace(value);
                    return true;
                }
                case NutsConstants.IdProperties.EXCLUSIONS: {
                    setExclusions(value);
                    return true;
                }
            }
        }
        return false;
    });

    public DefaultNutsDependencyBuilder() {
    }

    @Override
    public NutsDependencyBuilder set(NutsDependencyBuilder value) {
        if (value != null) {
            setNamespace(value.getNamespace());
            setGroupId(value.getGroupId());
            setArtifactId(value.getArtifactId());
            setVersion(value.getVersion());
            setScope(value.getScope());
            setOptional(value.getOptional());
            setExclusions(value.getExclusions());
            setClassifier(value.getClassifier());
            setProperties(value.getProperties());
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NutsDependencyBuilder set(NutsDependency value) {
        if (value != null) {
            setNamespace(value.getNamespace());
            setGroupId(value.getGroupId());
            setArtifactId(value.getArtifactId());
            setVersion(value.getVersion());
            setScope(value.getScope());
            setOptional(value.getOptional());
            setExclusions(value.getExclusions());
            setClassifier(value.getClassifier());
            setProperties(value.getProperties());
        } else {
            clear();
        }
        return this;
    }

    @Override
    public NutsDependencyBuilder clear() {
        setNamespace(null);
        setGroupId(null);
        setArtifactId(null);
        setVersion((NutsVersion) null);
        setScope(null);
        setOptional(null);
        setExclusions((NutsId[]) null);
        setClassifier(null);
        setProperties((Map<String, String>) null);
        return this;
    }

    @Override
    public NutsDependencyBuilder setNamespace(String namespace) {
        this.namespace = CoreStringUtils.trimToNull(namespace);
        return this;
    }

    @Override
    public NutsDependencyBuilder setGroupId(String groupId) {
        this.groupId = CoreStringUtils.trimToNull(groupId);
        return this;
    }

    @Override
    public NutsDependencyBuilder setArtifactId(String artifactId) {
        this.artifactId = CoreStringUtils.trimToNull(artifactId);
        return this;
    }

    @Override
    public NutsDependencyBuilder setVersion(NutsVersion version) {
        this.version = version == null ? DefaultNutsVersion.EMPTY : version;
        return this;
    }

    @Override
    public NutsDependencyBuilder setVersion(String classifier) {
        this.version = DefaultNutsVersion.valueOf(classifier);
        return this;
    }

    @Override
    public NutsDependencyBuilder setId(NutsId id) {
        if (id == null) {
            setNamespace(null);
            setGroupId(null);
            setArtifactId(null);
            setVersion((String) null);
        } else {
            setNamespace(id.getNamespace());
            setGroupId(id.getGroupId());
            setArtifactId(id.getArtifactId());
            setVersion(id.getVersion());
            addProperties(id.getProperties());
        }
        return this;
    }

    @Override
    public NutsDependencyBuilder setClassifier(String classifier) {
        this.classifier = CoreStringUtils.trimToNull(classifier);
        return this;
    }

    @Override
    public NutsDependencyBuilder setScope(String scope) {
        this.scope = NutsDependencyScopes.normalizeScope(scope);
        return this;
    }

    @Override
    public NutsDependencyBuilder setOptional(String optional) {
        this.optional = CoreStringUtils.isBlank(optional) ? "false" : CoreStringUtils.trim(optional);
        return this;
    }

    //@Override
    public NutsDependencyBuilder setExclusions(String exclusions) {
        if (exclusions == null) {
            exclusions = "";
        }
        List<NutsId> ids = new ArrayList<>();
        for (String s : exclusions.split(";")) {
            ids.add(CoreNutsUtils.parseNutsId(s.trim()));
        }
        setExclusions(ids.toArray(new NutsId[0]));
        return this;
    }

    @Override
    public NutsDependencyBuilder setExclusions(NutsId[] exclusions) {
        if (exclusions != null) {
            exclusions = Arrays.copyOf(exclusions, exclusions.length);
        }
        this.exclusions = exclusions;
        return this;
    }

    @Override
    public boolean isOptional() {
        return Boolean.parseBoolean(optional);
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
    public NutsId getId() {
        return new DefaultNutsId(
                getNamespace(),
                getGroupId(),
                getArtifactId(),
                getVersion().getValue(),
                ""
        );
    }

    @Override
    public NutsDependency build() {
        return new DefaultNutsDependency(
                getNamespace(), getGroupId(), getArtifactId(), getClassifier(),
                getVersion(),
                getScope(),
                getOptional(),
                getExclusions(),
                getPropertiesQuery()
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
    public String getFullName() {
        if (CoreStringUtils.isBlank(groupId)) {
            return CoreStringUtils.trim(artifactId);
        }
        return CoreStringUtils.trim(groupId) + ":" + CoreStringUtils.trim(artifactId);
    }

    @Override
    public NutsVersion getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return build().toString();
    }

    @Override
    public NutsId[] getExclusions() {
        return exclusions == null ? new NutsId[0] : Arrays.copyOf(exclusions, exclusions.length);
    }

    @Override
    public NutsDependencyBuilder setProperty(String property, String value) {
        this.propertiesQuery.setProperty(property, value);
        return this;
    }

    @Override
    public NutsDependencyBuilder setProperties(Map<String, String> queryMap) {
        this.propertiesQuery.setProperties(queryMap);
        return this;
    }

    @Override
    public NutsDependencyBuilder addProperties(Map<String, String> queryMap) {
        this.propertiesQuery.addProperties(queryMap);
        return this;
    }

    @Override
    public NutsDependencyBuilder setProperties(String propertiesQuery) {
        this.propertiesQuery.setProperties(propertiesQuery);
        return this;
    }

    @Override
    public NutsDependencyBuilder addProperties(String propertiesQuery) {
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

    @Override
    public NutsDependencyBuilder setDependency(NutsDependencyBuilder value) {
        return set(value);
    }

    @Override
    public NutsDependencyBuilder setDependency(NutsDependency value) {
        return set(value);
    }
}
