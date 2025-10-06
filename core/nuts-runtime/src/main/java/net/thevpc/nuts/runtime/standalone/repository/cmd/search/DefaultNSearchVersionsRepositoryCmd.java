/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.search;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NFetchMode;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.text.NPositionType;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionHelper;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.util.NIteratorBuilder;
import net.thevpc.nuts.util.NIteratorUtils;
import net.thevpc.nuts.spi.NSearchVersionsRepositoryCmd;

/**
 * @author thevpc %category SPI Base
 */
public class DefaultNSearchVersionsRepositoryCmd extends AbstractNSearchVersionsRepositoryCmd {

    public DefaultNSearchVersionsRepositoryCmd(NRepository repo) {
        super(repo);
    }

    protected NLog _LOG() {
            return NLog.of(DefaultNSearchVersionsRepositoryCmd.class);
    }

    @Override
    public NSearchVersionsRepositoryCmd run() {
        NSession session = getRepo().getWorkspace().currentSession();
        //id = id.builder().setFaceContent().build();
        getRepo().security().checkAllowed(NConstants.Permissions.FETCH_DESC, "find-versions");
        NRepositoryExt xrepo = NRepositoryExt.of(getRepo());
        CoreNIdUtils.checkShortId(id);
        xrepo.checkAllowedFetch(id);
        try {
            List<NIterator<? extends NId>> resultList = new ArrayList<>();
            if(getFetchMode()== NFetchMode.REMOTE) {
                if (session.isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled()) {
                    NIterator<NId> d = null;
                    try {
                        d = xrepo.getIndexStore().searchVersions(id);
                    } catch (NException ex) {
                        _LOG()
                                .log(NMsg.ofC("error finding version with Indexer for %s : %s", getRepo().getName(), ex)
                                        .withLevel(Level.FINEST).withIntent(NMsgIntent.FAIL));
                    }
                    if (d != null && filter != null) {
                        resultList.add(
                                NIteratorBuilder.of(d).filter(
                                        x -> filter.acceptDefinition(NDefinitionHelper.ofIdOnlyFromRepo(x,repo, "DefaultNSearchVersionsRepositoryCmd")),
                                        () -> NElementDescribables.describeResolveOrToString(filter)
                                ).safeIgnore().iterator()
                        );
                    }
                }
            }
            NIterator<NId> rr = xrepo.searchVersionsImpl(id, getFilter(), getFetchMode());
            if (rr != null) {
                resultList.add(rr);
            }
            result = NIteratorUtils.coalesce(resultList);
            return this;
        } catch (RuntimeException ex) {
            _LOG()
                    .log(NMsg.ofC("[%s] %s %s %s",
                            NStringUtils.formatAlign(getFetchMode().toString(), 7, NPositionType.FIRST),
                            NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST),
                            NStringUtils.formatAlign("Fetch versions for", 24, NPositionType.FIRST),
                            id)
                            .withLevel(Level.FINEST).withIntent(NMsgIntent.FAIL));
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
