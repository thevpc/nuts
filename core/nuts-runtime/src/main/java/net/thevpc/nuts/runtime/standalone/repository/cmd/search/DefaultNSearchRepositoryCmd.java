/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.lib.common.iter.IndexFirstIterator;
import net.thevpc.nuts.lib.common.iter.IteratorBuilder;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.spi.NSearchRepositoryCmd;

import java.util.Iterator;
import java.util.logging.Level;

/**
 * @author thevpc %category SPI Base
 */
public class DefaultNSearchRepositoryCmd extends AbstractNSearchRepositoryCmd {


    public DefaultNSearchRepositoryCmd(NRepository repo) {
        super(repo);
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
            return NLog.of(DefaultNSearchRepositoryCmd.class);
    }

    @Override
    public NSearchRepositoryCmd run() {
        NSession session = getRepo().getWorkspace().currentSession();
        NSessionUtils.checkSession(getRepo().getWorkspace(), session);
        NRunnable startRunnable = NRunnable.of(
                () -> {
                    getRepo().security().checkAllowed(NConstants.Permissions.FETCH_DESC, "search");
                    NRepositoryExt xrepo = NRepositoryExt.of(getRepo());
                    xrepo.checkAllowedFetch(null);
                    _LOGOP().level(Level.FINEST).verb(NLogVerb.START)
                            .log(NMsg.ofJ("{0} search packages", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST)));
                }
        ).withDesc(NEDesc.of("CheckAuthorizations"));
        NRunnable endRunnable =
                NRunnable.of(
                        () -> _LOGOP().level(Level.FINEST).verb(NLogVerb.SUCCESS)
                                .log(NMsg.ofJ("{0} search packages", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST)))
                        ).withDesc(NEDesc.of("Log"));
        try {
            NRepositoryExt xrepo = NRepositoryExt.of(getRepo());
            boolean processIndexFirst =
                getFetchMode()== NFetchMode.REMOTE && session.isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled();
            if (processIndexFirst) {
                Iterator<NId> o = null;
                try {
                    o = xrepo.getIndexStore().search(filter);
                } catch (NIndexerNotAccessibleException ex) {
                    //just ignore
                } catch (NException ex) {
                    _LOGOP().level(Level.FINEST).verb(NLogVerb.FAIL)
                            .log(NMsg.ofJ("error search operation using Indexer for {0} : {1}", getRepo().getName(), ex));
                }
                if (o != null) {
                    result = IteratorBuilder.of(new IndexFirstIterator<>(o,
                            xrepo.searchImpl(filter, getFetchMode()),session
                    )).onStart(startRunnable).onFinish(endRunnable).build();
                    return this;
                }
            }
            result = IteratorBuilder.of(xrepo.searchImpl(filter, getFetchMode()))
                    .onStart(startRunnable)
                    .onFinish(endRunnable)
                    .build();
        } catch (NNotFoundException | SecurityException ex) {
            _LOGOP().level(Level.FINEST).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("{0} search packages", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST)));
            throw ex;
        } catch (RuntimeException ex) {
            _LOGOP().level(Level.SEVERE).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("{0} search packages", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST)));
            throw ex;
        }
        return this;
    }

    @Override
    public NIterator<NId> getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

}
