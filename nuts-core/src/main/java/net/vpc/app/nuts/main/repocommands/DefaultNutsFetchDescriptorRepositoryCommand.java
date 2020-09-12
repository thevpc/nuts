/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.main.repocommands;

import java.util.Map;
import java.util.logging.Level;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.DefaultNutsVersion;
import net.vpc.app.nuts.runtime.NutsPatternIdFilter;
import net.vpc.app.nuts.runtime.filters.CoreFilterUtils;
import net.vpc.app.nuts.runtime.repocommands.AbstractNutsFetchDescriptorRepositoryCommand;
import net.vpc.app.nuts.core.repos.NutsRepositoryExt;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.common.TraceResult;

/**
 * @author vpc
 * @category SPI Base
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
                NutsIdFilter filter = CoreFilterUtils.idFilterOf(id.getProperties(), new NutsPatternIdFilter(id), null);
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
            CoreNutsUtils.traceMessage(LOG, Level.FINER, getRepo().config().name(), getSession(), getFetchMode(), id.getLongNameId(), TraceResult.SUCCESS, "fetch descriptor", startTime, null);
            result = d;
        } catch (Exception ex) {
            if (!CoreNutsUtils.isUnsupportedFetchModeException(ex)) {
                CoreNutsUtils.traceMessage(LOG, Level.FINEST, getRepo().config().name(), getSession(), getFetchMode(), id.getLongNameId(), TraceResult.FAIL, "fetch descriptor", startTime, CoreNutsUtils.resolveMessageToTraceOrNullIfNutsNotFoundException(ex));
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
