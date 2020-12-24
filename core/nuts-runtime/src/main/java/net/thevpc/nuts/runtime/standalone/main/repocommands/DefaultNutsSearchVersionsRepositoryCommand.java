/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.main.repocommands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.standalone.repocommands.AbstractNutsSearchVersionsRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
import net.thevpc.nuts.spi.NutsSearchVersionsRepositoryCommand;

/**
 * @author thevpc
 * %category SPI Base
 */
public class DefaultNutsSearchVersionsRepositoryCommand extends AbstractNutsSearchVersionsRepositoryCommand {

    private final NutsLogger LOG;

    public DefaultNutsSearchVersionsRepositoryCommand(NutsRepository repo) {
        super(repo);
        LOG = repo.getWorkspace().log().of(DefaultNutsSearchVersionsRepositoryCommand.class);
    }

    @Override
    public NutsSearchVersionsRepositoryCommand run() {
        NutsSession session = getValidWorkspaceSession();
        NutsWorkspaceUtils.of(getRepo().getWorkspace()).checkSession(session);
        //id = id.builder().setFaceContent().build();
        getRepo().security().checkAllowed(NutsConstants.Permissions.FETCH_DESC, "find-versions", session);
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        NutsWorkspaceUtils.of(getRepo().getWorkspace()).checkSimpleNameNutsId(id);
        xrepo.checkAllowedFetch(id, session);
        try {
            List<Iterator<NutsId>> resultList = new ArrayList<>();
            if (session.isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled()) {
                Iterator<NutsId> d = null;
                try {
                    d = xrepo.getIndexStore().searchVersions(id, session);
                } catch (NutsException ex) {
                    LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.FAIL).log("error finding version with Indexer for {0} : {1}", getRepo().getName(), ex);
                }
                if (d != null && filter != null) {
                    resultList.add(
                            IteratorUtils.safeIgnore(
                                    IteratorBuilder.of(d).filter(x -> filter.acceptId(x, session)).iterator())
                            );
                }
            }
            Iterator<NutsId> rr = xrepo.searchVersionsImpl(id, getFilter(), getFetchMode(), session);
            if (rr != null) {
                resultList.add(rr);
            }
            result = IteratorUtils.coalesce(resultList);
            return this;
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.FAIL).log( "[{0}] {1} {2} {3}", CoreStringUtils.alignLeft(getFetchMode().toString(), 7), CoreStringUtils.alignLeft(getRepo().getName(), 20), CoreStringUtils.alignLeft("Fetch versions for", 24), id);
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

}
