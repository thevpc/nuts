/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.runtime.wscommands.AbstractNutsUpdateCommand;
import net.vpc.app.nuts.runtime.DefaultNutsUpdateResult;
import net.vpc.app.nuts.runtime.DefaultNutsWorkspaceUpdateResult;
import net.vpc.app.nuts.runtime.NutsExtensionListHelper;
import net.vpc.app.nuts.core.NutsWorkspaceExt;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.config.NutsWorkspaceConfigManagerExt;

/**
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsUpdateCommand extends AbstractNutsUpdateCommand {

    public final NutsLogger LOG;

    public DefaultNutsUpdateCommand(NutsWorkspace ws) {
        super(ws);
        LOG=ws.log().of(DefaultNutsUpdateCommand.class);
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
        for (NutsId extension : ws.extensions().getExtensions()) {
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
                if (id.getShortName().equals(ws.config().getRuntimeId().getShortName())) {
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
        for (String extension : NutsWorkspaceExt.of(ws).getCompanionIds()) {
            ext.add(ws.id().parse(extension).getShortNameId());
        }
        return ext;
    }

    private Set<NutsId> getRegularIds() {
        HashSet<String> extensions = new HashSet<>();
        for (NutsId object : ws.extensions().getExtensions()) {
            extensions.add(object.getShortName());
        }

        HashSet<NutsId> baseRegulars = new HashSet<>(ids);
        if (isInstalled()) {
            baseRegulars.addAll(ws.search().session(getValidSession().copy().silent()).installed().getResultIds().stream().map(NutsId::getShortNameId).collect(Collectors.toList()));
        }
        HashSet<NutsId> regulars = new HashSet<>();
        for (NutsId id : baseRegulars) {
            if (id.getShortName().equals(NutsConstants.Ids.NUTS_API)) {
                continue;
            }
            if (id.getShortName().equals(ws.config().getRuntimeId().getShortName())) {
                continue;
            }
            if (extensions.contains(id.getShortName())) {
                continue;
            }
            regulars.add(id);
        }
        return regulars;
    }

    @Override
    public NutsUpdateCommand checkUpdates() {
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
//        NutsWorkspaceCurrentConfig actualBootConfig = ws.config().current();
//        NutsWorkspaceCurrentConfig jsonBootConfig = getConfigManager().getBootContext();
        NutsSession session = NutsWorkspaceUtils.of(ws).validateSession(this.getSession());
        Map<String, NutsUpdateResult> allUpdates = new LinkedHashMap<>();
        Map<String, NutsUpdateResult> extUpdates = new LinkedHashMap<>();
        Map<String, NutsUpdateResult> regularUpdates = new HashMap<>();
        NutsUpdateResult apiUpdate = null;
        String bootVersion0 = ws.config().getApiVersion();
        String bootVersion = bootVersion0;
        if (!CoreStringUtils.isBlank(this.getApiVersion())) {
            bootVersion = this.getApiVersion();
        }
        if (this.isApi() || !CoreStringUtils.isBlank(this.getApiVersion())) {
            apiUpdate = checkCoreUpdate(ws.id().parse(NutsConstants.Ids.NUTS_API), this.getApiVersion(), session,"api");
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
            if (dws.requiresCoreExtension()) {
                runtimeUpdate = checkCoreUpdate(ws.id().parse(ws.config().getRuntimeId().getShortName()),
                        apiUpdate != null && apiUpdate.getAvailable().getId() != null ? apiUpdate.getAvailable().getId().getVersion().toString()
                                : bootVersion, session,"runtime");
                if (runtimeUpdate.isUpdateAvailable()) {
                    allUpdates.put(runtimeUpdate.getId().getShortName(), runtimeUpdate);
                }
            }
        }

        if (this.isExtensions()) {
            for (NutsId ext : getExtensionsToUpdate()) {
                NutsUpdateResult extUpdate = checkCoreUpdate(ext, bootVersion, session,"extension");
                allUpdates.put(extUpdate.getId().getShortName(), extUpdate);
                extUpdates.put(extUpdate.getId().getShortName(), extUpdate);
            }
        }

        if (this.isCompanions()) {
            for (NutsId ext : getCompanionsToUpdate()) {
                NutsUpdateResult extUpdate = checkCoreUpdate(ext, bootVersion, session,"companion");
                allUpdates.put(extUpdate.getId().getShortName(), extUpdate);
                regularUpdates.put(extUpdate.getId().getShortName(), extUpdate);
            }
        }

        for (NutsId id : this.getRegularIds()) {
            NutsUpdateResult updated = checkRegularUpdate(id);
            allUpdates.put(updated.getAvailable().getId().getShortName(), updated);
            regularUpdates.put(updated.getId().getShortName(), updated);
        }
        NutsId[] frozenIds = this.getFrozenIds();
        if (frozenIds.length > 0) {
            for (NutsId d : new HashSet<>(Arrays.asList(frozenIds))) {
                NutsDependency dd = CoreNutsUtils.parseNutsDependency(ws, d.toString());
                if (regularUpdates.containsKey(dd.getSimpleName())) {
                    NutsUpdateResult updated = regularUpdates.get(dd.getSimpleName());
                    //FIX ME
                    if (!dd.getVersion().filter().accept(updated.getId().getVersion(), session)) {
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
            PrintStream out = CoreIOUtils.resolveOut(getValidSession());
            NutsUpdateResult[] updates = result.getAllUpdates();
            if (updates.length == 0) {
                out.printf("All components are [[up-to-date]]. You are running latest version%s.%n",result.getAllResults().length>1?"s":"");
            } else {
                out.printf("Workspace has ##%s## component%s to update.%n", updates.length, (updates.length > 1 ? "s" : ""));
                int widthCol1 = 2;
                int widthCol2 = 2;
                for (NutsUpdateResult update : updates) {
                    widthCol1 = Math.max(widthCol1, update.getAvailable().getId().getShortName().length());
                    widthCol2 = Math.max(widthCol2, update.getLocal().getId().getVersion().toString().length());
                }
                for (NutsUpdateResult update : updates) {
                    if(update.isUpdateVersionAvailable()) {
                        out.printf("((%s))  : %s => [[%s]]%n",
                                CoreStringUtils.alignLeft(update.getLocal().getId().getVersion().toString(), widthCol2),
                                CoreStringUtils.alignLeft(update.getAvailable().getId().getShortName(), widthCol1),
                                update.getAvailable().getId().getVersion().toString());
                    }else if(update.isUpdateStatusAvailable()){
                        out.printf("((%s))  : %s => [[%s]]%n",
                                CoreStringUtils.alignLeft(update.getLocal().getId().getVersion().toString(), widthCol2),
                                CoreStringUtils.alignLeft(update.getAvailable().getId().getShortName(), widthCol1),
                                "set as default");
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
//        se.optional(includeOptional ? null : false).latest();
//        return se;
//    }

    private NutsFetchCommand latestOnlineDependencies(NutsFetchCommand se) {
        se.dependencies();
        if (scopes.isEmpty()) {
            se.scope(NutsDependencyScopePattern.RUN);
        } else {
            se.scopes(scopes.toArray(new NutsDependencyScope[0]));
        }
        se.optional(isOptional() ? null : false).online();
        return se;
    }

    protected NutsUpdateResult checkRegularUpdate(NutsId id) {
        NutsSession session = getValidSession();
        NutsSession searchSession = session.copy().silent();
        NutsVersion version = id.getVersion();
        if (version.isSingleValue()) {
            throw new NutsIllegalArgumentException(ws, id + " : Version is too restrictive. You should use fetch or install instead");
        }

        DefaultNutsUpdateResult r = new DefaultNutsUpdateResult();
        r.setId(id.getShortNameId());
        boolean shouldUpdateDefault = false;
        NutsId d0Id = ws.search().id(id).session(searchSession).installed().optional(false).failFast(false).defaultVersions()
                .getResultIds().first();
        if (d0Id == null) {
            // may be the id is not default!
            d0Id = ws.search().id(id).session(searchSession).installed().optional(false).failFast(false).latest()
                    .getResultIds().first();
            if (d0Id != null) {
                shouldUpdateDefault = true;
            }
        }
        if(d0Id==null) {
            throw new NutsIllegalArgumentException(ws, id + " is not yet installed to be updated.");
        }
        NutsDefinition d0 = fetch0().id(d0Id)
                .session(searchSession).installed().optional(false).failFast(false)
                .getResultDefinition();
        if (d0 == null) {
            throw new NutsIllegalArgumentException(ws, d0Id + " installation is broken and cannot be updated.");
        }
        //search latest parse
        NutsId d1Id = ws.search().id(d0Id.getShortNameId())
                .session(searchSession)
                .failFast(false)
                .anyWhere()
                .latest()
                .getResultIds().first();
        //then fetch its definition!
        NutsDefinition d1 = d1Id == null ? null : latestOnlineDependencies(fetch0().id(d1Id)
                .session(searchSession))
                .failFast(false)
                .getResultDefinition();
        r.setLocal(d0);
        r.setAvailable(d1);
        if (d0 == null) {
            if (!this.isEnableInstall()) {
                throw new NutsIllegalArgumentException(ws, "No version is installed to be updated for " + id);
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
        return ws.fetch().content().effective();
    }

    private void applyResult(NutsWorkspaceUpdateResult result) {
        NutsUpdateResult apiUpdate = result.getApi();
        NutsUpdateResult runtimeUpdate = result.getRuntime();
        if (result.getUpdatesCount() == 0) {
            return;
        }
        NutsWorkspaceUtils.of(ws).checkReadOnly();
        boolean requireSave = false;
        final PrintStream out = CoreIOUtils.resolveOut(getValidSession());
        boolean accept = ws.io().getTerminal().ask()
                .forBoolean("Would you like to apply updates?").setDefaultValue(true)
                .session(getValidSession()).getValue();
        if (getValidSession().isAsk() && !accept) {
            throw new NutsUserCancelException(ws);
        }
        NutsWorkspaceConfigManagerExt wcfg = NutsWorkspaceConfigManagerExt.of(ws.config());
//        NutsWorkspaceCurrentConfig actualBootConfig = ws.config().getContext(net.vpc.app.nuts.NutsBootContextType.RUNTIME);
//        NutsWorkspaceConfigApi aconfig = null;
//        NutsWorkspaceConfigRuntime rconfig = null;
//        if (apiUpdate != null && apiUpdate.isUpdateAvailable() && !apiUpdate.isUpdateApplied()) {
//            aconfig = wcfg.getStoredConfigApi(apiUpdate.getAvailable().getId().getVersion().toString());
////            NutsWorkspaceExt.of(ws).deployBoot(getValidSession(), apiUpdate.getAvailable().getId(), false);
//            ((DefaultNutsUpdateResult) apiUpdate).setUpdateApplied(true);
//            traceSingleUpdate(apiUpdate);
//            requireSave = true;
//        }
//        if (runtimeUpdate != null && runtimeUpdate.isUpdateAvailable() && !runtimeUpdate.isUpdateApplied()) {
////            NutsWorkspaceExt.of(ws).deployBoot(getValidSession(), runtimeUpdate.getAvailable().getId(), true);
//            if (aconfig == null) {
//                aconfig = wcfg.getStoredConfigApi(runtimeUpdate.getAvailable().getApiId().getVersion().toString());
//            }
//            aconfig.setRuntimeId(runtimeUpdate.getAvailable().getId().getLongName());
//
//            rconfig = wcfg.getStoredConfigRuntime();
//            rconfig.setDependencies(Arrays.stream(runtimeUpdate.getDependencies()).map(NutsId::getLongName).collect(Collectors.joining(";")));
//            ((DefaultNutsUpdateResult) runtimeUpdate).setUpdateApplied(true);
//            traceSingleUpdate(runtimeUpdate);
//            requireSave = true;
//        }
        boolean apiUpdateAvailable = apiUpdate != null && apiUpdate.getAvailable() != null && !apiUpdate.isUpdateApplied();
        boolean runtimeUpdateAvailable = runtimeUpdate != null && runtimeUpdate.getAvailable() != null && !runtimeUpdate.isUpdateApplied();
        boolean apiUpdateApplicable = apiUpdateAvailable && !apiUpdate.isUpdateApplied();
        boolean runtimeUpdateApplicable = runtimeUpdateAvailable && !runtimeUpdate.isUpdateApplied();
        NutsId finalApiId = apiUpdateAvailable ? apiUpdate.getAvailable().getId() : ws.config().getApiId();
        NutsId finalRuntimeId = runtimeUpdateApplicable ? runtimeUpdate.getAvailable().getId() : ws.config().getRuntimeId();
        if (apiUpdateApplicable || runtimeUpdateApplicable) {
            wcfg.prepareBootApi(finalApiId, finalRuntimeId, true);
        }
        if (apiUpdateApplicable) {
            ((DefaultNutsUpdateResult) apiUpdate).setUpdateApplied(true);
            traceSingleUpdate(apiUpdate);
        }
        if (runtimeUpdateApplicable) {
            wcfg.prepareBootRuntime(finalRuntimeId, true);
        }
        if (runtimeUpdateApplicable) {
            ((DefaultNutsUpdateResult) runtimeUpdate).setUpdateApplied(true);
            traceSingleUpdate(runtimeUpdate);
        }
        for (NutsUpdateResult extension : result.getExtensions()) {
            NutsId finalExtensionId = extension.getAvailable() == null ? extension.getLocal().getId() : extension.getAvailable().getId();
            wcfg.prepareBootExtension(finalExtensionId, true);
        }
        NutsExtensionListHelper h = new NutsExtensionListHelper(wcfg.getStoredConfigBoot().getExtensions())
                .save();
        for (NutsUpdateResult extension : result.getExtensions()) {
            if (!extension.isUpdateApplied()) {
                if (extension.getAvailable() != null) {
                    h.add(extension.getAvailable().getId());
                    if (h.hasChanged()) {
                        NutsWorkspaceExt.of(ws).deployBoot(getValidSession(), extension.getAvailable().getId(), true);
                    }
                }
                ((DefaultNutsUpdateResult) extension).setUpdateApplied(true);
                traceSingleUpdate(extension);
            }
        }
        for (NutsUpdateResult component : result.getArtifacts()) {
            applyRegularUpdate((DefaultNutsUpdateResult) component);
        }

        if (ws.config().save(requireSave, getValidSession())) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.log(Level.INFO, NutsLogVerb.WARNING, "Workspace is updated. Nuts should be restarted for changes to take effect.");
            }
            if (apiUpdate!=null && apiUpdate.isUpdateAvailable() && !apiUpdate.isUpdateApplied()) {
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
        final String simpleName = d0 != null ? d0.getId().getShortName() : d1 != null ? d1.getId().getShortName() : id.getShortName();
        final PrintStream out = CoreIOUtils.resolveOut(getValidSession());
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

    public NutsUpdateResult checkCoreUpdate(NutsId id, String bootApiVersion, NutsSession session,String type) {
        //disable trace so that search do not write to stream
        session = NutsWorkspaceUtils.of(ws).validateSession(session);
        NutsSession searchSession = session.copy().silent();
        NutsId oldId = null;
        NutsDefinition oldFile = null;
        NutsDefinition newFile = null;
        NutsId newId = null;
//        List<NutsId> dependencies = new ArrayList<>();
//        NutsSession sessionOffline = session.copy().setFetchMode(NutsFetchMode.OFFLINE);
        switch (type){
            case "api":{
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
                    oldFile = fetch0().id(oldId).session(searchSession).online().getResultDefinition();
                } catch (NutsNotFoundException ex) {
                    //ignore
                }
                try {
                    newId = ws.search().session(searchSession).id(NutsConstants.Ids.NUTS_API + "#" + v).anyWhere().latest().getResultIds().first();
                    newFile = newId == null ? null : latestOnlineDependencies(fetch0()).failFast(false).session(searchSession).id(newId).getResultDefinition();
                } catch (NutsNotFoundException ex) {
                    LOG.log(Level.SEVERE, "Error " + ex, ex);
                    //ignore
                }
                break;
            }
            case "runtime":{
                oldId = ws.config().getRuntimeId();
                NutsId confId = ws.config().stored().getRuntimeId();
                if (confId != null) {
                    oldId = confId;
                }
                try {
                    oldFile = fetch0().id(oldId).session(searchSession).online().getResultDefinition();
                } catch (NutsNotFoundException ex) {
                    LOG.log(Level.SEVERE, "Error " + ex, ex);
                    //ignore
                }
                try {
                    newId = ws.search()
                            .addId(oldFile != null ? oldFile.getId().builder().setVersion("").build().toString() : NutsConstants.Ids.NUTS_RUNTIME)
                            .runtime()
                            .targetApiVersion(bootApiVersion)
                            .frozenIds(getFrozenIds())
                            .latest()
                            .anyWhere()
                            .session(searchSession)
                            .getResultIds().first();
                    newFile = newId == null ? null : latestOnlineDependencies(fetch0().id(newId))
                            .session(searchSession)
                            .failFast(false)
                            .getResultDefinition();
                } catch (NutsNotFoundException ex) {
                    LOG.log(Level.SEVERE, "Error " + ex, ex);
                    //ignore
                }
                break;
            }
            case "extension":{
                try {
                    oldId = ws.search().id(id).effective().session(searchSession)
                            .offline().getResultIds().first();
                    oldFile = fetch0().id(oldId).session(searchSession).getResultDefinition();
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, "Error " + ex, ex);
                    //ignore
                }
                try {
                    newId = ws.search().session(searchSession).addId(id)
                            .extensions()
                            .targetApiVersion(bootApiVersion)
                            .frozenIds(getFrozenIds())
                            .anyWhere()
                            .failFast(false)
                            .getResultIds().first();
                    newFile = newId == null ? null : latestOnlineDependencies(fetch0().session(searchSession).id(newId))
                            .online()
                            .getResultDefinition();
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, "Error " + ex, ex);
                    //ignore
                }
            }
            case "companion":{
                try {
                    oldId = ws.search().id(id).effective().session(searchSession)
                            .offline().getResultIds().first();
                    oldFile = fetch0().id(oldId).session(searchSession).getResultDefinition();
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, "Error " + ex, ex);
                    //ignore
                }
                try {
                    newId = ws.search().session(searchSession).addId(id)
                            .companion()
                            .targetApiVersion(bootApiVersion)
                            .frozenIds(getFrozenIds())
                            .anyWhere()
                            .failFast(false)
                            .getResultIds().first();
                    newFile = newId == null ? null : latestOnlineDependencies(fetch0().session(searchSession).id(newId))
                            .online()
                            .getResultDefinition();
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, "Error " + ex, ex);
                    //ignore
                }
            }
        }
        //compare canonical forms
        NutsId cnewId = toCanonicalForm(newId);
        NutsId coldId = toCanonicalForm(oldId);
        DefaultNutsUpdateResult defaultNutsUpdateResult = new DefaultNutsUpdateResult(id, oldFile, newFile,
                newFile == null ? null : Arrays.stream(newFile.getDependencies()).map(NutsDependency::getId).toArray(NutsId[]::new),
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
        final PrintStream out = CoreIOUtils.resolveOut(getValidSession());
        NutsId id = r.getId();
        NutsDefinition d0 = r.getLocal();
        NutsDefinition d1 = r.getAvailable();
        if (d0 == null) {
            ws.security().checkAllowed(NutsConstants.Permissions.UPDATE, "update");
            dws.updateImpl(d1, new String[0], null, getValidSession(), true);
            r.setUpdateApplied(true);
        } else if (d1 == null) {
            //this is very interesting. Why the hell is this happening?
        } else {
            NutsVersion v0 = d0.getId().getVersion();
            NutsVersion v1 = d1.getId().getVersion();
            if (v1.compareTo(v0) <= 0) {
                //no update needed!
                if (getValidSession().isYes()) {
                    ws.security().checkAllowed(NutsConstants.Permissions.UPDATE, "update");
                    dws.updateImpl(d1, new String[0], null, getValidSession(), true);
                    r.setUpdateApplied(true);
                    r.setUpdateForced(true);
                } else {
                    dws.getInstalledRepository().setDefaultVersion(d1.getId(), getValidSession());
                }
            } else {
                ws.security().checkAllowed(NutsConstants.Permissions.UPDATE, "update");
                dws.updateImpl(d1, new String[0], null, getValidSession(), true);
                r.setUpdateApplied(true);
            }
        }
        traceSingleUpdate(r);
    }
}
