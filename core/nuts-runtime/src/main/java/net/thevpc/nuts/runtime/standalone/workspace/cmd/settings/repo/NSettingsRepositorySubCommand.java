/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.repo;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.runtime.standalone.repository.DefaultNRepositoryDB;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user.NSettingsUserSubCommand;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.*;

import java.util.*;

/**
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NSettingsRepositorySubCommand extends AbstractNSettingsSubCommand {
    public NSettingsRepositorySubCommand() {
        super();
    }

    public static RepoInfo repoInfo(NRepository x, boolean tree) {
        return new RepoInfo(x.name(), x.config().type(), x.config().locationPath().toString(),
                x.config().isEnabled() ? RepoStatus.enabled : RepoStatus.disabled,
                (tree ? x.config().mirrors().stream().map(e -> repoInfo(e, tree)).toArray(RepoInfo[]::new) : null),
                x.config().isTemporary(),
                x.config().isPreview(),
                x.config().tags().toArray(new String[0])
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
            doAddRepo(cmdLine, out);
            return true;
        } else if (cmdLine.next("remove repo", "rr").isPresent()) {
            doRemoveRepo(cmdLine, out);

            return true;

        } else if (cmdLine.next("list repos", "lr").isPresent()) {
            doListRepo(cmdLine, out);
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

                NRepository editedRepo = workspace.getRepository(repoId).get();
                NRepository repo = editedRepo.config().addMirror(
                        new NRepositorySpec()
                                .name(repositoryName)
                                .location(repositoryName)
                                .sourceLocation(NRepositoryLocation.of(location))
                );
                workspace.saveConfig();

            } else if (cmdLine.next("remove repo", "rr").isPresent()) {
                String location = cmdLine.nextNonOption(NArgName.of("RepositoryName")).flatMap(NArg::asString).get();
                NRepository editedRepo = workspace.getRepository(repoId).get();
                editedRepo.config().removeMirror(location);
                workspace.saveConfig();

            } else if (cmdLine.next("enable", "br").isPresent()) {
                NRepository editedRepo = workspace.getRepository(repoId).get();
                editedRepo.config().enabled(true);
                workspace.saveConfig();

            } else if (cmdLine.next("disable", "dr").isPresent()) {
                NRepository editedRepo = workspace.getRepository(repoId).get();
                editedRepo.config().enabled(true);
                workspace.saveConfig();
            } else if (cmdLine.next("list repos", "repo list", "list repo", "lr").isPresent()) {
                NRepository editedRepo = workspace.getRepository(repoId).get();
                List<NRepository> linkRepositories = editedRepo.config().isSupportedMirroring() ? editedRepo.config().mirrors() : Collections.emptyList();
                out.println(NMsg.ofC("%s sub repositories.", linkRepositories.size()));

                while (cmdLine.hasNext()) {
                    if (!NSession.of().configureFirst(cmdLine)) {
                        cmdLine.commandName("config edit repo").throwUnexpectedArgument();
                    }
                }
                printPlainRepoList(
                        linkRepositories.stream().map(x->{
                            RepoInfo r = new RepoInfo();
                            r.name=x.name();
                            r.enabled=(x.config().isEnabled() && x.isEnabled())?RepoStatus.enabled :  RepoStatus.disabled;
                            r.type=x.repositoryType();
                            r.location=x.config().location().fullLocation();
                            r.preview=x.config().isPreview();
                            r.temporary=x.config().isTemporary();
                            TreeSet<String> t = new TreeSet<>();
                            if(r.preview){
                                t.add("@preview");
                            }
                            if(r.temporary){
                                t.add("@temp");
                            }
                            t.addAll(x.tags());
                            r.tags= t.toArray(new String[0]);
                            return r;
                        }).toArray(RepoInfo[]::new)
                        , out);
            } else if (cmdLine.next("-h", "-?", "--help").isPresent()) {
                out.println(NMsg.ofC("edit repository %s add repo ...", repoId));
                out.println(NMsg.ofC("edit repository %s remove repo ...", repoId));
                out.println(NMsg.ofC("edit repository %s list repos ...", repoId));
            } else {
                NRepository editedRepo = workspace.getRepository(repoId).get();
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
        cmdLine.commandName("config list repos");
        NRef<String> parent = NRef.ofNull(String.class);
        NRef<Boolean> longFormat = NRef.ofFalse();
        NSession session = NSession.of();
        while (cmdLine.hasNext()) {
            while (cmdLine.hasNext()) {
                NArg aa = cmdLine.peek().get();
                boolean enabled = aa.isUncommented();
                switch (aa.key()) {
                    case "--parent": {
                        cmdLine.matcher().withAny().matchEntry((v) -> parent.set(v.stringValue())).anyMatch();
                        break;
                    }
                    case "-l":
                    case "--long": {
                        cmdLine.matcher().withAny().matchFlag((v) -> longFormat.set(v.booleanValue())).anyMatch();
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
            List<NRepository> r = parent.isNull() ? workspace.repositories() : workspace.getRepository(parent.get())
                    .get().config().mirrors();
            RepoInfo[] array = r.stream().map(x -> repoInfo(x, session.outputFormat().orDefault() != NContentType.TABLE && session.outputFormat().orDefault() != NContentType.PLAIN)).toArray(RepoInfo[]::new);
            NContentType cf = NSession.of().outputFormat().orElse(NContentType.PLAIN);
            if (longFormat.get()) {
                if (cf == NContentType.PLAIN) {
                    for (RepoInfo repoInfo : array) {
                        out.println(repoInfo.name);
                    }
                } else {
                    out.println(array);
                }
            } else {
                if (cf == NContentType.PLAIN) {
                    printPlainRepoList(array, out);
                } else {
                    out.println(array);
                }
            }
        }
    }

    private void printPlainRepoList(RepoInfo[] array, NPrintStream out) {
        int[] colSizes = new int[5];
        for (RepoInfo repoInfo : array) {
            colSizes[0] = Math.max(colSizes[0], repoInfo.name.length());
            colSizes[1] = Math.max(colSizes[2], NStringUtils.strip(repoInfo.location).length());
            colSizes[3] = Math.max(colSizes[3], Arrays.toString(repoInfo.tags).length());
        }
        for (RepoInfo repoInfo : array) {
            NTextBuilder ll = NTextBuilder.of();
            if( repoInfo.enabled == RepoStatus.enabled){
                ll.append("[");
                ll.append(NText.ofStyled("x", NTextStyle.success()));
                ll.append("]");
            }else{
                ll.append("[ ]");
            }
            ll.append(" ");
            ll.append(NStringUtils.alignLeft(repoInfo.name, colSizes[0]));
            ll.append(" ");
            ll.append(
                    NText.ofStyled(NStringUtils.alignLeft(repoInfo.location, colSizes[1]), NTextStyle.path())
            );
            ll.append(" ");
            if(repoInfo.tags.length>0){
                ll.appendJoined(",",Arrays.asList(repoInfo.tags));
            }
            out.println(ll);
        }
    }

    private void doRemoveRepo(NCmdLine cmdLine, NPrintStream out) {
        NRef<String> repositoryName = NRef.ofNull(String.class);
        NRef<String> parent = NRef.ofNull(String.class);
        NSession session = NSession.of();
        while (cmdLine.hasNext()) {
            NArg aa = cmdLine.peek().get();
            boolean enabled = aa.isUncommented();
            switch (aa.key()) {
                case "--name": {
                    cmdLine.matcher().withAny().matchEntry((v) -> repositoryName.set(v.stringValue())).anyMatch();
                    break;
                }
                case "--parent": {
                    cmdLine.matcher().withAny().matchEntry((v) -> parent.set(v.stringValue())).anyMatch();
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
                NRepository p = NWorkspace.of().getRepository(parent.get()).get();
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
            boolean enabled = aa.isUncommented();
            switch (aa.key()) {
                case "-l":
                case "--location": {
                    cmdLine.matcher().withAny().matchEntry((v) -> d.location = v.stringValue()).anyMatch();
                    break;
                }
                case "--name": {
                    cmdLine.matcher().withAny().matchEntry((v) -> d.repositoryName = v.stringValue()).anyMatch();
                    break;
                }
                case "--parent": {
                    cmdLine.matcher().withAny().matchEntry((v) -> d.parent = v.stringValue()).anyMatch();
                    break;
                }
                case "--env": {
                    cmdLine.matcher().withAny().matchEntry((v) -> {
                        NArg vv = NArg.of(v.stringValue());
                        d.env.put(vv.getKey() == null ? null : vv.key(),
                                vv.literalValue() == null ? null : vv.getStringValue().get());
                    }).anyMatch();
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
                            DefaultNRepositoryDB db = NWorkspaceExt.of().getRepositoryModel().getDB();
                            NRepositorySpec ro = db.getDefinitionByName(d.location).orNull();
                            String loc2 = ro == null ? null : ro.sourceLocation().fullLocation();
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
            NRepositorySpec o = new NRepositorySpec()
                    .name(d.repositoryName)
                    .location(d.repositoryName)
                    .sourceLocation(d.location == null ? null : NRepositoryLocation.of(d.location))
                    .env(d.env);
            if (d.parent == null) {
                repo = NWorkspace.of().addRepository(o);
            } else {
                NRepository p = NWorkspace.of().getRepository(d.parent).get();
                repo = p.config().addMirror(o);
            }
            out.println(NMsg.ofC("repository %s added successfully", repo.name()));
            NWorkspace.of().saveConfig();

        }
        cmdLine.commandName("config add repo").throwUnexpectedArgument();
    }

    private void enableRepo(NCmdLine cmdLine, Boolean autoSave, NSession session, boolean enableRepo) {
        NRef<String> repositoryName = NRef.ofNull(String.class);
        while (cmdLine.hasNext()) {
            NArg aa = cmdLine.peek().get();
            boolean enabled = aa.isUncommented();
            switch (aa.key()) {
                case "--name": {
                    cmdLine.matcher().withAny().matchEntry((v) -> repositoryName.set(v.stringValue())).anyMatch();
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
            NRepository editedRepo = workspace.getRepository(repositoryName.get()).get();
            editedRepo.config().enabled(enableRepo);
            workspace.saveConfig();
        }
    }

    public enum RepoStatus {
        enabled, disabled,
    }

    public static class RepoInfo {

        String name;
        String type;
        String location;
        boolean temporary;
        boolean preview;
        RepoStatus enabled;
        RepoInfo[] mirrors;
        String[] tags;

        public RepoInfo(String name, String type, String location, RepoStatus enabled, RepoInfo[] mirrors, boolean temporary, boolean preview, String[] tags) {
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
