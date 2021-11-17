/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.search;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.iter.IndexFirstIterator;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsRepositoryExt;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSearchRepositoryCommand;

import java.util.Iterator;
import java.util.logging.Level;

/**
 * @author thevpc %category SPI Base
 */
public class DefaultNutsSearchRepositoryCommand extends AbstractNutsSearchRepositoryCommand {

    private NutsLogger LOG;

    public DefaultNutsSearchRepositoryCommand(NutsRepository repo) {
        super(repo);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsSearchRepositoryCommand.class,session);
        }
        return LOG;
    }

    @Override
    public NutsSearchRepositoryCommand run() {
        NutsSession session = getSession();
        NutsWorkspaceUtils.checkSession(getRepo().getWorkspace(), session);
        getRepo().security().setSession(session).checkAllowed(NutsConstants.Permissions.FETCH_DESC, "search");
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        xrepo.checkAllowedFetch(null, session);
        try {
            _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.START)
                    .log(NutsMessage.jstyle("{0} search packages", CoreStringUtils.alignLeft(getRepo().getName(), 20)));
            boolean processIndexFirst = session.isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled();
            if (processIndexFirst) {
                Iterator<NutsId> o = null;
                try {
                    o = xrepo.getIndexStore().search(filter, session);
                } catch (NutsIndexerNotAccessibleException ex) {
                    //just ignore
                } catch (NutsException ex) {
                    _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.FAIL)
                            .log(NutsMessage.jstyle("error search operation using Indexer for {0} : {1}", getRepo().getName(), ex));
                }
                if (o != null) {
                    result = new IndexFirstIterator<>(o,
                            xrepo.searchImpl(filter, getFetchMode(), session)
                    );
                    _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.SUCCESS)
                            .log(NutsMessage.jstyle("{0} Search packages (indexer)", CoreStringUtils.alignLeft(getRepo().getName(), 20)));
                    return this;
                }
            }
            result = xrepo.searchImpl(filter, getFetchMode(), session);
            _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.SUCCESS)
                    .log(NutsMessage.jstyle("{0} search packages", CoreStringUtils.alignLeft(getRepo().getName(), 20)));
        } catch (NutsNotFoundException | SecurityException ex) {
            _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("{0} search packages", CoreStringUtils.alignLeft(getRepo().getName(), 20)));
            throw ex;
        } catch (RuntimeException ex) {
            _LOGOP(session).level(Level.SEVERE).verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("{0} search packages", CoreStringUtils.alignLeft(getRepo().getName(), 20)));
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
