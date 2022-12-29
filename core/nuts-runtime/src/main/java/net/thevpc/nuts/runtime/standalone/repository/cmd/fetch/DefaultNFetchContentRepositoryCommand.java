/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.cmd.fetch;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.log.NLogUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NFetchContentRepositoryCommand;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.util.NLogger;
import net.thevpc.nuts.util.NLoggerOp;
import net.thevpc.nuts.util.NLoggerVerb;

import java.util.logging.Level;

/**
 *
 * @author thevpc %category SPI Base
 */
public class DefaultNFetchContentRepositoryCommand extends AbstractNFetchContentRepositoryCommand {

    private NLogger LOG;

    public DefaultNFetchContentRepositoryCommand(NRepository repo) {
        super(repo);
    }

    protected NLoggerOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLogger _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLogger.of(DefaultNFetchContentRepositoryCommand.class,session);
        }
        return LOG;
    }

    @Override
    public NFetchContentRepositoryCommand run() {
        NRepository repo = getRepo();
        NSession session = getSession();
        NSessionUtils.checkSession(repo.getWorkspace(), session);
        NDescriptor descriptor0 = descriptor;
        if (descriptor0 == null) {
            NRepositorySPI repoSPI = NWorkspaceUtils.of(session).repoSPI(repo);
            descriptor0 = repoSPI.fetchDescriptor().setId(id).setSession(session)
                    .setFetchMode(getFetchMode())
                    .getResult();
        }
        id = id.builder().setFaceContent().build();
        repo.security().setSession(getSession()).checkAllowed(NConstants.Permissions.FETCH_CONTENT, "fetch-content");
        NRepositoryExt xrepo = NRepositoryExt.of(repo);
        xrepo.checkAllowedFetch(id, session);
        long startTime = System.currentTimeMillis();
        try {
            NPath f = xrepo.fetchContentImpl(id, descriptor0, localPath, getFetchMode(), session);
            if (f == null) {
                throw new NNotFoundException(getSession(), id);
            }
            NLogUtils.traceMessage(_LOG(session), Level.FINER, repo.getName(), session, getFetchMode(), id.getLongId(), NLoggerVerb.SUCCESS, "fetch package", startTime, null);
            result = f;
        } catch (RuntimeException ex) {
            if (!CoreNUtils.isUnsupportedFetchModeException(ex)) {
                NLogUtils.traceMessage(_LOG(session), Level.FINEST, repo.getName(), session, getFetchMode(), id.getLongId(), NLoggerVerb.FAIL, "fetch package", startTime, CoreStringUtils.exceptionToMessage(ex));
            }
            throw ex;
        }
        return this;
    }

    @Override
    public NFetchContentRepositoryCommand setId(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public NId getId() {
        return id;
    }

}
