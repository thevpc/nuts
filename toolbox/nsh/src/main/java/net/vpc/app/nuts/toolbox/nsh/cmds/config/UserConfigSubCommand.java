/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nsh.cmds.config;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.NutsConsoleContext;
import net.vpc.app.nuts.toolbox.nsh.cmds.ConfigCommand;
import net.vpc.app.nuts.toolbox.nsh.options.GroupNonOption;
import net.vpc.app.nuts.toolbox.nsh.options.RepositoryNonOption;
import net.vpc.app.nuts.toolbox.nsh.options.RightNonOption;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.DefaultNonOption;
import net.vpc.common.strings.StringUtils;

import java.io.PrintStream;
import java.util.Arrays;

/**
 *
 * @author vpc
 */
public class UserConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        return exec(null, cmdLine, config, autoSave, context);
    }

    public static boolean exec(NutsRepository editedRepo, CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        NutsWorkspace workspace = context.getWorkspace();
        if (cmdLine.readAll("add user", "au")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.readAll("--repo", "-r")) {
                    repository = workspace.getRepositoryManager().findRepository(cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryId", workspace)).getString());
                }
            }
            if (repository == null) {
                String user = cmdLine.readRequiredNonOption(new DefaultNonOption("Username")).getString();
                String password = cmdLine.readNonOption(new DefaultNonOption("Password")).getString();
                if (cmdLine.isExecMode()) {
                    workspace.getSecurityManager().addUser(user, password);
                }
            } else {
                String user = cmdLine.readRequiredNonOption(new DefaultNonOption("Username")).getString();
                String mappedUser = cmdLine.readNonOption(new DefaultNonOption("MappedUser")).getString();
                String password = cmdLine.readNonOption(new DefaultNonOption("Password")).getString();
                if (cmdLine.isExecMode()) {
                    repository.getSecurityManager().addUser(user, password);
                    repository.getSecurityManager().setUserRemoteIdentity(user, mappedUser);
                }
            }
            if (cmdLine.isExecMode()) {
                ConfigCommand.trySave(context, workspace, repository, autoSave, null);
            }
            return true;
        } else {
            PrintStream out = context.getTerminal().getFormattedOut();
            if (cmdLine.readAll("list users", "lu")) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.readAll("--repo", "-r")) {
                        repository = workspace.getRepositoryManager().findRepository(cmdLine.readRequiredNonOption(new RepositoryNonOption("Repository", workspace)).getString());
                    }
                }
                if (cmdLine.isExecMode()) {
                    NutsEffectiveUser[] security;
                    if (repository == null) {
                        security = workspace.getSecurityManager().findUsers();
                    } else {
                        security = repository.getSecurityManager().findUsers();
                    }
                    for (NutsEffectiveUser u : security) {
                        out.printf("User: %s\n", u.getUser());
                        if (!StringUtils.isEmpty(u.getMappedUser())) {
                            out.printf("   Mapper to  : %s\n", u.getMappedUser());
                        }
                        out.printf("   Password   : %s\n", (u.hasCredentials() ? "Set" : "None"));
                        out.printf("   Groups     : %s\n", (u.getGroups().length == 0 ? "None" : Arrays.asList(u.getGroups())));
                        out.printf("   Rights     : %s\n", (u.getRights().length == 0 ? "None" : Arrays.asList(u.getRights())));
                    }
                }
                return true;

            } else if (cmdLine.readAll("password", "passwd", "pwd")) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.readAll("--repo", "-r")) {
                        repository = workspace.getRepositoryManager().findRepository(cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryId", workspace)).getString());
                    }
                }

                String user = null;
                String password = null;
                String oldPassword = null;
                do {
                    if (cmdLine.readAll("--user")) {
                        user = cmdLine.readRequiredNonOption(new DefaultNonOption("Username")).getString();
                    } else if (cmdLine.readAll("--password")) {
                        password = cmdLine.readRequiredNonOption(new DefaultNonOption("Password")).getString();
                    } else if (cmdLine.readAll("--old-password")) {
                        oldPassword = cmdLine.readRequiredNonOption(new DefaultNonOption("OldPassword")).getString();
                    } else {
                        cmdLine.unexpectedArgument("config password");
                    }
                } while (cmdLine.hasNext());
                if (cmdLine.isExecMode()) {
                    boolean admin;
                    if (repository == null) {
                        admin = workspace.getSecurityManager().isAllowed(NutsConstants.RIGHT_ADMIN);
                    } else {
                        admin = repository.getSecurityManager().isAllowed(NutsConstants.RIGHT_ADMIN);
                    }

                    if (oldPassword == null && !admin) {
                        oldPassword = context.getTerminal().readPassword("Old Password:");
                    }
                    if (password == null) {
                        password = context.getTerminal().readPassword("Password:");
                    }

                    if (repository == null) {
                        workspace.getSecurityManager().setUserCredentials(user, password, oldPassword);
                    } else {
                        repository.getSecurityManager().setUserCredentials(user, password, oldPassword);
                    }
                    ConfigCommand.trySave(context, workspace, repository, autoSave, null);
                }
                return true;

            } else if (cmdLine.readAll("edit user", "eu")) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.readAll("--repo", "-r")) {
                        repository = workspace.getRepositoryManager().findRepository(cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryId", workspace)).getString());
                    }
                }

                String user = cmdLine.readRequiredNonOption(new DefaultNonOption("Username")).getString();
                if (cmdLine.isExecMode()) {
                    NutsEffectiveUser u = null;
                    if (repository == null) {
                        u = workspace.getSecurityManager().findUser(user);
                    } else {
                        u = repository.getSecurityManager().getEffectiveUser(user);
                    }
                    if (u == null) {
                        throw new NutsElementNotFoundException("No such user " + user);
                    }
                }
    //            NutsUserConfig u = null;
    //            if (repository == null) {
    //                u = context.getWorkspace().getConfig().getUser(user);
    //            } else {
    //                u = repository.getConfig().getUser(user);
    //            }
    //            if (u == null) {
    //                if (cmdLine.isExecMode()) {
    //                    throw new NutsIllegalArgumentsException("No such user " + user);
    //                }
    //            }
                String lastOption = "";
                while (cmdLine.hasNext()) {
                    if (cmdLine.readAllOnce("--add-group")) {
                        lastOption = "--add-group";
                    } else if (cmdLine.readAllOnce("--remove-group")) {
                        lastOption = "--remove-group";
                    } else if (cmdLine.readAllOnce("--add-right")) {
                        lastOption = "--add-right";
                    } else if (cmdLine.readAllOnce("--remove-right")) {
                        lastOption = "--remove-right";
                    } else if (cmdLine.readAllOnce("--mapped-user")) {
                        lastOption = "--mapped-user";
                    } else if (cmdLine.readAllOnce("--password")) {
                        lastOption = "--password";
                    } else {
                        switch (lastOption) {
                            case "--add-group": {
                                String a = cmdLine.readRequiredNonOption(new DefaultNonOption("Group")).getString();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.getSecurityManager().addUserGroups(user, a);
                                    } else {
                                        workspace.getSecurityManager().addUserGroups(user, a);
                                    }
                                }
                                break;
                            }
                            case "--remove-group": {
                                String a = cmdLine.readRequiredNonOption(new GroupNonOption("Group", context.consoleContext(), repository)).getString();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.getSecurityManager().removeUserGroups(user, a);
                                    } else {
                                        workspace.getSecurityManager().removeUserGroups(user, a);
                                    }
                                }
                                break;
                            }
                            case "--add-right": {
                                String a = cmdLine.readRequiredNonOption(new RightNonOption("Right", workspace, repository, user, false)).getString();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.getSecurityManager().addUserRights(user, a);
                                    } else {
                                        workspace.getSecurityManager().addUserRights(user, a);
                                    }
                                }
                                break;
                            }
                            case "--remove-right": {
                                String a = cmdLine.readRequiredNonOption(new RightNonOption("Right", workspace, repository, user, true)).getString();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.getSecurityManager().removeUserRights(user, a);
                                    } else {
                                        workspace.getSecurityManager().removeUserRights(user, a);
                                    }
                                }
                                break;
                            }
                            case "--mapped-user": {
                                String a = cmdLine.readRequiredNonOption(new DefaultNonOption("MappedUser")).getString();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.getSecurityManager().setUserRemoteIdentity(user, a);
                                    } else {
                                        workspace.getSecurityManager().setUserRemoteIdentity(user, a);
                                    }
                                }
                                break;
                            }
                            case "--password":
                                String pwd = (cmdLine.readRequiredNonOption(new DefaultNonOption("Password")).getString());
                                String old = (cmdLine.readRequiredNonOption(new DefaultNonOption("OldPassword")).getString());
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.getSecurityManager().setUserCredentials(user, pwd, old);
                                    } else {
                                        workspace.getSecurityManager().setUserCredentials(user, pwd, old);
                                    }
                                }
                                break;
                            default:
                                cmdLine.unexpectedArgument("config edit user");
                                break;
                        }
                    }
                }
                if (cmdLine.isExecMode()) {
                    ConfigCommand.trySave(context, workspace, repository, autoSave, null);
                }
                return true;

            } else if (cmdLine.readAll("unsecure")) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.readAll("--repo", "-r")) {
                        repository = workspace.getRepositoryManager().findRepository(cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryId", workspace)).getString());
                    }
                }
                //unsecure-box
                if (cmdLine.isExecMode()) {
                    String adminPassword = null;
                    if (!context.getWorkspace().getSecurityManager().isAdmin()) {
                        adminPassword = context.getTerminal().readPassword("Enter password : ");
                    }
                    if (workspace.getSecurityManager().switchUnsecureMode(adminPassword)) {
                        out.println("<<unsecure box activated.Anonymous has all rights.>>");
                    } else {
                        out.println("<<unsecure box is already activated.>>");
                    }
                }
                ConfigCommand.trySave(context, workspace, repository, autoSave, cmdLine);
                return true;
            } else if (cmdLine.readAll("secure")) {
                String adminPassword = null;
                if (!context.getWorkspace().getSecurityManager().isAdmin()) {
                    adminPassword = context.getTerminal().readPassword("Enter password : ");
                }
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.readAll("--repo", "-r")) {
                        repository = workspace.getRepositoryManager().findRepository(cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryId", workspace)).getString());
                    }
                }
                //secure-box
                if (cmdLine.isExecMode()) {
                    if (workspace.getSecurityManager().switchSecureMode(adminPassword)) {
                        out.println("\"\"secure box activated.\"\"");
                    } else {
                        out.println("\"\"secure box already activated.\"\"");
                    }
                }
                ConfigCommand.trySave(context, workspace, repository, autoSave, cmdLine);
                return true;
            }
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

}
