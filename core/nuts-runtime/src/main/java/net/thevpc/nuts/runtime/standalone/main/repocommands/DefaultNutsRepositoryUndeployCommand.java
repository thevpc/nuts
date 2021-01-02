/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.main.repocommands;

import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.standalone.repocommands.AbstractNutsRepositoryUndeployCommand;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.spi.NutsRepositoryUndeployCommand;

/**
 *
 * @author thevpc
 * %category SPI Base
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
        NutsSession session = getValidWorkspaceSession();
        NutsWorkspaceUtils.of(getRepo().getWorkspace()).checkSession(session);
        getRepo().security().checkAllowed(NutsConstants.Permissions.UNDEPLOY, "undeploy", session);
        try {
            NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
            xrepo.undeployImpl(this);
            if (session.isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled()) {
                try {
                    xrepo.getIndexStore().invalidate(this.getId(), session);
                } catch (NutsException ex) {
                    LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.FAIL).log( "error invalidating Indexer for {0} : {1}", getRepo().getName(), ex);
                }
            }
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.SUCCESS).log( "{0} undeploy {1}", CoreStringUtils.alignLeft(getRepo().getName(), 20), this.getId());
            }
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.FAIL).log( "{0} undeploy {1}", CoreStringUtils.alignLeft(getRepo().getName(), 20), this.getId());
            }
        }
        return this;
    }

}
