/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.repo;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsArgumentName;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.format.NutsMutableTableModel;
import net.thevpc.nuts.format.NutsTableFormat;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNutsSettingsSubCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user.NutsSettingsUserSubCommand;
import net.thevpc.nuts.spi.NutsRepositoryDB;
import net.thevpc.nuts.spi.NutsRepositoryLocation;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsRef;

import java.util.*;

/**
 * @author thevpc
 */
public class NutsSettingsRepositorySubCommand extends AbstractNutsSettingsSubCommand {

    public static RepoInfo repoInfo(NutsRepository x, boolean tree, NutsSession session) {
        return new RepoInfo(x.getName(), x.config().getType(), x.config().getLocationPath(), x.config().isEnabled() ? RepoStatus.enabled : RepoStatus.disabled, tree ? x.config().setSession(session).getMirrors().stream().map(e -> repoInfo(e, tree, session)).toArray(RepoInfo[]::new) : null);
    }

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {

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
//                } else if (!cmdLine.isNextOption()) {
//                    location = cmdLine.nextNonOption(commandLineFormat.createName("RepositoryLocation")).getString();
//                } else {
//                    cmdLine.setCommandName("config add repo").throwUnexpectedArgument();
//                }
//            }
//            if (cmdLine.isExecMode()) {
//                NutsRepository repository = ws.repos().addRepository(
//                        new NutsAddRepositoryOptions()
//                                .setName(repositoryName)
//                                .setLocation(repositoryName)
//                                .setConfig(
//                                        new NutsRepositoryConfig507()
//                                                .setName(repositoryName)
//                                                .setLocation(location)
//                                                .setLocationType(repoType))
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
        if (cmdLine.next("add repo", "ar").isPresent()) {
            class Data {
                String location = null;
                String repositoryName = null;
                String parent = null;
                Map<String, String> env = new LinkedHashMap<>();
            }
            Data d = new Data();
            while (cmdLine.hasNext()) {
                NutsArgument aa = cmdLine.peek().get(session);
                boolean enabled = aa.isActive();
                switch (aa.key()) {
                    case "-l":
                    case "--location": {
                        cmdLine.withNextString((v, a, s) -> d.location = v);
                        break;
                    }
                    case "--name": {
                        cmdLine.withNextString((v, a, s) -> d.repositoryName = v);
                        break;
                    }
                    case "--parent": {
                        cmdLine.withNextString((v, a, s) -> d.parent = v);
                        break;
                    }
                    case "--env": {
                        cmdLine.withNextString((v, a, s) -> {
                            NutsArgument vv = NutsArgument.of(v);
                            d.env.put(vv.getKey() == null ? null : vv.key(),
                                    vv.getValue() == null ? null : vv.getStringValue().get(session));
                        });
                        break;
                    }
                    default: {
                        if (!session.configureFirst(cmdLine)) {
                            if (aa.isOption()) {
                                cmdLine.throwUnexpectedArgument();
                            } else if (aa.isKeyValue()) {
                                NutsArgument n = cmdLine.nextString().get(session);
                                d.repositoryName = n.getStringKey().get(session);
                                d.location = n.getStringValue().get(session);
                            } else {
                                d.location = cmdLine.next().flatMap(NutsValue::asString).get(session);
                                String loc2 = NutsRepositoryDB.of(session).getRepositoryLocationByName(d.location);
                                if (loc2 != null) {
                                    d.repositoryName = d.location;
                                    d.location = loc2;
                                } else {
                                    cmdLine.peek().get(session);
                                }
                            }
                        }
                        break;
                    }
                }
            }
            if (d.repositoryName == null) {
                cmdLine.peek().get(session);
            }

            if (cmdLine.isExecMode()) {
                NutsRepository repo = null;
                NutsAddRepositoryOptions o = new NutsAddRepositoryOptions().setName(d.repositoryName).setLocation(d.repositoryName).setConfig(d.location == null ? null : new NutsRepositoryConfig().setName(d.repositoryName).setLocation(NutsRepositoryLocation.of(d.location)).setEnv(d.env));
                if (d.parent == null) {
                    repo = session.repos().addRepository(o);
                } else {
                    NutsRepository p = session.repos().getRepository(d.parent);
                    repo = p.config().addMirror(o);
                }
                out.printlnf("repository %s added successfully", repo.getName());
                session.config().save();

            }
            cmdLine.setCommandName("config add repo").throwUnexpectedArgument();
            return true;
        } else if (cmdLine.next("remove repo", "rr").isPresent()) {
            NutsRef<String> repositoryName = NutsRef.ofNull(String.class);
            NutsRef<String> parent = NutsRef.ofNull(String.class);
            while (cmdLine.hasNext()) {
                NutsArgument aa = cmdLine.peek().get(session);
                boolean enabled = aa.isActive();
                switch (aa.key()) {
                    case "--name": {
                        cmdLine.withNextString((v, a, s) -> repositoryName.set(v));
                        break;
                    }
                    case "--parent": {
                        cmdLine.withNextString((v, a, s) -> parent.set(v));
                        break;
                    }
                    default: {
                        if (!session.configureFirst(cmdLine)) {
                            if (aa.isOption()) {
                                cmdLine.throwUnexpectedArgument();
                            } else if (repositoryName.isNotNull()) {
                                cmdLine.throwUnexpectedArgument();
                            } else {
                                repositoryName.set(cmdLine.next().flatMap(NutsValue::asString).get(session));
                            }
                        }
                        break;
                    }
                }
            }
            if (repositoryName.isNull()) {
                cmdLine.peek().get(session);
            }
            if (cmdLine.isExecMode()) {
                if (parent.isNull()) {
                    session.repos().removeRepository(repositoryName.get());
                } else {
                    NutsRepository p = session.repos().getRepository(parent.get());
                    p.config().removeMirror(repositoryName.get());
                }
                session.config().save();
            }
            return true;

        } else if (cmdLine.next("list repos", "lr").isPresent()) {
            cmdLine.setCommandName("config list repos");
            NutsRef<String> parent = NutsRef.ofNull(String.class);
            while (cmdLine.hasNext()) {
                while (cmdLine.hasNext()) {
                    NutsArgument aa = cmdLine.peek().get(session);
                    boolean enabled = aa.isActive();
                    switch (aa.key()) {
                        case "--parent": {
                            cmdLine.withNextString((v,a,s)->parent.set(v));
                            break;
                        }
                        default: {
                            if (!session.configureFirst(cmdLine)) {
                                if (aa.isOption()) {
                                    cmdLine.throwUnexpectedArgument();
                                } else if (parent.isNotNull()) {
                                    cmdLine.throwUnexpectedArgument();
                                } else {
                                    parent.set(cmdLine.next().flatMap(NutsValue::asString).get(session));
                                }
                            }
                            break;
                        }
                    }
                }
            }
            if (cmdLine.isExecMode()) {
                List<NutsRepository> r = parent.isNull() ? session.repos().getRepositories() : session.repos().getRepository(parent.get()).config().getMirrors();
                out.printlnf(r.stream().map(x -> repoInfo(x, session.getOutputFormat() != NutsContentType.TABLE && session.getOutputFormat() != NutsContentType.PLAIN, session)).toArray());
            }
            return true;

        } else if (cmdLine.next("enable repo", "er").isPresent()) {
            enableRepo(cmdLine, autoSave, session, true);
            return true;
        } else if (cmdLine.next("disable repo", "dr").isPresent()) {
            enableRepo(cmdLine, autoSave, session, false);
            return true;
        } else if (cmdLine.next("edit repo", "er").isPresent()) {
            String repoId = cmdLine.nextNonOption(NutsArgumentName.of("RepositoryName", session)).flatMap(NutsValue::asString).get(session);
            if (cmdLine.next("add repo", "ar").isPresent()) {
                String repositoryName = cmdLine.nextNonOption(NutsArgumentName.of("NewRepositoryName", session)).flatMap(NutsValue::asString).get(session);
                String location = cmdLine.nextNonOption(NutsArgumentName.of("folder", session)).flatMap(NutsValue::asString).get(session);

                NutsRepository editedRepo = session.repos().getRepository(repoId);
                NutsRepository repo = editedRepo.config().addMirror(new NutsAddRepositoryOptions().setName(repositoryName).setLocation(repositoryName).setConfig(new NutsRepositoryConfig().setName(repositoryName).setLocation(NutsRepositoryLocation.of(location))));
                session.config().save();

            } else if (cmdLine.next("remove repo", "rr").isPresent()) {
                String location = cmdLine.nextNonOption(NutsArgumentName.of("RepositoryName", session)).flatMap(NutsValue::asString).get(session);
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                editedRepo.config().removeMirror(location);
                session.config().save();

            } else if (cmdLine.next("enable", "br").isPresent()) {
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                editedRepo.config().setEnabled(true);
                session.config().save();

            } else if (cmdLine.next("disable", "dr").isPresent()) {
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                editedRepo.config().setEnabled(true);
                session.config().save();
            } else if (cmdLine.next("list repos", "lr").isPresent()) {
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                List<NutsRepository> linkRepositories = editedRepo.config().setSession(session).isSupportedMirroring() ? editedRepo.config().setSession(session).getMirrors() : Collections.emptyList();
                out.printf("%s sub repositories.%n", linkRepositories.size());
                NutsTableFormat t = NutsTableFormat.of(session);
                NutsMutableTableModel m = NutsMutableTableModel.of(session);
                t.setValue(m);
                m.addHeaderCells("Id", "Enabled", "Type", "Location");
                while (cmdLine.hasNext()) {
                    if (!t.configureFirst(cmdLine)) {
                        cmdLine.setCommandName("config edit repo").throwUnexpectedArgument();
                    }
                }
                for (NutsRepository repository : linkRepositories) {
                    m.addRow(NutsTexts.of(session).ofStyled(repository.getName(), NutsTextStyle.primary4()), repository.config().isEnabled() ? repository.isEnabled(session) ? NutsTexts.of(session).ofStyled("ENABLED", NutsTextStyle.success()) : NutsTexts.of(session).ofStyled("<RT-DISABLED>", NutsTextStyle.error()) : NutsTexts.of(session).ofStyled("<DISABLED>", NutsTextStyle.error()), repository.getRepositoryType(), repository.config().getLocation().toString());
                }
                out.printf(t.toString());
            } else if (cmdLine.next("-h", "-?", "--help").isPresent()) {
                out.printf("edit repository %s add repo ...%n", repoId);
                out.printf("edit repository %s remove repo ...%n", repoId);
                out.printf("edit repository %s list repos ...%n", repoId);
            } else {
                NutsRepository editedRepo = session.repos().getRepository(repoId);
                if (NutsSettingsUserSubCommand.exec(editedRepo, cmdLine, autoSave, session)) {
                    //okkay
                } else {
                    throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("config edit repo: Unsupported command %s", cmdLine));
                }
            }
            return true;
        }
//        }
        return false;
    }

    private void enableRepo(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session, boolean enableRepo) {
        NutsRef<String> repositoryName = NutsRef.ofNull(String.class);
        while (cmdLine.hasNext()) {
            NutsArgument aa = cmdLine.peek().get(session);
            boolean enabled = aa.isActive();
            switch (aa.key()) {
                case "--name": {
                    cmdLine.withNextString((v, a, s) -> repositoryName.set(v));
                    break;
                }
                default: {
                    if (!session.configureFirst(cmdLine)) {
                        if (aa.isOption()) {
                            cmdLine.throwUnexpectedArgument();
                        } else if (repositoryName.isNull()) {
                            repositoryName.set(cmdLine.next().flatMap(NutsValue::asString).get(session));
                        } else {
                            cmdLine.throwUnexpectedArgument();
                        }
                    }
                    break;
                }
            }
        }
        if (repositoryName.isNull()) {
            cmdLine.peek().get(session);
        }
        if (cmdLine.isExecMode()) {
            NutsRepository editedRepo = session.repos().getRepository(repositoryName.get());
            editedRepo.config().setEnabled(enableRepo);
            session.config().save();
        }
    }

    public enum RepoStatus {
        enabled, disabled,
    }

    public static class RepoInfo {

        String name;
        String type;
        NutsPath location;
        RepoStatus enabled;
        RepoInfo[] mirrors;

        public RepoInfo(String name, String type, NutsPath location, RepoStatus enabled, RepoInfo[] mirrors) {
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

        public NutsPath getLocation() {
            return location;
        }

        public void setLocation(NutsPath location) {
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
