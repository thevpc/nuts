/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.NutsRepositoryUndeployCommand;

/**
 *
 * @author vpc
 */
public class DefaultNutsRepositoryUndeployCommand extends NutsRepositoryCommandBase<NutsRepositoryUndeployCommand> implements NutsRepositoryUndeployCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsRepositoryUndeployCommand.class.getName());
    private NutsId id;
    private String repository;
    private boolean offline = false;
    private boolean transitive = true;

    public DefaultNutsRepositoryUndeployCommand(NutsRepository repo) {
        super(repo);
    }

    
    
    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsRepositoryUndeployCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NutsRepositoryUndeployCommand setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public NutsRepositoryUndeployCommand transitive() {
        return transitive(true);
    }

    @Override
    public NutsRepositoryUndeployCommand setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NutsRepositoryUndeployCommand setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NutsRepositoryUndeployCommand id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsRepositoryUndeployCommand repository(String repository) {
        return setRepository(repository);
    }

    @Override
    public NutsRepositoryUndeployCommand offline() {
        return offline(true);
    }

    @Override
    public NutsRepositoryUndeployCommand offline(boolean offline) {
        return setOffline(offline);
    }

    @Override
    public NutsRepositoryUndeployCommand transitive(boolean transitive) {
        return setTransitive(transitive);
    }

//    @Override
//    public NutsRepositoryUndeploymentOptions copy() {
//        return new DefaultNutsRepositoryUndeploymentOptions()
//                .setId(id)
//                .setOffline(offline)
//                .setRepository(repository)
//                .setTransitive(transitive);
//
//    }

    @Override
    public NutsRepositoryUndeployCommand run() {
        NutsWorkspaceUtils.checkSession(getRepo().getWorkspace(), getSession());
        getRepo().security().checkAllowed(NutsConstants.Rights.UNDEPLOY, "undeploy");
        try {
            NutsRepositoryExt xrepo=NutsRepositoryExt.of(getRepo());
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
