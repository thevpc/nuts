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
package net.vpc.app.nuts;

import net.vpc.app.nuts.util.NutsUtils;
import net.vpc.app.nuts.util.StringUtils;

import java.util.Map;

/**
 * Created by vpc on 1/5/17.
 */
public class NutsId {

    private final String namespace;
    private final String group;
    private final String name;
    private final NutsVersion version;
    private final String query;

    public NutsId(String namespace, String group, String name, String version, Map<String,String> query) {
        StringBuilder sb=new StringBuilder();
        if(query !=null) {
            for (Map.Entry<String, String> entry : query.entrySet()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        this.namespace = StringUtils.trimToNull(namespace);
        this.group = StringUtils.trimToNull(group);
        this.name = StringUtils.trimToNull(name);
        this.version = new NutsVersion(StringUtils.trimToNull(version));
        this.query = StringUtils.trimToNull(sb.toString());
    }
    public NutsId(String namespace, String group, String name, String version, String query) {
        this.namespace = StringUtils.trimToNull(namespace);
        this.group = StringUtils.trimToNull(group);
        this.name = StringUtils.trimToNull(name);
        this.version = new NutsVersion(StringUtils.trimToNull(version));
        this.query = StringUtils.trimToNull(query);
    }

    public boolean isSameFullName(NutsId other) {
        if (other == null) {
            return false;
        }
        return StringUtils.trim(name).equals(StringUtils.trim(other.name))
                && StringUtils.trim(group).equals(StringUtils.trim(other.group));
    }

    public boolean anyContains(String value) {
        if (value == null) {
            return true;
        }
        if (StringUtils.trim(namespace).contains(value)) {
            return true;
        }
        if (StringUtils.trim(name).contains(value)) {
            return true;
        }
        if (StringUtils.trim(version.getValue()).contains(value)) {
            return true;
        }
        if (StringUtils.trim(query).contains(value)) {
            return true;
        }
        return false;
    }

    public boolean anyMatches(String pattern) {
        if (pattern == null) {
            return true;
        }
        if (StringUtils.trim(namespace).matches(pattern)) {
            return true;
        }
        if (StringUtils.trim(name).matches(pattern)) {
            return true;
        }
        if (StringUtils.trim(version.getValue()).matches(pattern)) {
            return true;
        }
        if (StringUtils.trim(query).matches(pattern)) {
            return true;
        }
        return false;
    }

    public boolean anyLike(String pattern) {
        if (pattern == null) {
            return true;
        }
        return anyMatches(StringUtils.simpexpToRegexp(pattern, false));
    }

    public boolean like(String pattern) {
        if (pattern == null) {
            return true;
        }
        return toString().matches(StringUtils.simpexpToRegexp(pattern, false));
    }

    public boolean namespaceLike(String pattern) {
        if (pattern == null) {
            return true;
        }
        return StringUtils.trim(namespace).matches(StringUtils.simpexpToRegexp(pattern, false));
    }

    public boolean nameLike(String pattern) {
        if (pattern == null) {
            return true;
        }
        return StringUtils.trim(name).matches(StringUtils.simpexpToRegexp(pattern, false));
    }

    public boolean versionLike(String pattern) {
        if (pattern == null) {
            return true;
        }
        return StringUtils.trim(version.getValue()).matches(StringUtils.simpexpToRegexp(pattern, false));
    }

    public boolean queryLike(String pattern) {
        if (pattern == null) {
            return true;
        }
        return StringUtils.trim(query).matches(StringUtils.simpexpToRegexp(pattern, false));
    }

    /**
     * examples : script://groupId:artifactId/version?query
     * script://groupId:artifactId/version script://groupId:artifactId
     * script://artifactId artifactId
     *
     * @param nutFormat
     * @return
     */
    public static NutsId parse(String nutFormat) {
        return NutsUtils.parseNutsId(nutFormat);
    }

    public static NutsId parseOrError(String nutFormat) {
        return NutsUtils.parseOrErrorNutsId(nutFormat);
    }

    public static NutsId parseNullableOrError(String nutFormat) {
        return NutsUtils.parseNullableOrErrorNutsId(nutFormat);
    }

    public NutsId setGroup(String newGroupId) {
        return new NutsId(
                namespace,
                newGroupId,
                name,
                version.getValue(),
                query
        );
    }

    public NutsId setNamespace(String newNamespace) {
        return new NutsId(
                newNamespace,
                group,
                name,
                version.getValue(),
                query
        );
    }

    public NutsId setVersion(String newVersion) {
        return new NutsId(
                namespace,
                group,
                name,
                newVersion,
                query
        );
    }

    public String getFace() {
        String s = getQueryMap().get(NutsConstants.QUERY_FACE);
        return StringUtils.trimToNull(s);
    }

    public NutsId setFace(String value) {
        return setQueryProperty(NutsConstants.QUERY_FACE,StringUtils.trimToNull(value))
                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true)
                ;
    }
    public NutsId setQueryProperty(String property, String value) {
        Map<String, String> m = getQueryMap();
        if(value==null){
            m.remove(property);
        }else{
            m.put(property,value);
        }
        return setQuery(m);
    }

    public NutsId setQuery(Map<String, String> queryMap, boolean merge) {
        if(merge) {
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
        }else{
            return new NutsId(
                    namespace,
                    group,
                    name,
                    version.getValue(),
                    queryMap
            );
        }
    }

    public NutsId setQuery(Map<String,String> queryMap) {
        return setQuery(queryMap,false);
    }

    public NutsId unsetQuery() {
        return setQuery("");
    }

    public NutsId setQuery(String query) {
        return new NutsId(
                namespace,
                group,
                name,
                version.getValue(),
                query
        );
    }

    public String getQuery() {
        return query;
    }

    public Map<String, String> getQueryMap() {
        return StringUtils.parseMap(getQuery(), "&");
    }

    public String getNamespace() {
        return namespace;
    }

    public String getGroup() {
        return group;
    }

    public String getFullName() {
        if(StringUtils.isEmpty(group)){
            return StringUtils.trim(name);
        }
        return StringUtils.trim(group)+":"+StringUtils.trim(name);
    }

    public String getName() {
        return name;
    }

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

        NutsId nutsId = (NutsId) o;

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
}
