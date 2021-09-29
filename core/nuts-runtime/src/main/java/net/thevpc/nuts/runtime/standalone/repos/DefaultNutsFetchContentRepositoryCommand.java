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
package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.repocommands.AbstractNutsFetchContentRepositoryCommand;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsFetchContentRepositoryCommand;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.util.logging.Level;

/**
 *
 * @author thevpc %category SPI Base
 */
public class DefaultNutsFetchContentRepositoryCommand extends AbstractNutsFetchContentRepositoryCommand {

    private NutsLogger LOG;

    public DefaultNutsFetchContentRepositoryCommand(NutsRepository repo) {
        super(repo);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = session.log().of(DefaultNutsFetchContentRepositoryCommand.class);
        }
        return LOG;
    }

    @Override
    public NutsFetchContentRepositoryCommand run() {
        NutsRepository repo = getRepo();
        NutsSession session = getSession();
        NutsWorkspaceUtils.checkSession(repo.getWorkspace(), session);
        NutsDescriptor descriptor0 = descriptor;
        if (descriptor0 == null) {
            NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(repo);
            descriptor0 = repoSPI.fetchDescriptor().setId(id).setSession(session)
                    .setFetchMode(getFetchMode())
                    .getResult();
        }
        id = id.builder().setFaceContent().build();
        repo.security().setSession(getSession()).checkAllowed(NutsConstants.Permissions.FETCH_CONTENT, "fetch-content");
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(repo);
        xrepo.checkAllowedFetch(id, session);
        long startTime = System.currentTimeMillis();
        try {
            NutsContent f = xrepo.fetchContentImpl(id, descriptor0, localPath, getFetchMode(), session);
            if (f == null) {
                throw new NutsNotFoundException(getSession(), id);
            }
            CoreNutsUtils.traceMessage(_LOG(session), Level.FINER, repo.getName(), session, getFetchMode(), id.getLongNameId(), NutsLogVerb.SUCCESS, "fetch package", startTime, null);
            result = f;
        } catch (RuntimeException ex) {
            if (!CoreNutsUtils.isUnsupportedFetchModeException(ex)) {
                CoreNutsUtils.traceMessage(_LOG(session), Level.FINEST, repo.getName(), session, getFetchMode(), id.getLongNameId(), NutsLogVerb.FAIL, "fetch package", startTime, CoreStringUtils.exceptionToMessage(ex));
            }
            throw ex;
        }
        return this;
    }

    @Override
    public NutsFetchContentRepositoryCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsId getId() {
        return id;
    }

}
