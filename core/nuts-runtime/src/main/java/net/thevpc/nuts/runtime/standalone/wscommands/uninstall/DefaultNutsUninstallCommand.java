/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.uninstall;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.config.ConfigEventType;
import net.thevpc.nuts.runtime.core.events.DefaultNutsInstallEvent;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.NutsExtensionListHelper;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.uninstall.AbstractNutsUninstallCommand;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        NutsWorkspace ws = getSession().getWorkspace();
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = this.getSession();
        ws.security().setSession(getSession()).checkAllowed(NutsConstants.Permissions.UNINSTALL, "uninstall");
        List<NutsDefinition> defs = new ArrayList<>();
        for (NutsId id : this.getIds()) {
            List<NutsDefinition> resultDefinitions = ws.search().addId(id)
                    .setInstallStatus(ws.filters().installStatus().byInstalled(true))
                    .setSession(session.copy().setTransitive(false))
                    .setOptional(false).setEffective(true)
                    .setContent(true)//include content so that we can remove it by calling executor
                    .getResultDefinitions().list();
            for (Iterator<NutsDefinition> it = resultDefinitions.iterator(); it.hasNext();) {
                NutsDefinition resultDefinition = it.next();
                if (!resultDefinition.getInstallInformation().isInstalledOrRequired()) {
                    it.remove();
                }
            }
            if (resultDefinitions.isEmpty()) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("not installed : %s",id));
            }
            defs.addAll(resultDefinitions);
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
                        .setTraceSession(session)
                        .setWorkspace(session.getWorkspace())
                        .setFailFast(true)
                        .setTemporary(false)
                        .setExecutionType(ws.env().getBootOptions().getExecutionType())
                        .setRunAs(NutsRunAs.currentUser())//uninstall always uses current user
                        .build();
                ii.uninstall(executionContext, this.isErase());
            }

            dws.getInstalledRepository().uninstall(def, session);
            NutsId id = def.getId();
            CoreIOUtils.delete(getSession(), Paths.get(ws.locations().getStoreLocation(id, NutsStoreLocation.APPS)).toFile());
            CoreIOUtils.delete(getSession(), Paths.get(ws.locations().getStoreLocation(id, NutsStoreLocation.TEMP)).toFile());
            CoreIOUtils.delete(getSession(), Paths.get(ws.locations().getStoreLocation(id, NutsStoreLocation.LOG)).toFile());
            if (this.isErase()) {
                CoreIOUtils.delete(getSession(), Paths.get(ws.locations().getStoreLocation(id, NutsStoreLocation.VAR)).toFile());
                CoreIOUtils.delete(getSession(), Paths.get(ws.locations().getStoreLocation(id, NutsStoreLocation.CONFIG)).toFile());
            }

            if (def.getType() == NutsIdType.EXTENSION) {
                NutsWorkspaceConfigManagerExt wcfg = NutsWorkspaceConfigManagerExt.of(ws.config());
                NutsExtensionListHelper h = new NutsExtensionListHelper(wcfg.getModel().getStoredConfigBoot().getExtensions())
                        .save();
                h.remove(id);
                wcfg.getModel().getStoredConfigBoot().setExtensions(h.getConfs());
                wcfg.getModel().fireConfigurationChanged("extensions", session, ConfigEventType.BOOT);
            }
            if (getSession().isPlainTrace()) {
                out.printf("%s uninstalled %s%n", id, ws.text().forStyled(
                        "successfully", NutsTextStyle.success()
                ));
            }
            NutsWorkspaceUtils.of(session).events().fireOnUninstall(new DefaultNutsInstallEvent(def, session, new NutsId[0], isErase()));
        }
        return this;
    }
}
