/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nsh.cmds.config;

import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryDefinition;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.cmds.ConfigCommand;
import net.vpc.app.nuts.toolbox.nsh.options.RepositoryNonOption;
import net.vpc.app.nuts.toolbox.nsh.options.RepositoryTypeNonOption;
import net.vpc.app.nuts.toolbox.nsh.util.DefaultWorkspaceCellFormatter;
import net.vpc.common.commandline.*;
import net.vpc.common.commandline.format.TableFormatter;

import java.io.PrintStream;
import java.util.*;

/**
 *
 * @author vpc
 */
public class RepositoryConfigSubCommand extends AbstractConfigSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, ConfigCommand config, Boolean autoSave, NutsCommandContext context) {

        NutsWorkspace ws = context.getWorkspace();
        if (cmdLine.readAll("save repository", "sw")) {
            String repositoryId = cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryId", context.getWorkspace())).getString();
            cmdLine.unexpectedArgument("config save repository");
            if (cmdLine.isExecMode()) {
                ConfigCommand.trySave(context, ws, ws.getRepositoryManager().findRepository(repositoryId), true, null);
            }
            return true;

        } else if (cmdLine.readAll("create repo", "cr")) {
            String repositoryId = null;
            String location=null;
            String repoType=null;
            while(cmdLine.hasNext()){
                if(cmdLine.readAll("-t","--type")) {
                    repoType = cmdLine.readRequiredNonOption(new DefaultNonOption("RepositoryType")).getString();
                }else if(cmdLine.readAll("-l","--location")){
                    location = cmdLine.readNonOption(new DefaultNonOption("RepositoryLocation")).getString();
                }else if(cmdLine.readAll("-id","--id")){
                    repositoryId = cmdLine.readRequiredNonOption(new DefaultNonOption("NewRepositoryId")).getString();
                }else if(!cmdLine.isOption()){
                    location = cmdLine.readNonOption(new DefaultNonOption("RepositoryLocation")).getString();
                }else{
                    cmdLine.unexpectedArgument("config create repo");
                }
            }
            if (cmdLine.isExecMode()) {
                NutsRepository repository = ws.getRepositoryManager().addRepository(repositoryId, location, repoType, true);
                ConfigCommand.trySave(context, ws, repository, autoSave, null);
            }
            return true;

        } else {
            PrintStream out = context.getTerminal().getFormattedOut();
            if (cmdLine.readAll("add repo", "ar")) {
                boolean proxy = false;
                boolean pattern = false;
                while (cmdLine.hasNext()) {
                    if (cmdLine.readAllOnce("-p", "--proxy")) {
                        proxy = true;
                    } else if (cmdLine.readAllOnce("-P", "--pattern")) {
                        pattern = true;
                    } else {
                        final Map<String, NutsRepositoryDefinition> repoPatterns = new LinkedHashMap<String, NutsRepositoryDefinition>();
                        for (NutsRepositoryDefinition repoPattern : context.getWorkspace().getRepositoryManager().getDefaultRepositories()) {
                            repoPatterns.put(repoPattern.getId(), repoPattern);
                        }
                        String repositoryId = cmdLine.readRequiredNonOption(new DefaultNonOption("RepositoryId") {
                            @Override
                            public List<ArgumentCandidate> getValues() {
                                ArrayList<ArgumentCandidate> arrayList = new ArrayList<>();
                                for (Map.Entry<String, NutsRepositoryDefinition> e : repoPatterns.entrySet()) {
                                    arrayList.add(new DefaultArgumentCandidate(e.getKey()));
                                }
                                arrayList.add(new DefaultArgumentCandidate("<RepositoryId>"));
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
                            location = cmdLine.readRequiredNonOption(new FolderNonOption("Location")).getString();
                            repoType = cmdLine.readRequiredNonOption(new RepositoryTypeNonOption("RepositoryType", context.consoleContext())).getString();
                        }
                        if (cmdLine.isExecMode()) {
                            NutsRepository repo = null;
                            if (proxy) {
                                repo = ws.getRepositoryManager().addProxiedRepository(repositoryId, location, repoType, true);
                            } else {
                                repo = ws.getRepositoryManager().addRepository(repositoryId, location, repoType, true);
                            }
                            out.printf("Repository added successfully\n");
                            ConfigCommand.trySave(context, ws, repo, autoSave, null);
                            ConfigCommand.trySave(context, ws, null, autoSave, null);
                        }
                        cmdLine.unexpectedArgument("config add repo");
                    }
                }
                return true;

            } else if (cmdLine.readAll("remove repo", "rr")) {
                String locationOrRepositoryId = cmdLine.readRequiredNonOption(new RepositoryNonOption("Repository", context.getWorkspace())).getString();
                if (cmdLine.isExecMode()) {
                    ws.getRepositoryManager().removeRepository(locationOrRepositoryId);
                    ConfigCommand.trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
                }
                return true;

            } else if (cmdLine.readAll("list repos", "lr")) {
                if (cmdLine.isExecMode()) {
                    TableFormatter t = new TableFormatter(new DefaultWorkspaceCellFormatter(ws))
                            .setColumnsConfig("id","enabled","type","location")
                            .addHeaderCells("==Id==","==Enabled==","==Type==","==Location==")
                            ;
                    while (cmdLine.hasNext()) {
                        if(!t.configure(cmdLine)){
                            cmdLine.unexpectedArgument("config list repos");
                        }
                    }
                    for (NutsRepository repository : ws.getRepositoryManager().getRepositories()) {
                        t.addRow(
                                "==" + repository.getRepositoryId() + "==",
                                repository.isEnabled()?"ENABLED":"@@<DISABLED>@@",
                                repository.getRepositoryType(),
                                repository.getConfigManager().getLocation()
                        );
                    }
                    out.printf(t.toString());
                }
                return true;

            } else if (cmdLine.readAll("tree repos", "tr")) {
                if (cmdLine.isExecMode()) {

                    for (NutsRepository repository : ws.getRepositoryManager().getRepositories()) {
                        config.showRepoTree(context, repository, "");
                    }
                }
                return true;

            } else if (cmdLine.readAll("enable repo", "er")) {
                String localId = cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryId", context.getWorkspace())).getString();
                if (cmdLine.isExecMode()) {

                    NutsRepository editedRepo = ws.getRepositoryManager().findRepository(localId);
                    editedRepo.setEnabled(true);
                    ConfigCommand.trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
                }
                return true;
            } else if (cmdLine.readAll("disable repo", "rr")) {
                String localId = cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryId", context.getWorkspace())).getString();
                if (cmdLine.isExecMode()) {
                    NutsRepository editedRepo = ws.getRepositoryManager().findRepository(localId);
                    editedRepo.setEnabled(false);
                    ConfigCommand.trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
                }
                return true;
            } else if (cmdLine.readAll("edit repo", "er")) {
                String repoId = cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoyId", context.getWorkspace())).getString();
                if (cmdLine.readAll("add repo", "ar")) {
                    String repositoryId = cmdLine.readRequiredNonOption(new DefaultNonOption("NewRepositoryId")).getString();
                    String location = cmdLine.readRequiredNonOption(new FolderNonOption("RepositoryLocation")).getString();
                    String repoType = cmdLine.readNonOption(new RepositoryTypeNonOption("RepositoryType", context.consoleContext())).getString();

                    NutsRepository editedRepo = ws.getRepositoryManager().findRepository(repoId);
                    NutsRepository repo = editedRepo.addMirror(repositoryId, location, repoType, true);
                    ConfigCommand.trySave(context, ws, editedRepo, autoSave, null);
                    ConfigCommand.trySave(context, ws, repo, autoSave, null);

                } else if (cmdLine.readAll("remove repo", "rr")) {
                    String location = cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryId", context.getWorkspace())).getString();
                    NutsRepository editedRepo = ws.getRepositoryManager().findRepository(repoId);
                    editedRepo.removeMirror(location);
                    ConfigCommand.trySave(context, ws, editedRepo, autoSave, null);

                } else if (cmdLine.readAll("enable", "rr")) {
                    NutsRepository editedRepo = ws.getRepositoryManager().findRepository(repoId);
                    editedRepo.setEnabled(true);
                    ConfigCommand.trySave(context, ws, editedRepo, autoSave, null);

                } else if (cmdLine.readAll("disable", "rr")) {
                    NutsRepository editedRepo = ws.getRepositoryManager().findRepository(repoId);
                    editedRepo.setEnabled(true);
                    ConfigCommand.trySave(context, ws, editedRepo, autoSave, null);
                } else if (cmdLine.readAll("list repos", "lr")) {
                    NutsRepository editedRepo = ws.getRepositoryManager().findRepository(repoId);
                    NutsRepository[] linkRepositories = editedRepo.getMirrors();
                    out.printf("%s sub repositories.\n", linkRepositories.length);
                    TableFormatter t = new TableFormatter(new DefaultWorkspaceCellFormatter(ws))
                            .setColumnsConfig("id","enabled","type","location")
                            .addHeaderCells("==Id==","==Enabled==","==Type==","==Location==")
                            ;
                    while (cmdLine.hasNext()) {
                        if(!t.configure(cmdLine)){
                            cmdLine.unexpectedArgument("config edit repo");
                        }
                    }
                    for (NutsRepository repository : linkRepositories) {
                        t.addRow(
                                "==" + repository.getRepositoryId() + "==",
                                repository.isEnabled()?"ENABLED":"@@<DISABLED>@@",
                                repository.getRepositoryType(),
                                repository.getConfigManager().getLocation()
                        );
                    }
                    out.printf(t.toString());
                } else if (cmdLine.readAllOnce("-h", "-?", "--help")) {
                    out.printf("edit repository %s add repo ...\n", repoId);
                    out.printf("edit repository %s remove repo ...\n", repoId);
                    out.printf("edit repository %s list repos ...\n", repoId);
                } else {
                    NutsRepository editedRepo = ws.getRepositoryManager().findRepository(repoId);
                    if (UserConfigSubCommand.exec(editedRepo, cmdLine, config, autoSave, context)) {
                        //okkay
                    } else {
                        throw new NutsIllegalArgumentException("config edit repo: Unsupported command " + cmdLine);
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
