/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;
import net.vpc.common.strings.StringUtils;

import java.io.PrintStream;

/**
 *
 * @author vpc
 */
public class WorkspaceNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        if (cmdLine.next("show location") != null) {
            if (cmdLine.isExecMode()) {
                NutsSession session = context.getSession();
                PrintStream out = session.getTerminal().fout();
                out.printf("%s%n", context.getWorkspace().config().getWorkspaceLocation());
            }
            return true;
        }
        if (cmdLine.next("create workspace", "cw") != null) {
            boolean ignoreIdFound = false;
            boolean save = false;
            String archetype = null;
            String login = null;
            char[] password = null;
            boolean processed = false;
            while (cmdLine.hasNext()) {
                if (cmdLine.next("-i", "--ignore") != null) {
                    ignoreIdFound = true;
                } else if (cmdLine.next("-s", "--save") != null) {
                    save = true;
                } else if (cmdLine.next("-h", "--archetype") != null) {
                    archetype = cmdLine.required().nextNonOption(cmdLine.createName("Archetype")).required().getString();
                } else if (cmdLine.next("-u", "--login") != null) {
                    login = cmdLine.required().nextNonOption(cmdLine.createName("Login")).required().getString();
                } else if (cmdLine.next("-x", "--password") != null) {
                    password = cmdLine.required().nextNonOption(cmdLine.createName("Password")).required().getString().toCharArray();
                } else {
                    String ws = cmdLine.required().nextNonOption(cmdLine.createName("NewWorkspaceName")).getString();
                    if (cmdLine.isExecMode()) {
                        NutsWorkspace workspace = context.getWorkspace().openWorkspace(
                                new NutsWorkspaceOptions()
                                        .setWorkspace(ws)
                                        .setArchetype(archetype)
                                        .setOpenMode(ignoreIdFound ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : NutsWorkspaceOpenMode.CREATE_NEW)
                                        .setReadOnly(!save)
                        );
                        if (!StringUtils.isBlank(login)) {
                            workspace.security().login(login, password);
                        }
                        trySave(context, workspace, null, autoSave, cmdLine);
                    }
                    processed = true;
                    cmdLine.setCommandName("config create workspace").unexpectedArgument();
                }
            }
            if (cmdLine.isExecMode()) {
                if (!processed) {
                    throw new NutsExecutionException(context.getWorkspace(), "config: incorrect command : create workspace", 2);
                }
            }
            return true;
        } else if (cmdLine.next("set workspace api-version") != null) {
            String version = cmdLine.required().nextNonOption(cmdLine.createName("version")).getString();
            NutsBootConfig c = context.getWorkspace().config().getBootConfig();
            c.setApiVersion(version);
            context.getWorkspace().config().setBootConfig(c);
            cmdLine.setCommandName("config set workspace version").unexpectedArgument();

        } else if (cmdLine.next("set workspace runtime-version", "set workspace runtime-id") != null) {
            String version = cmdLine.required().nextNonOption(cmdLine.createName("version")).getString();
            NutsBootConfig c = context.getWorkspace().config().getBootConfig();
            if (version.contains("#")) {
                c.setRuntimeId(NutsConstants.Ids.NUTS_RUNTIME + "#" + version);
            } else {
                c.setRuntimeId(version);
            }
            context.getWorkspace().config().setBootConfig(c);
            cmdLine.setCommandName("config set workspace version").unexpectedArgument();

        } else if (cmdLine.next("set workspace", "sw") != null) {
            boolean createIfNotFound = false;
            boolean save = true;
            String login = null;
            char[] password = null;
            String archetype = null;
            int wsCount = 0;
            boolean processed = false;
            while (wsCount == 0 || cmdLine.hasNext()) {
                if (cmdLine.next("-c", "--create") != null) {
                    createIfNotFound = true;
                } else if (cmdLine.next("-s", "--save") != null) {
                    save = true;
                } else if (cmdLine.next("-s", "--nosave") != null) {
                    save = false;
                } else if (cmdLine.next("-h", "--archetype") != null) {
                    archetype = cmdLine.required().nextNonOption(cmdLine.createName("Archetype")).required().getString();
                } else if (cmdLine.next("-u", "--login") != null) {
                    login = cmdLine.required().nextNonOption(cmdLine.createName("Username")).required().getString();
                } else if (cmdLine.next("-x", "--password") != null) {
                    password = cmdLine.required().nextNonOption(cmdLine.createName("Password")).required().getString().toCharArray();
                } else {
                    String ws = cmdLine.required().nextNonOption(cmdLine.createName("WorkspacePath")).getString();
                    wsCount++;
                    cmdLine.setCommandName("config set workspace").unexpectedArgument();
                    processed = true;
                    if (cmdLine.isExecMode()) {
                        NutsWorkspace workspace = context.getWorkspace().openWorkspace(
                                new NutsWorkspaceOptions()
                                        .setWorkspace(ws)
                                        .setArchetype(archetype)
                                        .setReadOnly(!save)
                                        .setOpenMode(createIfNotFound ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : NutsWorkspaceOpenMode.OPEN_EXISTING)
                        );
                        if (!StringUtils.isBlank(login)) {
                            workspace.security().login(login, password);
                        }
                        //TODO Unsupported set workspace
                        context.session().out().print("Unsupported set workspace....");
                        //context.consoleContext().setWorkspace(workspace);
                        trySave(context, workspace, null, autoSave, cmdLine);
                    }
                }
            }
            cmdLine.setCommandName("config set workspace").unexpectedArgument();
            if (cmdLine.isExecMode()) {
                if (!processed) {
                    throw new NutsExecutionException(context.getWorkspace(), "incorrect command : create workspace", 2);
                }
            }
            return true;
        } else if (cmdLine.next("save workspace", "save", "sw") != null) {
            cmdLine.setCommandName("config save workspace").unexpectedArgument();
            if (cmdLine.isExecMode()) {
                trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
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
