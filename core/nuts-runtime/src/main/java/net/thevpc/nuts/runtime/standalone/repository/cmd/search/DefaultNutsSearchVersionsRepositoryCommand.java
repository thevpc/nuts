/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsRepositoryExt;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.bundles.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;
import net.thevpc.nuts.spi.NutsSearchVersionsRepositoryCommand;

/**
 * @author thevpc %category SPI Base
 */
public class DefaultNutsSearchVersionsRepositoryCommand extends AbstractNutsSearchVersionsRepositoryCommand {

    private NutsLogger LOG;

    public DefaultNutsSearchVersionsRepositoryCommand(NutsRepository repo) {
        super(repo);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsSearchVersionsRepositoryCommand.class,session);
        }
        return LOG;
    }

    @Override
    public NutsSearchVersionsRepositoryCommand run() {
        NutsSession session = getSession();
        NutsWorkspaceUtils.checkSession(getRepo().getWorkspace(), session);
        //id = id.builder().setFaceContent().build();
        getRepo().security().setSession(session).checkAllowed(NutsConstants.Permissions.FETCH_DESC, "find-versions");
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        NutsWorkspaceUtils.of(session).checkShortId(id);
        xrepo.checkAllowedFetch(id, session);
        try {
            List<Iterator<? extends NutsId>> resultList = new ArrayList<>();
            if (session.isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled()) {
                Iterator<NutsId> d = null;
                try {
                    d = xrepo.getIndexStore().searchVersions(id, session);
                } catch (NutsException ex) {
                    _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.FAIL)
                            .log(NutsMessage.jstyle("error finding version with Indexer for {0} : {1}", getRepo().getName(), ex));
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
            _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("[{0}] {1} {2} {3}", CoreStringUtils.alignLeft(getFetchMode().toString(), 7), CoreStringUtils.alignLeft(getRepo().getName(), 20), CoreStringUtils.alignLeft("Fetch versions for", 24), id));
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
