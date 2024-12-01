/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy;

import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.spi.NRepositoryUndeployCmd;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

/**
 *
 * @author thevpc %category SPI Base
 */
public class DefaultNRepositoryUndeployCmd extends AbstractNRepositoryUndeployCmd {


    public DefaultNRepositoryUndeployCmd(NRepository repo) {
        super(repo);
    }

    public DefaultNRepositoryUndeployCmd(NWorkspace ws) {
        super(null);
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNRepositoryUndeployCmd.class);
    }

    @Override
    public NRepositoryUndeployCmd run() {
        NSession session = getRepo().getWorkspace().currentSession();
        getRepo().security().checkAllowed(NConstants.Permissions.UNDEPLOY, "undeploy");
        try {
            NRepositoryExt xrepo = NRepositoryExt.of(getRepo());
            xrepo.undeployImpl(this);
            if (session.isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled()) {
                try {
                    xrepo.getIndexStore().invalidate(this.getId());
                } catch (NException ex) {
                    _LOGOP().level(Level.FINEST).verb(NLogVerb.FAIL).log(
                            NMsg.ofC("error invalidating Indexer for %s : %s", getRepo().getName(), ex));
                }
            }
            _LOGOP().level(Level.FINEST).verb(NLogVerb.SUCCESS)
                    .log(NMsg.ofC("%s undeploy %s", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST), this.getId()));
        } catch (RuntimeException ex) {
            _LOGOP().level(Level.FINEST).verb(NLogVerb.FAIL)
                    .log(NMsg.ofC("%s undeploy %s", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST), this.getId()));
        }
        return this;
    }

}
