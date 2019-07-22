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
package net.vpc.app.nuts.core.impl.def.wscommands;

import net.vpc.app.nuts.core.wscommands.AbstractNutsInstallCommand;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsInstallCommand;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.NutsConfirmationMode;
import net.vpc.app.nuts.NutsDependencyScopePattern;
import net.vpc.app.nuts.core.util.NutsCollectionSearchResult;
import net.vpc.app.nuts.NutsSearchResult;

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsInstallCommand extends AbstractNutsInstallCommand {

    public static final Logger LOG = Logger.getLogger(DefaultNutsInstallCommand.class.getName());

    public DefaultNutsInstallCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsInstallCommand run() {
        boolean emptyCommand = true;
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = getValidSession();
        NutsSession searchSession = session.copy().trace(false);
        PrintStream out = CoreIOUtils.resolveOut(session);
        ws.security().checkAllowed(NutsConstants.Permissions.INSTALL, "install");
        LinkedHashMap<NutsId,Boolean> allToInstall=new LinkedHashMap<>();
        Set<String> visited = new HashSet<>();
        if (this.isCompanions()) {
            for (String sid : dws.getCompanionIds()) {
                emptyCommand = false;
                if(!visited.contains(sid)) {
                    visited.add(sid);
                    List<NutsId> allIds = ws.search().id(sid).session(searchSession).latest().getResultIds().list();
                    if (allIds.isEmpty()) {
                        throw new NutsNotFoundException(ws, sid);
                    }
                    for (NutsId id0 : allIds) {
                        allToInstall.put(id0.setNamespace(null), false);
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
                    allToInstall.put(id0.setNamespace(null), false);
                    visited.add(id0.getLongName());
                }
            }
        }
        if(isInstalled()){
            for (NutsId resultId : ws.search().session(searchSession).installed().getResultIds()) {
                emptyCommand = false;
                if(!visited.contains(resultId.getLongName())) {
                    allToInstall.put(resultId.setNamespace(null), true);
                    visited.add(resultId.getLongName());
                }
            }
        }

        List<NutsDefinition> defsAll = new ArrayList<>();
        List<NutsDefinition> defsToInstall = new ArrayList<>();
        List<NutsDefinition> defsToInstallForced = new ArrayList<>();
        List<NutsDefinition> defsToDefVersion = new ArrayList<>();
        List<NutsDefinition> defsToIgnore = new ArrayList<>();
        for (Map.Entry<NutsId, Boolean> nutsIdBooleanEntry : allToInstall.entrySet()) {
            emptyCommand = false;
            NutsId nid=nutsIdBooleanEntry.getKey();
            boolean nForced = session.isForce() || nutsIdBooleanEntry.getValue();
            //must load dependencies because will be run later!!
            NutsDefinition def = ws.fetch().id(nid).session(searchSession)
                    .installInformation()
                    .setOptional(false)
                    .content()
                    .installInformation()
                    .effective()
                    .dependencies()
                    .scope(NutsDependencyScopePattern.RUN)
                    .failFast()
                    .getResultDefinition();
            if (def != null && def.getPath() != null) {
                boolean installed = def.getInstallInformation().isInstalled();
                boolean defVer = NutsWorkspaceExt.of(ws).getInstalledRepository().isDefaultVersion(def.getId());
                if(installed){
                    if (nForced || getValidSession().isYes()){
                        defsToInstallForced.add(def);
                    }else if(!defVer){
                        //installed, we only need to make it default!
                        defsToDefVersion.add(def);
                    }else{
                        //installed and default
                        defsToIgnore.add(def);
                    }
                }else{
                    defsToInstall.add(def);
                }
                defsAll.add(def);
            }
        }
        if (getValidSession().isPlainTrace() || (!defsToInstall.isEmpty() && getValidSession().getConfirm()==NutsConfirmationMode.ASK)) {
            if(!defsToInstall.isEmpty()) {
                out.println("The following {{new}} ==nuts== " + (defsToInstall.size() > 1 ? "components are" : "component is") + " going to be ##installed## : "
                        + defsToInstall.stream()
                        .map(x -> ws.id().setOmitImportedGroup(true).value(x.getId().getLongNameId()).format())
                        .collect(Collectors.joining(", ")));
            }
            if(!defsToInstallForced.isEmpty()) {
                out.println("The following already ##installed## ==nuts== " + (defsToInstallForced.size() > 1 ? "components are" : "component is") + " going to be [[reinstalled]] : "
                        + defsToInstallForced.stream()
                        .map(x -> ws.id().setOmitImportedGroup(true).value(x.getId().getLongNameId()).format())
                        .collect(Collectors.joining(", ")));
            }
            if(!defsToDefVersion.isEmpty()) {
                out.println("The following already ##installed## ==nuts== " + (defsToDefVersion.size() > 1 ? "components are" : "component is") + " going to be **set as default** : "
                        + defsToDefVersion.stream()
                        .map(x -> ws.id().setOmitImportedGroup(true).value(x.getId().getLongNameId()).format())
                        .collect(Collectors.joining(", ")));
            }
            if(!defsToIgnore.isEmpty()) {
                out.println("The following already ##installed## ==nuts== " + (defsToIgnore.size() > 1 ? "components are" : "component is") + " {{ignored}} : "
                        + defsToIgnore.stream()
                        .map(x -> ws.id().setOmitImportedGroup(true).value(x.getId().getLongNameId()).format())
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
        if (!defsToInstall.isEmpty()) {
            if(ws.io().getTerminal().ask().forBoolean("Continue installation")
                    .defaultValue(true)
                    .session(session).getBooleanValue()) {
                if(session.isForce()){
                    session=session.copy().yes();
                }
                for (NutsDefinition def : defsToInstall) {
                    dws.installImpl(def, cmdArgs.toArray(new String[0]), null, session, isDefaultVersion());
                }
            }
        }
        if(session.isForce()){
            session=session.copy().yes();
        }
        for (NutsDefinition def : defsToInstallForced) {
            dws.installImpl(def, cmdArgs.toArray(new String[0]), null, session.copy().yes(), isDefaultVersion());
        }
        for (NutsDefinition def : defsToDefVersion) {
            dws.getInstalledRepository().setDefaultVersion(def.getId(), session);
        }
        if (emptyCommand) {
            throw new NutsExecutionException(ws, "Missing components to install", 1);
        }
        result = defsAll.toArray(new NutsDefinition[0]);
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
