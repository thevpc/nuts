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
package net.vpc.app.nuts.core.bridges.maven;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenNutsRepositoryFactoryComponent implements NutsRepositoryFactoryComponent {

    private static final NutsRepositoryDefinition[] DEFAULTS = {
        new NutsRepositoryDefinition().setName("maven-local").setLocation(System.getProperty("maven-local", "~/.m2/repository")).setType(NutsConstants.RepoTypes.MAVEN).setProxy(CoreCommonUtils.getSystemBoolean("nuts.cache.cache-local-files", false)).setReference(false).setFailSafe(false).setCreate(true).setOrder(NutsRepositoryDefinition.ORDER_USER_LOCAL),
        new NutsRepositoryDefinition().setName("maven-central").setLocation("http://repo.maven.apache.org/maven2/").setType(NutsConstants.RepoTypes.MAVEN).setProxy(true).setReference(false).setFailSafe(false).setCreate(true).setOrder(NutsRepositoryDefinition.ORDER_USER_REMOTE),
        new NutsRepositoryDefinition().setName("vpc-public-maven").setLocation("https://raw.githubusercontent.com/thevpc/vpc-public-maven/master").setType(NutsConstants.RepoTypes.MAVEN).setProxy(true).setReference(false).setFailSafe(false).setCreate(true).setOrder(NutsRepositoryDefinition.ORDER_USER_REMOTE),
        new NutsRepositoryDefinition().setName("vpc-public-nuts").setLocation("https://raw.githubusercontent.com/thevpc/vpc-public-nuts/master").setType(NutsConstants.RepoTypes.NUTS).setProxy(true).setReference(false).setFailSafe(false).setCreate(true).setOrder(NutsRepositoryDefinition.ORDER_USER_REMOTE),};

    @Override
    public NutsRepositoryDefinition[] getDefaultRepositories(NutsWorkspace workspace) {
        return DEFAULTS;
    }

    @Override
    public int getSupportLevel(NutsRepositoryConfig criteria) {
        if (criteria == null) {
            return NO_SUPPORT;
        }
        String repositoryType = criteria.getType();
        String location = criteria.getLocation();
        if (!NutsConstants.RepoTypes.MAVEN.equals(repositoryType)) {
            return NO_SUPPORT;
        }
        if (CoreStringUtils.isBlank(location)) {
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
    public NutsRepository create(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        final NutsRepositoryConfig config = options.getConfig();
        if (NutsConstants.RepoTypes.MAVEN.equals(config.getType())) {
            if (CoreIOUtils.isPathHttp(config.getLocation())) {
                return (new MavenRemoteRepository(options, workspace, parentRepository));
            }
            if (CoreIOUtils.isPathFile(config.getLocation())) {
                return new MavenFolderRepository(options, workspace, parentRepository);
            }
        }
        return null;
    }
}
