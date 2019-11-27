/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.repocommands;

import java.util.logging.Level;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.runtime.repocommands.AbstractNutsRepositoryUndeployCommand;
import net.vpc.app.nuts.core.repos.NutsRepositoryExt;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsRepositoryUndeployCommand extends AbstractNutsRepositoryUndeployCommand {

    private final NutsLogger LOG;

    public DefaultNutsRepositoryUndeployCommand(NutsRepository repo) {
        super(repo);
        LOG=repo.workspace().log().of(DefaultNutsRepositoryUndeployCommand.class);
    }

    @Override
    public NutsRepositoryUndeployCommand run() {
        NutsWorkspaceUtils.of(getRepo().getWorkspace()).checkSession( getSession());
        getRepo().security().checkAllowed(NutsConstants.Permissions.UNDEPLOY, "undeploy");
        try {
            NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
            xrepo.undeployImpl(this);
            if (getSession().isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled()) {
                try {
                    xrepo.getIndexStore().invalidate(this.getId());
                } catch (NutsException ex) {
                    LOG.log(Level.FINEST, NutsLogVerb.FAIL, "Error invalidating Indexer for {0} : {1}", new Object[]{getRepo().config().getName(), ex});
                }
            }
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, NutsLogVerb.SUCCESS, "{0} Undeploy {1}", new Object[]{CoreStringUtils.alignLeft(getRepo().config().getName(), 20), this.getId()});
            }
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, NutsLogVerb.FAIL, "{0} Undeploy {1}", new Object[]{CoreStringUtils.alignLeft(getRepo().config().getName(), 20), this.getId()});
            }
        }
        return this;
    }

}
