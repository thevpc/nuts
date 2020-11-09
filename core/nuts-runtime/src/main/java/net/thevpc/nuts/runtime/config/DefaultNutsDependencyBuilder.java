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
import net.thevpc.nuts.runtime.DefaultNutsId;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.util.NutsDependencyScopes;
import net.thevpc.nuts.runtime.util.QueryStringMap;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.DefaultNutsVersion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    public NutsDependencyBuilder setScope(String scope) {
        this.scope = NutsDependencyScopes.normalizeScope(scope);
        return this;
    }

    @Override
    public NutsDependencyBuilder setOptional(String optional) {
        this.optional = CoreStringUtils.isBlank(optional) ? "false" : CoreStringUtils.trim(optional);
        return this;
    }

    @Override
    public NutsDependencyBuilder setClassifier(String classifier) {
        this.classifier = CoreStringUtils.trimToNull(classifier);
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
    public NutsDependencyBuilder setDependency(NutsDependencyBuilder value) {
        return set(value);
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
    public NutsDependencyBuilder setDependency(NutsDependency value) {
        return set(value);
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
    public String getClassifier() {
        return classifier;
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
    public NutsId[] getExclusions() {
        return exclusions == null ? new NutsId[0] : Arrays.copyOf(exclusions, exclusions.length);
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
    public String toString() {
        return build().toString();
    }
}
