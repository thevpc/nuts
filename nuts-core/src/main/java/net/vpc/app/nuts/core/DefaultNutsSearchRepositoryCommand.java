/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.NutsSearchRepositoryCommand;

/**
 *
 * @author vpc
 */
public class DefaultNutsSearchRepositoryCommand extends NutsRepositoryCommandBase<NutsSearchRepositoryCommand> implements NutsSearchRepositoryCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsSearchRepositoryCommand.class.getName());

    private NutsIdFilter filter;
    private Iterator<NutsId> result;

    public DefaultNutsSearchRepositoryCommand(NutsRepository repo) {
        super(repo);
    }

    @Override
    public NutsSearchRepositoryCommand run() {
        NutsWorkspaceUtils.checkSession(getRepo().getWorkspace(), getSession());
        getRepo().security().checkAllowed(NutsConstants.Rights.FETCH_DESC, "search");
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        xrepo.checkAllowedFetch(null, getSession());
        try {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "[SUCCESS] {0} Find components", CoreStringUtils.alignLeft(getRepo().config().getName(), 20));
            }
            if (getSession().isIndexed() && xrepo.getIndexStoreClient() != null && xrepo.getIndexStoreClient().isEnabled()) {
                Iterator<NutsId> o = null;
                try {
                    o = xrepo.getIndexStoreClient().search(filter, getSession());
                } catch (NutsException ex) {
                    LOG.log(Level.FINEST, "[ERROR  ] Error find operation using Indexer for {0} : {1}", new Object[]{getRepo().config().getName(), ex});
                }

                if (o != null) {
                    result=o;
                    return this;
                }
            }

            result=xrepo.searchImpl(filter, getSession());
        } catch (NutsNotFoundException | SecurityException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "[ERROR  ] {0} Find components", CoreStringUtils.alignLeft(getRepo().config().getName(), 20));
            }
            throw ex;
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "[ERROR  ] {0} Find components", CoreStringUtils.alignLeft(getRepo().config().getName(), 20));
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

    @Override
    public NutsSearchRepositoryCommand filter(NutsIdFilter id) {
        return setFilter(id);
    }

    @Override
    public NutsSearchRepositoryCommand setFilter(NutsIdFilter filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public NutsIdFilter getFilter() {
        return filter;
    }

}
