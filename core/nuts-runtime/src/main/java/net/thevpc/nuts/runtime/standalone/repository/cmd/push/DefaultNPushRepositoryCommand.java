/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.push;

import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.spi.NPushRepositoryCommand;
import net.thevpc.nuts.util.NLogger;
import net.thevpc.nuts.util.NLoggerOp;
import net.thevpc.nuts.util.NLoggerVerb;
import net.thevpc.nuts.util.NStringUtils;

/**
 *
 * @author thevpc %category SPI Base
 */
public class DefaultNPushRepositoryCommand extends AbstractNPushRepositoryCommand {

    private NLogger LOG;

    public DefaultNPushRepositoryCommand(NRepository repo) {
        super(repo);
    }

    protected NLoggerOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLogger _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLogger.of(DefaultNPushRepositoryCommand.class,session);
        }
        return LOG;
    }

    @Override
    public NPushRepositoryCommand run() {
        NSession session = getSession();
        NSessionUtils.checkSession(getRepo().getWorkspace(), session);
        getRepo().security().setSession(session).checkAllowed(NConstants.Permissions.PUSH, "push");
        try {
            NRepositoryExt.of(getRepo()).pushImpl(this);
                _LOGOP(session).level(Level.FINEST).verb(NLoggerVerb.SUCCESS)
                        .log(NMsg.ofJstyle("{0} push {1}", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST), getId()));
        } catch (RuntimeException ex) {

            if (_LOG(session).isLoggable(Level.FINEST)) {
                _LOGOP(session).level(Level.FINEST).verb(NLoggerVerb.FAIL)
                        .log(NMsg.ofJstyle("{0} push {1}", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST), getId()));
            }
        }
        return this;
    }
}