/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;
import net.vpc.app.nuts.app.options.ArchitectureNonOption;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.DefaultNonOption;
import net.vpc.common.commandline.FolderNonOption;
import net.vpc.common.strings.StringUtils;

import java.io.PrintStream;

/**
 *
 * @author vpc
 */
public class WorkspaceNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        if (cmdLine.readAllOnce("show location")) {
            if (cmdLine.isExecMode()) {
                NutsSession session = context.getSession();
                PrintStream out = session.getTerminal().fout();
                out.printf("%s%n", context.getWorkspace().config().getWorkspaceLocation());
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
                    archetype = cmdLine.readRequiredNonOption(new ArchitectureNonOption("Archetype", context.getWorkspace())).getStringOrError();
                } else if (cmdLine.readAllOnce("-u", "--login")) {
                    login = cmdLine.readRequiredNonOption(new DefaultNonOption("Login")).getStringOrError();
                } else if (cmdLine.readAllOnce("-x", "--password")) {
                    password = cmdLine.readRequiredNonOption(new DefaultNonOption("Password")).getStringOrError();
                } else {
                    String ws = cmdLine.readRequiredNonOption(new DefaultNonOption("NewWorkspaceName")).getStringExpression();
                    if (cmdLine.isExecMode()) {
                        NutsWorkspace workspace = context.getWorkspace().openWorkspace(
                                new NutsWorkspaceOptions()
                                        .setWorkspace(ws)
                                        .setArchetype(archetype)
                                        .setOpenMode(ignoreIdFound ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : NutsWorkspaceOpenMode.CREATE_NEW)
                                        .setReadOnly(!save)
                        );
                        if (!StringUtils.isEmpty(login)) {
                            workspace.security().login(login, password);
                        }
                        trySave(context, workspace, null, autoSave, cmdLine);
                    }
                    processed = true;
                    cmdLine.unexpectedArgument("config create workspace");
                }
            }
            if (cmdLine.isExecMode()) {
                if (!processed) {
                    throw new NutsExecutionException("config: incorrect command : create workspace", 2);
                }
            }
            return true;
        } else if (cmdLine.readAll("set workspace boot-version")) {
            String version = cmdLine.readRequiredNonOption(new DefaultNonOption("version")).getStringExpression();
            NutsBootConfig c = context.getWorkspace().config().getBootConfig();
            c.setApiVersion(version);
            context.getWorkspace().config().setBootConfig(c);
            cmdLine.unexpectedArgument("config set workspace version");

        } else if (cmdLine.readAll("set workspace runtime-version", "set workspace runtime-id")) {
            String version = cmdLine.readRequiredNonOption(new DefaultNonOption("version")).getStringExpression();
            NutsBootConfig c = context.getWorkspace().config().getBootConfig();
            if (version.contains("#")) {
                c.setRuntimeId(NutsConstants.Ids.NUTS_RUNTIME + "#" + version);
            } else {
                c.setRuntimeId(version);
            }
            context.getWorkspace().config().setBootConfig(c);
            cmdLine.unexpectedArgument("config set workspace version");

        } else if (cmdLine.readAll("get workspace version", "gwv")) {
            cmdLine.unexpectedArgument("config get workspace version");
            NutsBootConfig c = context.getWorkspace().config().getBootConfig();
            context.out().printf("boot-version  : %s%n", StringUtils.trim(c.getApiVersion()));
            context.out().printf("runtime-id    : %s%n", StringUtils.trim(c.getRuntimeId()));
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
                    archetype = cmdLine.readRequiredNonOption(new ArchitectureNonOption("Archetype", context.getWorkspace())).getStringOrError();
                } else if (cmdLine.readAllOnce("-u", "--login")) {
                    login = cmdLine.readRequiredNonOption(new DefaultNonOption("Username")).getStringOrError();
                } else if (cmdLine.readAllOnce("-x", "--password")) {
                    password = cmdLine.readRequiredNonOption(new DefaultNonOption("Password")).getStringOrError();
                } else {
                    String ws = cmdLine.readRequiredNonOption(new FolderNonOption("WorkspacePath")).getStringExpression();
                    wsCount++;
                    cmdLine.unexpectedArgument("config set workspace");
                    processed = true;
                    if (cmdLine.isExecMode()) {
                        NutsWorkspace workspace = context.getWorkspace().openWorkspace(
                                new NutsWorkspaceOptions()
                                        .setWorkspace(ws)
                                        .setArchetype(archetype)
                                        .setReadOnly(!save)
                                        .setOpenMode(createIfNotFound ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : NutsWorkspaceOpenMode.OPEN_EXISTING)
                        );
                        if (!StringUtils.isEmpty(login)) {
                            workspace.security().login(login, password);
                        }
                        //TODO Unsupported set workspace
                        context.out().print("Unsupported set workspace....");
                        //context.consoleContext().setWorkspace(workspace);
                        trySave(context, workspace, null, autoSave, cmdLine);
                    }
                }
            }
            cmdLine.unexpectedArgument("config set workspace");
            if (cmdLine.isExecMode()) {
                if (!processed) {
                    throw new NutsExecutionException("incorrect command : create workspace", 2);
                }
            }
            return true;
        } else if (cmdLine.readAll("save workspace", "save", "sw")) {
            cmdLine.unexpectedArgument("config save workspace");
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
