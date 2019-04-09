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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.NutsUnexpectedException;
import net.vpc.app.nuts.NutsWorkspaceUpdateResult;
import net.vpc.app.nuts.NutsUpdateWorkspaceCommand;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceUpdateResultItem;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsUpdateWorkspaceCommand implements NutsUpdateWorkspaceCommand {

    public static final Logger log = Logger.getLogger(DefaultNutsUpdateWorkspaceCommand.class.getName());

    private boolean ask = true;
    private boolean trace = true;
    private boolean force = false;
    private boolean enableInstall = true;
    private boolean enableMajorUpdates = true;
    private boolean updateExtensions = true;
    private String forceBootAPIVersion;
    private List<String> args;
    private List<NutsId> frozenIds = new ArrayList<>();
    private NutsSession session;
    private NutsWorkspace ws;
    private NutsWorkspaceUpdateResult result;
    private boolean resultApplied;

    public DefaultNutsUpdateWorkspaceCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public boolean isTrace() {
        return trace;
    }

    @Override
    public NutsUpdateWorkspaceCommand setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public boolean isForce() {
        return force;
    }

    @Override
    public NutsUpdateWorkspaceCommand setForce(boolean forceInstall) {
        this.force = forceInstall;
        return this;
    }

    @Override
    public boolean isAsk() {
        return ask;
    }

    @Override
    public NutsUpdateWorkspaceCommand setAsk(boolean ask) {
        this.ask = ask;
        return this;
    }

    @Override
    public String[] getArgs() {
        return args == null ? new String[0] : args.toArray(new String[0]);
    }

    @Override
    public NutsUpdateWorkspaceCommand setArgs(String... args) {
        return setArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NutsUpdateWorkspaceCommand setArgs(List<String> args) {
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
    public NutsUpdateWorkspaceCommand addArg(String arg) {
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
    public NutsUpdateWorkspaceCommand addArgs(String... args) {
        return addArgs(args == null ? null : Arrays.asList(args));
    }

    @Override
    public NutsUpdateWorkspaceCommand addArgs(List<String> args) {
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
    public NutsUpdateWorkspaceCommand setSession(NutsSession session) {
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
    public NutsUpdateWorkspaceCommand setEnableInstall(boolean enableInstall) {
        this.enableInstall = enableInstall;
        return this;
    }

    @Override
    public boolean isEnableMajorUpdates() {
        return enableMajorUpdates;
    }

    @Override
    public NutsUpdateWorkspaceCommand setEnableMajorUpdates(boolean enableMajorUpdates) {
        this.enableMajorUpdates = enableMajorUpdates;
        return this;
    }

    @Override
    public boolean isUpdateExtensions() {
        return updateExtensions;
    }

    @Override
    public NutsUpdateWorkspaceCommand setUpdateExtensions(boolean updateExtensions) {
        this.updateExtensions = updateExtensions;
        return this;
    }

    @Override
    public String getForceBootAPIVersion() {
        return forceBootAPIVersion;
    }

    @Override
    public NutsUpdateWorkspaceCommand setForceBootAPIVersion(String forceBootAPIVersion) {
        this.forceBootAPIVersion = forceBootAPIVersion;
        return this;
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
    public NutsUpdateWorkspaceCommand update() {
        if (!resultApplied) {
            applyResult(getUpdateResult());
            resultApplied = true;
        }
        return this;
    }

    @Override
    public NutsUpdateWorkspaceCommand checkUpdates(boolean applyUpdates) {
        checkUpdates();
        if (applyUpdates) {
            update();
        }
        return this;
    }

    @Override
    public NutsUpdateWorkspaceCommand checkUpdates() {
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsBootContext actualBootConfig = ws.config().getRunningContext();
//        NutsBootContext jsonBootConfig = getConfigManager().getBootContext();
        NutsSession session = CoreNutsUtils.validateSession(this.getSession(), ws);
        Map<String, NutsWorkspaceUpdateResultItem> allUpdates = new LinkedHashMap<>();
        Map<String, NutsWorkspaceUpdateResultItem> extUpdates = new LinkedHashMap<>();
        NutsWorkspaceUpdateResultItem bootUpdate = null;
        String bootVersion = ws.config().getRunningContext().getApiId().getVersion().toString();
        if (!CoreStringUtils.isBlank(this.getForceBootAPIVersion())) {
            bootVersion = this.getForceBootAPIVersion();
        }
        if (this.isEnableMajorUpdates()) {
            bootUpdate = checkUpdates(ws.parser().parseId(NutsConstants.Ids.NUTS_API), this.getForceBootAPIVersion(), session);
            if (bootUpdate != null) {
                bootVersion = bootUpdate.getAvailableId().getVersion().toString();
                allUpdates.put(NutsConstants.Ids.NUTS_API, bootUpdate);
            }
        }
        NutsWorkspaceUpdateResultItem runtimeUpdate = null;
        if (dws.requiresCoreExtension()) {

            runtimeUpdate = checkUpdates(ws.parser().parseId(actualBootConfig.getRuntimeId().getSimpleName()),
                    bootUpdate != null && bootUpdate.getAvailableId() != null ? bootUpdate.getAvailableId().toString()
                    : bootVersion, session);
            if (runtimeUpdate != null) {
                allUpdates.put(runtimeUpdate.getAvailableId().getSimpleName(), runtimeUpdate);
            }
        }
        if (this.isUpdateExtensions()) {
            for (NutsId ext : ws.extensions().getExtensions()) {
                NutsWorkspaceUpdateResultItem extUpdate = checkUpdates(ext, bootVersion, session);
                if (extUpdate != null) {
                    allUpdates.put(extUpdate.getAvailableId().getSimpleName(), extUpdate);
                    extUpdates.put(extUpdate.getAvailableId().getSimpleName(), extUpdate);
                }
            }
        }
        NutsWorkspaceUpdateResultItem[] updates = allUpdates.values().toArray(new NutsWorkspaceUpdateResultItem[0]);
        PrintStream out = CoreIOUtils.resolveOut(ws, session);
        if (this.isTrace()) {
            if (updates.length == 0) {
                out.printf("Workspace is [[up-to-date]]. You are running latest version ==%s==\n", actualBootConfig.getRuntimeId().getVersion());
                return null;
            } else {
                out.printf("Workspace has ##%s## component%s to update.\n", updates.length, (updates.length > 1 ? "s" : ""));
                int widthCol1 = 2;
                int widthCol2 = 2;
                for (NutsWorkspaceUpdateResultItem update : updates) {
                    widthCol1 = Math.max(widthCol1, update.getAvailableId().getSimpleName().length());
                    widthCol2 = Math.max(widthCol2, update.getLocalId().getVersion().toString().length());
                }
                for (NutsWorkspaceUpdateResultItem update : updates) {
                    out.printf("((%s))  : %s => [[%s]]\n",
                            CoreStringUtils.alignLeft(update.getLocalId().getVersion().toString(), widthCol2),
                            CoreStringUtils.alignLeft(update.getAvailableId().getSimpleName(), widthCol1),
                            update.getAvailableId().getVersion().toString());
                }
            }
        }
        result = new NutsWorkspaceUpdateResult(bootUpdate, runtimeUpdate, extUpdates.values().toArray(new NutsWorkspaceUpdateResultItem[0]));
        return this;
    }

    private void applyResult(NutsWorkspaceUpdateResult result) {
        NutsWorkspaceUpdateResultItem bootUpdate = result.getApi();
        NutsWorkspaceUpdateResultItem runtimeUpdate = result.getRuntime();
        if (result.getUpdatesCount() == 0) {
            return;
        }
        NutsBootContext actualBootConfig = ws.config().getRunningContext();
        Path bootstrapFolder = ws.config().getWorkspaceLocation().resolve(NutsConstants.Folders.BOOT);
        if (bootUpdate != null) {
            if (bootUpdate.getAvailableId() != null) {
                CoreNutsUtils.checkReadOnly(ws);
                NutsBootConfig bc = ws.config().getBootConfig();
                bc.setApiVersion(bootUpdate.getAvailableId().getVersion().toString());
                ws.config().setBootConfig(bc);
                ws.io().copy().from(bootUpdate.getAvailableIdFile()).to(ws.config().getStoreLocation(bootUpdate.getAvailableId(), bootstrapFolder)
                        .resolve(ws.config().getDefaultIdFilename(bootUpdate.getAvailableId().setFaceComponent().setPackaging("jar")))
                ).run();
                ws.formatter().createDescriptorFormat().pretty().print(ws.fetch().id(bootUpdate.getAvailableId()).getResultDescriptor(),
                        ws.config().getStoreLocation(bootUpdate.getAvailableId(), bootstrapFolder)
                                .resolve(ws.config().getDefaultIdFilename(bootUpdate.getAvailableId().setFaceDescriptor()))
                );
                if (runtimeUpdate == null) {

                }
            }
        }
        if (runtimeUpdate != null) {
            NutsBootConfig bc = ws.config().getBootConfig();
            bc.setRuntimeId(runtimeUpdate.getAvailableId().getVersion().toString());
            StringBuilder sb = new StringBuilder();
            for (NutsId dependency : runtimeUpdate.getDependencies()) {
                if (sb.length() > 0) {
                    sb.append(";");
                }
                sb.append(dependency.setNamespace(null).toString());
            }
            bc.setRuntimeDependencies(sb.toString());
            CoreNutsUtils.checkReadOnly(ws);
            ws.config().setBootConfig(bc);
            ws.io().copy().from(runtimeUpdate.getAvailableIdFile()).to(ws.config().getStoreLocation(runtimeUpdate.getAvailableId(), bootstrapFolder)
                    .resolve(ws.config().getDefaultIdFilename(runtimeUpdate.getAvailableId().setFaceComponent().setPackaging("jar")))
            ).run();
            NutsDescriptor runtimeDesc = ws.fetch().id(runtimeUpdate.getAvailableId()).getResultDescriptor();
            ws.formatter().createDescriptorFormat().pretty().print(runtimeDesc,
                    ws.config().getStoreLocation(runtimeUpdate.getAvailableId(), bootstrapFolder)
                            .resolve(ws.config().getDefaultIdFilename(runtimeUpdate.getAvailableId().setFaceDescriptor()))
            );
            for (NutsDependency dependency : runtimeDesc.getDependencies()) {
                if (dependency.getId().getSimpleNameId().equals(actualBootConfig.getApiId().getSimpleNameId())) {
                    Properties pr = new Properties();
                    pr.setProperty("project.id", dependency.getId().getSimpleNameId().toString());
                    pr.setProperty("project.name", dependency.getId().getSimpleNameId().toString());
                    pr.setProperty("project.version", dependency.getId().getVersion().toString());
                    pr.setProperty("repositories", "~/.m2/repository;https\\://raw.githubusercontent.com/thevpc/vpc-public-maven/master;http\\://repo.maven.apache.org/maven2/;https\\://raw.githubusercontent.com/thevpc/vpc-public-nuts/master");
                    pr.setProperty("bootRuntimeId", runtimeUpdate.getAvailableId().getLongName());
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
            pr.setProperty("project.id", runtimeUpdate.getAvailableId().getSimpleNameId().toString());
            pr.setProperty("project.name", runtimeUpdate.getAvailableId().getSimpleNameId().toString());
            pr.setProperty("project.version", runtimeUpdate.getAvailableId().getVersion().toString());
            final NutsId rtId = runtimeUpdate.getAvailableId();
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
                    ws.config().getStoreLocation(runtimeUpdate.getAvailableId().getLongNameId(), bootstrapFolder)
                            .resolve("nuts.properties")
            )) {
                pr.store(writer, "Updated on " + new Date());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        for (NutsWorkspaceUpdateResultItem extension : result.getExtensions()) {
            ws.extensions().updateExtension(extension.getAvailableId());
        }
        if (ws.config().save(false)) {
            if (log.isLoggable(Level.INFO)) {
                log.log(Level.INFO, "Workspace is updated. Nuts should be restarted for changes to take effect.");
            }
        }
    }

    public NutsWorkspaceUpdateResultItem checkUpdates(NutsId id, String bootApiVersion, NutsSession session) {
        session = CoreNutsUtils.validateSession(session, ws);
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
                v = "LATEST";
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
                for (NutsDefinition d : ws.find().addId(newFile.getId()).latestVersions()
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
            String sOldFile = oldFile == null ? null : oldFile.getContent().getPath().toString();
            String sNewFile = newFile.getContent() == null ? null : newFile.getContent().getPath().toString();
            return new NutsWorkspaceUpdateResultItem(id, oldId, newId, sOldFile, sNewFile, dependencies.toArray(new NutsId[0]), false);
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
}
