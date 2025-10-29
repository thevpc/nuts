package net.thevpc.nuts.runtime.standalone.workspace.cmd.install;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.runtime.standalone.event.DefaultNInstallEvent;
import net.thevpc.nuts.runtime.standalone.event.DefaultNUpdateEvent;
import net.thevpc.nuts.runtime.standalone.extension.NExtensionListHelper;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NExecutionContextBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.NRecommendationPhase;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.RequestQueryInfo;
import net.thevpc.nuts.runtime.standalone.workspace.config.ConfigEventType;
import net.thevpc.nuts.spi.NInstallerComponent;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.stream.Collectors;

public class InstallHelper {
    private DefaultNWorkspace ws;
    private InstallCache cache;
    protected NDefinition[] result;
    protected NId[] failed;
    protected List<String> args;
    protected List<AbstractNInstallCmd.ConditionalArguments> conditionalArguments;
    List<NDefinition> resultList = new ArrayList<>();
    List<NId> failedList = new ArrayList<>();
    protected final InstallIdList list;
    boolean updateMode;

    public InstallHelper(DefaultNWorkspace ws, InstallIdList list, boolean updateMode, List<String> args, List<AbstractNInstallCmd.ConditionalArguments> conditionalArguments) {
        this.ws = ws;
        this.cache = new InstallCache();
        this.list = list;
        this.updateMode = updateMode;
        this.args = args == null ? new ArrayList<>() : new ArrayList<>(args);
        this.conditionalArguments = conditionalArguments == null ? new ArrayList<>() : new ArrayList<>(conditionalArguments);
    }

    public InstallIdList getList() {
        return list;
    }

    public InstallIdCacheItem getCache(NId id) {
        return cache.get(id);
    }
    private Map<String, String> prepareInstallVars(NDefinition def) {
        Map<String, String> m = new HashMap<>();
        m.put("nutsIdContentPath", def.getContent().get().toString());
        for (NStoreType st : NStoreType.values()) {
            m.put("nutsId" + NNameFormat.TITLE_CASE.format(st.id()) + "Path", NPath.ofIdStore(def.getId(), st).toString());
        }
        return m;
    }

    private void ensureLoaded(InstallIdInfo info) {
        if (!info.loaded) {
            if (info.cacheItem == null) {
                info.cacheItem = cache.get(info.id);
            }
            if (info.flags.force || info.flags.repair) {
                info.cacheItem.revalidate(true);
            } else {
                info.cacheItem.getDefinition();
            }
            if (info.flags.install) {
                info.cacheItem.getEffectiveDescriptor();
                info.cacheItem.getDependencies();
            }
            info.loaded = true;
        }
    }

    private void _revisitRequirements(InstallIdList list) {
        for (InstallIdInfo info : list.infos()) {
            if (!info.ignored && info.doError == null) {
                ensureLoaded(info);
                if (info.flags.install) {
                    for (NDependency dependency : info.cacheItem.getDependencies()) {
                        InstallIdCacheItem c = cache.get(dependency.toId());
                        if (!dependency.isOptional() && c.optional) {
                            c.optional = false;
                        }
                        if (info.flags.deployOnly) {
                            ensureLoaded(list.addAsDeployed(c.id, info.flags));
                        } else {
                            ensureLoaded(list.addAsRequired(c.id, info.id, info.flags));
                        }
                    }
                }
            }
        }

    }

