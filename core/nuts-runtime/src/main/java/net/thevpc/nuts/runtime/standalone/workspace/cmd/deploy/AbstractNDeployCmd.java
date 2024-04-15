package net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NString;
import net.thevpc.nuts.util.NBlankable;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractNDeployCmd extends NWorkspaceCmdBase<NDeployCmd> implements NDeployCmd {

    protected List<Result> result;
    protected NInputSource content;
    protected Object descriptor;
    protected String sha1;
    protected String descSha1;
    protected String fromRepository;
    protected String toRepository;
    protected List<String> parseOptions;
    protected final List<NId> ids = new ArrayList<>();

    protected static class Result {
        NString source;
        String repository;
        NId id;

        public Result(NId nid, String repository, NString source) {
            this.id = nid.getLongId();
            this.source = source;
            this.repository = repository;
        }
    }

    public AbstractNDeployCmd(NSession session) {
        super(session, "deploy");
    }
    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
    public List<String> getParseOptions() {
        return parseOptions;
    }

    public NDeployCmd setParseOptions(String[] parseOptions) {
        this.parseOptions = new ArrayList<>(Arrays.asList(parseOptions));
        return this;
    }

    public NDeployCmd parseOptions(String[] parseOptions) {
        this.parseOptions = new ArrayList<>(Arrays.asList(parseOptions));
        return this;
    }

    @Override
    public NDeployCmd setContent(InputStream stream) {
        checkSession();
        content = stream == null ? null : NInputSource.of(stream,session);
        return this;
    }

    @Override
    public NDeployCmd setContent(NPath path) {
        content = path;
        return this;
    }

    @Override
    public NDeployCmd setContent(byte[] content) {
        this.content = content == null ? null : NInputSource.of(content,session);
        return this;
    }

    @Override
    public NDeployCmd setContent(File file) {
        content = file == null ? null : NPath.of(file, getSession());
        invalidateResult();
        return this;
    }

    @Override
    public NDeployCmd setContent(Path file) {
        content = file == null ? null : NPath.of(file, getSession());
        invalidateResult();
        return this;
    }

    @Override
    public NDeployCmd setDescriptor(InputStream stream) {
        descriptor = stream;
        invalidateResult();
        return this;
    }

    @Override
    public NDeployCmd setDescriptor(String path) {
        descriptor = path;
        invalidateResult();
        return this;
    }

    @Override
    public NDeployCmd setDescriptor(File file) {
        descriptor = file;
        invalidateResult();
        return this;
    }

    @Override
    public NDeployCmd setDescriptor(URL url) {
        descriptor = url;
        invalidateResult();
        return this;
    }

    @Override
    public String getSha1() {
        return sha1;
    }

    @Override
    public NDeployCmd setSha1(String sha1) {
        this.sha1 = sha1;
        invalidateResult();
        return this;
    }

    public String getDescSha1() {
        return descSha1;
    }

    @Override
    public NDeployCmd setDescSha1(String descSha1) {
        this.descSha1 = descSha1;
        invalidateResult();
        return this;
    }

    public NInputSource getContent() {
        return content;
    }

    @Override
    public NDeployCmd setContent(NInputSource content) {
        this.content = content;
        return this;
    }

    @Override
    public NDeployCmd setContent(URL url) {
        content = url == null ? null : NPath.of(url, getSession());
        invalidateResult();
        return this;
    }

    public Object getDescriptor() {
        return descriptor;
    }

    @Override
    public NDeployCmd setDescriptor(NDescriptor descriptor) {
        this.descriptor = descriptor;
        invalidateResult();
        return this;
    }

    @Override
    public String getTargetRepository() {
        return toRepository;
    }

    @Override
    public NDeployCmd to(String repository) {
        return setTargetRepository(repository);
    }

    @Override
    public NDeployCmd setRepository(String repository) {
        return setTargetRepository(repository);
    }

    @Override
    public NDeployCmd setTargetRepository(String repository) {
        this.toRepository = repository;
        invalidateResult();
        return this;
    }

    @Override
    public NDeployCmd from(String repository) {
        return setSourceRepository(repository);
    }

    @Override
    public NDeployCmd setSourceRepository(String repository) {
        this.fromRepository = repository;
        invalidateResult();
        return this;
    }

    @Override
    public NDeployCmd setDescriptor(Path path) {
        this.descriptor = path;
        invalidateResult();
        return this;
    }

    @Override
    public List<NId> getResult() {
        if (result == null) {
            run();
        }
        return result.stream().map(x -> x.id).collect(Collectors.toList());
    }

    @Override
    protected void invalidateResult() {
        result = null;
    }

    protected void addResult(NId nid, String repository, NString source) {
        checkSession();
        if (result == null) {
            result = new ArrayList<>();
        }
        checkSession();
        result.add(new Result(nid, repository, source));
//        if (getSession().isPlainTrace()) {
//            getSession().getTerminal().out().resetLine().print(NMsg.ofC("Nuts %s deployed successfully to %s%n",
//                    nid,
//                    NutsTexts.of(session).ofStyled(toRepository == null ? "<default-repo>" : toRepository, NutsTextStyle.primary3())
//            );
//        }
    }

    @Override
    public NDeployCmd addIds(String... values) {
        checkSession();
        if (values != null) {
            for (String s : values) {
                if (!NBlankable.isBlank(s)) {
                    ids.add(NId.of(s).get(session));
                }
            }
        }
        return this;
    }

    @Override
    public NDeployCmd addIds(NId... value) {
        if (value != null) {
            for (NId s : value) {
                if (s != null) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NDeployCmd clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NDeployCmd addId(NId id) {
        if (id != null) {
            addId(id.toString());
        }
        return this;
    }

    @Override
    public NDeployCmd removeId(NId id) {
        if (id != null) {
            removeId(id.toString());
        }
        return this;
    }

    @Override
    public NDeployCmd removeId(String id) {
        checkSession();
        ids.remove(NId.of(id).get(session));
        return this;
    }

    @Override
    public NDeployCmd addId(String id) {
        checkSession();
        if (!NBlankable.isBlank(id)) {
            ids.add(NId.of(id).get(session));
        }
        return this;
    }

    @Override
    public List<NId> getIds() {
        return this.ids;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        switch (a.key()) {
            case "-d":
            case "--desc": {
                cmdLine.withNextEntry((v, r, s) -> setDescriptor(v));
                return true;
            }
            case "-s":
            case "--source":
            case "--from": {
                cmdLine.withNextEntry((v, r, s) -> from(v));
                return true;
            }
            case "-r":
            case "--target":
            case "--to": {
                cmdLine.withNextEntry((v, r, s) -> to(v));
                return true;
            }
            case "--desc-sha1": {
                cmdLine.withNextEntry((v, r, s) -> setDescSha1(v));
                return true;
            }
            case "--desc-sha1-file": {
                cmdLine.withNextEntry((v, r, s) -> this.setDescSha1(NPath.of(v, getSession()).readString()));
                return true;
            }
            case "--sha1": {
                cmdLine.withNextEntry((v, r, s) -> this.setSha1(v));
                return true;
            }
            case "--sha1-file": {
                cmdLine.withNextEntry((v, r, s) -> this.setSha1(NPath.of(v, getSession()).readString()));
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
                        setContent(NPath.of(idOrPath, session));
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
