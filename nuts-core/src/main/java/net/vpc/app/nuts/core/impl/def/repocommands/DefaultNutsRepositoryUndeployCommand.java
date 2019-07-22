/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.impl.def.repocommands;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.core.repocommands.AbstractNutsRepositoryUndeployCommand;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.NutsRepositoryUndeployCommand;

/**
 *
 * @author vpc
 */
public class DefaultNutsRepositoryUndeployCommand extends AbstractNutsRepositoryUndeployCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsRepositoryUndeployCommand.class.getName());

    public DefaultNutsRepositoryUndeployCommand(NutsRepository repo) {
        super(repo);
    }

    @Override
    public NutsRepositoryUndeployCommand run() {
        NutsWorkspaceUtils.checkSession(getRepo().getWorkspace(), getSession());
        getRepo().security().checkAllowed(NutsConstants.Permissions.UNDEPLOY, "undeploy");
        try {
            NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
            xrepo.undeployImpl(this);
            if (getSession().isIndexed() && xrepo.getIndexStoreClient() != null && xrepo.getIndexStoreClient().isEnabled()) {
                try {
                    xrepo.getIndexStoreClient().invalidate(this.getId());
                } catch (NutsException ex) {
                    LOG.log(Level.FINEST, "[ERROR  ] Error invalidating Indexer for {0} : {1}", new Object[]{getRepo().config().getName(), ex});
                }
            }
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "[SUCCESS] {0} Undeploy {1}", new Object[]{CoreStringUtils.alignLeft(getRepo().config().getName(), 20), this.getId()});
            }
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "[ERROR  ] {0} Undeploy {1}", new Object[]{CoreStringUtils.alignLeft(getRepo().config().getName(), 20), this.getId()});
            }
        }
        return this;
    }

}
