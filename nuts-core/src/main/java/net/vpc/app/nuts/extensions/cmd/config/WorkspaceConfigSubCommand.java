/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.cmd.config;

import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsCommandSyntaxError;
import net.vpc.app.nuts.NutsFile;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceCreateOptions;
import net.vpc.app.nuts.extensions.cmd.AbstractConfigSubCommand;
import net.vpc.app.nuts.extensions.cmd.ConfigCommand;
import net.vpc.app.nuts.extensions.cmd.cmdline.ArchitectureNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.cmd.cmdline.DefaultNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.FolderNonOption;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class WorkspaceConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CmdLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        if (cmdLine.readOnce("show location")) {
            if (cmdLine.isExecMode()) {
                NutsSession session = context.getSession();
                session.getTerminal().getOut().drawln(context.getValidWorkspace().getWorkspaceLocation());
            }
            return true;
        }
        if (cmdLine.readOnce("check updates")) {
            if (cmdLine.isExecMode()) {
                NutsSession session = context.getSession();
                context.getValidWorkspace().checkWorkspaceUpdates(false, null, session);
            }
            return true;
        }
        if (cmdLine.readOnce("update")) {
            if (cmdLine.isExecMode()) {
                NutsSession session = context.getSession();
                NutsFile newVersion = null;
                try {
                    newVersion = context.getValidWorkspace().updateWorkspace(session);
                } catch (Exception ex) {
                    //not found
                }
                if (newVersion != null) {
                    session.getTerminal().getOut().drawln("Workspace updated to [[" + newVersion.getId() + "]]");
                }
            }
            return true;
        }
        if (cmdLine.read("create workspace", "cw")) {
            boolean ignoreIdFound = false;
            boolean save = false;
            String archetype = null;
            String login = null;
            String password = null;
            boolean processed = false;
            while (!cmdLine.isEmpty()) {
                if (cmdLine.readOnce("-i", "--ignore")) {
                    ignoreIdFound = true;
                } else if (cmdLine.readOnce("-s", "--save")) {
                    save = true;
                } else if (cmdLine.readOnce("-h", "--archetype")) {
                    archetype = cmdLine.readNonOptionOrError(new ArchitectureNonOption("Archetype", context)).getStringOrError();
                } else if (cmdLine.readOnce("-u", "--login")) {
                    login = cmdLine.readNonOptionOrError(new DefaultNonOption("Login")).getStringOrError();
                } else if (cmdLine.readOnce("-x", "--password")) {
                    password = cmdLine.readNonOptionOrError(new DefaultNonOption("Password")).getStringOrError();
                } else {
                    String ws = cmdLine.readNonOptionOrError(new DefaultNonOption("NewWorkspaceName")).getString();
                    if (cmdLine.isExecMode()) {
                        NutsWorkspace workspace = context.getWorkspace().openWorkspace(
                                ws,
                                new NutsWorkspaceCreateOptions()
                                        .setArchetype(archetype)
                                        .setCreateIfNotFound(true)
                                        .setSaveIfCreated(save)
                                        .setCreateIfNotFound(ignoreIdFound)
                        );
                        if (!CoreStringUtils.isEmpty(login)) {
                            workspace.login(login, password);
                        }
                        ConfigCommand.trySave(context, workspace, null, autoSave, cmdLine);
                    }
                    processed = true;
                    cmdLine.requireEmpty();
                }
            }
            if (cmdLine.isExecMode()) {
                if (!processed) {
                    throw new NutsCommandSyntaxError("incorrect command : create workspace");
                }
            }
            return true;
        } else if (cmdLine.read("set workspace", "sw")) {
            boolean createIfNotFound = false;
            boolean save = true;
            String login = null;
            String password = null;
            String archetype = null;
            int wsCount = 0;
            boolean processed = false;
            while (wsCount == 0 || !cmdLine.isEmpty()) {
                if (cmdLine.readOnce("-c", "--create")) {
                    createIfNotFound = true;
                } else if (cmdLine.readOnce("-s", "--save")) {
                    save = true;
                } else if (cmdLine.readOnce("-s", "--nosave")) {
                    save = false;
                } else if (cmdLine.readOnce("-h", "--archetype")) {
                    archetype = cmdLine.readNonOptionOrError(new ArchitectureNonOption("Archetype", context)).getStringOrError();
                } else if (cmdLine.readOnce("-u", "--login")) {
                    login = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getStringOrError();
                } else if (cmdLine.readOnce("-x", "--password")) {
                    password = cmdLine.readNonOptionOrError(new DefaultNonOption("Password")).getStringOrError();
                } else {
                    String ws = cmdLine.readNonOptionOrError(new FolderNonOption("WorkspacePath")).getString();
                    wsCount++;
                    cmdLine.requireEmpty();
                    processed = true;
                    if (cmdLine.isExecMode()) {
                        NutsWorkspace workspace = context.getValidWorkspace().openWorkspace(ws,
                                new NutsWorkspaceCreateOptions()
                                        .setArchetype(archetype)
                                        .setSaveIfCreated(save)
                                        .setCreateIfNotFound(createIfNotFound)
                        );
                        if (!CoreStringUtils.isEmpty(login)) {
                            workspace.login(login, password);
                        }
                        context.setWorkspace(workspace);
                        ConfigCommand.trySave(context, workspace, null, autoSave, cmdLine);
                    }
                }
            }
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                if (!processed) {
                    throw new NutsCommandSyntaxError("incorrect command : create workspace");
                }
            }
            return true;
        } else if (cmdLine.read("save workspace", "save", "sw")) {
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                ConfigCommand.trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT;
    }

}
