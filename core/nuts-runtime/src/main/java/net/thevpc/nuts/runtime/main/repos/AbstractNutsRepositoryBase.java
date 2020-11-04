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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts.runtime.main.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.core.repos.AbstractNutsRepository;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.main.repocommands.*;
import net.thevpc.nuts.runtime.repocommands.AbstractNutsUpdateRepositoryStatisticsCommand;
import net.thevpc.nuts.runtime.security.DefaultNutsRepositorySecurityManager;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.common.TraceResult;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;

import java.util.*;
import java.util.logging.Level;

/**
 * Created by vpc on 1/18/17.
 */
public abstract class AbstractNutsRepositoryBase extends AbstractNutsRepository implements NutsRepositoryExt {

    private static final long serialVersionUID = 1L;
    protected NutsIndexStore nutsIndexStore;

    private final NutsLogger LOG;

    public AbstractNutsRepositoryBase(NutsAddRepositoryOptions options,
                                      NutsWorkspace workspace, NutsRepository parentRepository,
                                      int speed, boolean supportedMirroring, String repositoryType) {
        LOG=workspace.log().of(AbstractNutsRepositoryBase.class);
        init(options, workspace, parentRepository, speed, supportedMirroring, repositoryType);
    }

    @Override
    public NutsIndexStore getIndexStore() {
        return nutsIndexStore;
    }

    protected void init(NutsAddRepositoryOptions options, NutsWorkspace workspace, NutsRepository parent, int speed, boolean supportedMirroring, String repositoryType) {
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
        setEnabled(
                this.workspace.config().options().getExcludedRepositories()==null ||
                Arrays.stream(this.workspace.config().options().getExcludedRepositories())
                .noneMatch(
                        n->optionsConfig.getName().equals(n)
                )
        );
    }

    @Override
    public boolean acceptAction(NutsId id, NutsRepositorySupportedAction supportedAction, NutsFetchMode mode, NutsSession session) {
        String groups = config().getGroups();
        if (CoreStringUtils.isBlank(groups)) {
            return true;
        }
        return id.getGroupId().matches(CoreStringUtils.simpexpToRegexp(groups));
    }

    @Override
    public String toString() {
        NutsRepositoryConfigManager c = config();
        String name = getName();
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
    public void checkAllowedFetch(NutsId id, NutsSession session) {
    }


    @Override
    public NutsFetchDescriptorRepositoryCommand fetchDescriptor() {
        return new DefaultNutsFetchDescriptorRepositoryCommand(this);
    }

    @Override
    public NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
        Iterator<NutsId> allVersions = searchVersions().setId(id).setFilter(filter)
                .setFetchMode(fetchMode)
                .setSession(session).getResult();
        NutsId a = null;
        while (allVersions.hasNext()) {
            NutsId next = allVersions.next();
            if (a == null || next.getVersion().compareTo(a.getVersion()) > 0) {
                a = next;
            }
        }
        return a;
    }

    protected void traceMessage(NutsSession session, NutsFetchMode fetchMode, Level lvl, NutsId id, TraceResult tracePhase, String title, long startTime, String extraMessage) {
        CoreNutsUtils.traceMessage(LOG, lvl,getName(), session, fetchMode, id, tracePhase, title, startTime,extraMessage);
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
        return getWorkspace().locations().getDefaultIdContentExtension(packaging);
    }

    protected String getIdExtension(NutsId id) {
        return getWorkspace().locations().getDefaultIdExtension(id);
    }

    @Override
    public String getIdBasedir(NutsId id) {
        return getWorkspace().locations().getDefaultIdBasedir(id);
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
