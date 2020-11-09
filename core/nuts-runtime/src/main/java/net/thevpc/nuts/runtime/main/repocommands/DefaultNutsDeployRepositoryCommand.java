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
 *
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
package net.thevpc.nuts.runtime.main.repocommands;

import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.log.NutsLogVerb;
import net.thevpc.nuts.runtime.repocommands.AbstractNutsDeployRepositoryCommand;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 * @category SPI Base
 */
public class DefaultNutsDeployRepositoryCommand extends AbstractNutsDeployRepositoryCommand {

    private final NutsLogger LOG;

    public DefaultNutsDeployRepositoryCommand(NutsRepository repo) {
        super(repo);
        LOG=repo.getWorkspace().log().of(DefaultNutsDeployRepositoryCommand.class);
    }


    @Override
    public NutsDeployRepositoryCommand run() {
        getRepo().security().checkAllowed(NutsConstants.Permissions.DEPLOY, "deploy");
        checkParameters();
        try {
            NutsRepositoryExt xrepo = NutsRepositoryExt.of(repo);
            NutsDescriptor rep = xrepo.deployImpl(this);
            this.setDescriptor(rep);
            this.setId(rep.getId());
            if (getSession().isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled()) {
                try {
                    xrepo.getIndexStore().revalidate(this.getId(), getSession());
                } catch (NutsException ex) {
                    LOG.with().level(Level.FINEST).verb(NutsLogVerb.FAIL).log( "Error revalidating Indexer for {0} : {1}", getRepo().getName(), ex);
                }
            }
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb(NutsLogVerb.SUCCESS).log( "{0} Deploy {1}", CoreStringUtils.alignLeft(getRepo().getName(), 20), this.getId());
            }
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb(NutsLogVerb.FAIL).log( "{0} Deploy {1}", CoreStringUtils.alignLeft(getRepo().getName(), 20), this.getId());
            }
            throw ex;
        }
        return this;
    }

}
