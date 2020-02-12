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
package net.vpc.app.nuts.main.repocommands;

import java.util.logging.Level;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.runtime.repocommands.AbstractNutsDeployRepositoryCommand;
import net.vpc.app.nuts.core.repos.NutsRepositoryExt;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsDeployRepositoryCommand extends AbstractNutsDeployRepositoryCommand {

    private final NutsLogger LOG;

    public DefaultNutsDeployRepositoryCommand(NutsRepository repo) {
        super(repo);
        LOG=repo.workspace().log().of(DefaultNutsDeployRepositoryCommand.class);
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
                    xrepo.getIndexStore().revalidate(this.getId());
                } catch (NutsException ex) {
                    LOG.with().level(Level.FINEST).verb(NutsLogVerb.FAIL).log( "Error revalidating Indexer for {0} : {1}", getRepo().config().getName(), ex);
                }
            }
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb(NutsLogVerb.SUCCESS).log( "{0} Deploy {1}", CoreStringUtils.alignLeft(getRepo().config().getName(), 20), this.getId());
            }
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb(NutsLogVerb.FAIL).log( "{0} Deploy {1}", CoreStringUtils.alignLeft(getRepo().config().getName(), 20), this.getId());
            }
            throw ex;
        }
        return this;
    }

}
