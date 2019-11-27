/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.NutsExtensionListHelper;
import net.vpc.app.nuts.main.config.DefaultNutsWorkspaceConfigManager;
import net.vpc.app.nuts.core.config.NutsWorkspaceConfigManagerExt;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.wscommands.AbstractNutsUninstallCommand;
import net.vpc.app.nuts.runtime.DefaultNutsInstallEvent;
import net.vpc.app.nuts.core.NutsWorkspaceExt;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;

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
        NutsWorkspaceUtils.of(ws).checkReadOnly();
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = NutsWorkspaceUtils.of(ws).validateSession( this.getSession());
        ws.security().checkAllowed(NutsConstants.Permissions.UNINSTALL, "uninstall");
        NutsSession searchSession = CoreNutsUtils.silent(session);
        List<NutsDefinition> defs = new ArrayList<>();
        for (NutsId id : this.getIds()) {
            List<NutsDefinition> resultDefinitions = ws.search().id(id).installed().session(searchSession)
                    .transitive(false).optional(false)
                    .getResultDefinitions().list();
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
                CoreIOUtils.delete(ws,ws.config().getStoreLocation(id, NutsStoreLocation.APPS).toFile());
                CoreIOUtils.delete(ws,ws.config().getStoreLocation(id, NutsStoreLocation.TEMP).toFile());
                CoreIOUtils.delete(ws,ws.config().getStoreLocation(id, NutsStoreLocation.LOG).toFile());
                if (this.isErase()) {
                    CoreIOUtils.delete(ws,ws.config().getStoreLocation(id, NutsStoreLocation.VAR).toFile());
                    CoreIOUtils.delete(ws,ws.config().getStoreLocation(id, NutsStoreLocation.CONFIG).toFile());
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            if(def.getType()==NutsIdType.EXTENSION){
                NutsWorkspaceConfigManagerExt wcfg = NutsWorkspaceConfigManagerExt.of(ws.config());
                NutsExtensionListHelper h = new NutsExtensionListHelper(wcfg.getStoredConfigBoot().getExtensions())
                        .save();
                h.remove(def.getId());
                wcfg.getStoredConfigBoot().setExtensions(h.getConfs());
                wcfg.fireConfigurationChanged("extensions",session, DefaultNutsWorkspaceConfigManager.ConfigEventType.BOOT);
            }
            if (getValidSession().isPlainTrace()) {
                out.println(ws.id().value(id).format()+" uninstalled ##successfully##");
            }
            NutsWorkspaceUtils.of(ws).events().fireOnUninstall(new DefaultNutsInstallEvent(def, session, isErase()));
        }
        return this;
    }
}
