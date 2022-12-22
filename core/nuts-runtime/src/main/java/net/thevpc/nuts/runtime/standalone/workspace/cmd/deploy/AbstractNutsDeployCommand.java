package net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsIO;
import net.thevpc.nuts.io.NutsInputSource;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NutsWorkspaceCommandBase;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractNutsDeployCommand extends NutsWorkspaceCommandBase<NutsDeployCommand> implements NutsDeployCommand {

    protected List<Result> result;
    protected NutsInputSource content;
    protected Object descriptor;
    protected String sha1;
    protected String descSha1;
    protected String fromRepository;
    protected String toRepository;
    protected List<String> parseOptions;
    protected final List<NutsId> ids = new ArrayList<>();

    protected static class Result {
        NutsString source;
        String repository;
        NutsId id;

        public Result(NutsId nid, String repository, NutsString source) {
            this.id = nid.getLongId();
            this.source = source;
            this.repository = repository;
        }
    }

    public AbstractNutsDeployCommand(NutsWorkspace ws) {
        super(ws, "deploy");
    }

    public List<String> getParseOptions() {
        return parseOptions;
    }

    public NutsDeployCommand setParseOptions(String[] parseOptions) {
        this.parseOptions = new ArrayList<>(Arrays.asList(parseOptions));
        return this;
    }

    public NutsDeployCommand parseOptions(String[] parseOptions) {
        this.parseOptions = new ArrayList<>(Arrays.asList(parseOptions));
        return this;
    }

    @Override
    public NutsDeployCommand setContent(InputStream stream) {
        checkSession();
        content = stream == null ? null : NutsIO.of(session).createInputSource(stream);
        return this;
    }

    @Override
    public NutsDeployCommand setContent(NutsPath path) {
        content = path;
        return this;
    }

    @Override
    public NutsDeployCommand setContent(byte[] content) {
        this.content = content == null ? null : NutsIO.of(session).createInputSource(content);
        return this;
    }

    @Override
    public NutsDeployCommand setContent(File file) {
        content = file == null ? null : NutsPath.of(file, getSession());
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setContent(Path file) {
        content = file == null ? null : NutsPath.of(file, getSession());
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(InputStream stream) {
        descriptor = stream;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(String path) {
        descriptor = path;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(File file) {
        descriptor = file;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(URL url) {
        descriptor = url;
        invalidateResult();
        return this;
    }

    @Override
    public String getSha1() {
        return sha1;
    }

    @Override
    public NutsDeployCommand setSha1(String sha1) {
        this.sha1 = sha1;
        invalidateResult();
        return this;
    }

    public String getDescSha1() {
        return descSha1;
    }

    @Override
    public NutsDeployCommand setDescSha1(String descSha1) {
        this.descSha1 = descSha1;
        invalidateResult();
        return this;
    }

    public NutsInputSource getContent() {
        return content;
    }

    @Override
    public NutsDeployCommand setContent(NutsInputSource content) {
        this.content = content;
        return this;
    }

    @Override
    public NutsDeployCommand setContent(URL url) {
        content = url == null ? null : NutsPath.of(url, getSession());
        invalidateResult();
        return this;
    }

    public Object getDescriptor() {
        return descriptor;
    }

    @Override
    public NutsDeployCommand setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        invalidateResult();
        return this;
    }

    @Override
    public String getTargetRepository() {
        return toRepository;
    }

    @Override
    public NutsDeployCommand to(String repository) {
        return setTargetRepository(repository);
    }

    @Override
    public NutsDeployCommand setRepository(String repository) {
        return setTargetRepository(repository);
    }

    @Override
    public NutsDeployCommand setTargetRepository(String repository) {
        this.toRepository = repository;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand from(String repository) {
        return setSourceRepository(repository);
    }

    @Override
    public NutsDeployCommand setSourceRepository(String repository) {
        this.fromRepository = repository;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(Path path) {
        this.descriptor = path;
        invalidateResult();
        return this;
    }

    @Override
    public List<NutsId> getResult() {
        if (result == null) {
            run();
        }
        return result.stream().map(x -> x.id).collect(Collectors.toList());
    }

    @Override
    protected void invalidateResult() {
        result = null;
    }

    protected void addResult(NutsId nid, String repository, NutsString source) {
        checkSession();
        if (result == null) {
            result = new ArrayList<>();
        }
        checkSession();
        result.add(new Result(nid, repository, source));
//        if (getSession().isPlainTrace()) {
//            getSession().getTerminal().out().resetLine().printf("Nuts %s deployed successfully to %s%n",
//                    nid,
//                    NutsTexts.of(session).ofStyled(toRepository == null ? "<default-repo>" : toRepository, NutsTextStyle.primary3())
//            );
//        }
    }

    @Override
    public NutsDeployCommand addIds(String... values) {
        checkSession();
        if (values != null) {
            for (String s : values) {
                if (!NutsBlankable.isBlank(s)) {
                    ids.add(NutsId.of(s).get(session));
                }
            }
        }
        return this;
    }

    @Override
    public NutsDeployCommand addIds(NutsId... value) {
        if (value != null) {
            for (NutsId s : value) {
                if (s != null) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NutsDeployCommand clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NutsDeployCommand addId(NutsId id) {
        if (id != null) {
            addId(id.toString());
        }
        return this;
    }

    @Override
    public NutsDeployCommand removeId(NutsId id) {
        if (id != null) {
            removeId(id.toString());
        }
        return this;
    }

    @Override
    public NutsDeployCommand removeId(String id) {
        checkSession();
        ids.remove(NutsId.of(id).get(session));
        return this;
    }

    @Override
    public NutsDeployCommand addId(String id) {
        checkSession();
        if (!NutsBlankable.isBlank(id)) {
            ids.add(NutsId.of(id).get(session));
        }
        return this;
    }

    @Override
    public List<NutsId> getIds() {
        return this.ids;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        switch (a.key()) {
            case "-d":
            case "--desc": {
                cmdLine.withNextString((v, r, s) -> setDescriptor(v));
                return true;
            }
            case "-s":
            case "--source":
            case "--from": {
                cmdLine.withNextString((v, r, s) -> from(v));
                return true;
            }
            case "-r":
            case "--target":
            case "--to": {
                cmdLine.withNextString((v, r, s) -> to(v));
                return true;
            }
            case "--desc-sha1": {
                cmdLine.withNextString((v, r, s) -> setDescSha1(v));
                return true;
            }
            case "--desc-sha1-file": {
                cmdLine.withNextString((v, r, s) -> this.setDescSha1(NutsPath.of(v, getSession()).readString()));
                return true;
            }
            case "--sha1": {
                cmdLine.withNextString((v, r, s) -> this.setSha1(v));
                return true;
            }
            case "--sha1-file": {
                cmdLine.withNextString((v, r, s) -> this.setSha1(NutsPath.of(v, getSession()).readString()));
                return true;
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                if (a.isOption()) {
                    cmdLine.throwUnexpectedArgument();
                } else {
                    cmdLine.skip();
                    String idOrPath = a.asString().get(session);
                    if (idOrPath.indexOf('/') >= 0 || idOrPath.indexOf('\\') >= 0) {
                        setContent(NutsPath.of(idOrPath, session));
                    } else {
                        addId(idOrPath);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
