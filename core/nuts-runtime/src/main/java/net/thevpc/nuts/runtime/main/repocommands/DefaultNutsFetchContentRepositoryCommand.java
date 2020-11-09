/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.main.repocommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.repocommands.AbstractNutsFetchContentRepositoryCommand;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.util.common.TraceResult;

import java.util.logging.Level;

/**
 *
 * @author vpc
 * @category SPI Base
 */
public class DefaultNutsFetchContentRepositoryCommand extends AbstractNutsFetchContentRepositoryCommand {

    private final NutsLogger LOG;

    public DefaultNutsFetchContentRepositoryCommand(NutsRepository repo) {
        super(repo);
        LOG = repo.getWorkspace().log().of(DefaultNutsFetchContentRepositoryCommand.class);
    }

    @Override
    public NutsFetchContentRepositoryCommand run() {
        NutsWorkspaceUtils.of(getRepo().getWorkspace()).checkSession(getSession());
        NutsDescriptor descriptor0 = descriptor;
        if (descriptor0 == null) {
            descriptor0 = getRepo().fetchDescriptor().setId(id).setSession(getSession())
                    .setFetchMode(getFetchMode())
                    .getResult();
        }
        id = id.builder().setFaceContent().build();
        getRepo().security().checkAllowed(NutsConstants.Permissions.FETCH_CONTENT, "fetch-content");
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        xrepo.checkAllowedFetch(id, getSession());
        long startTime = System.currentTimeMillis();
        try {
            NutsContent f = xrepo.fetchContentImpl(id, descriptor0, localPath, getFetchMode(), getSession());
            if (f == null) {
                throw new NutsNotFoundException(getRepo().getWorkspace(), id);
            }
            CoreNutsUtils.traceMessage(LOG, Level.FINER, getRepo().getName(), getSession(), getFetchMode(), id.getLongNameId(), TraceResult.SUCCESS, "fetch component", startTime, null);
            result = f;
        } catch (RuntimeException ex) {
            if (!CoreNutsUtils.isUnsupportedFetchModeException(ex)) {
                CoreNutsUtils.traceMessage(LOG, Level.FINEST, getRepo().getName(), getSession(), getFetchMode(), id.getLongNameId(), TraceResult.FAIL, "fetch component", startTime, CoreStringUtils.exceptionToString(ex));
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
