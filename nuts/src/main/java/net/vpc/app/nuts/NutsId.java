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

import java.io.Serializable;
import java.util.Map;

public interface NutsId extends Serializable{

    boolean isSameFullName(NutsId other);

    boolean anyContains(String value);

    boolean anyMatches(String pattern);

    boolean anyLike(String pattern);

    boolean like(String pattern);

    boolean namespaceLike(String pattern);

    boolean groupLike(String pattern);

    boolean nameLike(String pattern);

    boolean versionLike(String pattern);

    boolean queryLike(String pattern);

    NutsId setGroup(String newGroupId);

    NutsId setNamespace(String newNamespace);

    NutsId setVersion(String newVersion);

    NutsId setName(String newName);

    String getFace();

    NutsId setFace(String value);

    NutsId setQueryProperty(String property, String value);

    NutsId setQuery(Map<String, String> queryMap, boolean merge);

    NutsId setQuery(Map<String, String> queryMap);

    NutsId unsetQuery();

    NutsId setQuery(String query);

    String getQuery();

    Map<String, String> getQueryMap();

    String getNamespace();

    String getGroup();

    String getFullName();

    String getName();

    NutsVersion getVersion();

    NutsId apply(ObjectConverter<String,String> properties);

    NutsIdBuilder builder();
}
