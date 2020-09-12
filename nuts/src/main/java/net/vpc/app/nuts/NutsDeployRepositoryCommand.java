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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * Repository Deploy command provided by Repository and used by Workspace.
 * This class is part of Nuts SPI and is not to be used by end users.
 * @author vpc
 * @since 0.5.4
 * @category SPI Base
 */
public interface NutsDeployRepositoryCommand extends NutsRepositoryCommand {

    /**
     * content to deploy
     * @return content to deploy
     */
    Object getContent();

    /**
     * descriptor to deploy
     * @return descriptor to deploy
     */
    NutsDescriptor getDescriptor();

    /**
     * id to deploy
     * @return id to deploy
     */
    NutsId getId();


    /**
     * set content to deploy
     * @param content content to deploy
     * @return {@code this} instance
     */
    NutsDeployRepositoryCommand setContent(Path content);

    /**
     * set content to deploy
     * @param content content to deploy
     * @return {@code this} instance
     */
    NutsDeployRepositoryCommand setContent(URL content);

    /**
     * set content to deploy
     * @param content content to deploy
     * @return {@code this} instance
     */
    NutsDeployRepositoryCommand setContent(File content);

    /**
     * set content to deploy
     * @param content content to deploy
     * @return {@code this} instance
     */
    NutsDeployRepositoryCommand setContent(InputStream content) ;

    /**
     * set descriptor to deploy
     * @param descriptor descriptor to deploy
     * @return {@code this} instance
     */
    NutsDeployRepositoryCommand setDescriptor(NutsDescriptor descriptor);

    /**
     * set id to deploy
     * @param id id to deploy
     * @return {@code this} instance
     */
    NutsDeployRepositoryCommand setId(NutsId id);

    /**
     * session
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsDeployRepositoryCommand setSession(NutsSession session);

    /**
     * run deploy command
     * @return {@code this} instance
     */
    @Override
    NutsDeployRepositoryCommand run();
}
