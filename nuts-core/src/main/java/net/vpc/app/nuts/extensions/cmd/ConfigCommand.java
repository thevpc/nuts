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
import net.vpc.app.nuts.extensions.core.NutsSecurityEntityConfigImpl;
import net.vpc.app.nuts.extensions.util.CoreLogUtils;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.app.nuts.extensions.util.Ref;

import java.io.File;
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

    public int exec(String[] args, NutsCommandContext context) {
        NutsCommandAutoComplete autoComplete = context.getAutoComplete();
        Boolean autoSave = null;
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        boolean empty = true;
        do {
            if (cmdLine.readOnce("--save")) {
                autoSave = true;
                empty = false;
                continue;
            }
            if (cmdLine.readOnce("-h", "-?", "--help")) {
                empty = false;
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
                    out.println("set loglevel verbose|fine|finer|finest|error|severe|config|all|none");
                    out.println("");
                    out.println("type 'help config' for more detailed help");
                }
                continue;
            }
            if (cmdLine.readOnce("show location")) {
                empty = false;
                NutsSession session = context.getSession();
                session.getTerminal().getOut().drawln(context.getValidWorkspace().getWorkspaceLocation());
                continue;
            }
            if (cmdLine.readOnce("check-updates")) {
                empty = false;
                NutsSession session = context.getSession();
                context.getValidWorkspace().checkWorkspaceUpdates(false, null, session);
                continue;
            }
            if (cmdLine.readOnce("update")) {
                empty = false;
                NutsSession session = context.getSession();
                NutsFile newVersion = null;
                try {
                    newVersion = context.getValidWorkspace().updateWorkspace(session);
                } catch (Exception ex) {
                    //not found
                }
                if (newVersion != null) {
                    session.getTerminal().getOut().drawln("Workspace updated to [[" + newVersion.getId() + "]]");
                }
                continue;
            }
            if (processWorkspaceCommands(cmdLine, autoSave, context)) {
                empty = false;
                continue;
            }
            if (processRepositoryCommands(cmdLine, autoSave, context)) {
                empty = false;
                continue;
            }
            if (processExtensionCommands(cmdLine, autoSave, context)) {
                empty = false;
                continue;
            }
            if (processImportCommands(cmdLine, autoSave, context)) {
                empty = false;
                continue;
            }
            if (processUserCommands(cmdLine, null, autoSave, context)) {
                empty = false;
                continue;
            }
            if (processArchetypeCommands(cmdLine, autoSave, context)) {
                empty = false;
                continue;
            }
            if (processLogLevelCommands(cmdLine, autoSave, context)) {
                empty = false;
                continue;
            }
            if (processDescCommands(cmdLine, autoSave, context)) {
                empty = false;
                continue;
            }
            if (!cmdLine.isExecMode()) {
                return -1;
            }
            if (!cmdLine.isEmpty()) {
                NutsPrintStream out = context.getTerminal().getErr();
                out.println("Unexpected " + cmdLine.get(0));
                out.println("type for more help : config -h");
                return 1;
            }
            break;
        } while (!cmdLine.isEmpty());
        if (empty) {
            NutsPrintStream out = context.getTerminal().getErr();
            out.println("Missing config command");
            out.println("type for more help : config -h");
            return 1;
        }
        return 0;
    }

    private void showRepo(NutsCommandContext context, NutsRepository repository, String prefix) {
        boolean enabled = repository.getWorkspace().getConfig().getRepository(repository.getRepositoryId()).isEnabled();
        String disabledString = enabled ? "" : " <DISABLED>";
        context.getTerminal().getOut().print(prefix);
        context.getTerminal().getOut().print(enabled ? NutsPrintColors.BLUE : NutsPrintColors.RED, repository.getRepositoryId() + disabledString);
        context.getTerminal().getOut().print(" : " + repository);
        context.getTerminal().getOut().println();

    }

    private void showRepoTree(NutsCommandContext context, NutsRepository repository, String prefix) {
        showRepo(context, repository, prefix);
        String prefix1 = prefix + "  ";
        for (NutsRepository c : repository.getMirrors()) {
            showRepoTree(context, c, prefix1);
        }
    }

    private boolean processArchetypeCommands(CmdLine cmdLine, Boolean autoSave, NutsCommandContext context) {
        if (cmdLine.read("list archetypes", "la")) {
            if (cmdLine.isExecMode()) {
                for (String archetype : context.getValidWorkspace().getAvailableArchetypes()) {
                    context.getTerminal().getOut().println(archetype);
                }
            }
            return true;
        }
        return false;
    }

    private boolean processDescCommands(CmdLine cmdLine, Boolean autoSave, NutsCommandContext context) {
        boolean newDesc = false;
        String file = null;
        boolean save = false;
        final Ref<NutsDescriptor> desc = new Ref<>();
        if (cmdLine.read("new descriptor", "nd")) {
            newDesc = true;
        } else if (cmdLine.read("update descriptor", "ud")) {
            newDesc = false;
        } else {
            return false;
        }

        List<Runnable> all = new ArrayList<>();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.read("-executable")) {
                final boolean value = cmdLine.readNonOptionOrError(new ValueNonOption("executable", null, "true", "false")).getBoolean();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().setExecutable(value));
                    }
                });
            } else if (cmdLine.read("-ext")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("ext", null, "jar")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().setExt(value));
                    }
                });
            } else if (cmdLine.read("-packaging")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("packaging", null, "jar")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().setPackaging(value));
                    }
                });
            } else if (cmdLine.read("-name")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("name", null, "my-name")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().setId(desc.get().getId().setName(value)));
                    }
                });
            } else if (cmdLine.read("-group")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("group", null, "my-group")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().setId(desc.get().getId().setGroup(value)));
                    }
                });
            } else if (cmdLine.read("-id")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("id", null, "my-group:my-name#1.0")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().setId(context.getValidWorkspace().parseNutsId(value)));
                    }
                });

            } else if (cmdLine.read("-add-os")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().addOs(value));
                    }
                });
            } else if (cmdLine.read("-remove-os")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().removeOs(value));
                    }
                });

            } else if (cmdLine.read("-add-osdist")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().addOsdist(value));
                    }
                });
            } else if (cmdLine.read("-remove-osdist")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().removeOsdist(value));
                    }
                });

            } else if (cmdLine.read("-add-platform")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().addPlatform(value));
                    }
                });
            } else if (cmdLine.read("-remove-platform")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().removePlatform(value));
                    }
                });

            } else if (cmdLine.read("-add-arch")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().addArch(value));
                    }
                });
            } else if (cmdLine.read("-remove-arch")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().removeArch(value));
                    }
                });
            } else if (cmdLine.read("-add-property")) {
                String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                final String[] nv = CoreNutsUtils.splitNameAndValue(value);
                if (nv != null) {
                    all.add(new Runnable() {
                        @Override
                        public void run() {
                            desc.set(desc.get().addProperty(nv[0], nv[1]));
                        }
                    });
                }
            } else if (cmdLine.read("-remove-property")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("os", null, "os")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().removeProperty(value));
                    }
                });

            } else if (cmdLine.read("-add-dependency")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("dependency", null, "my-group:my-name#1.0")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().addDependency(CoreNutsUtils.parseNutsDependency(value)));
                    }
                });
            } else if (cmdLine.read("-remove-dependency")) {
                final String value = cmdLine.readNonOptionOrError(new ValueNonOption("dependency", null, "my-group:my-name#1.0")).getString();
                all.add(new Runnable() {
                    @Override
                    public void run() {
                        desc.set(desc.get().removeDependency(CoreNutsUtils.parseNutsDependency(value)));
                    }
                });
            } else if (cmdLine.read("-file")) {
                file = cmdLine.readNonOptionOrError(new FileNonOption("file")).getString();
            } else if (cmdLine.read("-save")) {
                save = cmdLine.readNonOptionOrError(new ValueNonOption("save", null, "true", "false")).getBoolean();
            } else {
                if (!cmdLine.isExecMode()) {
                    throw new NutsIllegalArgumentsException("Unsupported");
                }
            }
        }
        if (cmdLine.isExecMode()) {
            if (newDesc) {
                desc.set(CoreNutsUtils.createNutsDescriptor());
            } else {
                if (file != null) {
                    desc.set(CoreNutsUtils.parseNutsDescriptor(new File(file)));
                } else {
                    if (cmdLine.isExecMode()) {
                        throw new NutsIllegalArgumentsException("-file missing");
                    }
                }
            }

            for (Runnable r : all) {
                r.run();
            }
            if (save) {
                if (file != null) {
                    desc.get().write(new File(file));
                } else {
                    if (cmdLine.isExecMode()) {
                        throw new NutsIllegalArgumentsException("-file missing");
                    }
                }
            } else {
                context.getTerminal().getOut().println(desc.get().toString(true));
            }
        }
        return true;
    }

    private boolean processLogLevelCommands(CmdLine cmdLine, Boolean autoSave, NutsCommandContext context) {
        if (cmdLine.read("set loglevel", "sll")) {
            if (cmdLine.read("verbose", "finest")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.FINEST);
                }
            } else if (cmdLine.read("fine")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.FINE);
                }
            } else if (cmdLine.read("finer")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.FINER);
                }
            } else if (cmdLine.read("info")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.INFO);
                }
            } else if (cmdLine.read("warning")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.WARNING);
                }
            } else if (cmdLine.read("severe", "error")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.SEVERE);
                }
            } else if (cmdLine.read("config")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.CONFIG);
                }
            } else if (cmdLine.read("off")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.OFF);
                }
            } else if (cmdLine.read("all")) {
                if (cmdLine.isExecMode()) {
                    CoreLogUtils.setLevel(Level.ALL);
                }
            } else {
                if (cmdLine.isExecMode()) {
                    throw new NutsIllegalArgumentsException("Invalid loglevel");
                }
            }
            cmdLine.requireEmpty();
            return true;
        } else if (cmdLine.read("get loglevel")) {
            if (cmdLine.isExecMode()) {
                Logger rootLogger = Logger.getLogger("");
                System.out.println(rootLogger.getLevel().toString());
            }
        }
        return false;
    }

    private boolean processRepositoryCommands(CmdLine cmdLine, Boolean autoSave, NutsCommandContext context) {
        NutsWorkspace validWorkspace = context.getValidWorkspace();
        if (cmdLine.read("save repository", "sw")) {
            String repositoryId = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString();
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                trySave(context, validWorkspace, validWorkspace.findRepository(repositoryId), true, null);
            }
            return true;

        } else if (cmdLine.read("create repo", "cr")) {
            String repositoryId = cmdLine.readNonOptionOrError(new DefaultNonOption("NewRepositoryId")).getString();
            String location = cmdLine.readNonOption(new DefaultNonOption("RepositoryLocation")).getString();
            String repoType = cmdLine.readNonOption(new RepositoryNonOption("RepositoryType", context.getValidWorkspace())).getString();
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                NutsRepository repository = validWorkspace.openRepository(repositoryId, null, location, repoType, true);
                trySave(context, validWorkspace, repository, autoSave, null);
            }
            return true;

        } else if (cmdLine.read("add repo", "ar")) {
            boolean proxy = false;
            boolean pattern = false;
            while (!cmdLine.isEmpty()) {
                if (cmdLine.readOnce("-p","--proxy")) {
                    proxy = true;
                } else if (cmdLine.readOnce("-P","--pattern")) {
                    pattern = true;
                } else {
                    final Map<String, NutsRepositoryDefinition> repoPatterns = new HashMap<String, NutsRepositoryDefinition>();
                    for (NutsRepositoryDefinition repoPattern : context.getValidWorkspace().getDefaultRepositories()) {
                        repoPatterns.put(repoPattern.getId(), repoPattern);
                    }
                    String repositoryId = cmdLine.readNonOptionOrError(new DefaultNonOption("RepositoryId") {
                        @Override
                        public List<NutsArgumentCandidate> getValues() {
                            ArrayList<NutsArgumentCandidate> arrayList = new ArrayList<>();
                            for (Map.Entry<String, NutsRepositoryDefinition> e : repoPatterns.entrySet()) {
                                arrayList.add(new DefaultNutsArgumentCandidate(e.getKey()));
                            }
                            arrayList.add(new DefaultNutsArgumentCandidate("<RepositoryId>"));
                            return arrayList;
                        }

                    }).getString();
                    String location = null;
                    String repoType = null;
                    if (pattern) {
                        NutsRepositoryDefinition found = repoPatterns.get(repositoryId);
                        if (found == null) {
                            throw new NutsIllegalArgumentsException("Repository Pattern not found " + repositoryId + ". Try one of " + repoPatterns.keySet());
                        }
                        location = found.getLocation();
                        repoType = found.getType();
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
                        trySave(context, validWorkspace, repo, autoSave, null);
                        trySave(context, validWorkspace, null, autoSave, null);
                    }
                    cmdLine.requireEmpty();
                }
            }
            return true;

        } else if (cmdLine.read("remove repo", "rr")) {
            String locationOrRepositoryId = cmdLine.readNonOptionOrError(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString();
            if (cmdLine.isExecMode()) {
                validWorkspace.removeRepository(locationOrRepositoryId);
                trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;

        } else if (cmdLine.read("list repos", "lr")) {
            if (cmdLine.isExecMode()) {
                for (NutsRepository repository : validWorkspace.getRepositories()) {
                    showRepo(context, repository, "");
                }
            }
            return true;

        } else if (cmdLine.read("tree repos", "tr")) {
            if (cmdLine.isExecMode()) {

                for (NutsRepository repository : validWorkspace.getRepositories()) {
                    showRepoTree(context, repository, "");
                }
            }
            return true;

        } else if (cmdLine.read("enable repo", "er")) {
            String localId = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString();
            if (cmdLine.isExecMode()) {

                NutsRepository editedRepo = validWorkspace.findRepository(localId);
                editedRepo.getWorkspace().getConfig().getRepository(editedRepo.getRepositoryId()).setEnabled(true);
                trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.read("disable repo", "rr")) {
            String localId = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString();
            if (cmdLine.isExecMode()) {
                NutsRepository editedRepo = validWorkspace.findRepository(localId);
                editedRepo.getWorkspace().getConfig().getRepository(editedRepo.getRepositoryId()).setEnabled(false);
                trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.read("edit repo", "er")) {
            String repoId = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoyId", context.getValidWorkspace())).getString();
            if (cmdLine.read("add repo", "ar")) {
                String repositoryId = cmdLine.readNonOptionOrError(new DefaultNonOption("NewRepositoryId")).getString();
                String location = cmdLine.readNonOptionOrError(new FolderNonOption("RepositoryLocation")).getString();
                String repoType = cmdLine.readNonOption(new RepositoryTypeNonOption("RepositoryType", context)).getString();

                NutsRepository editedRepo = validWorkspace.findRepository(repoId);
                NutsRepository repo = editedRepo.addMirror(repositoryId, location, repoType, true);
                trySave(context, validWorkspace, editedRepo, autoSave, null);
                trySave(context, validWorkspace, repo, autoSave, null);

            } else if (cmdLine.read("remove repo", "rr")) {
                String location = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString();
                NutsRepository editedRepo = validWorkspace.findRepository(repoId);
                editedRepo.removeMirror(location);
                trySave(context, validWorkspace, editedRepo, autoSave, null);

            } else if (cmdLine.read("enable", "rr")) {
                NutsRepository editedRepo = validWorkspace.findRepository(repoId);
                editedRepo.getWorkspace().getConfig().getRepository(editedRepo.getRepositoryId()).setEnabled(true);
                trySave(context, validWorkspace, editedRepo, autoSave, null);

            } else if (cmdLine.read("disable", "rr")) {
                NutsRepository editedRepo = validWorkspace.findRepository(repoId);
                editedRepo.getWorkspace().getConfig().getRepository(editedRepo.getRepositoryId()).setEnabled(true);
                trySave(context, validWorkspace, editedRepo, autoSave, null);
            } else if (cmdLine.read("list repos", "lr")) {
                NutsRepository editedRepo = validWorkspace.findRepository(repoId);
                NutsRepository[] linkRepositories = editedRepo.getMirrors();
                context.getTerminal().getOut().println(linkRepositories.length + " sub repositories.");
                for (NutsRepository repository : linkRepositories) {
                    showRepo(context, repository, "");
                }
            } else if (cmdLine.readOnce("-h", "-?", "--help")) {
                context.getTerminal().getOut().println("edit repository " + repoId + " add repo ...");
                context.getTerminal().getOut().println("edit repository " + repoId + " remove repo ...");
                context.getTerminal().getOut().println("edit repository " + repoId + " list repos ...");
            } else {
                NutsRepository editedRepo = validWorkspace.findRepository(repoId);
                if (processUserCommands(cmdLine, editedRepo, autoSave, context)) {
                    //okkay
                } else {
                    throw new NutsIllegalArgumentsException("Unsupported command " + cmdLine);
                }
            }
            return true;
        }
        return false;
    }

    private boolean processUserCommands(CmdLine cmdLine, NutsRepository editedRepo, Boolean autoSave, NutsCommandContext context) {
        if (cmdLine.read("add user", "au")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
                    repository = context.getValidWorkspace().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString());
                }
            }
            if (repository == null) {
                NutsSecurityEntityConfig u = new NutsSecurityEntityConfigImpl();
                String user = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getString();
                String password = cmdLine.readNonOption(new DefaultNonOption("Password")).getString();
                if (cmdLine.isExecMode()) {
                    u.setUser(user);
                    context.getValidWorkspace().getConfig().setSecurity(u);
                    context.getValidWorkspace().setUserCredentials(u.getUser(), password);
                }
            } else {
                NutsSecurityEntityConfig u = new NutsSecurityEntityConfigImpl();
                String user = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getString();
                String mappedUser = cmdLine.readNonOption(new DefaultNonOption("MappedUser")).getString();
                String password = cmdLine.readNonOption(new DefaultNonOption("Password")).getString();
                if (cmdLine.isExecMode()) {
                    u.setUser(user);
                    u.setMappedUser(mappedUser);
                    repository.getConfig().setSecurity(u);
                    repository.setUserCredentials(u.getUser(), password);
                }
            }
            if (cmdLine.isExecMode()) {
                trySave(context, context.getValidWorkspace(), repository, autoSave, null);
            }
            return true;
        } else if (cmdLine.read("list users", "lu")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
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
                    if (!CoreStringUtils.isEmpty(u.getMappedUser())) {
                        context.getTerminal().getOut().println("   Mapper to  : " + u.getMappedUser());
                    }
                    if (CoreStringUtils.isEmpty(u.getCredentials())) {
                        context.getTerminal().getOut().println("   Password   : None");
                    } else {
                        context.getTerminal().getOut().println("   Password   : Set");
                    }
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
                    repository = context.getValidWorkspace().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString());
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

        } else if (cmdLine.read("edit user", "eu")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
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
                    throw new NutsIllegalArgumentsException("No such user " + user);
                }
            }
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
                            if (u != null && cmdLine.isExecMode()) {
                                u.addGroup(a);
                            }
                            break;
                        }
                        case "--remove-group": {
                            String a = cmdLine.readNonOptionOrError(new GroupNonOption("Group", u)).getString();
                            if (u != null && cmdLine.isExecMode()) {
                                u.removeGroup(a);
                            }
                            break;
                        }
                        case "--add-right": {
                            String a = cmdLine.readNonOptionOrError(new RightNonOption("Right", u, false)).getString();
                            if (u != null && cmdLine.isExecMode()) {
                                u.addRight(a);
                            }
                            break;
                        }
                        case "--remove-right": {
                            String a = cmdLine.readNonOptionOrError(new RightNonOption("Right", u, true)).getString();
                            if (u != null && cmdLine.isExecMode()) {
                                u.removeRight(a);
                            }
                            break;
                        }
                        case "--mapped-user": {
                            String a = cmdLine.readNonOptionOrError(new DefaultNonOption("MappedUser")).getString();
                            if (u != null && cmdLine.isExecMode()) {
                                u.setMappedUser(a);
                            }
                            break;
                        }
                        case "--password":
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
                            break;
                        default:
                            cmdLine.requireEmpty();
                            break;
                    }
                }
            }
            if (cmdLine.isExecMode()) {
                if (repository == null) {
                    context.getValidWorkspace().getConfig().setSecurity(u);
                } else {
                    repository.getConfig().setSecurity(u);
                }
                trySave(context, context.getValidWorkspace(), repository, autoSave, null);
            }
            return true;

        } else if (cmdLine.read("unsecure")) {
            NutsRepository repository = null;
            if (editedRepo != null) {
                repository = editedRepo;
            } else {
                if (cmdLine.read("--repo", "-r")) {
                    repository = context.getValidWorkspace().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString());
                }
            }
            //unsecure-box
            if (cmdLine.isExecMode()) {
                String adminPassword = null;
                if (!context.getWorkspace().isAdmin()) {
                    adminPassword = context.getTerminal().readPassword("Enter password : ");
                }
                if (context.getValidWorkspace().switchUnsecureMode(adminPassword)) {
                    context.getTerminal().getOut().drawln("<<unsecure box activated.Anonymous has all rights.>>");
                } else {
                    context.getTerminal().getOut().drawln("<<unsecure box is already activated.>>");
                }
            }
            trySave(context, context.getValidWorkspace(), repository, autoSave, cmdLine);
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
                    repository = context.getValidWorkspace().findRepository(cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString());
                }
            }
            //secure-box
            if (cmdLine.isExecMode()) {
                if (context.getValidWorkspace().switchSecureMode(adminPassword)) {
                    context.getTerminal().getOut().drawln("\"\"secure box activated.\"\"");
                } else {
                    context.getTerminal().getOut().drawln("\"\"secure box already activated.\"\"");
                }
            }
            trySave(context, context.getValidWorkspace(), repository, autoSave, cmdLine);
            return true;
        }
        return false;
    }

    private boolean trySave(NutsCommandContext context, NutsWorkspace workspace, NutsRepository repository, Boolean save, CmdLine cmdLine) {
        if (save == null) {
            if (cmdLine == null || cmdLine.isExecMode()) {
                if (repository != null) {
                    save = Boolean.parseBoolean(repository.getEnv("autosave", "false", true));
                } else {
                    save = Boolean.parseBoolean(context.getValidWorkspace().getConfig().getEnv("autosave", "false"));
                }
            } else {
                save = false;
            }
        } else {
            save = false;
        }
        if (cmdLine != null) {
            while (!cmdLine.isEmpty()) {
                if (cmdLine.read("--save")) {
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
                    context.getTerminal().getOut().drawln("<<repository " + repository.getRepositoryId() + " saved.>>");
                    repository.save();
                }
            }
        }
        return save;
    }

    private boolean processImportCommands(CmdLine cmdLine, Boolean autoSave, NutsCommandContext context) {
        if (cmdLine.read("list imports", "li")) {
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                for (String imp : (context.getValidWorkspace().getConfig().getImports())) {
                    context.getTerminal().getOut().println(imp);
                }
            }
            return true;
        } else if (cmdLine.read("clear imports", "ci")) {
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                context.getValidWorkspace().getConfig().removeAllImports();
                trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.read("import", "ia")) {
            do {
                String a = cmdLine.readNonOptionOrError(new DefaultNonOption("Import")).getString();
                if (cmdLine.isExecMode()) {
                    context.getValidWorkspace().getConfig().addImport(a);
                }
            } while (!cmdLine.isEmpty());
            if (cmdLine.isExecMode()) {
                trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.read("unimport", "ir")) {
            while (!cmdLine.isEmpty()) {
                String ii = cmdLine.readNonOptionOrError(new DefaultNonOption("Import")).getString();
                if (cmdLine.isExecMode()) {
                    context.getValidWorkspace().getConfig().removeImport(ii);
                }
            }
            if (cmdLine.isExecMode()) {
                trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        }
        return false;
    }

    private boolean processExtensionCommands(CmdLine cmdLine, Boolean autoSave, NutsCommandContext context) {
        if (autoSave == null) {
            autoSave = false;
        }
        if (cmdLine.read("add extension", "ax")) {
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
                trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.read("list extensions", "lx")) {
            if (cmdLine.isExecMode()) {
                for (NutsWorkspaceExtension extension : context.getValidWorkspace().getExtensions()) {
                    NutsDescriptor desc = context.getValidWorkspace().fetchDescriptor(extension.getWiredId().toString(), false, context.getSession());
                    String extDesc = CoreStringUtils.trim(desc.getName());
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
        } else if (cmdLine.read("list extension points", "lxp")) {
            if (cmdLine.isExecMode()) {
                for (Class extension : context.getValidWorkspace().getFactory().getExtensionPoints()) {
                    context.getTerminal().getOut().drawln("[[" + extension.getName() + "]]:");
                    for (Class impl : context.getValidWorkspace().getFactory().getExtensionTypes(extension)) {
                        context.getTerminal().getOut().drawln("\t" + impl.getName());
                    }
                    for (Object impl : context.getValidWorkspace().getFactory().getExtensionObjects(extension)) {
                        if (impl != null) {
                            context.getTerminal().getOut().drawln("\t" + impl.getClass().getName() + " :: " + impl);
                        } else {
                            context.getTerminal().getOut().drawln("\tnull");
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean processWorkspaceCommands(CmdLine cmdLine, Boolean autoSave, NutsCommandContext context) {
        if (cmdLine.read("create workspace", "cw")) {
            boolean ignoreIdFound = false;
            boolean save = false;
            String archetype = null;
            String login = null;
            String password = null;
            while (!cmdLine.isEmpty()) {
                if (cmdLine.readOnce("-i", "--ignore")) {
                    ignoreIdFound = true;
                } else if (cmdLine.readOnce("-s", "--save")) {
                    save = true;
                } else if (cmdLine.readOnce("-h", "--archetype")) {
                    archetype = cmdLine.readNonOptionOrError(new ArchitectureNonOption("Archetype", context)).getStringOrError();
                } else if (cmdLine.readOnce("-u", "--login")) {
                    login = cmdLine.readNonOptionOrError(new DefaultNonOption("Login")).getStringOrError();
                } else if (cmdLine.readOnce("-x", "--password")) {
                    password = cmdLine.readNonOptionOrError(new DefaultNonOption("Password")).getStringOrError();
                } else {
                    String ws = cmdLine.readNonOptionOrError(new DefaultNonOption("NewWorkspaceName")).getString();
                    NutsWorkspace workspace = context.getWorkspace().openWorkspace(
                            ws,
                            new NutsWorkspaceCreateOptions()
                                    .setArchetype(archetype)
                                    .setCreateIfNotFound(true)
                                    .setSaveIfCreated(save)
                                    .setCreateIfNotFound(ignoreIdFound)
                    );
                    if (!CoreStringUtils.isEmpty(login)) {
                        workspace.login(login, password);
                    }
                    trySave(context, workspace, null, autoSave, cmdLine);
                    cmdLine.requireEmpty();

                }
            }
            return true;
        } else if (cmdLine.read("set workspace", "sw")) {
            boolean createIfNotFound = false;
            boolean save = true;
            String login = null;
            String password = null;
            String archetype = null;
            int wsCount = 0;
            while (wsCount == 0 || !cmdLine.isEmpty()) {
                if (cmdLine.readOnce("-c", "--create")) {
                    createIfNotFound = true;
                } else if (cmdLine.readOnce("-s", "--save")) {
                    save = true;
                } else if (cmdLine.readOnce("-s", "--nosave")) {
                    save = false;
                } else if (cmdLine.readOnce("-h", "--archetype")) {
                    archetype = cmdLine.readNonOptionOrError(new ArchitectureNonOption("Archetype", context)).getStringOrError();
                } else if (cmdLine.readOnce("-u", "--login")) {
                    login = cmdLine.readNonOptionOrError(new DefaultNonOption("Username")).getStringOrError();
                } else if (cmdLine.readOnce("-x", "--password")) {
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
                                        .setCreateIfNotFound(createIfNotFound)
                        );
                        if (!CoreStringUtils.isEmpty(login)) {
                            workspace.login(login, password);
                        }
                        context.setWorkspace(workspace);
                        trySave(context, workspace, null, autoSave, cmdLine);
                    }
                }
            }
            cmdLine.requireEmpty();
            return true;
        } else if (cmdLine.read("save workspace", "save", "sw")) {
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        }
        return false;
    }
}
