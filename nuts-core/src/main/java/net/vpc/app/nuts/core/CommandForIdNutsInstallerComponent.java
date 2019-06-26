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
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsExecutionContext;
import net.vpc.app.nuts.NutsExecutionEntry;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsInstallerComponent;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsSupportLevelContext;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

/**
 *
 * @author vpc
 */
class CommandForIdNutsInstallerComponent implements NutsInstallerComponent {

    @Override
    public void install(NutsExecutionContext executionContext) {
        NutsWorkspaceUtils.checkReadOnly(executionContext.getWorkspace());
        NutsId id = executionContext.getDefinition().getId();
        NutsDescriptor descriptor = executionContext.getDefinition().getDescriptor();
        if (descriptor.isNutsApplication()) {
            executionContext.getWorkspace().exec()
                    //                    .executionType(NutsExecutionType.EMBEDDED)
                    .command(executionContext.getDefinition())
                    .command("--nuts-exec-mode=install")
                    .addExecutorOptions("-Dnuts.export.debug").addCommand(executionContext.getArguments())
                    .setExecutionType(executionContext.getWorkspace().config().options().getExecutionType())
                    .failFast()
                    .run();
        }
    }

    @Override
    public void update(NutsExecutionContext executionContext) {
        NutsWorkspaceUtils.checkReadOnly(executionContext.getWorkspace());
        NutsId id = executionContext.getDefinition().getId();
        NutsDescriptor descriptor = executionContext.getDefinition().getDescriptor();
        if (descriptor.isNutsApplication()) {
            executionContext.getWorkspace().exec()
                    .command(id.setNamespace(null).toString(), "--nuts-exec-mode=update", "--force")
                    .addExecutorOptions().addCommand(executionContext.getArguments())
                    .failFast().run();
        }
    }

    @Override
    public void uninstall(NutsExecutionContext executionContext, boolean deleteData) {
        NutsSession session = executionContext.getSession();
        NutsWorkspace ws = executionContext.getWorkspace();
        NutsWorkspaceUtils.checkReadOnly(ws);
        NutsId id = executionContext.getDefinition().getId();
        if ("jar".equals(executionContext.getDefinition().getDescriptor().getPackaging())) {
            NutsExecutionEntry[] executionEntries = ws.io().parseExecutionEntries(executionContext.getDefinition().getPath());
            for (NutsExecutionEntry executionEntry : executionEntries) {
                if (executionEntry.isApp()) {
                    //
                    int r = ws.exec().command(id.getLongName(), "--nuts-exec-mode=uninstall", "--force").addCommand(executionContext.getArguments()).run().getResult();
                    session.out().printf("Installation Exited with code : " + r + " %n");
                }
            }
        }
        //            NutsId parseId = executionContext.getPrivateStoreNutsDefinition().getId();
        //            NutsWorkspaceConfigManager cc = executionContext.getWorkspace().getConfigManager();
        //            for (NutsWorkspaceCommand command : cc.findCommands(parseId)) {
        //                //install if installed with the very same parseVersion !!
        //                if (parseId.getLongName().equals(command.getId().getLongName())) {
        //                    cc.uninstallCommand(command.getName());
        //                }
        //            }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsDefinition> criteria) {
        return DEFAULT_SUPPORT;
    }

}
