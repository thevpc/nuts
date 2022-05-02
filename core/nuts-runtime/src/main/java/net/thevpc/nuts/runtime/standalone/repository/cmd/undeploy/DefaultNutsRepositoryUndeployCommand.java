/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy;

import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NutsPositionType;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsRepositoryExt;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.spi.NutsRepositoryUndeployCommand;
import net.thevpc.nuts.util.NutsStringUtils;

/**
 *
 * @author thevpc %category SPI Base
 */
public class DefaultNutsRepositoryUndeployCommand extends AbstractNutsRepositoryUndeployCommand {

    private NutsLogger LOG;

    public DefaultNutsRepositoryUndeployCommand(NutsRepository repo) {
        super(repo);
    }

    public DefaultNutsRepositoryUndeployCommand(NutsWorkspace ws) {
        super(null);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsRepositoryUndeployCommand.class,session);
        }
        return LOG;
    }

    @Override
    public NutsRepositoryUndeployCommand run() {
        NutsSession session = getSession();
        NutsSessionUtils.checkSession(getRepo().getWorkspace(), session);
        getRepo().security().setSession(session).checkAllowed(NutsConstants.Permissions.UNDEPLOY, "undeploy");
        try {
            NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
            xrepo.undeployImpl(this);
            if (session.isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled()) {
                try {
                    xrepo.getIndexStore().invalidate(this.getId(), session);
                } catch (NutsException ex) {
                    _LOGOP(session).level(Level.FINEST).verb(NutsLoggerVerb.FAIL).log(
                            NutsMessage.jstyle("error invalidating Indexer for {0} : {1}", getRepo().getName(), ex));
                }
            }
            _LOGOP(session).level(Level.FINEST).verb(NutsLoggerVerb.SUCCESS)
                    .log(NutsMessage.jstyle("{0} undeploy {1}", NutsStringUtils.formatAlign(getRepo().getName(), 20, NutsPositionType.FIRST), this.getId()));
        } catch (RuntimeException ex) {
            _LOGOP(session).level(Level.FINEST).verb(NutsLoggerVerb.FAIL)
                    .log(NutsMessage.jstyle("{0} undeploy {1}", NutsStringUtils.formatAlign(getRepo().getName(), 20,NutsPositionType.FIRST), this.getId()));
        }
        return this;
    }

}
