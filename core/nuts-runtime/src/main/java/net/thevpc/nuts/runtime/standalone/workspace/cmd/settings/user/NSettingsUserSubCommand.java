/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElementNotFoundException;

import net.thevpc.nuts.NUser;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class NSettingsUserSubCommand extends AbstractNSettingsSubCommand {
    public NSettingsUserSubCommand(NWorkspace workspace) {
        super();
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {
        return exec(null, cmdLine, autoSave);
    }

    public static boolean exec(NRepository editedRepo, NCmdLine cmdLine, Boolean autoSave) {
        if (cmdLine.next("add user", "au").isPresent()) {
            NRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.next("--repo", "-r").isPresent()) {
                    repository = NWorkspace.of().findRepository(cmdLine.nextNonOption(NArgName.of("RepositoryId"))
                            .get().image())
                            .get();
                }
            }
            if (repository == null) {
                String user = cmdLine.nextNonOption(NArgName.of("Username")).get().image();
                char[] password = cmdLine.nextNonOption(NArgName.of("Password")).get().image().toCharArray();
                if (cmdLine.isExecMode()) {
                    NWorkspaceSecurityManager.of().addUser(user).setCredentials(password).run();
                }
            } else {
                String user = cmdLine.nextNonOption(NArgName.of("Username")).get().image();
                char[] password = cmdLine.nextNonOption(NArgName.of("Password")).get().image().toCharArray();
                String mappedUser = null;
                char[] remotePassword = null;
                if (!cmdLine.isEmpty()) {
                    mappedUser = cmdLine.nextNonOption(NArgName.of("RemoteId")).get().image();
                    remotePassword = cmdLine.nextNonOption(NArgName.of("RemotePassword")).get().image().toCharArray();
                }
                if (cmdLine.isExecMode()) {
                    repository.security().addUser(user).setCredentials(password).setRemoteIdentity(mappedUser).setRemoteCredentials(remotePassword).run();
                }
            }
            if (cmdLine.isExecMode()) {
                NWorkspace.of().saveConfig();
            }
            return true;
        } else {
            NSession session = NSession.of();
            NPrintStream out = session.out();
            if (cmdLine.next("list users","list user","user list", "lu").isPresent()) {
                NRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r").isPresent()) {
                        repository = NWorkspace.of()
                                .findRepository(
                                        cmdLine.nextNonOption(NArgName.of("repository"))
                                                .get().image()
                                ).get();
                    }
                }
                if (cmdLine.isExecMode()) {
                    List<NUser> security;
                    if (repository == null) {
                        security = NWorkspaceSecurityManager.of().findUsers();
                    } else {
                        security = repository.security().findUsers();
                    }
                    for (NUser u : security) {
                        out.println(NMsg.ofC("User: %s", u.getUser()));
                        if (!NBlankable.isBlank(u.getRemoteIdentity())) {
                            out.println(NMsg.ofC("   Mapper to  : %s", u.getRemoteIdentity()));
                        }
                        out.println(NMsg.ofC("   Password   : %s", (u.hasCredentials() ? "Set" : "None")));
                        out.println(NMsg.ofC("   Groups     : %s", (u.getGroups().size() == 0 ? "None" : u.getGroups())));
                        out.println(NMsg.ofC("   Rights     : %s", (u.getPermissions().size() == 0 ? "None" : u.getPermissions())));
                    }
                }
                return true;

            } else if (cmdLine.next("password", "passwd", "pwd").isPresent()) {
                NRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r").isPresent()) {
                        repository = NWorkspace.of()
                                .findRepository(cmdLine.nextNonOption(NArgName.of("RepositoryId")).get().image()
                                ).get();
                    }
                }

                String user = null;
                char[] password = null;
                char[] oldPassword = null;
                do {
                    if (cmdLine.next("--user").isPresent()) {
                        user = cmdLine.nextNonOption(NArgName.of("Username")).get().image();
                    } else if (cmdLine.next("--password").isPresent()) {
                        password = cmdLine.nextNonOption(NArgName.of("Password")).get().image().toCharArray();
                    } else if (cmdLine.next("--old-password").isPresent()) {
                        oldPassword = cmdLine.nextNonOption(NArgName.of("OldPassword")).get().image().toCharArray();
                    } else {
                        cmdLine.setCommandName("config password").throwUnexpectedArgument();
                    }
                } while (cmdLine.hasNext());
                if (cmdLine.isExecMode()) {
                    boolean admin;
                    if (repository == null) {
                        admin = NWorkspaceSecurityManager.of().isAllowed(NConstants.Permissions.ADMIN);
                    } else {
                        admin = repository.security().isAllowed(NConstants.Permissions.ADMIN);
                    }

                    if (oldPassword == null && !admin) {
                        oldPassword = session.getTerminal().readPassword(NMsg.ofPlain("Old Password:"));
                    }
                    if (password == null) {
                        password = session.getTerminal().readPassword(NMsg.ofPlain("Password:"));
                    }

                    if (repository == null) {
                        NWorkspaceSecurityManager.of().updateUser(user).setCredentials(password).setOldCredentials(oldPassword).run();
                    } else {
                        repository.security().updateUser(user).setCredentials(password).setOldCredentials(oldPassword).run();
                    }
                    NWorkspace.of().saveConfig();
                }
                return true;

            } else if (cmdLine.next("edit user", "eu").isPresent()) {
                NRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r").isPresent()) {
                        repository = NWorkspace.of().findRepository(
                                cmdLine.nextNonOption(NArgName.of("RepositoryId")).get().image()
                        ).get();
                    }
                }

                String user = cmdLine.nextNonOption(NArgName.of("Username")).get().image();
                if (cmdLine.isExecMode()) {
                    NUser u = null;
                    if (repository == null) {
                        u = NWorkspaceSecurityManager.of().findUser(user);
                    } else {
                        u = repository.security().getEffectiveUser(user);
                    }
                    if (u == null) {
                        throw new NElementNotFoundException(NMsg.ofC("no such user %s", user));
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
                    if (cmdLine.next("--add-group").isPresent()) {
                        lastOption = "--add-group";
                    } else if (cmdLine.next("--remove-group").isPresent()) {
                        lastOption = "--remove-group";
                    } else if (cmdLine.next("--add-right").isPresent()) {
                        lastOption = "--add-right";
                    } else if (cmdLine.next("--remove-right").isPresent()) {
                        lastOption = "--remove-right";
                    } else if (cmdLine.next("--mapped-user").isPresent()) {
                        lastOption = "--mapped-user";
                    } else if (cmdLine.next("--password").isPresent()) {
                        lastOption = "--password";
                    } else {
                        switch (lastOption) {
                            case "--add-group": {
                                String a = cmdLine.nextNonOption(NArgName.of("Group")).get().image();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).addGroup(a).run();
                                    } else {
                                        NWorkspaceSecurityManager.of().updateUser(user).addGroup(a).run();
                                    }
                                }
                                break;
                            }
                            case "--remove-group": {
                                String a = cmdLine.nextNonOption(NArgName.of("Group")).get().image();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).removeGroup(a).run();
                                    } else {
                                        NWorkspaceSecurityManager.of().updateUser(user).removeGroup(a).run();
                                    }
                                }
                                break;
                            }
                            case "--add-right": {
                                String a = cmdLine.nextNonOption(NArgName.of("Right")).get().image();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).addPermission(a).run();
                                    } else {
                                        NWorkspaceSecurityManager.of().updateUser(user).addPermission(a).run();
                                    }
                                }
                                break;
                            }
                            case "--remove-right": {
                                String a = cmdLine.nextNonOption(NArgName.of("Right")).get().image();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).removePermission(a).run();
                                    } else {
                                        NWorkspaceSecurityManager.of().updateUser(user).removePermission(a).run();
                                    }
                                }
                                break;
                            }
                            case "--mapped-user": {
                                String a = cmdLine.nextNonOption(NArgName.of("RemoteIdentity")).get().image();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).setRemoteIdentity(a).run();
                                    } else {
                                        NWorkspaceSecurityManager.of().updateUser(user).setRemoteIdentity(a).run();
                                    }
                                }
                                break;
                            }
                            case "--password":
                                char[] pwd = (cmdLine.nextNonOption(NArgName.of("password", "Password")).get().image()).toCharArray();
                                char[] old = (cmdLine.nextNonOption(NArgName.of("password", "OldPassword")).get().image()).toCharArray();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).setCredentials(pwd).setOldCredentials(old).run();
                                    } else {
                                        NWorkspaceSecurityManager.of().updateUser(user).setCredentials(pwd).setOldCredentials(old).run();
                                    }
                                }
                                Arrays.fill(pwd, '\0');
                                Arrays.fill(old, '\0');
                                break;
                            default:
                                cmdLine.setCommandName("config edit user").throwUnexpectedArgument();
                                break;
                        }
                    }
                }
                if (cmdLine.isExecMode()) {
                    NWorkspace.of().saveConfig();
                }
                return true;

            } else if (cmdLine.next("unsecure").isPresent()) {
                NRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r").isPresent()) {
                        repository = NWorkspace.of().findRepository(
                                cmdLine.nextNonOption(NArgName.of("RepositoryId")).get().image()
                        ).get();
                    }
                }
                //unsecure-box
                if (cmdLine.isExecMode()) {
                    char[] credentials = null;
                    if (!NWorkspaceSecurityManager.of().isAdmin()) {
                        credentials = session.getTerminal().readPassword(NMsg.ofPlain("Enter password : "));
                    }
                    if (NWorkspaceSecurityManager.of().setSecureMode(false, credentials)) {
                        out.println("<<unsecure box activated.Anonymous has all rights.>>");
                    } else {
                        out.println("<<unsecure box is already activated.>>");
                    }
                    if (credentials != null) {
                        Arrays.fill(credentials, '\0');
                    }
                }
                NWorkspace.of().saveConfig();
                return true;
            } else if (cmdLine.next("secure").isPresent()) {
                char[] credentials = null;
                if (!NWorkspaceSecurityManager.of().isAdmin()) {
                    credentials = session.getTerminal().readPassword(NMsg.ofPlain("Enter password : "));
                }
                NRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r").isPresent()) {
                        repository = NWorkspace.of().findRepository(
                                cmdLine.nextNonOption(NArgName.of("RepositoryId")).get().image()
                        ).get();
                    }
                }
                //secure-box
                if (cmdLine.isExecMode()) {
                    if (NWorkspaceSecurityManager.of().setSecureMode(true, credentials)) {
                        out.println("\"\"secure box activated.\"\"");
                    } else {
                        out.println("\"\"secure box already activated.\"\"");
                    }
                }
                if (credentials != null) {
                    Arrays.fill(credentials, '\0');
                }
                NWorkspace.of().saveConfig();
                return true;
            }
        }
        return false;
    }


}