    public void installAll() {
        for (InstallIdInfo info : list.infos()) {
            if (info.cacheItem == null) {
                info.cacheItem = cache.get(info.id);
            }
            info.cacheItem.getOldInstallStatus();
            if (!info.cacheItem.getOldInstallStatus().isInstalled()) {
                if (info.flags.repair) {
                    info.doError = "cannot repair non installed package";
                } else if (info.flags.switchVersion) {
                    info.doError = "cannot repair non installed package";
                } else {
                    info.flags.install = true;
                    info.flags.update = updateMode;
                }
            } else if (info.cacheItem.getOldInstallStatus().isObsolete()) {
                info.flags.install = true;
            } else if (info.cacheItem.getOldInstallStatus().isInstalled()) {
                if (!info.flags.repair && !info.flags.force) {
                    info.ignored = true;
                } else {
                    info.flags.install = true;
                    info.flags.update = updateMode;
                }
            } else if (info.cacheItem.getOldInstallStatus().isRequired()) {
                if (info.flags.switchVersion) {
                    info.doError = "cannot switch version for non installed package";
                } else {
                    info.flags.install = true;
                    info.flags.update = updateMode;
                }
            } else {
                throw new NUnexpectedException(NMsg.ofC("unsupported status %s", info.cacheItem.getOldInstallStatus()));
            }
        }
        _revisitRequirements(list);
        Map<String, List<InstallIdInfo>> error = list.infos().stream().filter(x -> x.doError != null).collect(Collectors.groupingBy(installIdInfo -> installIdInfo.doError));
        if (error.size() > 0) {
            StringBuilder sb = new StringBuilder();
            NPrintStream out = NOut.out();
            for (Map.Entry<String, List<InstallIdInfo>> stringListEntry : error.entrySet()) {
                out.resetLine().println("the following " + (stringListEntry.getValue().size() > 1 ? "artifacts are" : "artifact is") + " cannot be ```error installed``` (" + stringListEntry.getKey() + ") : "
                        + stringListEntry.getValue().stream().map(x -> x.id)
                        .map(x -> NIdFormat.of().setOmitImportedGroupId(true).setValue(x.getLongId()).format().toString())
                        .collect(Collectors.joining(", ")));
                sb.append("\n" + "the following ").append(stringListEntry.getValue().size() > 1 ? "artifacts are" : "artifact is").append(" cannot be installed (").append(stringListEntry.getKey()).append(") : ").append(stringListEntry.getValue().stream().map(x -> x.id)
                        .map(x -> NIdFormat.of().setOmitImportedGroupId(true).setValue(x.getLongId()).format().toString())
                        .collect(Collectors.joining(", ")));
            }
            throw new NInstallException(null, NMsg.ofNtf(sb.toString().trim()), null);
        }
        NMemoryPrintStream mout = NMemoryPrintStream.of();
        List<NId> nonIgnored = list.ids(x -> !x.ignored);
        List<NId> list_new_installed = list.ids(x -> x.flags.install && !x.isAlreadyExists());
        List<NId> list_new_required = list.ids(x -> x.flags.require && !x.flags.install && !x.isAlreadyExists());
        List<NId> list_required_rerequired = list.ids(x -> (!x.flags.install && x.flags.require) && x.isAlreadyRequired());
        List<NId> list_required_installed = list.ids(x -> x.flags.install && x.isAlreadyRequired() && !x.isAlreadyInstalled());
        List<NId> list_required_reinstalled = list.ids(x -> x.flags.install && x.isAlreadyInstalled());
        List<NId> list_installed_setdefault = list.ids(x -> x.flags.switchVersion && x.isAlreadyInstalled());
        List<NId> installed_ignored = list.ids(x -> x.ignored);

        NSession session = NSession.of();
        if (!nonIgnored.isEmpty()) {
            if (session.isPlainTrace() || (!list.emptyCommand && session.getConfirm().orDefault() == NConfirmationMode.ASK)) {
                printList(mout, "new", "installed", list_new_installed);
                printList(mout, "new", "required", list_new_required);
                printList(mout, "required", "re-required", list_required_rerequired);
                printList(mout, "required", "installed", list_required_installed);
                printList(mout, "required", "re-reinstalled", list_required_reinstalled);
                printList(mout, "installed", "set as default", list_installed_setdefault);
                printList(mout, "installed", "ignored", installed_ignored);
            }
            if (!list_required_reinstalled.isEmpty() || !list_required_rerequired.isEmpty()) {
                mout.println("should we proceed re-installation?");
            } else {
                mout.println("should we proceed installation?");
            }
            if (!NIO.of().getDefaultTerminal().ask()
                    .forBoolean(NMsg.ofNtf(mout.toString()))
                    .setDefaultValue(true)
                    .setCancelMessage(
                            NMsg.ofC("installation cancelled : %s ", nonIgnored.stream().map(NId::getFullName).collect(Collectors.joining(", ")))
                    )
                    .getBooleanValue()) {
                throw new NCancelException(NMsg.ofC("installation cancelled: %s", nonIgnored.stream().map(NId::getFullName).collect(Collectors.joining(", "))));
            }
        } else if (!installed_ignored.isEmpty()) {
            //all packages are already installed, ask if we need to re-install!
            if (session.isPlainTrace() || (!list.emptyCommand && session.getConfirm().orDefault() == NConfirmationMode.ASK)) {
                printList(mout, "installed", "re-reinstalled", installed_ignored);
            }
            mout.println("should we proceed?");
            if (!NIO.of().getDefaultTerminal().ask()
                    .forBoolean(NMsg.ofNtf(mout.toString()))
                    .setDefaultValue(true)
                    .setCancelMessage(
                            NMsg.ofC("installation cancelled : %s ", nonIgnored.stream().map(NId::getFullName).collect(Collectors.joining(", ")))
                    )
                    .getBooleanValue()) {
                throw new NCancelException(NMsg.ofC("installation cancelled: %s", nonIgnored.stream().map(NId::getFullName).collect(Collectors.joining(", "))));
            }
            //force installation
            for (InstallIdInfo info : list.infos()) {
                if (info.ignored) {
                    info.ignored = false;
                    info.flags.install = true;
                    info.flags.force = true;
                }
            }
            _revisitRequirements(list);
        }
        try {
            for (InstallIdInfo info : list.infos(x -> !x.ignored)) {
                try {
                    if (doInstallOneImplUnsafe(info, list)) {
                        resultList.add(info.cacheItem.definition);
                    }
                } catch (RuntimeException ex) {
                    _LOG()
                            .log(NMsg.ofC("failed to install %s", info.id).asFine(ex)
                                    .withIntent(NMsgIntent.ALERT)
                            );
                    failedList.add(info.id);
                    if (session.isPlainTrace()) {
                        if (!NIO.of().getDefaultTerminal().ask()
                                .forBoolean(NMsg.ofC("%s %s and its dependencies... Continue installation?",
                                        NMsg.ofStyledError("failed to install"),
                                        info.id))
                                .setDefaultValue(true)
                                .getBooleanValue()) {
                            NOut.resetLine().println(NMsg.ofC("%s ```error installation cancelled with error:``` %s%n", info.id, ex));
                            result = new NDefinition[0];
                            return;
                        } else {
                            NOut.resetLine().println(NMsg.ofC("%s ```error installation cancelled with error:``` %s%n", info.id, ex));
                        }
                    } else {
                        throw ex;
                    }
                }
            }
        } finally {
            result = resultList.toArray(new NDefinition[0]);
            failed = failedList.toArray(new NId[0]);
        }
        if (list.emptyCommand) {
            throw new NExecutionException(NMsg.ofPlain("missing packages to install"), NExecutionException.ERROR_1);
        }
    }

