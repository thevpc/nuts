/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.common.IteratorBuilder;
import net.vpc.app.nuts.NutsSearchVersionsRepositoryCommand;

/**
 *
 * @author vpc
 */
public class DefaultNutsSeachVersionsRepositoryCommand extends NutsRepositoryCommandBase<NutsSearchVersionsRepositoryCommand> implements NutsSearchVersionsRepositoryCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsSeachVersionsRepositoryCommand.class.getName());

    private NutsId id;
    private Iterator<NutsId> result;
    private NutsIdFilter filter;

    public DefaultNutsSeachVersionsRepositoryCommand(NutsRepository repo) {
        super(repo);
    }

    @Override
    public NutsSearchVersionsRepositoryCommand filter(NutsIdFilter id) {
        return setFilter(id);
    }

    @Override
    public NutsSearchVersionsRepositoryCommand setFilter(NutsIdFilter filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public NutsIdFilter getFilter() {
        return filter;
    }

    @Override
    public NutsSearchVersionsRepositoryCommand run() {
        NutsWorkspaceUtils.checkSession(getRepo().getWorkspace(), getSession());
        id = id.setFaceComponent();
        getRepo().security().checkAllowed(NutsConstants.Rights.FETCH_DESC, "find-versions");
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        NutsWorkspaceUtils.checkSimpleNameNutsId(repo.getWorkspace(),id);
        xrepo.checkAllowedFetch(id, getSession());
        try {
            if (getSession().isIndexed() && xrepo.getIndexStoreClient() != null && xrepo.getIndexStoreClient().isEnabled()) {
                List<NutsId> d = null;
                try {
                    d = xrepo.getIndexStoreClient().searchVersions(id, getSession());
                } catch (NutsException ex) {
                    LOG.log(Level.FINEST, "[ERROR  ] Error find version operation with Indexer for {0} : {1}", new Object[]{getRepo().config().getName(), ex});
                }
                if (d != null && !d.isEmpty() && filter != null) {
                    result = IteratorBuilder.of(d.iterator()).filter(x -> filter.accept(x, getRepo().getWorkspace(), getSession().getSession())).iterator();
                    return this;
                }
            }
            Iterator<NutsId> rr = xrepo.searchVersionsImpl(id, getFilter(), getSession());
            if(rr==null){
                rr=Collections.emptyIterator();
            }
            result = rr;
            return this;
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "[ERROR  ] [{0}] {1} {2} {3}", new Object[]{CoreStringUtils.alignLeft(getSession().getFetchMode().toString(), 7), CoreStringUtils.alignLeft(getRepo().config().getName(), 20), CoreStringUtils.alignLeft("Fetch versions for", 24), id});
            }
            throw ex;
        }
    }

    @Override
    public Iterator<NutsId> getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

    @Override
    public NutsSearchVersionsRepositoryCommand id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsSearchVersionsRepositoryCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsId getId() {
        return id;
    }

}
