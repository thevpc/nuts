/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgumentName;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElementNotFoundException;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;

import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class NSettingsUserSubCommand extends AbstractNSettingsSubCommand {

    @Override
    public boolean exec(NCommandLine cmdLine, Boolean autoSave, NSession session) {
        return exec(null, cmdLine, autoSave, session);
    }

    public static boolean exec(NRepository editedRepo, NCommandLine cmdLine, Boolean autoSave, NSession session) {
        if (cmdLine.next("add user", "au").isPresent()) {
            NRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.next("--repo", "-r").isPresent()) {
                    repository = session.repos().getRepository(cmdLine.nextNonOption(NArgumentName.of("RepositoryId", session)).flatMap(NValue::asString).get(session));
                }
            }
            if (repository == null) {
                String user = cmdLine.nextNonOption(NArgumentName.of("Username", session)).flatMap(NValue::asString).get(session);
                char[] password = cmdLine.nextNonOption(NArgumentName.of("Password", session)).flatMap(NValue::asString).get(session).toCharArray();
                if (cmdLine.isExecMode()) {
                    session.security().addUser(user).setCredentials(password).run();
                }
            } else {
                String user = cmdLine.nextNonOption(NArgumentName.of("Username", session)).flatMap(NValue::asString).get(session);
                char[] password = cmdLine.nextNonOption(NArgumentName.of("Password", session)).flatMap(NValue::asString).get(session).toCharArray();
                String mappedUser = null;
                char[] remotePassword = null;
                if (!cmdLine.isEmpty()) {
                    mappedUser = cmdLine.nextNonOption(NArgumentName.of("RemoteId", session)).flatMap(NValue::asString).get(session);
                    remotePassword = cmdLine.nextNonOption(NArgumentName.of("RemotePassword", session)).flatMap(NValue::asString).get(session).toCharArray();
                }
                if (cmdLine.isExecMode()) {
                    repository.security().addUser(user).setCredentials(password).setRemoteIdentity(mappedUser).setRemoteCredentials(remotePassword).run();
                }
            }
            if (cmdLine.isExecMode()) {
                session.config().save();
            }
            return true;
        } else {
            NOutStream out = session.out();
            if (cmdLine.next("list users", "lu").isPresent()) {
                NRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r").isPresent()) {
                        repository = session.repos().getRepository(cmdLine.nextNonOption(NArgumentName.of("repository", session)).flatMap(NValue::asString).get(session));
                    }
                }
                if (cmdLine.isExecMode()) {
                    List<NUser> security;
                    if (repository == null) {
                        security = session.security().findUsers();
                    } else {
                        security = repository.security().findUsers();
                    }
                    for (NUser u : security) {
                        out.printf("User: %s%n", u.getUser());
                        if (!NBlankable.isBlank(u.getRemoteIdentity())) {
                            out.printf("   Mapper to  : %s%n", u.getRemoteIdentity());
                        }
                        out.printf("   Password   : %s%n", (u.hasCredentials() ? "Set" : "None"));
                        out.printf("   Groups     : %s%n", (u.getGroups().size() == 0 ? "None" : u.getGroups()));
                        out.printf("   Rights     : %s%n", (u.getPermissions().size() == 0 ? "None" : u.getPermissions()));
                    }
                }
                return true;

            } else if (cmdLine.next("password", "passwd", "pwd").isPresent()) {
                NRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r").isPresent()) {
                        repository = session.repos().getRepository(cmdLine.nextNonOption(NArgumentName.of("RepositoryId", session)).flatMap(NValue::asString).get(session));
                    }
                }

                String user = null;
                char[] password = null;
                char[] oldPassword = null;
                do {
                    if (cmdLine.next("--user").isPresent()) {
                        user = cmdLine.nextNonOption(NArgumentName.of("Username", session)).flatMap(NValue::asString).get(session);
                    } else if (cmdLine.next("--password").isPresent()) {
                        password = cmdLine.nextNonOption(NArgumentName.of("Password", session)).flatMap(NValue::asString).get(session).toCharArray();
                    } else if (cmdLine.next("--old-password").isPresent()) {
                        oldPassword = cmdLine.nextNonOption(NArgumentName.of("OldPassword", session)).flatMap(NValue::asString).get(session).toCharArray();
                    } else {
                        cmdLine.setCommandName("config password").throwUnexpectedArgument();
                    }
                } while (cmdLine.hasNext());
                if (cmdLine.isExecMode()) {
                    boolean admin;
                    if (repository == null) {
                        admin = session.security().isAllowed(NConstants.Permissions.ADMIN);
                    } else {
                        admin = repository.security().isAllowed(NConstants.Permissions.ADMIN);
                    }

                    if (oldPassword == null && !admin) {
                        oldPassword = session.getTerminal().readPassword("Old Password:");
                    }
                    if (password == null) {
                        password = session.getTerminal().readPassword("Password:");
                    }

                    if (repository == null) {
                        session.security().updateUser(user).setCredentials(password).setOldCredentials(oldPassword).run();
                    } else {
                        repository.security().updateUser(user).setCredentials(password).setOldCredentials(oldPassword).run();
                    }
                    session.config().save();
                }
                return true;

            } else if (cmdLine.next("edit user", "eu").isPresent()) {
                NRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r").isPresent()) {
                        repository = session.repos().getRepository(cmdLine.nextNonOption(NArgumentName.of("RepositoryId", session)).flatMap(NValue::asString).get(session));
                    }
                }

                String user = cmdLine.nextNonOption(NArgumentName.of("Username", session)).flatMap(NValue::asString).get(session);
                if (cmdLine.isExecMode()) {
                    NUser u = null;
                    if (repository == null) {
                        u = session.security().findUser(user);
                    } else {
                        u = repository.security().getEffectiveUser(user);
                    }
                    if (u == null) {
                        throw new NElementNotFoundException(session, NMsg.ofCstyle("no such user %s", user));
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
                                String a = cmdLine.nextNonOption(NArgumentName.of("Group", session)).flatMap(NValue::asString).get(session);
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).addGroup(a).run();
                                    } else {
                                        session.security().updateUser(user).addGroup(a).run();
                                    }
                                }
                                break;
                            }
                            case "--remove-group": {
                                String a = cmdLine.nextNonOption(NArgumentName.of("Group", session)).flatMap(NValue::asString).get(session);
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).removeGroup(a).run();
                                    } else {
                                        session.security().updateUser(user).removeGroup(a).run();
                                    }
                                }
                                break;
                            }
                            case "--add-right": {
                                String a = cmdLine.nextNonOption(NArgumentName.of("Right", session)).flatMap(NValue::asString).get(session);
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).addPermission(a).run();
                                    } else {
                                        session.security().updateUser(user).addPermission(a).run();
                                    }
                                }
                                break;
                            }
                            case "--remove-right": {
                                String a = cmdLine.nextNonOption(NArgumentName.of("Right", session)).flatMap(NValue::asString).get(session);
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).removePermission(a).run();
                                    } else {
                                        session.security().updateUser(user).removePermission(a).run();
                                    }
                                }
                                break;
                            }
                            case "--mapped-user": {
                                String a = cmdLine.nextNonOption(NArgumentName.of("RemoteIdentity", session)).flatMap(NValue::asString).get(session);
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).setRemoteIdentity(a).run();
                                    } else {
                                        session.security().updateUser(user).setRemoteIdentity(a).run();
                                    }
                                }
                                break;
                            }
                            case "--password":
                                char[] pwd = (cmdLine.nextNonOption(NArgumentName.of("password", "Password", session)).flatMap(NValue::asString).get(session)).toCharArray();
                                char[] old = (cmdLine.nextNonOption(NArgumentName.of("password", "OldPassword", session)).flatMap(NValue::asString).get(session)).toCharArray();
                                if (cmdLine.isExecMode()) {
                                    if (repository != null) {
                                        repository.security().updateUser(user).setCredentials(pwd).setOldCredentials(old).run();
                                    } else {
                                        session.security().updateUser(user).setCredentials(pwd).setOldCredentials(old).run();
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
                    session.config().save();
                }
                return true;

            } else if (cmdLine.next("unsecure").isPresent()) {
                NRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r").isPresent()) {
                        repository = session.repos().getRepository(cmdLine.nextNonOption(NArgumentName.of("RepositoryId", session)).flatMap(NValue::asString).get(session));
                    }
                }
                //unsecure-box
                if (cmdLine.isExecMode()) {
                    char[] credentials = null;
                    if (!session.security().isAdmin()) {
                        credentials = session.getTerminal().readPassword("Enter password : ");
                    }
                    if (session.security().setSecureMode(false, credentials)) {
                        out.println("<<unsecure box activated.Anonymous has all rights.>>");
                    } else {
                        out.println("<<unsecure box is already activated.>>");
                    }
                    if (credentials != null) {
                        Arrays.fill(credentials, '\0');
                    }
                }
                session.config().save();
                return true;
            } else if (cmdLine.next("secure").isPresent()) {
                char[] credentials = null;
                if (!session.security().isAdmin()) {
                    credentials = session.getTerminal().readPassword("Enter password : ");
                }
                NRepository repository = null;
                if (editedRepo != null) {
                    repository = editedRepo;
                } else {
                    if (cmdLine.next("--repo", "-r").isPresent()) {
                        repository = session.repos().getRepository(cmdLine.nextNonOption(NArgumentName.of("RepositoryId", session)).flatMap(NValue::asString).get(session));
                    }
                }
                //secure-box
                if (cmdLine.isExecMode()) {
                    if (session.security().setSecureMode(true, credentials)) {
                        out.println("\"\"secure box activated.\"\"");
                    } else {
                        out.println("\"\"secure box already activated.\"\"");
                    }
                }
                if (credentials != null) {
                    Arrays.fill(credentials, '\0');
                }
                session.config().save();
                return true;
            }
        }
        return false;
    }


}
