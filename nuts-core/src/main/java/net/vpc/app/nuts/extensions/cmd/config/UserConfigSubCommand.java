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
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.cmd.cmdline.DefaultNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.GroupNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.RepositoryNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.RightNonOption;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class UserConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CmdLine cmdLine, net.vpc.app.nuts.extensions.cmd.ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        return exec(null, cmdLine, config, autoSave, context);
    }

    public static boolean exec(NutsRepository editedRepo, CmdLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        NutsWorkspace workspaĉe = context.getValidWorkspace();
        if (cmdLine.read("add user", "au")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
                    repository = workspaĉe.findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", workspaĉe)).getString());
                }
            }
            if (repository == null) {
                String user = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getString();
                String password = cmdLine.readNonOption(new DefaultNonOption("Password")).getString();
                if (cmdLine.isExecMode()) {
                    workspaĉe.addUser(user, password);
                }
            } else {
                String user = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getString();
                String mappedUser = cmdLine.readNonOption(new DefaultNonOption("MappedUser")).getString();
                String password = cmdLine.readNonOption(new DefaultNonOption("Password")).getString();
                if (cmdLine.isExecMode()) {
                    repository.addUser(user, password);
                    repository.setUserRemoteIdentity(user, mappedUser);
                }
            }
            if (cmdLine.isExecMode()) {
                ConfigCommand.trySave(context, workspaĉe, repository, autoSave, null);
            }
            return true;
        } else if (cmdLine.read("list users", "lu")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
                    repository = workspaĉe.findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("Repository", workspaĉe)).getString());
                }
            }
            if (cmdLine.isExecMode()) {
                NutsUserInfo[] security;
                if (repository == null) {
                    security = workspaĉe.findUsers();
                } else {
                    security = repository.findUsers();
                }
                for (NutsUserInfo u : security) {
                    context.getTerminal().getOut().println("User: " + u.getUser());
                    if (!CoreStringUtils.isEmpty(u.getMappedUser())) {
                        context.getTerminal().getOut().println("   Mapper to  : " + u.getMappedUser());
                    }
                    context.getTerminal().getOut().println("   Password   : " + (u.hasCredentials() ? "Set" : "None"));
                    context.getTerminal().getOut().println("   Groups     : " + (u.getGroups().length == 0 ? "None" : Arrays.asList(u.getGroups())));
                    context.getTerminal().getOut().println("   Rights     : " + (u.getRights().length == 0 ? "None" : Arrays.asList(u.getRights())));
                }
            }
            return true;

        } else if (cmdLine.read("password", "passwd", "pwd")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
                    repository = workspaĉe.findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", workspaĉe)).getString());
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
                    admin = workspaĉe.isAllowed(NutsConstants.RIGHT_ADMIN);
                } else {
                    admin = repository.isAllowed(NutsConstants.RIGHT_ADMIN);
                }

                if (oldPassword == null && !admin) {
                    oldPassword = context.getTerminal().readPassword("Old Password:");
                }
                if (password == null) {
                    password = context.getTerminal().readPassword("Password:");
                }

                if (repository == null) {
                    workspaĉe.setUserCredentials(user, password, oldPassword);
                } else {
                    repository.setUserCredentials(user, password, oldPassword);
                }
                ConfigCommand.trySave(context, workspaĉe, repository, autoSave, null);
            }
            return true;

        } else if (cmdLine.read("edit user", "eu")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
                    repository = workspaĉe.findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", workspaĉe)).getString());
                }
            }

            String user = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getString();
            if (cmdLine.isExecMode()) {
                NutsUserInfo u = null;
                if (repository == null) {
                    u = workspaĉe.findUser(user);
                } else {
                    u = repository.findUser(user);
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
                                    repository.addUserGroups(user, a);
                                } else {
                                    workspaĉe.addUserGroups(user, a);
                                }
                            }
                            break;
                        }
                        case "--remove-group": {
                            String a = cmdLine.readNonOptionOrError(new GroupNonOption("Group", context, repository)).getString();
                            if (cmdLine.isExecMode()) {
                                if (repository != null) {
                                    repository.removeUserGroups(user, a);
                                } else {
                                    workspaĉe.removeUserGroups(user, a);
                                }
                            }
                            break;
                        }
                        case "--add-right": {
                            String a = cmdLine.readNonOptionOrError(new RightNonOption("Right", workspaĉe, repository, user, false)).getString();
                            if (cmdLine.isExecMode()) {
                                if (repository != null) {
                                    repository.addUserRights(user, a);
                                } else {
                                    workspaĉe.addUserRights(user, a);
                                }
                            }
                            break;
                        }
                        case "--remove-right": {
                            String a = cmdLine.readNonOptionOrError(new RightNonOption("Right", workspaĉe, repository, user, true)).getString();
                            if (cmdLine.isExecMode()) {
                                if (repository != null) {
                                    repository.removeUserRights(user, a);
                                } else {
                                    workspaĉe.removeUserRights(user, a);
                                }
                            }
                            break;
                        }
                        case "--mapped-user": {
                            String a = cmdLine.readNonOptionOrError(new DefaultNonOption("MappedUser")).getString();
                            if (cmdLine.isExecMode()) {
                                if (repository != null) {
                                    repository.setUserRemoteIdentity(user, a);
                                } else {
                                    workspaĉe.setUserRemoteIdentity(user, a);
                                }
                            }
                            break;
                        }
                        case "--password":
                            String pwd = (cmdLine.readNonOptionOrError(new DefaultNonOption("Password")).getString());
                            String old = (cmdLine.readNonOptionOrError(new DefaultNonOption("OldPassword")).getString());
                            if (cmdLine.isExecMode()) {
                                if (repository != null) {
                                    repository.setUserCredentials(user, pwd, old);
                                } else {
                                    workspaĉe.setUserCredentials(user, pwd, old);
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
                ConfigCommand.trySave(context, workspaĉe, repository, autoSave, null);
            }
            return true;

        } else if (cmdLine.read("unsecure")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
                    repository = workspaĉe.findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", workspaĉe)).getString());
                }
            }
            //unsecure-box
            if (cmdLine.isExecMode()) {
                String adminPassword = null;
                if (!context.getWorkspace().isAdmin()) {
                    adminPassword = context.getTerminal().readPassword("Enter password : ");
                }
                if (workspaĉe.switchUnsecureMode(adminPassword)) {
                    context.getTerminal().getOut().println("<<unsecure box activated.Anonymous has all rights.>>");
                } else {
                    context.getTerminal().getOut().println("<<unsecure box is already activated.>>");
                }
            }
            ConfigCommand.trySave(context, workspaĉe, repository, autoSave, cmdLine);
            return true;
        } else if (cmdLine.read("secure")) {
            String adminPassword = null;
            if (!context.getWorkspace().isAdmin()) {
                adminPassword = context.getTerminal().readPassword("Enter password : ");
            }
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
                    repository = workspaĉe.findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", workspaĉe)).getString());
                }
            }
            //secure-box
            if (cmdLine.isExecMode()) {
                if (workspaĉe.switchSecureMode(adminPassword)) {
                    context.getTerminal().getOut().println("\"\"secure box activated.\"\"");
                } else {
                    context.getTerminal().getOut().println("\"\"secure box already activated.\"\"");
                }
            }
            ConfigCommand.trySave(context, workspaĉe, repository, autoSave, cmdLine);
            return true;
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT;
    }

}
