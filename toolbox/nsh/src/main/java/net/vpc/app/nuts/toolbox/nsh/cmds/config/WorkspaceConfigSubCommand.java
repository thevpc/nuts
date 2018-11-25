/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nsh.cmds.config;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandSyntaxError;
import net.vpc.app.nuts.toolbox.nsh.cmds.ConfigCommand;
import net.vpc.app.nuts.toolbox.nsh.options.ArchitectureNonOption;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.DefaultNonOption;
import net.vpc.common.commandline.FolderNonOption;
import net.vpc.common.strings.StringUtils;

import java.io.PrintStream;

/**
 *
 * @author vpc
 */
public class WorkspaceConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        if (cmdLine.readAllOnce("show location")) {
            if (cmdLine.isExecMode()) {
                NutsSession session = context.getSession();
                PrintStream out = session.getTerminal().getFormattedOut();
                out.printf("%s\n", context.getValidWorkspace().getConfigManager().getWorkspaceLocation());
            }
            return true;
        }
        if (cmdLine.readAllOnce("check updates")) {
            if (cmdLine.isExecMode()) {
                NutsSession session = context.getSession();
                context.getValidWorkspace().checkWorkspaceUpdates(false, null, session);
            }
            return true;
        }
        if (cmdLine.readAllOnce("update")) {
            NutsConfirmAction force = NutsConfirmAction.FORCE;
            String version = null;
            if (cmdLine.isExecMode()) {
                NutsSession session = context.getSession();
                NutsFile newVersion = null;
                try {
                    newVersion = context.getValidWorkspace().updateWorkspace(version, force, session);
                } catch (Exception ex) {
                    //not found
                    ex.printStackTrace();
                    session.getTerminal().getFormattedErr().printf("[[%s]]\n", ex.toString());
                }
                if (newVersion != null) {
                    PrintStream out = session.getTerminal().getFormattedOut();
                    out.printf("Workspace updated to [[%s]]\n", newVersion.getId());
                }
            }
            return true;
        }
        if (cmdLine.readAll("create workspace", "cw")) {
            boolean ignoreIdFound = false;
            boolean save = false;
            String archetype = null;
            String login = null;
            String password = null;
            boolean processed = false;
            while (cmdLine.hasNext()) {
                if (cmdLine.readAllOnce("-i", "--ignore")) {
                    ignoreIdFound = true;
                } else if (cmdLine.readAllOnce("-s", "--save")) {
                    save = true;
                } else if (cmdLine.readAllOnce("-h", "--archetype")) {
                    archetype = cmdLine.readRequiredNonOption(new ArchitectureNonOption("Archetype", context)).getStringOrError();
                } else if (cmdLine.readAllOnce("-u", "--login")) {
                    login = cmdLine.readRequiredNonOption(new DefaultNonOption("Login")).getStringOrError();
                } else if (cmdLine.readAllOnce("-x", "--password")) {
                    password = cmdLine.readRequiredNonOption(new DefaultNonOption("Password")).getStringOrError();
                } else {
                    String ws = cmdLine.readRequiredNonOption(new DefaultNonOption("NewWorkspaceName")).getString();
                    if (cmdLine.isExecMode()) {
                        NutsWorkspace workspace = context.getWorkspace().openWorkspace(
                                new NutsWorkspaceOptions()
                                        .setWorkspace(ws)
                                        .setArchetype(archetype)
                                        .setCreateIfNotFound(true)
                                        .setSaveIfCreated(save)
                                        .setCreateIfNotFound(ignoreIdFound)
                        );
                        if (!StringUtils.isEmpty(login)) {
                            workspace.getSecurityManager().login(login, password);
                        }
                        ConfigCommand.trySave(context, workspace, null, autoSave, cmdLine);
                    }
                    processed = true;
                    cmdLine.unexpectedArgument();
                }
            }
            if (cmdLine.isExecMode()) {
                if (!processed) {
                    throw new NutsCommandSyntaxError("incorrect command : create workspace");
                }
            }
            return true;
        } else if (cmdLine.readAll("set workspace version", "swv")) {
            while (cmdLine.hasNext()) {
                String version = cmdLine.readRequiredNonOption(new DefaultNonOption("version")).getString();
                Nuts.setConfigCurrentVersion(version,context.getWorkspace().getConfigManager().getNutsHomeLocation(),context.getWorkspace().getConfigManager().getWorkspaceLocation());
                cmdLine.unexpectedArgument();
            }
        } else if (cmdLine.readAll("get workspace version", "gwv")) {
            cmdLine.unexpectedArgument();
            String s=Nuts.getConfigCurrentVersion(context.getWorkspace().getConfigManager().getNutsHomeLocation(),context.getWorkspace().getConfigManager().getWorkspaceLocation());
            context.getFormattedOut().printf("%s\n",s==null?"":s);
        } else if (cmdLine.readAll("set workspace", "sw")) {
            boolean createIfNotFound = false;
            boolean save = true;
            String login = null;
            String password = null;
            String archetype = null;
            int wsCount = 0;
            boolean processed = false;
            while (wsCount == 0 || cmdLine.hasNext()) {
                if (cmdLine.readAllOnce("-c", "--create")) {
                    createIfNotFound = true;
                } else if (cmdLine.readAllOnce("-s", "--save")) {
                    save = true;
                } else if (cmdLine.readAllOnce("-s", "--nosave")) {
                    save = false;
                } else if (cmdLine.readAllOnce("-h", "--archetype")) {
                    archetype = cmdLine.readRequiredNonOption(new ArchitectureNonOption("Archetype", context)).getStringOrError();
                } else if (cmdLine.readAllOnce("-u", "--login")) {
                    login = cmdLine.readRequiredNonOption(new DefaultNonOption("Username")).getStringOrError();
                } else if (cmdLine.readAllOnce("-x", "--password")) {
                    password = cmdLine.readRequiredNonOption(new DefaultNonOption("Password")).getStringOrError();
                } else {
                    String ws = cmdLine.readRequiredNonOption(new FolderNonOption("WorkspacePath")).getString();
                    wsCount++;
                    cmdLine.unexpectedArgument();
                    processed = true;
                    if (cmdLine.isExecMode()) {
                        NutsWorkspace workspace = context.getValidWorkspace().openWorkspace(
                                new NutsWorkspaceOptions()
                                        .setWorkspace(ws)
                                        .setArchetype(archetype)
                                        .setSaveIfCreated(save)
                                        .setCreateIfNotFound(createIfNotFound)
                        );
                        if (!StringUtils.isEmpty(login)) {
                            workspace.getSecurityManager().login(login, password);
                        }
                        context.setWorkspace(workspace);
                        ConfigCommand.trySave(context, workspace, null, autoSave, cmdLine);
                    }
                }
            }
            cmdLine.unexpectedArgument();
            if (cmdLine.isExecMode()) {
                if (!processed) {
                    throw new NutsCommandSyntaxError("incorrect command : create workspace");
                }
            }
            return true;
        } else if (cmdLine.readAll("save workspace", "save", "sw")) {
            cmdLine.unexpectedArgument();
            if (cmdLine.isExecMode()) {
                ConfigCommand.trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

}
