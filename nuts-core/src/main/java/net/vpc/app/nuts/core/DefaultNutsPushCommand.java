/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import net.vpc.app.nuts.NutsRepositorySupportedAction;
import net.vpc.app.nuts.NutsResultFormatType;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

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
    private final List<NutsId> ids = new ArrayList<>();
    private List<NutsId> frozenIds;
    private NutsSession session;
    private final NutsWorkspace ws;
    private String repository;
    private NutsResultFormatType formatType = NutsResultFormatType.PLAIN;

    public DefaultNutsPushCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsPushCommand id(String id) {
        return addId(id);
    }

    @Override
    public NutsPushCommand id(NutsId id) {
        return addId(id);
    }

    @Override
    public NutsPushCommand addId(String id) {
        return addId(id == null ? null : ws.parser().parseRequiredId(id));
    }

    @Override
    public NutsPushCommand addFrozenId(String id) {
        return addFrozenId(id == null ? null : ws.parser().parseRequiredId(id));
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
    public NutsPushCommand removeId(NutsId id) {
        if (id != null) {
            ids.remove(id);
        }
        return this;
    }

    @Override
    public NutsPushCommand removeId(String id) {
        if (id != null) {
            ids.remove(ws.parser().parseId(id));
        }
        return this;
    }

    @Override
    public NutsPushCommand removeFrozenId(NutsId id) {
        if (id != null) {
            if (frozenIds != null) {
                frozenIds.remove(id);
            }
        }
        return this;
    }

    @Override
    public NutsPushCommand removeFrozenId(String id) {
        if (id != null) {
            if (frozenIds != null) {
                frozenIds.remove(ws.parser().parseId(id));
            }
        }
        return this;
    }

    @Override
    public NutsPushCommand addFrozenId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(id);
        } else {
            if (frozenIds == null) {
                frozenIds = new ArrayList<>();
            }
            frozenIds.add(id);
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
    public NutsPushCommand addFrozenIds(String... ids) {
        for (String id : ids) {
            addFrozenId(id);
        }
        return this;
    }

    @Override
    public NutsPushCommand addFrozenIds(NutsId... ids) {
        for (NutsId id : ids) {
            addFrozenId(id);
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
    public NutsPushCommand addArgs(Collection<String> args) {
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
    public NutsPushCommand push() {
        NutsSession session = NutsWorkspaceUtils.validateSession(ws, this.getSession());
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
                boolean ok = false;
                for (NutsRepository repo : NutsWorkspaceUtils.filterRepositories(ws,NutsRepositorySupportedAction.DEPLOY, file.getId(), repositoryFilter, NutsFetchMode.LOCAL, fetchOptions)) {
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
                            ok = true;
                            break;
                        } catch (Exception e) {
                            errors.add(CoreStringUtils.exceptionToString(e));
                            //
                        }
                    }
                }
                if (!ok) {
                    throw new NutsRepositoryNotFoundException(this.getRepository() + " : " + CoreStringUtils.join("\n", errors));
                }
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
                        .setOffline(this.isOffline())
                        .setTransitive(true);
                repo.deploy(dep, rsession);
            }
        }
        return this;
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

    @Override
    public NutsPushCommand frozenId(NutsId id) {
        return addFrozenId(id);
    }

    @Override
    public NutsPushCommand frozenId(String id) {
        return addFrozenId(id);
    }

    @Override
    public NutsPushCommand frozenIds(NutsId... ids) {
        return addFrozenIds(ids);
    }

    @Override
    public NutsPushCommand frozenIds(String... ids) {
        return addFrozenIds(ids);
    }

    @Override
    public NutsPushCommand arg(String arg) {
        return addArg(arg);
    }

    @Override
    public NutsPushCommand args(String... args) {
        return addArgs(args);
    }

    @Override
    public NutsPushCommand args(Collection<String> args) {
        return addArgs(args);
    }

    @Override
    public NutsPushCommand clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public NutsPushCommand clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NutsPushCommand clearFrozenIds() {
        frozenIds = null;
        return this;
    }

    @Override
    public NutsPushCommand session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsPushCommand ask() {
        return ask(true);
    }

    @Override
    public NutsPushCommand ask(boolean enable) {
        return setAsk(enable);
    }

    @Override
    public NutsPushCommand force() {
        return force(true);
    }

    @Override
    public NutsPushCommand force(boolean enable) {
        return setForce(enable);
    }

    @Override
    public NutsPushCommand trace() {
        return trace(true);
    }

    @Override
    public NutsPushCommand trace(boolean enable) {
        return setTrace(enable);
    }

    @Override
    public NutsPushCommand offline() {
        return offline(true);
    }

    @Override
    public NutsPushCommand offline(boolean enable) {
        return setOffline(enable);
    }

     @Override
    public NutsPushCommand formatType(NutsResultFormatType formatType) {
        return setFormatType(formatType);
    }

    @Override
    public NutsPushCommand setFormatType(NutsResultFormatType formatType) {
        if(formatType==null){
            formatType=NutsResultFormatType.PLAIN;
        }
        this.formatType=formatType;
        return this;
    }

    @Override
    public NutsPushCommand json() {
        return setFormatType(NutsResultFormatType.JSON);
    }

    @Override
    public NutsPushCommand plain() {
        return setFormatType(NutsResultFormatType.PLAIN);
    }

    @Override
    public NutsPushCommand props() {
        return setFormatType(NutsResultFormatType.PROPS);
    }

    @Override
    public NutsResultFormatType getFormatType() {
        return this.formatType;
    }
}
