/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.main.repocommands;

import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.repocommands.AbstractNutsRepositoryUndeployCommand;
import net.thevpc.nuts.runtime.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.log.NutsLogVerb;

/**
 *
 * @author vpc
 * @category SPI Base
 */
public class DefaultNutsRepositoryUndeployCommand extends AbstractNutsRepositoryUndeployCommand {

    private final NutsLogger LOG;

    public DefaultNutsRepositoryUndeployCommand(NutsRepository repo) {
        super(repo);
        LOG=repo.getWorkspace().log().of(DefaultNutsRepositoryUndeployCommand.class);
    }
    public DefaultNutsRepositoryUndeployCommand(NutsWorkspace ws) {
        super(null);
        LOG=ws.log().of(DefaultNutsRepositoryUndeployCommand.class);
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
                    xrepo.getIndexStore().invalidate(this.getId(), getSession());
                } catch (NutsException ex) {
                    LOG.with().level(Level.FINEST).verb(NutsLogVerb.FAIL).log( "Error invalidating Indexer for {0} : {1}", getRepo().getName(), ex);
                }
            }
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb(NutsLogVerb.SUCCESS).log( "{0} Undeploy {1}", CoreStringUtils.alignLeft(getRepo().getName(), 20), this.getId());
            }
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb(NutsLogVerb.FAIL).log( "{0} Undeploy {1}", CoreStringUtils.alignLeft(getRepo().getName(), 20), this.getId());
            }
        }
        return this;
    }

}
