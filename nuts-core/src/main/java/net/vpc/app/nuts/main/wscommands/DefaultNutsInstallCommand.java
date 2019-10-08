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
import net.vpc.app.nuts.runtime.wscommands.AbstractNutsInstallCommand;
import net.vpc.app.nuts.core.NutsWorkspaceExt;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.util.NutsCollectionSearchResult;

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
        LOG=ws.log().of(DefaultNutsInstallCommand.class);
    }

    private NutsDefinition _loadIdContent(NutsId id,NutsSession session,boolean includeDeps,Map<NutsId,NutsDefinition> loaded){
        NutsId longNameId = id.getLongNameId();
        NutsDefinition def = loaded.get(longNameId);
        if(def!=null){
            return def;
        }
        def = ws.fetch().id(id).session(session.copy().silent())
                .optional(false)
                .content()
                .effective()
                .dependencies()
                .scope(NutsDependencyScopePattern.RUN)
                .failFast()
                .getResultDefinition();
        loaded.put(longNameId,def);
        if(includeDeps){
            for (NutsDependency dependency : def.getDependencies()) {
                _loadIdContent(dependency.getId(),session,includeDeps,loaded);
            }
        }
        return def;
    }
    @Override
    public NutsInstallCommand run() {
        boolean emptyCommand = true;
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = getValidSession();
        NutsSession searchSession = session.copy().silent();
        PrintStream out = CoreIOUtils.resolveOut(session);
        ws.security().checkAllowed(NutsConstants.Permissions.INSTALL, "install");
        LinkedHashMap<NutsId,Boolean> allToInstall=new LinkedHashMap<>();
        Set<String> visited = new HashSet<>();
        if (this.isCompanions()) {
            for (String sid : dws.getCompanionIds()) {
                emptyCommand = false;
                if(!visited.contains(sid)) {
                    visited.add(sid);
                    List<NutsId> allIds = ws.search().id(sid).session(searchSession).latest().targetApiVersion(ws.config().getApiVersion()).getResultIds().list();
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
            if(!visited.contains(id.getLongName())) {
                visited.add(id.getLongName());
                List<NutsId> allIds = ws.search().id(id).session(searchSession).latest().getResultIds().list();
                if (allIds.isEmpty()) {
                    throw new NutsNotFoundException(ws, id);
                }
                for (NutsId id0 : allIds) {
                    allToInstall.put(id0.builder().setNamespace(null).build(), false);
                    visited.add(id0.getLongName());
                }
            }
        }
        if(isInstalled()){
            for (NutsId resultId : ws.search().session(searchSession).installed().getResultIds()) {
                emptyCommand = false;
                if(!visited.contains(resultId.getLongName())) {
                    allToInstall.put(resultId.builder().setNamespace(null).build(), true);
                    visited.add(resultId.getLongName());
                }
            }
        }

        Map<NutsId,NutsDefinition> defsAll = new LinkedHashMap<>();
        Map<NutsId,NutsDefinition> defsToInstall = new LinkedHashMap<>();
        Map<NutsId,NutsDefinition> defsToInstallForced = new LinkedHashMap<>();
        Map<NutsId,NutsDefinition> defsToDefVersion = new LinkedHashMap<>();
        Map<NutsId,NutsDefinition> defsToIgnore = new LinkedHashMap<>();
        for (Map.Entry<NutsId, Boolean> nutsIdBooleanEntry : allToInstall.entrySet()) {
            emptyCommand = false;
            NutsId nid=nutsIdBooleanEntry.getKey();
            boolean installed = dws.getInstalledRepository().isInstalled(nid);
            boolean defVer = dws.getInstalledRepository().isDefaultVersion(nid);

            boolean nForced = session.isForce() || nutsIdBooleanEntry.getValue();
            //must load dependencies because will be run later!!
            if(installed){
                if (nForced || getValidSession().isYes()){
                    defsToInstallForced.put(nid,null);
                }else if(!defVer){
                    //installed, we only need to make it default!
                    defsToDefVersion.put(nid,null);
                }else{
                    //installed and default
                    defsToIgnore.put(nid,null);
                }
            }else{
                defsToInstall.put(nid,null);
            }
        }
        if (getValidSession().isPlainTrace() || (!defsToInstall.isEmpty() && getValidSession().getConfirm()==NutsConfirmationMode.ASK)) {
            if(!defsToInstall.isEmpty()) {
                out.println("The following {{new}} ==nuts== " + (defsToInstall.size() > 1 ? "components are" : "component is") + " going to be ##installed## : "
                        + defsToInstall.keySet().stream()
                        .map(x -> ws.id().omitImportedGroupId().value(x.getLongNameId()).format())
                        .collect(Collectors.joining(", ")));
            }
            if(!defsToInstallForced.isEmpty()) {
                out.println("The following already ##installed## ==nuts== " + (defsToInstallForced.size() > 1 ? "components are" : "component is") + " going to be [[reinstalled]] : "
                        + defsToInstallForced.keySet().stream()
                        .map(x -> ws.id().omitImportedGroupId().value(x.getLongNameId()).format())
                        .collect(Collectors.joining(", ")));
            }
            if(!defsToDefVersion.isEmpty()) {
                out.println("The following already ##installed## ==nuts== " + (defsToDefVersion.size() > 1 ? "components are" : "component is") + " going to be **set as default** : "
                        + defsToDefVersion.keySet().stream()
                        .map(x -> ws.id().omitImportedGroupId().value(x.getLongNameId()).format())
                        .collect(Collectors.joining(", ")));
            }
            if(!defsToIgnore.isEmpty()) {
                out.println("The following already ##installed## ==nuts== " + (defsToIgnore.size() > 1 ? "components are" : "component is") + " {{ignored}} : "
                        + defsToIgnore.keySet().stream()
                        .map(x -> ws.id().omitImportedGroupId().value(x.getLongNameId()).format())
                        .collect(Collectors.joining(", ")));
            }

        }
        List<String> cmdArgs=new ArrayList<>(Arrays.asList(this.getArgs()));
        if(session.isForce()){
            cmdArgs.add(0,"--force");
        }
        if(session.isTrace()){
            cmdArgs.add(0,"--force");
            cmdArgs.add(0,"--trace");
        }
        if (!defsToInstall.isEmpty() || !defsToInstallForced.isEmpty() || !defsToDefVersion.isEmpty()) {
            if(!ws.io().getTerminal().ask().forBoolean("Continue installation?")
                    .defaultValue(true)
                    .session(session).getBooleanValue()) {
                result = new NutsDefinition[0];
                return this;
            }
        }
        //fetch all items
        LinkedHashMap<NutsId, NutsDefinition> loadedDefs = new LinkedHashMap<>();
        for (NutsId id : defsToInstall.keySet().toArray(new NutsId[0])) {
            if(session.isPlainTrace()){
                session.out().println("downloading "+ws.id().set(id).format()+" and it dependencies...");
            }
            NutsDefinition def = _loadIdContent(id, session, true, loadedDefs);
            defsToInstall.put(id, def);
            defsAll.put(id,def);
        }

        for (NutsId id : defsToInstallForced.keySet().toArray(new NutsId[0])) {
            if(session.isPlainTrace()){
                session.out().println("downloading "+ws.id().set(id).format()+" and it dependencies...");
            }
            NutsDefinition def = _loadIdContent(id, session, true, loadedDefs);
            defsToInstallForced.put(id, def);
            defsAll.put(id,def);
        }

        for (NutsId id : defsToDefVersion.keySet().toArray(new NutsId[0])) {
            if(session.isPlainTrace()){
                session.out().println("downloading "+ws.id().set(id).format()+" and it dependencies...");
            }
            NutsDefinition def = _loadIdContent(id, session, true, loadedDefs);
            defsToDefVersion.put(id,def);
            defsAll.put(id,def);
        }

        if (!defsToInstall.isEmpty()) {
            if(session.isForce()){
                session=session.copy().yes();
            }
            for (NutsDefinition def : defsToInstall.values()) {
                dws.installImpl(def, cmdArgs.toArray(new String[0]), null, session, isDefaultVersion());
            }
        }
        if(session.isForce()){
            session=session.copy().yes();
        }
        for (NutsDefinition def : defsToInstallForced.values()) {
            dws.installImpl(def, cmdArgs.toArray(new String[0]), null, session.copy().yes(), isDefaultVersion());
        }
        for (NutsDefinition def : defsToDefVersion.values()) {
            dws.getInstalledRepository().setDefaultVersion(def.getId(), session);
        }
        if (emptyCommand) {
            throw new NutsExecutionException(ws, "Missing components to install", 1);
        }
        result = defsAll.values().toArray(new NutsDefinition[0]);
        return this;
    }

    @Override
    public NutsSearchResult<NutsDefinition> getResult() {
        if (result == null) {
            run();
        }
        return new NutsCollectionSearchResult<NutsDefinition>(ws,
                ids.isEmpty() ? null : ids.get(0).toString(),
                Arrays.asList(result)
        );
    }
}
