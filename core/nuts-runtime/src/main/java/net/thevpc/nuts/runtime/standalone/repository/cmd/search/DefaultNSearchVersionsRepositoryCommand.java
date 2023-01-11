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
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.id.util.NIdUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
import net.thevpc.nuts.spi.NSearchVersionsRepositoryCommand;

/**
 * @author thevpc %category SPI Base
 */
public class DefaultNSearchVersionsRepositoryCommand extends AbstractNSearchVersionsRepositoryCommand {

    private NLogger LOG;

    public DefaultNSearchVersionsRepositoryCommand(NRepository repo) {
        super(repo);
    }

    protected NLoggerOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLogger _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLogger.of(DefaultNSearchVersionsRepositoryCommand.class,session);
        }
        return LOG;
    }

    @Override
    public NSearchVersionsRepositoryCommand run() {
        NSession session = getSession();
        NSessionUtils.checkSession(getRepo().getWorkspace(), session);
        //id = id.builder().setFaceContent().build();
        getRepo().security().setSession(session).checkAllowed(NConstants.Permissions.FETCH_DESC, "find-versions");
        NRepositoryExt xrepo = NRepositoryExt.of(getRepo());
        NIdUtils.checkShortId(id,session);
        xrepo.checkAllowedFetch(id, session);
        try {
            List<NIterator<? extends NId>> resultList = new ArrayList<>();
            if(getFetchMode()== NFetchMode.REMOTE) {
                if (session.isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled()) {
                    NIterator<NId> d = null;
                    try {
                        d = xrepo.getIndexStore().searchVersions(id, session);
                    } catch (NException ex) {
                        _LOGOP(session).level(Level.FINEST).verb(NLoggerVerb.FAIL)
                                .log(NMsg.ofJ("error finding version with Indexer for {0} : {1}", getRepo().getName(), ex));
                    }
                    if (d != null && filter != null) {
                        resultList.add(
                                IteratorBuilder.of(d, session).filter(
                                        x -> filter.acceptId(x, session),
                                        e -> NDescribables.resolveOrToString(filter, e)
                                ).safeIgnore().iterator()
                        );
                    }
                }
            }
            NIterator<NId> rr = xrepo.searchVersionsImpl(id, getFilter(), getFetchMode(), session);
            if (rr != null) {
                resultList.add(rr);
            }
            result = IteratorUtils.coalesce(resultList);
            return this;
        } catch (RuntimeException ex) {
            _LOGOP(session).level(Level.FINEST).verb(NLoggerVerb.FAIL)
                    .log(NMsg.ofJ("[{0}] {1} {2} {3}",
                            NStringUtils.formatAlign(getFetchMode().toString(), 7, NPositionType.FIRST),
                            NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST),
                            NStringUtils.formatAlign("Fetch versions for", 24, NPositionType.FIRST),
                            id));
            throw ex;
        }
    }

    @Override
    public NIterator<NId> getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

}
