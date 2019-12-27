/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.main.repos;

import net.vpc.app.nuts.core.repos.AbstractNutsRepository;
import net.vpc.app.nuts.core.repos.NutsRepositoryExt;
import net.vpc.app.nuts.main.repocommands.*;
import net.vpc.app.nuts.runtime.repocommands.AbstractNutsUpdateRepositoryStatisticsCommand;
import net.vpc.app.nuts.runtime.util.common.TraceResult;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.*;

import java.util.*;
import java.util.logging.Level;

import net.vpc.app.nuts.runtime.security.DefaultNutsRepositorySecurityManager;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;

/**
 * Created by vpc on 1/18/17.
 */
public abstract class AbstractNutsRepositoryBase extends AbstractNutsRepository implements NutsRepositoryExt {

    private static final long serialVersionUID = 1L;
    protected NutsIndexStore nutsIndexStore;

    private final NutsLogger LOG;

    public AbstractNutsRepositoryBase(NutsCreateRepositoryOptions options,
                                      NutsWorkspace workspace, NutsRepository parentRepository,
                                      int speed, boolean supportedMirroring, String repositoryType) {
        LOG=workspace.log().of(AbstractNutsRepositoryBase.class);
        init(options, workspace, parentRepository, speed, supportedMirroring, repositoryType);
    }

    @Override
    public NutsIndexStore getIndexStore() {
        return nutsIndexStore;
    }

    protected void init(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parent, int speed, boolean supportedMirroring, String repositoryType) {
        NutsRepositoryConfig optionsConfig = options.getConfig();
        if (optionsConfig == null) {
            throw new NutsIllegalArgumentException(workspace, "Null Config");
        }
        this.workspace = workspace;
        this.parentRepository = parent;
        securityManager = new DefaultNutsRepositorySecurityManager(this);
        if(options.getSession()==null){
            options.setSession(workspace.createSession());
        }
        configManager = new DefaultNutsRepoConfigManager(
                this, options.getSession(), options.getLocation(), optionsConfig,
                Math.max(0, speed), options.getDeployOrder(),
                options.isTemporary(), options.isEnabled(),
                optionsConfig.getName(), supportedMirroring,
                options.getName(), repositoryType
        );
        this.nutsIndexStore = workspace.config().getIndexStoreClientFactory().createIndexStore(this);
    }

    @Override
    public boolean acceptAction(NutsId id, NutsRepositorySupportedAction supportedAction, NutsFetchMode mode) {
        String groups = config().getGroups();
        if (CoreStringUtils.isBlank(groups)) {
            return true;
        }
        return id.getGroupId().matches(CoreStringUtils.simpexpToRegexp(groups));
    }

    @Override
    public String toString() {
        NutsRepositoryConfigManager c = config();
        String name = config().getName();
        String storePath = null;
        String loc = config().getLocation(false);
        String impl = getClass().getSimpleName();
        if (c != null) {
            storePath = c.getStoreLocation().toAbsolutePath().toString();
        }
        LinkedHashMap<String, String> a = new LinkedHashMap<>();
        if (name != null) {
            a.put("name", name);
        }
        a.put("impl", impl);
        if (storePath != null) {
            a.put("store", storePath);
        }
        if (loc != null) {
            a.put("location", loc);
        }
        return a.toString();
    }

    @Override
    public void checkAllowedFetch(NutsId id, NutsRepositorySession session) {
    }


    @Override
    public NutsFetchDescriptorRepositoryCommand fetchDescriptor() {
        return new DefaultNutsFetchDescriptorRepositoryCommand(this);
    }

    @Override
    public NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        Iterator<NutsId> allVersions = searchVersions().setId(id).setFilter(filter).setSession(session).run().getResult();
        NutsId a = null;
        while (allVersions.hasNext()) {
            NutsId next = allVersions.next();
            if (a == null || next.getVersion().compareTo(a.getVersion()) > 0) {
                a = next;
            }
        }
        return a;
    }

    protected void traceMessage(NutsRepositorySession session, Level lvl, NutsId id, TraceResult tracePhase, String title, long startTime,String extraMessage) {
        CoreNutsUtils.traceMessage(LOG, lvl,config().name(), session, id, tracePhase, title, startTime,extraMessage);
    }

    @Override
    public NutsDeployRepositoryCommand deploy() {
        return new DefaultNutsDeployRepositoryCommand(this);
    }

    @Override
    public NutsPushRepositoryCommand push() {
        return new DefaultNutsPushRepositoryCommand(this);
    }

    @Override
    public NutsSearchRepositoryCommand search() {
        return new DefaultNutsSearchRepositoryCommand(this);
    }

    @Override
    public NutsFetchContentRepositoryCommand fetchContent() {
        return new DefaultNutsFetchContentRepositoryCommand(this);
    }

    @Override
    public NutsSearchVersionsRepositoryCommand searchVersions() {
        return new DefaultNutsSearchVersionsRepositoryCommand(this);
    }

    @Override
    public NutsRepositoryUndeployCommand undeploy() {
        return new DefaultNutsRepositoryUndeployCommand(this);
    }

    protected String getIdComponentExtension(String packaging) {
        return getWorkspace().config().getDefaultIdContentExtension(packaging);
    }

    protected String getIdExtension(NutsId id) {
        return getWorkspace().config().getDefaultIdExtension(id);
    }

    @Override
    public String getIdBasedir(NutsId id) {
        return getWorkspace().config().getDefaultIdBasedir(id);
    }



    protected String getIdRemotePath(NutsId id) {
        return CoreIOUtils.buildUrl(config().getLocation(true), getIdRelativePath(id));
    }

    protected String getIdRelativePath(NutsId id) {
        return getIdBasedir(id) + "/" + getIdFilename(id);
    }

    @Override
    public NutsUpdateRepositoryStatisticsCommand updateStatistics() {
        return new AbstractNutsUpdateRepositoryStatisticsCommand(this) {
            @Override
            public NutsUpdateRepositoryStatisticsCommand run() {
                return this;
            }
        };
    }

}
