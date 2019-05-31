/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.filters.CoreFilterUtils;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.TraceResult;

/**
 *
 * @author vpc
 */
public class DefaultNutsFetchDescriptorRepositoryCommand extends NutsRepositoryCommandBase<NutsFetchDescriptorRepositoryCommand> implements NutsFetchDescriptorRepositoryCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsFetchDescriptorRepositoryCommand.class.getName());

    private NutsId id;
    private NutsDescriptor result;

    public DefaultNutsFetchDescriptorRepositoryCommand(NutsRepository repo) {
        super(repo,"fetch-descriptor");
    }

    @Override
    public boolean configureFirst(NutsCommand cmd) {
        if (super.configureFirst(cmd)) {
            return true;
        }
        return false;
    }

    @Override
    public NutsFetchDescriptorRepositoryCommand run() {
        NutsWorkspaceUtils.checkSession(getRepo().getWorkspace(), getSession());
        getRepo().security().checkAllowed(NutsConstants.Rights.FETCH_DESC, "fetch-descriptor");
        Map<String, String> queryMap = id.getQueryMap();
        queryMap.remove(NutsConstants.QueryKeys.OPTIONAL);
        queryMap.remove(NutsConstants.QueryKeys.SCOPE);
        queryMap.put(NutsConstants.QueryKeys.FACE, NutsConstants.QueryFaces.DESCRIPTOR);
        id = id.setQuery(queryMap);
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        xrepo.checkAllowedFetch(id, getSession());
        long startTime = System.currentTimeMillis();
        try {
            String versionString = id.getVersion().getValue();
            NutsDescriptor d = null;
            if (DefaultNutsVersion.isBlank(versionString)) {
                NutsId a = xrepo.searchLatestVersion(id.setVersion(""), null, getSession());
                if (a == null) {
                    throw new NutsNotFoundException(getRepo().getWorkspace(),id);
                }
                a = a.setFaceDescriptor();
                d = xrepo.fetchDescriptorImpl(a, getSession());
            } else if (DefaultNutsVersion.isStaticVersionPattern(versionString)) {
                id = id.setFaceDescriptor();
                d = xrepo.fetchDescriptorImpl(id, getSession());
            } else {
                NutsIdFilter filter = CoreFilterUtils.idFilterOf(id.getQueryMap(), new NutsPatternIdFilter(id), null);
                NutsId a = xrepo.searchLatestVersion(id.setVersion(""), filter, getSession());
                if (a == null) {
                    throw new NutsNotFoundException(getRepo().getWorkspace(),id);
                }
                a = a.setFaceDescriptor();
                d = xrepo.fetchDescriptorImpl(a, getSession());
            }
            if (d == null) {
                throw new NutsNotFoundException(getRepo().getWorkspace(),id);
            }
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(LOG, getRepo().config().name(), getSession(), id, TraceResult.SUCCESS, "Fetch descriptor", startTime);
            }
            result = d;
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(LOG, getRepo().config().name(), getSession(), id, TraceResult.ERROR, "Fetch descriptor", startTime);
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

    @Override
    public NutsFetchDescriptorRepositoryCommand id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsFetchDescriptorRepositoryCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsId getId() {
        return id;
    }

}
