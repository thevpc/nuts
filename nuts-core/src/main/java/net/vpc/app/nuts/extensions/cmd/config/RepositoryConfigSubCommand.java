/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.cmd.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.vpc.app.nuts.NutsArgumentCandidate;
import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsIllegalArgumentsException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryDefinition;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.extensions.cmd.AbstractConfigSubCommand;
import net.vpc.app.nuts.extensions.cmd.ConfigCommand;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.cmd.cmdline.DefaultNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.DefaultNutsArgumentCandidate;
import net.vpc.app.nuts.extensions.cmd.cmdline.FolderNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.RepositoryNonOption;
import net.vpc.app.nuts.extensions.cmd.cmdline.RepositoryTypeNonOption;

/**
 *
 * @author vpc
 */
public class RepositoryConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CmdLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        NutsWorkspace validWorkspace = context.getValidWorkspace();
        if (cmdLine.read("save repository", "sw")) {
            String repositoryId = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString();
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                ConfigCommand.trySave(context, validWorkspace, validWorkspace.getRepositoryManager().findRepository(repositoryId), true, null);
            }
            return true;

        } else if (cmdLine.read("create repo", "cr")) {
            String repositoryId = cmdLine.readNonOptionOrError(new DefaultNonOption("NewRepositoryId")).getString();
            String location = cmdLine.readNonOption(new DefaultNonOption("RepositoryLocation")).getString();
            String repoType = cmdLine.readNonOption(new RepositoryNonOption("RepositoryType", context.getValidWorkspace())).getString();
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                NutsRepository repository = validWorkspace.getRepositoryManager().openRepository(repositoryId, null, location, repoType, true);
                ConfigCommand.trySave(context, validWorkspace, repository, autoSave, null);
            }
            return true;

        } else if (cmdLine.read("add repo", "ar")) {
            boolean proxy = false;
            boolean pattern = false;
            while (!cmdLine.isEmpty()) {
                if (cmdLine.readOnce("-p", "--proxy")) {
                    proxy = true;
                } else if (cmdLine.readOnce("-P", "--pattern")) {
                    pattern = true;
                } else {
                    final Map<String, NutsRepositoryDefinition> repoPatterns = new HashMap<String, NutsRepositoryDefinition>();
                    for (NutsRepositoryDefinition repoPattern : context.getValidWorkspace().getRepositoryManager().getDefaultRepositories()) {
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
                            repo = validWorkspace.getRepositoryManager().addProxiedRepository(repositoryId, location, repoType, true);
                        } else {
                            repo = validWorkspace.getRepositoryManager().addRepository(repositoryId, location, repoType, true);
                        }
                        context.getTerminal().getOut().printf("Repository added successfully\n");
                        ConfigCommand.trySave(context, validWorkspace, repo, autoSave, null);
                        ConfigCommand.trySave(context, validWorkspace, null, autoSave, null);
                    }
                    cmdLine.requireEmpty();
                }
            }
            return true;

        } else if (cmdLine.read("remove repo", "rr")) {
            String locationOrRepositoryId = cmdLine.readNonOptionOrError(new RepositoryNonOption("Repository", context.getValidWorkspace())).getString();
            if (cmdLine.isExecMode()) {
                validWorkspace.getRepositoryManager().removeRepository(locationOrRepositoryId);
                ConfigCommand.trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;

        } else if (cmdLine.read("list repos", "lr")) {
            if (cmdLine.isExecMode()) {
                for (NutsRepository repository : validWorkspace.getRepositoryManager().getRepositories()) {
                    config.showRepo(context, repository, "");
                }
            }
            return true;

        } else if (cmdLine.read("tree repos", "tr")) {
            if (cmdLine.isExecMode()) {

                for (NutsRepository repository : validWorkspace.getRepositoryManager().getRepositories()) {
                    config.showRepoTree(context, repository, "");
                }
            }
            return true;

        } else if (cmdLine.read("enable repo", "er")) {
            String localId = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString();
            if (cmdLine.isExecMode()) {

                NutsRepository editedRepo = validWorkspace.getRepositoryManager().findRepository(localId);
                editedRepo.setEnabled(true);
                ConfigCommand.trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.read("disable repo", "rr")) {
            String localId = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString();
            if (cmdLine.isExecMode()) {
                NutsRepository editedRepo = validWorkspace.getRepositoryManager().findRepository(localId);
                editedRepo.setEnabled(false);
                ConfigCommand.trySave(context, context.getValidWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.read("edit repo", "er")) {
            String repoId = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoyId", context.getValidWorkspace())).getString();
            if (cmdLine.read("add repo", "ar")) {
                String repositoryId = cmdLine.readNonOptionOrError(new DefaultNonOption("NewRepositoryId")).getString();
                String location = cmdLine.readNonOptionOrError(new FolderNonOption("RepositoryLocation")).getString();
                String repoType = cmdLine.readNonOption(new RepositoryTypeNonOption("RepositoryType", context)).getString();

                NutsRepository editedRepo = validWorkspace.getRepositoryManager().findRepository(repoId);
                NutsRepository repo = editedRepo.addMirror(repositoryId, location, repoType, true);
                ConfigCommand.trySave(context, validWorkspace, editedRepo, autoSave, null);
                ConfigCommand.trySave(context, validWorkspace, repo, autoSave, null);

            } else if (cmdLine.read("remove repo", "rr")) {
                String location = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString();
                NutsRepository editedRepo = validWorkspace.getRepositoryManager().findRepository(repoId);
                editedRepo.removeMirror(location);
                ConfigCommand.trySave(context, validWorkspace, editedRepo, autoSave, null);

            } else if (cmdLine.read("enable", "rr")) {
                NutsRepository editedRepo = validWorkspace.getRepositoryManager().findRepository(repoId);
                editedRepo.setEnabled(true);
                ConfigCommand.trySave(context, validWorkspace, editedRepo, autoSave, null);

            } else if (cmdLine.read("disable", "rr")) {
                NutsRepository editedRepo = validWorkspace.getRepositoryManager().findRepository(repoId);
                editedRepo.setEnabled(true);
                ConfigCommand.trySave(context, validWorkspace, editedRepo, autoSave, null);
            } else if (cmdLine.read("list repos", "lr")) {
                NutsRepository editedRepo = validWorkspace.getRepositoryManager().findRepository(repoId);
                NutsRepository[] linkRepositories = editedRepo.getMirrors();
                context.getTerminal().getOut().printf("%s sub repositories.\n",linkRepositories.length);
                for (NutsRepository repository : linkRepositories) {
                    config.showRepo(context, repository, "");
                }
            } else if (cmdLine.readOnce("-h", "-?", "--help")) {
                context.getTerminal().getOut().printf("edit repository %s add repo ...\n",repoId);
                context.getTerminal().getOut().printf("edit repository %s remove repo ...\n",repoId);
                context.getTerminal().getOut().printf("edit repository %s list repos ...\n",repoId);
            } else {
                NutsRepository editedRepo = validWorkspace.getRepositoryManager().findRepository(repoId);
                if (UserConfigSubCommand.exec(editedRepo, cmdLine, config, autoSave, context)) {
                    //okkay
                } else {
                    throw new NutsIllegalArgumentsException("Unsupported command " + cmdLine);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT;
    }

}
