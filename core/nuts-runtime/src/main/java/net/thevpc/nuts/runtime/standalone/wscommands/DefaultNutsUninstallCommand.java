/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.config.ConfigEventType;
import net.thevpc.nuts.runtime.core.events.DefaultNutsInstallEvent;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.AbstractNutsUninstallCommand;
import net.thevpc.nuts.runtime.standalone.NutsExtensionListHelper;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

import java.io.PrintStream;
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
        NutsWorkspaceUtils.of(ws).checkReadOnly();
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = this.getValidWorkspaceSession();
        ws.security().checkAllowed(NutsConstants.Permissions.UNINSTALL, "uninstall", session);
        NutsSession searchSession = CoreNutsUtils.silent(session);
        List<NutsDefinition> defs = new ArrayList<>();
        for (NutsId id : this.getIds()) {
            List<NutsDefinition> resultDefinitions = ws.search().addId(id)
                    .setInstallStatus(ws.filters().installStatus().byInstalled())
                    .setSession(searchSession.copy())
                    .setTransitive(false).setOptional(false)
                    .getResultDefinitions().list();
            for (Iterator<NutsDefinition> it = resultDefinitions.iterator(); it.hasNext(); ) {
                NutsDefinition resultDefinition = it.next();
                if (!resultDefinition.getInstallInformation().isInstalledOrRequired()) {
                    it.remove();
                }
            }
            if (resultDefinitions.isEmpty()) {
                throw new NutsIllegalArgumentException(ws, id + " is not installed");
            }
            defs.addAll(resultDefinitions);
        }
        for (NutsDefinition def : defs) {
            NutsId id = dws.resolveEffectiveId(def.getDescriptor(), searchSession);

            NutsInstallerComponent ii = dws.getInstaller(def, session);
            PrintStream out = CoreIOUtils.resolveOut(session);
            if (ii != null) {
                NutsExecutionContext executionContext = dws.createExecutionContext()
                        .setDefinition(def)
                        .setArguments(getArgs())
                        .setExecSession(session)
                        .setTraceSession(session)
                        .setFailFast(true)
                        .setTemporary(false)
                        .setExecutionType(ws.config().options().getExecutionType())
                        .build()
                        ;
                ii.uninstall(executionContext, this.isErase());
            }

            dws.getInstalledRepository().uninstall(id, session);
            CoreIOUtils.delete(getValidWorkspaceSession(), Paths.get(ws.locations().getStoreLocation(id, NutsStoreLocation.APPS)).toFile());
            CoreIOUtils.delete(getValidWorkspaceSession(), Paths.get(ws.locations().getStoreLocation(id, NutsStoreLocation.TEMP)).toFile());
            CoreIOUtils.delete(getValidWorkspaceSession(), Paths.get(ws.locations().getStoreLocation(id, NutsStoreLocation.LOG)).toFile());
            if (this.isErase()) {
                CoreIOUtils.delete(getValidWorkspaceSession(), Paths.get(ws.locations().getStoreLocation(id, NutsStoreLocation.VAR)).toFile());
                CoreIOUtils.delete(getValidWorkspaceSession(), Paths.get(ws.locations().getStoreLocation(id, NutsStoreLocation.CONFIG)).toFile());
            }

            if (def.getType() == NutsIdType.EXTENSION) {
                NutsWorkspaceConfigManagerExt wcfg = NutsWorkspaceConfigManagerExt.of(ws.config());
                NutsExtensionListHelper h = new NutsExtensionListHelper(wcfg.getStoredConfigBoot().getExtensions())
                        .save();
                h.remove(def.getId());
                wcfg.getStoredConfigBoot().setExtensions(h.getConfs());
                wcfg.fireConfigurationChanged("extensions", session, ConfigEventType.BOOT);
            }
            if (getValidWorkspaceSession().isPlainTrace()) {
                out.println(ws.id().formatter(id).format() + " uninstalled "+ws.formats().text().factory().styled(
                        "successfully",NutsTextNodeStyle.success()
                ));
            }
            NutsWorkspaceUtils.of(ws).events().fireOnUninstall(new DefaultNutsInstallEvent(def, session,new NutsId[0], isErase()));
        }
        return this;
    }
}
