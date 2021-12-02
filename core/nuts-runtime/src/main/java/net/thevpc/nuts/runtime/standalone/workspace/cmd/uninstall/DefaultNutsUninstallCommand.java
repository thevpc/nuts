/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.ConfigEventType;
import net.thevpc.nuts.runtime.standalone.event.DefaultNutsInstallEvent;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.extensions.NutsExtensionListHelper;
import net.thevpc.nuts.spi.NutsInstallerComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNutsUninstallCommand extends AbstractNutsUninstallCommand {

    public DefaultNutsUninstallCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsUninstallCommand run() {
        checkSession();
        NutsWorkspaceUtils.of(getSession()).checkReadOnly();
        checkSession();
        NutsSession session = getSession();
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(session.getWorkspace());
        session.security().setSession(getSession()).checkAllowed(NutsConstants.Permissions.UNINSTALL, "uninstall");
        List<NutsDefinition> defs = new ArrayList<>();
        NutsId[] nutsIds = this.getIds();
        if (nutsIds.length == 0) {
            throw new NutsExecutionException(getSession(), NutsMessage.cstyle("missing packages to uninstall"), 1);
        }
        for (NutsId id : nutsIds) {
            List<NutsDefinition> resultDefinitions = session.search().addId(id)
                    .setInstallStatus(NutsInstallStatusFilters.of(session).byInstalled(true))
                    .setSession(session.copy().setTransitive(false))
                    .setOptional(false).setEffective(true)
                    .setContent(true)//include content so that we can remove it by calling executor
                    .getResultDefinitions().toList();
            resultDefinitions.removeIf(it -> !it.getInstallInformation().isInstalledOrRequired());
            if (resultDefinitions.isEmpty()) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("not installed : %s", id));
            }
            defs.addAll(resultDefinitions);
        }

        NutsMemoryPrintStream mout = NutsMemoryPrintStream.of(session);
        mout.println("should we proceed?");
        NutsMessage cancelMessage = NutsMessage.cstyle("removal cancelled : %s", defs.stream()
                .map(NutsDefinition::getId)
                .map(NutsId::getFullName)
                .collect(Collectors.joining(", ")));
        if (!defs.isEmpty() && !getSession().getTerminal().ask()
                .resetLine()
                .setSession(session)
                .forBoolean(mout.toString())
                .setDefaultValue(true)
                .setCancelMessage(cancelMessage)
                .getBooleanValue()) {
            throw new NutsUserCancelException(getSession(), cancelMessage);
        }

        for (NutsDefinition def : defs) {
//            NutsId id = dws.resolveEffectiveId(def.getDescriptor(), searchSession);

            NutsInstallerComponent ii = dws.getInstaller(def, session);
            NutsPrintStream out = CoreIOUtils.resolveOut(session);
            if (ii != null) {
                NutsExecutionContext executionContext = dws.createExecutionContext()
                        .setDefinition(def)
                        .setArguments(getArgs())
                        .setExecSession(session)
                        .setSession(session)
                        .setWorkspace(session.getWorkspace())
                        .setFailFast(true)
                        .setTemporary(false)
                        .setExecutionType(session.boot().getBootOptions().getExecutionType())
                        .setRunAs(NutsRunAs.currentUser())//uninstall always uses current user
                        .build();
                ii.uninstall(executionContext, this.isErase());
            }

            dws.getInstalledRepository().uninstall(def, session);
            NutsId id = def.getId();
            session.locations().getStoreLocation(id, NutsStoreLocation.APPS).deleteTree();
            session.locations().getStoreLocation(id, NutsStoreLocation.LOG).deleteTree();
            if (this.isErase()) {
                session.locations().getStoreLocation(id, NutsStoreLocation.VAR).deleteTree();
                session.locations().getStoreLocation(id, NutsStoreLocation.CONFIG).deleteTree();
            }

            if (def.getDescriptor().getIdType() == NutsIdType.EXTENSION) {
                NutsWorkspaceConfigManagerExt wcfg = NutsWorkspaceConfigManagerExt.of(session.config());
                NutsExtensionListHelper h = new NutsExtensionListHelper(wcfg.getModel().getStoredConfigBoot().getExtensions())
                        .save();
                h.remove(id);
                wcfg.getModel().getStoredConfigBoot().setExtensions(h.getConfs());
                wcfg.getModel().fireConfigurationChanged("extensions", session, ConfigEventType.BOOT);
            }
            if (getSession().isPlainTrace()) {
                out.printf("%s uninstalled %s%n", id, NutsTexts.of(session).ofStyled(
                        "successfully", NutsTextStyle.success()
                ));
            }
            NutsWorkspaceUtils.of(session).events().fireOnUninstall(new DefaultNutsInstallEvent(def, session, new NutsId[0], isErase()));
        }
        return this;
    }
}
