/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.search;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NutsPositionType;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.id.util.NutsIdUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsRepositoryExt;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
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
        NutsSessionUtils.checkSession(getRepo().getWorkspace(), session);
        //id = id.builder().setFaceContent().build();
        getRepo().security().setSession(session).checkAllowed(NutsConstants.Permissions.FETCH_DESC, "find-versions");
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        NutsIdUtils.checkShortId(id,session);
        xrepo.checkAllowedFetch(id, session);
        try {
            List<NutsIterator<? extends NutsId>> resultList = new ArrayList<>();
            if(getFetchMode()==NutsFetchMode.REMOTE) {
                if (session.isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled()) {
                    NutsIterator<NutsId> d = null;
                    try {
                        d = xrepo.getIndexStore().searchVersions(id, session);
                    } catch (NutsException ex) {
                        _LOGOP(session).level(Level.FINEST).verb(NutsLoggerVerb.FAIL)
                                .log(NutsMessage.ofJstyle("error finding version with Indexer for {0} : {1}", getRepo().getName(), ex));
                    }
                    if (d != null && filter != null) {
                        resultList.add(
                                IteratorBuilder.of(d, session).filter(
                                        x -> filter.acceptId(x, session),
                                        e -> NutsDescribables.resolveOrToString(filter, e)
                                ).safeIgnore().iterator()
                        );
                    }
                }
            }
            NutsIterator<NutsId> rr = xrepo.searchVersionsImpl(id, getFilter(), getFetchMode(), session);
            if (rr != null) {
                resultList.add(rr);
            }
            result = IteratorUtils.coalesce(resultList);
            return this;
        } catch (RuntimeException ex) {
            _LOGOP(session).level(Level.FINEST).verb(NutsLoggerVerb.FAIL)
                    .log(NutsMessage.ofJstyle("[{0}] {1} {2} {3}",
                            NutsStringUtils.formatAlign(getFetchMode().toString(), 7, NutsPositionType.FIRST),
                            NutsStringUtils.formatAlign(getRepo().getName(), 20,NutsPositionType.FIRST),
                            NutsStringUtils.formatAlign("Fetch versions for", 24,NutsPositionType.FIRST),
                            id));
            throw ex;
        }
    }

    @Override
    public NutsIterator<NutsId> getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

}
