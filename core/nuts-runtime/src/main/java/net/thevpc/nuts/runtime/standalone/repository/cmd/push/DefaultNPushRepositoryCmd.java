/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.push;

import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.spi.NPushRepositoryCmd;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

/**
 *
 * @author thevpc %category SPI Base
 */
public class DefaultNPushRepositoryCmd extends AbstractNPushRepositoryCmd {


    public DefaultNPushRepositoryCmd(NRepository repo) {
        super(repo);
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNPushRepositoryCmd.class);
    }

    @Override
    public NPushRepositoryCmd run() {
        getRepo().security().checkAllowed(NConstants.Permissions.PUSH, "push");
        try {
            NRepositoryExt.of(getRepo()).pushImpl(this);
            _LOG()
                    .log(NMsg.ofC("%s push %s", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST), getId()).withLevel(Level.FINEST).withIntent(NMsgIntent.SUCCESS));
        } catch (RuntimeException ex) {

            if (_LOG().isLoggable(Level.FINEST)) {
                _LOG()
                        .log(NMsg.ofC("%s push %s", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST), getId()).withLevel(Level.FINEST).withIntent(NMsgIntent.FAIL));
            }
        }
        return this;
    }
}
