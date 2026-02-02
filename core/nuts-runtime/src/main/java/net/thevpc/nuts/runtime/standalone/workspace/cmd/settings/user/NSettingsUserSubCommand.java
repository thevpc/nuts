/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElementNotFoundException;

import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.security.NSecurityManager;
import net.thevpc.nuts.security.NUser;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.text.NMsg;

import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NSettingsUserSubCommand extends AbstractNSettingsSubCommand {
    public NSettingsUserSubCommand() {
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
            String user = cmdLine.nextNonOption(NArgName.of("Username")).get().image();
            char[] password = cmdLine.nextNonOption(NArgName.of("Password")).get().image().toCharArray();
            if (cmdLine.isExecMode()) {
                NSecurityManager.of().updateUser(
                        NSecurityManager.of().findUser(user)
                                .get().toSpec()
                                .setCredential(NSecurityManager.of().addOneWayCredential(password))
                );
            }
            if (repository != null) {
                String mappedUser = null;
                char[] remotePassword = null;
                if (!cmdLine.isEmpty()) {
                    mappedUser = cmdLine.nextNonOption(NArgName.of("RemoteId")).get().image();
                    remotePassword = cmdLine.nextNonOption(NArgName.of("RemotePassword")).get().image().toCharArray();
                }
                if (cmdLine.isExecMode()) {
                    NSecurityManager.of().updateRepositoryAccess(NSecurityManager.of().findRepositoryAccess(
                                    user, repository.getUuid()).get()
                            .toSpec()
                            .setRemoteUserName(mappedUser)
                            .setRemoteCredential(NSecurityManager.of().addSecret(remotePassword))
                            );
                }
            }
            if (cmdLine.isExecMode()) {
                NWorkspace.of().saveConfig();
            }
            return true;
        } else {
            NSession session = NSession.of();
            NPrintStream out = session.out();
            if (cmdLine.next("list users", "list user", "user list", "lu").isPresent()) {
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
                    security = NSecurityManager.of().findUsers();
                    for (NUser u : security) {
                        out.println(NMsg.ofC("User: %s", u.getUsername()));
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
                        admin = NSecurityManager.of().isAllowed(NConstants.Permissions.ADMIN);
                    } else {
                        admin = NSecurityManager.of().isRepositoryAllowed(repository.getUuid(), NConstants.Permissions.ADMIN);
                    }

                    if (oldPassword == null && !admin) {
                        oldPassword = session.getTerminal().readPassword(NMsg.ofPlain("Old Password:"));
                    }
                    if (password == null) {
                        password = session.getTerminal().readPassword(NMsg.ofPlain("Password:"));
                    }
                    NSecurityManager.of().updateUser(
                            NSecurityManager.of().findUser(user)
                                    .get().toSpec()
                                    .setCredential(NSecurityManager.of().addOneWayCredential(password))
                    );

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
                    u = NSecurityManager.of().findUser(user).orNull();
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
                String remoteUser = null;
                String remotePassword = null;
                while (cmdLine.hasNext()) {
                    if (cmdLine.next("--add-group").isPresent()) {
                        lastOption = "--add-group";
                    } else if (cmdLine.next("--remove-group").isPresent()) {
                        lastOption = "--remove-group";
                    } else if (cmdLine.next("--add-right").isPresent()) {
                        lastOption = "--add-right";
                    } else if (cmdLine.next("--remove-right").isPresent()) {
                        lastOption = "--remove-right";
                    } else if (cmdLine.next("--remote-user").isPresent()) {
                        lastOption = "--remote-user";
                    } else if (cmdLine.next("--password").isPresent()) {
                        lastOption = "--password";
                    } else {
                        switch (lastOption) {
                            case "--add-group": {
                                String a = cmdLine.nextNonOption(NArgName.of("Group")).get().image();
                                if (cmdLine.isExecMode()) {
                                    NSecurityManager.of().updateUser(
                                            NSecurityManager.of().findUser(user)
                                                    .get().toSpec()
                                                    .addGroups(a)
                                    );
                                }
                                break;
                            }
                            case "--remove-group": {
                                String a = cmdLine.nextNonOption(NArgName.of("Group")).get().image();
                                if (cmdLine.isExecMode()) {
                                    NSecurityManager.of().updateUser(
                                            NSecurityManager.of().findUser(user)
                                                    .get().toSpec()
                                                    .removeGroups(a)
                                    );
                                }
                                break;
                            }
                            case "--add-right": {
                                String a = cmdLine.nextNonOption(NArgName.of("Right")).get().image();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        NSecurityManager.of().addRepositoryPermissions(user, repository.getUuid(), a);
                                    } else {
                                        NSecurityManager.of().updateUser(
                                                NSecurityManager.of().findUser(user)
                                                        .get().toSpec()
                                                        .addPermissions(a)
                                        );
                                    }
                                }
                                break;
                            }
                            case "--remove-right": {
                                String a = cmdLine.nextNonOption(NArgName.of("Right")).get().image();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        NSecurityManager.of().removeRepositoryPermissions(user, repository.getUuid(), a);
                                    } else {
                                        NSecurityManager.of().updateUser(
                                                NSecurityManager.of().findUser(user).get()
                                                        .toSpec()
                                                        .removePermissions(a)
                                        );
                                    }
                                }
                                break;
                            }
                            case "--remote-user": {
                                String a = cmdLine.nextNonOption(NArgName.of("RemoteIdentity")).get().image();
                                NOptional<NArg> bb = cmdLine.peek();
                                if (bb.isPresent() && bb.get().key().equals("--remote-password")) {
                                    String b = cmdLine.nextNonOption(NArgName.of("RemotePassword")).get().image();
                                    if (cmdLine.isExecMode()) {
                                        if (repository != null) {
                                            NSecurityManager.of().updateRepositoryAccess(NSecurityManager.of().findRepositoryAccess(
                                                            user, repository.getUuid()).get()
                                                    .toSpec()
                                                    .setRemoteUserName(a)
                                                    .setRemoteCredential(NSecurityManager.of().addSecret(b.toCharArray()))
                                                    );
                                        }
                                    }
                                }
                                break;
                            }
                            case "--password":
                                char[] pwd = (cmdLine.nextNonOption(NArgName.of("password", "Password")).get().image()).toCharArray();
                                char[] old = (cmdLine.nextNonOption(NArgName.of("password", "OldPassword")).get().image()).toCharArray();
                                if (cmdLine.isExecMode()) {
                                    NSecurityManager.of().updateUser(
                                            NSecurityManager.of().findUser(user)
                                                    .get().toSpec()
                                                    .setCredential(NSecurityManager.of().addOneWayCredential(pwd))
                                    );
//                                                .setOldCredentials(old)

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
                    if (!NSecurityManager.of().isAdmin()) {
                        credentials = session.getTerminal().readPassword(NMsg.ofPlain("Enter password : "));
                    }
                    if (NSecurityManager.of().setSecureMode(false, credentials)) {
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
                if (!NSecurityManager.of().isAdmin()) {
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
                    if (NSecurityManager.of().setSecureMode(true, credentials)) {
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
