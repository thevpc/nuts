/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.deploy;

import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.spi.NDeployRepositoryCmd;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

/**
 *
 * @author thevpc %category SPI Base
 */
public class DefaultNDeployRepositoryCmd extends AbstractNDeployRepositoryCmd {


    public DefaultNDeployRepositoryCmd(NRepository repo) {
        super(repo);
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNDeployRepositoryCmd.class);
    }

    @Override
    public NDeployRepositoryCmd run() {
        NSession session = repo.getWorkspace().currentSession();
        getRepo().security().checkAllowed(NConstants.Permissions.DEPLOY, "deploy");
        checkParameters();
        try {
            NRepositoryExt xrepo = NRepositoryExt.of(repo);
            NDescriptor rep = xrepo.deployImpl(this);
            this.setDescriptor(rep);
            this.setId(rep.getId());
            if (session.isIndexed() && xrepo.getIndexStore() != null && xrepo.getIndexStore().isEnabled()) {
                try {
                    xrepo.getIndexStore().revalidate(this.getId());
                } catch (NException ex) {
                    _LOGOP().level(Level.FINEST).verb(NLogVerb.FAIL)
                            .log(NMsg.ofJ("error revalidating Indexer for {0} : {1}", getRepo().getName(), ex));
                }
            }
            _LOGOP().level(Level.FINEST).verb(NLogVerb.SUCCESS)
                    .log(NMsg.ofJ("{0} deploy {1}", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST), this.getId()));
        } catch (RuntimeException ex) {
            _LOGOP().level(Level.FINEST).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("{0} deploy {1}", NStringUtils.formatAlign(getRepo().getName(), 20, NPositionType.FIRST), this.getId()));
            throw ex;
        }
        return this;
    }

}
