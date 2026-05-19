/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.update;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.NWorkspaceUpdateResult;
import net.thevpc.nuts.elem.NArrayElementBuilder;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.core.NRepositoryFilters;
import net.thevpc.nuts.io.NIn;
import net.thevpc.nuts.runtime.standalone.util.collections.NIteratorUtils;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.install.*;
import net.thevpc.nuts.security.NSecurityManager;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNUpdateResult;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;

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
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNUpdate extends AbstractNUpdate {

    private final NComparator<NId> LATEST_VERSION_FIRST = new NComparator<NId>() {
        @Override
        public int compare(NId x, NId y) {
            return -x.version().compareTo(y.version());
        }

        @Override
        public NElement describe() {
            return NElement.ofString("latestVersionFirst");
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
            return -x.version().compareTo(y.version());
        }

        @Override
        public NElement describe() {
            return NElement.ofString("defaultThenLatestVersionFirst");
        }
    };
    private boolean checkFixes = false;
    private List<FixAction> resultFixes = null;

    public DefaultNUpdate() {
        super();
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
        boolean enabled = a.isUncommented();
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
    public NUpdate update() {
        applyResult(getResult());
        return this;
    }

    @Override
    public NUpdate checkUpdates() {
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
        NVersion bootVersion0 = NWorkspace.of().apiVersion();
        NVersion bootVersion = bootVersion0;
        if (!(this.apiVersion() == null || this.apiVersion().isBlank())) {
            bootVersion = this.apiVersion();
        }
        if (this.isApi() || !(this.apiVersion() == null || this.apiVersion().isBlank())) {
            apiUpdate = checkCoreUpdate(NId.get(NConstants.Ids.NUTS_API).get(), this.apiVersion(), Type.API, now);
            if (apiUpdate.isUpdatable()) {
                bootVersion = apiUpdate.available().id().version();
                allUpdates.put(NConstants.Ids.NUTS_API, apiUpdate);
            } else {
                //reset bootVersion
                bootVersion = bootVersion0;
            }
        }
        NUpdateResult runtimeUpdate = null;
        if (this.isRuntime()) {
            if (dws.requiresRuntimeExtension()) {
                runtimeUpdate = checkCoreUpdate(NId.get(NWorkspace.of().runtimeId().shortName()).get(),
                        apiUpdate != null && apiUpdate.available() != null && apiUpdate.available().id() != null ? apiUpdate.available().id().version()
                                : bootVersion, Type.RUNTIME, now);
                if (runtimeUpdate.isUpdatable()) {
                    allUpdates.put(runtimeUpdate.id().shortName(), runtimeUpdate);
                }
            }
        }

        if (this.isExtensions()) {
            for (NId d : getExtensionsToUpdate()) {
                NUpdateResult updated = checkRegularUpdate(d, Type.EXTENSION, bootVersion, now, expireTime != null);
                if (updated.isUpdatable()) {
                    allUpdates.put(updated.id().shortName(), updated);
                    extUpdates.put(updated.id().shortName(), updated);
                }
            }
        }

        if (this.isCompanions()) {
            for (NId d : getCompanionsToUpdate()) {
                NUpdateResult updated = checkRegularUpdate(d, Type.COMPANION, bootVersion, now, expireTime != null);
                if (updated.isUpdatable()) {
                    allUpdates.put(updated.id().shortName(), updated);
                    regularUpdates.put(updated.id().shortName(), updated);
                }
            }
        }

        for (NId id : this.getRegularIds()) {
            NUpdateResult updated = checkRegularUpdate(id, Type.REGULAR, null, now, expireTime != null);
            allUpdates.put(updated.id().shortName(), updated);
            regularUpdates.put(updated.id().shortName(), updated);
        }
        List<NId> lockedIds = this.lockedIds();
        if (lockedIds.size() > 0) {
            for (NId d : new HashSet<>(lockedIds)) {
                NDependency dd = NDependency.get(d.toString()).get();
                if (regularUpdates.containsKey(dd.shortName())) {
                    NUpdateResult updated = regularUpdates.get(dd.shortName());
                    //FIX ME
                    if (!dd.version().toFilter().acceptVersion(updated.id().version())) {
                        throw new NIllegalArgumentException(
                                NMsg.ofC("%s unsatisfied  : %s", dd, updated.id().version())
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
            ext.add(extension.shortId());
        }
        if (updateExtensions) {
            return ext;
        } else {
            Set<NId> ext2 = new HashSet<>();
            for (NId id : ids) {
                if (id.shortName().equals(NConstants.Ids.NUTS_API)) {
                    continue;
                }
                if (id.shortName().equals(NWorkspace.of().runtimeId().shortName())) {
                    continue;
                }
                if (ext.contains(id.shortId())) {
                    ext2.add(id.shortId());
                }

            }
            return ext2;
        }
    }

    private Set<NId> getCompanionsToUpdate() {
        Set<NId> ext = new HashSet<>();
        for (NId extension : NExtensions.of().getCompanionIds()) {
            ext.add(extension.shortId());
        }
        return ext;
    }

    private Set<NId> getRegularIds() {
        HashSet<String> extensions = new HashSet<>();
        for (NId object : NExtensions.of().getConfigExtensions()) {
            extensions.add(object.shortName());
        }

        HashSet<NId> baseRegulars = new HashSet<>(ids);
        if (isInstalled()) {
            baseRegulars.addAll(NSearch.of()
                    .definitionFilter(NDefinitionFilters.of().byInstalled(true))
                    .getResultIds().stream().map(NId::shortId).collect(Collectors.toList()));
            // This bloc is to handle packages that were installed by their jar/content but was removed for any reason!
            NWorkspaceExt dws = NWorkspaceExt.of();
            NInstalledRepository ir = dws.getInstalledRepository();
            for (NInstallInformation y : NIteratorUtils.toList(ir.searchInstallInformation())) {
                if (y != null && y.installStatus().isInstalled() && y.id() != null) {
                    baseRegulars.add(y.id().builder().version("").build());
                }
            }
        }
        HashSet<NId> regulars = new HashSet<>();
        for (NId id : baseRegulars) {
            if (id.shortName().equals(NConstants.Ids.NUTS_API)) {
                continue;
            }
            if (id.shortName().equals(NWorkspace.of().runtimeId().shortName())) {
                continue;
            }
            if (extensions.contains(id.shortName())) {
                continue;
            }
            regulars.add(id);
        }
        return regulars;
    }

    public NUpdate checkFixes() {
        resultFixes = null;
        NWorkspaceExt dws = NWorkspaceExt.of();
        NInstalledRepository ir = dws.getInstalledRepository();
        resultFixes = NIteratorUtils.toList(NIteratorUtils.convertNonNull(ir.searchInstallInformation(), new Function<NInstallInformation, FixAction>() {
            @Override
            public FixAction apply(NInstallInformation nInstallInformation) {
                NId id = NSearch.of()
                        .definitionFilter(NDefinitionFilters.of().byInstalled(true))
                        .addId(nInstallInformation.id()).getResultIds()
                        .findFirst().orNull();
                if (id == null) {
                    return new FixAction(nInstallInformation.id(), "MissingInstallation") {
                        @Override
                        public void fix() {
                            NInstall.of(getId()).run();
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
            NSession session = NSession.of();
            NPrintStream out = session.out();
            for (FixAction n : resultFixes) {
                out.println(NMsg.ofC("[```error FIX```] %s %s", n.getId(), n.getProblemKey()));
            }
        }
    }

    protected void traceUpdates(NWorkspaceUpdateResult result) {
        NSession session = NSession.of();
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
                out.println(NMsg.ofC("all packages are %s. You are running latest version%s.",
                        NText.ofStyledSuccess("up-to-date"),
                        result.getAllResults().size() > 1 ? "s" : ""));
            } else {
                if (updates.size() > 0 && notInstalled.size() > 0) {
                    out.println(NMsg.ofC("workspace has %s package%s not installed and %s package%s to update.",
                            NText.ofStyledPrimary1("" + notInstalled.size()),
                            (notInstalled.size() > 1 ? "s" : ""),
                            NText.ofStyledPrimary1("" + updates.size()),
                            (updates.size() > 1 ? "s" : "")
                    ));
                } else if (updates.size() > 0) {
                    out.println(NMsg.ofC("workspace has %s package%s to update.", NText.ofStyledPrimary1("" + updates.size()),
                            (updates.size() > 1 ? "s" : "")));
                } else if (notInstalled.size() > 0) {
                    out.println(NMsg.ofC("workspace has %s package%s not installed.", NText.ofStyledPrimary1("" + notInstalled.size()),
                            (notInstalled.size() > 1 ? "s" : "")));
                }
                int widthCol1 = 2;
                int widthCol2 = 2;
                for (NUpdateResult update : all) {
                    widthCol1 = Math.max(widthCol1, update.available() == null ? 0 : update.available().id().shortName().length());
                    widthCol2 = Math.max(widthCol2, update.installed() == null ? 0 : update.installed().id().version().toString().length());
                }
                NTexts factory = NTexts.of();
                for (NUpdateResult update : all) {
                    if (update.installed() == null) {
                        out.println(NMsg.ofC("%s  : %s",
                                factory.ofStyled(NStringUtils.formatAlign(update.id().toString(), widthCol2, NPositionType.FIRST), NTextStyle.primary6()),
                                factory.ofStyled("not installed", NTextStyle.error())));
                    } else if (update.isUpdateVersionAvailable()) {
                        out.println(NMsg.ofC("%s  : %s => %s",
                                factory.ofStyled(NStringUtils.formatAlign(update.installed().id().version().toString(), widthCol2, NPositionType.FIRST), NTextStyle.primary6()),
                                NStringUtils.formatAlign(update.available().id().shortName(), widthCol1, NPositionType.FIRST),
                                factory.ofPlain(update.available().id().version().toString())));
                    } else if (update.isUpdateStatusAvailable()) {
                        out.println(NMsg.ofC("%s  : %s => %s",
                                factory.ofStyled(NStringUtils.formatAlign(update.installed().id().version().toString(), widthCol2, NPositionType.FIRST), NTextStyle.primary6()),
                                NStringUtils.formatAlign(update.available().id().shortName(), widthCol1, NPositionType.FIRST),
                                factory.ofStyled("set as default", NTextStyle.primary4())));
                    } else {
                        out.println(NMsg.ofC("%s  : %s",
                                factory.ofStyled(NStringUtils.formatAlign(update.installed().id().version().toString(), widthCol2, NPositionType.FIRST), NTextStyle.primary6()),
                                factory.ofStyled("up-to-date", NTextStyle.warn())));
                    }
                }
            }
        } else {
            if (updates.size() == 0 && notInstalled.size() == 0) {
                out.println(NElement.ofObjectBuilder()
                        .set("message", "all packages are up-to-date. You are running latest version" + (result.getAllResults().size() > 1 ? "s" : "") + ".")
                        .build());
            } else {
                NArrayElementBuilder arrayElementBuilder = NElement.ofArrayBuilder();
                for (NUpdateResult update : all) {
                    if (update.installed() == null) {
                        arrayElementBuilder.add(NElement.ofObjectBuilder()
                                .set("package", update.id().shortName())
                                .set("status", "not-installed")
                                .build());
                    } else if (update.isUpdateVersionAvailable()) {
                        arrayElementBuilder.add(NElement.ofObjectBuilder()
                                .set("package", update.available().id().shortName())
                                .set("status", "update-version-available")
                                .set("localVersion", update.installed().id().version().toString())
                                .set("newVersion", update.available().id().version().toString())
                                .build());
                    } else if (update.isUpdateStatusAvailable()) {
                        arrayElementBuilder.add(NElement.ofObjectBuilder()
                                .set("package", update.available().id().shortName())
                                .set("localVersion", update.installed().id().version().toString())
                                .set("status", "update-default-available")
                                .set("newVersion", "set as default")
                                .build());
                    } else {
                        arrayElementBuilder.add(NElement.ofObjectBuilder()
                                .set("package", update.id().shortName())
                                .set("localVersion", update.installed().id().version().toString())
                                .set("status", "up-to-date")
                                .build());
                    }
                }
                out.println(arrayElementBuilder.build());
            }
        }
    }

    private NFetch latestOnlineDependencies() {
        NFetch se = NFetch.of();
        se.addDependencyFilter(NDependencyFilters.of().byRunnable(isOptional()));
        if (!scopes.isEmpty()) {
            se.addDependencyFilter(NDependencyFilters.of().byScope(scopes.toArray(new NDependencyScope[0])));
        }
        return se;
    }

    protected NUpdateResult checkRegularUpdate(NId id, Type type, NVersion targetApiVersion, Instant now, boolean updateEvenIfExisting) {
        NVersion version = id.version();
        if (!updateEvenIfExisting && version.isSingleValue()) {
            updateEvenIfExisting = NIn.ask()
                    .defaultValue(true)
                    .forBoolean(NMsg.ofC("version is too restrictive. Do you intend to force update of %s ?", id))
                    .booleanValue();
        }
        DefaultNUpdateResult r = new DefaultNUpdateResult();
        r.setId(id.shortId());
        boolean shouldUpdateDefault = false;
        NDefinition d0 = NSearch.of().addId(id)
                .definitionFilter(NDefinitionFilters.of().byDeployed(true))
                .dependencyFilter(NDependencyFilters.of().byOptional(false))
                .failFast(false)//.setDefaultVersions(true)
                .sort(DEFAULT_THEN_LATEST_VERSION_FIRST)
                .getResultDefinitions()
                .findFirst().orNull();
        if (d0 == null) {
            //should not throw exception here, this is a check and not update method
            return r;
        }
        if (!d0.installInformation().get().isDefaultVersion()) {
            shouldUpdateDefault = true;
        }
        //search latest parse

        NSearch sc = NSearch.of()
                .fetchStrategy(NFetchStrategy.ANYWHERE)
                .addId(d0.id().shortId())
                .failFast(false)
                .latest(true)
                .addDefinitionFilter(NDefinitionFilters.of().byLockedIds(lockedIds().toArray(new NId[0])))
                .addRepositoryFilter(NRepositoryFilters.of().installedRepo().neg())
//                .setDependencies(true)
                .dependencyFilter(NDependencyFilters.of().byOptional(isOptional() ? null : false));
        if (updateEvenIfExisting) {
            sc.expireTime(now);
        }

        if (type == Type.EXTENSION) {
            sc.extension(true);
        } else if (type == Type.COMPANION) {
            sc.companion(true);
        }
        if (targetApiVersion != null) {
            sc.targetApiVersion(targetApiVersion);
        }

        sc.dependencyFilter(resolveDependencyFilter());
        NDefinition d1 = sc.getResultDefinitions()
                .findFirst().orNull();
        r.setInstalled(d0);
        r.setAvailable(d1);
        if (d1 == null) {
            //this is very interesting. Why the hell is this happening?
            r.setAvailable(d0);
        } else {
            NVersion v0 = d0.id().version();
            NVersion v1 = d1.id().version();
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

    private NDependencyFilter resolveDependencyFilter() {
        return NDependencyFilters.of()
                .byRunnable(isOptional())
                .and(
                        NDependencyFilters.of().byScope(scopes.toArray(new NDependencyScope[0]))
                )
                ;
    }

    private void applyFixes() {
        if (resultFixes != null) {
            NSession session = NSession.of();
            NPrintStream out = session.out();
            for (FixAction n : resultFixes) {
                n.fix();
                out.println(NMsg.ofC("[```error FIX```] unable to %s %s ", n.getId(), n.getProblemKey()));
            }
        }
    }

    private void applyResult(NWorkspaceUpdateResult result) {
        NSession session = NSession.of();
        NWorkspace ws = session.workspace();
        applyFixes();
        NUpdateResult apiUpdate = result.getApi();
        NUpdateResult runtimeUpdate = result.getRuntime();
        List<NId> notInstalled = result.getAllResults().stream()
                .filter(x -> x.installed() == null) //not installed
                .map(NUpdateResult::id)
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
        NWorkspaceUtils.of().checkReadOnly();
        boolean requireSave = false;
        NSession validWorkspaceSession = session;
        final NPrintStream out = validWorkspaceSession.out();
        boolean accept = NIO.of().getDefaultTerminal().ask()
                .forBoolean(NMsg.ofPlain("would you like to apply updates?")).defaultValue(true)
                .value();
        if (validWorkspaceSession.isAsk() && !accept) {
            throw new NCancelException();
        }
        boolean apiUpdateAvailable = apiUpdate != null && apiUpdate.available() != null && !apiUpdate.isUpdateApplied();
        boolean runtimeUpdateAvailable = runtimeUpdate != null && runtimeUpdate.available() != null && !runtimeUpdate.isUpdateApplied();
        boolean apiUpdateApplicable = apiUpdateAvailable && !apiUpdate.isUpdateApplied();
        boolean runtimeUpdateApplicable = runtimeUpdateAvailable && !runtimeUpdate.isUpdateApplied();
        NId finalApiId = apiUpdateAvailable ? apiUpdate.available().id() : ws.apiId();
        NId finalRuntimeId = runtimeUpdateApplicable ? runtimeUpdate.available().id() : ws.runtimeId();
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
            List<NId> baseApiIds = CoreNUtils.resolveNutsApiIdsFromIdList(runtimeUpdate.dependencies());
            DefaultNWorkspaceConfigModel configModel = NWorkspaceExt.of().getModel().configModel;
            for (NId newApi : baseApiIds) {
                configModel.setExtraBootRuntimeId(
                        newApi,
                        runtimeUpdate.available().id(),
                        runtimeUpdate.available().dependencies().get().transitive().toList()
                );
            }
            traceSingleUpdate(runtimeUpdate);
        }
        for (NUpdateResult extension : result.getExtensions()) {
            if (!extension.isUpdateApplied()) {
                if (extension.available() != null) {
                    applyRegularUpdate(((DefaultNUpdateResult) extension));
                    List<NId> baseApiIds = CoreNUtils.resolveNutsApiIdsFromIdList(extension.dependencies());
                    DefaultNWorkspaceConfigModel configModel = NWorkspaceExt.of().getModel().configModel;
                    for (NId newApi : baseApiIds) {
                        configModel.setExtraBootExtensionId(
                                newApi,
                                extension.available().id(),
                                extension.available().dependencies().get().transitive().toList()
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
                _LOG()
                        .log(NMsg.ofPlain("workspace is updated. Nuts should be restarted for changes to take effect.")
                                .withLevel(Level.INFO).withIntent(NMsgIntent.ALERT)
                        );
            }
            if (apiUpdate != null && apiUpdate.isUpdatable() && !apiUpdate.isUpdateApplied()) {
                if (validWorkspaceSession.isPlainTrace()) {
                    out.println("workspace is updated. Nuts should be restarted for changes to take effect.");
                }
            }
        }
    }

    private void traceSingleUpdate(NUpdateResult r) {
        NSession session = NSession.of();
        NId id = r.id();
        NDefinition d0 = r.installed();
        NDefinition d1 = r.available();
//        final String simpleName = d0 != null ? d0.getId().getShortName() : d1 != null ? d1.getId().getShortName() : id.getShortName();
        final NId simpleId = d0 != null ? d0.id().shortId() : d1 != null ? d1.id().shortId() : id.shortId();
        final NPrintStream out = session.out();
        NTexts factory = NTexts.of();
        if (r.isUpdateApplied()) {
            if (r.isUpdateForced()) {
                if (d0 == null) {
                    out.println(NMsg.ofC("%s is %s to latest version %s",
                            simpleId,
                            factory.ofStyled("updated", NTextStyle.primary3()),
                            d1 == null ? null : d1.id().version()
                    ));
                } else if (d1 == null) {
                    //this is very interesting. Why the hell is this happening?
                } else {
                    NVersion v0 = d0.id().version();
                    NVersion v1 = d1.id().version();
                    if (v1.compareTo(v0) <= 0) {
                        if (v1.compareTo(v0) == 0) {
                            out.println(NMsg.ofC("%s is %s to %s",
                                    simpleId,
                                    factory.ofStyled("forced", NTextStyle.primary3()),
                                    d0.id().version()));
                        } else {
                            out.println(NMsg.ofC("%s is %s from %s to older version %s",
                                    simpleId,
                                    factory.ofStyled("forced", NTextStyle.primary3()),
                                    d0.id().version(), d1.id().version()));
                        }
                    } else {
                        out.println(NMsg.ofC("%s is %s from %s to latest version %s",
                                simpleId,
                                factory.ofStyled("updated", NTextStyle.primary3()),
                                d0.id().version(), d1.id().version()));
                    }
                }
            }
        }
    }

    public NUpdateResult checkCoreUpdate(NId id, NVersion bootApiVersion, Type type, Instant now) {
        //disable trace so that search do not write to stream
        NSession session = NSession.of();
        NWorkspace ws = session.workspace();
        NId oldId = null;
        NDefinition oldFile = null;
        NDefinition newFile = null;
        NId newId = null;
//        List<NutsId> dependencies = new ArrayList<>();
//        NSession sessionOffline = session.copy().setFetchMode(NutsFetchMode.OFFLINE);
        switch (type) {
            case API: {
                oldId = NWorkspace.of().storedConfig().getApiId();
                NId confId = NWorkspace.of().storedConfig().getApiId();
                if (confId != null) {
                    oldId = confId;
                }
                NVersion v = bootApiVersion;
                if (v == null || v.isBlank()) {
                    v = NVersion.get(NConstants.Versions.LATEST).get();
                }
                try {
                    NId finalOldId = oldId;
                    oldFile = session.copy().fetchStrategy(NFetchStrategy.ONLINE).callWith(() -> NFetch.of(finalOldId)
                            .dependencyFilter(NDependencyFilters.of().byRunnable())
                            .getResultDefinition());
                } catch (NArtifactNotFoundException ex) {
                    //ignore
                }
                try {
                    newId = NSearch.of()
                            .fetchStrategy(NFetchStrategy.ANYWHERE)
                            .repositoryFilter(repositoryFilter())
                            .addId(NConstants.Ids.NUTS_API + "#" + v).latest(true).getResultIds()
                            .findFirst().orNull();
                    NId finalNewId1 = newId;
                    newFile = newId == null ? null :
                            session.copy().fetchStrategy(NFetchStrategy.ONLINE)
                                    .callWith(() ->
                                            latestOnlineDependencies().failFast(false)
                                                    .id(finalNewId1).getResultDefinition()
                                    );
                } catch (NArtifactNotFoundException ex) {
                    _LOG().log(NMsg.ofC("error : %s", ex).asError(ex));
                    //ignore
                }
                break;
            }
            case RUNTIME: {
                oldId = ws.runtimeId();
                NId confId = NWorkspace.of().storedConfig().getRuntimeId();
                if (confId != null) {
                    oldId = confId;
                }
                if (oldId != null) {
                    try {
                        NId finalOldId1 = oldId;
                        oldFile = session.copy().fetchStrategy(NFetchStrategy.ONLINE)
                                .callWith(() -> NFetch.of().id(finalOldId1)
                                        .dependencyFilter(NDependencyFilters.of().byRunnable())
                                        .getResultDefinition());
                    } catch (NArtifactNotFoundException ex) {
                        _LOG().log(NMsg.ofC("error : %s", ex).asError(ex));
                        //ignore
                    }
                }
                try {
                    NSearch se = NSearch.of()
                            .fetchStrategy(NFetchStrategy.ANYWHERE)
                            .addId(oldFile != null ? oldFile.id().builder().version("").build().toString() : NConstants.Ids.NUTS_RUNTIME)
                            .runtime(true)
                            .targetApiVersion(bootApiVersion)
                            .addDefinitionFilter(NDefinitionFilters.of().byLockedIds(lockedIds().toArray(new NId[0])))
                            .latest(true)
                            .sort(LATEST_VERSION_FIRST);
                    newId = se.getResultIds()
                            .findFirst().orNull();
                    NId finalNewId = newId;
                    newFile = newId == null ? null :

                            session.copy().fetchStrategy(NFetchStrategy.ONLINE)
                                    .callWith(() -> latestOnlineDependencies().id(finalNewId)
                                            .failFast(false)
                                            .getResultDefinition()
                                    );
                } catch (NArtifactNotFoundException ex) {
                    _LOG().log(NMsg.ofC("error : %s", ex).asError(ex));
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
//                    _LOG().level(Level.SEVERE).error(ex).log("error : {0}", ex);
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
//                    _LOG().level(Level.SEVERE).error(ex).log("error : {0}", ex);
//                    //ignore
//                }
//                break;
//            }
        }
        //compare canonical forms
        NId cnewId = toCanonicalForm(newId);
        NId coldId = toCanonicalForm(oldId);
        DefaultNUpdateResult defaultNutsUpdateResult = new DefaultNUpdateResult(id, oldFile, newFile,
                newFile == null ? null : newFile.dependencies().get().transitive()
                        .map(NDependency::toId)
                        .withDescription(NDescribables.ofDesc("toId"))
                        .toList(),
                false);
        if (cnewId != null && newFile != null && coldId != null && cnewId.version().compareTo(coldId.version()) > 0) {
            defaultNutsUpdateResult.setUpdateVersionAvailable(true);
        }
        return defaultNutsUpdateResult;
    }

    private NId toCanonicalForm(NId id) {
        if (id != null) {
            id = id.builder().repository(null).build();
            String oldValue = id.properties().get(NConstants.IdProperties.FACE);
            if (oldValue != null && oldValue.trim().isEmpty()) {
                id = id.builder().setProperty(NConstants.IdProperties.FACE, null).build();
            }
        }
        return id;
    }

    private void applyRegularUpdate(DefaultNUpdateResult r) {
        if (r.isUpdateApplied()) {
            return;
        }
        NWorkspaceExt dws = NWorkspaceExt.of();
//        NutsId id = r.getId();
        NDefinition d0 = r.installed();
        NDefinition d1 = r.available();
        if (d0 == null) {
            NSecurityManager.of().checkAllowed(NConstants.Permissions.UPDATE, "update");
            applyRegularUpdate0(d1, new String[0]);
            r.setUpdateApplied(true);
        } else if (d1 == null) {
            //this is very interesting. Why the hell is this happening?
        } else {
            NVersion v0 = d0.id().version();
            NVersion v1 = d1.id().version();
            if (v1.compareTo(v0) <= 0) {
                //no update needed!
                if (/*session.isYes() || */r.isUpdateForced()) {
                    NSecurityManager.of().checkAllowed(NConstants.Permissions.UPDATE, "update");
                    applyRegularUpdate0(d1, new String[0]);
                    r.setUpdateApplied(true);
                    r.setUpdateForced(true);
                } else {
                    dws.getInstalledRepository().setDefaultVersion(d1.id());
                }
            } else {
                NSecurityManager.of().checkAllowed(NConstants.Permissions.UPDATE, "update");
                applyRegularUpdate0(d1, new String[0]);
                r.setUpdateApplied(true);
            }
        }
        traceSingleUpdate(r);
    }

    private void applyRegularUpdate0(NDefinition d1, String[] args) {
        InstallIdList li = new InstallIdList();
        InstallFlags flags = new InstallFlags();
        InstallHelper h = new InstallHelper((DefaultNWorkspace) NWorkspaceExt.of(), li, true, args == null ? new ArrayList<>() : Arrays.asList(args), null);
        InstallIdInfo uu = li.addAsInstalled(d1.id(), flags);
        uu.cacheItem = h.getCache(d1.id());
        uu.cacheItem.revalidate(d1);

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

        public abstract void fix();

        @Override
        public String toString() {
            return "FixAction{"
                    + "id=" + id
                    + ", problemKey='" + problemKey + '\''
                    + '}';
        }
    }
}
