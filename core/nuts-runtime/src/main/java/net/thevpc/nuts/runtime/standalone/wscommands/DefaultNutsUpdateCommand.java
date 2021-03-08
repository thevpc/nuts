/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.repos.NutsInstalledRepository;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;
import net.thevpc.nuts.runtime.core.commands.ws.DefaultNutsUpdateResult;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspaceUpdateResult;
import net.thevpc.nuts.runtime.standalone.NutsExtensionListHelper;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNutsUpdateCommand extends AbstractNutsUpdateCommand {

    private Comparator<NutsId> LATEST_VERSION_FIRST = (x, y) -> -x.getVersion().compareTo(y.getVersion());
    private Comparator<NutsId> DEFAULT_THEN_LATEST_VERSION_FIRST = (x, y) -> {
        NutsInstalledRepository rr = NutsWorkspaceExt.of(ws).getInstalledRepository();
        int xi = rr.isDefaultVersion(x, session) ? 0 : 1;
        int yi = rr.isDefaultVersion(y, session) ? 0 : 1;
        int v = Integer.compare(xi, yi);
        if (v != 0) {
            return v;
        }
        return -x.getVersion().compareTo(y.getVersion());
    };

    public final NutsLogger LOG;
    private boolean checkFixes = false;
    private List<FixAction> resultFixes = null;

    public DefaultNutsUpdateCommand(NutsWorkspace ws) {
        super(ws);
        LOG = ws.log().of(DefaultNutsUpdateCommand.class);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isEnabled();
        switch (a.getStringKey()) {
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
    public NutsUpdateCommand update() {
        applyResult(getResult());
        return this;
    }

    private Set<NutsId> getExtensionsToUpdate() {
        Set<NutsId> ext = new HashSet<>();
        for (NutsId extension : ws.extensions().getConfigExtensions(session)) {
            ext.add(extension.getShortNameId());
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
                if (ext.contains(id.getShortNameId())) {
                    ext2.add(id.getShortNameId());
                }

            }
            return ext2;
        }
    }

    private Set<NutsId> getCompanionsToUpdate() {
        Set<NutsId> ext = new HashSet<>();
        for (NutsId extension : ws.getCompanionIds()) {
            ext.add(extension.getShortNameId());
        }
        return ext;
    }

    private Set<NutsId> getRegularIds() {
        HashSet<String> extensions = new HashSet<>();
        for (NutsId object : ws.extensions().getConfigExtensions(session)) {
            extensions.add(object.getShortName());
        }

        HashSet<NutsId> baseRegulars = new HashSet<>(ids);
        if (isInstalled()) {
            baseRegulars.addAll(ws.search().setSession(CoreNutsUtils.silent(getValidWorkspaceSession()))
                    .setInstallStatus(ws.filters().installStatus().byInstalled(true))
                    .getResultIds().stream().map(NutsId::getShortNameId).collect(Collectors.toList()));
            // This bloc is to handle packages that were installed by their jar/content was removed for any reason!
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
            if (id.getShortName().equals(ws.getRuntimeId().getShortName())) {
                continue;
            }
            if (extensions.contains(id.getShortName())) {
                continue;
            }
            regulars.add(id);
        }
        return regulars;
    }

    private static abstract class FixAction {

        private NutsId id;
        private String problemKey;

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

    public NutsUpdateCommand checkFixes() {
        resultFixes = null;
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsInstalledRepository ir = dws.getInstalledRepository();
        NutsSession session = NutsWorkspaceUtils.of(ws).validateSession(this.getValidWorkspaceSession());
        resultFixes = IteratorUtils.toList(IteratorUtils.convertNonNull(ir.searchInstallInformation(session), new Function<NutsInstallInformation, FixAction>() {
            @Override
            public FixAction apply(NutsInstallInformation nutsInstallInformation) {
                NutsId id = ws.search().setInstallStatus(
                        ws.filters().installStatus().byInstalled(true)
                ).addId(nutsInstallInformation.getId()).getResultIds().first();
                if (id == null) {
                    return new FixAction(nutsInstallInformation.getId(), "MissingInstallation") {
                        @Override
                        public void fix(NutsSession session) {
                            session.getWorkspace().install().id(getId()).run();
                        }
                    };
                }
                return null;
            }
        }, "CheckFixes"));
        return this;
    }

    @Override
    public NutsUpdateCommand checkUpdates() {
        if (checkFixes) {
            checkFixes();
            traceFixes();
        }
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
//        NutsWorkspaceCurrentConfig actualBootConfig = ws.config().current();
//        NutsWorkspaceCurrentConfig jsonBootConfig = getConfigManager().getBootContext();
        NutsSession session = NutsWorkspaceUtils.of(ws).validateSession(this.getValidWorkspaceSession());
        Map<String, NutsUpdateResult> allUpdates = new LinkedHashMap<>();
        Map<String, NutsUpdateResult> extUpdates = new LinkedHashMap<>();
        Map<String, NutsUpdateResult> regularUpdates = new HashMap<>();
        NutsUpdateResult apiUpdate = null;
        String bootVersion0 = ws.getApiVersion();
        String bootVersion = bootVersion0;
        if (!CoreStringUtils.isBlank(this.getApiVersion())) {
            bootVersion = this.getApiVersion();
        }
        if (this.isApi() || !CoreStringUtils.isBlank(this.getApiVersion())) {
            apiUpdate = checkCoreUpdate(ws.id().parser().parse(NutsConstants.Ids.NUTS_API), this.getApiVersion(), session, "api");
            if (apiUpdate.isUpdateAvailable()) {
                bootVersion = apiUpdate.getAvailable().getId().getVersion().getValue();
                allUpdates.put(NutsConstants.Ids.NUTS_API, apiUpdate);
            } else {
                //reset bootVersion
                bootVersion = bootVersion0;
            }
        }
        NutsUpdateResult runtimeUpdate = null;
        if (this.isRuntime()) {
            if (dws.requiresRuntimeExtension(session)) {
                runtimeUpdate = checkCoreUpdate(ws.id().parser().parse(ws.getRuntimeId().getShortName()),
                        apiUpdate != null && apiUpdate.getAvailable().getId() != null ? apiUpdate.getAvailable().getId().getVersion().toString()
                        : bootVersion, session, "runtime");
                if (runtimeUpdate.isUpdateAvailable()) {
                    allUpdates.put(runtimeUpdate.getId().getShortName(), runtimeUpdate);
                }
            }
        }

        if (this.isExtensions()) {
            for (NutsId ext : getExtensionsToUpdate()) {
                NutsUpdateResult extUpdate = checkCoreUpdate(ext, bootVersion, session, "extension");
                allUpdates.put(extUpdate.getId().getShortName(), extUpdate);
                extUpdates.put(extUpdate.getId().getShortName(), extUpdate);
            }
        }

        if (this.isCompanions()) {
            for (NutsId ext : getCompanionsToUpdate()) {
                NutsUpdateResult extUpdate = checkCoreUpdate(ext, bootVersion, session, "companion");
                allUpdates.put(extUpdate.getId().getShortName(), extUpdate);
                regularUpdates.put(extUpdate.getId().getShortName(), extUpdate);
            }
        }

        for (NutsId id : this.getRegularIds()) {
            NutsUpdateResult updated = checkRegularUpdate(id);
            allUpdates.put(updated.getAvailable().getId().getShortName(), updated);
            regularUpdates.put(updated.getId().getShortName(), updated);
        }
        NutsId[] lockedIds = this.getLockedIds();
        if (lockedIds.length > 0) {
            for (NutsId d : new HashSet<>(Arrays.asList(lockedIds))) {
                NutsDependency dd = getWorkspace().dependency().parser().parseDependency(d.toString());
                if (regularUpdates.containsKey(dd.getSimpleName())) {
                    NutsUpdateResult updated = regularUpdates.get(dd.getSimpleName());
                    //FIX ME
                    if (!dd.getVersion().filter().acceptVersion(updated.getId().getVersion(), session)) {
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

    protected void traceFixes() {
        if (resultFixes != null) {
            PrintStream out = CoreIOUtils.resolveOut(getValidWorkspaceSession());
            for (FixAction n : resultFixes) {
                out.printf("[```error FIX```] %s %s %n", n.getId(), n.getProblemKey());
            }
        }
    }

    protected void traceUpdates(NutsWorkspaceUpdateResult result) {
        if (getValidWorkspaceSession().isPlainTrace()) {
            PrintStream out = CoreIOUtils.resolveOut(getValidWorkspaceSession());
            NutsUpdateResult[] updates = result.getAllUpdates();
            if (updates.length == 0) {
                out.printf("All components are [[up-to-date]]. You are running latest version%s.%n", result.getAllResults().length > 1 ? "s" : "");
            } else {
                out.printf("Workspace has %s component%s to update.%n", ws.formats().text().styled("" + updates.length, NutsTextNodeStyle.primary(1)),
                        (updates.length > 1 ? "s" : ""));
                int widthCol1 = 2;
                int widthCol2 = 2;
                for (NutsUpdateResult update : updates) {
                    widthCol1 = Math.max(widthCol1, update.getAvailable().getId().getShortName().length());
                    widthCol2 = Math.max(widthCol2, update.getLocal().getId().getVersion().toString().length());
                }
                NutsTextManager factory = getWorkspace().formats().text();
                for (NutsUpdateResult update : updates) {
                    if (update.isUpdateVersionAvailable()) {
                        out.printf("%s  : %s => %s%n",
                                factory.styled(CoreStringUtils.alignLeft(update.getLocal().getId().getVersion().toString(), widthCol2), NutsTextNodeStyle.primary(6)),
                                CoreStringUtils.alignLeft(update.getAvailable().getId().getShortName(), widthCol1),
                                factory.plain(update.getAvailable().getId().getVersion().toString()), NutsTextNodeStyle.primary(4));
                    } else if (update.isUpdateStatusAvailable()) {
                        out.printf("%s  : %s => %s%n",
                                factory.styled(CoreStringUtils.alignLeft(update.getLocal().getId().getVersion().toString(), widthCol2), NutsTextNodeStyle.primary(6)),
                                CoreStringUtils.alignLeft(update.getAvailable().getId().getShortName(), widthCol1),
                                factory.styled("set as default", NutsTextNodeStyle.primary(4)));
                    }
                }
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
                .setSession(se.getSession().copy().setFetchStrategy(NutsFetchStrategy.ONLINE))
                ;
        return se;
    }

    protected NutsUpdateResult checkRegularUpdate(NutsId id) {
        NutsSession session = getValidWorkspaceSession();
        NutsSession searchSession = CoreNutsUtils.silent(session);
        NutsVersion version = id.getVersion();
        if (version.isSingleValue()) {
            throw new NutsIllegalArgumentException(ws, id + " : version is too restrictive. You should use fetch or install instead");
        }

        DefaultNutsUpdateResult r = new DefaultNutsUpdateResult();
        r.setId(id.getShortNameId());
        boolean shouldUpdateDefault = false;
        NutsId d0Id = ws.search().addId(id).setSession(searchSession)
                .setInstallStatus(ws.filters().installStatus().byDeployed(true))
                .setOptional(false).setFailFast(false).setDefaultVersions(true)
                .getResultIds().first();
        if (d0Id == null) {
            // may be the id is not default!
            d0Id = ws.search().addId(id).setSession(searchSession)
                    .setInstallStatus(ws.filters().installStatus().byDeployed(true))
                    .setOptional(false).setFailFast(false).setLatest(true)
                    .getResultIds().first();
            if (d0Id != null) {
                shouldUpdateDefault = true;
            }
        }
        if (d0Id == null) {
            throw new NutsIllegalArgumentException(ws, id + " is not yet installed to be updated.");
        }
        NutsDefinition d0 = fetch0().setId(d0Id)
                .setSession(searchSession).installed().setOptional(false).setFailFast(false)
                .getResultDefinition();
        if (d0 == null) {
            throw new NutsIllegalArgumentException(ws, d0Id + " installation is broken and cannot be updated.");
        }
        //search latest parse
        NutsId d1Id = ws.search().addId(d0Id.getShortNameId())
                .setSession(searchSession.copy().setFetchStrategy(NutsFetchStrategy.ANYWHERE))
                .setFailFast(false)
                .setLatest(true)
                .getResultIds().first();
        //then fetch its definition!
        NutsDefinition d1 = d1Id == null ? null : latestOnlineDependencies(fetch0().setId(d1Id)
                .setSession(searchSession))
                .setFailFast(false)
                .getResultDefinition();
        r.setLocal(d0);
        r.setAvailable(d1);
        if (d0 == null) {
            if (!this.isEnableInstall()) {
                throw new NutsIllegalArgumentException(ws, "no version is installed to be updated for " + id);
            }
            if (d1 == null) {
                throw new NutsNotFoundException(ws, id);
            }
            r.setUpdateVersionAvailable(true);
            r.setUpdateForced(false);
        } else if (d1 == null) {
            //this is very interisting. Why the hell is this happening?
            r.setAvailable(d0);
        } else {
            NutsVersion v0 = d0.getId().getVersion();
            NutsVersion v1 = d1.getId().getVersion();
            if (v1.compareTo(v0) <= 0) {
                //no update needed!
                if (session.isYes()) {
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
        return ws.fetch().setContent(true).setEffective(true);
    }

    private void applyFixes() {
        if (resultFixes != null) {
            NutsSession session = getValidWorkspaceSession();
            PrintStream out = CoreIOUtils.resolveOut(session);
            for (FixAction n : resultFixes) {
                n.fix(session);
                out.printf("[```error FIX```] unable to %s %s %n", n.getId(), n.getProblemKey());
            }
        }
    }

    private void applyResult(NutsWorkspaceUpdateResult result) {
        applyFixes();
        NutsUpdateResult apiUpdate = result.getApi();
        NutsUpdateResult runtimeUpdate = result.getRuntime();
        if (result.getUpdatesCount() == 0) {
            return;
        }
        NutsWorkspaceUtils.of(ws).checkReadOnly();
        boolean requireSave = false;
        NutsSession validWorkspaceSession = getValidWorkspaceSession();
        final PrintStream out = CoreIOUtils.resolveOut(validWorkspaceSession);
        boolean accept = ws.io().term().getTerminal().ask()
                .forBoolean("Would you like to apply updates?").setDefaultValue(true)
                .setSession(validWorkspaceSession).getValue();
        if (validWorkspaceSession.isAsk() && !accept) {
            throw new NutsUserCancelException(ws);
        }
        NutsWorkspaceConfigManagerExt wcfg = NutsWorkspaceConfigManagerExt.of(ws.config());
        boolean apiUpdateAvailable = apiUpdate != null && apiUpdate.getAvailable() != null && !apiUpdate.isUpdateApplied();
        boolean runtimeUpdateAvailable = runtimeUpdate != null && runtimeUpdate.getAvailable() != null && !runtimeUpdate.isUpdateApplied();
        boolean apiUpdateApplicable = apiUpdateAvailable && !apiUpdate.isUpdateApplied();
        boolean runtimeUpdateApplicable = runtimeUpdateAvailable && !runtimeUpdate.isUpdateApplied();
        NutsId finalApiId = apiUpdateAvailable ? apiUpdate.getAvailable().getId() : ws.getApiId();
        NutsId finalRuntimeId = runtimeUpdateApplicable ? runtimeUpdate.getAvailable().getId() : ws.getRuntimeId();
        if (apiUpdateApplicable || runtimeUpdateApplicable) {
            wcfg.prepareBootApi(finalApiId, finalRuntimeId, true, validWorkspaceSession);
        }
        if (apiUpdateApplicable) {
            ((DefaultNutsUpdateResult) apiUpdate).setUpdateApplied(true);
            traceSingleUpdate(apiUpdate);
        }
        if (runtimeUpdateApplicable) {
            wcfg.prepareBootRuntime(finalRuntimeId, true, validWorkspaceSession);
        }
        if (runtimeUpdateApplicable) {
            ((DefaultNutsUpdateResult) runtimeUpdate).setUpdateApplied(true);
            traceSingleUpdate(runtimeUpdate);
        }
        for (NutsUpdateResult extension : result.getExtensions()) {
            NutsId finalExtensionId = extension.getAvailable() == null ? extension.getLocal().getId() : extension.getAvailable().getId();
            wcfg.prepareBootExtension(finalExtensionId, true, validWorkspaceSession);
        }
        NutsExtensionListHelper h = new NutsExtensionListHelper(wcfg.getStoredConfigBoot().getExtensions())
                .save();
        for (NutsUpdateResult extension : result.getExtensions()) {
            if (!extension.isUpdateApplied()) {
                if (extension.getAvailable() != null) {
                    h.add(extension.getAvailable().getId());
                    if (h.hasChanged()) {
                        NutsWorkspaceExt.of(ws).deployBoot(validWorkspaceSession, extension.getAvailable().getId(), true);
                    }
                }
                ((DefaultNutsUpdateResult) extension).setUpdateApplied(true);
                traceSingleUpdate(extension);
            }
        }
        for (NutsUpdateResult component : result.getArtifacts()) {
            applyRegularUpdate((DefaultNutsUpdateResult) component);
        }

        if (ws.config().save(requireSave, validWorkspaceSession)) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.with().session(session).level(Level.INFO).verb(NutsLogVerb.WARNING).log("Workspace is updated. Nuts should be restarted for changes to take effect.");
            }
            if (apiUpdate != null && apiUpdate.isUpdateAvailable() && !apiUpdate.isUpdateApplied()) {
                if (validWorkspaceSession.isPlainTrace()) {
                    out.println("Workspace is updated. Nuts should be restarted for changes to take effect.");
                }
            }
        }
    }

    private void traceSingleUpdate(NutsUpdateResult r) {
        NutsId id = r.getId();
        NutsDefinition d0 = r.getLocal();
        NutsDefinition d1 = r.getAvailable();
        final String simpleName = d0 != null ? d0.getId().getShortName() : d1 != null ? d1.getId().getShortName() : id.getShortName();
        final PrintStream out = CoreIOUtils.resolveOut(getValidWorkspaceSession());
        NutsTextManager factory = ws.formats().text();
        if (r.isUpdateApplied()) {
            if (r.isUpdateForced()) {
                if (d0 == null) {
                    out.printf("%s is [updated] to latest version %s%n",
                            factory.styled(simpleName, NutsTextNodeStyle.primary(3)),
                            d1 == null ? null : d1.getId().getVersion()
                    );
                } else if (d1 == null) {
                    //this is very interesting. Why the hell is this happening?
                } else {
                    NutsVersion v0 = d0.getId().getVersion();
                    NutsVersion v1 = d1.getId().getVersion();
                    if (v1.compareTo(v0) <= 0) {
                        if (v1.compareTo(v0) == 0) {
                            out.printf("%s is [forced] to %s %n", factory.styled(simpleName, NutsTextNodeStyle.primary(3)), d0.getId().getVersion());
                        } else {
                            out.printf("%s is [forced] from %s to older version %s%n",
                                    factory.styled(simpleName, NutsTextNodeStyle.primary(3)), d0.getId().getVersion(), d1.getId().getVersion());
                        }
                    } else {
                        out.printf("%s is [updated] from %s to latest version %s%n", factory.styled(simpleName, NutsTextNodeStyle.primary(3)),
                                d0.getId().getVersion(), d1.getId().getVersion());
                    }
                }
            }
        }
    }

    public NutsUpdateResult checkCoreUpdate(NutsId id, String bootApiVersion, NutsSession session, String type) {
        //disable trace so that search do not write to stream
        session = NutsWorkspaceUtils.of(ws).validateSilentSession(session);
        NutsId oldId = null;
        NutsDefinition oldFile = null;
        NutsDefinition newFile = null;
        NutsId newId = null;
//        List<NutsId> dependencies = new ArrayList<>();
//        NutsSession sessionOffline = session.copy().setFetchMode(NutsFetchMode.OFFLINE);
        switch (type) {
            case "api": {
                oldId = ws.config().stored().getApiId();
                NutsId confId = ws.config().stored().getApiId();
                if (confId != null) {
                    oldId = confId;
                }
                String v = bootApiVersion;
                if (CoreStringUtils.isBlank(v)) {
                    v = NutsConstants.Versions.LATEST;
                }
                try {
                    oldFile = fetch0().setId(oldId).setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ONLINE)).getResultDefinition();
                } catch (NutsNotFoundException ex) {
                    //ignore
                }
                try {
                    newId = ws.search().setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ANYWHERE))
                            .addId(NutsConstants.Ids.NUTS_API + "#" + v).setLatest(true).getResultIds().first();
                    newFile = newId == null ? null : latestOnlineDependencies(fetch0()).setFailFast(false).setSession(session).setId(newId).getResultDefinition();
                } catch (NutsNotFoundException ex) {
                    LOG.with().session(session).level(Level.SEVERE).error(ex).log("error : {0}", ex);
                    //ignore
                }
                break;
            }
            case "runtime": {
                oldId = ws.getRuntimeId();
                NutsId confId = ws.config().stored().getRuntimeId();
                if (confId != null) {
                    oldId = confId;
                }
                if (oldId != null) {
                    try {
                        oldFile = fetch0().setId(oldId).setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ONLINE)).getResultDefinition();
                    } catch (NutsNotFoundException ex) {
                        LOG.with().session(session).level(Level.SEVERE).error(ex).log("error : {0}", ex);
                        //ignore
                    }
                }
                try {
                    NutsSearchCommand se = ws.search()
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
                    LOG.with().session(session).level(Level.SEVERE).error(ex).log("error : {0}", ex);
                    //ignore
                }
                break;
            }
            case "companion":
            case "extension": {
                try {
                    oldId = ws.search().addId(id).setEffective(true).setSession(session)
                            .setInstallStatus(ws.filters().installStatus().byDeployed(true))
                            .sort(DEFAULT_THEN_LATEST_VERSION_FIRST).setFailFast(false).getResultIds().first();
                    if (oldId != null) {
                        oldFile = fetch0().setId(oldId).setSession(session).getResultDefinition();
                    }
                } catch (Exception ex) {
                    LOG.with().session(session).level(Level.SEVERE).error(ex).log("error : {0}", ex);
                    //ignore
                }
                try {
                    NutsSearchCommand se = ws.search()
                            .setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ANYWHERE))
                            .addId(id)
                            .setTargetApiVersion(bootApiVersion)
                            .addLockedIds(getLockedIds())
                            .setFailFast(false)
                            .setLatest(true)
                            .sort(LATEST_VERSION_FIRST);
                    if (type.equals("extension")) {
                        se.setExtension(true);
                    } else if (type.equals("companion")) {
                        se.setCompanion(true);
                    }
                    newId = se.getResultIds().first();

                    newFile = newId == null ? null : latestOnlineDependencies(fetch0().setSession(session).setId(newId))
                            .setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ONLINE))
                            .getResultDefinition();
                } catch (Exception ex) {
                    LOG.with().session(session).level(Level.SEVERE).error(ex).log("error : {0}", ex);
                    //ignore
                }
                break;
            }
        }
        //compare canonical forms
        NutsId cnewId = toCanonicalForm(newId);
        NutsId coldId = toCanonicalForm(oldId);
        DefaultNutsUpdateResult defaultNutsUpdateResult = new DefaultNutsUpdateResult(id, oldFile, newFile,
                newFile == null ? null : newFile.getDependencies().stream().map(NutsDependency::toId).toArray(NutsId[]::new),
                false);
        if (cnewId != null && newFile != null && coldId != null && cnewId.getVersion().compareTo(coldId.getVersion()) > 0) {
            defaultNutsUpdateResult.setUpdateVersionAvailable(true);
        }
        return defaultNutsUpdateResult;
    }

    private NutsId toCanonicalForm(NutsId id) {
        if (id != null) {
            id = id.builder().setNamespace(null).build();
            String oldValue = id.getProperties().get(NutsConstants.IdProperties.FACE);
            if (oldValue != null && oldValue.trim().isEmpty()) {
                id = id.builder().setProperty(NutsConstants.IdProperties.FACE, null).build();
            }
        }
        return id;
    }

    private void applyRegularUpdate(DefaultNutsUpdateResult r) {
        if (r.isUpdateApplied()) {
            return;
        }
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = getValidWorkspaceSession();
        final PrintStream out = CoreIOUtils.resolveOut(session);
        NutsId id = r.getId();
        NutsDefinition d0 = r.getLocal();
        NutsDefinition d1 = r.getAvailable();
        if (d0 == null) {
            ws.security().checkAllowed(NutsConstants.Permissions.UPDATE, "update", this.session);
            dws.updateImpl(d1, new String[0], null, session, true);
            r.setUpdateApplied(true);
        } else if (d1 == null) {
            //this is very interesting. Why the hell is this happening?
        } else {
            NutsVersion v0 = d0.getId().getVersion();
            NutsVersion v1 = d1.getId().getVersion();
            if (v1.compareTo(v0) <= 0) {
                //no update needed!
                if (session.isYes()) {
                    ws.security().checkAllowed(NutsConstants.Permissions.UPDATE, "update", this.session);
                    dws.updateImpl(d1, new String[0], null, session, true);
                    r.setUpdateApplied(true);
                    r.setUpdateForced(true);
                } else {
                    dws.getInstalledRepository().setDefaultVersion(d1.getId(), session);
                }
            } else {
                ws.security().checkAllowed(NutsConstants.Permissions.UPDATE, "update", this.session);
                dws.updateImpl(d1, new String[0], null, session, true);
                r.setUpdateApplied(true);
            }
        }
        traceSingleUpdate(r);
    }
}