    enum DoInstallOneImplSafeResult {
        ISTALLED,
        NON_INSTALLED,
        EXIT,
    }

    private DoInstallOneImplSafeResult doInstallOneImplSafe(InstallIdInfo info, InstallIdList list) {
        try {
            if (doInstallOneImplUnsafe(info, list)) {
                resultList.add(info.cacheItem.definition);
                return DoInstallOneImplSafeResult.ISTALLED;
            }
            return DoInstallOneImplSafeResult.NON_INSTALLED;
        } catch (RuntimeException ex) {
            _LOG()
                    .log(NMsg.ofC("failed to install %s", info.id).asFine(ex)
                            .withIntent(NMsgIntent.ALERT)
                    );
            failedList.add(info.id);
            if (NSession.of().isPlainTrace()) {
                if (!NIO.of().getDefaultTerminal().ask()
                        .forBoolean(NMsg.ofC("%s %s and its dependencies... Continue installation?",
                                NMsg.ofStyledError("failed to install"),
                                info.id))
                        .setDefaultValue(true)
                        .getBooleanValue()) {
                    NOut.resetLine().println(NMsg.ofC("%s ```error installation cancelled with error:``` %s%n", info.id, ex));
                    result = new NDefinition[0];
                    return DoInstallOneImplSafeResult.EXIT;
                } else {
                    NOut.resetLine().println(NMsg.ofC("%s ```error installation cancelled with error:``` %s%n", info.id, ex));
                }
            } else {
                throw ex;
            }
            return DoInstallOneImplSafeResult.EXIT;
        }
    }
//    private boolean doInstallOneImplUnsafe(InstallIdInfo info, InstallIdList list) {
//        NId id = info.id;
//        List<String> cmdArgs = new ArrayList<>(args);
//        NWorkspaceExt dws = NWorkspaceExt.of();
//        if (info.flags.install) {
//            _loadIdContent(info.id, null, true, list, info.flags, NDependencyUtils.isRequiredDependency(id.toDependency()));
//            for (AbstractNInstallCmd.ConditionalArguments conditionalArgument : conditionalArguments) {
//                if (conditionalArgument.getPredicate().test(info.cacheItem.getDefinition())) {
//                    cmdArgs.addAll(conditionalArgument.getArgs());
//                }
//            }
//            installImpl(info, cmdArgs.toArray(new String[0]), info.doSwitchVersion);
//            return true;
//        } else if (info.doRequire) {
//            _loadIdContent(info.id, null, true, list, info.flags, NDependencyUtils.isRequiredDependency(id.toDependency()));
//            dws.requireImpl(info, info.doRequireDependencies, new NId[0]);
//            return true;
//        } else if (info.flags.switchVersion) {
//            dws.getInstalledRepository().setDefaultVersion(info.id);
//            return true;
//        } else if (info.ignored) {
//            return false;
//        } else {
//            throw new NUnexpectedException(NMsg.ofPlain("unexpected"));
//        }
//    }
//    NDependencyFilter ndf = NDependencyFilters.of().byRunnable();
//            if (def.getEffectiveDescriptor().isNotPresent()
//                    || (!def.getDescriptor().isNoContent() && def.getContent().isNotPresent())) {
//        // reload def
//        NFetchCmd fetch2 = NFetchCmd.of(def.getId())
//                .setDependencyFilter(NDependencyFilters.of().byRunnable())
//                .setRepositoryFilter(NRepositoryFilters.of().installedRepo())
//                .failFast();
//        if (requireDependencies && def.getDependencies().isPresent()) {
//            fetch2.setDependencyFilter(def.getDependencies().get().filter());
//        }
//        def = fetch2.getResultDefinition();
//    }

