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
import net.thevpc.nuts.spi.NPushRepositoryCmd;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

/**
 *
 * @author thevpc %category SPI Base
 */
public class DefaultNPushRepositoryCmd extends AbstractNPushRepositoryCmd {

    private NLog LOG;

    public DefaultNPushRepositoryCmd(NRepository repo) {
        super(repo);
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultNPushRepositoryCmd.class,session);
        }
        return LOG;
    }

    @Override
    public NPushRepositoryCmd run() {
        NSession session = getSession();
        NSessionUtils.checkSession(getRepo().getWorkspace(), session);
        getRepo().security().setSession(session).checkAllowed(NConstants.Permissions.PUSH, "push");
        try {
            NRepositoryExt.of(getRepo()).pushImpl(this);
                _LOGOP(session).level(Level.FINEST).verb(NLogVerb.SUCCESS)
                        .log(NMsg.ofC("%s push %s", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST), getId()));
        } catch (RuntimeException ex) {

            if (_LOG(session).isLoggable(Level.FINEST)) {
                _LOGOP(session).level(Level.FINEST).verb(NLogVerb.FAIL)
                        .log(NMsg.ofC("%s push %s", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST), getId()));
            }
        }
        return this;
    }
}
