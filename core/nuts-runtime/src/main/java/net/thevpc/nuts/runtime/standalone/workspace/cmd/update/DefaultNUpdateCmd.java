/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.update;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NIteratorUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNUpdateResult;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NComparator;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NAsk;
import net.thevpc.nuts.util.NStringUtils;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNUpdateCmd extends AbstractNUpdateCmd {

    private final NComparator<NId> LATEST_VERSION_FIRST = new NComparator<NId>() {
        @Override
        public int compare(NId x, NId y) {
            return -x.getVersion().compareTo(y.getVersion());
        }

        @Override
        public NElement describe() {
            return NElements.of().ofString("latestVersionFirst");
        }
    };
    private final NComparator<NId> DEFAULT_THEN_LATEST_VERSION_FIRST = new NComparator<NId>() {
        @Override
        public int compare(NId x, NId y) {
            NInstalledRepository rr = NWorkspaceExt.of().getInstalledRepository();
            int xi = rr.isDefaultVersion(x) ? 0 : 1;
            int yi = rr.isDefaultVersion(y) ? 0 : 1;
            int v = Integer.compare(xi, yi);
            if (v != 0) {
                return v;
            }
            return -x.getVersion().compareTo(y.getVersion());
        }

        @Override
        public NElement describe() {
            return NElements.of().ofString("defaultThenLatestVersionFirst");
        }
    };
    private boolean checkFixes = false;
    private List<FixAction> resultFixes = null;

    public DefaultNUpdateCmd(NWorkspace workspace) {
        super(workspace);
    }

    @Override
    public int getResultCount() {
        return getResult().getUpdatesCount();
    }

    @Override
    public NWorkspaceUpdateResult getResult() {
        if (result == null) {
            checkUpdates();
        }
        if (result == null) {
            throw new NUnexpectedException();
        }
        return result;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch (a.key()) {
            case "--check-fixes": {
                cmdLine.skip();
                if (enabled) {
                    this.checkFixes = true;
                }
                return true;
            }
        }
        return super.configureFirst(cmdLine);
    }

    @Override
    public NUpdateCmd update() {
        applyResult(getResult());
        return this;
    }

    @Override
    public NUpdateCmd checkUpdates() {
        if (checkFixes) {
            checkFixes();
            traceFixes();
        }
        Instant now = expireTime == null ? Instant.now() : expireTime;
        NWorkspaceExt dws = NWorkspaceExt.of();
//        NutsWorkspaceCurrentConfig actualBootConfig = ws.config().current();
//        NutsWorkspaceCurrentConfig jsonBootConfig = getConfigManager().getBootContext();
        Map<String, NUpdateResult> allUpdates = new LinkedHashMap<>();
        Map<String, NUpdateResult> extUpdates = new LinkedHashMap<>();
        Map<String, NUpdateResult> regularUpdates = new HashMap<>();
        NUpdateResult apiUpdate = null;
        NVersion bootVersion0 = workspace.getApiVersion();
        NVersion bootVersion = bootVersion0;
        if (!(this.getApiVersion() == null || this.getApiVersion().isBlank())) {
            bootVersion = this.getApiVersion();
        }
        if (this.isApi() || !(this.getApiVersion() == null || this.getApiVersion().isBlank())) {
            apiUpdate = checkCoreUpdate(NId.get(NConstants.Ids.NUTS_API).get(), this.getApiVersion(), Type.API, now);
            if (apiUpdate.isUpdatable()) {
                bootVersion = apiUpdate.getAvailable().getId().getVersion();
                allUpdates.put(NConstants.Ids.NUTS_API, apiUpdate);
            } else {
                //reset bootVersion
                bootVersion = bootVersion0;
            }
        }
        NUpdateResult runtimeUpdate = null;
        if (this.isRuntime()) {
            if (dws.requiresRuntimeExtension()) {
                runtimeUpdate = checkCoreUpdate(NId.get(workspace.getRuntimeId().getShortName()).get(),
                        apiUpdate != null && apiUpdate.getAvailable() != null && apiUpdate.getAvailable().getId() != null ? apiUpdate.getAvailable().getId().getVersion()
                                : bootVersion, Type.RUNTIME, now);
                if (runtimeUpdate.isUpdatable()) {
                    allUpdates.put(runtimeUpdate.getId().getShortName(), runtimeUpdate);
                }
            }
        }

        if (this.isExtensions()) {
            for (NId d : getExtensionsToUpdate()) {
                NUpdateResult updated = checkRegularUpdate(d, Type.EXTENSION, bootVersion, now, expireTime != null);
                if (updated.isUpdatable()) {
                    allUpdates.put(updated.getId().getShortName(), updated);
                    extUpdates.put(updated.getId().getShortName(), updated);
                }
            }
        }

        if (this.isCompanions()) {
            for (NId d : getCompanionsToUpdate()) {
                NUpdateResult updated = checkRegularUpdate(d, Type.COMPANION, bootVersion, now, expireTime != null);
                if (updated.isUpdatable()) {
                    allUpdates.put(updated.getId().getShortName(), updated);
                    regularUpdates.put(updated.getId().getShortName(), updated);
                }
            }
        }

        for (NId id : this.getRegularIds()) {
            NUpdateResult updated = checkRegularUpdate(id, Type.REGULAR, null, now, expireTime != null);
            allUpdates.put(updated.getId().getShortName(), updated);
            regularUpdates.put(updated.getId().getShortName(), updated);
        }
        List<NId> lockedIds = this.getLockedIds();
        if (lockedIds.size() > 0) {
            for (NId d : new HashSet<>(lockedIds)) {
                NDependency dd = NDependency.get(d.toString()).get();
                if (regularUpdates.containsKey(dd.getSimpleName())) {
                    NUpdateResult updated = regularUpdates.get(dd.getSimpleName());
                    //FIX ME
                    if (!dd.getVersion().filter().acceptVersion(updated.getId().getVersion())) {
                        throw new NIllegalArgumentException(
                                NMsg.ofC("%s unsatisfied  : %s", dd, updated.getId().getVersion())
                        );
                    }
                }
            }
        }

        result = new DefaultNWorkspaceUpdateResult(apiUpdate, runtimeUpdate, new ArrayList<>(extUpdates.values()),
                new ArrayList<>(regularUpdates.values())
        );
        traceUpdates(result);
        return this;
    }

    private Set<NId> getExtensionsToUpdate() {
        Set<NId> ext = new HashSet<>();
        for (NId extension : NExtensions.of().getConfigExtensions()) {
            ext.add(extension.getShortId());
        }
        if (updateExtensions) {
            return ext;
        } else {
            Set<NId> ext2 = new HashSet<>();
            for (NId id : ids) {
                if (id.getShortName().equals(NConstants.Ids.NUTS_API)) {
                    continue;
                }
                if (id.getShortName().equals(workspace.getRuntimeId().getShortName())) {
                    continue;
                }
                if (ext.contains(id.getShortId())) {
                    ext2.add(id.getShortId());
                }

            }
            return ext2;
        }
    }

    private Set<NId> getCompanionsToUpdate() {
        Set<NId> ext = new HashSet<>();
        for (NId extension : NExtensions.of().getCompanionIds()) {
            ext.add(extension.getShortId());
        }
        return ext;
    }

    private Set<NId> getRegularIds() {
        HashSet<String> extensions = new HashSet<>();
        for (NId object : NExtensions.of().getConfigExtensions()) {
            extensions.add(object.getShortName());
        }

        HashSet<NId> baseRegulars = new HashSet<>(ids);
        if (isInstalled()) {
            baseRegulars.addAll(NSearchCmd.of()
                    .setInstallStatus(NInstallStatusFilters.of().byInstalled(true))
                    .getResultIds().stream().map(NId::getShortId).collect(Collectors.toList()));
            // This bloc is to handle packages that were installed by their jar/content but was removed for any reason!
            NWorkspaceExt dws = NWorkspaceExt.of();
            NInstalledRepository ir = dws.getInstalledRepository();
            for (NInstallInformation y : NIteratorUtils.toList(ir.searchInstallInformation())) {
                if (y != null && y.getInstallStatus().isInstalled() && y.getId() != null) {
                    baseRegulars.add(y.getId().builder().setVersion("").build());
                }
            }
        }
        HashSet<NId> regulars = new HashSet<>();
        for (NId id : baseRegulars) {
            if (id.getShortName().equals(NConstants.Ids.NUTS_API)) {
                continue;
            }
            if (id.getShortName().equals(workspace.getRuntimeId().getShortName())) {
                continue;
            }
            if (extensions.contains(id.getShortName())) {
                continue;
            }
            regulars.add(id);
        }
        return regulars;
    }

    public NUpdateCmd checkFixes() {
        resultFixes = null;
        NWorkspaceExt dws = NWorkspaceExt.of();
        NInstalledRepository ir = dws.getInstalledRepository();
        resultFixes = NIteratorUtils.toList(NIteratorUtils.convertNonNull(ir.searchInstallInformation(), new Function<NInstallInformation, FixAction>() {
            @Override
            public FixAction apply(NInstallInformation nInstallInformation) {
                NId id = NSearchCmd.of().setInstallStatus(
                                NInstallStatusFilters.of().byInstalled(true)
                        ).addId(nInstallInformation.getId()).getResultIds()
                        .findFirst().orNull();
                if (id == null) {
                    return new FixAction(nInstallInformation.getId(), "MissingInstallation") {
                        @Override
                        public void fix(NSession session) {
                            NInstallCmd.of().addId(getId()).run();
                        }
                    };
                }
                return null;
            }
        }, "CheckFixes"));
        return this;
    }

    protected void traceFixes() {
        if (resultFixes != null) {
            NSession session = getWorkspace().currentSession();
            NPrintStream out = session.out();
            for (FixAction n : resultFixes) {
                out.println(NMsg.ofC("[```error FIX```] %s %s", n.getId(), n.getProblemKey()));
            }
        }
    }

    protected void traceUpdates(NWorkspaceUpdateResult result) {
        NSession session = getWorkspace().currentSession();
        NPrintStream out = session.out();
        List<NUpdateResult> all = result.getAllResults();
        List<NUpdateResult> updates = result.getUpdatable();
        List<NUpdateResult> notInstalled = result.getAllResults().stream()
                .filter(x -> !x.isInstalled()).collect(Collectors.toList());
        all.sort(new Comparator<NUpdateResult>() {
            private int itemOrder(NUpdateResult o) {
                if (!o.isInstalled()) {
                    return 1;
                }
                if (!o.isUpdateVersionAvailable()) {
                    return 2;
                }
                if (!o.isUpdateStatusAvailable()) {
                    return 3;
                }
                return 4;
            }

            @Override
            public int compare(NUpdateResult o1, NUpdateResult o2) {
                return Integer.compare(itemOrder(o1), itemOrder(o2));
            }
        });
        if (session.isPlainTrace()) {
            if (notInstalled.size() == 0 && updates.size() == 0) {
                out.resetLine().println(NMsg.ofC("all packages are %s. You are running latest version%s.",
                        NText.ofStyledSuccess("up-to-date"),
                        result.getAllResults().size() > 1 ? "s" : ""));
            } else {
                if (updates.size() > 0 && notInstalled.size() > 0) {
                    out.resetLine().println(NMsg.ofC("workspace has %s package%s not installed and %s package%s to update.",
                            NText.ofStyledPrimary1("" + notInstalled.size()),
                            (notInstalled.size() > 1 ? "s" : ""),
                            NText.ofStyledPrimary1("" + updates.size()),
                            (updates.size() > 1 ? "s" : "")
                    ));
                } else if (updates.size() > 0) {
                    out.resetLine().println(NMsg.ofC("workspace has %s package%s to update.", NText.ofStyledPrimary1("" + updates.size()),
                            (updates.size() > 1 ? "s" : "")));
                } else if (notInstalled.size() > 0) {
                    out.resetLine().println(NMsg.ofC("workspace has %s package%s not installed.", NText.ofStyledPrimary1("" + notInstalled.size()),
                            (notInstalled.size() > 1 ? "s" : "")));
                }
                int widthCol1 = 2;
                int widthCol2 = 2;
                for (NUpdateResult update : all) {
                    widthCol1 = Math.max(widthCol1, update.getAvailable() == null ? 0 : update.getAvailable().getId().getShortName().length());
                    widthCol2 = Math.max(widthCol2, update.getInstalled() == null ? 0 : update.getInstalled().getId().getVersion().toString().length());
                }
                NTexts factory = NTexts.of();
                for (NUpdateResult update : all) {
                    if (update.getInstalled() == null) {
                        out.println(NMsg.ofC("%s  : %s",
                                factory.ofStyled(NStringUtils.formatAlign(update.getId().toString(), widthCol2, NPositionType.FIRST), NTextStyle.primary6()),
                                factory.ofStyled("not installed", NTextStyle.error())));
                    } else if (update.isUpdateVersionAvailable()) {
                        out.println(NMsg.ofC("%s  : %s => %s",
                                factory.ofStyled(NStringUtils.formatAlign(update.getInstalled().getId().getVersion().toString(), widthCol2, NPositionType.FIRST), NTextStyle.primary6()),
                                NStringUtils.formatAlign(update.getAvailable().getId().getShortName(), widthCol1, NPositionType.FIRST),
                                factory.ofPlain(update.getAvailable().getId().getVersion().toString())));
                    } else if (update.isUpdateStatusAvailable()) {
                        out.println(NMsg.ofC("%s  : %s => %s",
                                factory.ofStyled(NStringUtils.formatAlign(update.getInstalled().getId().getVersion().toString(), widthCol2, NPositionType.FIRST), NTextStyle.primary6()),
                                NStringUtils.formatAlign(update.getAvailable().getId().getShortName(), widthCol1, NPositionType.FIRST),
                                factory.ofStyled("set as default", NTextStyle.primary4())));
                    } else {
                        out.println(NMsg.ofC("%s  : %s",
                                factory.ofStyled(NStringUtils.formatAlign(update.getInstalled().getId().getVersion().toString(), widthCol2, NPositionType.FIRST), NTextStyle.primary6()),
                                factory.ofStyled("up-to-date", NTextStyle.warn())));
                    }
                }
            }
        } else {
            NElements e = NElements.of();

            if (updates.size() == 0 && notInstalled.size() == 0) {
                out.println(e.ofObject()
                        .set("message", "all packages are up-to-date. You are running latest version" + (result.getAllResults().size() > 1 ? "s" : "") + ".")
                        .build());
            } else {
                NArrayElementBuilder arrayElementBuilder = e.ofArray();
                for (NUpdateResult update : all) {
                    if (update.getInstalled() == null) {
                        arrayElementBuilder.add(e.ofObject()
                                .set("package", update.getId().getShortName())
                                .set("status", "not-installed")
                                .build());
                    } else if (update.isUpdateVersionAvailable()) {
                        arrayElementBuilder.add(e.ofObject()
                                .set("package", update.getAvailable().getId().getShortName())
                                .set("status", "update-version-available")
                                .set("localVersion", update.getInstalled().getId().getVersion().toString())
                                .set("newVersion", update.getAvailable().getId().getVersion().toString())
                                .build());
                    } else if (update.isUpdateStatusAvailable()) {
                        arrayElementBuilder.add(e.ofObject()
                                .set("package", update.getAvailable().getId().getShortName())
                                .set("localVersion", update.getInstalled().getId().getVersion().toString())
                                .set("status", "update-default-available")
                                .set("newVersion", "set as default")
                                .build());
                    } else {
                        arrayElementBuilder.add(e.ofObject()
                                .set("package", update.getId().getShortName())
                                .set("localVersion", update.getInstalled().getId().getVersion().toString())
                                .set("status", "up-to-date")
                                .build());
                    }
                }
                out.println(arrayElementBuilder.build());
            }
        }
    }

    //    private NutsSearchCommand latestDependencies(NutsSearchCommand se) {
//        se.inlineDependencies();
//        if (scopes.isEmpty()) {
//            se.scope(NutsDependencyScopePattern.RUN);
//        } else {
//            se.scopes(scopes.toArray(new NutsDependencyScope[0]));
//        }
//        se.optional(includeOptional ? null : false).setLatest(true);
//        return se;
//    }
    private NFetchCmd latestOnlineDependencies(NFetchCmd se) {
        se.setDependencies(true);
        if (scopes.isEmpty()) {
            se.addScope(NDependencyScopePattern.RUN);
        } else {
            se.addScopes(scopes.toArray(new NDependencyScope[0]));
        }
        se.setOptional(isOptional() ? null : false)
        //TODO
        //.setSession(se.session.copy().setFetchStrategy(NFetchStrategy.ONLINE))
        ;
        return se;
    }

    protected NUpdateResult checkRegularUpdate(NId id, Type type, NVersion targetApiVersion, Instant now, boolean updateEvenIfExisting) {
        NVersion version = id.getVersion();
        if (!updateEvenIfExisting && version.isSingleValue()) {
            updateEvenIfExisting = NAsk.of()
                    .setDefaultValue(true)
                    .forBoolean(NMsg.ofC("version is too restrictive. Do you intend to force update of %s ?", id))
                    .getBooleanValue();
        }
        DefaultNUpdateResult r = new DefaultNUpdateResult();
        r.setId(id.getShortId());
        boolean shouldUpdateDefault = false;
        NDefinition d0 = NSearchCmd.of().addId(id)
                .setInstallStatus(NInstallStatusFilters.of().byDeployed(true))
                .setOptional(false).setFailFast(false)//.setDefaultVersions(true)
                .sort(DEFAULT_THEN_LATEST_VERSION_FIRST)
                .getResultDefinitions()
                .findFirst().orNull();
        if (d0 == null) {
            //should not throw exception here, this is a check and not update method
            return r;
        }
        if (!d0.getInstallInformation().get().isDefaultVersion()) {
            shouldUpdateDefault = true;
        }
        //search latest parse

        NSearchCmd sc = NSearchCmd.of()
                .setFetchStrategy(NFetchStrategy.ANYWHERE)
                .addId(d0.getId().getShortId())
                .setFailFast(false)
                .setLatest(true)
                .addLockedIds(getLockedIds())
                .addRepositoryFilter(NRepositoryFilters.of().installedRepo().neg())
                .setDependencies(true)
                .setOptional(isOptional() ? null : false);
        if (updateEvenIfExisting) {
            sc.setExpireTime(now);
        }

        if (type == Type.EXTENSION) {
            sc.setExtension(true);
        } else if (type == Type.COMPANION) {
            sc.setCompanion(true);
        }
        if (targetApiVersion != null) {
            sc.setTargetApiVersion(targetApiVersion);
        }

        if (scopes.isEmpty()) {
            sc.addScope(NDependencyScopePattern.RUN);
        } else {
            sc.addScopes(scopes.toArray(new NDependencyScope[0]));
        }
        NDefinition d1 = sc.getResultDefinitions()
                .findFirst().orNull();
        r.setInstalled(d0);
        r.setAvailable(d1);
        if (d1 == null) {
            //this is very interesting. Why the hell is this happening?
            r.setAvailable(d0);
        } else {
            NVersion v0 = d0.getId().getVersion();
            NVersion v1 = d1.getId().getVersion();
            if (v1.compareTo(v0) <= 0) {
                //no update needed!
                if (updateEvenIfExisting) {
                    r.setUpdateForced(true);
                }
                if (shouldUpdateDefault) {
                    r.setUpdateStatusAvailable(true);
                }
            } else {
                r.setUpdateVersionAvailable(true);
            }
        }

        return r;
    }

    private NFetchCmd fetch0() {
        return NFetchCmd.of().setContent(true).setEffective(true);
    }

    private void applyFixes() {
        if (resultFixes != null) {
            NSession session = workspace.currentSession();
            NPrintStream out = session.out();
            for (FixAction n : resultFixes) {
                n.fix(session);
                out.println(NMsg.ofC("[```error FIX```] unable to %s %s ", n.getId(), n.getProblemKey()));
            }
        }
    }

    private void applyResult(NWorkspaceUpdateResult result) {
        NSession session = getWorkspace().currentSession();
        NWorkspace ws = session.getWorkspace();
        applyFixes();
        NUpdateResult apiUpdate = result.getApi();
        NUpdateResult runtimeUpdate = result.getRuntime();
        List<NId> notInstalled = result.getAllResults().stream()
                .filter(x -> x.getInstalled() == null) //not installed
                .map(NUpdateResult::getId)
                .collect(Collectors.toList());
        if (!notInstalled.isEmpty()) {
            if (notInstalled.size() == 1) {
                throw new NIllegalArgumentException(NMsg.ofC("%s is not yet installed for it to be updated.", notInstalled.get(0)));
            } else {
                throw new NIllegalArgumentException(NMsg.ofC("%s are not yet installed for them to be updated.", notInstalled));
            }
        }
        if (result.getUpdatesCount() == 0) {
            return;
        }
        NWorkspaceUtils.of(workspace).checkReadOnly();
        boolean requireSave = false;
        NSession validWorkspaceSession = session;
        final NPrintStream out = validWorkspaceSession.out();
        boolean accept = NIO.of().getDefaultTerminal().ask()
                .forBoolean(NMsg.ofPlain("would you like to apply updates?")).setDefaultValue(true)
                .getValue();
        if (validWorkspaceSession.isAsk() && !accept) {
            throw new NCancelException();
        }
        boolean apiUpdateAvailable = apiUpdate != null && apiUpdate.getAvailable() != null && !apiUpdate.isUpdateApplied();
        boolean runtimeUpdateAvailable = runtimeUpdate != null && runtimeUpdate.getAvailable() != null && !runtimeUpdate.isUpdateApplied();
        boolean apiUpdateApplicable = apiUpdateAvailable && !apiUpdate.isUpdateApplied();
        boolean runtimeUpdateApplicable = runtimeUpdateAvailable && !runtimeUpdate.isUpdateApplied();
        NId finalApiId = apiUpdateAvailable ? apiUpdate.getAvailable().getId() : ws.getApiId();
        NId finalRuntimeId = runtimeUpdateApplicable ? runtimeUpdate.getAvailable().getId() : ws.getRuntimeId();
        if (apiUpdateApplicable || runtimeUpdateApplicable) {
            //wcfg.getModel().prepareBootApi(finalApiId, finalRuntimeId, true, validWorkspaceSession);
        }
        if (apiUpdateApplicable) {
            applyRegularUpdate(((DefaultNUpdateResult) apiUpdate));
            ((DefaultNUpdateResult) apiUpdate).setUpdateApplied(true);
            traceSingleUpdate(apiUpdate);
        }
        if (runtimeUpdateApplicable) {
//            wcfg.getModel().prepareBootRuntime(finalRuntimeId, true, validWorkspaceSession);

            applyRegularUpdate(((DefaultNUpdateResult) runtimeUpdate));
            ((DefaultNUpdateResult) runtimeUpdate).setUpdateApplied(true);
            List<NId> baseApiIds = CoreNUtils.resolveNutsApiIdsFromIdList(runtimeUpdate.getDependencies(), session);
            DefaultNWorkspaceConfigModel configModel = NWorkspaceExt.of().getModel().configModel;
            for (NId newApi : baseApiIds) {
                configModel.setExtraBootRuntimeId(
                        newApi,
                        runtimeUpdate.getAvailable().getId(),
                        runtimeUpdate.getAvailable().getDependencies().get().transitive().toList()
                );
            }
            traceSingleUpdate(runtimeUpdate);
        }
        for (NUpdateResult extension : result.getExtensions()) {
            if (!extension.isUpdateApplied()) {
                if (extension.getAvailable() != null) {
                    applyRegularUpdate(((DefaultNUpdateResult) extension));
                    List<NId> baseApiIds = CoreNUtils.resolveNutsApiIdsFromIdList(extension.getDependencies(), session);
                    DefaultNWorkspaceConfigModel configModel = NWorkspaceExt.of().getModel().configModel;
                    for (NId newApi : baseApiIds) {
                        configModel.setExtraBootExtensionId(
                                newApi,
                                extension.getAvailable().getId(),
                                extension.getAvailable().getDependencies().get().transitive().toList()
                        );
                    }
                    ((DefaultNUpdateResult) extension).setUpdateApplied(true);
                    traceSingleUpdate(extension);
                }
            }
        }
        for (NUpdateResult component : result.getArtifacts()) {
            applyRegularUpdate((DefaultNUpdateResult) component);
        }

        if (NWorkspace.of().saveConfig(requireSave)) {
            if (_LOG().isLoggable(Level.INFO)) {
                _LOGOP().level(Level.INFO).verb(NLogVerb.WARNING)
                        .log(NMsg.ofPlain("workspace is updated. Nuts should be restarted for changes to take effect."));
            }
            if (apiUpdate != null && apiUpdate.isUpdatable() && !apiUpdate.isUpdateApplied()) {
                if (validWorkspaceSession.isPlainTrace()) {
                    out.println("workspace is updated. Nuts should be restarted for changes to take effect.");
                }
            }
        }
    }

    private void traceSingleUpdate(NUpdateResult r) {
        NSession session = getWorkspace().currentSession();
        NId id = r.getId();
        NDefinition d0 = r.getInstalled();
        NDefinition d1 = r.getAvailable();
//        final String simpleName = d0 != null ? d0.getId().getShortName() : d1 != null ? d1.getId().getShortName() : id.getShortName();
        final NId simpleId = d0 != null ? d0.getId().getShortId() : d1 != null ? d1.getId().getShortId() : id.getShortId();
        final NPrintStream out = session.out();
        NTexts factory = NTexts.of();
        if (r.isUpdateApplied()) {
            if (r.isUpdateForced()) {
                if (d0 == null) {
                    out.resetLine().println(NMsg.ofC("%s is %s to latest version %s",
                            simpleId,
                            factory.ofStyled("updated", NTextStyle.primary3()),
                            d1 == null ? null : d1.getId().getVersion()
                    ));
                } else if (d1 == null) {
                    //this is very interesting. Why the hell is this happening?
                } else {
                    NVersion v0 = d0.getId().getVersion();
                    NVersion v1 = d1.getId().getVersion();
                    if (v1.compareTo(v0) <= 0) {
                        if (v1.compareTo(v0) == 0) {
                            out.resetLine().println(NMsg.ofC("%s is %s to %s",
                                    simpleId,
                                    factory.ofStyled("forced", NTextStyle.primary3()),
                                    d0.getId().getVersion()));
                        } else {
                            out.resetLine().println(NMsg.ofC("%s is %s from %s to older version %s",
                                    simpleId,
                                    factory.ofStyled("forced", NTextStyle.primary3()),
                                    d0.getId().getVersion(), d1.getId().getVersion()));
                        }
                    } else {
                        out.resetLine().println(NMsg.ofC("%s is %s from %s to latest version %s",
                                simpleId,
                                factory.ofStyled("updated", NTextStyle.primary3()),
                                d0.getId().getVersion(), d1.getId().getVersion()));
                    }
                }
            }
        }
    }

    public NUpdateResult checkCoreUpdate(NId id, NVersion bootApiVersion, Type type, Instant now) {
        //disable trace so that search do not write to stream
        NSession session = getWorkspace().currentSession();
        NWorkspace ws = session.getWorkspace();
        NId oldId = null;
        NDefinition oldFile = null;
        NDefinition newFile = null;
        NId newId = null;
//        List<NutsId> dependencies = new ArrayList<>();
//        NSession sessionOffline = session.copy().setFetchMode(NutsFetchMode.OFFLINE);
        switch (type) {
            case API: {
                oldId = NWorkspace.of().getStoredConfig().getApiId();
                NId confId = NWorkspace.of().getStoredConfig().getApiId();
                if (confId != null) {
                    oldId = confId;
                }
                NVersion v = bootApiVersion;
                if (v == null || v.isBlank()) {
                    v = NVersion.get(NConstants.Versions.LATEST).get();
                }
                try {
                    NId finalOldId = oldId;
                    oldFile = session.copy().setFetchStrategy(NFetchStrategy.ONLINE).callWith(() -> fetch0().setId(finalOldId)
                            .getResultDefinition());
                } catch (NNotFoundException ex) {
                    //ignore
                }
                try {
                    newId = NSearchCmd.of()
                            .setFetchStrategy(NFetchStrategy.ANYWHERE)
                            .setRepositoryFilter(getRepositoryFilter())
                            .addId(NConstants.Ids.NUTS_API + "#" + v).setLatest(true).getResultIds()
                            .findFirst().orNull();
                    NId finalNewId1 = newId;
                    newFile = newId == null ? null :
                            session.copy().setFetchStrategy(NFetchStrategy.ONLINE)
                                    .callWith(() ->
                                            latestOnlineDependencies(fetch0()).setFailFast(false)
                                                    .setId(finalNewId1).getResultDefinition()
                                    );
                } catch (NNotFoundException ex) {
                    _LOGOP().level(Level.SEVERE).error(ex).log(NMsg.ofC("error : %s", ex));
                    //ignore
                }
                break;
            }
            case RUNTIME: {
                oldId = ws.getRuntimeId();
                NId confId = NWorkspace.of().getStoredConfig().getRuntimeId();
                if (confId != null) {
                    oldId = confId;
                }
                if (oldId != null) {
                    try {
                        NId finalOldId1 = oldId;
                        oldFile = session.copy().setFetchStrategy(NFetchStrategy.ONLINE)
                                .callWith(() -> fetch0().setId(finalOldId1).getResultDefinition());
                    } catch (NNotFoundException ex) {
                        _LOGOP().level(Level.SEVERE).error(ex).log(NMsg.ofC("error : %s", ex));
                        //ignore
                    }
                }
                try {
                    NSearchCmd se = NSearchCmd.of()
                            .setFetchStrategy(NFetchStrategy.ANYWHERE)
                            .addId(oldFile != null ? oldFile.getId().builder().setVersion("").build().toString() : NConstants.Ids.NUTS_RUNTIME)
                            .setRuntime(true)
                            .setTargetApiVersion(bootApiVersion)
                            .addLockedIds(getLockedIds())
                            .setLatest(true)
                            .sort(LATEST_VERSION_FIRST);
                    newId = se.getResultIds()
                            .findFirst().orNull();
                    NId finalNewId = newId;
                    newFile = newId == null ? null :

                            session.copy().setFetchStrategy(NFetchStrategy.ONLINE)
                                    .callWith(() -> latestOnlineDependencies(fetch0().setId(finalNewId))
                                            .setFailFast(false)
                                            .getResultDefinition()
                                    );
                } catch (NNotFoundException ex) {
                    _LOGOP().level(Level.SEVERE).error(ex).log(NMsg.ofC("error : %s", ex));
                    //ignore
                }
                break;
            }
//            case "companion":
//            case "extension": {
//                try {
//                    oldId = ws.search().addId(id).setEffective(true).setSession(session)
//                            .setInstallStatus(ws.filters().installStatus().byDeployed(true))
//                            .sort(DEFAULT_THEN_LATEST_VERSION_FIRST).setFailFast(false).getResultIds().first();
//                    if (oldId != null) {
//                        oldFile = fetch0().setId(oldId).setSession(session).getResultDefinition();
//                    }
//                } catch (Exception ex) {
//                    _LOGOP().level(Level.SEVERE).error(ex).log("error : {0}", ex);
//                    //ignore
//                }
//                try {
//                    NutsSearchCommand se = ws.search()
//                            .setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ANYWHERE))
//                            .addId(id)
//                            .setTargetApiVersion(bootApiVersion)
//                            .addLockedIds(getLockedIds())
//                            .setFailFast(false)
//                            .setLatest(true)
//                            .sort(LATEST_VERSION_FIRST);
//                    if (type.equals("extension")) {
//                        se.setExtension(true);
//                    } else if (type.equals("companion")) {
//                        se.setCompanion(true);
//                    }
//                    newId = se.getResultIds().first();
//
//                    newFile = newId == null ? null : latestOnlineDependencies(fetch0().setSession(session).setId(newId))
//                            .setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ONLINE))
//                            .getResultDefinition();
//                } catch (Exception ex) {
//                    _LOGOP().level(Level.SEVERE).error(ex).log("error : {0}", ex);
//                    //ignore
//                }
//                break;
//            }
        }
        //compare canonical forms
        NId cnewId = toCanonicalForm(newId);
        NId coldId = toCanonicalForm(oldId);
        DefaultNUpdateResult defaultNutsUpdateResult = new DefaultNUpdateResult(id, oldFile, newFile,
                newFile == null ? null : newFile.getDependencies().get().transitive()
                        .map(NDependency::toId)
                        .withDesc(NEDesc.of("toId"))
                        .toList(),
                false);
        if (cnewId != null && newFile != null && coldId != null && cnewId.getVersion().compareTo(coldId.getVersion()) > 0) {
            defaultNutsUpdateResult.setUpdateVersionAvailable(true);
        }
        return defaultNutsUpdateResult;
    }

    private NId toCanonicalForm(NId id) {
        if (id != null) {
            id = id.builder().setRepository(null).build();
            String oldValue = id.getProperties().get(NConstants.IdProperties.FACE);
            if (oldValue != null && oldValue.trim().isEmpty()) {
                id = id.builder().setProperty(NConstants.IdProperties.FACE, null).build();
            }
        }
        return id;
    }

    private void applyRegularUpdate(DefaultNUpdateResult r) {
        NSession session = getWorkspace().currentSession();
        NWorkspace ws = session.getWorkspace();
        if (r.isUpdateApplied()) {
            return;
        }
        NWorkspaceExt dws = NWorkspaceExt.of();
        final NPrintStream out = session.out();
//        NutsId id = r.getId();
        NDefinition d0 = r.getInstalled();
        NDefinition d1 = r.getAvailable();
        if (d0 == null) {
            NWorkspaceSecurityManager.of().checkAllowed(NConstants.Permissions.UPDATE, "update");
            dws.updateImpl(d1, new String[0], true);
            r.setUpdateApplied(true);
        } else if (d1 == null) {
            //this is very interesting. Why the hell is this happening?
        } else {
            NVersion v0 = d0.getId().getVersion();
            NVersion v1 = d1.getId().getVersion();
            if (v1.compareTo(v0) <= 0) {
                //no update needed!
                if (/*session.isYes() || */r.isUpdateForced()) {
                    NWorkspaceSecurityManager.of().checkAllowed(NConstants.Permissions.UPDATE, "update");
                    dws.updateImpl(d1, new String[0], true);
                    r.setUpdateApplied(true);
                    r.setUpdateForced(true);
                } else {
                    dws.getInstalledRepository().setDefaultVersion(d1.getId());
                }
            } else {
                NWorkspaceSecurityManager.of().checkAllowed(NConstants.Permissions.UPDATE, "update");
                dws.updateImpl(d1, new String[0], true);
                r.setUpdateApplied(true);
            }
        }
        traceSingleUpdate(r);
    }

    public enum Type {
        API,
        RUNTIME,
        REGULAR,
        EXTENSION,
        COMPANION,
    }

    private static abstract class FixAction {

        private final NId id;
        private final String problemKey;

        public FixAction(NId id, String problemKey) {
            this.id = id;
            this.problemKey = problemKey;
        }

        public NId getId() {
            return id;
        }

        public String getProblemKey() {
            return problemKey;
        }

        public abstract void fix(NSession session);

        @Override
        public String toString() {
            return "FixAction{"
                    + "id=" + id
                    + ", problemKey='" + problemKey + '\''
                    + '}';
        }
    }
}
