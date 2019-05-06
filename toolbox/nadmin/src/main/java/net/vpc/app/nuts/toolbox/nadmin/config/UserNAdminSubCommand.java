/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;
import net.vpc.app.nuts.app.options.GroupNonOption;
import net.vpc.app.nuts.app.options.RepositoryNonOption;
import net.vpc.app.nuts.app.options.RightNonOption;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.DefaultNonOption;
import net.vpc.common.strings.StringUtils;

import java.io.PrintStream;
import java.util.Arrays;

/**
 *
 * @author vpc
 */
public class UserNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        return exec(null, cmdLine, config, autoSave, context);
    }

    public static boolean exec(NutsRepository editedRepo, CommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext
            context) {
        NutsWorkspace workspace = context.getWorkspace();
        if (cmdLine.readAll("add user", "au")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.readAll("--repo", "-r")) {
                    repository = workspace.config().getRepository(cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryId", workspace)).getStringExpression());
                }
            }
            if (repository == null) {
                String user = cmdLine.readRequiredNonOption(new DefaultNonOption("Username")).getStringExpression();
                String password = cmdLine.readNonOption(new DefaultNonOption("Password")).getStringExpression();
                if (cmdLine.isExecMode()) {
                    workspace.security().addUser(user).credentials(password).run();
                }
            } else {
                String user = cmdLine.readRequiredNonOption(new DefaultNonOption("Username")).getStringExpression();
                String mappedUser = cmdLine.readNonOption(new DefaultNonOption("MappedUser")).getStringExpression();
                String password = cmdLine.readNonOption(new DefaultNonOption("Password")).getStringExpression();
                if (cmdLine.isExecMode()) {
                    repository.security().addUser(user).credentials(password).remoteIdentity(mappedUser).run();
                }
            }
            if (cmdLine.isExecMode()) {
                trySave(context, workspace, repository, autoSave, null);
            }
            return true;
        } else {
            PrintStream out = context.getTerminal().fout();
            if (cmdLine.readAll("list users", "lu")) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.readAll("--repo", "-r")) {
                        repository = workspace.config().getRepository(cmdLine.readRequiredNonOption(new RepositoryNonOption("Repository", workspace)).getStringExpression());
                    }
                }
                if (cmdLine.isExecMode()) {
                    NutsEffectiveUser[] security;
                    if (repository == null) {
                        security = workspace.security().findUsers();
                    } else {
                        security = repository.security().findUsers();
                    }
                    for (NutsEffectiveUser u : security) {
                        out.printf("User: %s%n", u.getUser());
                        if (!StringUtils.isEmpty(u.getMappedUser())) {
                            out.printf("   Mapper to  : %s%n", u.getMappedUser());
                        }
                        out.printf("   Password   : %s%n", (u.hasCredentials() ? "Set" : "None"));
                        out.printf("   Groups     : %s%n", (u.getGroups().length == 0 ? "None" : Arrays.asList(u.getGroups())));
                        out.printf("   Rights     : %s%n", (u.getRights().length == 0 ? "None" : Arrays.asList(u.getRights())));
                    }
                }
                return true;

            } else if (cmdLine.readAll("password", "passwd", "pwd")) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.readAll("--repo", "-r")) {
                        repository = workspace.config().getRepository(cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryId", workspace)).getStringExpression());
                    }
                }

                String user = null;
                String password = null;
                String oldPassword = null;
                do {
                    if (cmdLine.readAll("--user")) {
                        user = cmdLine.readRequiredNonOption(new DefaultNonOption("Username")).getStringExpression();
                    } else if (cmdLine.readAll("--password")) {
                        password = cmdLine.readRequiredNonOption(new DefaultNonOption("Password")).getStringExpression();
                    } else if (cmdLine.readAll("--old-password")) {
                        oldPassword = cmdLine.readRequiredNonOption(new DefaultNonOption("OldPassword")).getStringExpression();
                    } else {
                        cmdLine.unexpectedArgument("config password");
                    }
                } while (cmdLine.hasNext());
                if (cmdLine.isExecMode()) {
                    boolean admin;
                    if (repository == null) {
                        admin = workspace.security().isAllowed(NutsConstants.Rights.ADMIN);
                    } else {
                        admin = repository.security().isAllowed(NutsConstants.Rights.ADMIN);
                    }

                    if (oldPassword == null && !admin) {
                        oldPassword = context.getTerminal().readPassword("Old Password:");
                    }
                    if (password == null) {
                        password = context.getTerminal().readPassword("Password:");
                    }

                    if (repository == null) {
                        workspace.security().updateUser(user).setCredentials(password).setOldCredentials(oldPassword).run();
                    } else {
                        repository.security().updateUser(user).setCredentials(password).setOldCredentials(oldPassword).run();
                    }
                    trySave(context, workspace, repository, autoSave, null);
                }
                return true;

            } else if (cmdLine.readAll("edit user", "eu")) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.readAll("--repo", "-r")) {
                        repository = workspace.config().getRepository(cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryId", workspace)).getStringExpression());
                    }
                }

                String user = cmdLine.readRequiredNonOption(new DefaultNonOption("Username")).getStringExpression();
                if (cmdLine.isExecMode()) {
                    NutsEffectiveUser u = null;
                    if (repository == null) {
                        u = workspace.security().findUser(user);
                    } else {
                        u = repository.security().getEffectiveUser(user);
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
                                String a = cmdLine.readRequiredNonOption(new DefaultNonOption("Group")).getStringExpression();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).addGroup(a).run();
                                    } else {
                                        workspace.security().updateUser(user).addGroup(a).run();
                                    }
                                }
                                break;
                            }
                            case "--remove-group": {
                                String a = cmdLine.readRequiredNonOption(new GroupNonOption("Group", context.getWorkspace(), repository)).getStringExpression();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).removeGroup(a).run();
                                    } else {
                                        workspace.security().updateUser(user).removeGroup(a).run();
                                    }
                                }
                                break;
                            }
                            case "--add-right": {
                                String a = cmdLine.readRequiredNonOption(new RightNonOption("Right", workspace, repository, user, false)).getStringExpression();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).addRight(a).run();
                                    } else {
                                        workspace.security().updateUser(user).addRight(a).run();
                                    }
                                }
                                break;
                            }
                            case "--remove-right": {
                                String a = cmdLine.readRequiredNonOption(new RightNonOption("Right", workspace, repository, user, true)).getStringExpression();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).removeRight(a).run();
                                    } else {
                                        workspace.security().updateUser(user).removeRight(a).run();
                                    }
                                }
                                break;
                            }
                            case "--mapped-user": {
                                String a = cmdLine.readRequiredNonOption(new DefaultNonOption("RemoteIdentity")).getStringExpression();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).setRemoteIdentity(a).run();
                                    } else {
                                        workspace.security().updateUser(user).setRemoteIdentity(a).run();
                                    }
                                }
                                break;
                            }
                            case "--password":
                                String pwd = (cmdLine.readRequiredNonOption(new DefaultNonOption("Password")).getStringExpression());
                                String old = (cmdLine.readRequiredNonOption(new DefaultNonOption("OldPassword")).getStringExpression());
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).setCredentials(pwd).setOldCredentials(old).run();
                                    } else {
                                        workspace.security().updateUser(user).setCredentials(pwd).setOldCredentials(old).run();
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
                    trySave(context, workspace, repository, autoSave, null);
                }
                return true;

            } else if (cmdLine.readAll("unsecure")) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.readAll("--repo", "-r")) {
                        repository = workspace.config().getRepository(cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryId", workspace)).getStringExpression());
                    }
                }
                //unsecure-box
                if (cmdLine.isExecMode()) {
                    String adminPassword = null;
                    if (!context.getWorkspace().security().isAdmin()) {
                        adminPassword = context.getTerminal().readPassword("Enter password : ");
                    }
                    if (workspace.security().switchUnsecureMode(adminPassword)) {
                        out.println("<<unsecure box activated.Anonymous has all rights.>>");
                    } else {
                        out.println("<<unsecure box is already activated.>>");
                    }
                }
                trySave(context, workspace, repository, autoSave, cmdLine);
                return true;
            } else if (cmdLine.readAll("secure")) {
                String adminPassword = null;
                if (!context.getWorkspace().security().isAdmin()) {
                    adminPassword = context.getTerminal().readPassword("Enter password : ");
                }
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.readAll("--repo", "-r")) {
                        repository = workspace.config().getRepository(cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryId", workspace)).getStringExpression());
                    }
                }
                //secure-box
                if (cmdLine.isExecMode()) {
                    if (workspace.security().switchSecureMode(adminPassword)) {
                        out.println("\"\"secure box activated.\"\"");
                    } else {
                        out.println("\"\"secure box already activated.\"\"");
                    }
                }
                trySave(context, workspace, repository, autoSave, cmdLine);
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
