/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy;

import java.util.logging.Level;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.security.NSecurityManager;
import net.thevpc.nuts.text.NPositionType;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.log.NLog;

import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.spi.NUndeployRepositoryCmd;
import net.thevpc.nuts.util.NException;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NStringUtils;

/**
 *
 * @author thevpc %category SPI Base
 */
public class DefaultNUndeployRepositoryCmd extends AbstractNUndeployRepositoryCmd {


    public DefaultNUndeployRepositoryCmd(NRepository repo) {
        super(repo);
    }

    public DefaultNUndeployRepositoryCmd() {
        super(null);
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNUndeployRepositoryCmd.class);
    }

    @Override
    public NUndeployRepositoryCmd run() {
        NSession session = getRepo().workspace().currentSession();
        NSecurityManager.of().checkRepositoryAllowed(getRepo().uuid(), NConstants.Permissions.UNDEPLOY, "undeploy");
        try {
            NRepositoryExt xrepo = NRepositoryExt.of(getRepo());
            xrepo.undeployImpl(this);
            if (session.isIndexed() && xrepo.indexStore() != null && xrepo.indexStore().isEnabled()) {
                try {
                    xrepo.indexStore().invalidate(this.id());
                } catch (NException ex) {
                    _LOG().log(
                            NMsg.ofC("error invalidating Indexer for %s : %s", getRepo().name(), ex)
                                    .withLevel(Level.FINEST).withIntent(NMsgIntent.FAIL)
                    );
                }
            }
            _LOG()
                    .log(NMsg.ofC("%s undeploy %s", NStringUtils.formatAlign(getRepo().name(), 20, NPositionType.FIRST), this.id())
                            .withLevel(Level.FINEST).withIntent(NMsgIntent.SUCCESS)
                    );
        } catch (RuntimeException ex) {
            _LOG()
                    .log(NMsg.ofC("%s undeploy %s", NStringUtils.formatAlign(getRepo().name(), 20, NPositionType.FIRST), this.id())
                            .withLevel(Level.FINEST).withIntent(NMsgIntent.FAIL)
                    );
        }
        return this;
    }

}
