/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.util.NIndexFirstIterator;
import net.thevpc.nuts.util.NIteratorBuilder;
import net.thevpc.nuts.log.NLog;

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

    protected NLog _LOG() {
            return NLog.of(DefaultNSearchRepositoryCmd.class);
    }

    @Override
    public NSearchRepositoryCmd run() {
        NSession session = getRepo().getWorkspace().currentSession();
        NRunnable startRunnable = NRunnable.of(
                () -> {
                    getRepo().security().checkAllowed(NConstants.Permissions.FETCH_DESC, "search");
                    NRepositoryExt xrepo = NRepositoryExt.of(getRepo());
                    xrepo.checkAllowedFetch(null);
                    _LOG()
                            .log(NMsg.ofJ("{0} search packages", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST))
                                    .withLevel(Level.FINEST).withIntent(NMsgIntent.START));
                }
        ).redescribe(NElementDescribables.ofDesc("CheckAuthorizations"));
        NRunnable endRunnable =
                NRunnable.of(
                        () -> _LOG()
                                .log(NMsg.ofJ("{0} search packages", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST))
                                        .withLevel(Level.FINEST).withIntent(NMsgIntent.SUCCESS))
                        ).redescribe(NElementDescribables.ofDesc("Log"));
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
                    _LOG()
                            .log(NMsg.ofJ("error search operation using Indexer for {0} : {1}", getRepo().getName(), ex)
                                    .withLevel(Level.FINEST).withIntent(NMsgIntent.FAIL));
                }
                if (o != null) {
                    result = NIteratorBuilder.of(new NIndexFirstIterator<>(o,
                            xrepo.searchImpl(filter, getFetchMode())
                    )).onStart(startRunnable).onFinish(endRunnable).build();
                    return this;
                }
            }
            result = NIteratorBuilder.of(xrepo.searchImpl(filter, getFetchMode()))
                    .onStart(startRunnable)
                    .onFinish(endRunnable)
                    .build();
        } catch (NNotFoundException | SecurityException ex) {
            _LOG()
                    .log(NMsg.ofJ("{0} search packages", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST))
                            .withLevel(Level.FINEST).withIntent(NMsgIntent.FAIL));
            throw ex;
        } catch (RuntimeException ex) {
            _LOG()
                    .log(NMsg.ofJ("{0} search packages", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST))
                            .withLevel(Level.SEVERE).withIntent(NMsgIntent.FAIL));
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
