/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsFetchCommand;
import net.vpc.app.nuts.NutsFetchMode;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsPushCommand;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryDeploymentOptions;
import net.vpc.app.nuts.NutsRepositoryFilter;
import net.vpc.app.nuts.NutsRepositoryNotFoundException;
import net.vpc.app.nuts.NutsRepositorySession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;

/**
 *
 * @author vpc
 */
public class DefaultNutsPushCommand implements NutsPushCommand {

    private boolean ask = true;
    private boolean trace = true;
    private boolean force = false;
    private boolean offline = false;
    private List<String> args;
    private List<NutsId> ids = new ArrayList<>();
    private List<NutsId> frozenIds = new ArrayList<>();
    private NutsSession session;
    private NutsWorkspace ws;
    private String repository;

    public DefaultNutsPushCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsPushCommand id(String id) {
        return setId(id);
    }

    @Override
    public NutsPushCommand id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsPushCommand setId(String id) {
        return setId(id == null ? null : ws.parser().parseId(id));
    }

    @Override
    public NutsPushCommand setId(NutsId id) {
        if (id == null) {
            ids.clear();
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NutsPushCommand addId(String id) {
        return addId(id == null ? null : ws.parser().parseId(id));
    }

    @Override
    public NutsPushCommand addId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(id);
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NutsPushCommand ids(String... ids) {
        return addIds(ids);
    }

    @Override
    public NutsPushCommand ids(NutsId... ids) {
        return addIds(ids);
    }

    @Override
    public NutsPushCommand addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NutsPushCommand addIds(NutsId... ids) {
        for (NutsId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public boolean isTrace() {
        return trace;
    }

    @Override
    public NutsPushCommand setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public boolean isForce() {
        return force;
    }

    @Override
    public NutsPushCommand setForce(boolean forceInstall) {
        this.force = forceInstall;
        return this;
    }

    @Override
    public boolean isAsk() {
        return ask;
    }

    @Override
    public NutsPushCommand setAsk(boolean ask) {
        this.ask = ask;
        return this;
    }

    @Override
    public String[] getArgs() {
        return args == null ? new String[0] : args.toArray(new String[0]);
    }

    @Override
    public NutsPushCommand setArgs(String... args) {
        return setArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NutsPushCommand setArgs(List<String> args) {
        this.args = new ArrayList<>();
        if (args != null) {
            for (String arg : args) {
                if (arg == null) {
                    throw new NullPointerException();
                }
                this.args.add(arg);
            }
        }
        return this;
    }

    @Override
    public NutsPushCommand addArg(String arg) {
        if (this.args == null) {
            this.args = new ArrayList<>();
        }
        if (arg == null) {
            throw new NullPointerException();
        }
        this.args.add(arg);
        return this;
    }

    @Override
    public NutsPushCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NutsPushCommand addArgs(List<String> args) {
        if (this.args == null) {
            this.args = new ArrayList<>();
        }
        if (args != null) {
            for (String arg : args) {
                if (arg == null) {
                    throw new NullPointerException();
                }
                this.args.add(arg);
            }
        }
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsPushCommand setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsId[] getIds() {
        return ids == null ? new NutsId[0] : ids.toArray(new NutsId[0]);
    }

    @Override
    public NutsId[] getFrozenIds() {
        return frozenIds == null ? new NutsId[0] : frozenIds.toArray(new NutsId[0]);
    }

    @Override
    public void push() {
        NutsSession session = CoreNutsUtils.validateSession(this.getSession(), ws);
        NutsRepositoryFilter repositoryFilter = null;
        Map<NutsId, NutsDefinition> toProcess = new LinkedHashMap<>();
        for (NutsId id : this.getIds()) {
            if (CoreStringUtils.trim(id.getVersion().getValue()).endsWith(NutsConstants.Versions.CHECKED_OUT_EXTENSION)) {
                throw new NutsIllegalArgumentException("Invalid Version " + id.getVersion());
            }
            NutsDefinition file = ws.fetch().id(id).setSession(session).setTransitive(false).getResultDefinition();
            if (file == null) {
                throw new NutsIllegalArgumentException("Nothing to push");
            }
            toProcess.put(id, file);
        }
        NutsFetchCommand fetchOptions = ws.fetch().setTransitive(true).setSession(session);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        for (Map.Entry<NutsId, NutsDefinition> entry : toProcess.entrySet()) {
            NutsId id = entry.getKey();
            NutsDefinition file = entry.getValue();
            if (CoreStringUtils.isBlank(this.getRepository())) {
                Set<String> errors = new LinkedHashSet<>();
                //TODO : CHEK ME, why offline?
                for (NutsRepository repo : dws.getEnabledRepositories(NutsWorkspaceHelper.FilterMode.DEPLOY, file.getId(), repositoryFilter, session, NutsFetchMode.LOCAL, fetchOptions)) {
                    NutsDescriptor descr = null;
                    NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, repo, this.isOffline() ? NutsFetchMode.LOCAL : NutsFetchMode.REMOTE, fetchOptions);
                    try {
                        descr = repo.fetchDescriptor(file.getId(), rsession);
                    } catch (Exception e) {
                        errors.add(CoreStringUtils.exceptionToString(e));
                        //
                    }
                    if (descr != null && repo.config().isSupportedMirroring()) {
                        NutsId id2 = ws.config().createComponentFaceId(dws.resolveEffectiveId(descr,
                                ws.fetch().setTransitive(true).session(session)), descr);
                        try {
                            repo.push(id2, this, rsession);
                            return;
                        } catch (Exception e) {
                            errors.add(CoreStringUtils.exceptionToString(e));
                            //
                        }
                    }
                }
                throw new NutsRepositoryNotFoundException(this.getRepository() + " : " + CoreStringUtils.join("\n", errors));
            } else {
                NutsRepository repo = ws.config().getRepository(this.getRepository());
                NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, repo, this.isOffline() ? NutsFetchMode.LOCAL : NutsFetchMode.REMOTE,
                        fetchOptions
                );

                if (!repo.config().isEnabled()) {
                    throw new NutsIllegalArgumentException("Repository " + repo.config().getName() + " is disabled");
                }
                NutsId effId = ws.config().createComponentFaceId(id.unsetQuery(), file.getDescriptor()).setAlternative(CoreStringUtils.trim(file.getDescriptor().getAlternative()));
                NutsRepositoryDeploymentOptions dep = new DefaultNutsRepositoryDeploymentOptions()
                        .setId(effId)
                        .setContent(file.getPath())
                        .setDescriptor(file.getDescriptor())
                        .setRepository(repo.config().getName())
                        .setTrace(this.isTrace())
                        .setForce(this.isForce())
                        .setTransitive(true)
                        .setOffline(this.isOffline());
                repo.deploy(dep, rsession);
            }
        }

    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NutsPushCommand setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NutsPushCommand setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public NutsPushCommand repository(String repository) {
        return setRepository(repository);
    }

}
