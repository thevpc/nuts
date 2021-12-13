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

import java.util.Map;
import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.log.NutsLogUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.NutsRepositoryExt;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.version.DefaultNutsVersion;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.spi.NutsFetchDescriptorRepositoryCommand;

/**
 * @author thevpc
 */
public class DefaultNutsFetchDescriptorRepositoryCommand extends AbstractNutsFetchDescriptorRepositoryCommand {

    private NutsLogger LOG;

    public DefaultNutsFetchDescriptorRepositoryCommand(NutsRepository repo) {
        super(repo);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = NutsLogger.of(DefaultNutsFetchDescriptorRepositoryCommand.class,session);
        }
        return LOG;
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
//        NutsWorkspace ws = getRepo().getWorkspace();
        NutsSession session = getSession();
        NutsWorkspaceUtils.of(session).checkLongId(id, session);
        NutsWorkspaceUtils.checkSession(getRepo().getWorkspace(), session);
        getRepo().security().setSession(getSession()).checkAllowed(NutsConstants.Permissions.FETCH_DESC, "fetch-descriptor");
        Map<String, String> queryMap = id.getProperties();
        queryMap.remove(NutsConstants.IdProperties.OPTIONAL);
        queryMap.remove(NutsConstants.IdProperties.SCOPE);
        queryMap.put(NutsConstants.IdProperties.FACE, NutsConstants.QueryFaces.DESCRIPTOR);
        id = id.builder().setProperties(queryMap).build();
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        xrepo.checkAllowedFetch(id, session);
        long startTime = System.currentTimeMillis();
        try {
            String versionString = id.getVersion().getValue();
            NutsDescriptor d = null;
            if (DefaultNutsVersion.isBlankVersion(versionString)) {
                NutsId a = xrepo.searchLatestVersion(id.builder().setVersion("").build(), null, getFetchMode(), session);
                if (a == null) {
                    throw new NutsNotFoundException(getSession(), id.getLongId());
                }
                a = a.builder().setFaceDescriptor().build();
                d = xrepo.fetchDescriptorImpl(a, getFetchMode(), session);
            } else if (DefaultNutsVersion.isStaticVersionPattern(versionString)) {
                id = id.builder().setFaceDescriptor().build();
                d = xrepo.fetchDescriptorImpl(id, getFetchMode(), session);
            } else {
                NutsIdFilter filter = CoreFilterUtils.idFilterOf(id.getProperties(), NutsIdFilters.of(session).byName(id.getFullName()), null, session);
                NutsId a = xrepo.searchLatestVersion(id.builder().setVersion("").build(), filter, getFetchMode(), session);
                if (a == null) {
                    throw new NutsNotFoundException(getSession(), id.getLongId());
                }
                a = a.builder().setFaceDescriptor().build();
                d = xrepo.fetchDescriptorImpl(a, getFetchMode(), session);
            }
            if (d == null) {
                throw new NutsNotFoundException(getSession(), id.getLongId());
            }
            NutsLogUtils.traceMessage(_LOG(session), Level.FINER, getRepo().getName(), session, getFetchMode(), id.getLongId(), NutsLogVerb.SUCCESS, "fetch descriptor", startTime, null);
            result = d;
        } catch (Exception ex) {
            if (!CoreNutsUtils.isUnsupportedFetchModeException(ex)) {
                NutsLogUtils.traceMessage(_LOG(session), Level.FINEST, getRepo().getName(), session, getFetchMode(), id.getLongId(), NutsLogVerb.FAIL, "fetch descriptor", startTime, CoreStringUtils.exceptionToMessage(ex));
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
