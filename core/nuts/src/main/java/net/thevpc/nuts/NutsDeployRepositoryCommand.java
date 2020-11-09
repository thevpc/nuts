/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts;

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
