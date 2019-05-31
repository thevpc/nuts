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

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsContent;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsFetchContentRepositoryCommand;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.TraceResult;

/**
 *
 * @author vpc
 */
public class DefaultNutsFetchContentRepositoryCommand extends NutsRepositoryCommandBase<NutsFetchContentRepositoryCommand> implements NutsFetchContentRepositoryCommand {

    private static final Logger LOG = Logger.getLogger(DefaultNutsFetchContentRepositoryCommand.class.getName());

    private NutsId id;
    private NutsContent result;
    private NutsDescriptor descriptor;
    private Path localPath;
    
    public DefaultNutsFetchContentRepositoryCommand(NutsRepository repo) {
        super(repo,"fetch");
    }

    @Override
    public boolean configureFirst(NutsCommand cmd) {
        if (super.configureFirst(cmd)) {
            return true;
        }
        return false;
    }

    @Override
    public NutsDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public NutsFetchContentRepositoryCommand setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }
    @Override
    public NutsFetchContentRepositoryCommand descriptor(NutsDescriptor descriptor) {
        return setDescriptor(descriptor);
    }

    
    @Override
    public Path getLocalPath() {
        return localPath;
    }

    @Override
    public NutsFetchContentRepositoryCommand localPath(Path localPath) {
        return setLocalPath(localPath);
    }
    
    @Override
    public NutsFetchContentRepositoryCommand setLocalPath(Path localPath) {
        this.localPath = localPath;
        return this;
    }

    @Override
    public NutsFetchContentRepositoryCommand run() {
        NutsWorkspaceUtils.checkSession(getRepo().getWorkspace(), getSession());
        NutsDescriptor descriptor0=descriptor;
        if (descriptor0 == null) {
            descriptor0 = getRepo().fetchDescriptor().id(id).session(getSession()).getResult();
        }
        id = id.setFaceComponent();
        getRepo().security().checkAllowed(NutsConstants.Rights.FETCH_CONTENT, "fetch-content");
        NutsRepositoryExt xrepo = NutsRepositoryExt.of(getRepo());
        xrepo.checkAllowedFetch(id, getSession());
        long startTime = System.currentTimeMillis();
        try {
            NutsContent f = xrepo.fetchContentImpl(id, descriptor0, localPath, getSession());
            if (f == null) {
                throw new NutsNotFoundException(getRepo().getWorkspace(),id);
            }
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(LOG, getRepo().config().name(),getSession(), id, TraceResult.SUCCESS, "Fetch component", startTime);
            }
            result= f;
        } catch (RuntimeException ex) {
            if (LOG.isLoggable(Level.FINEST)) {
                CoreNutsUtils.traceMessage(LOG, getRepo().config().name(),getSession(), id, TraceResult.ERROR, "Fetch component", startTime);
            }
            throw ex;
        }
        return this;
    }

    @Override
    public NutsContent getResult() {
        if (result == null) {
            run();
        }
        return result;
    }

    @Override
    public NutsFetchContentRepositoryCommand id(NutsId id) {
        return setId(id);
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
