/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.update;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.elem.NutsArrayElementBuilder;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.format.NutsPositionType;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.DefaultNutsUpdateResult;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceConfigModel;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsComparator;
import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.util.NutsStringUtils;

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
public class DefaultNutsUpdateCommand extends AbstractNutsUpdateCommand {

    private final NutsComparator<NutsId> LATEST_VERSION_FIRST = new NutsComparator<NutsId>() {
        @Override
        public int compare(NutsId x, NutsId y) {
            return -x.getVersion().compareTo(y.getVersion());
        }

        @Override
        public NutsElement describe(NutsSession session) {
            return NutsElements.of(session).ofString("latestVersionFirst");
        }
    };
    private final NutsComparator<NutsId> DEFAULT_THEN_LATEST_VERSION_FIRST = new NutsComparator<NutsId>() {
        @Override
        public int compare(NutsId x, NutsId y) {
            NutsInstalledRepository rr = NutsWorkspaceExt.of(ws).getInstalledRepository();
            int xi = rr.isDefaultVersion(x, session) ? 0 : 1;
            int yi = rr.isDefaultVersion(y, session) ? 0 : 1;
            int v = Integer.compare(xi, yi);
            if (v != 0) {
                return v;
            }
            return -x.getVersion().compareTo(y.getVersion());
        }

        @Override
        public NutsElement describe(NutsSession session) {
            return NutsElements.of(session).ofString("defaultThenLatestVersionFirst");
        }
    };
    private boolean checkFixes = false;
    private List<FixAction> resultFixes = null;

    public DefaultNutsUpdateCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public int getResultCount() {
        return getResult().getUpdatesCount();
    }

