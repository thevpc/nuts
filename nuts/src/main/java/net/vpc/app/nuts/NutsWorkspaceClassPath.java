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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
/**
 * 
 * @author vpc
 * @since 0.5.4
 */
final class NutsWorkspaceClassPath {

    private NutsBootId id;
    private String dependencies;
    private String repositories;

    public NutsWorkspaceClassPath(Properties properties) {
        this(
                properties.getProperty("project.id"),
                properties.getProperty("project.version"),
                properties.getProperty("project.dependencies.compile"),
                properties.getProperty("project.repositories")
        );
    }

    public NutsWorkspaceClassPath(String id, String version, String dependencies, String repositories) {
        if (NutsUtils.isBlank(id)) {
            throw new NutsIllegalArgumentException("Missing id");
        }
        if (NutsUtils.isBlank(version)) {
            throw new NutsIllegalArgumentException("Missing version");
        }
        if (NutsUtils.isBlank(dependencies)) {
            throw new NutsIllegalArgumentException("Missing dependencies");
        }
        if (repositories == null) {
            repositories = "";
        }
        this.dependencies = dependencies;
        this.repositories = repositories;
        this.id = NutsBootId.parse(id + "#" + version);
    }

    public NutsBootId getId() {
        return id;
    }

    public String getRepositoriesString() {
        return repositories;
    }

    public String getDependenciesString() {
        return dependencies;
    }

    public NutsBootId[] getDependenciesArray() {
        List<String> split = NutsUtils.split(dependencies, "\n\t ,;");
        List<NutsBootId> ts = new ArrayList<>();
        for (String s : split) {
            s = s.trim();
            if (!s.isEmpty()) {
                ts.add(NutsBootId.parse(s));
            }
        }
        return ts.toArray(new NutsBootId[0]);
    }

    public String[] getRepositoriesArray() {
        List<String> ts = NutsUtils.split(repositories, "\n;");
        for (int i = 0; i < ts.size(); i++) {
            ts.set(i, ts.get(i).trim());
        }
        return ts.toArray(new String[0]);
    }
}
