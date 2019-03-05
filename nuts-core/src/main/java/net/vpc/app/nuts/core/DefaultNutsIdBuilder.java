/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.common.strings.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsIdBuilder implements NutsIdBuilder {

    private String namespace;
    private String group;
    private String name;
    private NutsVersion version;
    private String query;

    public DefaultNutsIdBuilder() {
    }

    public DefaultNutsIdBuilder(NutsId id) {
        setNamespace(id.getNamespace());
        setGroup(id.getGroup());
        setName(id.getName());
        setVersion(id.getVersion());
    }

    public DefaultNutsIdBuilder(String namespace, String group, String name, String version, Map<String, String> query) {
        this.namespace = StringUtils.trimToNull(namespace);
        this.group = StringUtils.trimToNull(group);
        this.name = StringUtils.trimToNull(name);
        this.version = DefaultNutsVersion.valueOf(version);
        this.query = DefaultNutsId.formatQuery(query);
    }

    public DefaultNutsIdBuilder(String namespace, String group, String name, String version, String query) {
        this.namespace = StringUtils.trimToNull(namespace);
        this.group = StringUtils.trimToNull(group);
        this.name = StringUtils.trimToNull(name);
        this.version = DefaultNutsVersion.valueOf(version);
        this.query = StringUtils.trimToNull(query);
    }


    @Override
    public NutsIdBuilder setGroup(String group) {
        this.group = StringUtils.trimToNull(group);
        return this;
    }

    @Override
    public NutsIdBuilder setNamespace(String namespace) {
        this.namespace = StringUtils.trimToNull(namespace);
        return this;
    }

    @Override
    public NutsIdBuilder setVersion(NutsVersion version) {
        this.version = version;
        return this;
    }

    @Override
    public NutsIdBuilder setVersion(String version) {
        this.version = DefaultNutsVersion.valueOf(version);
        return this;
    }

    @Override
    public DefaultNutsIdBuilder setName(String name) {
        this.name = StringUtils.trimToNull(name);
        return this;
    }

    @Override
    public String getFace() {
        String s = getQueryMap().get(NutsConstants.QUERY_FACE);
        return StringUtils.trimToNull(s);
    }

    @Override
    public String getAlternative() {
        String s = getQueryMap().get(NutsConstants.QUERY_ALTERNATIVE);
        return StringUtils.trimToNull(s);
    }

    @Override
    public String getOs() {
        String s = getQueryMap().get(NutsConstants.QUERY_OS);
        return StringUtils.trimToNull(s);
    }

    @Override
    public String getOsdist() {
        String s = getQueryMap().get(NutsConstants.QUERY_OSDIST);
        return StringUtils.trimToNull(s);
    }

    @Override
    public String getPlatform() {
        String s = getQueryMap().get(NutsConstants.QUERY_PLATFORM);
        return StringUtils.trimToNull(s);
    }

    @Override
    public String getArch() {
        String s = getQueryMap().get(NutsConstants.QUERY_ARCH);
        return StringUtils.trimToNull(s);
    }

    @Override
    public NutsIdBuilder setFace(String value) {
        return setQueryProperty(NutsConstants.QUERY_FACE, StringUtils.trimToNull(value));
//                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
    }

    @Override
    public NutsIdBuilder setFaceComponent() {
        return setFace(NutsConstants.FACE_COMPONENT);
    }

    @Override
    public NutsIdBuilder setFaceDescriptor() {
        return setFace(NutsConstants.FACE_DESCRIPTOR);
    }

    @Override
    public NutsIdBuilder setAlternative(String value) {
        return setQueryProperty(NutsConstants.QUERY_ALTERNATIVE, StringUtils.trimToNull(value));
//                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
    }

    @Override
    public String getClassifier() {
        String s = getQueryMap().get("classifier");
        return StringUtils.trimToNull(s);
    }

    @Override
    public NutsIdBuilder setClassifier(String value) {
        return setQueryProperty("classifier", StringUtils.trimToNull(value));
//                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
    }

    @Override
    public NutsIdBuilder setScope(String value) {
        return setQueryProperty(NutsConstants.QUERY_SCOPE, StringUtils.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setOptional(String value) {
        return setQueryProperty(NutsConstants.QUERY_OPTIONAL, StringUtils.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setPackaging(String value) {
        return setQueryProperty(NutsConstants.QUERY_PACKAGING, StringUtils.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setPlatform(String value) {
        return setQueryProperty(NutsConstants.QUERY_PLATFORM, StringUtils.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setArch(String value) {
        return setQueryProperty(NutsConstants.QUERY_ARCH, StringUtils.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setOs(String value) {
        return setQueryProperty(NutsConstants.QUERY_OSDIST, StringUtils.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setOsdist(String value) {
        return setQueryProperty(NutsConstants.QUERY_OS, StringUtils.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setQueryProperty(String property, String value) {
        Map<String, String> m = getQueryMap();
        if (value == null) {
            m.remove(property);
        } else {
            m.put(property, value);
        }
        return setQuery(m);
    }

    @Override
    public NutsIdBuilder setQuery(Map<String, String> queryMap, boolean merge) {
        Map<String, String> m = null;
        if (merge) {
            m = getQueryMap();
            if (queryMap != null) {
                for (Map.Entry<String, String> e : queryMap.entrySet()) {
                    String property = e.getKey();
                    String value = e.getValue();
                    if (value == null) {
                        m.remove(property);
                    } else {
                        m.put(property, value);
                    }
                }
            }
        } else {
            m = new HashMap<>();
            if (queryMap != null) {
                m.putAll(queryMap);
            }
        }
        this.query = DefaultNutsId.formatQuery(m);
        return this;
    }

    @Override
    public NutsIdBuilder setQuery(Map<String, String> queryMap) {
        return setQuery(queryMap, false);
    }

    @Override
    public NutsIdBuilder unsetQuery() {
        return setQuery("");
    }

    @Override
    public NutsIdBuilder setQuery(String query) {
        return new DefaultNutsIdBuilder(
                namespace,
                group,
                name,
                version.getValue(),
                query
        );
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public Map<String, String> getQueryMap() {
        return StringUtils.parseMap(getQuery(), "&");
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getFullName() {
        if (StringUtils.isEmpty(group)) {
            return StringUtils.trim(name);
        }
        return StringUtils.trim(group) + ":" + StringUtils.trim(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NutsVersion getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(namespace)) {
            sb.append(namespace).append("://");
        }
        if (!StringUtils.isEmpty(group)) {
            sb.append(group).append(":");
        }
        sb.append(name);
        if (!StringUtils.isEmpty(version.getValue())) {
            sb.append("#").append(version);
        }
        if (!StringUtils.isEmpty(query)) {
            sb.append("?");
            sb.append(query);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultNutsIdBuilder nutsId = (DefaultNutsIdBuilder) o;

        if (namespace != null ? !namespace.equals(nutsId.namespace) : nutsId.namespace != null) {
            return false;
        }
        if (group != null ? !group.equals(nutsId.group) : nutsId.group != null) {
            return false;
        }
        if (name != null ? !name.equals(nutsId.name) : nutsId.name != null) {
            return false;
        }
        if (version != null ? !version.equals(nutsId.version) : nutsId.version != null) {
            return false;
        }
        return query != null ? query.equals(nutsId.query) : nutsId.query == null;

    }

    @Override
    public int hashCode() {
        int result = namespace != null ? namespace.hashCode() : 0;
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (query != null ? query.hashCode() : 0);
        return result;
    }

    @Override
    public NutsIdBuilder apply(NutsObjectConverter<String, String> properties) {
        setNamespace(CoreNutsUtils.applyStringProperties(this.getNamespace(), properties));
        setGroup(CoreNutsUtils.applyStringProperties(this.getGroup(), properties));
        setName(CoreNutsUtils.applyStringProperties(this.getName(), properties));
        setVersion(CoreNutsUtils.applyStringProperties(this.getVersion().getValue(), properties));
        setQuery(CoreNutsUtils.applyMapProperties(this.getQueryMap(), properties));
        return this;
    }

    @Override
    public NutsId build() {
        return new DefaultNutsId(
                namespace, group, name, version == null ? null : version.getValue(), query
        );
    }
}
