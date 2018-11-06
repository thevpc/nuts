/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nsh.cmds.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.cmds.ConfigCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.options.DefaultNutsArgumentCandidate;
import net.vpc.app.nuts.toolbox.nsh.options.FolderNonOption;
import net.vpc.app.nuts.toolbox.nsh.options.RepositoryNonOption;
import net.vpc.app.nuts.toolbox.nsh.options.RepositoryTypeNonOption;
import net.vpc.common.commandline.ArgumentCandidate;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.DefaultNonOption;

/**
 *
 * @author vpc
 */
public class RepositoryConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {
        NutsWorkspace validWorkspace = context.getValidWorkspace();
        if (cmdLine.read("save repository", "sw")) {
            String repositoryId = cmdLine.readNonOptionOrError(new RepositoryNonOption("RepositoryId", context.getValidWorkspace())).getString();
            cmdLine.requireEmpty();
            if (cmdLine.isExecMode()) {
                ConfigCommand.trySave(context, validWorkspace, validWorkspace.getRepositoryManager().findRepository(repositoryId), true, null);
            }
            return true;

        } else if (cmdLine.read("create repo", "cr")) {
            String repositoryId = null;
            String location=null;
            String repoType=null;
            while(!cmdLine.isEmpty()){
                if(cmdLine.read("-t","--type")) {
                    repoType = cmdLine.readNonOptionOrError(new DefaultNonOption("RepositoryType")).getString();
                }else if(cmdLine.read("-l","--location")){
                    location = cmdLine.readNonOption(new DefaultNonOption("RepositoryLocation")).getString();
                }else if(cmdLine.read("-id","--id")){
                    repositoryId = cmdLine.readNonOptionOrError(new DefaultNonOption("NewRepositoryId")).getString();
                }else if(!cmdLine.isOption()){
                    location = cmdLine.readNonOption(new DefaultNonOption("RepositoryLocation")).getString();
                }else{
                    cmdLine.requireEmpty();
                }
            }
            if (cmdLine.isExecMode()) {
                NutsRepository repository = validWorkspace.getRepositoryManager().addRepository(repositoryId, location, repoType, true);
                ConfigCommand.trySave(context, validWorkspace, repository, autoSave, null);
            }
            return true;

        } else {
            NutsPrintStream out = context.getTerminal().getFormattedOut();
            if (cmdLine.read("add repo", "ar")) {
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
                            public List<ArgumentCandidate> getValues() {
                                ArrayList<ArgumentCandidate> arrayList = new ArrayList<>();
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
                                throw new NutsIllegalArgumentException("Repository Pattern not found " + repositoryId + ". Try one of " + repoPatterns.keySet());
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
                            out.printf("Repository added successfully\n");
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
                    out.printf("%s sub repositories.\n", linkRepositories.length);
                    for (NutsRepository repository : linkRepositories) {
                        config.showRepo(context, repository, "");
                    }
                } else if (cmdLine.readOnce("-h", "-?", "--help")) {
                    out.printf("edit repository %s add repo ...\n", repoId);
                    out.printf("edit repository %s remove repo ...\n", repoId);
                    out.printf("edit repository %s list repos ...\n", repoId);
                } else {
                    NutsRepository editedRepo = validWorkspace.getRepositoryManager().findRepository(repoId);
                    if (UserConfigSubCommand.exec(editedRepo, cmdLine, config, autoSave, context)) {
                        //okkay
                    } else {
                        throw new NutsIllegalArgumentException("Unsupported command " + cmdLine);
                    }
                }
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
