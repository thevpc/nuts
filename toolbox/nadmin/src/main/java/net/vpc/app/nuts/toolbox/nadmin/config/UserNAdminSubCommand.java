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
import java.util.Arrays;

/**
 *
 * @author vpc
 */
public class UserNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommand cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        return exec(null, cmdLine, config, autoSave, context);
    }

    public static boolean exec(NutsRepository editedRepo, NutsCommand cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext
            context) {
        NutsWorkspace workspace = context.getWorkspace();
        if (cmdLine.next("add user", "au")!=null) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.next("--repo", "-r")!=null) {
                    repository = workspace.config().getRepository(cmdLine.required().nextNonOption(cmdLine.createNonOption("RepositoryId")).getString());
                }
            }
            if (repository == null) {
                String user = cmdLine.required().nextNonOption(cmdLine.createNonOption("Username")).getString();
                char[] password = cmdLine.nextNonOption(cmdLine.createNonOption("Password")).getString().toCharArray();
                if (cmdLine.isExecMode()) {
                    workspace.security().addUser(user).credentials(password).run();
                }
            } else {
                String user = cmdLine.required().nextNonOption(cmdLine.createNonOption("Username")).getString();
                String mappedUser = cmdLine.nextNonOption(cmdLine.createNonOption("MappedUser")).getString();
                char[] password = cmdLine.nextNonOption(cmdLine.createNonOption("Password")).getString().toCharArray();
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
            if (cmdLine.next("list users", "lu")!=null) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r")!=null) {
                        repository = workspace.config().getRepository(cmdLine.required().nextNonOption(cmdLine.createNonOption("repository")).getString());
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

            } else if (cmdLine.next("password", "passwd", "pwd")!=null) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r")!=null) {
                        repository = workspace.config().getRepository(cmdLine.required().nextNonOption(cmdLine.createNonOption("RepositoryId")).getString());
                    }
                }

                String user = null;
                char[] password = null;
                char[] oldPassword = null;
                do {
                    if (cmdLine.next("--user")!=null) {
                        user = cmdLine.required().nextNonOption(cmdLine.createNonOption("Username")).getString();
                    } else if (cmdLine.next("--password")!=null) {
                        password = cmdLine.required().nextNonOption(cmdLine.createNonOption("Password")).getString().toCharArray();
                    } else if (cmdLine.next("--old-password")!=null) {
                        oldPassword = cmdLine.required().nextNonOption(cmdLine.createNonOption("OldPassword")).getString().toCharArray();
                    } else {
                        cmdLine.setCommandName("config password").unexpectedArgument();
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

            } else if (cmdLine.next("edit user", "eu")!=null) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r")!=null) {
                        repository = workspace.config().getRepository(cmdLine.required().nextNonOption(cmdLine.createNonOption("RepositoryId")).getString());
                    }
                }

                String user = cmdLine.required().nextNonOption(cmdLine.createNonOption("Username")).getString();
                if (cmdLine.isExecMode()) {
                    NutsEffectiveUser u = null;
                    if (repository == null) {
                        u = workspace.security().findUser(user);
                    } else {
                        u = repository.security().getEffectiveUser(user);
                    }
                    if (u == null) {
                        throw new NutsElementNotFoundException(workspace, "No such user " + user);
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
                    if (cmdLine.next("--add-group")!=null) {
                        lastOption = "--add-group";
                    } else if (cmdLine.next("--remove-group")!=null) {
                        lastOption = "--remove-group";
                    } else if (cmdLine.next("--add-right")!=null) {
                        lastOption = "--add-right";
                    } else if (cmdLine.next("--remove-right")!=null) {
                        lastOption = "--remove-right";
                    } else if (cmdLine.next("--mapped-user")!=null) {
                        lastOption = "--mapped-user";
                    } else if (cmdLine.next("--password")!=null) {
                        lastOption = "--password";
                    } else {
                        switch (lastOption) {
                            case "--add-group": {
                                String a = cmdLine.required().nextNonOption(cmdLine.createNonOption("Group")).getString();
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
                                String a = cmdLine.required().nextNonOption(cmdLine.createNonOption("Group")).getString();
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
                                String a = cmdLine.required().nextNonOption(cmdLine.createNonOption("Right")).getString();
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
                                String a = cmdLine.required().nextNonOption(cmdLine.createNonOption("Right")).getString();
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
                                String a = cmdLine.required().nextNonOption(cmdLine.createNonOption("RemoteIdentity")).getString();
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
                                char[] pwd = (cmdLine.required().nextNonOption(cmdLine.createNonOption("password","Password")).getString()).toCharArray();
                                char[] old = (cmdLine.required().nextNonOption(cmdLine.createNonOption("password","OldPassword")).getString()).toCharArray();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).setCredentials(pwd).setOldCredentials(old).run();
                                    } else {
                                        workspace.security().updateUser(user).setCredentials(pwd).setOldCredentials(old).run();
                                    }
                                }
                                Arrays.fill(pwd,'\0');
                                Arrays.fill(old,'\0');
                                break;
                            default:
                                cmdLine.setCommandName("config edit user").unexpectedArgument();
                                break;
                        }
                    }
                }
                if (cmdLine.isExecMode()) {
                    trySave(context, workspace, repository, autoSave, null);
                }
                return true;

            } else if (cmdLine.next("unsecure")!=null) {
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r")!=null) {
                        repository = workspace.config().getRepository(cmdLine.required().nextNonOption(cmdLine.createNonOption("RepositoryId")).getString());
                    }
                }
                //unsecure-box
                if (cmdLine.isExecMode()) {
                    char[] credentials = null;
                    if (!context.getWorkspace().security().isAdmin()) {
                        credentials = context.getTerminal().readPassword("Enter password : ");
                    }
                    if (workspace.security().switchUnsecureMode(credentials)) {
                        out.println("<<unsecure box activated.Anonymous has all rights.>>");
                    } else {
                        out.println("<<unsecure box is already activated.>>");
                    }
                    if(credentials!=null) {
                        Arrays.fill(credentials, '\0');
                    }
                }
                trySave(context, workspace, repository, autoSave, cmdLine);
                return true;
            } else if (cmdLine.next("secure")!=null) {
                char[]  credentials = null;
                if (!context.getWorkspace().security().isAdmin()) {
                    credentials = context.getTerminal().readPassword("Enter password : ");
                }
                NutsRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r")!=null) {
                        repository = workspace.config().getRepository(cmdLine.required().nextNonOption(cmdLine.createNonOption("RepositoryId")).getString());
                    }
                }
                //secure-box
                if (cmdLine.isExecMode()) {
                    if (workspace.security().switchSecureMode(credentials)) {
                        out.println("\"\"secure box activated.\"\"");
                    } else {
                        out.println("\"\"secure box already activated.\"\"");
                    }
                }
                if(credentials!=null) {
                    Arrays.fill(credentials, '\0');
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
