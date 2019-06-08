/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.vpc.app.nuts.NutsBootConfig;
import net.vpc.app.nuts.NutsBootContext;
import net.vpc.app.nuts.NutsBootContextType;
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
import net.vpc.app.nuts.NutsQuestion;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.NutsUpdateCommand;
import net.vpc.app.nuts.NutsUserCancelException;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsSearchCommand;

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsUpdateCommand extends NutsWorkspaceCommandBase<NutsUpdateCommand> implements NutsUpdateCommand {

    public static final Logger LOG = Logger.getLogger(DefaultNutsUpdateCommand.class.getName());
    private boolean enableInstall = true;
    private boolean updateApi = false;
    private boolean updateRuntime = false;
    private boolean updateExtensions = false;
    private boolean updateInstalled = false;
    private boolean includeOptional = false;
    private String forceBootAPIVersion;
    private List<String> args;
    private final List<NutsDependencyScope> scopes = new ArrayList<>();
    private final List<NutsId> frozenIds = new ArrayList<>();
    private final List<NutsId> ids = new ArrayList<>();

    private NutsWorkspaceUpdateResult result;

    public DefaultNutsUpdateCommand(NutsWorkspace ws) {
        super(ws, "update");
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
        return addId(id == null ? null : ws.parse().id(id));
    }

    @Override
    public NutsUpdateCommand addId(NutsId id) {
        if (id == null) {
            throw new NutsNotFoundException(ws, id);
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
        return removeId(ws.parse().id(id));
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
    public boolean isIncludeOptional() {
        return includeOptional;
    }

    @Override
    public NutsUpdateCommand setIncludeOptional(boolean includeOptional) {
        this.includeOptional = includeOptional;
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
    public int getResultCount() {
        return getResult().getUpdatesCount();
    }

    @Override
    public NutsWorkspaceUpdateResult getResult() {
        if (result == null) {
            checkUpdates();
        }
        if (result == null) {
            throw new NutsUnexpectedException(ws);
        }
        return result;
    }

    @Override
    public NutsUpdateCommand run() {
        return update();
    }

    @Override
    public NutsUpdateCommand update() {
        applyResult(getResult());
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

    private Set<NutsId> getCompanionsToUpdate() {
        Set<NutsId> ext = new HashSet<>();
        for (String extension : NutsWorkspaceExt.of(ws).getCompanionTools()) {
            ext.add(ws.parse().id(extension).getSimpleNameId());
        }
        return ext;
    }

    private Set<NutsId> getRegularIds() {
        HashSet<String> extensions = new HashSet<>();
        for (NutsId object : ws.extensions().getExtensions()) {
            extensions.add(object.getSimpleName());
        }

        HashSet<NutsId> baseRegulars = new HashSet<>(ids);
        if (isUpdateInstalled()) {
            baseRegulars.addAll(ws.search().session(getValidSession().copy().trace(false)).installed().getResultIds().stream().map(x -> x.getSimpleNameId()).collect(Collectors.toList()));
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
        NutsBootContext actualBootConfig = ws.config().getContext(NutsBootContextType.RUNTIME);
//        NutsBootContext jsonBootConfig = getConfigManager().getBootContext();
        NutsSession session = NutsWorkspaceUtils.validateSession(ws, this.getSession());
        Map<String, NutsUpdateResult> allUpdates = new LinkedHashMap<>();
        Map<String, NutsUpdateResult> extUpdates = new LinkedHashMap<>();
        Map<String, NutsUpdateResult> regularUpdates = new HashMap<>();
        NutsUpdateResult apiUpdate = null;
        String bootVersion0 = ws.config().getContext(NutsBootContextType.RUNTIME).getApiId().getVersion().getValue();
        String bootVersion = bootVersion0;
        if (!CoreStringUtils.isBlank(this.getApiVersion())) {
            bootVersion = this.getApiVersion();
        }
        if (this.isUpdateApi() || !CoreStringUtils.isBlank(this.getApiVersion())) {
            apiUpdate = checkCoreUpdate(ws.parse().id(NutsConstants.Ids.NUTS_API), this.getApiVersion(), session);
            if (apiUpdate.isUpdateAvailable()) {
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
                runtimeUpdate = checkCoreUpdate(ws.parse().id(actualBootConfig.getRuntimeId().getSimpleName()),
                        apiUpdate != null && apiUpdate.getAvailable().getId() != null ? apiUpdate.getAvailable().getId().toString()
                        : bootVersion, session);
                if (runtimeUpdate.isUpdateAvailable()) {
                    allUpdates.put(runtimeUpdate.getAvailable().getId().getSimpleName(), runtimeUpdate);
                }
            }
        }
        for (NutsId ext : getExtensionsToUpdate()) {
            NutsUpdateResult extUpdate = checkCoreUpdate(ext, bootVersion, session);
            allUpdates.put(extUpdate.getAvailable().getId().getSimpleName(), extUpdate);
            extUpdates.put(extUpdate.getAvailable().getId().getSimpleName(), extUpdate);
        }

        for (NutsId ext : getCompanionsToUpdate()) {
            NutsUpdateResult extUpdate = checkCoreUpdate(ext, bootVersion, session);
            allUpdates.put(extUpdate.getAvailable().getId().getSimpleName(), extUpdate);
            regularUpdates.put(extUpdate.getAvailable().getId().getSimpleName(), extUpdate);
        }

        for (NutsId id : this.getRegularIds()) {
            NutsUpdateResult updated = checkRegularUpdate(id);
            allUpdates.put(updated.getAvailable().getId().getSimpleName(), updated);
            regularUpdates.put(updated.getId().getSimpleName(), updated);
        }
        NutsId[] frozenIds = this.getFrozenIds();
        if (frozenIds.length > 0) {
            for (NutsId d : new HashSet<>(Arrays.asList(frozenIds))) {
                NutsDependency dd = CoreNutsUtils.parseNutsDependency(ws, d.toString());
                if (regularUpdates.containsKey(dd.getSimpleName())) {
                    NutsUpdateResult updated = regularUpdates.get(dd.getSimpleName());
                    //FIX ME
                    if (!dd.getVersion().toFilter().accept(updated.getId().getVersion(), ws, session)) {
                        throw new NutsIllegalArgumentException(ws, dd + " unsatisfied  : " + updated.getId().getVersion());
                    }
                }
            }
        }

        result = new DefaultNutsWorkspaceUpdateResult(apiUpdate, runtimeUpdate, extUpdates.values().toArray(new NutsUpdateResult[0]),
                regularUpdates.values().toArray(new NutsUpdateResult[0])
        );
        traceUpdates(result);
        return this;
    }

    protected void traceUpdates(NutsWorkspaceUpdateResult result) {
        if (getValidSession().isPlainTrace()) {
            PrintStream out = CoreIOUtils.resolveOut(ws, getValidSession());
            NutsUpdateResult[] updates = result.getAllUpdates();
            if (updates.length == 0) {
                out.printf("All components are [[up-to-date]]. You are running latest version.%n");
            } else {
                out.printf("Workspace has ##%s## component%s to update.%n", updates.length, (updates.length > 1 ? "s" : ""));
                int widthCol1 = 2;
                int widthCol2 = 2;
                for (NutsUpdateResult update : updates) {
                    widthCol1 = Math.max(widthCol1, update.getAvailable().getId().getSimpleName().length());
                    widthCol2 = Math.max(widthCol2, update.getLocal().getId().getVersion().toString().length());
                }
                for (NutsUpdateResult update : updates) {
                    out.printf("((%s))  : %s => [[%s]]%n",
                            CoreStringUtils.alignLeft(update.getLocal().getId().getVersion().toString(), widthCol2),
                            CoreStringUtils.alignLeft(update.getAvailable().getId().getSimpleName(), widthCol1),
                            update.getAvailable().getId().getVersion().toString());
                }
            }
        }
    }

    private NutsSearchCommand latestDependencies(NutsSearchCommand se) {
        return se.inlineDependencies()
                .scopes(scopes.isEmpty() ? Arrays.asList(NutsDependencyScope.PROFILE_RUN) : scopes)
                .optional(includeOptional ? null : false)
                .latest();
    }

    protected NutsUpdateResult checkRegularUpdate(NutsId id) {
        NutsSession session = getValidSession();
        NutsSession searchSession = session.copy().trace(false);
        NutsVersion version = id.getVersion();
        if (version.isSingleValue()) {
            throw new NutsIllegalArgumentException(ws, id + " : Version is too restrictive. You should use fetch or install instead");
        }

        DefaultNutsUpdateResult r = new DefaultNutsUpdateResult();
        r.setId(id.getSimpleNameId());

        NutsDefinition d0 = ws.search().id(id).setSession(searchSession).installed().setOptional(false).failFast(false).defaultVersions().getResultDefinitions().first();
        if (d0 == null) {
            throw new NutsIllegalArgumentException(ws, id + " is not yet installed to be updated.");
        }
        NutsDefinition d1 = latestDependencies(ws.search().id(id).setSession(searchSession))
                .failFast(false)
                .anyWhere()
                .getResultDefinitions().first();
        r.setLocal(d0);
        r.setAvailable(d1);
        if (d0 == null) {
            if (!this.isEnableInstall()) {
                throw new NutsIllegalArgumentException(ws, "No version is installed to be updated for " + id);
            }
            if (d1 == null) {
                throw new NutsNotFoundException(ws, id);
            }
            r.setUpdateAvailable(true);
            r.setUpdateForced(false);
        } else if (d1 == null) {
            //this is very interisting. Why the hell is this happening?
            r.setAvailable(d0);
        } else {
            NutsVersion v0 = d0.getId().getVersion();
            NutsVersion v1 = d1.getId().getVersion();
            if (v1.compareTo(v0) <= 0) {
                //no update needed!
                if (session.isForce()) {
                    r.setUpdateForced(true);
                }
            } else {
                r.setUpdateAvailable(true);
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
        traceUpdates(result);
        final PrintStream out = CoreIOUtils.resolveOut(ws, getValidSession());
        boolean accept = ws.io().getTerminal().ask()
                .forBoolean("Would you like to apply updates").setDefaultValue(true)
                .session(getValidSession()).getValue();
        if (getValidSession().isAsk() && !accept) {
            throw new NutsUserCancelException(ws);
        }
        NutsBootContext actualBootConfig = ws.config().getContext(net.vpc.app.nuts.NutsBootContextType.RUNTIME);
        Path bootstrapFolder = ws.config().getWorkspaceLocation().resolve(NutsConstants.Folders.BOOT);
        if (apiUpdate.isUpdateAvailable() && !apiUpdate.isUpdateApplied()) {
            NutsWorkspaceUtils.checkReadOnly(ws);

            NutsBootConfig bc = ws.config().getBootConfig();
            bc.setApiVersion(apiUpdate.getAvailable().getId().getVersion().toString());
            ws.config().setBootConfig(bc);
            ws.io().copy().from(apiUpdate.getAvailable().getPath()).to(ws.config().getStoreLocation(apiUpdate.getAvailable().getId(), bootstrapFolder)
                    .resolve(ws.config().getDefaultIdFilename(apiUpdate.getAvailable().getId().setFaceComponent().setPackaging("jar")))
            ).run();
            ws.format().descriptor().print(ws.fetch().id(apiUpdate.getAvailable().getId()).getResultDescriptor(),
                    ws.config().getStoreLocation(apiUpdate.getAvailable().getId(), bootstrapFolder)
                            .resolve(ws.config().getDefaultIdFilename(apiUpdate.getAvailable().getId().setFaceDescriptor()))
            );
            ((DefaultNutsUpdateResult) apiUpdate).setUpdateApplied(true);
            traceSingleUpdate(apiUpdate);
        }
        if (runtimeUpdate.isUpdateAvailable() && !runtimeUpdate.isUpdateApplied()) {
            NutsBootConfig bc = ws.config().getBootConfig();
            bc.setRuntimeId(runtimeUpdate.getAvailable().getId().getLongName());
            bc.setRuntimeDependencies(Arrays.stream(runtimeUpdate.getDependencies()).map(NutsId::getLongName).collect(Collectors.joining(";")));
            NutsWorkspaceUtils.checkReadOnly(ws);
            ws.config().setBootConfig(bc);
            ws.io().copy().from(runtimeUpdate.getAvailable().getPath())
                    .to(ws.config().getStoreLocation(runtimeUpdate.getAvailable().getId(), bootstrapFolder)
                            .resolve(ws.config().getDefaultIdFilename(runtimeUpdate.getAvailable().getId().setFaceComponent().setPackaging("jar")))
                    ).run();
            NutsDescriptor runtimeDesc = ws.fetch().id(runtimeUpdate.getAvailable().getId()).getResultDescriptor();
            ws.format().descriptor().print(runtimeDesc,
                    ws.config().getStoreLocation(runtimeUpdate.getAvailable().getId(), bootstrapFolder)
                            .resolve(ws.config().getDefaultIdFilename(runtimeUpdate.getAvailable().getId().setFaceDescriptor()))
            );
            for (NutsDependency dependency : runtimeDesc.getDependencies()) {
                if (dependency.getId().getSimpleNameId().equals(actualBootConfig.getApiId().getSimpleNameId())) {
                    Map<String, String> pr = new LinkedHashMap<>();
                    pr.put("file.updated.date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
                    pr.put("project.id", dependency.getId().getSimpleNameId().toString());
                    pr.put("project.name", dependency.getId().getSimpleNameId().toString());
                    pr.put("project.version", dependency.getId().getVersion().toString());
                    pr.put("repositories", "~/.m2/repository;https\\://raw.githubusercontent.com/thevpc/vpc-public-maven/master;http\\://repo.maven.apache.org/maven2/;https\\://raw.githubusercontent.com/thevpc/vpc-public-nuts/master");
                    pr.put("bootRuntimeId", runtimeUpdate.getAvailable().getId().getLongName());
                    try (Writer writer = Files.newBufferedWriter(
                            ws.config().getStoreLocation(dependency.getId().getLongNameId(), bootstrapFolder)
                                    .resolve("nuts.properties")
                    )) {
                        CoreIOUtils.storeProperties(pr, writer, false);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
            Map<String, String> pr = new LinkedHashMap<>();
            pr.put("file.updated.date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));
            pr.put("project.id", runtimeUpdate.getAvailable().getId().getSimpleNameId().toString());
            pr.put("project.name", runtimeUpdate.getAvailable().getId().getSimpleNameId().toString());
            pr.put("project.version", runtimeUpdate.getAvailable().getId().getVersion().toString());
            final NutsId rtId = runtimeUpdate.getAvailable().getId();
            pr.put("project.dependencies.compile",
                    CoreStringUtils.join(";",
                            Arrays.stream(runtimeDesc.getDependencies())
                                    .filter(new Predicate<NutsDependency>() {
                                        @Override
                                        public boolean test(NutsDependency x) {
                                            return !x.isOptional() && CoreNutsUtils.SCOPE_RUN.accept(rtId, x, ws, getValidSession());
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
                CoreIOUtils.storeProperties(pr, writer, false);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            ((DefaultNutsUpdateResult) runtimeUpdate).setUpdateApplied(true);
            traceSingleUpdate(runtimeUpdate);
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
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, "Workspace is updated. Nuts should be restarted for changes to take effect.");
            }
            if (apiUpdate.isUpdateAvailable() && !apiUpdate.isUpdateApplied()) {
                if (getValidSession().isPlainTrace()) {
                    out.println("Workspace is updated. Nuts should be restarted for changes to take effect.");
                }
            }
        }
    }

    private void traceSingleUpdate(NutsUpdateResult r) {
        NutsId id = r.getId();
        NutsDefinition d0 = r.getLocal();
        NutsDefinition d1 = r.getAvailable();
        final String simpleName = d0 != null ? d0.getId().getSimpleName() : d1 != null ? d1.getId().getSimpleName() : id.getSimpleName();
        final PrintStream out = CoreIOUtils.resolveOut(ws, getValidSession());
        if (r.isUpdateApplied()) {
            if (r.isUpdateForced()) {
                if (d0 == null) {
                    out.printf("==%s== is [[updated]] to latest version ==%s==%n", simpleName, d1 == null ? null : d1.getId().getVersion());
                } else if (d1 == null) {
                    //this is very interesting. Why the hell is this happening?
                } else {
                    NutsVersion v0 = d0.getId().getVersion();
                    NutsVersion v1 = d1.getId().getVersion();
                    if (v1.compareTo(v0) <= 0) {
                        if (v1.compareTo(v0) == 0) {
                            out.printf("==%s== is [[forced]] to ==%s== %n", simpleName, d0.getId().getVersion());
                        } else {
                            out.printf("==%s== is [[forced]] from ==%s== to older version ==%s==%n", simpleName, d0.getId().getVersion(), d1.getId().getVersion());
                        }
                    } else {
                        out.printf("==%s== is [[updated]] from ==%s== to latest version ==%s==%n", simpleName, d0.getId().getVersion(), d1.getId().getVersion());
                    }
                }
            }
        }
    }

    public NutsUpdateResult checkCoreUpdate(NutsId id, String bootApiVersion, NutsSession session) {
        //disable trace so that search do not write to stream
        session = NutsWorkspaceUtils.validateSession(ws, session);
        NutsSession searchSession = session.copy().trace(false);
        NutsId oldId = null;
        NutsDefinition oldFile = null;
        NutsDefinition newFile = null;
        NutsId newId = null;
        List<NutsId> dependencies = new ArrayList<>();
//        NutsSession sessionOffline = session.copy().setFetchMode(NutsFetchMode.OFFLINE);
        if (id.getSimpleName().equals(NutsConstants.Ids.NUTS_API)) {
            oldId = ws.config().getContext(NutsBootContextType.CONFIG).getApiId();
            NutsId confId = ws.config().getContext(NutsBootContextType.CONFIG).getApiId();
            if (confId != null) {
                oldId = confId;
            }
            String v = bootApiVersion;
            if (CoreStringUtils.isBlank(v)) {
                v = NutsConstants.Versions.LATEST;
            }
            try {
                oldFile = ws.fetch().id(oldId).session(session).online().getResultDefinition();
            } catch (NutsNotFoundException ex) {
                //ignore
            }
            try {
                newFile = ws.search().session(searchSession).id(NutsConstants.Ids.NUTS_API + "#" + v).anyWhere().latest().getResultDefinitions().first();
                newId = newFile.getId();
            } catch (NutsNotFoundException ex) {
                //ignore
            }
        } else if (id.getSimpleName().equals(NutsConstants.Ids.NUTS_RUNTIME)) {
            oldId = ws.config().getContext(NutsBootContextType.RUNTIME).getRuntimeId();
            NutsId confId = ws.config().getContext(NutsBootContextType.CONFIG).getRuntimeId();
            if (confId != null) {
                oldId = confId;
            }
            try {
                oldFile = ws.fetch().id(oldId).session(searchSession).online().getResultDefinition();
            } catch (NutsNotFoundException ex) {
                //ignore
            }
            try {
                newFile = ws.search()
                        .addId(oldFile != null ? oldFile.getId().setVersion("").toString() : NutsConstants.Ids.NUTS_RUNTIME)
                        .setDescriptorFilter(new BootAPINutsDescriptorFilter(bootApiVersion))
                        .latest()
                        .online()
                        .session(session)
                        .getResultDefinitions().first();
                if (newFile != null) {
                    for (NutsDefinition d : latestDependencies(ws.search().addId(newFile.getId()))
                            .anyWhere()
                            .setSession(searchSession).main(false)
                            .getResultDefinitions()) {
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
                oldId = ws.search().id(id).setEffective(true).setSession(searchSession)
                        .offline().getResultIds().first();
                oldFile = ws.fetch().id(oldId).setSession(searchSession).getResultDefinition();
            } catch (Exception ex) {
                //ignore
            }
            try {
                newFile = latestDependencies(ws.search().session(searchSession).addId(id))
                        .setDescriptorFilter(new BootAPINutsDescriptorFilter(bootApiVersion))
                        .online()
                        .getResultDefinitions().first();
                for (NutsDefinition d : latestDependencies(ws.search().session(searchSession).id(newFile.getId()))
                        .anyWhere().main(false).getResultDefinitions()) {
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
        DefaultNutsUpdateResult defaultNutsUpdateResult = new DefaultNutsUpdateResult(id, oldFile, newFile, dependencies.toArray(new NutsId[0]), false);
        if (cnewId != null && coldId != null && cnewId.getVersion().compareTo(coldId.getVersion()) > 0) {
            defaultNutsUpdateResult.setUpdateAvailable(true);
        } else {
            defaultNutsUpdateResult.setUpdateAvailable(false);
        }
        return defaultNutsUpdateResult;
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
        final PrintStream out = CoreIOUtils.resolveOut(ws, getValidSession());
        NutsId id = r.getId();
        NutsDefinition d0 = r.getLocal();
        NutsDefinition d1 = r.getAvailable();
        if (d0 == null) {
            ws.security().checkAllowed(NutsConstants.Rights.UPDATE, "update");
            dws.updateImpl(d1, new String[0], null, getValidSession(), true);
            r.setUpdateApplied(true);
        } else if (d1 == null) {
            //this is very interesting. Why the hell is this happening?
        } else {
            NutsVersion v0 = d0.getId().getVersion();
            NutsVersion v1 = d1.getId().getVersion();
            if (v1.compareTo(v0) <= 0) {
                //no update needed!
                if (getValidSession().isForce()) {
                    ws.security().checkAllowed(NutsConstants.Rights.UPDATE, "update");
                    dws.updateImpl(d1, new String[0], null, getValidSession(), true);
                    r.setUpdateApplied(true);
                    r.setUpdateForced(true);
                }
            } else {
                ws.security().checkAllowed(NutsConstants.Rights.UPDATE, "update");
                dws.updateImpl(d1, new String[0], null, getValidSession(), true);
                r.setUpdateApplied(true);
            }
        }
        traceSingleUpdate(r);
    }

    private static class BootAPINutsDescriptorFilter implements NutsDescriptorFilter {

        private final String bootApiVersion;

        public BootAPINutsDescriptorFilter(String bootApiVersion) {
            this.bootApiVersion = bootApiVersion;
        }

        @Override
        public boolean accept(NutsDescriptor descriptor, NutsWorkspace ws, NutsSession session) {
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
        setUpdateExtensions(true);
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
            frozenIds.add(ws.parse().requiredId(id));
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
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "-a":
            case "--all": {
                this.all();
                return true;
            }
            case "-w":
            case "--ws":
            case "--workspace": {
                this.workspace();
                return true;
            }
            case "-i":
            case "--installed": {
                this.installed(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "-r":
            case "--runtime": {
                this.runtime(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "-A":
            case "--api": {
                this.runtime(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }

            case "-e":
            case "--extensions": {
                this.extensions(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "-v":
            case "--version": {
                this.setApiVersion(cmdLine.nextString().getStringValue());
                return true;
            }
            case "-g":
            case "--args": {
                cmdLine.skip();
                this.addArgs(cmdLine.toArray());
                cmdLine.skipAll();
                return true;
            }

            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                if (a.isOption()) {
                    return false;
                } else {
                    cmdLine.skip();
                    id(a.getString());
                    return true;
                }
            }
        }
    }

}
