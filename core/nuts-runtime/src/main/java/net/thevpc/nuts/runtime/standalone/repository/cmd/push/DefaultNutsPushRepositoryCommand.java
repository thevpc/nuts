/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.push;

import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NutsPositionType;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsRepositoryExt;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.spi.NutsPushRepositoryCommand;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.util.NutsStringUtils;

/**
 *
 * @author thevpc %category SPI Base
 */
public class DefaultNutsPushRepositoryCommand extends AbstractNutsPushRepositoryCommand {

    private NutsLogger LOG;

    public DefaultNutsPushRepositoryCommand(NutsRepository repo) {
        super(repo);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsPushRepositoryCommand.class,session);
        }
        return LOG;
    }

    @Override
    public NutsPushRepositoryCommand run() {
        NutsSession session = getSession();
        NutsSessionUtils.checkSession(getRepo().getWorkspace(), session);
        getRepo().security().setSession(session).checkAllowed(NutsConstants.Permissions.PUSH, "push");
        try {
            NutsRepositoryExt.of(getRepo()).pushImpl(this);
                _LOGOP(session).level(Level.FINEST).verb(NutsLoggerVerb.SUCCESS)
                        .log(NutsMessage.jstyle("{0} push {1}", NutsStringUtils.formatAlign(getRepo().getName(), 20, NutsPositionType.FIRST), getId()));
        } catch (RuntimeException ex) {

            if (_LOG(session).isLoggable(Level.FINEST)) {
                _LOGOP(session).level(Level.FINEST).verb(NutsLoggerVerb.FAIL)
                        .log(NutsMessage.jstyle("{0} push {1}", NutsStringUtils.formatAlign(getRepo().getName(), 20,NutsPositionType.FIRST), getId()));
            }
        }
        return this;
    }
}
