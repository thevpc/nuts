/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.cmd.config;

import java.util.Arrays;
import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsElementNotFoundException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsUserInfo;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.extensions.cmd.AbstractConfigSubCommand;
import net.vpc.app.nuts.extensions.cmd.ConfigCommand;
import net.vpc.app.nuts.extensions.cmd.cmdline.GroupNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.RepositoryNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.RightNonOption;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.DefaultNonOption;

/**
 *
 * @author vpc
 */
public class UserConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, net.vpc.app.nuts.extensions.cmd.ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        return exec(null, cmdLine, config, autoSave, context);
    }

    public static boolean exec(NutsRepository editedRepo, CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        NutsWorkspace workspace = context.getValidWorkspace();
        if (cmdLine.read("add user", "au")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
                    repository = workspace.getRepositoryManager().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", workspace)).getString());
                }
            }
            if (repository == null) {
                String user = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getString();
                String password = cmdLine.readNonOption(new DefaultNonOption("Password")).getString();
                if (cmdLine.isExecMode()) {
                    workspace.getSecurityManager().addUser(user, password);
                }
            } else {
                String user = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getString();
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
        } else if (cmdLine.read("list users", "lu")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
                    repository = workspace.getRepositoryManager().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("Repository", workspace)).getString());
                }
            }
            if (cmdLine.isExecMode()) {
                NutsUserInfo[] security;
                if (repository == null) {
                    security = workspace.getSecurityManager().findUsers();
                } else {
                    security = repository.getSecurityManager().findUsers();
                }
                for (NutsUserInfo u : security) {
                    context.getTerminal().getOut().printf("User: %s\n", u.getUser());
                    if (!CoreStringUtils.isEmpty(u.getMappedUser())) {
                        context.getTerminal().getOut().printf("   Mapper to  : %s\n", u.getMappedUser());
                    }
                    context.getTerminal().getOut().printf("   Password   : %s\n", (u.hasCredentials() ? "Set" : "None"));
                    context.getTerminal().getOut().printf("   Groups     : %s\n", (u.getGroups().length == 0 ? "None" : Arrays.asList(u.getGroups())));
                    context.getTerminal().getOut().printf("   Rights     : %s\n", (u.getRights().length == 0 ? "None" : Arrays.asList(u.getRights())));
                }
            }
            return true;

        } else if (cmdLine.read("password", "passwd", "pwd")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
                    repository = workspace.getRepositoryManager().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", workspace)).getString());
                }
            }

            String user = null;
            String password = null;
            String oldPassword = null;
            do {
                if (cmdLine.read("--user")) {
                    user = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getString();
                } else if (cmdLine.read("--password")) {
                    password = cmdLine.readNonOptionOrError(new DefaultNonOption("Password")).getString();
                } else if (cmdLine.read("--old-password")) {
                    oldPassword = cmdLine.readNonOptionOrError(new DefaultNonOption("OldPassword")).getString();
                } else {
                    cmdLine.requireEmpty();
                }
            } while (!cmdLine.isEmpty());
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

        } else if (cmdLine.read("edit user", "eu")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
                    repository = workspace.getRepositoryManager().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", workspace)).getString());
                }
            }

            String user = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getString();
            if (cmdLine.isExecMode()) {
                NutsUserInfo u = null;
                if (repository == null) {
                    u = workspace.getSecurityManager().findUser(user);
                } else {
                    u = repository.getSecurityManager().findUser(user);
                }
                if (u == null) {
                    throw new NutsElementNotFoundException("No such user " + user);
                }
            }
//            NutsSecurityEntityConfig u = null;
//            if (repository == null) {
//                u = context.getValidWorkspace().getConfig().getSecurity(user);
//            } else {
//                u = repository.getConfig().getSecurity(user);
//            }
//            if (u == null) {
//                if (cmdLine.isExecMode()) {
//                    throw new NutsIllegalArgumentsException("No such user " + user);
//                }
//            }
            String lastOption = "";
            while (!cmdLine.isEmpty()) {
                if (cmdLine.readOnce("--add-group")) {
                    lastOption = "--add-group";
                } else if (cmdLine.readOnce("--remove-group")) {
                    lastOption = "--remove-group";
                } else if (cmdLine.readOnce("--add-right")) {
                    lastOption = "--add-right";
                } else if (cmdLine.readOnce("--remove-right")) {
                    lastOption = "--remove-right";
                } else if (cmdLine.readOnce("--mapped-user")) {
                    lastOption = "--mapped-user";
                } else if (cmdLine.readOnce("--password")) {
                    lastOption = "--password";
                } else {
                    switch (lastOption) {
                        case "--add-group": {
                            String a = cmdLine.readNonOptionOrError(new DefaultNonOption("Group")).getString();
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
                            String a = cmdLine.readNonOptionOrError(new GroupNonOption("Group", context, repository)).getString();
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
                            String a = cmdLine.readNonOptionOrError(new RightNonOption("Right", workspace, repository, user, false)).getString();
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
                            String a = cmdLine.readNonOptionOrError(new RightNonOption("Right", workspace, repository, user, true)).getString();
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
                            String a = cmdLine.readNonOptionOrError(new DefaultNonOption("MappedUser")).getString();
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
                            String pwd = (cmdLine.readNonOptionOrError(new DefaultNonOption("Password")).getString());
                            String old = (cmdLine.readNonOptionOrError(new DefaultNonOption("OldPassword")).getString());
                            if (cmdLine.isExecMode()) {
                                if (repository != null) {
                                    repository.getSecurityManager().setUserCredentials(user, pwd, old);
                                } else {
                                    workspace.getSecurityManager().setUserCredentials(user, pwd, old);
                                }
                            }
                            break;
                        default:
                            cmdLine.requireEmpty();
                            break;
                    }
                }
            }
            if (cmdLine.isExecMode()) {
                ConfigCommand.trySave(context, workspace, repository, autoSave, null);
            }
            return true;

        } else if (cmdLine.read("unsecure")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
                    repository = workspace.getRepositoryManager().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", workspace)).getString());
                }
            }
            //unsecure-box
            if (cmdLine.isExecMode()) {
                String adminPassword = null;
                if (!context.getWorkspace().getSecurityManager().isAdmin()) {
                    adminPassword = context.getTerminal().readPassword("Enter password : ");
                }
                if (workspace.getSecurityManager().switchUnsecureMode(adminPassword)) {
                    context.getTerminal().getOut().println("<<unsecure box activated.Anonymous has all rights.>>");
                } else {
                    context.getTerminal().getOut().println("<<unsecure box is already activated.>>");
                }
            }
            ConfigCommand.trySave(context, workspace, repository, autoSave, cmdLine);
            return true;
        } else if (cmdLine.read("secure")) {
            String adminPassword = null;
            if (!context.getWorkspace().getSecurityManager().isAdmin()) {
                adminPassword = context.getTerminal().readPassword("Enter password : ");
            }
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
                    repository = workspace.getRepositoryManager().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", workspace)).getString());
                }
            }
            //secure-box
            if (cmdLine.isExecMode()) {
                if (workspace.getSecurityManager().switchSecureMode(adminPassword)) {
                    context.getTerminal().getOut().println("\"\"secure box activated.\"\"");
                } else {
                    context.getTerminal().getOut().println("\"\"secure box already activated.\"\"");
                }
            }
            ConfigCommand.trySave(context, workspace, repository, autoSave, cmdLine);
            return true;
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

}
