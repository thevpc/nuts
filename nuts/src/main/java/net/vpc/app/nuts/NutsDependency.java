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

import net.vpc.app.nuts.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/5/17.
 */
public class NutsDependency {
    public static final Pattern NUTS_DESCRIPTOR_PATTERN = Pattern.compile("^(([a-zA-Z0-9_${}-]+)://)?([a-zA-Z0-9_.${}-]+)(:([a-zA-Z0-9_.${}-]+))?(#(?<version>[^?]+))?(\\?(?<face>.+))?$");
    private static Set<String> SUPPORTED_PARAMS = new HashSet<>(Arrays.asList("scope", "optional"));

    private final String namespace;
    private final String group;
    private final String name;
    private final NutsVersion version;
    private final String scope;
    private final String optional;

    public NutsDependency(String namespace, String group, String name, String version, String scope, String optional) {
        this.namespace = StringUtils.trimToNull(namespace);
        this.group = StringUtils.trimToNull(group);
        this.name = StringUtils.trimToNull(name);
        this.version = new NutsVersion(StringUtils.trimToNull(version));
        String s = StringUtils.trimToNull(scope);
        this.scope = StringUtils.isEmpty(s) ? "compile" : s;
        this.optional = StringUtils.isEmpty(optional) ? "false" : StringUtils.trim(optional);
    }

    public static NutsDependency parseOrError(String nutFormat) {
        NutsDependency id = parse(nutFormat);
        if (id == null) {
            throw new NutsIdInvalidFormatException("Invalid Dependency format : " + nutFormat);
        }
        return id;
    }

    public static NutsDependency parse(String nutFormat) {
        if (nutFormat == null) {
            return null;
        }
        Matcher m = NUTS_DESCRIPTOR_PATTERN.matcher(nutFormat);
        if (m.find()) {
            String protocol = m.group(2);
            String group = m.group(3);
            String name = m.group(5);
            String version = m.group(7);
            String face = StringUtils.trim(m.group(9));
            Map<String, String> scope = StringUtils.parseMap(face, "&");
            for (String s : scope.keySet()) {
                if (!SUPPORTED_PARAMS.contains(s)) {
                    throw new IllegalArgumentException("Unsupported parameter " + StringUtils.simpleQuote(s, false, "") + " in " + nutFormat);
                }
            }
            if (name == null) {
                name = group;
                group = null;
            }
            return new NutsDependency(
                    protocol,
                    group,
                    name,
                    version,
                    scope.get("scope"),
                    scope.get("optional")
            );
        }
        return null;
    }

    public boolean isOptional() {
        return Boolean.parseBoolean(optional);
    }

    public String getOptional() {
        return optional;
    }

    public String getScope() {
        return scope;
    }

//    public static void main(String[] args) {
//        NutsDependency parsed = parse("a:b#2.3?scope=compile&optional=true");
//        System.out.println(parsed);
//    }

    public NutsId toId() {
        return new NutsId(
                getNamespace(),
                getGroup(),
                getName(),
                getVersion().getValue(),
                ""
        );
    }

    public String getNamespace() {
        return namespace;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        if (StringUtils.isEmpty(group)) {
            return StringUtils.trim(name);
        }
        return StringUtils.trim(group) + ":" + StringUtils.trim(name);
    }

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
        Map<String, String> p = new TreeMap<>();
        if (!StringUtils.isEmpty(scope)) {
            if (!scope.equals("compile")) {
                p.put("scope", scope);
            }
        }
        if (!StringUtils.isEmpty(optional)) {
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
                sb.append(StringUtils.simpleQuote(e.getKey(), true, "&="));
                if (e.getValue() != null) {
                    sb.append('=');
                    sb.append(StringUtils.simpleQuote(e.getValue(), true, "&="));
                }
                i++;
            }
        }
        return sb.toString();
    }

}
