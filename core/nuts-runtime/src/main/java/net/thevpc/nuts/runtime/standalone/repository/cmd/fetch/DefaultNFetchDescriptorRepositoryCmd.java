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

import java.util.Map;
import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.log.NLogUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryExt;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.spi.NFetchDescriptorRepositoryCmd;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;

/**
 * @author thevpc
 */
public class DefaultNFetchDescriptorRepositoryCmd extends AbstractNFetchDescriptorRepositoryCmd {


    public DefaultNFetchDescriptorRepositoryCmd(NRepository repo) {
        super(repo);
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNFetchDescriptorRepositoryCmd.class);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        if (super.configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    @Override
    public NFetchDescriptorRepositoryCmd run() {
//        NutsWorkspace ws = getRepo().getWorkspace();
        NSession session = getRepo().getWorkspace().currentSession();
        CoreNIdUtils.checkLongId(id);
        NSessionUtils.checkSession(getRepo().getWorkspace(), session);
        getRepo().security().checkAllowed(NConstants.Permissions.FETCH_DESC, "fetch-descriptor");
        Map<String, String> queryMap = id.getProperties();
        queryMap.remove(NConstants.IdProperties.OPTIONAL);
        queryMap.remove(NConstants.IdProperties.SCOPE);
        queryMap.put(NConstants.IdProperties.FACE, NConstants.QueryFaces.DESCRIPTOR);
        id = id.builder().setProperties(queryMap).build();
        NRepositoryExt xrepo = NRepositoryExt.of(getRepo());
        xrepo.checkAllowedFetch(id);
        long startTime = System.currentTimeMillis();
        try {
            String versionString = id.getVersion().getValue();
            NDescriptor d = null;
            NVersion nutsVersion = NVersion.of(versionString).orElse(NVersion.BLANK);
            if (nutsVersion.isBlank() || nutsVersion.isReleaseVersion() || nutsVersion.isLatestVersion()) {
                NId a = xrepo.searchLatestVersion(id.builder().setVersion("").build(), null, getFetchMode());
                if (a == null) {
                    throw new NNotFoundException(id.getLongId());
                }
                a = a.builder().setFaceDescriptor().build();
                d = xrepo.fetchDescriptorImpl(a, getFetchMode());
            } else {
                if (nutsVersion.isSingleValue()) {
                    id = id.builder().setFaceDescriptor().build();
                    d = xrepo.fetchDescriptorImpl(id, getFetchMode());
                } else {
                    NIdFilter filter = CoreFilterUtils.idFilterOf(id.getProperties(), NIdFilters.of().byName(id.getFullName()), null);
                    NId a = xrepo.searchLatestVersion(id.builder().setVersion("").build(), filter, getFetchMode());
                    if (a == null) {
                        throw new NNotFoundException(id.getLongId());
                    }
                    a = a.builder().setFaceDescriptor().build();
                    d = xrepo.fetchDescriptorImpl(a, getFetchMode());
                }
            }
            if (d == null) {
                throw new NNotFoundException(id.getLongId());
            }
            NLogUtils.traceMessage(_LOG(), Level.FINER, getRepo().getName(), getFetchMode(), id.getLongId(), NLogVerb.SUCCESS, "fetch descriptor", startTime, null);
            result = d;
        } catch (Exception ex) {
            if (!CoreNUtils.isUnsupportedFetchModeException(ex)) {
                NLogUtils.traceMessage(_LOG(), Level.FINEST, getRepo().getName(), getFetchMode(), id.getLongId(), NLogVerb.FAIL, "fetch descriptor", startTime, CoreStringUtils.exceptionToMessage(ex));
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
