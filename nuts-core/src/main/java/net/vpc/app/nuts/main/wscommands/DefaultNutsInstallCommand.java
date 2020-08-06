/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.main.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.repos.NutsInstalledRepository;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.iter.IteratorUtils;
import net.vpc.app.nuts.runtime.wscommands.AbstractNutsInstallCommand;
import net.vpc.app.nuts.core.NutsWorkspaceExt;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.util.NutsCollectionResult;

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsInstallCommand extends AbstractNutsInstallCommand {

    public final NutsLogger LOG;

    public DefaultNutsInstallCommand(NutsWorkspace ws) {
        super(ws);
        LOG = ws.log().of(DefaultNutsInstallCommand.class);
    }

    private NutsDefinition _loadIdContent(NutsId id, NutsSession session, boolean includeDeps, Map<NutsId, NutsDefinition> loaded) {
        NutsId longNameId = id.getLongNameId();
        NutsDefinition def = loaded.get(longNameId);
        if (def != null) {
            return def;
        }
        NutsSession ss = CoreNutsUtils.silent(session).copy();
        def = ws.fetch().setId(id).setSession(ss)
                .setOptional(false)
                .setContent(true)
                .setEffective(true)
                .setDependencies(true)
                .setInstalled(false)
                .addScope(NutsDependencyScopePattern.RUN)
                .setFailFast(true)
                .getResultDefinition();
        loaded.put(longNameId, def);
        if (includeDeps) {
            for (NutsDependency dependency : def.getDependencies()) {
                _loadIdContent(dependency.getId(), session, includeDeps, loaded);
            }
        }
        return def;
    }

    @Override
    public NutsInstallCommand run() {
        boolean emptyCommand = true;
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = getSession();
        NutsSession searchSession = CoreNutsUtils.silent(session);
        PrintStream out = CoreIOUtils.resolveOut(session);
        ws.security().checkAllowed(NutsConstants.Permissions.INSTALL, "install");
        LinkedHashMap<NutsId, Boolean> allToInstall = new LinkedHashMap<>();
        Set<String> visited = new HashSet<>();
        if (this.isCompanions()) {
            for (String sid : dws.getCompanionIds()) {
                emptyCommand = false;
                if (!visited.contains(sid)) {
                    visited.add(sid);
                    List<NutsId> allIds = ws.search().addId(sid).setSession(searchSession).setLatest(true).setTargetApiVersion(ws.config().getApiVersion()).getResultIds().list();
                    if (allIds.isEmpty()) {
                        throw new NutsNotFoundException(ws, sid);
                    }
                    for (NutsId id0 : allIds) {
                        allToInstall.put(id0.builder().setNamespace(null).build(), false);
                        visited.add(id0.getLongName());
                    }
                }
            }
        }
        for (NutsId id : this.getIds()) {
            emptyCommand = false;
            if (!visited.contains(id.getLongName())) {
                visited.add(id.getLongName());
                List<NutsId> allIds = ws.search().addId(id).setSession(searchSession).setLatest(true).getResultIds().list();
                if (allIds.isEmpty()) {
                    throw new NutsNotFoundException(ws, id);
                }
                for (NutsId id0 : allIds) {
                    allToInstall.put(id0.builder().setNamespace(null).build(), false);
                    visited.add(id0.getLongName());
                }
            }
        }
        Map<NutsId, NutsDefinition> defsAll = new LinkedHashMap<>();
        Map<NutsId, NutsDefinition> defsToInstall = new LinkedHashMap<>();
        Map<NutsId, NutsDefinition> defsToInstallForced = new LinkedHashMap<>();
        Map<NutsId, NutsDefinition> defsToDefVersion = new LinkedHashMap<>();
        Map<NutsId, NutsDefinition> defsToIgnore = new LinkedHashMap<>();
        Map<NutsId, NutsDefinition> defsOk = new LinkedHashMap<>();
        if (isInstalled()) {
            for (NutsId resultId : ws.search().setSession(searchSession).installed().getResultIds()) {
                emptyCommand = false;
                if (!visited.contains(resultId.getLongName())) {
                    allToInstall.put(resultId.builder().setNamespace(null).build(), true);
                    visited.add(resultId.getLongName());
                }
            }
            // This bloc is to handle packages that were installed but their jar/content was removed for any reason!
            NutsInstalledRepository ir = dws.getInstalledRepository();
            for (NutsInstallInformation y : IteratorUtils.toList(ir.searchInstallInformation(session))) {
                if (y != null && y.getInstallStatus() == NutsInstallStatus.INSTALLED && y.getId() != null) {
                    NutsId resultId = y.getId();
                    if (!visited.contains(resultId.getLongName())) {
                        NutsId newId = resultId.builder().setNamespace(null).build();
                        allToInstall.put(newId, true);
                        visited.add(resultId.getLongName());
                        defsToInstallForced.put(newId, null);
                    }
                }
            }
        }

        for (Map.Entry<NutsId, Boolean> nutsIdBooleanEntry : allToInstall.entrySet()) {
            emptyCommand = false;
            NutsId nid = nutsIdBooleanEntry.getKey();
            boolean installed = dws.getInstalledRepository().getInstallStatus(nid, session) == NutsInstallStatus.INSTALLED;
            boolean defVer = dws.getInstalledRepository().isDefaultVersion(nid, session);
            if (defsToInstallForced.containsKey(nid)) {
                installed = true;
            }
            boolean nForced = session.isForce() || nutsIdBooleanEntry.getValue();
            //must load dependencies because will be run later!!
            if (installed) {
                if (nForced || getSession().isYes()) {
                    defsToInstallForced.put(nid, null);
                } else if (!defVer) {
                    //installed, we only need to make it default!
                    defsToDefVersion.put(nid, null);
                } else {
                    //installed and default
                    defsToIgnore.put(nid, null);
                }
            } else {
                defsToInstall.put(nid, null);
            }
        }
        if (getSession().isPlainTrace() || (!defsToInstall.isEmpty() && getSession().getConfirm() == NutsConfirmationMode.ASK)) {
            if (!defsToInstall.isEmpty()) {
                out.println("the following {{new}} ==nuts== " + (defsToInstall.size() > 1 ? "components are" : "component is") + " going to be ##installed## : "
                        + defsToInstall.keySet().stream()
                                .map(x -> ws.id().omitImportedGroupId().value(x.getLongNameId()).format())
                                .collect(Collectors.joining(", ")));
            }
            if (!defsToInstallForced.isEmpty()) {
                out.println("the following already ##installed## ==nuts== " + (defsToInstallForced.size() > 1 ? "components are" : "component is") + " going to be [[reinstalled]] : "
                        + defsToInstallForced.keySet().stream()
                                .map(x -> ws.id().omitImportedGroupId().value(x.getLongNameId()).format())
                                .collect(Collectors.joining(", ")));
            }
            if (!defsToDefVersion.isEmpty()) {
                out.println("the following already ##installed## ==nuts== " + (defsToDefVersion.size() > 1 ? "components are" : "component is") + " going to be **set as default** : "
                        + defsToDefVersion.keySet().stream()
                                .map(x -> ws.id().omitImportedGroupId().value(x.getLongNameId()).format())
                                .collect(Collectors.joining(", ")));
            }
            if (!defsToIgnore.isEmpty()) {
                out.println("the following already ##installed## ==nuts== " + (defsToIgnore.size() > 1 ? "components are" : "component is") + " {{ignored}} : "
                        + defsToIgnore.keySet().stream()
                                .map(x -> ws.id().omitImportedGroupId().value(x.getLongNameId()).format())
                                .collect(Collectors.joining(", ")));
            }

        }
        List<String> cmdArgs = new ArrayList<>(Arrays.asList(this.getArgs()));
        if (session.isForce()) {
            cmdArgs.add(0, "--force");
        }
        if (session.isTrace()) {
            cmdArgs.add(0, "--force");
            cmdArgs.add(0, "--trace");
        }
        if (!defsToInstall.isEmpty() || !defsToInstallForced.isEmpty() || !defsToDefVersion.isEmpty()) {
            if (!ws.io().getTerminal().ask().forBoolean("Continue installation?")
                    .defaultValue(true)
                    .setSession(session).getBooleanValue()) {
                result = new NutsDefinition[0];
                fails = new NutsId[0];
                return this;
            }
        }
        //fetch all items
        LinkedHashMap<NutsId, NutsDefinition> loadedDefs = new LinkedHashMap<>();
        LinkedHashSet<NutsId> failed = new LinkedHashSet<>();

        if (!doThis(defsToInstall, loadedDefs, defsAll, failed)) {
            return this;
        }
        if (!doThis(defsToInstallForced, loadedDefs, defsAll, failed)) {
            return this;
        }
        if (!doThis(defsToDefVersion, loadedDefs, defsAll, failed)) {
            return this;
        }
        NutsId forId = null;//always null? may be should remove this!
        if (!defsToInstall.isEmpty()) {
            if (session.isForce()) {
                session = session.copy().yes();
            }
            for (NutsDefinition def : defsToInstall.values()) {
                dws.installImpl(def, cmdArgs.toArray(new String[0]), null, session, isDefaultVersion(), forId);
            }
        }
        if (session.isForce()) {
            session = session.copy().yes();
        }
        for (NutsDefinition def : defsToInstallForced.values()) {
            try {
                dws.installImpl(def, cmdArgs.toArray(new String[0]), null, session.copy().yes(), isDefaultVersion(), forId);
            } catch (RuntimeException ex) {
                failed.add(def.getId());
                if (session.isPlainTrace()) {
                    if (!ws.io().getTerminal().ask().forBoolean("failed to install " + ws.id().set(def.getId()).format() + " and its dependencies... Continue installation?")
                            .defaultValue(true)
                            .setSession(session).getBooleanValue()) {
                        result = defsOk.values().toArray(new NutsDefinition[0]);
                        fails = failed.toArray(new NutsId[0]);
                        return this;
                    }
                    session.out().println();
                } else {
                    throw ex;
                }
            }
        }
        for (NutsDefinition def : defsToDefVersion.values()) {
            try {
                dws.getInstalledRepository().setDefaultVersion(def.getId(), session);
            } catch (RuntimeException ex) {
                failed.add(def.getId());
                if (session.isPlainTrace()) {
                    if (!ws.io().getTerminal().ask().forBoolean("@@failed to install@@ " + ws.id().set(def.getId()).format() + " and its dependencies... Continue installation?")
                            .defaultValue(true)
                            .setSession(session).getBooleanValue()) {
                        result = defsOk.values().toArray(new NutsDefinition[0]);
                        fails = failed.toArray(new NutsId[0]);
                        return this;
                    }
                    session.out().println();
                } else {
                    throw ex;
                }
            }
        }
        if (emptyCommand) {
            throw new NutsExecutionException(ws, "Missing components to install", 1);
        }
        result = defsOk.values().toArray(new NutsDefinition[0]);
        fails = failed.toArray(new NutsId[0]);
        return this;
    }

    private boolean doThis(Map<NutsId, NutsDefinition> defsToInstall, LinkedHashMap<NutsId, NutsDefinition> loadedDefs, Map<NutsId, NutsDefinition> defsAll, LinkedHashSet<NutsId> failed) {
        for (NutsId id : defsToInstall.keySet().toArray(new NutsId[0])) {
            if (session.isPlainTrace()) {
                session.out().println("downloading " + ws.id().set(id).format() + " and its dependencies...");
            }
            try {
                NutsDefinition def = _loadIdContent(id, session, true, loadedDefs);
                defsToInstall.put(id, def);
                defsAll.put(id, def);
            } catch (RuntimeException ex) {
                failed.add(id);
                if (session.isPlainTrace()) {
                    if (!ws.io().getTerminal().ask().forBoolean("@@failed to install@@ " + ws.id().set(id).format() + " and its dependencies... Continue installation?")
                            .defaultValue(true)
                            .setSession(session).getBooleanValue()) {
                        result = new NutsDefinition[0];
                        fails = failed.toArray(new NutsId[0]);
                        return false;
                    }
                    session.out().println();
                } else {
                    throw ex;
                }
            }
        }
        return true;
    }

    @Override
    public NutsResultList<NutsDefinition> getResult() {
        if (result == null) {
            run();
        }
        return new NutsCollectionResult<NutsDefinition>(ws,
                ids.isEmpty() ? null : ids.get(0).toString(),
                Arrays.asList(result)
        );
    }
}
