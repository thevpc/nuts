/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.impl.def.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.NutsExtensionListHelper;
import net.vpc.app.nuts.core.impl.def.config.DefaultNutsWorkspaceConfigManager;
import net.vpc.app.nuts.core.spi.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.core.wscommands.AbstractNutsUninstallCommand;
import net.vpc.app.nuts.core.DefaultNutsInstallEvent;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultNutsUninstallCommand extends AbstractNutsUninstallCommand {

    public DefaultNutsUninstallCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsUninstallCommand run() {
        NutsWorkspaceUtils.checkReadOnly(ws);
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = NutsWorkspaceUtils.validateSession(ws, this.getSession());
        ws.security().checkAllowed(NutsConstants.Permissions.UNINSTALL, "uninstall");
        NutsSession searchSession = session.copy().trace(false);
        List<NutsDefinition> defs = new ArrayList<>();
        for (NutsId id : this.getIds()) {
            List<NutsDefinition> resultDefinitions = ws.search().id(id).installed().setSession(searchSession).setTransitive(false).setOptional(false)
                    .setInstallInformation(true).getResultDefinitions().list();
            for (Iterator<NutsDefinition> it = resultDefinitions.iterator(); it.hasNext();) {
                NutsDefinition resultDefinition = it.next();
                if (!resultDefinition.getInstallInformation().isInstalled()) {
                    it.remove();
                }
            }
            if (resultDefinitions.isEmpty()) {
                throw new NutsIllegalArgumentException(ws, id + " is not installed");
            }
            defs.addAll(resultDefinitions);
        }
        for (NutsDefinition def : defs) {
            NutsId id = dws.resolveEffectiveId(def.getDescriptor(), ws.fetch().session(searchSession));

            NutsInstallerComponent ii = dws.getInstaller(def, session);
            PrintStream out = CoreIOUtils.resolveOut(session);
            if (ii != null) {
                NutsExecutionContext executionContext = dws.createNutsExecutionContext(def, this.getArgs(), new String[0], session,
                        true,
                        false,
                        ws.config().options().getExecutionType(),
                        null);
                ii.uninstall(executionContext, this.isErase());
            }
            try {
                dws.getInstalledRepository().uninstall(id, session);
                CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.APPS).toFile());
                CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.TEMP).toFile());
                CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.LOG).toFile());
                if (this.isErase()) {
                    CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.VAR).toFile());
                    CoreIOUtils.delete(ws.config().getStoreLocation(id, NutsStoreLocation.CONFIG).toFile());
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            if(def.isExtension()){
                NutsWorkspaceConfigManagerExt wcfg = NutsWorkspaceConfigManagerExt.of(ws.config());
                NutsExtensionListHelper h = new NutsExtensionListHelper(wcfg.getStoredConfigBoot().getExtensions())
                        .save();
                h.remove(def.getId());
                wcfg.getStoredConfigBoot().setExtensions(h.getConfs());
                wcfg.fireConfigurationChanged("extensions",session, DefaultNutsWorkspaceConfigManager.ConfigEventType.BOOT);
            }
            if (getValidSession().isPlainTrace()) {
                out.printf("%N uninstalled ##successfully##%n", ws.id().value(id).format());
            }
            NutsWorkspaceUtils.Events.fireOnUninstall(ws,new DefaultNutsInstallEvent(def, session, isErase()));
        }
        return this;
    }
}
