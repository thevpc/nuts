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
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.log.NLogUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.spi.NFetchDescriptorRepositoryCommand;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NLogOp;
import net.thevpc.nuts.util.NLogVerb;

/**
 * @author thevpc
 */
public class DefaultNFetchDescriptorRepositoryCommand extends AbstractNFetchDescriptorRepositoryCommand {

    private NLog LOG;

    public DefaultNFetchDescriptorRepositoryCommand(NRepository repo) {
        super(repo);
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultNFetchDescriptorRepositoryCommand.class,session);
        }
        return LOG;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        if (super.configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    @Override
    public NFetchDescriptorRepositoryCommand run() {
//        NutsWorkspace ws = getRepo().getWorkspace();
        NSession session = getSession();
        CoreNIdUtils.checkLongId(id, session);
        NSessionUtils.checkSession(getRepo().getWorkspace(), session);
        getRepo().security().setSession(getSession()).checkAllowed(NConstants.Permissions.FETCH_DESC, "fetch-descriptor");
        Map<String, String> queryMap = id.getProperties();
        queryMap.remove(NConstants.IdProperties.OPTIONAL);
        queryMap.remove(NConstants.IdProperties.SCOPE);
        queryMap.put(NConstants.IdProperties.FACE, NConstants.QueryFaces.DESCRIPTOR);
        id = id.builder().setProperties(queryMap).build();
        NRepositoryExt xrepo = NRepositoryExt.of(getRepo());
        xrepo.checkAllowedFetch(id, session);
        long startTime = System.currentTimeMillis();
        try {
            String versionString = id.getVersion().getValue();
            NDescriptor d = null;
            NVersion nutsVersion = NVersion.of(versionString).orElse(NVersion.BLANK);
            if (nutsVersion.isBlank()||nutsVersion.isReleaseVersion()||nutsVersion.isLatestVersion()) {
                NId a = xrepo.searchLatestVersion(id.builder().setVersion("").build(), null, getFetchMode(), session);
                if (a == null) {
                    throw new NNotFoundException(getSession(), id.getLongId());
                }
                a = a.builder().setFaceDescriptor().build();
                d = xrepo.fetchDescriptorImpl(a, getFetchMode(), session);
            } else {
                if (nutsVersion.isSingleValue()) {
                    id = id.builder().setFaceDescriptor().build();
                    d = xrepo.fetchDescriptorImpl(id, getFetchMode(), session);
                } else {
                    NIdFilter filter = CoreFilterUtils.idFilterOf(id.getProperties(), NIdFilters.of(session).byName(id.getFullName()), null, session);
                    NId a = xrepo.searchLatestVersion(id.builder().setVersion("").build(), filter, getFetchMode(), session);
                    if (a == null) {
                        throw new NNotFoundException(getSession(), id.getLongId());
                    }
                    a = a.builder().setFaceDescriptor().build();
                    d = xrepo.fetchDescriptorImpl(a, getFetchMode(), session);
                }
            }
            if (d == null) {
                throw new NNotFoundException(getSession(), id.getLongId());
            }
            NLogUtils.traceMessage(_LOG(session), Level.FINER, getRepo().getName(), session, getFetchMode(), id.getLongId(), NLogVerb.SUCCESS, "fetch descriptor", startTime, null);
            result = d;
        } catch (Exception ex) {
            if (!CoreNUtils.isUnsupportedFetchModeException(ex)) {
                NLogUtils.traceMessage(_LOG(session), Level.FINEST, getRepo().getName(), session, getFetchMode(), id.getLongId(), NLogVerb.FAIL, "fetch descriptor", startTime, CoreStringUtils.exceptionToMessage(ex));
            }
            throw ex;
        }
        return this;
    }

    @Override
    public NDescriptor getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

}
