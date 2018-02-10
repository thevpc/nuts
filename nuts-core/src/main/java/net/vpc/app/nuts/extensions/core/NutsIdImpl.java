/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.app.nuts.StringMapper;

import java.util.Map;

/**
 * Created by vpc on 1/5/17.
 */
public class NutsIdImpl implements NutsId {

    private final String namespace;
    private final String group;
    private final String name;
    private final NutsVersionImpl version;
    private final String query;

    public NutsIdImpl(String namespace, String group, String name, String version, Map<String, String> query) {
        StringBuilder sb = new StringBuilder();
        if (query != null) {
            for (Map.Entry<String, String> entry : query.entrySet()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        this.namespace = CoreStringUtils.trimToNull(namespace);
        this.group = CoreStringUtils.trimToNull(group);
        this.name = CoreStringUtils.trimToNull(name);
        this.version = new NutsVersionImpl(CoreStringUtils.trimToNull(version));
        this.query = CoreStringUtils.trimToNull(sb.toString());
    }

    public NutsIdImpl(String namespace, String group, String name, String version, String query) {
        this.namespace = CoreStringUtils.trimToNull(namespace);
        this.group = CoreStringUtils.trimToNull(group);
        this.name = CoreStringUtils.trimToNull(name);
        this.version = new NutsVersionImpl(CoreStringUtils.trimToNull(version));
        this.query = CoreStringUtils.trimToNull(query);
    }

    @Override
    public boolean isSameFullName(NutsId other) {
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
    public NutsIdImpl setGroup(String newGroupId) {
        return new NutsIdImpl(
                namespace,
                newGroupId,
                name,
                version.getValue(),
                query
        );
    }

    @Override
    public NutsId setNamespace(String newNamespace) {
        return new NutsIdImpl(
                newNamespace,
                group,
                name,
                version.getValue(),
                query
        );
    }

    @Override
    public NutsId setVersion(String newVersion) {
        return new NutsIdImpl(
                namespace,
                group,
                name,
                newVersion,
                query
        );
    }

    @Override
    public NutsId setName(String newName) {
        return new NutsIdImpl(
                namespace,
                group,
                newName,
                version.getValue(),
                query
        );
    }

    @Override
    public String getFace() {
        String s = getQueryMap().get(NutsConstants.QUERY_FACE);
        return CoreStringUtils.trimToNull(s);
    }

    @Override
    public NutsId setFace(String value) {
        return setQueryProperty(NutsConstants.QUERY_FACE, CoreStringUtils.trimToNull(value))
                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
    }

    @Override
    public NutsId setQueryProperty(String property, String value) {
        Map<String, String> m = getQueryMap();
        if (value == null) {
            m.remove(property);
        } else {
            m.put(property, value);
        }
        return setQuery(m);
    }

    @Override
    public NutsId setQuery(Map<String, String> queryMap, boolean merge) {
        if (merge) {
            Map<String, String> m = getQueryMap();
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
            return setQuery(m);
        } else {
            return new NutsIdImpl(
                    namespace,
                    group,
                    name,
                    version.getValue(),
                    queryMap
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
        return new NutsIdImpl(
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
        return CoreStringUtils.parseMap(getQuery(), "&");
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
        if (CoreStringUtils.isEmpty(group)) {
            return CoreStringUtils.trim(name);
        }
        return CoreStringUtils.trim(group) + ":" + CoreStringUtils.trim(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NutsVersionImpl getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!CoreStringUtils.isEmpty(namespace)) {
            sb.append(namespace).append("://");
        }
        if (!CoreStringUtils.isEmpty(group)) {
            sb.append(group).append(":");
        }
        sb.append(name);
        if (!CoreStringUtils.isEmpty(version.getValue())) {
            sb.append("#").append(version);
        }
        if (!CoreStringUtils.isEmpty(query)) {
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

        NutsIdImpl nutsId = (NutsIdImpl) o;

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
    public NutsId apply(StringMapper properties) {
        return new NutsIdImpl(
                CoreNutsUtils.applyStringProperties(this.getNamespace(), properties),
                CoreNutsUtils.applyStringProperties(this.getGroup(), properties),
                CoreNutsUtils.applyStringProperties(this.getName(), properties),
                CoreNutsUtils.applyStringProperties(this.getVersion().getValue(), properties),
                CoreNutsUtils.applyMapProperties(this.getQueryMap(), properties)
        );
    }

}
