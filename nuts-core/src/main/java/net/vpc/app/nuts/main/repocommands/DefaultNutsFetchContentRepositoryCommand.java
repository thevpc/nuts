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
package net.vpc.app.nuts.main.repocommands;

import java.util.logging.Level;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.repocommands.AbstractNutsFetchContentRepositoryCommand;
import net.vpc.app.nuts.core.repos.NutsRepositoryExt;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.common.TraceResult;

/**
 *
 * @author vpc
 */
public class DefaultNutsFetchContentRepositoryCommand extends AbstractNutsFetchContentRepositoryCommand {

    private final NutsLogger LOG;

    public DefaultNutsFetchContentRepositoryCommand(NutsRepository repo) {
        super(repo);
        LOG=repo.workspace().log().of(DefaultNutsFetchContentRepositoryCommand.class);
    }

    @Override
    public NutsFetchContentRepositoryCommand run() {
        NutsWorkspaceUtils.of(getRepo().getWorkspace()).checkSession( getSession());
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
                CoreNutsUtils.traceMessage(LOG,Level.FINER, getRepo().config().name(), getSession(), getFetchMode(), id.getLongNameId(), TraceResult.SUCCESS, "fetch component", startTime,null);
            result = f;
        } catch (RuntimeException ex) {
            if(!CoreNutsUtils.isUnsupportedFetchModeException(ex)) {
                CoreNutsUtils.traceMessage(LOG, Level.FINEST, getRepo().config().name(), getSession(), getFetchMode(), id.getLongNameId(), TraceResult.FAIL, "fetch component", startTime, CoreNutsUtils.resolveMessageToTraceOrNullIfNutsNotFoundException(ex));
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
