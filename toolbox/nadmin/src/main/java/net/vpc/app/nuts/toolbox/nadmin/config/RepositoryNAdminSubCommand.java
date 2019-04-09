/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;
import net.vpc.app.nuts.app.options.RepositoryNonOption;
import net.vpc.app.nuts.app.options.RepositoryTypeNonOption;
import net.vpc.app.nuts.app.util.DefaultWorkspaceCellFormatter;
import net.vpc.common.commandline.*;
import net.vpc.common.commandline.format.TableFormatter;

import java.io.PrintStream;
import java.util.*;

/**
 *
 * @author vpc
 */
public class RepositoryNAdminSubCommand extends AbstractNAdminSubCommand {

    @Override
    public boolean exec(CommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {

        NutsWorkspace ws = context.getWorkspace();
        if (cmdLine.readAll("save repository", "sw")) {
            String repositoryName = cmdLine.readRequiredNonOption(new RepositoryNonOption("repositoryName", context.getWorkspace())).getStringExpression();
            cmdLine.unexpectedArgument("config save repository");
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
                    repoType = cmdLine.readRequiredNonOption(new DefaultNonOption("RepositoryType")).getStringExpression();
                } else if (cmdLine.readAll("-l", "--location")) {
                    location = cmdLine.readNonOption(new DefaultNonOption("RepositoryLocation")).getStringExpression();
                } else if (cmdLine.readAll("-id", "--id")) {
                    repositoryName = cmdLine.readRequiredNonOption(new DefaultNonOption("NewRepositoryName")).getStringExpression();
                } else if (!cmdLine.isOption()) {
                    location = cmdLine.readNonOption(new DefaultNonOption("RepositoryLocation")).getStringExpression();
                } else {
                    cmdLine.unexpectedArgument("config create repo");
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
                        for (NutsRepositoryDefinition repoPattern : context.getWorkspace().config().getDefaultRepositories()) {
                            repoPatterns.put(repoPattern.getName(), repoPattern);
                        }
                        String repositoryName = cmdLine.readRequiredNonOption(new DefaultNonOption("RepositoryName") {
                            @Override
                            public List<ArgumentCandidate> getValues() {
                                ArrayList<ArgumentCandidate> arrayList = new ArrayList<>();
                                for (Map.Entry<String, NutsRepositoryDefinition> e : repoPatterns.entrySet()) {
                                    arrayList.add(new DefaultArgumentCandidate(e.getKey()));
                                }
                                arrayList.add(new DefaultArgumentCandidate("<RepositoryName>"));
                                return arrayList;
                            }

                        }).getStringExpression();
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
                            location = cmdLine.readRequiredNonOption(new FolderNonOption("Location")).getStringExpression();
                            repoType = cmdLine.readRequiredNonOption(new RepositoryTypeNonOption("RepositoryType", context.getWorkspace())).getStringExpression();
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
                            out.printf("Repository added successfully\n");
                            trySave(context, ws, repo, autoSave, null);
                            trySave(context, ws, null, autoSave, null);
                        }
                        cmdLine.unexpectedArgument("config add repo");
                    }
                }
                return true;

            } else if (cmdLine.readAll("remove repo", "rr")) {
                String locationOrRepositoryName = cmdLine.readRequiredNonOption(new RepositoryNonOption("Repository", context.getWorkspace())).getStringExpression();
                if (cmdLine.isExecMode()) {
                    ws.config().removeRepository(locationOrRepositoryName);
                    trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
                }
                return true;

            } else if (cmdLine.readAll("list repos", "lr")) {
                if (cmdLine.isExecMode()) {
                    TableFormatter t = new TableFormatter(new DefaultWorkspaceCellFormatter(ws))
                            .setColumnsConfig("id", "enabled", "type", "location")
                            .addHeaderCells("==Id==", "==Enabled==", "==Type==", "==Location==");
                    while (cmdLine.hasNext()) {
                        if (!t.configure(cmdLine)) {
                            cmdLine.unexpectedArgument("config list repos");
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
                String localId = cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryName", context.getWorkspace())).getStringExpression();
                if (cmdLine.isExecMode()) {

                    NutsRepository editedRepo = ws.config().getRepository(localId);
                    editedRepo.config().setEnabled(true);
                    trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
                }
                return true;
            } else if (cmdLine.readAll("disable repo", "rr")) {
                String localId = cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryName", context.getWorkspace())).getStringExpression();
                if (cmdLine.isExecMode()) {
                    NutsRepository editedRepo = ws.config().getRepository(localId);
                    editedRepo.config().setEnabled(false);
                    trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
                }
                return true;
            } else if (cmdLine.readAll("edit repo", "er")) {
                String repoId = cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryName", context.getWorkspace())).getStringExpression();
                if (cmdLine.readAll("add repo", "ar")) {
                    String repositoryName = cmdLine.readRequiredNonOption(new DefaultNonOption("NewRepositoryName")).getStringExpression();
                    String location = cmdLine.readRequiredNonOption(new FolderNonOption("RepositoryLocation")).getStringExpression();
                    String repoType = cmdLine.readNonOption(new RepositoryTypeNonOption("RepositoryType", context.getWorkspace())).getStringExpression();

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
                    String location = cmdLine.readRequiredNonOption(new RepositoryNonOption("RepositoryName", context.getWorkspace())).getStringExpression();
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
                    out.printf("%s sub repositories.\n", linkRepositories.length);
                    TableFormatter t = new TableFormatter(new DefaultWorkspaceCellFormatter(ws))
                            .setColumnsConfig("id", "enabled", "type", "location")
                            .addHeaderCells("==Id==", "==Enabled==", "==Type==", "==Location==");
                    while (cmdLine.hasNext()) {
                        if (!t.configure(cmdLine)) {
                            cmdLine.unexpectedArgument("config edit repo");
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
                    out.printf("edit repository %s add repo ...\n", repoId);
                    out.printf("edit repository %s remove repo ...\n", repoId);
                    out.printf("edit repository %s list repos ...\n", repoId);
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
