/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsFindVersionsRepositoryCommand;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.common.IteratorBuilder;

/**
 *
 * @author vpc
 */
public class DefaultNutsFindVersionsRepositoryCommand extends NutsRepositoryCommandBase<NutsFindVersionsRepositoryCommand> implements NutsFindVersionsRepositoryCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsFindVersionsRepositoryCommand.class.getName());

    private NutsId id;
    private Iterator<NutsId> result;
    private NutsIdFilter filter;

    public DefaultNutsFindVersionsRepositoryCommand(NutsRepository repo) {
        super(repo);
    }

    @Override
    public NutsFindVersionsRepositoryCommand filter(NutsIdFilter id) {
        return setFilter(id);
    }

    @Override
    public NutsFindVersionsRepositoryCommand setFilter(NutsIdFilter filter) {
        this.filter = filter;
        return this;
    }

    @Override
    public NutsIdFilter getFilter() {
        return filter;
    }

    @Override
    public NutsFindVersionsRepositoryCommand run() {
        CoreNutsUtils.checkSession(getSession());
        id = id.setFaceComponent();
        getRepo().security().checkAllowed(NutsConstants.Rights.FETCH_DESC, "find-versions");
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        CoreNutsUtils.checkNutsId(id);
        xrepo.checkAllowedFetch(id, getSession());
        try {
            if (getSession().isIndexed() && xrepo.getIndexStoreClient() != null && xrepo.getIndexStoreClient().isEnabled()) {
                List<NutsId> d = null;
                try {
                    d = xrepo.getIndexStoreClient().findVersions(id, getSession());
                } catch (NutsException ex) {
                    LOG.log(Level.FINEST, "[ERROR  ] Error find version operation with Indexer for {0} : {1}", new Object[]{getRepo().config().getName(), ex});
                }
                if (d != null && !d.isEmpty() && filter != null) {
                    result = IteratorBuilder.of(d.iterator()).filter(x -> filter.accept(x, getRepo().getWorkspace())).iterator();
                    return this;
                }
            }
            result = xrepo.findVersionsImpl(id, getFilter(), getSession());
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
    public NutsFindVersionsRepositoryCommand id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsFindVersionsRepositoryCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsId getId() {
        return id;
    }

}
