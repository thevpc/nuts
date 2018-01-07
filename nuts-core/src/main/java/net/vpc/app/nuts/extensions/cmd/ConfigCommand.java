/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.cmd.cmdline.*;
import net.vpc.app.nuts.util.LogUtils;
import net.vpc.app.nuts.util.StringUtils;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class ConfigCommand extends AbstractNutsCommand {

    public ConfigCommand() {
        super("config", CORE_SUPPORT);
    }

    public void run(String[] args, NutsCommandContext context, NutsCommandAutoComplete autoComplete) throws IOException, LoginException {
        Boolean autoSave=null;
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        do {
            if (cmdLine.acceptAndRemoveNoDuplicates("--save")) {
                autoSave = true;
                continue;
            }
            if (cmdLine.acceptAndRemoveNoDuplicates("-h", "-?", "--help")) {
                if (cmdLine.isExecMode()) {
                    NutsPrintStream out = context.getTerminal().getOut();
                    out.println("update");
                    out.println("check-updates");
                    out.println("create workspace ...");
                    out.println("set workspace ...");
                    out.println("create repo ...");
                    out.println("add repo ...");
                    out.println("remove repo ...");
                    out.println("list repos ...");
                    out.println("add extension ...");
                    out.println("list extensions ...");
                    out.println("edit repo <repoId> ...");
                    out.println("list imports");
                    out.println("clear imports");
                    out.println("list archetypes");
                    out.println("import");
                    out.println("list imports");
                    out.println("clear imports");
                    out.println("unimport");
                    out.println("list users");
                    out.println("add user");
                    out.println("edit user");
                    out.println("passwd");
                    out.println("secure");
                    out.println("unsecure");
                    out.println("");
                    out.println("type 'help config' for more detailed help");
                }
                continue;
            }
            if (cmdLine.acceptAndRemoveNoDuplicates("show location")) {
                NutsSession session = context.getSession();
                session.getTerminal().getOut().drawln(context.getValidWorkspace().getWorkspaceLocation());
                continue;
            }
            if (cmdLine.acceptAndRemoveNoDuplicates("check-updates")) {
                NutsSession session = context.getSession();
                NutsUpdate[] updates = context.getValidWorkspace().checkWorkspaceUpdates(session);
                if (updates.length==0) {
                    session.getTerminal().getOut().drawln("Workspace is [[up-to-date]]");
                }else{
                    session.getTerminal().getOut().drawln("Workspace has "+updates.length+" component"+(updates.length>1?"s":"")+" to update");
                    for (NutsUpdate update : updates) {
                        session.getTerminal().getOut().drawln(update.getBaseId()+"  : "+update.getLocalId()+" => [["+update.getAvailableId()+"]]");
                    }
                }
                continue;
            }
            if (cmdLine.acceptAndRemoveNoDuplicates("update")) {
                NutsSession session = context.getSession();
                NutsFile newVersion=null;
                try {
                    newVersion = context.getValidWorkspace().updateWorkspace(session);
                } catch (Exception ex) {
                    //not found
                }
                if (newVersion != null) {
                    session.getTerminal().getOut().drawln("Workspace updated to [["+newVersion.getId()+"]]");
                }
                continue;
            }
            if (processWorkspaceCommands(cmdLine, autoSave, context)) {
                continue;
            }
            if (processRepositoryCommands(cmdLine, autoSave, context)) {
                continue;
            }
            if (processExtensionCommands(cmdLine, autoSave, context)) {
                continue;
            }
            if (processImportCommands(cmdLine, autoSave, context)) {
                continue;
            }
            if (processUserCommands(cmdLine, null, autoSave, context)) {
                continue;
            }
            if (processArchetypeCommands(cmdLine, autoSave, context)) {
                continue;
            }
            if (processLogLevelCommands(cmdLine, autoSave, context)) {
                continue;
            }
            if (!cmdLine.isExecMode()) {
                return;
            }
            if (!cmdLine.isEmpty()) {
                NutsPrintStream out = context.getTerminal().getErr();
                out.println("Unexpected "+cmdLine.get(0));
                out.println("type for more help : config -h");
                return;
            }
            break;
        } while (!cmdLine.isEmpty());

    }

    private void showRepo(NutsCommandContext context, NutsRepository repository, String prefix) throws IOException {
        boolean enabled = repository.getWorkspace().getConfig().getRepository(repository.getRepositoryId()).isEnabled();
        String disabledString = enabled ? "" : " <DISABLED>";
        context.getTerminal().getOut().print(prefix);
        context.getTerminal().getOut().print(enabled ? NutsPrintColors.BLUE : NutsPrintColors.RED, repository.getRepositoryId() + disabledString);
        context.getTerminal().getOut().print(" : " + repository);
        context.getTerminal().getOut().println();

    }

    private void showRepoTree(NutsCommandContext context, NutsRepository repository, String prefix) throws IOException {
        showRepo(context, repository, prefix);
        String prefix1 = prefix + "  ";
        for (NutsRepository c : repository.getMirrors()) {
            showRepoTree(context, c, prefix1);
        }
    }

    private boolean processArchetypeCommands(CmdLine cmdLine, Boolean autoSave, NutsCommandContext context) throws IOException, LoginException {
        if (cmdLine.acceptAndRemove("list archetypes", "la")) {
            if (cmdLine.isExecMode()) {
                for (String archetype : context.getValidWorkspace().getAvailableArchetypes()) {
                    context.getTerminal().getOut().println(archetype);
                }
            }
            return true;
        }
        return false;
    }

    private boolean processLogLevelCommands(CmdLine cmdLine, Boolean autoSave, NutsCommandContext context) throws IOException, LoginException {
        if (cmdLine.acceptAndRemove("set loglevel", "sll")) {
            if (cmdLine.acceptAndRemove("verbose", "finest")) {
                if (cmdLine.isExecMode()) {
                    LogUtils.setLevel(Level.FINEST);
                }
            } else if (cmdLine.acceptAndRemove("fine")) {
                if (cmdLine.isExecMode()) {
                    LogUtils.setLevel(Level.FINE);
                }
            } else if (cmdLine.acceptAndRemove("finer")) {
                if (cmdLine.isExecMode()) {
                    LogUtils.setLevel(Level.FINER);
                }
            } else if (cmdLine.acceptAndRemove("info")) {
                if (cmdLine.isExecMode()) {
                    LogUtils.setLevel(Level.INFO);
                }
            } else if (cmdLine.acceptAndRemove("warning")) {
                if (cmdLine.isExecMode()) {
                    LogUtils.setLevel(Level.WARNING);
                }
            } else if (cmdLine.acceptAndRemove("severe", "error")) {
                if (cmdLine.isExecMode()) {
                    LogUtils.setLevel(Level.SEVERE);
                }
            } else if (cmdLine.acceptAndRemove("config")) {
                if (cmdLine.isExecMode()) {
                    LogUtils.setLevel(Level.CONFIG);
                }
            } else if (cmdLine.acceptAndRemove("off")) {
                if (cmdLine.isExecMode()) {
                    LogUtils.setLevel(Level.OFF);
                }
            } else if (cmdLine.acceptAndRemove("all")) {
                if (cmdLine.isExecMode()) {
                    LogUtils.setLevel(Level.ALL);
                }
            } else {
                if (cmdLine.isExecMode()) {
                    throw new IllegalArgumentException("Invalid loglevel");
                }
            }
            cmdLine.requireEmpty();
            return true;
        } else if (cmdLine.acceptAndRemove("get loglevel")) {
            if (cmdLine.isExecMode()) {
                Logger rootLogger = Logger.getLogger("");
                System.out.println(rootLogger.getLevel().toString());
            }
        }
        return false;
    }

    private boolean processRepositoryCommands(CmdLine cmdLine, Boolean autoSave, NutsCommandContext context) throws IOException, LoginException {
        NutsWorkspace validWorkspace = context.getValidWorkspace();
        if (cmdLine.acceptAndRemove("save repository", "sw")) {
            String repositoryId = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString();
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                trySave(context,validWorkspace,validWorkspace.findRepository(repositoryId),true,null);
            }
            return true;

        } else if (cmdLine.acceptAndRemove("create repo", "cr")) {
            String repositoryId = cmdLine.readNonOptionOrError(new DefaultNonOption("NewRepositoryId")).getString();
            String location = cmdLine.removeNonOption(new DefaultNonOption("RepositoryLocation")).getString();
            String repoType = cmdLine.removeNonOption(new RepositoryNonOption("RepositoryType", context.getValidWorkspace())).getString();
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                NutsRepository repository = validWorkspace.openRepository(repositoryId, null,location, repoType, true);
                trySave(context,validWorkspace,repository,autoSave,null);
            }
            return true;

        } else if (cmdLine.acceptAndRemove("add repo", "ar")) {
            boolean proxy = false;
            boolean pattern = false;
            while (!cmdLine.isEmpty()) {
                if (cmdLine.acceptAndRemoveNoDuplicates("--proxy", "-p")) {
                    proxy = true;
                } else if (cmdLine.acceptAndRemoveNoDuplicates("--pattern", "-P")) {
                    pattern = true;
                } else {
                    class RepoPattern {

                        String id;
                        String location;
                        String type;

                        public RepoPattern(String id, String location, String type) {
                            this.id = id;
                            this.location = location;
                            this.type = type;
                        }
                    }
                    Map<String, RepoPattern> repoPatterns = new HashMap<String, RepoPattern>();
                    for (RepoPattern repoPattern : new RepoPattern[]{
                            new RepoPattern("maven-local", System.getProperty("maven-local","~/.m2/repository"), "maven"),
                            new RepoPattern("maven-central", "http://repo.maven.apache.org/maven2/", "maven"),
                            new RepoPattern("maven-vpc-public", "https://raw.githubusercontent.com/thevpc/vpc-public-maven/master", "maven")}) {
                        repoPatterns.put(repoPattern.id, repoPattern);
                    }
                    String repositoryId = cmdLine.readNonOptionOrError(new DefaultNonOption("RepositoryId") {
                        @Override
                        public List<ArgumentCandidate> getValues() {
                            ArrayList<ArgumentCandidate> arrayList = new ArrayList<>();
                            for (Map.Entry<String, RepoPattern> e : repoPatterns.entrySet()) {
                                arrayList.add(new DefaultArgumentCandidate(e.getKey()));
                            }
                            arrayList.add(new DefaultArgumentCandidate("<RepositoryId>"));
                            return arrayList;
                        }

                    }).getString();
                    String location = null;
                    String repoType = null;
                    if (pattern) {
                        RepoPattern found = repoPatterns.get(repositoryId);
                        if (found == null) {
                            throw new IllegalArgumentException("Repository Pattern not found " + repositoryId + ". Try one of " + repoPatterns.keySet());
                        }
                        location = found.location;
                        repoType = found.type;
                    } else {
                        location = cmdLine.readNonOptionOrError(new FolderNonOption("Location")).getString();
                        repoType = cmdLine.readNonOptionOrError(new RepositoryTypeNonOption("RepositoryType", context)).getString();
                    }
                    if (cmdLine.isExecMode()) {
                        NutsRepository repo = null;
                        if (proxy) {
                            repo = validWorkspace.addProxiedRepository(repositoryId, location, repoType, true);
                        } else {
                            repo = validWorkspace.addRepository(repositoryId, location, repoType, true);
                        }
                        context.getTerminal().getOut().println("Repository added successfully");
                        trySave(context,validWorkspace,repo,autoSave,null);
                        trySave(context,validWorkspace,null,autoSave,null);
                    }
                    cmdLine.requireEmpty();
                }
            }
            return true;

        } else if (cmdLine.acceptAndRemove("remove repo", "rr")) {
            String locationOrRepositoryId = cmdLine.readNonOptionOrError(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString();
            if (cmdLine.isExecMode()) {
                validWorkspace.removeRepository(locationOrRepositoryId);
                trySave(context,context.getValidWorkspace(),null,autoSave,cmdLine);
            }
            return true;

        } else if (cmdLine.acceptAndRemove("list repos", "lr")) {
            if (cmdLine.isExecMode()) {
                for (NutsRepository repository : validWorkspace.getRepositories()) {
                    showRepo(context, repository, "");
                }
            }
            return true;

        } else if (cmdLine.acceptAndRemove("tree repos", "tr")) {
            if (cmdLine.isExecMode()) {

                for (NutsRepository repository : validWorkspace.getRepositories()) {
                    showRepoTree(context, repository, "");
                }
            }
            return true;

        } else if (cmdLine.acceptAndRemove("enable repo", "er")) {
            String localId = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString();
            if (cmdLine.isExecMode()) {

                NutsRepository editedRepo = validWorkspace.findRepository(localId);
                editedRepo.getWorkspace().getConfig().getRepository(editedRepo.getRepositoryId()).setEnabled(true);
                trySave(context,context.getValidWorkspace(),null,autoSave,cmdLine);
            }
            return true;
        } else if (cmdLine.acceptAndRemove("disable repo", "rr")) {
            String localId = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString();
            if (cmdLine.isExecMode()) {
                NutsRepository editedRepo = validWorkspace.findRepository(localId);
                editedRepo.getWorkspace().getConfig().getRepository(editedRepo.getRepositoryId()).setEnabled(false);
                trySave(context,context.getValidWorkspace(),null,autoSave,cmdLine);
            }
            return true;
        } else if (cmdLine.acceptAndRemove("edit repo", "er")) {
            String repoId = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoyId", context.getValidWorkspace())).getString();
            if (cmdLine.acceptAndRemove("add repo", "ar")) {
                String repositoryId = cmdLine.readNonOptionOrError(new DefaultNonOption("NewRepositoryId")).getString();
                String location = cmdLine.readNonOptionOrError(new FolderNonOption("RepositoryLocation")).getString();
                String repoType = cmdLine.removeNonOption(new RepositoryTypeNonOption("RepositoryType", context)).getString();

                NutsRepository editedRepo = validWorkspace.findRepository(repoId);
                NutsRepository repo = editedRepo.addMirror(repositoryId, location, repoType, true);
                trySave(context,validWorkspace,editedRepo,autoSave,null);
                trySave(context,validWorkspace,repo,autoSave,null);

            } else if (cmdLine.acceptAndRemove("remove repo", "rr")) {
                String location = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString();
                NutsRepository editedRepo = validWorkspace.findRepository(repoId);
                editedRepo.removeMirror(location);
                trySave(context,validWorkspace,editedRepo,autoSave,null);

            } else if (cmdLine.acceptAndRemove("enable", "rr")) {
                NutsRepository editedRepo = validWorkspace.findRepository(repoId);
                editedRepo.getWorkspace().getConfig().getRepository(editedRepo.getRepositoryId()).setEnabled(true);
                trySave(context,validWorkspace,editedRepo,autoSave,null);

            } else if (cmdLine.acceptAndRemove("disable", "rr")) {
                NutsRepository editedRepo = validWorkspace.findRepository(repoId);
                editedRepo.getWorkspace().getConfig().getRepository(editedRepo.getRepositoryId()).setEnabled(true);
                trySave(context,validWorkspace,editedRepo,autoSave,null);
            } else if (cmdLine.acceptAndRemove("list repos", "lr")) {
                NutsRepository editedRepo = validWorkspace.findRepository(repoId);
                NutsRepository[] linkRepositories = editedRepo.getMirrors();
                context.getTerminal().getOut().println(linkRepositories.length + " sub repositories.");
                for (NutsRepository repository : linkRepositories) {
                    showRepo(context, repository, "");
                }
            } else if (cmdLine.acceptAndRemoveNoDuplicates("-h", "-?", "--help")) {
                context.getTerminal().getOut().println("edit repository " + repoId + " add repo ...");
                context.getTerminal().getOut().println("edit repository " + repoId + " remove repo ...");
                context.getTerminal().getOut().println("edit repository " + repoId + " list repos ...");
            } else {
                NutsRepository editedRepo = validWorkspace.findRepository(repoId);
                if (processUserCommands(cmdLine, editedRepo, autoSave, context)) {
                    //okkay
                } else {
                    throw new IllegalArgumentException("Unsupported command " + cmdLine);
                }
            }
            return true;
        }
        return false;
    }

    private boolean processUserCommands(CmdLine cmdLine, NutsRepository editedRepo, Boolean autoSave, NutsCommandContext context) throws IOException, LoginException {
        if (cmdLine.acceptAndRemove("add user", "au")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.acceptAndRemove("--repo", "-r")) {
                    repository = context.getValidWorkspace().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString());
                }
            }
            if (repository == null) {
                NutsSecurityEntityConfig u = new NutsSecurityEntityConfig();
                String user = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getString();
                String password = cmdLine.removeNonOption(new DefaultNonOption("Password")).getString();
                if (cmdLine.isExecMode()) {
                    u.setUser(user);
                    context.getValidWorkspace().getConfig().setSecurity(u);
                    context.getValidWorkspace().setUserCredentials(u.getUser(), password);
                }
            } else {
                NutsSecurityEntityConfig u = new NutsSecurityEntityConfig();
                String user = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getString();
                String mappedUser = cmdLine.removeNonOption(new DefaultNonOption("MappedUser")).getString();
                String password = cmdLine.removeNonOption(new DefaultNonOption("Password")).getString();
                if (cmdLine.isExecMode()) {
                    u.setUser(user);
                    u.setMappedUser(mappedUser);
                    repository.getConfig().setSecurity(u);
                    repository.setUserCredentials(u.getUser(), password);
                }
            }
            if (cmdLine.isExecMode()) {
                trySave(context, context.getValidWorkspace(),repository, autoSave, null);
            }
            return true;
        } else if (cmdLine.acceptAndRemove("list users", "lu")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.acceptAndRemove("--repo", "-r")) {
                    repository = context.getValidWorkspace().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString());
                }
            }
            if (cmdLine.isExecMode()) {
                NutsSecurityEntityConfig[] security;
                if (repository == null) {
                    security = context.getValidWorkspace().getConfig().getSecurity();
                } else {
                    security = repository.getConfig().getSecurity();
                }
                for (NutsSecurityEntityConfig u : security) {
                    context.getTerminal().getOut().println("User: " + u.getUser());
                    if (!StringUtils.isEmpty(u.getMappedUser())) {
                        context.getTerminal().getOut().println("   Mapper to  : " + u.getMappedUser());
                    }
                    if (StringUtils.isEmpty(u.getCredentials())) {
                        context.getTerminal().getOut().println("   Password   : None");
                    } else {
                        context.getTerminal().getOut().println("   Password   : Set");
                    }
                    context.getTerminal().getOut().println("   Groups     : " + (u.getGroups().length == 0 ? "None" : Arrays.asList(u.getGroups())));
                    context.getTerminal().getOut().println("   Rights     : " + (u.getRights().length == 0 ? "None" : Arrays.asList(u.getRights())));
                }
            }
            return true;

        } else if (cmdLine.acceptAndRemove("password", "passwd", "pwd")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.acceptAndRemove("--repo", "-r")) {
                    repository = context.getValidWorkspace().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString());
                }
            }

            String user = null;
            String password = null;
            String oldPassword = null;
            do {
                if (cmdLine.acceptAndRemove("--user")) {
                    user = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getString();
                } else if (cmdLine.acceptAndRemove("--password")) {
                    password = cmdLine.readNonOptionOrError(new DefaultNonOption("Password")).getString();
                } else if (cmdLine.acceptAndRemove("--old-password")) {
                    oldPassword = cmdLine.readNonOptionOrError(new DefaultNonOption("OldPassword")).getString();
                } else {
                    cmdLine.requireEmpty();
                }
            } while (!cmdLine.isEmpty());
            if (cmdLine.isExecMode()) {
                boolean admin;
                if (repository == null) {
                    admin = context.getValidWorkspace().isAllowed(NutsConstants.RIGHT_ADMIN);
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
                    context.getValidWorkspace().setUserCredentials(user, password, oldPassword);
                } else {
                    repository.setUserCredentials(user, password, oldPassword);
                }
                trySave(context, context.getValidWorkspace(), repository, autoSave, null);
            }
            return true;

        } else if (cmdLine.acceptAndRemove("edit user", "eu")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.acceptAndRemove("--repo", "-r")) {
                    repository = context.getValidWorkspace().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString());
                }
            }

            String user = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getString();
            NutsSecurityEntityConfig u = null;
            if (repository == null) {
                u = context.getValidWorkspace().getConfig().getSecurity(user);
            } else {
                u = repository.getConfig().getSecurity(user);
            }
            if (u == null) {
                if (cmdLine.isExecMode()) {
                    throw new IllegalArgumentException("No such user " + user);
                }
            }
            String lastOption = "";
            while (!cmdLine.isEmpty()) {
                if (cmdLine.acceptAndRemoveNoDuplicates("--add-group")) {
                    lastOption = "--add-group";
                } else if (cmdLine.acceptAndRemoveNoDuplicates("--remove-group")) {
                    lastOption = "--remove-group";
                } else if (cmdLine.acceptAndRemoveNoDuplicates("--add-right")) {
                    lastOption = "--add-right";
                } else if (cmdLine.acceptAndRemoveNoDuplicates("--remove-right")) {
                    lastOption = "--remove-right";
                } else if (cmdLine.acceptAndRemoveNoDuplicates("--mapped-user")) {
                    lastOption = "--mapped-user";
                } else if (cmdLine.acceptAndRemoveNoDuplicates("--password")) {
                    lastOption = "--password";
                } else {
                    if (lastOption.equals("--add-group")) {
                        String a = cmdLine.readNonOptionOrError(new DefaultNonOption("Group")).getString();
                        if (u != null && cmdLine.isExecMode()) {
                            u.addGroup(a);
                        }
                    } else if (lastOption.equals("--remove-group")) {
                        String a = cmdLine.readNonOptionOrError(new GroupNonOption("Group", u)).getString();
                        if (u != null && cmdLine.isExecMode()) {
                            u.removeGroup(a);
                        }
                    } else if (lastOption.equals("--add-right")) {
                        String a = cmdLine.readNonOptionOrError(new RightNonOption("Right", u, false)).getString();
                        if (u != null && cmdLine.isExecMode()) {
                            u.addRight(a);
                        }
                    } else if (lastOption.equals("--remove-right")) {
                        String a = cmdLine.readNonOptionOrError(new RightNonOption("Right", u, true)).getString();
                        if (u != null && cmdLine.isExecMode()) {
                            u.removeRight(a);
                        }
                    } else if (lastOption.equals("--mapped-user")) {
                        String a = cmdLine.readNonOptionOrError(new DefaultNonOption("MappedUser")).getString();
                        if (u != null && cmdLine.isExecMode()) {
                            u.setMappedUser(a);
                        }
                    } else if (lastOption.equals("--password")) {
                        String pwd = (cmdLine.readNonOptionOrError(new DefaultNonOption("Password")).getString());
                        if (cmdLine.isExecMode()) {
                            if (repository == null) {
                                context.getValidWorkspace().getConfig().setSecurity(u);
                                context.getValidWorkspace().setUserCredentials(user, pwd);
                            } else {
                                repository.getConfig().setSecurity(u);
                                repository.setUserCredentials(user, pwd);
                            }
                        }
                    } else {
                        cmdLine.requireEmpty();
                    }
                }
            }
            if (cmdLine.isExecMode()) {
                if (repository == null) {
                    context.getValidWorkspace().getConfig().setSecurity(u);
                } else {
                    repository.getConfig().setSecurity(u);
                }
                trySave(context, context.getValidWorkspace(),repository, autoSave, null);
            }
            return true;

        } else if (cmdLine.acceptAndRemove("unsecure")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.acceptAndRemove("--repo", "-r")) {
                    repository = context.getValidWorkspace().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString());
                }
            }
            //unsecure-box
            if (cmdLine.isExecMode()) {
                String adminPassword=null;
                if(!context.getWorkspace().isAdmin()){
                    adminPassword = context.getTerminal().readPassword("Enter password : ");
                }
                if(context.getValidWorkspace().switchUnsecureMode(adminPassword)){
                    context.getTerminal().getOut().drawln("<<unsecure box activated.Anonymous has all rights.>>");
                }else{
                    context.getTerminal().getOut().drawln("<<unsecure box is already activated.>>");
                }
            }
            trySave(context, context.getValidWorkspace(),repository, autoSave, cmdLine);
            return true;
        } else if (cmdLine.acceptAndRemove("secure")) {
            String adminPassword=null;
            if(!context.getWorkspace().isAdmin()){
                adminPassword = context.getTerminal().readPassword("Enter password : ");
            }
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.acceptAndRemove("--repo", "-r")) {
                    repository = context.getValidWorkspace().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString());
                }
            }
            //secure-box
            if (cmdLine.isExecMode()) {
                if(context.getValidWorkspace().switchSecureMode(adminPassword)){
                    context.getTerminal().getOut().drawln("\"\"secure box activated.\"\"");
                }else{
                    context.getTerminal().getOut().drawln("\"\"secure box already activated.\"\"");
                }
            }
            trySave(context, context.getValidWorkspace(),repository, autoSave,cmdLine);
            return true;
        }
        return false;
    }

    private boolean trySave(NutsCommandContext context, NutsWorkspace workspace, NutsRepository repository, Boolean save, CmdLine cmdLine) throws IOException {
        if(save==null) {
            if (cmdLine == null || cmdLine.isExecMode()) {
                if (repository != null) {
                    save = Boolean.parseBoolean(repository.getEnv("autosave", "false", true));
                } else {
                    save = Boolean.parseBoolean(context.getValidWorkspace().getConfig().getEnv("autosave", "false"));
                }
            }else {
                save = false;
            }
        }else{
            save=false;
        }
        if (cmdLine != null) {
            while (!cmdLine.isEmpty()) {
                if (cmdLine.acceptAndRemove("--save")) {
                    save = true;
                } else {
                    cmdLine.requireEmpty();
                }
            }
        }
        if (save) {
            if (cmdLine == null || cmdLine.isExecMode()) {
                if (repository == null) {
                    workspace.save();
                    context.getTerminal().getOut().drawln("<<workspace saved.>>");
                } else {
                    context.getTerminal().getOut().drawln("<<repository "+repository.getRepositoryId()+" saved.>>");
                    repository.save();
                }
            }
        }
        return save;
    }

    private boolean processImportCommands(CmdLine cmdLine, Boolean autoSave, NutsCommandContext context) throws IOException, LoginException {
        if (cmdLine.acceptAndRemove("list imports", "li")) {
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                for (String imp : (context.getValidWorkspace().getConfig().getImports())) {
                    context.getTerminal().getOut().println(imp);
                }
            }
            return true;
        } else if (cmdLine.acceptAndRemove("clear imports", "ci")) {
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                context.getValidWorkspace().getConfig().removeAllImports();
                trySave(context,context.getValidWorkspace(),null,autoSave,cmdLine);
            }
            return true;
        } else if (cmdLine.acceptAndRemove("import", "ia")) {
            do {
                String a = cmdLine.readNonOptionOrError(new DefaultNonOption("Import")).getString();
                if (cmdLine.isExecMode()) {
                    context.getValidWorkspace().getConfig().addImport(a);
                }
            } while (!cmdLine.isEmpty());
            if (cmdLine.isExecMode()) {
                trySave(context,context.getValidWorkspace(),null,autoSave,cmdLine);
            }
            return true;
        } else if (cmdLine.acceptAndRemove("unimport", "ir")) {
            while (!cmdLine.isEmpty()) {
                String ii = cmdLine.readNonOptionOrError(new DefaultNonOption("Import")).getString();
                if (cmdLine.isExecMode()) {
                    context.getValidWorkspace().getConfig().removeImport(ii);
                }
            }
            if (cmdLine.isExecMode()) {
                trySave(context,context.getValidWorkspace(),null,autoSave,cmdLine);
            }
            return true;
        }
        return false;
    }

    private boolean processExtensionCommands(CmdLine cmdLine, Boolean autoSave, NutsCommandContext context) throws IOException, LoginException {
        if(autoSave==null){
            autoSave=false;
        }
        if (cmdLine.acceptAndRemove("add extension", "ax")) {
            String extensionId = cmdLine.readNonOptionOrError(new ExtensionNonOption("ExtensionNutsId", context)).getString();
            if (cmdLine.isExecMode()) {
                context.getValidWorkspace().addExtension(extensionId, context.getSession());
            }
            while (!cmdLine.isEmpty()) {
                extensionId = cmdLine.readNonOptionOrError(new ExtensionNonOption("ExtensionNutsId", context)).getString();
                if (cmdLine.isExecMode()) {
                    context.getValidWorkspace().addExtension(extensionId, context.getSession());
                }
            }
            if (cmdLine.isExecMode()) {
                trySave(context,context.getValidWorkspace(),null,autoSave,cmdLine);
            }
            return true;
        } else if (cmdLine.acceptAndRemove("list extensions", "lx")) {
            if (cmdLine.isExecMode()) {
                for (NutsWorkspaceExtension extension : context.getValidWorkspace().getExtensions()) {
                    NutsDescriptor desc = context.getValidWorkspace().fetchDescriptor(extension.getWiredId().toString(), false, context.getSession());
                    String extDesc = StringUtils.trim(desc.getName());
                    if (!extDesc.isEmpty()) {
                        extDesc = " : " + extDesc;
                    }
                    if (!extension.getId().equals(extension.getWiredId())) {
                        context.getTerminal().getOut().println(extension.getId() + " (" + extension.getWiredId() + ")" + extDesc);
                    } else {
                        context.getTerminal().getOut().println(extension.getId() + extDesc);
                    }
                }
            }
            return true;
        } else if (cmdLine.acceptAndRemove("list extension points", "lxp")) {
            if (cmdLine.isExecMode()) {
                for (Class extension : context.getValidWorkspace().getFactory().getExtensionPoints()) {
                    context.getTerminal().getOut().drawln("[["+extension.getName()+"]]:");
                    for (Class impl : context.getValidWorkspace().getFactory().getExtensionTypes(extension)) {
                        context.getTerminal().getOut().drawln("\t"+impl.getName());
                    }
                    for (Object impl : context.getValidWorkspace().getFactory().getExtensionObjects(extension)) {
                        if(impl!=null) {
                            context.getTerminal().getOut().drawln("\t" + impl.getClass().getName() + " :: " + impl);
                        }else{
                            context.getTerminal().getOut().drawln("\tnull");
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean processWorkspaceCommands(CmdLine cmdLine, Boolean autoSave, NutsCommandContext context) throws IOException, LoginException {
        if (cmdLine.acceptAndRemove("create workspace", "cw")) {
            boolean ignoreIdFound = false;
            boolean save = false;
            String archetype = null;
            String login = null;
            String password = null;
            while (!cmdLine.isEmpty()) {
                if (cmdLine.acceptAndRemoveNoDuplicates("-i", "--ignore")) {
                    ignoreIdFound = true;
                } else if (cmdLine.acceptAndRemoveNoDuplicates("-s", "--save")) {
                    save = true;
                } else if (cmdLine.acceptAndRemoveNoDuplicates("-h", "--archetype")) {
                    archetype = cmdLine.readNonOptionOrError(new ArchitectureNonOption("Archetype", context)).getStringOrError();
                } else if (cmdLine.acceptAndRemoveNoDuplicates("-u", "--login")) {
                    login = cmdLine.readNonOptionOrError(new DefaultNonOption("Login")).getStringOrError();
                } else if (cmdLine.acceptAndRemoveNoDuplicates("-x", "--password")) {
                    password = cmdLine.readNonOptionOrError(new DefaultNonOption("Password")).getStringOrError();
                } else {
                    String ws = cmdLine.readNonOptionOrError(new DefaultNonOption("NewWorkspaceName")).getString();
                    NutsWorkspace workspace = context.getWorkspace().openWorkspace(
                            ws,
                            new NutsWorkspaceCreateOptions()
                                    .setArchetype(archetype)
                                    .setCreateIfNotFound(true)
                                    .setSaveIfCreated(save)
                                    .setCreateIfNotFound(ignoreIdFound),
                            context.getSession()
                    );
                    if (!StringUtils.isEmpty(login)) {
                        workspace.login(login, password);
                    }
                    trySave(context,workspace,null,autoSave,cmdLine);
                    cmdLine.requireEmpty();

                }
            }
            return true;
        } else if (cmdLine.acceptAndRemove("set workspace", "sw")) {
            boolean createIfNotFound = false;
            boolean save = true;
            String login = null;
            String password = null;
            String archetype = null;
            int wsCount = 0;
            while (wsCount == 0 || !cmdLine.isEmpty()) {
                if (cmdLine.acceptAndRemoveNoDuplicates("-c", "--create")) {
                    createIfNotFound = true;
                } else if (cmdLine.acceptAndRemoveNoDuplicates("-s", "--save")) {
                    save = true;
                } else if (cmdLine.acceptAndRemoveNoDuplicates("-s", "--nosave")) {
                    save = false;
                } else if (cmdLine.acceptAndRemoveNoDuplicates("-h", "--archetype")) {
                    archetype = cmdLine.readNonOptionOrError(new ArchitectureNonOption("Archetype", context)).getStringOrError();
                } else if (cmdLine.acceptAndRemoveNoDuplicates("-u", "--login")) {
                    login = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getStringOrError();
                } else if (cmdLine.acceptAndRemoveNoDuplicates("-x", "--password")) {
                    password = cmdLine.readNonOptionOrError(new DefaultNonOption("Password")).getStringOrError();
                } else {
                    String ws = cmdLine.readNonOptionOrError(new FolderNonOption("WorkspacePath")).getString();
                    wsCount++;
                    cmdLine.requireEmpty();
                    if (cmdLine.isExecMode()) {
                        NutsWorkspace workspace = context.getValidWorkspace().openWorkspace(ws,
                                new NutsWorkspaceCreateOptions()
                                        .setArchetype(archetype)
                                        .setSaveIfCreated(save)
                                        .setCreateIfNotFound(createIfNotFound),
                                context.getSession()
                        );
                        if (!StringUtils.isEmpty(login)) {
                            workspace.login(login, password);
                        }
                        context.setWorkspace(workspace);
                        trySave(context,workspace,null,autoSave,cmdLine);
                    }
                }
            }
            cmdLine.requireEmpty();
            return true;
        } else if (cmdLine.acceptAndRemove("save workspace", "save", "sw")) {
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                trySave(context,context.getValidWorkspace(),null,autoSave,cmdLine);
            }
            return true;
        }
        return false;
    }
}
