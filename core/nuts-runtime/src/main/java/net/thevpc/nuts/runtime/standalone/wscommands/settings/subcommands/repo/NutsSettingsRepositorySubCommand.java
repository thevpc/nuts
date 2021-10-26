/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.repo;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.AbstractNutsSettingsSubCommand;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.user.NutsSettingsUserSubCommand;

import java.util.Arrays;

/**
 * @author thevpc
 */
public class NutsSettingsRepositorySubCommand extends AbstractNutsSettingsSubCommand {

    public static RepoInfo repoInfo(NutsRepository x, boolean tree, NutsSession session) {
        return new RepoInfo(x.getName(), x.config().getType(), x.config().getLocation(true), x.config().isEnabled()?RepoStatus.enabled : RepoStatus.disabled,
                 tree ? Arrays.stream(x.config().setSession(session).getMirrors()).map(e -> repoInfo(e, tree, session)).toArray(RepoInfo[]::new) : null
        );
    }

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {

        NutsCommandLineManager commandLineFormat = session.commandLine();
//        NutsWorkspace ws = session.getWorkspace();
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
//                context.getWorkspace().config().save();
//            }
//            return true;
//
//        } else {
        NutsPrintStream out = session.out();
        if (cmdLine.next("add repo", "ar") != null) {
            String location = null;
            String repositoryName = null;
            String repoType = null;
            String parent = null;
            while (cmdLine.hasNext()) {
                NutsArgument a = cmdLine.peek();
                boolean enabled = a.isEnabled();
                switch (a.getKey().getString()) {
                    case "-l":
                    case "--location": {
                        String val = cmdLine.nextString().getValue().getString();
                        if (enabled) {
                            location = val;
                        }
                        break;
                    }
                    case "--name": {
                        String val = cmdLine.nextString().getValue().getString();
                        if (enabled) {
                            repositoryName = val;
                        }
                        break;
                    }
                    case "--type": {
                        String val = cmdLine.nextString().getValue().getString();
                        if (enabled) {
                            repoType = val;
                        }
                        break;
                    }
                    case "--parent": {
                        String val = cmdLine.nextString().getValue().getString();
                        if (enabled) {
                            parent = val;
                        }
                        break;
                    }
                    default: {
                        if (!session.configureFirst(cmdLine)) {
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
                        .setName(repositoryName)
                        .setLocation(repositoryName)
                        .setConfig(
                                location == null ? null : new NutsRepositoryConfig()
                                                .setName(repositoryName)
                                                .setLocation(location)
                                                .setType(repoType));
                if (parent == null) {
                    repo = session.repos().addRepository(o);
                } else {
                    NutsRepository p = session.repos().getRepository(parent);
                    repo = p.config().addMirror(o);
                }
                out.printf("repository added successfully%n");
                session.config().save();

            }
            cmdLine.setCommandName("config add repo").unexpectedArgument();
            return true;
        } else if (cmdLine.next("remove repo", "rr") != null) {
            String repositoryName = null;
            String parent = null;
            while (cmdLine.hasNext()) {
                NutsArgument a = cmdLine.peek();
                boolean enabled = a.isEnabled();
                switch (a.getKey().getString()) {
                    case "--name": {
                        String val = cmdLine.nextString().getValue().getString();
                        if (enabled) {
                            repositoryName = val;
                        }
                        break;
                    }
                    case "--parent": {
                        String val = cmdLine.nextString().getValue().getString();
                        if (enabled) {
                            parent = val;
                        }
                        break;
                    }
                    default: {
                        if (!session.configureFirst(cmdLine)) {
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
                    session.repos().removeRepository(repositoryName);
                } else {
                    NutsRepository p = session.repos().getRepository(parent);
                    p.config().removeMirror(repositoryName);
                }
                session.config().save();
            }
            return true;

        } else if (cmdLine.next("list repos", "lr") != null) {
            cmdLine.setCommandName("config list repos");
            String parent = null;
            while (cmdLine.hasNext()) {
                while (cmdLine.hasNext()) {
                    NutsArgument a = cmdLine.peek();
                    boolean enabled = a.isEnabled();
                    switch (a.getKey().getString()) {
                        case "--parent": {
                            String val = cmdLine.nextString().getValue().getString();
                            if (enabled) {
                                parent = val;
                            }
                            break;
                        }
                        default: {
                            if (!session.configureFirst(cmdLine)) {
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
                NutsRepository[] r = parent == null ? session.repos().getRepositories() : session.repos().getRepository(parent).config().getMirrors();
                session.formats().object(
                        Arrays.stream(session.repos().getRepositories())
                                .map(x -> repoInfo(x, session.getOutputFormat() != NutsContentType.TABLE && session.getOutputFormat() != NutsContentType.PLAIN, session)
                                )
                                .toArray()
                ).println(out);
            }
            return true;

        } else if (cmdLine.next("enable repo", "er") != null) {
            enableRepo(cmdLine, autoSave, session, true);
            return true;
        } else if (cmdLine.next("disable repo", "er") != null) {
            enableRepo(cmdLine, autoSave, session, true);
            return true;
        } else if (cmdLine.next("edit repo", "er") != null) {
            String repoId = cmdLine.required().nextNonOption(commandLineFormat.createName("RepositoryName")).getString();
            if (cmdLine.next("add repo", "ar") != null) {
                String repositoryName = cmdLine.required().nextNonOption(commandLineFormat.createName("NewRepositoryName")).getString();
                String location = cmdLine.required().nextNonOption(commandLineFormat.createName("folder")).getString();
                String repoType = cmdLine.nextNonOption(commandLineFormat.createName("repository-type")).getString();

                NutsRepository editedRepo = session.repos().getRepository(repoId);
                NutsRepository repo = editedRepo.config().addMirror(
                        new NutsAddRepositoryOptions().setName(repositoryName).setLocation(repositoryName)
                                .setConfig(
                                        new NutsRepositoryConfig()
                                                .setName(repositoryName)
                                                .setLocation(location)
                                                .setType(repoType)));
                session.config().save();

            } else if (cmdLine.next("remove repo", "rr") != null) {
                String location = cmdLine.required().nextNonOption(commandLineFormat.createName("RepositoryName")).getString();
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                editedRepo.config().removeMirror(location);
                session.config().save();

            } else if (cmdLine.next("enable", "br") != null) {
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                editedRepo.config().setEnabled(true);
                session.config().save();

            } else if (cmdLine.next("disable", "dr") != null) {
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                editedRepo.config().setEnabled(true);
                session.config().save();
            } else if (cmdLine.next("list repos", "lr") != null) {
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                NutsRepository[] linkRepositories = editedRepo.config()
                        .setSession(session)
                        .isSupportedMirroring()
                                ? editedRepo.config().setSession(session).getMirrors() : new NutsRepository[0];
                out.printf("%s sub repositories.%n", linkRepositories.length);
                NutsTableFormat t = session.formats().table();
                NutsMutableTableModel m = t.createModel();
                t.setValue(m);
                m.addHeaderCells("Id", "Enabled", "Type", "Location");
                while (cmdLine.hasNext()) {
                    if (!t.configureFirst(cmdLine)) {
                        cmdLine.setCommandName("config edit repo").unexpectedArgument();
                    }
                }
                for (NutsRepository repository : linkRepositories) {
                    m.addRow(
                            session.text().ofStyled(repository.getName(), NutsTextStyle.primary4()),
                            repository.config().isEnabled()
                            ? repository.isEnabled() ? session.text().ofStyled("ENABLED", NutsTextStyle.success())
                            : session.text().ofStyled("<RT-DISABLED>", NutsTextStyle.error())
                            : session.text().ofStyled("<DISABLED>", NutsTextStyle.error()),
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
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                if (NutsSettingsUserSubCommand.exec(editedRepo, cmdLine, autoSave, session)) {
                    //okkay
                } else {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("config edit repo: Unsupported command %s", cmdLine));
                }
            }
            return true;
        }
//        }
        return false;
    }

    private void enableRepo(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session, boolean enableRepo) {
        String repositoryName = null;
        while (cmdLine.hasNext()) {
            NutsArgument a = cmdLine.peek();
            boolean enabled = a.isEnabled();
            switch (a.getKey().getString()) {
                case "--name": {
                    String val = cmdLine.nextString().getValue().getString();
                    if (enabled) {
                        repositoryName = val;
                    }
                    break;
                }
                default: {
                    if (!session.configureFirst(cmdLine)) {
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
            NutsRepository editedRepo = session.repos().getRepository(repositoryName);
            editedRepo.config().setEnabled(enableRepo);
            session.config().save();
        }
    }

    public enum RepoStatus {
        enabled,
        disabled,
    }
    public static class RepoInfo {

        String name;
        String type;
        String location;
        RepoStatus enabled;
        RepoInfo[] mirrors;

        public RepoInfo(String name, String type, String location, RepoStatus enabled, RepoInfo[] mirrors) {
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

        public RepoStatus getEnabled() {
            return enabled;
        }

        public void setEnabled(RepoStatus enabled) {
            this.enabled = enabled;
        }
    }
}
