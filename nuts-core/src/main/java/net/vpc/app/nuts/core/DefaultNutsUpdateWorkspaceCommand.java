/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.vpc.app.nuts.NutsBootConfig;
import net.vpc.app.nuts.NutsBootContext;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDependency;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsDescriptorFilter;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsUnexpectedException;
import net.vpc.app.nuts.NutsUpdateResult;
import net.vpc.app.nuts.NutsWorkspaceUpdateResult;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsDependencyScope;
import net.vpc.app.nuts.NutsResultFormatType;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.app.nuts.NutsUpdateCommand;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsUpdateWorkspaceCommand implements NutsUpdateCommand {

    public static final Logger log = Logger.getLogger(DefaultNutsUpdateWorkspaceCommand.class.getName());

    private boolean ask = true;
    private boolean trace = true;
    private boolean force = false;
    private boolean enableInstall = true;
    private boolean updateApi = false;
    private boolean updateRuntime = false;
    private boolean updateExtensions = false;
    private boolean updateInstalled = false;
    private boolean includeOptional = false;
    private NutsResultFormatType formatType = NutsResultFormatType.PLAIN;
    private String forceBootAPIVersion;
    private List<String> args;
    private List<NutsDependencyScope> scopes = new ArrayList<>();
    private List<NutsId> frozenIds = new ArrayList<>();
    private NutsSession session;
    private NutsWorkspace ws;
    private List<NutsId> ids = new ArrayList<>();

    private NutsWorkspaceUpdateResult result;

    public DefaultNutsUpdateWorkspaceCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsId[] getIds() {
        return ids == null ? new NutsId[0] : ids.toArray(new NutsId[0]);
    }

    @Override
    public NutsUpdateCommand id(String id) {
        return addId(id);
    }

    @Override
    public NutsUpdateCommand id(NutsId id) {
        return addId(id);
    }

    @Override
    public NutsUpdateCommand addId(String id) {
        return addId(id == null ? null : ws.parser().parseId(id));
    }

    @Override
    public NutsUpdateCommand addId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(id);
        } else {
            ids.add(id);
        }
        return this;
    }

    @Override
    public NutsUpdateCommand ids(String... ids) {
        return addIds(ids);
    }

    @Override
    public NutsUpdateCommand ids(NutsId... ids) {
        return addIds(ids);
    }

    @Override
    public NutsUpdateCommand addIds(String... ids) {
        for (String id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NutsUpdateCommand addIds(NutsId... ids) {
        for (NutsId id : ids) {
            addId(id);
        }
        return this;
    }

    @Override
    public NutsUpdateCommand removeId(NutsId id) {
        if (id != null) {
            this.ids.remove(id);
        }
        return this;
    }

    @Override
    public NutsUpdateCommand removeId(String id) {
        return removeId(ws.parser().parseId(id));
    }

    @Override
    public NutsUpdateCommand scope(NutsDependencyScope scope) {
        return addScope(scope);
    }

    @Override
    public NutsUpdateCommand addScope(NutsDependencyScope scope) {
        if (scope != null) {
            scopes.add(scope);
        }
        return this;
    }

    @Override
    public NutsUpdateCommand scopes(NutsDependencyScope... scopes) {
        return addScopes(scopes);
    }

    @Override
    public NutsUpdateCommand scopes(Collection<NutsDependencyScope> scopes) {
        return addScopes(scopes);
    }

    @Override
    public NutsUpdateCommand addScopes(NutsDependencyScope... scopes) {
        if (scopes != null) {
            for (NutsDependencyScope s : scopes) {
                addScope(s);
            }
        }
        return this;
    }

    @Override
    public NutsUpdateCommand addScopes(Collection<NutsDependencyScope> scopes) {
        if (scopes != null) {
            for (NutsDependencyScope s : scopes) {
                addScope(s);
            }
        }
        return this;
    }

    @Override
    public boolean isTrace() {
        return trace;
    }

    @Override
    public NutsUpdateCommand setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public boolean isIncludeOptional() {
        return includeOptional;
    }

    @Override
    public NutsUpdateCommand setIncludeOptional(boolean includeOptional) {
        this.includeOptional = includeOptional;
        return this;
    }

    @Override
    public boolean isForce() {
        return force;
    }

    @Override
    public NutsUpdateCommand setForce(boolean forceInstall) {
        this.force = forceInstall;
        return this;
    }

    @Override
    public boolean isAsk() {
        return ask;
    }

    @Override
    public NutsUpdateCommand setAsk(boolean ask) {
        this.ask = ask;
        return this;
    }

    @Override
    public String[] getArgs() {
        return args == null ? new String[0] : args.toArray(new String[0]);
    }

    @Override
    public NutsUpdateCommand addArg(String arg) {
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
    public NutsUpdateCommand clearArgs() {
        this.args = null;
        return this;
    }

    @Override
    public NutsUpdateCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NutsUpdateCommand addArgs(Collection<String> args) {
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
    public NutsUpdateCommand setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsId[] getFrozenIds() {
        return frozenIds == null ? new NutsId[0] : frozenIds.toArray(new NutsId[0]);
    }

    @Override
    public boolean isEnableInstall() {
        return enableInstall;
    }

    @Override
    public NutsUpdateCommand enableInstall() {
        return enableInstall(true);
    }

    @Override
    public NutsUpdateCommand enableInstall(boolean enableInstall) {
        return setEnableInstall(enableInstall);
    }

    @Override
    public NutsUpdateCommand setEnableInstall(boolean enableInstall) {
        this.enableInstall = enableInstall;
        return this;
    }

    @Override
    public boolean isUpdateApi() {
        if (updateApi || isUpdateNone()) {
            return true;
        }

        for (NutsId id : ids) {
            if (id.getSimpleName().equals(NutsConstants.Ids.NUTS_API)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isUpdateInstalled() {
        return updateInstalled;
    }

    @Override
    public boolean isUpdateRuntime() {
        if (isUpdateApi()) {
            return true;
        }
        if (updateRuntime) {
            return true;
        }
        for (NutsId id : ids) {
            if (id.getSimpleName().equals(ws.config().getRuntimeId().getSimpleName())) {
                return true;
            }

        }
        return false;
    }

    @Override
    public NutsUpdateCommand setUpdateApi(boolean enableMajorUpdates) {
        this.updateApi = enableMajorUpdates;
        return this;
    }

    @Override
    public boolean isUpdateExtensions() {
        if (isUpdateApi()) {
            return true;
        }
        return updateExtensions;
    }

    @Override
    public NutsUpdateCommand setUpdateExtensions(boolean updateExtensions) {
        this.updateExtensions = updateExtensions;
        return this;
    }

    @Override
    public String getApiVersion() {
        return forceBootAPIVersion;
    }

    @Override
    public NutsUpdateCommand setApiVersion(String forceBootAPIVersion) {
        this.forceBootAPIVersion = forceBootAPIVersion;
        return this;
    }

    @Override
    public int getUpdateResultCount() {
        return getUpdateResult().getUpdatesCount();
    }

    @Override
    public NutsWorkspaceUpdateResult getUpdateResult() {
        if (result == null) {
            checkUpdates();
        }
        if (result == null) {
            throw new NutsUnexpectedException();
        }
        return result;
    }

    @Override
    public NutsUpdateCommand update() {
        applyResult(getUpdateResult());
        return this;
    }

    @Override
    public NutsUpdateCommand checkUpdates(boolean applyUpdates) {
        checkUpdates();
        if (applyUpdates) {
            update();
        }
        return this;
    }

    private Set<NutsId> getExtensionsToUpdate() {
        Set<NutsId> ext = new HashSet<>();
        for (NutsId extension : ws.extensions().getExtensions()) {
            ext.add(extension.getSimpleNameId());
        }
        if (updateExtensions) {
            return ext;
        } else {
            Set<NutsId> ext2 = new HashSet<>();
            for (NutsId id : ids) {
                if (id.getSimpleName().equals(NutsConstants.Ids.NUTS_API)) {
                    continue;
                }
                if (id.getSimpleName().equals(ws.config().getRuntimeId().getSimpleName())) {
                    continue;
                }
                if (ext.contains(id.getSimpleNameId())) {
                    ext2.add(id.getSimpleNameId());
                }

            }
            return ext2;
        }
    }

    private Set<NutsId> getRegularIds() {
        HashSet<String> extensions = new HashSet<>();
        for (NutsId object : ws.extensions().getExtensions()) {
            extensions.add(object.getSimpleName());
        }

        HashSet<NutsId> baseRegulars = new HashSet<>(ids);
        if (isUpdateInstalled()) {
            baseRegulars.addAll(ws.find().installed().getResultIds().stream().map(x -> x.getSimpleNameId()).collect(Collectors.toList()));
        }
        HashSet<NutsId> regulars = new HashSet<>();
        for (NutsId id : baseRegulars) {
            if (id.getSimpleName().equals(NutsConstants.Ids.NUTS_API)) {
                continue;
            }
            if (id.getSimpleName().equals(ws.config().getRuntimeId().getSimpleName())) {
                continue;
            }
            if (extensions.contains(id.getSimpleName())) {
                continue;
            }
            regulars.add(id);
        }
        return regulars;
    }

    private boolean isUpdateNone() {
        return !updateApi && !updateRuntime && !updateExtensions && !updateInstalled && ids.isEmpty();
    }

    @Override
    public NutsUpdateCommand checkUpdates() {
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsBootContext actualBootConfig = ws.config().getRunningContext();
//        NutsBootContext jsonBootConfig = getConfigManager().getBootContext();
        NutsSession session = NutsWorkspaceUtils.validateSession(ws, this.getSession());
        Map<String, NutsUpdateResult> allUpdates = new LinkedHashMap<>();
        Map<String, NutsUpdateResult> extUpdates = new LinkedHashMap<>();
        NutsUpdateResult apiUpdate = null;
        String bootVersion0 = ws.config().getRunningContext().getApiId().getVersion().getValue();
        String bootVersion = bootVersion0;
        if (!CoreStringUtils.isBlank(this.getApiVersion())) {
            bootVersion = this.getApiVersion();
        }
        if (this.isUpdateApi() || !CoreStringUtils.isBlank(this.getApiVersion())) {
            apiUpdate = checkUpdates(ws.parser().parseId(NutsConstants.Ids.NUTS_API), this.getApiVersion(), session);
            if (apiUpdate != null) {
                bootVersion = apiUpdate.getAvailable().getId().getVersion().getValue();
                allUpdates.put(NutsConstants.Ids.NUTS_API, apiUpdate);
            } else {
                //reset bootVersion
                bootVersion = bootVersion0;
            }
        }
        NutsUpdateResult runtimeUpdate = null;
        if (this.isUpdateRuntime()) {
            if (dws.requiresCoreExtension()) {
                runtimeUpdate = checkUpdates(ws.parser().parseId(actualBootConfig.getRuntimeId().getSimpleName()),
                        apiUpdate != null && apiUpdate.getAvailable().getId() != null ? apiUpdate.getAvailable().getId().toString()
                        : bootVersion, session);
                if (runtimeUpdate != null) {
                    allUpdates.put(runtimeUpdate.getAvailable().getId().getSimpleName(), runtimeUpdate);
                }
            }
        }
        for (NutsId ext : getExtensionsToUpdate()) {
            NutsUpdateResult extUpdate = checkUpdates(ext, bootVersion, session);
            if (extUpdate != null) {
                allUpdates.put(extUpdate.getAvailable().getId().getSimpleName(), extUpdate);
                extUpdates.put(extUpdate.getAvailable().getId().getSimpleName(), extUpdate);
            }
        }

        Map<String, NutsUpdateResult> regularUpdates = new HashMap<>();
        for (NutsId id : this.getRegularIds()) {
            NutsUpdateResult updated = checkRegularUpdate(id);
            regularUpdates.put(updated.getId().getSimpleName(), updated);
        }
        NutsId[] frozenIds = this.getFrozenIds();
        if (frozenIds.length > 0) {
            for (NutsId d : new HashSet<>(Arrays.asList(frozenIds))) {
                NutsDependency dd = CoreNutsUtils.parseNutsDependency(d.toString());
                if (regularUpdates.containsKey(dd.getSimpleName())) {
                    NutsUpdateResult updated = regularUpdates.get(dd.getSimpleName());
                    //FIX ME
                    if (!dd.getVersion().toFilter().accept(updated.getId().getVersion())) {
                        throw new NutsIllegalArgumentException(dd + " unsatisfied  : " + updated.getId().getVersion());
                    }
                }
            }
        }

        NutsUpdateResult[] updates = allUpdates.values().toArray(new NutsUpdateResult[0]);
        PrintStream out = CoreIOUtils.resolveOut(ws, session);
        if (this.isTrace()) {
            if (updates.length == 0) {
                out.printf("Workspace is [[up-to-date]]. You are running latest version ==%s==\n", actualBootConfig.getRuntimeId().getVersion());
            } else {
                out.printf("Workspace has ##%s## component%s to update.\n", updates.length, (updates.length > 1 ? "s" : ""));
                int widthCol1 = 2;
                int widthCol2 = 2;
                for (NutsUpdateResult update : updates) {
                    widthCol1 = Math.max(widthCol1, update.getAvailable().getId().getSimpleName().length());
                    widthCol2 = Math.max(widthCol2, update.getLocal().getId().getVersion().toString().length());
                }
                for (NutsUpdateResult update : updates) {
                    out.printf("((%s))  : %s => [[%s]]\n",
                            CoreStringUtils.alignLeft(update.getLocal().getId().getVersion().toString(), widthCol2),
                            CoreStringUtils.alignLeft(update.getAvailable().getId().getSimpleName(), widthCol1),
                            update.getAvailable().getId().getVersion().toString());
                }
            }
        }
        result = new NutsWorkspaceUpdateResult(apiUpdate, runtimeUpdate, extUpdates.values().toArray(new NutsUpdateResult[0]),
                regularUpdates.values().toArray(new NutsUpdateResult[0])
        );
        return this;
    }

    protected NutsUpdateResult checkRegularUpdate(NutsId id) {
        NutsSession session = NutsWorkspaceUtils.validateSession(ws, this.getSession());
        NutsVersion version = id.getVersion();
        if (version.isSingleValue()) {
            throw new NutsIllegalArgumentException("Version is too restrictive. You would use fetch or install instead");
        }

        DefaultNutsUpdateResult r = new DefaultNutsUpdateResult();
        r.setId(id.getSimpleNameId());

        final PrintStream out = CoreIOUtils.resolveOut(ws, session);
        NutsDefinition d0 = ws.fetch().id(id).setSession(session).offline().setAcceptOptional(false).setLenient(true).getResultDefinition();
        NutsDefinition d1 = ws.fetch().id(id).setSession(session).setAcceptOptional(false).includeDependencies().setLenient(true).getResultDefinition();
        r.setLocal(d0);
        r.setAvailable(d1);
        final String simpleName = d0 != null ? d0.getId().getSimpleName() : d1 != null ? d1.getId().getSimpleName() : id.getSimpleName();
        if (d0 == null) {
            if (!this.isEnableInstall()) {
                throw new NutsIllegalArgumentException("No version is installed to be updated for " + id);
            }
            if (d1 == null) {
                throw new NutsNotFoundException(id);
            }
            r.setUpdateAvailable(true);
            r.setUpdateForced(false);
            if (this.isTrace()) {
                out.printf("==%s== is [[not-installed]] . New version is available ==%s==\n", simpleName, d1.getId().getVersion());
            }
        } else if (d1 == null) {
            //this is very interisting. Why the hell is this happening?
            r.setAvailable(d0);
            if (this.isTrace()) {
                out.printf("==%s== is [[up-to-date]]. You are running latest version ==%s==\n", d0.getId().getSimpleName(), d0.getId().getVersion());
            }
        } else {
            NutsVersion v0 = d0.getId().getVersion();
            NutsVersion v1 = d1.getId().getVersion();
            if (v1.compareTo(v0) <= 0) {
                //no update needed!
                if (this.isForce()) {
                    r.setUpdateForced(true);
                    if (this.isTrace()) {
                        out.printf("==%s== would be [[forced]] from ==%s== to older version ==%s==\n", simpleName, d0.getId().getVersion(), d1.getId().getVersion());
                    }
                } else {
                    if (this.isTrace()) {
                        out.printf("==%s== is [[up-to-date]]. You are running latest version ==%s==\n", simpleName, d0.getId().getVersion());
                    }
                }
            } else {
                r.setUpdateAvailable(true);
                if (this.isTrace()) {
                    out.printf("==%s== is [[updatable]] from ==%s== to latest version ==%s==\n", simpleName, d0.getId().getVersion(), d1.getId().getVersion());
                }
            }
        }

        return r;
    }

    private void applyResult(NutsWorkspaceUpdateResult result) {
        NutsUpdateResult apiUpdate = result.getApi();
        NutsUpdateResult runtimeUpdate = result.getRuntime();
        if (result.getUpdatesCount() == 0) {
            return;
        }
        NutsBootContext actualBootConfig = ws.config().getRunningContext();
        Path bootstrapFolder = ws.config().getWorkspaceLocation().resolve(NutsConstants.Folders.BOOT);
        if (apiUpdate != null && !apiUpdate.isUpdateApplied()) {
            if (apiUpdate.getAvailable() != null) {
                NutsWorkspaceUtils.checkReadOnly(ws);
                NutsBootConfig bc = ws.config().getBootConfig();
                bc.setApiVersion(apiUpdate.getAvailable().getId().getVersion().toString());
                ws.config().setBootConfig(bc);
                ws.io().copy().from(apiUpdate.getAvailable().getPath()).to(ws.config().getStoreLocation(apiUpdate.getAvailable().getId(), bootstrapFolder)
                        .resolve(ws.config().getDefaultIdFilename(apiUpdate.getAvailable().getId().setFaceComponent().setPackaging("jar")))
                ).run();
                ws.formatter().createDescriptorFormat().pretty().print(ws.fetch().id(apiUpdate.getAvailable().getId()).getResultDescriptor(),
                        ws.config().getStoreLocation(apiUpdate.getAvailable().getId(), bootstrapFolder)
                                .resolve(ws.config().getDefaultIdFilename(apiUpdate.getAvailable().getId().setFaceDescriptor()))
                );
            }
            ((DefaultNutsUpdateResult) apiUpdate).setUpdateApplied(true);
        }
        if (runtimeUpdate != null && !runtimeUpdate.isUpdateApplied()) {
            NutsBootConfig bc = ws.config().getBootConfig();
            bc.setRuntimeId(runtimeUpdate.getAvailable().getId().toString());
            StringBuilder sb = new StringBuilder();
            for (NutsId dependency : runtimeUpdate.getDependencies()) {
                if (sb.length() > 0) {
                    sb.append(";");
                }
                sb.append(dependency.setNamespace(null).toString());
            }
            bc.setRuntimeDependencies(sb.toString());
            NutsWorkspaceUtils.checkReadOnly(ws);
            ws.config().setBootConfig(bc);
            ws.io().copy().from(runtimeUpdate.getAvailable().getPath())
                    .to(ws.config().getStoreLocation(runtimeUpdate.getAvailable().getId(), bootstrapFolder)
                            .resolve(ws.config().getDefaultIdFilename(runtimeUpdate.getAvailable().getId().setFaceComponent().setPackaging("jar")))
                    ).run();
            NutsDescriptor runtimeDesc = ws.fetch().id(runtimeUpdate.getAvailable().getId()).getResultDescriptor();
            ws.formatter().createDescriptorFormat().pretty().print(runtimeDesc,
                    ws.config().getStoreLocation(runtimeUpdate.getAvailable().getId(), bootstrapFolder)
                            .resolve(ws.config().getDefaultIdFilename(runtimeUpdate.getAvailable().getId().setFaceDescriptor()))
            );
            for (NutsDependency dependency : runtimeDesc.getDependencies()) {
                if (dependency.getId().getSimpleNameId().equals(actualBootConfig.getApiId().getSimpleNameId())) {
                    Properties pr = new Properties();
                    pr.setProperty("project.id", dependency.getId().getSimpleNameId().toString());
                    pr.setProperty("project.name", dependency.getId().getSimpleNameId().toString());
                    pr.setProperty("project.version", dependency.getId().getVersion().toString());
                    pr.setProperty("repositories", "~/.m2/repository;https\\://raw.githubusercontent.com/thevpc/vpc-public-maven/master;http\\://repo.maven.apache.org/maven2/;https\\://raw.githubusercontent.com/thevpc/vpc-public-nuts/master");
                    pr.setProperty("bootRuntimeId", runtimeUpdate.getAvailable().getId().getLongName());
                    try (Writer writer = Files.newBufferedWriter(
                            ws.config().getStoreLocation(dependency.getId().getLongNameId(), bootstrapFolder)
                                    .resolve("nuts.properties")
                    )) {
                        pr.store(writer, "Updated on " + new Date());
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
            Properties pr = new Properties();
            pr.setProperty("project.id", runtimeUpdate.getAvailable().getId().getSimpleNameId().toString());
            pr.setProperty("project.name", runtimeUpdate.getAvailable().getId().getSimpleNameId().toString());
            pr.setProperty("project.version", runtimeUpdate.getAvailable().getId().getVersion().toString());
            final NutsId rtId = runtimeUpdate.getAvailable().getId();
            pr.setProperty("project.dependencies.compile",
                    CoreStringUtils.join(";",
                            Arrays.stream(runtimeDesc.getDependencies())
                                    .filter(new Predicate<NutsDependency>() {
                                        @Override
                                        public boolean test(NutsDependency x) {
                                            return !x.isOptional() && CoreNutsUtils.SCOPE_RUN.accept(rtId, x);
                                        }
                                    })
                                    .map(x -> x.getId().getLongName())
                                    .collect(Collectors.toList())
                    )
            );
            try (Writer writer = Files.newBufferedWriter(
                    ws.config().getStoreLocation(runtimeUpdate.getAvailable().getId().getLongNameId(), bootstrapFolder)
                            .resolve("nuts.properties")
            )) {
                pr.store(writer, "Updated on " + new Date());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            ((DefaultNutsUpdateResult) runtimeUpdate).setUpdateApplied(true);
        }
        for (NutsUpdateResult extension : result.getExtensions()) {
            if (!extension.isUpdateApplied()) {
                ws.extensions().updateExtension(extension.getAvailable().getId());
                ((DefaultNutsUpdateResult) extension).setUpdateApplied(true);
            }
        }
        for (NutsUpdateResult component : result.getComponents()) {
            applyRegularUpdate((DefaultNutsUpdateResult) component);
        }

        if (ws.config().save(false)) {
            if (log.isLoggable(Level.INFO)) {
                log.log(Level.INFO, "Workspace is updated. Nuts should be restarted for changes to take effect.");
            }
        }
    }

    public NutsUpdateResult checkUpdates(NutsId id, String bootApiVersion, NutsSession session) {
        session = NutsWorkspaceUtils.validateSession(ws, session);
        NutsId oldId = null;
        NutsDefinition oldFile = null;
        NutsDefinition newFile = null;
        NutsId newId = null;
        List<NutsId> dependencies = new ArrayList<>();
//        NutsSession sessionOffline = session.copy().setFetchMode(NutsFetchMode.OFFLINE);
        if (id.getSimpleName().equals(NutsConstants.Ids.NUTS_API)) {
            oldId = ws.config().getConfigContext().getApiId();
            NutsId confId = ws.config().getConfigContext().getApiId();
            if (confId != null) {
                oldId = confId;
            }
            String v = bootApiVersion;
            if (CoreStringUtils.isBlank(v)) {
                v = NutsConstants.Versions.LATEST;
            }
            try {
                oldFile = ws.fetch().id(oldId).session(session).wired().getResultDefinition();
            } catch (NutsNotFoundException ex) {
                //ignore
            }
            try {
                newFile = ws.fetch().id(NutsConstants.Ids.NUTS_API + "#" + v).session(session).wired().getResultDefinition();
                newId = newFile.getId();
            } catch (NutsNotFoundException ex) {
                //ignore
            }
        } else if (id.getSimpleName().equals(NutsConstants.Ids.NUTS_RUNTIME)) {
            oldId = ws.config().getRunningContext().getRuntimeId();
            NutsId confId = ws.config().getConfigContext().getRuntimeId();
            if (confId != null) {
                oldId = confId;
            }
            try {
                oldFile = ws.fetch().id(oldId).session(session).wired().getResultDefinition();
            } catch (NutsNotFoundException ex) {
                //ignore
            }
            try {
                newFile = ws.find()
                        .addId(oldFile != null ? oldFile.getId().setVersion("").toString() : NutsConstants.Ids.NUTS_RUNTIME)
                        .setDescriptorFilter(new BootAPINutsDescriptorFilter(bootApiVersion))
                        .latestVersions()
                        .wired()
                        .session(session)
                        .getResultDefinitions().first();
                if (newFile != null) {
                    for (NutsDefinition d : ws.find().addId(newFile.getId()).latestVersions()
                            .wired()
                            .setSession(session).dependenciesOnly().getResultDefinitions()) {
                        dependencies.add(d.getId());
                    }
                }

            } catch (NutsNotFoundException ex) {
                //ignore
            }
            if (newFile != null) {
                newId = newFile.getId();
            }
        } else {
            try {
                oldId = ws.fetch().id(id).setEffective(true).setSession(session)
                        .offline().getResultId();
                oldFile = ws.fetch().id(oldId).setSession(session).getResultDefinition();
            } catch (Exception ex) {
                //ignore
            }
            try {
                newFile = ws.find().addId(NutsConstants.Ids.NUTS_RUNTIME)
                        .setDescriptorFilter(new BootAPINutsDescriptorFilter(bootApiVersion))
                        .latestVersions().setSession(session).online().mainAndDependencies().getResultDefinitions().first();
                for (NutsDefinition d : ws.find().id(newFile.getId()).latestVersions().scopes(scopes.isEmpty() ? Arrays.asList(NutsDependencyScope.PROFILE_RUN) : scopes)
                        .includeOptional(includeOptional)
                        .wired().setSession(session).dependenciesOnly().getResultDefinitions()) {
                    dependencies.add(d.getId());
                }
            } catch (Exception ex) {
                //ignore
            }
            if (newFile != null) {
                newId = newFile.getId();
            }
        }

        //compare canonical forms
        NutsId cnewId = toCanonicalForm(newId);
        NutsId coldId = toCanonicalForm(oldId);
        if (cnewId != null && coldId != null && cnewId.getVersion().compareTo(coldId.getVersion()) > 0) {
            return new DefaultNutsUpdateResult(id, oldFile, newFile, dependencies.toArray(new NutsId[0]), false);
        }
        return null;
    }

    private NutsId toCanonicalForm(NutsId id) {
        if (id != null) {
            id = id.setNamespace(null);
            if (NutsConstants.QueryKeys.FACE_DEFAULT_VALUE.equals(id.getQueryMap().get(NutsConstants.QueryKeys.FACE))) {
                id = id.setQueryProperty(NutsConstants.QueryKeys.FACE, null);
            }
        }
        return id;
    }

    private void applyRegularUpdate(DefaultNutsUpdateResult r) {
        if (r.isUpdateApplied()) {
            return;
        }
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        final PrintStream out = CoreIOUtils.resolveOut(ws, session);
        NutsId id = r.getId();
        NutsDefinition d0 = r.getLocal();
        NutsDefinition d1 = r.getAvailable();
        final String simpleName = d0 != null ? d0.getId().getSimpleName() : d1 != null ? d1.getId().getSimpleName() : id.getSimpleName();
        if (d0 == null) {
            ws.security().checkAllowed(NutsConstants.Rights.UPDATE, "update");
            dws.installImpl(d1, new String[0], null, session, true, this.isTrace(), true);
            r.setUpdateApplied(true);
            if (this.isTrace()) {
                out.printf("==%s== is [[forced]] to latest version ==%s==\n", simpleName, d1.getId().getVersion());
            }
        } else if (d1 == null) {
            //this is very interisting. Why the hell is this happening?
        } else {
            NutsVersion v0 = d0.getId().getVersion();
            NutsVersion v1 = d1.getId().getVersion();
            if (v1.compareTo(v0) <= 0) {
                //no update needed!
                if (this.isForce()) {
                    ws.security().checkAllowed(NutsConstants.Rights.UPDATE, "update");
                    dws.installImpl(d1, new String[0], null, session, true, this.isTrace(), true);
                    r.setUpdateApplied(true);
                    r.setUpdateForced(true);
                    if (this.isTrace()) {
                        out.printf("==%s== is [[forced]] from ==%s== to older version ==%s==\n", simpleName, d0.getId().getVersion(), d1.getId().getVersion());
                    }
                }
            } else {
                ws.security().checkAllowed(NutsConstants.Rights.UPDATE, "update");
                dws.installImpl(d1, new String[0], null, session, true, this.isTrace(), true);
                r.setUpdateApplied(true);
                if (this.isTrace()) {
                    out.printf("==%s== is [[updated]] from ==%s== to latest version ==%s==\n", simpleName, d0.getId().getVersion(), d1.getId().getVersion());
                }
            }
        }
    }

    private static class BootAPINutsDescriptorFilter implements NutsDescriptorFilter {

        private final String bootApiVersion;

        public BootAPINutsDescriptorFilter(String bootApiVersion) {
            this.bootApiVersion = bootApiVersion;
        }

        @Override
        public boolean accept(NutsDescriptor descriptor) {
            for (NutsDependency dependency : descriptor.getDependencies()) {
                if (dependency.getSimpleName().equals(NutsConstants.Ids.NUTS_API)) {
                    if (dependency.getVersion().matches("]" + bootApiVersion + "]")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public NutsUpdateCommand setUpdateRuntime(boolean updateRuntime) {
        this.updateRuntime = updateRuntime;
        return this;
    }

    @Override
    public NutsUpdateCommand setUpdateInstalled(boolean updateInstalled) {
        this.updateInstalled = updateInstalled;
        return this;
    }

    @Override
    public NutsUpdateCommand workspace() {
        setUpdateApi(true);
        setUpdateRuntime(true);
        return this;
    }

    @Override
    public NutsUpdateCommand api() {
        return api(true);
    }

    @Override
    public NutsUpdateCommand api(boolean enable) {
        setUpdateApi(enable);
        return this;
    }

    @Override
    public NutsUpdateCommand runtime() {
        return runtime(true);
    }

    @Override
    public NutsUpdateCommand runtime(boolean enable) {
        return setUpdateRuntime(enable);
    }

    @Override
    public NutsUpdateCommand extensions() {
        return extensions(true);
    }

    @Override
    public NutsUpdateCommand extensions(boolean enable) {
        return setUpdateExtensions(enable);
    }

    @Override
    public NutsUpdateCommand installed() {
        return installed(true);
    }

    @Override
    public NutsUpdateCommand installed(boolean enable) {
        return setUpdateInstalled(enable);
    }

    @Override
    public NutsUpdateCommand arg(String arg) {
        return addArg(arg);
    }

    @Override
    public NutsUpdateCommand args(String... arg) {
        return addArgs(arg);
    }

    @Override
    public NutsUpdateCommand args(Collection<String> arg) {
        return addArgs(arg);
    }

    @Override
    public NutsUpdateCommand ask() {
        return ask(true);
    }

    @Override
    public NutsUpdateCommand ask(boolean ask) {
        return setAsk(ask);
    }

    @Override
    public NutsUpdateCommand force() {
        return force(true);
    }

    @Override
    public NutsUpdateCommand force(boolean forceInstall) {
        return setForce(forceInstall);
    }

    @Override
    public NutsUpdateCommand session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsUpdateCommand all() {
        setUpdateApi(true);
        setUpdateRuntime(true);
        setUpdateExtensions(true);
        setUpdateInstalled(true);
        return this;
    }

    @Override
    public NutsUpdateCommand clearIds() {
        this.ids.clear();
        return this;
    }

    @Override
    public NutsUpdateCommand frozenId(NutsId id) {
        return addFrozenId(id);
    }

    @Override
    public NutsUpdateCommand frozenId(String id) {
        return addFrozenId(id);
    }

    @Override
    public NutsUpdateCommand addFrozenId(NutsId id) {
        if (id != null) {
            frozenIds.add(id);
        }
        return this;
    }

    @Override
    public NutsUpdateCommand addFrozenId(String id) {
        if (!CoreStringUtils.isBlank(id)) {
            frozenIds.add(ws.parser().parseRequiredId(id));
        }
        return this;
    }

    @Override
    public NutsUpdateCommand frozenIds(NutsId... ids) {
        return addFrozenIds(ids);
    }

    @Override
    public NutsUpdateCommand frozenIds(String... ids) {
        return addFrozenIds(ids);
    }

    @Override
    public NutsUpdateCommand addFrozenIds(NutsId... ids) {
        if (ids != null) {
            for (NutsId id : ids) {
                addId(id);
            }
        }
        return this;
    }

    @Override
    public NutsUpdateCommand addFrozenIds(String... ids) {
        if (ids != null) {
            for (String id : ids) {
                addId(id);
            }
        }
        return this;
    }

    @Override
    public NutsUpdateCommand clearFrozenIds() {
        this.frozenIds.clear();
        return this;
    }

    @Override
    public NutsUpdateCommand trace() {
        return trace(true);
    }

    @Override
    public NutsUpdateCommand trace(boolean enable) {
        return setTrace(enable);
    }

    @Override
    public NutsUpdateCommand includeOptional() {
        return includeOptional(true);
    }

    @Override
    public NutsUpdateCommand includeOptional(boolean enable) {
        return setIncludeOptional(enable);
    }

    @Override
    public NutsUpdateCommand apiVersion(String forceBootAPIVersion) {
        return setApiVersion(forceBootAPIVersion);
    }

    @Override
    public NutsUpdateCommand updateWorkspace() {
        return updateWorkspace(true);
    }

    @Override
    public NutsUpdateCommand updateWorkspace(boolean enable) {
        return setUpdateApi(enable);
    }

    @Override
    public NutsUpdateCommand updateExtensions() {
        return updateExtensions(true);
    }

    @Override
    public NutsUpdateCommand updateExtensions(boolean enable) {
        return setUpdateExtensions(enable);
    }

    @Override
    public NutsUpdateCommand updateRunime() {
        return setUpdateRuntime(true);
    }

    @Override
    public NutsUpdateCommand updateRuntime(boolean enable) {
        return setUpdateRuntime(enable);
    }

    @Override
    public NutsUpdateCommand updateInstalled() {
        return updateInstalled(true);
    }

    @Override
    public NutsUpdateCommand updateInstalled(boolean enable) {
        return setUpdateInstalled(enable);
    }

    @Override
    public NutsUpdateCommand clearScopes() {
        this.scopes.clear();
        return this;
    }

    @Override
    public NutsUpdateCommand parseOptions(String... args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-a":
                    case "--all": {
                        this.all();
                        break;
                    }
                    case "-i":
                    case "--installed": {
                        this.all();
                        break;
                    }
                    case "-w":
                    case "--ws":
                    case "--workspace": {
                        this.workspace();
                        break;
                    }
                    case "-r":
                    case "--runtime": {
                        this.runtime();
                        break;
                    }
                    case "-A":
                    case "--api": {
                        this.runtime();
                        break;
                    }

                    case "-e":
                    case "--extensions": {
                        this.extensions();
                        break;
                    }
                    case "-v":
                    case "--version": {
                        i++;
                        this.setApiVersion(args[i]);
                        break;
                    }
                    case "--args": {
                        while (i < args.length) {
                            this.addArg(args[i]);
                            i++;
                        }
                        break;
                    }
                    case "--help": {
                        //
                        break;
                    }
                    default: {
                        if (args[i].startsWith("-")) {
                            throw new NutsIllegalArgumentException("Unsupported option " + args[i]);
                        } else {
                            id(args[i]);
                        }
                    }
                }
            }
        }
        return this;
    }

    @Override
    public NutsUpdateCommand formatType(NutsResultFormatType formatType) {
        return setFormatType(formatType);
    }

    @Override
    public NutsUpdateCommand setFormatType(NutsResultFormatType formatType) {
        if(formatType==null){
            formatType=NutsResultFormatType.PLAIN;
        }
        this.formatType=formatType;
        return this;
    }

    @Override
    public NutsUpdateCommand json() {
        return setFormatType(NutsResultFormatType.JSON);
    }

    @Override
    public NutsUpdateCommand plain() {
        return setFormatType(NutsResultFormatType.PLAIN);
    }

    @Override
    public NutsUpdateCommand props() {
        return setFormatType(NutsResultFormatType.PROPS);
    }

    @Override
    public NutsResultFormatType getFormatType() {
        return this.formatType;
    }
    

}
