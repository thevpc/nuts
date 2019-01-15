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
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.Map;

public interface NutsIdBuilder extends Serializable {

    NutsIdBuilder setGroup(String newGroupId);

    NutsIdBuilder setNamespace(String newNamespace);

    NutsIdBuilder setVersion(String newVersion);

    NutsIdBuilder setVersion(NutsVersion version);

    NutsIdBuilder setName(String newName);

    String getFace();

    String getAlternative();

    String getClassifier();

    NutsIdBuilder setFace(String value);

    NutsIdBuilder setAlternative(String value);

    NutsIdBuilder setClassifier(String value);

    NutsIdBuilder setQueryProperty(String property, String value);

    NutsIdBuilder setQuery(Map<String, String> queryMap, boolean merge);

    NutsIdBuilder setQuery(Map<String, String> queryMap);

    NutsIdBuilder unsetQuery();

    NutsIdBuilder setQuery(String query);

    String getQuery();

    Map<String, String> getQueryMap();

    String getNamespace();

    String getGroup();

    String getFullName();

    String getName();

    NutsVersion getVersion();

    NutsIdBuilder apply(NutsObjectConverter<String, String> properties);

    NutsId build();
}
