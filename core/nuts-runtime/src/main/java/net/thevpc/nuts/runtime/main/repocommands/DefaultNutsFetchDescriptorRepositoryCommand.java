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

import java.util.Map;
import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.repocommands.AbstractNutsFetchDescriptorRepositoryCommand;
import net.thevpc.nuts.runtime.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.DefaultNutsVersion;
import net.thevpc.nuts.runtime.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.util.common.TraceResult;

/**
 * @author vpc
 */
public class DefaultNutsFetchDescriptorRepositoryCommand extends AbstractNutsFetchDescriptorRepositoryCommand {

    private final NutsLogger LOG;

    public DefaultNutsFetchDescriptorRepositoryCommand(NutsRepository repo) {
        super(repo);
        LOG = repo.getWorkspace().log().of(DefaultNutsFetchDescriptorRepositoryCommand.class);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        if (super.configureFirst(cmd)) {
            return true;
        }
        return false;
    }

    @Override
    public NutsFetchDescriptorRepositoryCommand run() {
        NutsWorkspace ws = getRepo().getWorkspace();
        NutsWorkspaceUtils.of(ws).checkLongNameNutsId(id);
        NutsWorkspaceUtils.of(ws).checkSession(getSession());
        getRepo().security().checkAllowed(NutsConstants.Permissions.FETCH_DESC, "fetch-descriptor");
        Map<String, String> queryMap = id.getProperties();
        queryMap.remove(NutsConstants.IdProperties.OPTIONAL);
        queryMap.remove(NutsConstants.IdProperties.SCOPE);
        queryMap.put(NutsConstants.IdProperties.FACE, NutsConstants.QueryFaces.DESCRIPTOR);
        id = id.builder().setProperties(queryMap).build();
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        xrepo.checkAllowedFetch(id, getSession());
        long startTime = System.currentTimeMillis();
        try {
            String versionString = id.getVersion().getValue();
            NutsDescriptor d = null;
            if (DefaultNutsVersion.isBlank(versionString)) {
                NutsId a = xrepo.searchLatestVersion(id.builder().setVersion("").build(), null, getFetchMode(), getSession());
                if (a == null) {
                    throw new NutsNotFoundException(ws, id.getLongNameId());
                }
                a = a.builder().setFaceDescriptor().build();
                d = xrepo.fetchDescriptorImpl(a, getFetchMode(), getSession());
            } else if (DefaultNutsVersion.isStaticVersionPattern(versionString)) {
                id = id.builder().setFaceDescriptor().build();
                d = xrepo.fetchDescriptorImpl(id, getFetchMode(), getSession());
            } else {
                NutsIdFilter filter = CoreFilterUtils.idFilterOf(id.getProperties(), ws.id().filter().byName(id.getFullName()), null,ws);
                NutsId a = xrepo.searchLatestVersion(id.builder().setVersion("").build(), filter, getFetchMode(), getSession());
                if (a == null) {
                    throw new NutsNotFoundException(ws, id.getLongNameId());
                }
                a = a.builder().setFaceDescriptor().build();
                d = xrepo.fetchDescriptorImpl(a, getFetchMode(), getSession());
            }
            if (d == null) {
                throw new NutsNotFoundException(ws, id.getLongNameId());
            }
            CoreNutsUtils.traceMessage(LOG, Level.FINER, getRepo().getName(), getSession(), getFetchMode(), id.getLongNameId(), TraceResult.SUCCESS, "fetch descriptor", startTime, null);
            result = d;
        } catch (Exception ex) {
            if (!CoreNutsUtils.isUnsupportedFetchModeException(ex)) {
                CoreNutsUtils.traceMessage(LOG, Level.FINEST, getRepo().getName(), getSession(), getFetchMode(), id.getLongNameId(), TraceResult.FAIL, "fetch descriptor", startTime, CoreStringUtils.exceptionToString(ex));
            }
            throw ex;
        }
        return this;
    }

    @Override
    public NutsDescriptor getResult() {
        if (result == null) {
            run();
        }
        return result;
    }


}
