/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.repocommands;

import java.util.Iterator;
import java.util.logging.Level;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.runtime.repocommands.AbstractNutsSearchRepositoryCommand;
import net.vpc.app.nuts.core.repos.NutsRepositoryExt;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsSearchRepositoryCommand extends AbstractNutsSearchRepositoryCommand {

    private final NutsLogger LOG;

    public DefaultNutsSearchRepositoryCommand(NutsRepository repo) {
        super(repo);
        LOG=repo.workspace().log().of(DefaultNutsSearchRepositoryCommand.class);
    }

    @Override
    public NutsSearchRepositoryCommand run() {
        NutsWorkspaceUtils.of(getRepo().getWorkspace()).checkSession(getSession());
        getRepo().security().checkAllowed(NutsConstants.Permissions.FETCH_DESC, "search");
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        xrepo.checkAllowedFetch(null, getSession());
        try {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, NutsLogVerb.SUCCESS, "{0} Find components", CoreStringUtils.alignLeft(getRepo().config().getName(), 20));
            }
            if (getSession().getSession().isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled()) {
                Iterator<NutsId> o = null;
                try {
                    o = xrepo.getIndexStore().search(filter, getSession());
                } catch (NutsException ex) {
                    LOG.log(Level.FINEST, NutsLogVerb.FAIL, "Error find operation using Indexer for {0} : {1}", new Object[]{getRepo().config().getName(), ex});
                }

                if (o != null) {
                    result = o;
                    return this;
                }
            }

            result = xrepo.searchImpl(filter, getSession());
        } catch (NutsNotFoundException | SecurityException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, NutsLogVerb.FAIL, "{0} Find components", CoreStringUtils.alignLeft(getRepo().config().getName(), 20));
            }
            throw ex;
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, NutsLogVerb.FAIL, "{0} Find components", CoreStringUtils.alignLeft(getRepo().config().getName(), 20));
            }
            throw ex;
        }
        return this;
    }

    @Override
    public Iterator<NutsId> getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

}
