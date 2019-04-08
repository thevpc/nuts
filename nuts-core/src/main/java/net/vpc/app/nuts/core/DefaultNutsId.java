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
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsId implements NutsId {

    private final String namespace;
    private final String group;
    private final String name;
    private final NutsVersion version;
    private final String query;

    public DefaultNutsId(String namespace, String group, String name, String version, Map<String, String> query) {
        this(namespace, group, name, DefaultNutsVersion.valueOf(version), query);
    }

    protected DefaultNutsId(String namespace, String group, String name, NutsVersion version, Map<String, String> query) {
        this.namespace = CoreStringUtils.trimToNull(namespace);
        this.group = CoreStringUtils.trimToNull(group);
        this.name = CoreStringUtils.trimToNull(name);
        this.version = version == null ? DefaultNutsVersion.EMPTY : version;
        this.query = formatQuery(query);
    }

    public static String formatQuery(Map<String, String> query) {
        StringBuilder sb = new StringBuilder();
        if (query != null) {
            Set<String> sortedKeys = new TreeSet<>(query.keySet());
            for (String k : sortedKeys) {
                String v = query.get(k);
                switch (k) {
                    case "face": 
                    case "alternative": 
                    {
                        if ("default".equals(v)) {
                            v = null;
                        }
                        break;
                    }
                }
                if (v != null && v.length() > 0) {
                    if (sb.length() > 0) {
                        sb.append("&");
                    }
                    sb.append(k).append("=").append(v);
                }
            }
        }
        return CoreStringUtils.trimToNull(sb.toString());
    }

    protected DefaultNutsId(String namespace, String group, String name, NutsVersion version, String query) {
        this.namespace = CoreStringUtils.trimToNull(namespace);
        this.group = CoreStringUtils.trimToNull(group);
        this.name = CoreStringUtils.trimToNull(name);
        this.version = version == null ? DefaultNutsVersion.EMPTY : version;
        this.query = CoreStringUtils.trimToNull(query);
    }

    public DefaultNutsId(String group, String name, String version) {
        this(null, group, name, version, (String) null);
    }

    public DefaultNutsId(String namespace, String group, String name, String version, String query) {
        this.namespace = CoreStringUtils.trimToNull(namespace);
        this.group = CoreStringUtils.trimToNull(group);
        this.name = CoreStringUtils.trimToNull(name);
        this.version = DefaultNutsVersion.valueOf(version);
        this.query = CoreStringUtils.trimToNull(query);
    }

    @Override
    public boolean equalsSimpleName(NutsId other) {
        if (other == null) {
            return false;
        }
        return CoreStringUtils.trim(name).equals(CoreStringUtils.trim(other.getName()))
                && CoreStringUtils.trim(group).equals(CoreStringUtils.trim(other.getGroup()));
    }

    @Override
    public boolean anyContains(String value) {
        if (value == null) {
            return true;
        }
        if (CoreStringUtils.trim(namespace).contains(value)) {
            return true;
        }
        if (CoreStringUtils.trim(name).contains(value)) {
            return true;
        }
        if (CoreStringUtils.trim(version.getValue()).contains(value)) {
            return true;
        }
        return CoreStringUtils.trim(query).contains(value);
    }

    @Override
    public boolean anyMatches(String pattern) {
        if (pattern == null) {
            return true;
        }
        if (CoreStringUtils.trim(namespace).matches(pattern)) {
            return true;
        }
        if (CoreStringUtils.trim(name).matches(pattern)) {
            return true;
        }
        if (CoreStringUtils.trim(version.getValue()).matches(pattern)) {
            return true;
        }
        return CoreStringUtils.trim(query).matches(pattern);
    }

    @Override
    public boolean anyLike(String pattern) {
        if (pattern == null) {
            return true;
        }
        return anyMatches(CoreStringUtils.simpexpToRegexp(pattern));
    }

    @Override
    public boolean like(String pattern) {
        if (pattern == null) {
            return true;
        }
        return toString().matches(CoreStringUtils.simpexpToRegexp(pattern));
    }

    @Override
    public boolean namespaceLike(String pattern) {
        if (pattern == null) {
            return true;
        }
        return CoreStringUtils.trim(namespace).matches(CoreStringUtils.simpexpToRegexp(pattern));
    }

    @Override
    public boolean nameLike(String pattern) {
        if (pattern == null) {
            return true;
        }
        return CoreStringUtils.trim(name).matches(CoreStringUtils.simpexpToRegexp(pattern));
    }

    @Override
    public boolean groupLike(String pattern) {
        if (pattern == null) {
            return true;
        }
        return CoreStringUtils.trim(group).matches(CoreStringUtils.simpexpToRegexp(pattern));
    }

    @Override
    public boolean versionLike(String pattern) {
        if (pattern == null) {
            return true;
        }
        return CoreStringUtils.trim(version.getValue()).matches(CoreStringUtils.simpexpToRegexp(pattern));
    }

    @Override
    public boolean queryLike(String pattern) {
        if (pattern == null) {
            return true;
        }
        return CoreStringUtils.trim(query).matches(CoreStringUtils.simpexpToRegexp(pattern));
    }

    @Override
    public DefaultNutsId setGroup(String newGroup) {
        if (CoreStringUtils.trim(group).equals(CoreStringUtils.trim(newGroup))) {
            return this;
        }
        return new DefaultNutsId(
                namespace,
                newGroup,
                name,
                version,
                query
        );
    }

    @Override
    public NutsId setNamespace(String newNamespace) {
        if (CoreStringUtils.trim(namespace).equals(CoreStringUtils.trim(newNamespace))) {
            return this;
        }
        return new DefaultNutsId(
                newNamespace,
                group,
                name,
                version,
                query
        );
    }

    @Override
    public NutsId setVersion(NutsVersion newVersion) {
        if(newVersion==null){
            newVersion=DefaultNutsVersion.EMPTY;
        }
        if (newVersion.equals(version)) {
            return this;
        }
        return new DefaultNutsId(
                namespace,
                group,
                name,
                newVersion,
                query
        );
    }

    @Override
    public NutsId setVersion(String newVersion) {
        NutsVersion nv = DefaultNutsVersion.valueOf(newVersion);
        if (nv.equals(version)) {
            return this;
        }
        return new DefaultNutsId(
                namespace,
                group,
                name,
                newVersion,
                query
        );
    }

    @Override
    public NutsId setName(String newName) {
        if (CoreStringUtils.trim(name).equals(CoreStringUtils.trim(newName))) {
            return this;
        }
        return new DefaultNutsId(
                namespace,
                group,
                newName,
                version,
                query
        );
    }

    @Override
    public String getFace() {
        String s = getQueryMap().get(NutsConstants.QueryKeys.FACE);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public String getScope() {
        String s = getQueryMap().get(NutsConstants.QueryKeys.SCOPE);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public String getAlternative() {
        String s = getQueryMap().get(NutsConstants.QueryKeys.ALTERNATIVE);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public String getClassifier() {
        String s = getQueryMap().get("classifier");
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public NutsId setFace(String value) {
        if (NutsConstants.QueryKeys.FACE_DEFAULT_VALUE.equals(value)) {
            value = null;
        }
        return setQueryProperty(NutsConstants.QueryKeys.FACE, CoreStringUtils.trimToNull(value))
                .setQuery(CoreNutsUtils.QUERY_EMPTY_ENV, true);
    }

    @Override
    public NutsId setScope(String value) {
        return setQueryProperty(NutsConstants.QueryKeys.SCOPE, CoreStringUtils.trimToNull(value))
                .setQuery(CoreNutsUtils.QUERY_EMPTY_ENV, true);
    }

    @Override
    public NutsId setOptional(String value) {
        return setQueryProperty(NutsConstants.QueryKeys.OPTIONAL, CoreStringUtils.trimToNull(value))
                .setQuery(CoreNutsUtils.QUERY_EMPTY_ENV, true);
    }

    @Override
    public NutsId setAlternative(String value) {
        if (NutsConstants.QueryKeys.ALTERNATIVE_DEFAULT_VALUE.equals(value)) {
            value = null;
        }
        return setQueryProperty(NutsConstants.QueryKeys.ALTERNATIVE, CoreStringUtils.trimToNull(value))
                .setQuery(CoreNutsUtils.QUERY_EMPTY_ENV, true);
    }

    @Override
    public NutsId setArch(String value) {
        return setQueryProperty(NutsConstants.QueryKeys.ARCH, CoreStringUtils.trimToNull(value))
                .setQuery(CoreNutsUtils.QUERY_EMPTY_ENV, true);
    }

    @Override
    public NutsId setPackaging(String value) {
        return setQueryProperty(NutsConstants.QueryKeys.PACKAGING, CoreStringUtils.trimToNull(value));
    }

    @Override
    public NutsId setPlatform(String value) {
        return setQueryProperty(NutsConstants.QueryKeys.PLATFORM, CoreStringUtils.trimToNull(value));
    }

    @Override
    public NutsId setOsdist(String value) {
        return setQueryProperty(NutsConstants.QueryKeys.OSDIST, CoreStringUtils.trimToNull(value));
    }

    @Override
    public NutsId setOs(String value) {
        return setQueryProperty(NutsConstants.QueryKeys.OS, CoreStringUtils.trimToNull(value));
    }

    @Override
    public String getOs() {
        String s = getQueryMap().get(NutsConstants.QueryKeys.OS);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public String getOsdist() {
        String s = getQueryMap().get(NutsConstants.QueryKeys.OSDIST);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public String getPlatform() {
        String s = getQueryMap().get(NutsConstants.QueryKeys.PLATFORM);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public String getArch() {
        String s = getQueryMap().get(NutsConstants.QueryKeys.ARCH);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public NutsId setFaceComponent() {
        return setFace(NutsConstants.QueryFaces.COMPONENT);
    }

    @Override
    public NutsId setFaceDescriptor() {
        return setFace(NutsConstants.QueryFaces.DESCRIPTOR);
    }

    @Override
    public NutsId setQueryProperty(String property, String value) {
        if (value == null || value.length() == 0) {
            if (query != null && !query.isEmpty()) {
                Map<String, String> m = getQueryMap();
                m.remove(property);
                return setQuery(m);
            }
            return this;
        } else {
            Map<String, String> m = getQueryMap();
            m.put(property, value);
            return setQuery(m);
        }
    }

    @Override
    public NutsId setQuery(Map<String, String> queryMap, boolean merge) {
        if (merge) {
            Map<String, String> m = getQueryMap();
            if (queryMap != null) {
                for (Map.Entry<String, String> e : queryMap.entrySet()) {
                    String property = e.getKey();
                    String value = e.getValue();
                    if (value == null || value.isEmpty()) {
                        m.remove(property);
                    } else {
                        m.put(property, value);
                    }
                }
            }
            return setQuery(m);
        } else {
            String m = DefaultNutsId.formatQuery(queryMap);
            if (m == null) {
                m = "";
            }
            if (m.equals(query == null ? "" : query)) {
                return this;
            }
            return new DefaultNutsId(
                    namespace,
                    group,
                    name,
                    version,
                    m
            );
        }
    }

    @Override
    public NutsId setQuery(Map<String, String> queryMap) {
        return setQuery(queryMap, false);
    }

    @Override
    public NutsId unsetQuery() {
        return setQuery("");
    }

    @Override
    public NutsId setQuery(String query) {
        if (CoreStringUtils.trim(this.query).equals(query)) {
            return this;
        }
        return new DefaultNutsId(
                namespace,
                group,
                name,
                version,
                query
        );
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public Map<String, String> getQueryMap() {
        String q = getQuery();
        if (q == null || q.equals("")) {
            return new LinkedHashMap<>();
        }
        return CoreStringUtils.parseMap(q, "&");
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
    public NutsId getSimpleNameId() {
        return new DefaultNutsId(null, group, name, (NutsVersion) null, "");
    }

    @Override
    public NutsId getLongNameId() {
        return new DefaultNutsId(null, group, name, version, "");
    }

    @Override
    public String getSimpleName() {
        if (CoreStringUtils.isBlank(group)) {
            return CoreStringUtils.trim(name);
        }
        return CoreStringUtils.trim(group) + ":" + CoreStringUtils.trim(name);
    }

    @Override
    public String getLongName() {
        String s = getSimpleName();
        NutsVersion v = getVersion();
        if (v.isEmpty()) {
            return s;
        }
        return s + "#" + v;
    }

    @Override
    public String getFullName() {
        return toString();
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
        if (!CoreStringUtils.isBlank(namespace)) {
            sb.append(namespace).append("://");
        }
        if (!CoreStringUtils.isBlank(group)) {
            sb.append(group).append(":");
        }
        sb.append(name);
        if (!version.isEmpty()) {
            sb.append("#").append(version);
        }
        if (!CoreStringUtils.isBlank(query)) {
            sb.append("?");
            sb.append(query);
        }
        return sb.toString();
    }

    public boolean isOptional() {
        return Boolean.parseBoolean(getOptional());
    }

    @Override
    public String getOptional() {
        String s = getQueryMap().get(NutsConstants.QueryKeys.OPTIONAL);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultNutsId nutsId = (DefaultNutsId) o;

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
    public NutsId apply(Function<String, String> properties) {
        return new DefaultNutsId(
                CoreNutsUtils.applyStringProperties(this.getNamespace(), properties),
                CoreNutsUtils.applyStringProperties(this.getGroup(), properties),
                CoreNutsUtils.applyStringProperties(this.getName(), properties),
                CoreNutsUtils.applyStringProperties(this.getVersion().getValue(), properties),
                CoreNutsUtils.applyMapProperties(this.getQueryMap(), properties)
        );
    }

    @Override
    public NutsIdBuilder builder() {
        return new DefaultNutsIdBuilder(this);
    }
}
