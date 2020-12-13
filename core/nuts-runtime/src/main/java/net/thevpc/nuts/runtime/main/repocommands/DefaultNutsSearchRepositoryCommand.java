/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.main.repocommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.log.NutsLogVerb;
import net.thevpc.nuts.runtime.repocommands.AbstractNutsSearchRepositoryCommand;
import net.thevpc.nuts.runtime.util.NutsWorkspaceUtils;

import java.util.Iterator;
import java.util.logging.Level;

/**
 * @author thevpc
 * %category SPI Base
 */
public class DefaultNutsSearchRepositoryCommand extends AbstractNutsSearchRepositoryCommand {

    private final NutsLogger LOG;

    public DefaultNutsSearchRepositoryCommand(NutsRepository repo) {
        super(repo);
        LOG = repo.getWorkspace().log().of(DefaultNutsSearchRepositoryCommand.class);
    }

    @Override
    public NutsSearchRepositoryCommand run() {
        NutsWorkspaceUtils.of(getRepo().getWorkspace()).checkSession(getSession());
        getRepo().security().checkAllowed(NutsConstants.Permissions.FETCH_DESC, "search");
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        xrepo.checkAllowedFetch(null, getSession());
        try {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb(NutsLogVerb.START).log("{0} Search components", CoreStringUtils.alignLeft(getRepo().getName(), 20));
            }
            boolean processIndexFirst = getSession().isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled();
            if (processIndexFirst) {
                Iterator<NutsId> o = null;
                try {
                    o = xrepo.getIndexStore().search(filter, getSession());
                } catch (NutsIndexerNotAccessibleException ex) {
                    //just ignore
                } catch (NutsException ex) {
                    LOG.with().level(Level.FINEST).verb(NutsLogVerb.FAIL).log("error search operation using Indexer for {0} : {1}", getRepo().getName(), ex);
                }
                if (o != null) {
                    result = new IndexFirstIterator<>(o,
                            xrepo.searchImpl(filter, getFetchMode(), getSession())
                    );
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.with().level(Level.FINEST).verb(NutsLogVerb.SUCCESS).log("{0} Search components (indexer)", CoreStringUtils.alignLeft(getRepo().getName(), 20));
                    }
                    return this;
                }
            }
            result = xrepo.searchImpl(filter, getFetchMode(), getSession());
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb(NutsLogVerb.SUCCESS).log("{0} Search components", CoreStringUtils.alignLeft(getRepo().getName(), 20));
            }
        } catch (NutsNotFoundException | SecurityException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().level(Level.FINEST).verb(NutsLogVerb.FAIL).log("{0} Search components", CoreStringUtils.alignLeft(getRepo().getName(), 20));
            }
            throw ex;
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.with().level(Level.SEVERE).verb(NutsLogVerb.FAIL).log("{0} Search components", CoreStringUtils.alignLeft(getRepo().getName(), 20));
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
