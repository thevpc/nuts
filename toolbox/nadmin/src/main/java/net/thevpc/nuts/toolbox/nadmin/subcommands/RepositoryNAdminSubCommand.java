/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nadmin.subcommands;

import net.thevpc.nuts.*;

import java.io.PrintStream;
import java.util.Arrays;

/**
 * @author thevpc
 */
public class RepositoryNAdminSubCommand extends AbstractNAdminSubCommand {

    public static RepoInfo repoInfo(NutsRepository x, boolean tree, NutsSession session) {
        return new RepoInfo(x.getName(), x.config().getType(), x.config().getLocation(true), x.config().isEnabled()
                , tree ? Arrays.stream(x.config().getMirrors(session)).map(e -> repoInfo(e, tree, session)).toArray(RepoInfo[]::new) : null
        );
    }

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsApplicationContext context) {

        NutsCommandLineManager commandLineFormat = context.getWorkspace().commandLine();
        NutsWorkspace ws = context.getWorkspace();
//        if (cmdLine.next("add repo", "cr") != null) {
//            String repositoryName = null;
//            String location = null;
//            String repoType = null;
//            while (cmdLine.hasNext()) {
//                if (cmdLine.next("-t", "--type") != null) {
//                    repoType = cmdLine.required().nextNonOption(commandLineFormat.createName("repository-type")).getString();
//                } else if (cmdLine.next("-l", "--location") != null) {
//                    location = cmdLine.nextNonOption(commandLineFormat.createName("folder")).getString();
//                } else if (cmdLine.next("-id", "--id") != null) {
//                    repositoryName = cmdLine.required().nextNonOption(commandLineFormat.createName("NewRepositoryName")).getString();
//                } else if (!cmdLine.peek().isOption()) {
//                    location = cmdLine.nextNonOption(commandLineFormat.createName("RepositoryLocation")).getString();
//                } else {
//                    cmdLine.setCommandName("config add repo").unexpectedArgument();
//                }
//            }
//            if (cmdLine.isExecMode()) {
//                NutsRepository repository = ws.repos().addRepository(
//                        new NutsAddRepositoryOptions()
//                                .setName(repositoryName)
//                                .setLocation(repositoryName)
//                                .setConfig(
//                                        new NutsRepositoryConfig()
//                                                .setName(repositoryName)
//                                                .setLocation(location)
//                                                .setType(repoType))
//                );
//                if (repository == null) {
//                    throw new NutsIllegalArgumentException(context.getWorkspace(), "unable to configure repository : " + repositoryName);
//                }
//                context.getWorkspace().config().save(context.getSession());
//            }
//            return true;
//
//        } else {
            PrintStream out = context.getSession().out();
            if (cmdLine.next("add repo", "ar") != null) {
                String location = null;
                String repositoryName = null;
                String repoType = null;
                String parent = null;
                while (cmdLine.hasNext()) {
                    NutsArgument a = cmdLine.peek();
                    boolean enabled = a.isEnabled();
                    switch (a.getStringKey()) {
                        case "-l":
                        case "--location": {
                            String val = cmdLine.nextString().getStringValue();
                            if (enabled) {
                                location = val;
                            }
                            break;
                        }
                        case "--name": {
                            String val = cmdLine.nextString().getStringValue();
                            if (enabled) {
                                repositoryName = val;
                            }
                            break;
                        }
                        case "--type": {
                            String val = cmdLine.nextString().getStringValue();
                            if (enabled) {
                                repoType = val;
                            }
                            break;
                        }
                        case "--parent": {
                            String val = cmdLine.nextString().getStringValue();
                            if (enabled) {
                                parent = val;
                            }
                            break;
                        }
                        default: {
                            if (!context.getSession().configureFirst(cmdLine)) {
                                if (a.isOption()) {
                                    cmdLine.unexpectedArgument();
                                } else if (repositoryName == null) {
                                    repositoryName = cmdLine.next().getString();
                                } else if (location == null) {
                                    location = cmdLine.next().getString();
                                } else {
                                    cmdLine.unexpectedArgument();
                                }
                            }
                            break;
                        }
                    }
                }
                if (repositoryName == null) {
                    cmdLine.required();
                }

                if (cmdLine.isExecMode()) {
                    NutsRepository repo = null;
                    NutsAddRepositoryOptions o = new NutsAddRepositoryOptions()
                            .setSession(context.getSession())
                            .setName(repositoryName)
                            .setLocation(repositoryName)
                            .setConfig(
                                    location==null?null:new NutsRepositoryConfig()
                                            .setName(repositoryName)
                                            .setLocation(location)
                                            .setType(repoType));
                    if (parent == null) {
                        repo = ws.repos().addRepository(o);
                    } else {
                        NutsRepository p = ws.repos().getRepository(parent, context.getSession());
                        repo = p.config().addMirror(o);
                    }
                    out.printf("repository added successfully%n");
                    context.getWorkspace().config().save(context.getSession());

                }
                cmdLine.setCommandName("config add repo").unexpectedArgument();
                return true;
            } else if (cmdLine.next("remove repo", "rr") != null) {
                String repositoryName = null;
                String parent = null;
                while (cmdLine.hasNext()) {
                    NutsArgument a = cmdLine.peek();
                    boolean enabled = a.isEnabled();
                    switch (a.getStringKey()) {
                        case "--name": {
                            String val = cmdLine.nextString().getStringValue();
                            if (enabled) {
                                repositoryName = val;
                            }
                            break;
                        }
                        case "--parent": {
                            String val = cmdLine.nextString().getStringValue();
                            if (enabled) {
                                parent = val;
                            }
                            break;
                        }
                        default: {
                            if (!context.getSession().configureFirst(cmdLine)) {
                                if (a.isOption()) {
                                    cmdLine.unexpectedArgument();
                                } else if (repositoryName != null) {
                                    cmdLine.unexpectedArgument();
                                } else {
                                    repositoryName = cmdLine.next().getString();
                                }
                            }
                            break;
                        }
                    }
                }
                if (repositoryName == null) {
                    cmdLine.required();
                }
                if (cmdLine.isExecMode()) {
                    if (parent == null) {
                        ws.repos().removeRepository(repositoryName, new NutsRemoveOptions().setSession(context.getSession()));
                    } else {
                        NutsRepository p = ws.repos().getRepository(parent, context.getSession());
                        p.config().removeMirror(repositoryName, new NutsRemoveOptions().setSession(context.getSession()));
                    }
                    context.getWorkspace().config().save(context.getSession());
                }
                return true;

            } else if (cmdLine.next("list repos", "lr") != null) {
                cmdLine.setCommandName("config list repos");
                String parent = null;
                while (cmdLine.hasNext()) {
                    while (cmdLine.hasNext()) {
                        NutsArgument a = cmdLine.peek();
                        boolean enabled = a.isEnabled();
                        switch (a.getStringKey()) {
                            case "--parent": {
                                String val = cmdLine.nextString().getStringValue();
                                if (enabled) {
                                    parent = val;
                                }
                                break;
                            }
                            default: {
                                if (!context.getSession().configureFirst(cmdLine)) {
                                    if (a.isOption()) {
                                        cmdLine.unexpectedArgument();
                                    } else if (parent != null) {
                                        cmdLine.unexpectedArgument();
                                    } else {
                                        parent = cmdLine.next().getString();
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                if (cmdLine.isExecMode()) {
                    NutsRepository[] r = parent == null ? ws.repos().getRepositories(context.getSession()) : ws.repos().getRepository(parent, context.getSession()).config().getMirrors(context.getSession());
                    context.getSession().formatObject(
                            Arrays.stream(ws.repos().getRepositories(context.getSession()))
                                    .map(x -> repoInfo(x, context.getSession().getOutputFormat() != NutsContentType.TABLE && context.getSession().getOutputFormat() != NutsContentType.PLAIN, context.getSession())
                                    )
                                    .toArray()
                    ).println(out);
                }
                return true;

            } else if (cmdLine.next("enable repo", "er") != null) {
                enableRepo(cmdLine, autoSave, context, ws, true);
                return true;
            } else if (cmdLine.next("disable repo", "er") != null) {
                enableRepo(cmdLine, autoSave, context, ws, true);
                return true;
            } else if (cmdLine.next("edit repo", "er") != null) {
                String repoId = cmdLine.required().nextNonOption(commandLineFormat.createName("RepositoryName")).getString();
                if (cmdLine.next("add repo", "ar") != null) {
                    String repositoryName = cmdLine.required().nextNonOption(commandLineFormat.createName("NewRepositoryName")).getString();
                    String location = cmdLine.required().nextNonOption(commandLineFormat.createName("folder")).getString();
                    String repoType = cmdLine.nextNonOption(commandLineFormat.createName("repository-type")).getString();

                    NutsRepository editedRepo = ws.repos().getRepository(repoId, context.getSession());
                    NutsRepository repo = editedRepo.config().addMirror(
                            new NutsAddRepositoryOptions().setName(repositoryName).setLocation(repositoryName)
                                    .setConfig(
                                            new NutsRepositoryConfig()
                                                    .setName(repositoryName)
                                                    .setLocation(location)
                                                    .setType(repoType)));
                    context.getWorkspace().config().save(context.getSession());

                } else if (cmdLine.next("remove repo", "rr") != null) {
                    String location = cmdLine.required().nextNonOption(commandLineFormat.createName("RepositoryName")).getString();
                    NutsRepository editedRepo = ws.repos().getRepository(repoId, context.getSession());
                    editedRepo.config().removeMirror(location, new NutsRemoveOptions().setSession(context.getSession()));
                    context.getWorkspace().config().save(context.getSession());

                } else if (cmdLine.next("enable", "br") != null) {
                    NutsRepository editedRepo = ws.repos().getRepository(repoId, context.getSession());
                    editedRepo.config().setEnabled(true, new NutsUpdateOptions().setSession(context.getSession()));
                    context.getWorkspace().config().save(context.getSession());

                } else if (cmdLine.next("disable", "dr") != null) {
                    NutsRepository editedRepo = ws.repos().getRepository(repoId, context.getSession());
                    editedRepo.config().setEnabled(true, new NutsUpdateOptions().setSession(context.getSession()));
                    context.getWorkspace().config().save(context.getSession());
                } else if (cmdLine.next("list repos", "lr") != null) {
                    NutsRepository editedRepo = ws.repos().getRepository(repoId, context.getSession());
                    NutsRepository[] linkRepositories = editedRepo.config().isSupportedMirroring() ? editedRepo.config().getMirrors(context.getSession()) : new NutsRepository[0];
                    out.printf("%s sub repositories.%n", linkRepositories.length);
                    NutsTableFormat t = ws.formats().table();
                    NutsMutableTableModel m = t.createModel();
                    t.setModel(m);
                    m.addHeaderCells("Id", "Enabled", "Type", "Location");
                    while (cmdLine.hasNext()) {
                        if (!t.configureFirst(cmdLine)) {
                            cmdLine.setCommandName("config edit repo").unexpectedArgument();
                        }
                    }
                    for (NutsRepository repository : linkRepositories) {
                        NutsTextFormatManager text = ws.formats().text();
                        m.addRow(
                                text.factory().styled(repository.getName(), NutsTextNodeStyle.primary(4)),
                                repository.config().isEnabled() ?
                                        repository.isEnabled() ? text.factory().styled("ENABLED", NutsTextNodeStyle.success()) :
                                                text.factory().styled("<RT-DISABLED>", NutsTextNodeStyle.error()) :
                                        text.factory().styled("<DISABLED>", NutsTextNodeStyle.error()),
                                repository.getRepositoryType(),
                                repository.config().getLocation(false)
                        );
                    }
                    out.printf(t.toString());
                } else if (cmdLine.next("-h", "-?", "--help") != null) {
                    out.printf("edit repository %s add repo ...%n", repoId);
                    out.printf("edit repository %s remove repo ...%n", repoId);
                    out.printf("edit repository %s list repos ...%n", repoId);
                } else {
                    NutsRepository editedRepo = ws.repos().getRepository(repoId, context.getSession());
                    if (UserNAdminSubCommand.exec(editedRepo, cmdLine, autoSave, context)) {
                        //okkay
                    } else {
                        throw new NutsIllegalArgumentException(context.getWorkspace(), "config edit repo: Unsupported command " + cmdLine);
                    }
                }
                return true;
            }
//        }
        return false;
    }

    private void enableRepo(NutsCommandLine cmdLine, Boolean autoSave, NutsApplicationContext context, NutsWorkspace ws, boolean enableRepo) {
        String repositoryName = null;
        while (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.peek();
            boolean enabled = a.isEnabled();
            switch (a.getStringKey()) {
                case "--name": {
                    String val = cmdLine.nextString().getStringValue();
                    if (enabled) {
                        repositoryName = val;
                    }
                    break;
                }
                default: {
                    if (!context.getSession().configureFirst(cmdLine)) {
                        if (a.isOption()) {
                            cmdLine.unexpectedArgument();
                        } else if (repositoryName == null) {
                            repositoryName = cmdLine.next().getString();
                        } else {
                            cmdLine.unexpectedArgument();
                        }
                    }
                    break;
                }
            }
        }
        if (repositoryName == null) {
            cmdLine.required();
        }
        if (cmdLine.isExecMode()) {
            NutsRepository editedRepo = ws.repos().getRepository(repositoryName, context.getSession());
            editedRepo.config().setEnabled(enableRepo, new NutsUpdateOptions().setSession(context.getSession()));
            context.getWorkspace().config().save(context.getSession());
        }
    }

    public static class RepoInfo {
        String name;
        String type;
        String location;
        boolean enabled;
        RepoInfo[] mirrors;

        public RepoInfo(String name, String type, String location, boolean enabled, RepoInfo[] mirrors) {
            this.name = name;
            this.type = type;
            this.location = location;
            this.enabled = enabled;
            this.mirrors = mirrors;
        }

        public RepoInfo() {
        }

        public RepoInfo[] getMirrors() {
            return mirrors;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