    @Override
    public NutsWorkspaceUpdateResult getResult() {
        checkSession();
        if (result == null) {
            checkUpdates();
        }
        if (result == null) {
            throw new NutsUnexpectedException(getSession());
        }
        return result;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch(a.getStringKey().orElse("")) {
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
    public NutsUpdateCommand update() {
        applyResult(getResult());
        return this;
    }

    @Override
    public NutsUpdateCommand checkUpdates() {
        if (checkFixes) {
            checkFixes();
            traceFixes();
        }
        checkSession();
        NutsSession session = getSession();
        Instant now = expireTime == null ? Instant.now() : expireTime;
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(session);
//        NutsWorkspaceCurrentConfig actualBootConfig = ws.config().current();
//        NutsWorkspaceCurrentConfig jsonBootConfig = getConfigManager().getBootContext();
        NutsSession session2 = NutsWorkspaceUtils.of(getSession()).validateSession(this.getSession());
        Map<String, NutsUpdateResult> allUpdates = new LinkedHashMap<>();
        Map<String, NutsUpdateResult> extUpdates = new LinkedHashMap<>();
        Map<String, NutsUpdateResult> regularUpdates = new HashMap<>();
        NutsUpdateResult apiUpdate = null;
        NutsVersion bootVersion0 = session2.getWorkspace().getApiVersion();
        NutsVersion bootVersion = bootVersion0;
        if (!(this.getApiVersion() == null || this.getApiVersion().isBlank())) {
            bootVersion = this.getApiVersion();
        }
        if (this.isApi() || !(this.getApiVersion() == null || this.getApiVersion().isBlank())) {
            apiUpdate = checkCoreUpdate(NutsId.of(NutsConstants.Ids.NUTS_API).get( session2), this.getApiVersion(), session2, Type.API, now);
            if (apiUpdate.isUpdatable()) {
                bootVersion = apiUpdate.getAvailable().getId().getVersion();
                allUpdates.put(NutsConstants.Ids.NUTS_API, apiUpdate);
            } else {
                //reset bootVersion
                bootVersion = bootVersion0;
            }
        }
        NutsUpdateResult runtimeUpdate = null;
        if (this.isRuntime()) {
            if (dws.requiresRuntimeExtension(session2)) {
                runtimeUpdate = checkCoreUpdate(NutsId.of(session2.getWorkspace().getRuntimeId().getShortName()).get( session2),
                        apiUpdate != null && apiUpdate.getAvailable()!=null && apiUpdate.getAvailable().getId() != null ? apiUpdate.getAvailable().getId().getVersion()
                                : bootVersion, session2, Type.RUNTIME, now);
                if (runtimeUpdate.isUpdatable()) {
                    allUpdates.put(runtimeUpdate.getId().getShortName(), runtimeUpdate);
                }
            }
        }

        if (this.isExtensions()) {
            for (NutsId d : getExtensionsToUpdate()) {
                NutsUpdateResult updated = checkRegularUpdate(d, Type.EXTENSION, bootVersion, now, expireTime != null);
                if (updated.isUpdatable()) {
                    allUpdates.put(updated.getId().getShortName(), updated);
                    extUpdates.put(updated.getId().getShortName(), updated);
                }
            }
        }

        if (this.isCompanions()) {
            for (NutsId d : getCompanionsToUpdate()) {
                NutsUpdateResult updated = checkRegularUpdate(d, Type.COMPANION, bootVersion, now, expireTime != null);
                if (updated.isUpdatable()) {
                    allUpdates.put(updated.getId().getShortName(), updated);
                    regularUpdates.put(updated.getId().getShortName(), updated);
                }
            }
        }

        for (NutsId id : this.getRegularIds()) {
            NutsUpdateResult updated = checkRegularUpdate(id, Type.REGULAR, null, now, expireTime != null);
            allUpdates.put(updated.getId().getShortName(), updated);
            regularUpdates.put(updated.getId().getShortName(), updated);
        }
        List<NutsId> lockedIds = this.getLockedIds();
        if (lockedIds.size() > 0) {
            for (NutsId d : new HashSet<>(lockedIds)) {
                NutsDependency dd = NutsDependency.of(d.toString()).get(session2);
                if (regularUpdates.containsKey(dd.getSimpleName())) {
                    NutsUpdateResult updated = regularUpdates.get(dd.getSimpleName());
                    //FIX ME
                    if (!dd.getVersion().filter(session2).acceptVersion(updated.getId().getVersion(), session2)) {
                        throw new NutsIllegalArgumentException(getSession(),
                                NutsMessage.cstyle("%s unsatisfied  : %s", dd, updated.getId().getVersion())
                        );
                    }
                }
            }
        }

        result = new DefaultNutsWorkspaceUpdateResult(apiUpdate, runtimeUpdate, new ArrayList<>(extUpdates.values()),
                new ArrayList<>(regularUpdates.values())
        );
        traceUpdates(result);
        return this;
    }

    private Set<NutsId> getExtensionsToUpdate() {
        Set<NutsId> ext = new HashSet<>();
        for (NutsId extension : getSession().extensions().getConfigExtensions()) {
            ext.add(extension.getShortId());
        }
        if (updateExtensions) {
            return ext;
        } else {
            Set<NutsId> ext2 = new HashSet<>();
            for (NutsId id : ids) {
                if (id.getShortName().equals(NutsConstants.Ids.NUTS_API)) {
                    continue;
                }
                if (id.getShortName().equals(ws.getRuntimeId().getShortName())) {
                    continue;
                }
                if (ext.contains(id.getShortId())) {
                    ext2.add(id.getShortId());
                }

            }
            return ext2;
        }
    }

    private Set<NutsId> getCompanionsToUpdate() {
        Set<NutsId> ext = new HashSet<>();
        for (NutsId extension : session.extensions().getCompanionIds()) {
            ext.add(extension.getShortId());
        }
        return ext;
    }

    private Set<NutsId> getRegularIds() {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        HashSet<String> extensions = new HashSet<>();
        for (NutsId object : getSession().extensions().getConfigExtensions()) {
            extensions.add(object.getShortName());
        }

        HashSet<NutsId> baseRegulars = new HashSet<>(ids);
        if (isInstalled()) {
            baseRegulars.addAll(getSession().search().setSession(getSession())
                    .setInstallStatus(NutsInstallStatusFilters.of(getSession()).byInstalled(true))
                    .getResultIds().stream().map(NutsId::getShortId).collect(Collectors.toList()));
            // This bloc is to handle packages that were installed by their jar/content but was removed for any reason!
            NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
            NutsInstalledRepository ir = dws.getInstalledRepository();
            for (NutsInstallInformation y : IteratorUtils.toList(ir.searchInstallInformation(session))) {
                if (y != null && y.getInstallStatus().isInstalled() && y.getId() != null) {
                    baseRegulars.add(y.getId().builder().setVersion("").build());
                }
            }
        }
        HashSet<NutsId> regulars = new HashSet<>();
        for (NutsId id : baseRegulars) {
            if (id.getShortName().equals(NutsConstants.Ids.NUTS_API)) {
                continue;
            }
            if (id.getShortName().equals(getSession().getWorkspace().getRuntimeId().getShortName())) {
                continue;
            }
            if (extensions.contains(id.getShortName())) {
                continue;
            }
            regulars.add(id);
        }
        return regulars;
    }

    public NutsUpdateCommand checkFixes() {
        resultFixes = null;
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsInstalledRepository ir = dws.getInstalledRepository();
        NutsSession session = NutsWorkspaceUtils.of(getSession()).validateSession(this.getSession());
        resultFixes = IteratorUtils.toList(IteratorUtils.convertNonNull(ir.searchInstallInformation(session), new Function<NutsInstallInformation, FixAction>() {
            @Override
            public FixAction apply(NutsInstallInformation nutsInstallInformation) {
                NutsId id = getSession().search().setInstallStatus(
                        NutsInstallStatusFilters.of(session).byInstalled(true)
                ).addId(nutsInstallInformation.getId()).getResultIds().first();
                if (id == null) {
                    return new FixAction(nutsInstallInformation.getId(), "MissingInstallation") {
                        @Override
                        public void fix(NutsSession session) {
                            session.install().addId(getId()).run();
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
            NutsPrintStream out = getSession().out();
            for (FixAction n : resultFixes) {
                out.printf("[```error FIX```] %s %s %n", n.getId(), n.getProblemKey());
            }
        }
    }

    protected void traceUpdates(NutsWorkspaceUpdateResult result) {
        checkSession();
        NutsSession session = getSession();
        NutsPrintStream out = getSession().out();
        List<NutsUpdateResult> all = result.getAllResults();
        List<NutsUpdateResult> updates = result.getUpdatable();
        List<NutsUpdateResult> notInstalled = result.getAllResults().stream()
                .filter(x -> !x.isInstalled()).collect(Collectors.toList());
        all.sort(new Comparator<NutsUpdateResult>() {
            private int itemOrder(NutsUpdateResult o) {
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
            public int compare(NutsUpdateResult o1, NutsUpdateResult o2) {
                return Integer.compare(itemOrder(o1), itemOrder(o2));
            }
        });
        if (getSession().isPlainTrace()) {
            if (notInstalled.size() == 0 && updates.size() == 0) {
                out.resetLine().printf("all packages are %s. You are running latest version%s.%n",
                        NutsTexts.of(session).ofStyled("up-to-date", NutsTextStyle.success()),
                        result.getAllResults().size() > 1 ? "s" : "");
            } else {
                if (updates.size() > 0 && notInstalled.size() > 0) {
                    out.resetLine().printf("workspace has %s package%s not installed and %s package%s to update.%n",
                            NutsTexts.of(session).ofStyled("" + notInstalled.size(), NutsTextStyle.primary1()),
                            (notInstalled.size() > 1 ? "s" : ""),
                            NutsTexts.of(session).ofStyled("" + updates.size(), NutsTextStyle.primary1()),
                            (updates.size() > 1 ? "s" : "")
                    );
                } else if (updates.size() > 0) {
                    out.resetLine().printf("workspace has %s package%s to update.%n", NutsTexts.of(session).ofStyled("" + updates.size(), NutsTextStyle.primary1()),
                            (updates.size() > 1 ? "s" : ""));
                } else if (notInstalled.size() > 0) {
                    out.resetLine().printf("workspace has %s package%s not installed.%n", NutsTexts.of(session).ofStyled("" + notInstalled.size(), NutsTextStyle.primary1()),
                            (notInstalled.size() > 1 ? "s" : ""));
                }
                int widthCol1 = 2;
                int widthCol2 = 2;
                for (NutsUpdateResult update : all) {
                    widthCol1 = Math.max(widthCol1, update.getAvailable() == null ? 0 : update.getAvailable().getId().getShortName().length());
                    widthCol2 = Math.max(widthCol2, update.getInstalled() == null ? 0 : update.getInstalled().getId().getVersion().toString().length());
                }
                NutsTexts factory = NutsTexts.of(session);
                for (NutsUpdateResult update : all) {
                    if (update.getInstalled() == null) {
                        out.printf("%s  : %s%n",
                                factory.ofStyled(NutsStringUtils.formatAlign(update.getId().toString(), widthCol2, NutsPositionType.FIRST), NutsTextStyle.primary6()),
                                factory.ofStyled("not installed", NutsTextStyle.error()));
                    } else if (update.isUpdateVersionAvailable()) {
                        out.printf("%s  : %s => %s%n",
                                factory.ofStyled(NutsStringUtils.formatAlign(update.getInstalled().getId().getVersion().toString(), widthCol2,NutsPositionType.FIRST), NutsTextStyle.primary6()),
                                NutsStringUtils.formatAlign(update.getAvailable().getId().getShortName(), widthCol1,NutsPositionType.FIRST),
                                factory.ofPlain(update.getAvailable().getId().getVersion().toString()));
                    } else if (update.isUpdateStatusAvailable()) {
                        out.printf("%s  : %s => %s%n",
                                factory.ofStyled(NutsStringUtils.formatAlign(update.getInstalled().getId().getVersion().toString(), widthCol2,NutsPositionType.FIRST), NutsTextStyle.primary6()),
                                NutsStringUtils.formatAlign(update.getAvailable().getId().getShortName(), widthCol1,NutsPositionType.FIRST),
                                factory.ofStyled("set as default", NutsTextStyle.primary4()));
                    } else {
                        out.printf("%s  : %s%n",
                                factory.ofStyled(NutsStringUtils.formatAlign(update.getInstalled().getId().getVersion().toString(), widthCol2,NutsPositionType.FIRST), NutsTextStyle.primary6()),
                                factory.ofStyled("up-to-date", NutsTextStyle.warn()));
                    }
                }
            }
        } else {
            NutsElements e = NutsElements.of(getSession());

            if (updates.size() == 0 && notInstalled.size() == 0) {
                out.printlnf(e.ofObject()
                        .set("message", "all packages are up-to-date. You are running latest version" + (result.getAllResults().size() > 1 ? "s" : "") + ".")
                        .build());
            } else {
                NutsArrayElementBuilder arrayElementBuilder = e.ofArray();
                for (NutsUpdateResult update : all) {
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
                out.printlnf(arrayElementBuilder.build());
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
    private NutsFetchCommand latestOnlineDependencies(NutsFetchCommand se) {
        se.setDependencies(true);
        if (scopes.isEmpty()) {
            se.addScope(NutsDependencyScopePattern.RUN);
        } else {
            se.addScopes(scopes.toArray(new NutsDependencyScope[0]));
        }
        se.setOptional(isOptional() ? null : false)
                .setSession(se.getSession().copy().setFetchStrategy(NutsFetchStrategy.ONLINE));
        return se;
    }

    protected NutsUpdateResult checkRegularUpdate(NutsId id, Type type, NutsVersion targetApiVersion, Instant now, boolean updateEvenIfExisting) {
        checkSession();
        NutsSession session = getSession();
        NutsVersion version = id.getVersion();
        if (!updateEvenIfExisting && version.isSingleValue()) {
            updateEvenIfExisting = session.getTerminal().ask()
                    .resetLine()
                    .setDefaultValue(true).setSession(session)
                    .forBoolean("version is too restrictive. Do you intend to force update of %s ?", id).getBooleanValue();
        }
        DefaultNutsUpdateResult r = new DefaultNutsUpdateResult();
        r.setId(id.getShortId());
        boolean shouldUpdateDefault = false;
        NutsDefinition d0 = session.search().addId(id).setSession(session)
                .setInstallStatus(NutsInstallStatusFilters.of(session).byDeployed(true))
                .setOptional(false).setFailFast(false)//.setDefaultVersions(true)
                .sort(DEFAULT_THEN_LATEST_VERSION_FIRST)
                .getResultDefinitions().first();
        if (d0 == null) {
            //should not throw exception here, this is a check and not update method
            return r;
        }
        if (!d0.getInstallInformation().get(session).isDefaultVersion()) {
            shouldUpdateDefault = true;
        }
        //search latest parse
        NutsSession newAnywhereSession = session
                .copy()
                .setFetchStrategy(NutsFetchStrategy.ANYWHERE);
        if (updateEvenIfExisting) {
            newAnywhereSession.setExpireTime(now);
        }

        NutsSearchCommand sc = session.search().addId(d0.getId().getShortId())
                .setSession(newAnywhereSession)
                .setFailFast(false)
                .setLatest(true)
                .addLockedIds(getLockedIds())
                .addRepositoryFilter(NutsRepositoryFilters.of(session).installedRepo().neg())
                .setDependencies(true)
                .setOptional(isOptional() ? null : false);
        if (type == Type.EXTENSION) {
            sc.setExtension(true);
        } else if (type == Type.COMPANION) {
            sc.setCompanion(true);
        }
        if (targetApiVersion != null) {
            sc.setTargetApiVersion(targetApiVersion);
        }

        if (scopes.isEmpty()) {
            sc.addScope(NutsDependencyScopePattern.RUN);
        } else {
            sc.addScopes(scopes.toArray(new NutsDependencyScope[0]));
        }
        NutsDefinition d1 = sc.getResultDefinitions().first();
        r.setInstalled(d0);
        r.setAvailable(d1);
        if (d1 == null) {
            //this is very interesting. Why the hell is this happening?
            r.setAvailable(d0);
        } else {
            NutsVersion v0 = d0.getId().getVersion();
            NutsVersion v1 = d1.getId().getVersion();
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

    private NutsFetchCommand fetch0() {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        return session.fetch().setContent(true).setEffective(true);
    }

    private void applyFixes() {
        if (resultFixes != null) {
            NutsSession session = getSession();
            NutsPrintStream out = session.out();
            for (FixAction n : resultFixes) {
                n.fix(session);
                out.printf("[```error FIX```] unable to %s %s %n", n.getId(), n.getProblemKey());
            }
        }
    }

    private void applyResult(NutsWorkspaceUpdateResult result) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        applyFixes();
        NutsUpdateResult apiUpdate = result.getApi();
        NutsUpdateResult runtimeUpdate = result.getRuntime();
        List<NutsId> notInstalled = result.getAllResults().stream()
                .filter(x -> x.getInstalled() == null) //not installed
                .map(NutsUpdateResult::getId)
                .collect(Collectors.toList());
        if (!notInstalled.isEmpty()) {
            if (notInstalled.size() == 1) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("%s is not yet installed for it to be updated.", notInstalled.get(0)));
            } else {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("%s are not yet installed for them to be updated.", notInstalled));
            }
        }
        if (result.getUpdatesCount() == 0) {
            return;
        }
        NutsWorkspaceUtils.of(getSession()).checkReadOnly();
        boolean requireSave = false;
        NutsSession validWorkspaceSession = getSession();
        final NutsPrintStream out = validWorkspaceSession.out();
        boolean accept = getSession().config().getDefaultTerminal().ask()
                .resetLine()
                .forBoolean("would you like to apply updates?").setDefaultValue(true)
                .setSession(validWorkspaceSession).getValue();
        if (validWorkspaceSession.isAsk() && !accept) {
            throw new NutsUserCancelException(getSession());
        }
        NutsWorkspaceConfigManagerExt wcfg = NutsWorkspaceConfigManagerExt.of(session.config());
        boolean apiUpdateAvailable = apiUpdate != null && apiUpdate.getAvailable() != null && !apiUpdate.isUpdateApplied();
        boolean runtimeUpdateAvailable = runtimeUpdate != null && runtimeUpdate.getAvailable() != null && !runtimeUpdate.isUpdateApplied();
        boolean apiUpdateApplicable = apiUpdateAvailable && !apiUpdate.isUpdateApplied();
        boolean runtimeUpdateApplicable = runtimeUpdateAvailable && !runtimeUpdate.isUpdateApplied();
        NutsId finalApiId = apiUpdateAvailable ? apiUpdate.getAvailable().getId() : ws.getApiId();
        NutsId finalRuntimeId = runtimeUpdateApplicable ? runtimeUpdate.getAvailable().getId() : ws.getRuntimeId();
        if (apiUpdateApplicable || runtimeUpdateApplicable) {
            //wcfg.getModel().prepareBootApi(finalApiId, finalRuntimeId, true, validWorkspaceSession);
        }
        if (apiUpdateApplicable) {
            applyRegularUpdate(((DefaultNutsUpdateResult) apiUpdate));
            ((DefaultNutsUpdateResult) apiUpdate).setUpdateApplied(true);
            traceSingleUpdate(apiUpdate);
        }
        if (runtimeUpdateApplicable) {
//            wcfg.getModel().prepareBootRuntime(finalRuntimeId, true, validWorkspaceSession);

            applyRegularUpdate(((DefaultNutsUpdateResult) runtimeUpdate));
            ((DefaultNutsUpdateResult) runtimeUpdate).setUpdateApplied(true);
            List<NutsId> baseApiIds = CoreNutsUtils.resolveNutsApiIdsFromIdList(runtimeUpdate.getDependencies(), session);
            DefaultNutsWorkspaceConfigModel configModel = NutsWorkspaceExt.of(session).getModel().configModel;
            for (NutsId newApi : baseApiIds) {
                configModel.setExtraBootRuntimeId(
                        newApi,
                        runtimeUpdate.getAvailable().getId(),
                        runtimeUpdate.getAvailable().getDependencies().get(session).transitive().toList(),
                        session);
            }
            traceSingleUpdate(runtimeUpdate);
        }
        for (NutsUpdateResult extension : result.getExtensions()) {
            if (!extension.isUpdateApplied()) {
                if (extension.getAvailable() != null) {
                    applyRegularUpdate(((DefaultNutsUpdateResult) extension));
                    List<NutsId> baseApiIds = CoreNutsUtils.resolveNutsApiIdsFromIdList(extension.getDependencies(), session);
                    DefaultNutsWorkspaceConfigModel configModel = NutsWorkspaceExt.of(session).getModel().configModel;
                    for (NutsId newApi : baseApiIds) {
                        configModel.setExtraBootExtensionId(
                                newApi,
                                extension.getAvailable().getId(),
                                extension.getAvailable().getDependencies().get(session).transitive().toArray(NutsDependency[]::new),
                                session);
                    }
                    ((DefaultNutsUpdateResult) extension).setUpdateApplied(true);
                    traceSingleUpdate(extension);
                }
            }
        }
        for (NutsUpdateResult component : result.getArtifacts()) {
            applyRegularUpdate((DefaultNutsUpdateResult) component);
        }

        if (session.config().setSession(validWorkspaceSession).save(requireSave)) {
            if (_LOG(session).isLoggable(Level.INFO)) {
                _LOGOP(session).level(Level.INFO).verb(NutsLoggerVerb.WARNING)
                        .log(NutsMessage.jstyle("workspace is updated. Nuts should be restarted for changes to take effect."));
            }
            if (apiUpdate != null && apiUpdate.isUpdatable() && !apiUpdate.isUpdateApplied()) {
                if (validWorkspaceSession.isPlainTrace()) {
                    out.println("workspace is updated. Nuts should be restarted for changes to take effect.");
                }
            }
        }
    }

    private void traceSingleUpdate(NutsUpdateResult r) {
        checkSession();
        NutsSession session = getSession();
        NutsId id = r.getId();
        NutsDefinition d0 = r.getInstalled();
        NutsDefinition d1 = r.getAvailable();
//        final String simpleName = d0 != null ? d0.getId().getShortName() : d1 != null ? d1.getId().getShortName() : id.getShortName();
        final NutsId simpleId = d0 != null ? d0.getId().getShortId() : d1 != null ? d1.getId().getShortId() : id.getShortId();
        final NutsPrintStream out = getSession().out();
        NutsTexts factory = NutsTexts.of(session);
        if (r.isUpdateApplied()) {
            if (r.isUpdateForced()) {
                if (d0 == null) {
                    out.resetLine().printf("%s is %s to latest version %s%n",
                            simpleId,
                            factory.ofStyled("updated", NutsTextStyle.primary3()),
                            d1 == null ? null : d1.getId().getVersion()
                    );
                } else if (d1 == null) {
                    //this is very interesting. Why the hell is this happening?
                } else {
                    NutsVersion v0 = d0.getId().getVersion();
                    NutsVersion v1 = d1.getId().getVersion();
                    if (v1.compareTo(v0) <= 0) {
                        if (v1.compareTo(v0) == 0) {
                            out.resetLine().printf("%s is %s to %s %n",
                                    simpleId,
                                    factory.ofStyled("forced", NutsTextStyle.primary3()),
                                    d0.getId().getVersion());
                        } else {
                            out.resetLine().printf("%s is %s from %s to older version %s%n",
                                    simpleId,
                                    factory.ofStyled("forced", NutsTextStyle.primary3()),
                                    d0.getId().getVersion(), d1.getId().getVersion());
                        }
                    } else {
                        out.resetLine().printf("%s is %s from %s to latest version %s%n",
                                simpleId,
                                factory.ofStyled("updated", NutsTextStyle.primary3()),
                                d0.getId().getVersion(), d1.getId().getVersion());
                    }
                }
            }
        }
    }

    public NutsUpdateResult checkCoreUpdate(NutsId id, NutsVersion bootApiVersion, NutsSession session, Type type, Instant now) {
        //disable trace so that search do not write to stream
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        NutsId oldId = null;
        NutsDefinition oldFile = null;
        NutsDefinition newFile = null;
        NutsId newId = null;
//        List<NutsId> dependencies = new ArrayList<>();
//        NutsSession sessionOffline = session.copy().setFetchMode(NutsFetchMode.OFFLINE);
        switch (type) {
            case API: {
                oldId = session.config().stored().getApiId();
                NutsId confId = session.config().stored().getApiId();
                if (confId != null) {
                    oldId = confId;
                }
                NutsVersion v = bootApiVersion;
                if (v == null || v.isBlank()) {
                    v = NutsVersion.of(NutsConstants.Versions.LATEST).get( getSession());
                }
                try {
                    oldFile = fetch0().setId(oldId).setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ONLINE)).getResultDefinition();
                } catch (NutsNotFoundException ex) {
                    //ignore
                }
                try {
                    newId = getSession().search().setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ANYWHERE))
                            .addId(NutsConstants.Ids.NUTS_API + "#" + v).setLatest(true).getResultIds().first();
                    newFile = newId == null ? null : latestOnlineDependencies(fetch0()).setFailFast(false).setSession(session).setId(newId).getResultDefinition();
                } catch (NutsNotFoundException ex) {
                    _LOGOP(session).level(Level.SEVERE).error(ex).log(NutsMessage.jstyle("error : {0}", ex));
                    //ignore
                }
                break;
            }
            case RUNTIME: {
                oldId = ws.getRuntimeId();
                NutsId confId = getSession().config().stored().getRuntimeId();
                if (confId != null) {
                    oldId = confId;
                }
                if (oldId != null) {
                    try {
                        oldFile = fetch0().setId(oldId).setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ONLINE)).getResultDefinition();
                    } catch (NutsNotFoundException ex) {
                        _LOGOP(session).level(Level.SEVERE).error(ex).log(NutsMessage.jstyle("error : {0}", ex));
                        //ignore
                    }
                }
                try {
                    NutsSearchCommand se = getSession().search()
                            .addId(oldFile != null ? oldFile.getId().builder().setVersion("").build().toString() : NutsConstants.Ids.NUTS_RUNTIME)
                            .setRuntime(true)
                            .setTargetApiVersion(bootApiVersion)
                            .addLockedIds(getLockedIds())
                            .setLatest(true)
                            .setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ANYWHERE))
                            .sort(LATEST_VERSION_FIRST);
                    newId = se.getResultIds().first();
                    newFile = newId == null ? null : latestOnlineDependencies(fetch0().setId(newId))
                            .setSession(session)
                            .setFailFast(false)
                            .getResultDefinition();
                } catch (NutsNotFoundException ex) {
                    _LOGOP(session).level(Level.SEVERE).error(ex).log(NutsMessage.jstyle("error : {0}", ex));
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
//                    _LOGOP(session).level(Level.SEVERE).error(ex).log("error : {0}", ex);
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
//                    _LOGOP(session).level(Level.SEVERE).error(ex).log("error : {0}", ex);
//                    //ignore
//                }
//                break;
//            }
        }
        //compare canonical forms
        NutsId cnewId = toCanonicalForm(newId);
        NutsId coldId = toCanonicalForm(oldId);
        DefaultNutsUpdateResult defaultNutsUpdateResult = new DefaultNutsUpdateResult(id, oldFile, newFile,
                newFile == null ? null : newFile.getDependencies().get(session).transitive().map(NutsDependency::toId, "toId").toList(),
                false);
        if (cnewId != null && newFile != null && coldId != null && cnewId.getVersion().compareTo(coldId.getVersion()) > 0) {
            defaultNutsUpdateResult.setUpdateVersionAvailable(true);
        }
        return defaultNutsUpdateResult;
    }

    private NutsId toCanonicalForm(NutsId id) {
        if (id != null) {
            id = id.builder().setRepository(null).build();
            String oldValue = id.getProperties().get(NutsConstants.IdProperties.FACE);
            if (oldValue != null && oldValue.trim().isEmpty()) {
                id = id.builder().setProperty(NutsConstants.IdProperties.FACE, null).build();
            }
        }
        return id;
    }

    private void applyRegularUpdate(DefaultNutsUpdateResult r) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        if (r.isUpdateApplied()) {
            return;
        }
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = getSession();
        final NutsPrintStream out = session.out();
//        NutsId id = r.getId();
        NutsDefinition d0 = r.getInstalled();
        NutsDefinition d1 = r.getAvailable();
        if (d0 == null) {
            getSession().security().checkAllowed(NutsConstants.Permissions.UPDATE, "update");
            dws.updateImpl(d1, new String[0], true, session);
            r.setUpdateApplied(true);
        } else if (d1 == null) {
            //this is very interesting. Why the hell is this happening?
        } else {
            NutsVersion v0 = d0.getId().getVersion();
            NutsVersion v1 = d1.getId().getVersion();
            if (v1.compareTo(v0) <= 0) {
                //no update needed!
                if (/*session.isYes() || */r.isUpdateForced()) {
                    getSession().security().checkAllowed(NutsConstants.Permissions.UPDATE, "update");
                    dws.updateImpl(d1, new String[0], true, session);
                    r.setUpdateApplied(true);
                    r.setUpdateForced(true);
                } else {
                    dws.getInstalledRepository().setDefaultVersion(d1.getId(), session);
                }
            } else {
                getSession().security().checkAllowed(NutsConstants.Permissions.UPDATE, "update");
                dws.updateImpl(d1, new String[0], true, session);
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

        private final NutsId id;
        private final String problemKey;

        public FixAction(NutsId id, String problemKey) {
            this.id = id;
            this.problemKey = problemKey;
        }

        public NutsId getId() {
            return id;
        }

        public String getProblemKey() {
            return problemKey;
        }

        public abstract void fix(NutsSession session);

        @Override
        public String toString() {
            return "FixAction{"
                    + "id=" + id
                    + ", problemKey='" + problemKey + '\''
                    + '}';
        }
    }
}
