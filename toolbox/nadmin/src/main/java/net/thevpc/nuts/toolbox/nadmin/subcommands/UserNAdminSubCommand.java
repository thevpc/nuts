/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nadmin.subcommands;

import net.thevpc.nuts.*;
import net.thevpc.common.strings.StringUtils;

import java.io.PrintStream;
import java.util.Arrays;

/**
 *
 * @author thevpc
 */
public class UserNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsApplicationContext context) {
        return exec(null, cmdLine, autoSave, context);
    }

    public static boolean exec(NutsRepository editedRepo, NutsCommandLine cmdLine, Boolean autoSave, NutsApplicationContext context) {
        NutsWorkspace workspace = context.getWorkspace();
        NutsSession session = context.getSession();
        NutsCommandLineManager commandLineFormat = context.getWorkspace().commandLine();
        if (cmdLine.next("add user", "au") != null) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.next("--repo", "-r") != null) {
                    repository = workspace.repos().getRepository(cmdLine.required().nextNonOption(commandLineFormat.createName("RepositoryId")).getString());
                }
            }
            if (repository == null) {
                String user = cmdLine.required().nextNonOption(commandLineFormat.createName("Username")).getString();
                char[] password = cmdLine.nextNonOption(commandLineFormat.createName("Password")).getString().toCharArray();
                if (cmdLine.isExecMode()) {
                    workspace.security().addUser(user).setCredentials(password).run();
                }
            } else {
                String user = cmdLine.required().nextNonOption(commandLineFormat.createName("Username")).getString();
                char[] password = cmdLine.nextNonOption(commandLineFormat.createName("Password")).getString().toCharArray();
                String mappedUser=null;
                char[] remotePassword=null;
                if(!cmdLine.isEmpty()) {
                    mappedUser = cmdLine.nextNonOption(commandLineFormat.createName("RemoteId")).getString();
                    remotePassword = cmdLine.nextNonOption(commandLineFormat.createName("RemotePassword")).getString().toCharArray();
                }
                if (cmdLine.isExecMode()) {
                    repository.security().addUser(user).setCredentials(password).setRemoteIdentity(mappedUser).setRemoteCredentials(remotePassword).run();
                }
            }
            if (cmdLine.isExecMode()) {
                context.getWorkspace().config().save();
            }
            return true;
        } else {
            PrintStream out = session.out();
            if (cmdLine.next("list users", "lu") != null) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r") != null) {
                        repository = workspace.repos().getRepository(cmdLine.required().nextNonOption(commandLineFormat.createName("repository")).getString());
                    }
                }
                if (cmdLine.isExecMode()) {
                    NutsUser[] security;
                    if (repository == null) {
                        security = workspace.security().findUsers();
                    } else {
                        security = repository.security().findUsers();
                    }
                    for (NutsUser u : security) {
                        out.printf("User: %s%n", u.getUser());
                        if (!StringUtils.isBlank(u.getRemoteIdentity())) {
                            out.printf("   Mapper to  : %s%n", u.getRemoteIdentity());
                        }
                        out.printf("   Password   : %s%n", (u.hasCredentials() ? "Set" : "None"));
                        out.printf("   Groups     : %s%n", (u.getGroups().length == 0 ? "None" : Arrays.asList(u.getGroups())));
                        out.printf("   Rights     : %s%n", (u.getPermissions().length == 0 ? "None" : Arrays.asList(u.getPermissions())));
                    }
                }
                return true;

            } else if (cmdLine.next("password", "passwd", "pwd") != null) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r") != null) {
                        repository = workspace.repos().getRepository(cmdLine.required().nextNonOption(commandLineFormat.createName("RepositoryId")).getString());
                    }
                }

                String user = null;
                char[] password = null;
                char[] oldPassword = null;
                do {
                    if (cmdLine.next("--user") != null) {
                        user = cmdLine.required().nextNonOption(commandLineFormat.createName("Username")).getString();
                    } else if (cmdLine.next("--password") != null) {
                        password = cmdLine.required().nextNonOption(commandLineFormat.createName("Password")).getString().toCharArray();
                    } else if (cmdLine.next("--old-password") != null) {
                        oldPassword = cmdLine.required().nextNonOption(commandLineFormat.createName("OldPassword")).getString().toCharArray();
                    } else {
                        cmdLine.setCommandName("config password").unexpectedArgument();
                    }
                } while (cmdLine.hasNext());
                if (cmdLine.isExecMode()) {
                    boolean admin;
                    if (repository == null) {
                        admin = workspace.security().isAllowed(NutsConstants.Permissions.ADMIN);
                    } else {
                        admin = repository.security().isAllowed(NutsConstants.Permissions.ADMIN);
                    }

                    if (oldPassword == null && !admin) {
                        oldPassword = session.getTerminal().readPassword("Old Password:");
                    }
                    if (password == null) {
                        password = session.getTerminal().readPassword("Password:");
                    }

                    if (repository == null) {
                        workspace.security().updateUser(user).setCredentials(password).setOldCredentials(oldPassword).run();
                    } else {
                        repository.security().updateUser(user).setCredentials(password).setOldCredentials(oldPassword).run();
                    }
                    context.getWorkspace().config().save();
                }
                return true;

            } else if (cmdLine.next("edit user", "eu") != null) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r") != null) {
                        repository = workspace.repos().getRepository(cmdLine.required().nextNonOption(commandLineFormat.createName("RepositoryId")).getString());
                    }
                }

                String user = cmdLine.required().nextNonOption(commandLineFormat.createName("Username")).getString();
                if (cmdLine.isExecMode()) {
                    NutsUser u = null;
                    if (repository == null) {
                        u = workspace.security().findUser(user);
                    } else {
                        u = repository.security().getEffectiveUser(user);
                    }
                    if (u == null) {
                        throw new NutsElementNotFoundException(session, "No such user " + user);
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
                    if (cmdLine.next("--add-group") != null) {
                        lastOption = "--add-group";
                    } else if (cmdLine.next("--remove-group") != null) {
                        lastOption = "--remove-group";
                    } else if (cmdLine.next("--add-right") != null) {
                        lastOption = "--add-right";
                    } else if (cmdLine.next("--remove-right") != null) {
                        lastOption = "--remove-right";
                    } else if (cmdLine.next("--mapped-user") != null) {
                        lastOption = "--mapped-user";
                    } else if (cmdLine.next("--password") != null) {
                        lastOption = "--password";
                    } else {
                        switch (lastOption) {
                            case "--add-group": {
                                String a = cmdLine.required().nextNonOption(commandLineFormat.createName("Group")).getString();
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
                                String a = cmdLine.required().nextNonOption(commandLineFormat.createName("Group")).getString();
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
                                String a = cmdLine.required().nextNonOption(commandLineFormat.createName("Right")).getString();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).addPermission(a).run();
                                    } else {
                                        workspace.security().updateUser(user).addPermission(a).run();
                                    }
                                }
                                break;
                            }
                            case "--remove-right": {
                                String a = cmdLine.required().nextNonOption(commandLineFormat.createName("Right")).getString();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).removePermission(a).run();
                                    } else {
                                        workspace.security().updateUser(user).removePermission(a).run();
                                    }
                                }
                                break;
                            }
                            case "--mapped-user": {
                                String a = cmdLine.required().nextNonOption(commandLineFormat.createName("RemoteIdentity")).getString();
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
                                char[] pwd = (cmdLine.required().nextNonOption(commandLineFormat.createName("password", "Password")).getString()).toCharArray();
                                char[] old = (cmdLine.required().nextNonOption(commandLineFormat.createName("password", "OldPassword")).getString()).toCharArray();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).setCredentials(pwd).setOldCredentials(old).run();
                                    } else {
                                        workspace.security().updateUser(user).setCredentials(pwd).setOldCredentials(old).run();
                                    }
                                }
                                Arrays.fill(pwd, '\0');
                                Arrays.fill(old, '\0');
                                break;
                            default:
                                cmdLine.setCommandName("config edit user").unexpectedArgument();
                                break;
                        }
                    }
                }
                if (cmdLine.isExecMode()) {
                    context.getWorkspace().config().save();
                }
                return true;

            } else if (cmdLine.next("unsecure") != null) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r") != null) {
                        repository = workspace.repos().getRepository(cmdLine.required().nextNonOption(commandLineFormat.createName("RepositoryId")).getString());
                    }
                }
                //unsecure-box
                if (cmdLine.isExecMode()) {
                    char[] credentials = null;
                    if (!context.getWorkspace().security().isAdmin()) {
                        credentials = session.getTerminal().readPassword("Enter password : ");
                    }
                    if (workspace.security().setSecureMode(false,credentials)) {
                        out.println("<<unsecure box activated.Anonymous has all rights.>>");
                    } else {
                        out.println("<<unsecure box is already activated.>>");
                    }
                    if (credentials != null) {
                        Arrays.fill(credentials, '\0');
                    }
                }
                context.getWorkspace().config().save();
                return true;
            } else if (cmdLine.next("secure") != null) {
                char[] credentials = null;
                if (!context.getWorkspace().security().isAdmin()) {
                    credentials = session.getTerminal().readPassword("Enter password : ");
                }
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r") != null) {
                        repository = workspace.repos().getRepository(cmdLine.required().nextNonOption(commandLineFormat.createName("RepositoryId")).getString());
                    }
                }
                //secure-box
                if (cmdLine.isExecMode()) {
                    if (workspace.security().setSecureMode(true,credentials)) {
                        out.println("\"\"secure box activated.\"\"");
                    } else {
                        out.println("\"\"secure box already activated.\"\"");
                    }
                }
                if (credentials != null) {
                    Arrays.fill(credentials, '\0');
                }
                context.getWorkspace().config().save();
                return true;
            }
        }
        return false;
    }


}
