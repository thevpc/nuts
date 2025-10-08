/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
package net.thevpc.nuts.runtime.standalone.repository.cmd.fetch;

import net.thevpc.nuts.artifact.NArtifactNotFoundException;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.runtime.standalone.log.NLogUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NFetchContentRepositoryCmd;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.log.NLog;


import java.util.logging.Level;

/**
 *
 * @author thevpc %category SPI Base
 */
public class DefaultNFetchContentRepositoryCmd extends AbstractNFetchContentRepositoryCmd {


    public DefaultNFetchContentRepositoryCmd(NRepository repo) {
        super(repo);
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNFetchContentRepositoryCmd.class);
    }

    @Override
    public NFetchContentRepositoryCmd run() {
        NRepository repo = getRepo();
        NDescriptor descriptor0 = descriptor;
        if (descriptor0 == null) {
            NRepositorySPI repoSPI = NWorkspaceUtils.of(getRepo().getWorkspace()).toRepositorySPI(repo);
            descriptor0 = repoSPI.fetchDescriptor().setId(id)
                    .setFetchMode(getFetchMode())
                    .getResult();
        }
        id = id.builder().setFaceContent().build();
        repo.security().checkAllowed(NConstants.Permissions.FETCH_CONTENT, "fetch-content");
        NRepositoryExt xrepo = NRepositoryExt.of(repo);
        xrepo.checkAllowedFetch(id);
        long startTime = System.currentTimeMillis();
        try {
            NPath f = xrepo.fetchContentImpl(id, descriptor0, getFetchMode());
            if (f == null) {
                throw new NArtifactNotFoundException(id);
            }
            NLogUtils.traceMessage(_LOG(), Level.FINER, repo.getName(), getFetchMode(), id.getLongId(), NMsgIntent.SUCCESS, "fetch content", startTime, null);
            result = f;
        } catch (RuntimeException ex) {
            if (!CoreNUtils.isUnsupportedFetchModeException(ex)) {
                NLogUtils.traceMessage(_LOG(), Level.FINEST, repo.getName(), getFetchMode(), id.getLongId(), NMsgIntent.FAIL, "fetch content", startTime, CoreStringUtils.exceptionToMessage(ex));
            }
            throw ex;
        }
        return this;
    }

    @Override
    public NFetchContentRepositoryCmd setId(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public NId getId() {
        return id;
    }

}
