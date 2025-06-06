/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.repo;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;

import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.format.NMutableTableModel;
import net.thevpc.nuts.format.NTableFormat;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user.NSettingsUserSubCommand;
import net.thevpc.nuts.spi.NRepositoryDB;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NRef;

import java.util.*;

/**
 * @author thevpc
 */
public class NSettingsRepositorySubCommand extends AbstractNSettingsSubCommand {
    public NSettingsRepositorySubCommand(NWorkspace workspace) {
        super();
    }

    public static RepoInfo repoInfo(NRepository x, boolean tree) {
        return new RepoInfo(x.getName(), x.config().getType(), x.config().getLocationPath(),
                x.config().isEnabled() ? RepoStatus.enabled : RepoStatus.disabled,
                (tree ? x.config().getMirrors().stream().map(e -> repoInfo(e, tree)).toArray(RepoInfo[]::new) : null),
                x.config().isTemporary(),
                x.config().isPreview(),
                x.config().getTags().toArray(new String[0])
        );
    }

    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave) {

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
        NWorkspace workspace = NWorkspace.of();
        NSession session = NSession.of();
        NPrintStream out = session.out();
        if (cmdLine.next("add repo", "ar").isPresent()) {
            doAddRepo(cmdLine,out);
            return true;
        } else if (cmdLine.next("remove repo", "rr").isPresent()) {
            doRemoveRepo(cmdLine,out);

            return true;

        } else if (cmdLine.next("list repos", "lr").isPresent()) {
            doListRepo(cmdLine,out);
            return true;

        } else if (cmdLine.next("enable repo", "er").isPresent()) {
            enableRepo(cmdLine, autoSave, session, true);
            return true;
        } else if (cmdLine.next("disable repo", "dr").isPresent()) {
            enableRepo(cmdLine, autoSave, session, false);
            return true;
        } else if (cmdLine.next("edit repo", "er").isPresent()) {
            String repoId = cmdLine.nextNonOption(NArgName.of("RepositoryName")).flatMap(NArg::asString).get();
            if (cmdLine.next("add repo", "ar").isPresent()) {
                String repositoryName = cmdLine.nextNonOption(NArgName.of("NewRepositoryName")).flatMap(NArg::asString).get();
                String location = cmdLine.nextNonOption(NArgName.of("folder")).flatMap(NArg::asString).get();

                NRepository editedRepo = workspace.findRepository(repoId).get();
                NRepository repo = editedRepo.config().addMirror(new NAddRepositoryOptions().setName(repositoryName).setLocation(repositoryName).setConfig(new NRepositoryConfig().setName(repositoryName).setLocation(NRepositoryLocation.of(location))));
                workspace.saveConfig();

            } else if (cmdLine.next("remove repo", "rr").isPresent()) {
                String location = cmdLine.nextNonOption(NArgName.of("RepositoryName")).flatMap(NArg::asString).get();
                NRepository editedRepo = workspace.findRepository(repoId).get();
                editedRepo.config().removeMirror(location);
                workspace.saveConfig();

            } else if (cmdLine.next("enable", "br").isPresent()) {
                NRepository editedRepo = workspace.findRepository(repoId).get();
                editedRepo.config().setEnabled(true);
                workspace.saveConfig();

            } else if (cmdLine.next("disable", "dr").isPresent()) {
                NRepository editedRepo = workspace.findRepository(repoId).get();
                editedRepo.config().setEnabled(true);
                workspace.saveConfig();
            } else if (cmdLine.next("list repos","repo list", "list repo", "lr").isPresent()) {
                NRepository editedRepo = workspace.findRepository(repoId).get();
                List<NRepository> linkRepositories = editedRepo.config().isSupportedMirroring() ? editedRepo.config().getMirrors() : Collections.emptyList();
                out.println(NMsg.ofC("%s sub repositories.", linkRepositories.size()));
                NTableFormat t = NTableFormat.of();
                NMutableTableModel m = NMutableTableModel.of();
                t.setValue(m);
                m.addHeaderCells("Id", "Enabled", "Type", "Location");
                while (cmdLine.hasNext()) {
                    if (!t.configureFirst(cmdLine)) {
                        cmdLine.setCommandName("config edit repo").throwUnexpectedArgument();
                    }
                }
                for (NRepository repository : linkRepositories) {
                    m.addRow(NText.ofStyledPrimary4(repository.getName()), repository.config().isEnabled() ? repository.isEnabled() ? NText.ofStyled("ENABLED", NTextStyle.success()) : NText.ofStyled("<RT-DISABLED>", NTextStyle.error()) : NText.ofStyled("<DISABLED>", NTextStyle.error()), repository.getRepositoryType(), repository.config().getLocation().toString());
                }
                out.print(t);
            } else if (cmdLine.next("-h", "-?", "--help").isPresent()) {
                out.println(NMsg.ofC("edit repository %s add repo ...", repoId));
                out.println(NMsg.ofC("edit repository %s remove repo ...", repoId));
                out.println(NMsg.ofC("edit repository %s list repos ...", repoId));
            } else {
                NRepository editedRepo = workspace.findRepository(repoId).get();
                if (NSettingsUserSubCommand.exec(editedRepo, cmdLine, autoSave)) {
                    //okkay
                } else {
                    throw new NIllegalArgumentException(NMsg.ofC("config edit repo: Unsupported command %s", cmdLine));
                }
            }
            return true;
        }
//        }
        return false;
    }

    private void doListRepo(NCmdLine cmdLine, NPrintStream out) {
        cmdLine.setCommandName("config list repos");
        NRef<String> parent = NRef.ofNull(String.class);
        NSession session = NSession.of();
        while (cmdLine.hasNext()) {
            while (cmdLine.hasNext()) {
                NArg aa = cmdLine.peek().get();
                boolean enabled = aa.isNonCommented();
                switch (aa.key()) {
                    case "--parent": {
                        cmdLine.withNextEntry((v, a) -> parent.set(v));
                        break;
                    }
                    default: {
                        if (!session.configureFirst(cmdLine)) {
                            if (aa.isOption()) {
                                cmdLine.throwUnexpectedArgument();
                            } else if (parent.isNotNull()) {
                                cmdLine.throwUnexpectedArgument();
                            } else {
                                parent.set(cmdLine.next().flatMap(NArg::asString).get());
                            }
                        }
                        break;
                    }
                }
            }
        }
        if (cmdLine.isExecMode()) {
            NWorkspace workspace = NWorkspace.of();
            List<NRepository> r = parent.isNull() ? workspace.getRepositories() : workspace.findRepository(parent.get())
                    .get().config().getMirrors();
            out.println(r.stream().map(x -> repoInfo(x, session.getOutputFormat().orDefault() != NContentType.TABLE && session.getOutputFormat().orDefault() != NContentType.PLAIN)).toArray());
        }
    }

    private void doRemoveRepo(NCmdLine cmdLine, NPrintStream out) {
        NRef<String> repositoryName = NRef.ofNull(String.class);
        NRef<String> parent = NRef.ofNull(String.class);
        NSession session = NSession.of();
        while (cmdLine.hasNext()) {
            NArg aa = cmdLine.peek().get();
            boolean enabled = aa.isNonCommented();
            switch (aa.key()) {
                case "--name": {
                    cmdLine.withNextEntry((v, a) -> repositoryName.set(v));
                    break;
                }
                case "--parent": {
                    cmdLine.withNextEntry((v, a) -> parent.set(v));
                    break;
                }
                default: {
                    if (!session.configureFirst(cmdLine)) {
                        if (aa.isOption()) {
                            cmdLine.throwUnexpectedArgument();
                        } else if (repositoryName.isNotNull()) {
                            cmdLine.throwUnexpectedArgument();
                        } else {
                            repositoryName.set(cmdLine.next().flatMap(NArg::asString).get());
                        }
                    }
                    break;
                }
            }
        }
        if (repositoryName.isNull()) {
            cmdLine.peek().get();
        }
        if (cmdLine.isExecMode()) {
            if (parent.isNull()) {
                NWorkspace.of().removeRepository(repositoryName.get());
            } else {
                NRepository p = NWorkspace.of().findRepository(parent.get()).get();
                p.config().removeMirror(repositoryName.get());
            }
            NWorkspace.of().saveConfig();
        }
    }

    private void doAddRepo(NCmdLine cmdLine, NPrintStream out) {
        class Data {
            String location = null;
            String repositoryName = null;
            String parent = null;
            Map<String, String> env = new LinkedHashMap<>();
        }
        NSession session = NSession.of();
        Data d = new Data();
        while (cmdLine.hasNext()) {
            NArg aa = cmdLine.peek().get();
            boolean enabled = aa.isNonCommented();
            switch (aa.key()) {
                case "-l":
                case "--location": {
                    cmdLine.withNextEntry((v, a) -> d.location = v);
                    break;
                }
                case "--name": {
                    cmdLine.withNextEntry((v, a) -> d.repositoryName = v);
                    break;
                }
                case "--parent": {
                    cmdLine.withNextEntry((v, a) -> d.parent = v);
                    break;
                }
                case "--env": {
                    cmdLine.withNextEntry((v, a) -> {
                        NArg vv = NArg.of(v);
                        d.env.put(vv.getKey() == null ? null : vv.key(),
                                vv.getValue() == null ? null : vv.getStringValue().get());
                    });
                    break;
                }
                default: {
                    if (!session.configureFirst(cmdLine)) {
                        if (aa.isOption()) {
                            cmdLine.throwUnexpectedArgument();
                        } else if (aa.isKeyValue()) {
                            NArg n = cmdLine.nextEntry().get();
                            d.repositoryName = n.getStringKey().get();
                            d.location = n.getStringValue().get();
                        } else {
                            d.location = cmdLine.next().flatMap(NArg::asString).get();
                            NAddRepositoryOptions ro = NRepositoryDB.of().getRepositoryOptionsByName(d.location).orNull();
                            String loc2 = ro==null?null:ro.getConfig().getLocation().getFullLocation();
                            if (loc2 != null) {
                                d.repositoryName = d.location;
                                d.location = loc2;
                            } else {
                                cmdLine.peek().get();
                            }
                        }
                    }
                    break;
                }
            }
        }
        if (d.repositoryName == null) {
            cmdLine.peek().get();
        }

        if (cmdLine.isExecMode()) {
            NRepository repo = null;
            NAddRepositoryOptions o = new NAddRepositoryOptions().setName(d.repositoryName).setLocation(d.repositoryName).setConfig(d.location == null ? null : new NRepositoryConfig().setName(d.repositoryName).setLocation(NRepositoryLocation.of(d.location)).setEnv(d.env));
            if (d.parent == null) {
                repo = NWorkspace.of().addRepository(o);
            } else {
                NRepository p = NWorkspace.of().findRepository(d.parent).get();
                repo = p.config().addMirror(o);
            }
            out.println(NMsg.ofC("repository %s added successfully", repo.getName()));
            NWorkspace.of().saveConfig();

        }
        cmdLine.setCommandName("config add repo").throwUnexpectedArgument();
    }

    private void enableRepo(NCmdLine cmdLine, Boolean autoSave, NSession session, boolean enableRepo) {
        NRef<String> repositoryName = NRef.ofNull(String.class);
        while (cmdLine.hasNext()) {
            NArg aa = cmdLine.peek().get();
            boolean enabled = aa.isNonCommented();
            switch (aa.key()) {
                case "--name": {
                    cmdLine.withNextEntry((v, a) -> repositoryName.set(v));
                    break;
                }
                default: {
                    if (!session.configureFirst(cmdLine)) {
                        if (aa.isOption()) {
                            cmdLine.throwUnexpectedArgument();
                        } else if (repositoryName.isNull()) {
                            repositoryName.set(cmdLine.next().flatMap(NArg::asString).get());
                        } else {
                            cmdLine.throwUnexpectedArgument();
                        }
                    }
                    break;
                }
            }
        }
        if (repositoryName.isNull()) {
            cmdLine.peek().get();
        }
        if (cmdLine.isExecMode()) {
            NWorkspace workspace = NWorkspace.of();
            NRepository editedRepo = workspace.findRepository(repositoryName.get()).get();
            editedRepo.config().setEnabled(enableRepo);
            workspace.saveConfig();
        }
    }

    public enum RepoStatus {
        enabled, disabled,
    }

    public static class RepoInfo {

        String name;
        String type;
        NPath location;
        boolean temporary;
        boolean preview;
        RepoStatus enabled;
        RepoInfo[] mirrors;
        String[] tags;

        public RepoInfo(String name, String type, NPath location, RepoStatus enabled, RepoInfo[] mirrors, boolean temporary, boolean preview,String[] tags) {
            this.name = name;
            this.type = type;
            this.location = location;
            this.enabled = enabled;
            this.mirrors = mirrors;
            this.preview = preview;
            this.temporary = temporary;
            this.tags = tags;
        }

        public RepoInfo() {
        }

        public RepoInfo setTemporary(boolean temporary) {
            this.temporary = temporary;
            return this;
        }

        public RepoInfo setPreview(boolean preview) {
            this.preview = preview;
            return this;
        }

        public RepoInfo setMirrors(RepoInfo[] mirrors) {
            this.mirrors = mirrors;
            return this;
        }

        public String[] getTags() {
            return tags;
        }

        public RepoInfo setTags(String[] tags) {
            this.tags = tags;
            return this;
        }

        public boolean isTemporary() {
            return temporary;
        }

        public boolean isPreview() {
            return preview;
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

        public NPath getLocation() {
            return location;
        }

        public void setLocation(NPath location) {
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
