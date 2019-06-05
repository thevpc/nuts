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
import net.vpc.app.nuts.NutsExecCommand;
import net.vpc.app.nuts.NutsExecutionContext;
import net.vpc.app.nuts.NutsExecutionEntry;
import net.vpc.app.nuts.NutsExecutionType;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsInstallerComponent;
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
                    .command(id.setNamespace(null).toString(), "--nuts-exec-mode=on-install", "--force")
                    .addExecutorOptions("-Dnuts.export.debug").addCommand(executionContext.getArguments())
                    .setExecutionType(executionContext.getWorkspace().config().getOptions().getExecutionType())
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
                    .command(id.setNamespace(null).toString(), "--nuts-exec-mode=on-update", "--force")
                    .addExecutorOptions().addCommand(executionContext.getArguments())
                    .failFast().run();
        }
    }

    @Override
    public void uninstall(NutsExecutionContext executionContext, boolean deleteData) {
        NutsWorkspaceUtils.checkReadOnly(executionContext.getWorkspace());
        NutsId id = executionContext.getDefinition().getId();
        if ("jar".equals(executionContext.getDefinition().getDescriptor().getPackaging())) {
            NutsExecutionEntry[] executionEntries = executionContext.getWorkspace().parser().parseExecutionEntries(executionContext.getDefinition().getPath());
            for (NutsExecutionEntry executionEntry : executionEntries) {
                if (executionEntry.isApp()) {
                    //
                    int r = executionContext.getWorkspace().exec().command(id.toString(), "--nuts-exec-mode=on-uninstall", "--force").addCommand(executionContext.getArguments()).run().getResult();
                    executionContext.getWorkspace().io().getTerminal().fout().printf("Installation Exited with code : " + r + " %n");
                }
            }
        }
        //            NutsId id = executionContext.getPrivateStoreNutsDefinition().getId();
        //            NutsWorkspaceConfigManager cc = executionContext.getWorkspace().getConfigManager();
        //            for (NutsWorkspaceCommand command : cc.findCommands(id)) {
        //                //install if installed with the very same version !!
        //                if (id.getLongName().equals(command.getId().getLongName())) {
        //                    cc.uninstallCommand(command.getName());
        //                }
        //            }
    }

    @Override
    public int getSupportLevel(NutsDefinition criteria) {
        return 0;
    }

}
