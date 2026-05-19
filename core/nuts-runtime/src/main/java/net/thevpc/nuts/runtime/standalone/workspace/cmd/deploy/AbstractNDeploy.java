package net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy;

import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NDeploy;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBase;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public abstract class AbstractNDeploy extends NWorkspaceCmdBase<NDeploy> implements NDeploy {

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
        NText source;
        String repository;
        NId id;

        public Result(NId nid, String repository, NText source) {
            this.id = nid.longId();
            this.source = source;
            this.repository = repository;
        }
    }

    public AbstractNDeploy() {
        super("deploy");
    }

    public List<String> getParseOptions() {
        return parseOptions;
    }

    public NDeploy setParseOptions(String[] parseOptions) {
        this.parseOptions = new ArrayList<>(Arrays.asList(parseOptions));
        return this;
    }

    public NDeploy parseOptions(String[] parseOptions) {
        this.parseOptions = new ArrayList<>(Arrays.asList(parseOptions));
        return this;
    }

    @Override
    public NDeploy content(InputStream stream) {
        content = stream == null ? null : NInputSource.of(stream);
        return this;
    }

    @Override
    public NDeploy content(NPath path) {
        content = path;
        return this;
    }

    @Override
    public NDeploy content(byte[] content) {
        this.content = content == null ? null : NInputSource.of(content);
        return this;
    }

    @Override
    public NDeploy content(File file) {
        content = file == null ? null : NPath.of(file);
        invalidateResult();
        return this;
    }

    @Override
    public NDeploy content(Path file) {
        content = file == null ? null : NPath.of(file);
        invalidateResult();
        return this;
    }

    @Override
    public NDeploy descriptor(InputStream stream) {
        descriptor = stream;
        invalidateResult();
        return this;
    }

    @Override
    public NDeploy descriptor(String path) {
        descriptor = path;
        invalidateResult();
        return this;
    }

    @Override
    public NDeploy descriptor(File file) {
        descriptor = file;
        invalidateResult();
        return this;
    }

    @Override
    public NDeploy descriptor(URL url) {
        descriptor = url;
        invalidateResult();
        return this;
    }

    @Override
    public String sha1() {
        return sha1;
    }

    @Override
    public NDeploy sha1(String sha1) {
        this.sha1 = sha1;
        invalidateResult();
        return this;
    }

    public String getDescSha1() {
        return descSha1;
    }

    @Override
    public NDeploy descriptorSha1(String descSha1) {
        this.descSha1 = descSha1;
        invalidateResult();
        return this;
    }

    public NInputSource content() {
        return content;
    }

    @Override
    public NDeploy content(NInputSource content) {
        this.content = content;
        return this;
    }

    @Override
    public NDeploy content(URL url) {
        content = url == null ? null : NPath.of(url);
        invalidateResult();
        return this;
    }

    public Object getDescriptor() {
        return descriptor;
    }

    @Override
    public NDeploy descriptor(NDescriptor descriptor) {
        this.descriptor = descriptor;
        invalidateResult();
        return this;
    }

    @Override
    public String targetRepository() {
        return toRepository;
    }

    @Override
    public NDeploy to(String repository) {
        return targetRepository(repository);
    }

    @Override
    public NDeploy repository(String repository) {
        return targetRepository(repository);
    }

    @Override
    public NDeploy targetRepository(String repository) {
        this.toRepository = repository;
        invalidateResult();
        return this;
    }

    @Override
    public NDeploy from(String repository) {
        return sourceRepository(repository);
    }

    @Override
    public NDeploy sourceRepository(String repository) {
        this.fromRepository = repository;
        invalidateResult();
        return this;
    }

    @Override
    public NDeploy descriptor(Path path) {
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

    protected void addResult(NId nid, String repository, NText source) {
        if (result == null) {
            result = new ArrayList<>();
        }
        result.add(new Result(nid, repository, source));
//        if (getSession().isPlainTrace()) {
//            getSession().getTerminal().out().resetLine().print(NMsg.ofC("Nuts %s deployed successfully to %s%n",
//                    nid,
//                    NTexts.of().ofStyled(toRepository == null ? "<default-repo>" : toRepository, NutsTextStyle.primary3())
//            );
//        }
    }

    @Override
    public NDeploy addIds(String... values) {
        if (values != null) {
            for (String s : values) {
                if (!NBlankable.isBlank(s)) {
                    ids.add(NId.get(s).get());
                }
            }
        }
        return this;
    }

    @Override
    public NDeploy addIds(NId... value) {
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
    public NDeploy clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NDeploy addId(NId id) {
        if (id != null) {
            addId(id.toString());
        }
        return this;
    }

    @Override
    public NDeploy removeId(NId id) {
        if (id != null) {
            removeId(id.toString());
        }
        return this;
    }

    @Override
    public NDeploy removeId(String id) {
        ids.remove(NId.get(id).get());
        return this;
    }

    @Override
    public NDeploy addId(String id) {
        if (!NBlankable.isBlank(id)) {
            ids.add(NId.get(id).get());
        }
        return this;
    }

    @Override
    public List<NId> ids() {
        return this.ids;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        switch (a.key()) {
            case "-d":
            case "--desc": {
                return cmdLine.matcher().matchEntry((v) -> descriptor(v.stringValue())).anyMatch();
            }
            case "-s":
            case "--source":
            case "--from": {
                return cmdLine.matcher().matchEntry((v) -> from(v.stringValue())).anyMatch();
            }
            case "-r":
            case "--target":
            case "--to": {
                return cmdLine.matcher().matchEntry((v) -> to(v.stringValue())).anyMatch();
            }
            case "--desc-sha1": {
                return cmdLine.matcher().matchEntry((v) -> descriptorSha1(v.stringValue())).anyMatch();
            }
            case "--desc-sha1-file": {
                return cmdLine.matcher().matchEntry((v) -> this.descriptorSha1(NPath.of(v.stringValue()).readString())).anyMatch();
            }
            case "--sha1": {
                return cmdLine.matcher().matchEntry((v) -> this.sha1(v.stringValue())).anyMatch();
            }
            case "--sha1-file": {
                return cmdLine.matcher().matchEntry((v) -> this.sha1(NPath.of(v.stringValue()).readString())).anyMatch();
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                if (a.isOption()) {
                    cmdLine.throwUnexpectedArgument();
                } else {
                    cmdLine.skip();
                    String idOrPath = a.asString().get();
                    if (idOrPath.indexOf('/') >= 0 || idOrPath.indexOf('\\') >= 0) {
                        content(NPath.of(idOrPath));
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