    public boolean doInstallOneImplUnsafe(InstallIdInfo info, InstallIdList list) {
        if (info == null) {
            return false;
        }
        NDefinition def = info.cacheItem.getDefinition();
        if (def == null) {
            return false;
        }
        if (info.ignored) {
            return false;
        }
        if (info.flags.switchVersion) {
            ws.getInstalledRepository().setDefaultVersion(info.id);
            return true;
        }
        List<String> args = new ArrayList<>();
        if (info.flags.install) {
            args = buildArgs(info);
        }
        boolean resolveInstaller = info.resolveInstaller;
        fireEventBeforeInstall(def);
        NSession session = ws.getModel().workspace.currentSession();
        NPrintStream out = session.out();
        NInstallInformation newNInstallInformation = null;
        boolean remoteRepo = true;
        try {
            boolean reinstall = false;
            NInstalledRepository installedRepository = ws.getInstalledRepository();
            NWorkspaceUtils wu = NWorkspaceUtils.of(ws.getModel().workspace);

            if (session.isPlainTrace()) {
                NTexts text = NTexts.of();
                if (updateMode) {
                    NOut.resetLine().println(NMsg.ofC("%s %s ...",
                            text.ofStyled("update", NTextStyle.warn()),
                            def.getId().getLongId()
                    ));
                } else if (info.flags.require) {
                    reinstall = def.getInstallInformation().get().getInstallStatus().isRequired();
                    if (reinstall) {
                        //NOut.println("re-requiring  " + id().formatter().set(def.getId().getLongNameId()).format() + " ...");
                    } else {
                        //session.out().println("requiring  " + id().formatter().set(def.getId().getLongNameId()).format() + " ...");
                    }
                } else {
                    reinstall = def.getInstallInformation().get().getInstallStatus().isInstalled();
                    if (reinstall) {
                        session.out().resetLine().println(NMsg.ofC(
                                "%s %s ...",
                                text.ofStyled("re-install", NTextStyles.of(NTextStyle.success(), NTextStyle.underlined())),
                                def.getId().getLongId()
                        ));
                    } else {
                        session.out().resetLine().println(NMsg.ofC("%s %s ...",
                                text.ofStyled("install", NTextStyle.success()),
                                def.getId().getLongId()
                        ));
                    }
                }
            }
            if (reinstall) {
                if (!info.flags.require) {
                    if (def.getInstallInformation().get().getInstallStatus().isInstalled()) {
                        info.cacheItem.revalidate(false);
                        uninstallImpl(info, resolveInstaller, true, false, false);
                    }
                }
            }
            info.oldDef = reloadOldDef(info);
            out.flush();
            if (def.getContent().isPresent() || def.getDescriptor().isNoContent()) {
                //should change def to reflect install location!
                NExecutionContextBuilder cc = ws.createExecutionContext()
                        .setDefinition(def).setArguments(args.toArray(new String[0])).failFast().setTemporary(false)
                        .setRunAs(NRunAs.currentUser())// install or update always uses current user
                        ;
                NArtifactCall installer = def.getDescriptor().getInstaller();
                if (installer != null) {
                    String scriptName = installer.getScriptName();
                    String scriptContent = installer.getScriptContent();
                    NPath installScriptPath = null;
                    if (!NBlankable.isBlank(scriptName) && !NBlankable.isBlank(scriptContent)) {
                        installScriptPath = NPath.ofTempIdFile(scriptName, def.getId());
                    }
                    Map<String, String> installVars = prepareInstallVars(def);
                    if (installScriptPath != null) {
                        installScriptPath.writeString(scriptContent == null ? "" : scriptContent);
                        installVars.put("nutsIdInstallScriptPath", installScriptPath.toString());
                    }

                    // all vars are replicated as environment vars
                    Map<String, String> installEnv = installVars.entrySet().stream().map(x -> {
                        return new AbstractMap.SimpleImmutableEntry<>(
                                NNameFormat.CONST_NAME.format(x.getKey())
                                , x.getValue());
                    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    //accept both namings...
                    installEnv.putAll(installVars);
                    cc.setEnv(installEnv);
                    cc.addExecutorOptions(
                            installer.getArguments()
                                    .stream().map(x -> NMsg.ofV(x, installVars
                                    ).toString()).collect(Collectors.toList())
                    );
                }
                NExecutionContext executionContext = cc.build();
                if (updateMode || info.flags.install) {
                    newNInstallInformation = installedRepository.deploy(executionContext.getDefinition());
                    newNInstallInformation = installedRepository.install(executionContext.getDefinition());
                    if (info.flags.require){
                        newNInstallInformation = installedRepository.require(executionContext.getDefinition(),  info.requiredForIds.toArray(new NId[0]), null);
                    }
                } else if (info.flags.require) {
                    newNInstallInformation = installedRepository.deploy(executionContext.getDefinition());
                    newNInstallInformation = installedRepository.require(executionContext.getDefinition(),  info.requiredForIds.toArray(new NId[0]), null);
                } else if (info.flags.deployOnly) {
                    newNInstallInformation = installedRepository.deploy(executionContext.getDefinition());
                    newNInstallInformation = installedRepository.deploy(executionContext.getDefinition());
                }
                if (info.flags.switchVersion) {
                    installedRepository.setDefaultVersion(def.getId());
                }

                //now should reload definition from install repo
                NFetchCmd fetch2 = NFetchCmd.of(executionContext.getDefinition().getId())
                        .setDependencyFilter(NDependencyFilters.of().byRunnable())
                        .setRepositoryFilter(NRepositoryFilters.of().installedRepo())
                        .failFast();
                if (def.getDependencies().isPresent()
                        && def.getDependencies().get().filter() != null
                ) {
                    fetch2.setDependencyFilter(def.getDependencies().get().filter());
                }
                //update definition in the execution context
                NDefinition defOnInstallRepo = fetch2.getResultDefinition();
                cc.setDefinition(defOnInstallRepo);
                executionContext = cc.build();
                NRepository rep = ws.findRepository(def.getRepositoryUuid()).orNull();
                remoteRepo = rep == null || rep.isRemote();
                if (updateMode) {
                    NInstallerComponent installerComponent = null;
                    if (resolveInstaller) {
                        installerComponent = ws.getInstaller(def);
                    }
                    RuntimeException updateError = null;
                    if (installerComponent != null) {
                        try {
                            installerComponent.update(executionContext);
                        } catch (NReadOnlyException ex) {
                            throw ex;
                        } catch (Exception ex) {
                            if (session.isPlainTrace()) {
                                out.resetLine().println(NMsg.ofC("%s ```error failed``` to update : %s.", def.getId(), ex));
                            }
                            updateError = new NExecutionException(
                                    NMsg.ofC("unable to update %s", def.getId()),
                                    ex);
                        }
                    }
                    ws.getModel().recomm.trackRecommendationsAsync(new RequestQueryInfo(defOnInstallRepo.getId().toString(), updateError), NRecommendationPhase.UPDATE, updateError != null);
                    if (updateError != null) {
                        throw updateError;
                    }
                } else if (info.flags.install) {
                    NInstallerComponent installerComponent = null;
                    if (resolveInstaller) {
                        installerComponent = ws.getInstaller(def);
                    }
                    if (installerComponent != null) {
                        RuntimeException updateError = null;
                        try {
                            installerComponent.install(executionContext);
                        } catch (NReadOnlyException ex) {
                            throw ex;
                        } catch (RuntimeException ex) {
                            if (session.isPlainTrace()) {
                                out.resetLine().println(NMsg.ofC("```error error: failed to install``` %s: %s.", def.getId(), ex));
                            }
                            try {
                                installedRepository.uninstall(executionContext.getDefinition());
                            } catch (Exception ex2) {
                                ws.getModel().LOG
                                        .log(NMsg.ofC("failed to uninstall  %s", executionContext.getDefinition().getId()).asFine(ex));
                                //ignore if we could not uninstall
                            }
                            updateError = new NExecutionException(NMsg.ofC("unable to install %s", def.getId()), ex);
                        }
                        ws.getModel().recomm.trackRecommendationsAsync(new RequestQueryInfo(def.getId().toString(), updateError), NRecommendationPhase.INSTALL, updateError != null);
                    }
                }
            } else {
                throw new NExecutionException(
                        NMsg.ofC("unable to install %s: unable to locate content", def.getId()),
                        NExecutionException.ERROR_2);
            }

            switch (def.getDescriptor().getIdType()) {
                case API: {
                    ws.getModel().configModel.prepareBootClassPathConf(NIdType.API, def.getId(),
                            null
                            , null, true, false);
                    break;
                }
                case RUNTIME:
                case EXTENSION: {
                    ws.getModel().configModel.prepareBootClassPathConf(
                            def.getDescriptor().getIdType(),
                            def.getId(),
                            null
                            , null, true, true);
                    break;
                }
            }
            if (updateMode) {
                wu.events().fireOnUpdate(new DefaultNUpdateEvent(info.oldDef, def, session, reinstall));
            } else if (info.flags.install) {
                wu.events().fireOnInstall(new DefaultNInstallEvent(def, session, new NId[0], reinstall));
            } else if (info.flags.require) {
                wu.events().fireOnRequire(new DefaultNInstallEvent(def, session, info.forIds.toArray(new NId[0]), reinstall));
            }
            if (def.getDescriptor().getIdType() == NIdType.EXTENSION) {
                NExtensionListHelper h = new NExtensionListHelper(
                        session.getWorkspace().getApiId(),
                        ws.getConfigModel().getStoredConfigBoot().getExtensions())
                        .save();
                NDependencies nDependencies = null;
                if (!def.getDependencies().isPresent()) {
                    nDependencies = NFetchCmd.of(def.getId())
                            .setDependencyFilter(NDependencyFilters.of().byRunnable())
                            .getResultDefinition().getDependencies().get();
                } else {
                    nDependencies = def.getDependencies().get();
                }
                h.add(def.getId(), nDependencies.transitiveWithSource().toList());
                ws.getConfigModel().getStoredConfigBoot().setExtensions(h.getConfs());
                ws.getConfigModel().fireConfigurationChanged("extensions", ConfigEventType.BOOT);
            }
        } catch (RuntimeException ex) {
            NDefinition finalDef2 = def;
            ws.getModel().recomm.trackRecommendationsAsync(new RequestQueryInfo(finalDef2.getId().toString(), ex), info.flags.update ? NRecommendationPhase.UPDATE : NRecommendationPhase.INSTALL, true);
            throw ex;
        }
        if (session.isPlainTrace()) {
            String setAsDefaultString = "";
            NTexts text = NTexts.of();
            if (info.flags.switchVersion) {
                setAsDefaultString = " set as " + text.ofBuilder().append("default", NTextStyle.primary1()) + ".";
            }
            if (newNInstallInformation != null
                    && (newNInstallInformation.isJustInstalled()
                    || newNInstallInformation.isJustRequired())) {
                NText installedString = null;
                if (newNInstallInformation != null) {
                    if (newNInstallInformation.isJustReInstalled()) {
                        installedString = text.ofStyled("re-install", NTextStyles.of(NTextStyle.success(), NTextStyle.underlined()));
                    } else if (newNInstallInformation.isJustInstalled()) {
                        installedString = text.ofStyled("install", NTextStyle.success());
                    } else if (newNInstallInformation.isJustReRequired()) {
                        installedString = text.ofStyled("re-require", NTextStyles.of(NTextStyle.info(), NTextStyle.underlined()));
                    } else if (newNInstallInformation.isJustRequired()) {
                        installedString = text.ofStyled("require", NTextStyle.info());
                    }
                }
                if (installedString != null) {
                    //(reinstalled ? "re-installed" : "installed")
                    if (def.getContent().isNotPresent()) {
                        //this happens when deploying a 'pom' artifact
                        if (session.isPlainTrace()) {
                            out.resetLine().println(NMsg.ofC("%s %s from %s repository (%s).%s",
                                    installedString,
                                    def.getId().getLongId(),
                                    remoteRepo ? "remote" : "local",
                                    def.getRepositoryName(),
                                    text.of(setAsDefaultString)
                            ));
                        }
                    } else if (!def.getContent().get().isUserCache()) {
                        if (def.getContent().get().isUserTemporary()) {
                            if (session.isPlainTrace()) {
                                out.resetLine().println(NMsg.ofC("%s %s from %s repository (%s) temporarily file %s.%s",
                                        installedString,
                                        def.getId().getLongId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        def.getContent().orNull(),
                                        text.of(setAsDefaultString)
                                ));
                            }
                        } else {
                            if (session.isPlainTrace()) {
                                out.resetLine().println(NMsg.ofC("%s %s from %s repository (%s).%s", installedString,
                                        def.getId().getLongId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        text.of(setAsDefaultString)));
                            }
                        }
                    } else {
                        if (def.getContent().get().isUserTemporary()) {
                            if (session.isPlainTrace()) {
                                out.resetLine().println(NMsg.ofC("%s %s from %s repository (%s) temporarily file %s.%s",
                                        installedString,
                                        def.getId().getLongId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        def.getContent().orNull(),
                                        text.of(setAsDefaultString)));
                            }
                        } else {
                            if (session.isPlainTrace()) {
                                out.resetLine().println(NMsg.ofC("%s %s from %s repository (%s).%s",
                                        installedString,
                                        def.getId().getLongId(),
                                        remoteRepo ? "remote" : "local",
                                        def.getRepositoryName(),
                                        text.of(setAsDefaultString)
                                ));
                            }
                        }
                    }
                }
            } else {
                String installedString = null;
                if (newNInstallInformation != null) {
                    if (newNInstallInformation.isJustReInstalled()) {
                        installedString = "re-installed";
                    } else if (newNInstallInformation.isJustInstalled()) {
                        installedString = "installed";
                    } else if (newNInstallInformation.isJustReRequired()) {
                        installedString = "re-required";
                    } else if (newNInstallInformation.isJustRequired()) {
                        installedString = "required";
                    }
                }
                if (installedString != null) {
                    if (session.isPlainTrace()) {
                        out.resetLine().println(NMsg.ofC("%s  %s %s.%s",
                                installedString,
                                def.getId().getLongId(),
                                text.ofStyled("successfully", NTextStyle.success()),
                                text.of(setAsDefaultString)
                        ));
                    }
                }
            }
        }
        return true;
    }

    private List<String> buildArgs(InstallIdInfo info) {
        List<String> cmdArgs = new ArrayList<>();
        cmdArgs.addAll(args);
        for (AbstractNInstallCmd.ConditionalArguments conditionalArgument : conditionalArguments) {
            if (conditionalArgument.getPredicate().test(info.cacheItem.getDefinition())) {
                cmdArgs.addAll(conditionalArgument.getArgs());
            }
        }
        return cmdArgs;
    }

    private NDefinition reloadOldDef(InstallIdInfo info) {
        NDefinition def = info.cacheItem.getDefinition();
        NDefinition oldDef = null;
        if (updateMode) {
            switch (def.getDescriptor().getIdType()) {
                case API: {
                    oldDef = NFetchCmd.of(NId.getApi(Nuts.getVersion()).get())
                            .setDependencyFilter(NDependencyFilters.of().byRunnable())
                            .setFetchStrategy(NFetchStrategy.ONLINE)
                            .setFailFast(false).getResultDefinition();
                    break;
                }
                case RUNTIME: {
                    oldDef = NFetchCmd.of(ws.getRuntimeId())
                            .setDependencyFilter(NDependencyFilters.of().byRunnable())
                            .setFetchStrategy(NFetchStrategy.ONLINE)
                            .setFailFast(false).getResultDefinition();
                    break;
                }
                default: {
                    oldDef = NSearchCmd.of().addId(def.getId().getShortId())
                            .setDependencyFilter(NDependencyFilters.of().byRunnable())
                            .setDefinitionFilter(NDefinitionFilters.of().byDeployed(true))
                            .setFailFast(false).getResultDefinitions()
                            .findFirst().orNull();
                    break;
                }
            }
        }
        return oldDef;
    }

    private void fireEventBeforeInstall(NDefinition def) {
        try {
            NDefinition finalDef = def;
            ws.runWith(() -> {
                new Thread(() -> {
                    Map rec = null;
                    if (updateMode) {
                        rec = ws.getModel().recomm.getRecommendations(new RequestQueryInfo(finalDef.getId().toString()), NRecommendationPhase.UPDATE, false);
                    } else {
                        rec = ws.getModel().recomm.getRecommendations(new RequestQueryInfo(finalDef.getId().toString()), NRecommendationPhase.INSTALL, false);
                    }
                }).start();
            });

            //TODO: should check here for any security issue!
        } catch (Exception ex2) {
            //just ignore
        }
    }


    private void printList(NPrintStream out, String skind, String saction, List<NId> all) {
        if (!all.isEmpty()) {
            NSession session = NSession.of();
            if (NOut.isPlain()) {
                NTexts text = NTexts.of();
                NText kind = text.ofStyled(skind, NTextStyle.primary2());
                NText action =
                        text.ofStyled(saction,
                                saction.equals("set as default") ? NTextStyle.primary3() :
                                        saction.equals("ignored") ? NTextStyle.pale() :
                                        NTextStyle.primary1()
                        );
                NTextBuilder msg = NTextBuilder.of();
                msg.append("the following ")
                        .append(kind).append(" ").append((all.size() > 1 ? "artifacts are" : "artifact is"))
                        .append(" going to be ").append(action).append(" : ")
                        .appendJoined(
                                NText.ofPlain(", "),
                                all.stream().map(x
                                                -> NText.of(
                                                x.builder().build()
                                        )
                                ).collect(Collectors.toList())
                        );
                out.resetLine().println(msg);
            } else {
                session.eout().add(NElement.ofObjectBuilder()
                        .set("command", "warning")
                        .set("artifact-kind", skind)
                        .set("action-warning", saction)
                        .set("artifacts", NElement.ofArrayBuilder().addAll(
                                all.stream().map(x -> x.toString()).toArray(String[]::new)
                        ).build())
                        .build()
                );
            }
        }
    }

    protected NLog _LOG() {
        return NLog.of(getClass());
    }


    public void uninstallImpl(InstallIdInfo def,
                              boolean runInstaller,
                              boolean deleteFiles,
                              boolean eraseFiles,
                              boolean traceBeforeEvent) {
        NPrintStream out = CoreIOUtils.resolveOut();
        NDefinition definition = def.cacheItem.getDefinition();
        if (runInstaller) {
            NInstallerComponent installerComponent = ws.getInstaller(definition);
            if (installerComponent != null) {
                NExecutionContext executionContext = ws.createExecutionContext()
                        .setDefinition(definition)
                        .setArguments(buildArgs(def).toArray(new String[0]))
                        .failFast()
                        .setTemporary(false)
                        .setRunAs(NRunAs.currentUser())//uninstall always uses current user
                        .build();
                installerComponent.uninstall(executionContext, eraseFiles);
            }
        }

        ws.getInstalledRepository().uninstall(definition);
        NId id = definition.getId();
        if (deleteFiles) {
            if (ws.getLocationModel().getStoreLocation(id, NStoreType.BIN).exists()) {
                ws.getLocationModel().getStoreLocation(id, NStoreType.BIN).deleteTree();
            }
            if (ws.getLocationModel().getStoreLocation(id, NStoreType.LIB).exists()) {
                ws.getLocationModel().getStoreLocation(id, NStoreType.LIB).deleteTree();
            }
            if (ws.getLocationModel().getStoreLocation(id, NStoreType.LOG).exists()) {
                ws.getLocationModel().getStoreLocation(id, NStoreType.LOG).deleteTree();
            }
            if (ws.getLocationModel().getStoreLocation(id, NStoreType.CACHE).exists()) {
                ws.getLocationModel().getStoreLocation(id, NStoreType.CACHE).deleteTree();
            }
            if (eraseFiles) {
                if (ws.getLocationModel().getStoreLocation(id, NStoreType.VAR).exists()) {
                    ws.getLocationModel().getStoreLocation(id, NStoreType.VAR).deleteTree();
                }
                if (ws.getLocationModel().getStoreLocation(id, NStoreType.CONF).exists()) {
                    ws.getLocationModel().getStoreLocation(id, NStoreType.CONF).deleteTree();
                }
            }
        }

        if (definition.getDescriptor().getIdType() == NIdType.EXTENSION) {
            NExtensionListHelper h = new NExtensionListHelper(
                    ws.getApiId(),
                    ws.getConfigModel().getStoredConfigBoot().getExtensions())
                    .save();
            h.remove(id);
            ws.getConfigModel().getStoredConfigBoot().setExtensions(h.getConfs());
            ws.getConfigModel().fireConfigurationChanged("extensions", ConfigEventType.BOOT);
        }
        if (traceBeforeEvent && NSession.of().isPlainTrace()) {
            out.println(NMsg.ofC("%s uninstalled %s", id, NText.ofStyled(
                    "successfully", NTextStyle.success()
            )));
        }
        NWorkspaceUtils.of(ws.getModel().workspace).events().fireOnUninstall(new DefaultNInstallEvent(definition, NSession.of(), new NId[0], eraseFiles));
    }
}
