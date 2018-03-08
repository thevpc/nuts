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

import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by vpc on 1/5/17.
 */
public class NutsDependencyImpl implements NutsDependency {

    private final String namespace;
    private final String group;
    private final String name;
    private final NutsVersion version;
    private final String scope;
    private final String optional;
    private final NutsId[] exclusions;

    public NutsDependencyImpl(String namespace, String group, String name, String version, String scope, String optional, NutsId[] exclusions) {
        this.namespace = CoreStringUtils.trimToNull(namespace);
        this.group = CoreStringUtils.trimToNull(group);
        this.name = CoreStringUtils.trimToNull(name);
        this.version = new NutsVersionImpl(CoreStringUtils.trimToNull(version));
        String s = CoreStringUtils.trimToNull(scope);
        this.scope = CoreStringUtils.isEmpty(s) ? "compile" : s;
        this.optional = CoreStringUtils.isEmpty(optional) ? "false" : CoreStringUtils.trim(optional);
        this.exclusions = exclusions == null ? new NutsId[0] : Arrays.copyOf(exclusions, exclusions.length);
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
    public NutsId toId() {
        return new NutsIdImpl(
                getNamespace(),
                getGroup(),
                getName(),
                getVersion().getValue(),
                ""
        );
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
    public String getName() {
        return name;
    }

    @Override
    public String getFullName() {
        if (CoreStringUtils.isEmpty(group)) {
            return CoreStringUtils.trim(name);
        }
        return CoreStringUtils.trim(group) + ":" + CoreStringUtils.trim(name);
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
        Map<String, String> p = new TreeMap<>();
        if (!CoreStringUtils.isEmpty(scope)) {
            if (!scope.equals("compile")) {
                p.put("scope", scope);
            }
        }
        if (!CoreStringUtils.isEmpty(optional)) {
            if (!optional.equals("false")) {
                p.put("optional", optional);
            }
        }
        if (!p.isEmpty()) {
            sb.append("?");
            int i = 0;
            for (Map.Entry<String, String> e : p.entrySet()) {
                if (i > 0) {
                    sb.append('&');
                }
                sb.append(CoreStringUtils.simpleQuote(e.getKey(), true, "&="));
                if (e.getValue() != null) {
                    sb.append('=');
                    sb.append(CoreStringUtils.simpleQuote(e.getValue(), true, "&="));
                }
                i++;
            }
        }
        return sb.toString();
    }

    @Override
    public NutsId[] getExclusions() {
        return Arrays.copyOf(exclusions, exclusions.length);
    }
}
