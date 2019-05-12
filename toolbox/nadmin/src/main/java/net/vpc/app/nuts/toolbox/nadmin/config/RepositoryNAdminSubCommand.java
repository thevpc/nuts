/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;

import java.io.PrintStream;
import java.util.*;
import net.vpc.app.nuts.NutsArgumentCandidate;

/**
 *
 * @author vpc
 */
public class RepositoryNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(NutsCommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {

        NutsWorkspace ws = context.getWorkspace();
        if (cmdLine.readAll("save repository", "sw")) {
            String repositoryName = cmdLine.readRequiredNonOption(cmdLine.createNonOption("repository")).getString();
            cmdLine.setCommandName("config save repository").unexpectedArgument();
            if (cmdLine.isExecMode()) {
                trySave(context, ws, ws.config().getRepository(repositoryName), true, null);
            }
            return true;

        } else if (cmdLine.readAll("create repo", "cr")) {
            String repositoryName = null;
            String location = null;
            String repoType = null;
            while (cmdLine.hasNext()) {
                if (cmdLine.readAll("-t", "--type")) {
                    repoType = cmdLine.readRequiredNonOption(cmdLine.createNonOption("repository-type")).getString();
                } else if (cmdLine.readAll("-l", "--location")) {
                    location = cmdLine.readNonOption(cmdLine.createNonOption("folder")).getString();
                } else if (cmdLine.readAll("-id", "--id")) {
                    repositoryName = cmdLine.readRequiredNonOption(cmdLine.createNonOption("NewRepositoryName")).getString();
                } else if (!cmdLine.get().isOption()) {
                    location = cmdLine.readNonOption(cmdLine.createNonOption("RepositoryLocation")).getString();
                } else {
                    cmdLine.setCommandName("config create repo").unexpectedArgument();
                }
            }
            if (cmdLine.isExecMode()) {
                NutsRepository repository = ws.config().addRepository(
                        new NutsCreateRepositoryOptions()
                                .setName(repositoryName)
                                .setLocation(repositoryName)
                                .setConfig(
                                        new NutsRepositoryConfig()
                                                .setName(repositoryName)
                                                .setLocation(location)
                                                .setType(repoType))
                );
                if (repository == null) {
                    throw new NutsIllegalArgumentException("Unable to configure repository : " + repositoryName);
                }
                trySave(context, ws, repository, autoSave, null);
            }
            return true;

        } else {
            PrintStream out = context.getTerminal().fout();
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
                        for (NutsRepositoryDefinition repoPattern : context.getWorkspace().config().getDefaultRepositories()) {
                            repoPatterns.put(repoPattern.getName(), repoPattern);
                        }
                        String repositoryName = cmdLine.readRequiredNonOption(new NutsArgumentNonOption() {
                            @Override
                            public String getName() {
                                return "RepositoryName";
                            }
                            
                            @Override
                            public List<NutsArgumentCandidate> getValues() {
                                ArrayList<NutsArgumentCandidate> arrayList = new ArrayList<>();
                                for (Map.Entry<String, NutsRepositoryDefinition> e : repoPatterns.entrySet()) {
                                    arrayList.add(new NutsDefaultArgumentCandidate(e.getKey()));
                                }
                                arrayList.add(new NutsDefaultArgumentCandidate("<RepositoryName>"));
                                return arrayList;
                            }

                        }).getString();
                        String location = null;
                        String repoType = null;
                        if (pattern) {
                            NutsRepositoryDefinition found = repoPatterns.get(repositoryName);
                            if (found == null) {
                                throw new NutsIllegalArgumentException("Repository Pattern not found " + repositoryName + ". Try one of " + repoPatterns.keySet());
                            }
                            location = found.getLocation();
                            repoType = found.getType();
                        } else {
                            location = cmdLine.readRequiredNonOption(cmdLine.createNonOption("folder","location")).getString();
                            repoType = cmdLine.readRequiredNonOption(cmdLine.createNonOption("repository-type")).getString();
                        }
                        if (cmdLine.isExecMode()) {
                            NutsRepository repo = null;
                            NutsCreateRepositoryOptions o = new NutsCreateRepositoryOptions()
                                    .setName(repositoryName)
                                    .setLocation(repositoryName)
                                    .setProxy(proxy)
                                    .setConfig(
                                            new NutsRepositoryConfig()
                                                    .setName(repositoryName)
                                                    .setLocation(location)
                                                    .setType(repoType));

                            repo = ws.config().addRepository(o);
                            out.printf("Repository added successfully%n");
                            trySave(context, ws, repo, autoSave, null);
                            trySave(context, ws, null, autoSave, null);
                        }
                        cmdLine.setCommandName("config add repo").unexpectedArgument();
                    }
                }
                return true;

            } else if (cmdLine.readAll("remove repo", "rr")) {
                String locationOrRepositoryName = cmdLine.readRequiredNonOption(cmdLine.createNonOption("repository")).getString();
                if (cmdLine.isExecMode()) {
                    ws.config().removeRepository(locationOrRepositoryName);
                    trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
                }
                return true;

            } else if (cmdLine.readAll("list repos", "lr")) {
                if (cmdLine.isExecMode()) {
                    NutsTableFormat t = ws.formatter().createTableFormat()
                            .setColumnsConfig("id", "enabled", "type", "location")
                            .addHeaderCells("==Id==", "==Enabled==", "==Type==", "==Location==");
                    while (cmdLine.hasNext()) {
                        if (!t.configureFirst(cmdLine)) {
                            cmdLine.setCommandName("config list repos").unexpectedArgument();
                        }
                    }
                    for (NutsRepository repository : ws.config().getRepositories()) {
                        t.addRow(
                                "==" + repository.config().getName() + "==",
                                repository.config().isEnabled() ? "ENABLED" : "@@<DISABLED>@@",
                                repository.getRepositoryType(),
                                repository.config().getLocation(false)
                        );
                    }
                    out.printf(t.toString());
                }
                return true;

            } else if (cmdLine.readAll("tree repos", "tr")) {
                if (cmdLine.isExecMode()) {

                    for (NutsRepository repository : ws.config().getRepositories()) {
                        config.showRepoTree(context, repository, "");
                    }
                }
                return true;

            } else if (cmdLine.readAll("enable repo", "er")) {
                String localId = cmdLine.readRequiredNonOption(cmdLine.createNonOption("RepositoryName")).getString();
                if (cmdLine.isExecMode()) {

                    NutsRepository editedRepo = ws.config().getRepository(localId);
                    editedRepo.config().setEnabled(true);
                    trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
                }
                return true;
            } else if (cmdLine.readAll("disable repo", "rr")) {
                String localId = cmdLine.readRequiredNonOption(cmdLine.createNonOption("RepositoryName")).getString();
                if (cmdLine.isExecMode()) {
                    NutsRepository editedRepo = ws.config().getRepository(localId);
                    editedRepo.config().setEnabled(false);
                    trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
                }
                return true;
            } else if (cmdLine.readAll("edit repo", "er")) {
                String repoId = cmdLine.readRequiredNonOption(cmdLine.createNonOption("RepositoryName")).getString();
                if (cmdLine.readAll("add repo", "ar")) {
                    String repositoryName = cmdLine.readRequiredNonOption(cmdLine.createNonOption("NewRepositoryName")).getString();
                    String location = cmdLine.readRequiredNonOption(cmdLine.createNonOption("folder")).getString();
                    String repoType = cmdLine.readNonOption(cmdLine.createNonOption("repository-type")).getString();

                    NutsRepository editedRepo = ws.config().getRepository(repoId);
                    NutsRepository repo = editedRepo.config().addMirror(
                            new NutsCreateRepositoryOptions().setName(repositoryName).setLocation(repositoryName)
                                    .setConfig(
                                            new NutsRepositoryConfig()
                                                    .setName(repositoryName)
                                                    .setLocation(location)
                                                    .setType(repoType)));
                    trySave(context, ws, editedRepo, autoSave, null);
                    trySave(context, ws, repo, autoSave, null);

                } else if (cmdLine.readAll("remove repo", "rr")) {
                    String location = cmdLine.readRequiredNonOption(cmdLine.createNonOption("RepositoryName")).getString();
                    NutsRepository editedRepo = ws.config().getRepository(repoId);
                    editedRepo.config().removeMirror(location);
                    trySave(context, ws, editedRepo, autoSave, null);

                } else if (cmdLine.readAll("enable", "rr")) {
                    NutsRepository editedRepo = ws.config().getRepository(repoId);
                    editedRepo.config().setEnabled(true);
                    trySave(context, ws, editedRepo, autoSave, null);

                } else if (cmdLine.readAll("disable", "rr")) {
                    NutsRepository editedRepo = ws.config().getRepository(repoId);
                    editedRepo.config().setEnabled(true);
                    trySave(context, ws, editedRepo, autoSave, null);
                } else if (cmdLine.readAll("list repos", "lr")) {
                    NutsRepository editedRepo = ws.config().getRepository(repoId);
                    NutsRepository[] linkRepositories = editedRepo.config().isSupportedMirroring() ? editedRepo.config().getMirrors() : new NutsRepository[0];
                    out.printf("%s sub repositories.%n", linkRepositories.length);
                    NutsTableFormat t = ws.formatter().createTableFormat()
                            .setColumnsConfig("id", "enabled", "type", "location")
                            .addHeaderCells("==Id==", "==Enabled==", "==Type==", "==Location==");
                    while (cmdLine.hasNext()) {
                        if (!t.configureFirst(cmdLine)) {
                            cmdLine.setCommandName("config edit repo").unexpectedArgument();
                        }
                    }
                    for (NutsRepository repository : linkRepositories) {
                        t.addRow(
                                "==" + repository.config().getName() + "==",
                                repository.config().isEnabled() ? "ENABLED" : "@@<DISABLED>@@",
                                repository.getRepositoryType(),
                                repository.config().getLocation(false)
                        );
                    }
                    out.printf(t.toString());
                } else if (cmdLine.readAllOnce("-h", "-?", "--help")) {
                    out.printf("edit repository %s add repo ...%n", repoId);
                    out.printf("edit repository %s remove repo ...%n", repoId);
                    out.printf("edit repository %s list repos ...%n", repoId);
                } else {
                    NutsRepository editedRepo = ws.config().getRepository(repoId);
                    if (UserNAdminSubCommand.exec(editedRepo, cmdLine, config, autoSave, context)) {
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
