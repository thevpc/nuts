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
package net.vpc.app.nuts.core.bridges.maven;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenNutsRepositoryFactoryComponent implements NutsRepositoryFactoryComponent {

    private static final NutsRepositoryDefinition[] DEFAULTS = {
            new NutsRepositoryDefinition("maven-local", System.getProperty("maven-local", "~/.m2/repository"), NutsConstants.REPOSITORY_TYPE_NUTS_MAVEN, Boolean.getBoolean("nuts.cache.cache-local-files"),NutsRepositoryDefinition.ORDER_USER_LOCAL),
            new NutsRepositoryDefinition("maven-central", "http://repo.maven.apache.org/maven2/", NutsConstants.REPOSITORY_TYPE_NUTS_MAVEN, true,NutsRepositoryDefinition.ORDER_USER_REMOTE),
            new NutsRepositoryDefinition("vpc-public-maven", "https://raw.githubusercontent.com/thevpc/vpc-public-maven/master", NutsConstants.REPOSITORY_TYPE_NUTS_MAVEN, true,NutsRepositoryDefinition.ORDER_USER_REMOTE),
            new NutsRepositoryDefinition("vpc-public-nuts", "https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master", NutsConstants.REPOSITORY_TYPE_NUTS_FOLDER, true,NutsRepositoryDefinition.ORDER_USER_REMOTE)
    };

    public NutsRepositoryDefinition[] getDefaultRepositories(NutsWorkspace workspace) {
        return DEFAULTS;
    }

    @Override
    public int getSupportLevel(NutsRepositoryLocation criteria) {
        if (criteria == null) {
            return NO_SUPPORT;
        }
        String repositoryType = criteria.getType();
        String location = criteria.getLocation();
        if (!NutsConstants.REPOSITORY_TYPE_NUTS_MAVEN.equals(repositoryType)) {
            return NO_SUPPORT;
        }
        if (StringUtils.isEmpty(location)) {
            return DEFAULT_SUPPORT;
        }
        if (location.startsWith("http://") || location.startsWith("https://")) {
            return DEFAULT_SUPPORT;
        }
        if (!location.contains("://")) {
            return DEFAULT_SUPPORT;
        }
        return NO_SUPPORT;
    }

    @Override
    public NutsRepository create(NutsRepositoryLocation loc, NutsWorkspace workspace, NutsRepository parentRepository, String repositoryRoot) {
        if (NutsConstants.REPOSITORY_TYPE_NUTS_MAVEN.equals(loc.getType())) {
            if (loc.getLocation().startsWith("http://") || loc.getLocation().startsWith("https://")) {
                return (new MavenRemoteRepository(loc.getName(), loc.getLocation(), workspace, parentRepository, repositoryRoot));
            }
            if (!loc.getLocation().contains("://")) {
                return new MavenFolderRepository(loc.getName(), loc.getLocation(), workspace, parentRepository, repositoryRoot);
            }
        }
        return null;
    }
}
