package net.vpc.app.nuts.runtime.wscommands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNutsDeployCommand extends NutsWorkspaceCommandBase<NutsDeployCommand> implements NutsDeployCommand {

    protected List<NutsId> result;
    protected Object content;
    protected Object descriptor;
    protected String sha1;
    protected String descSha1;
    protected String fromRepository;
    protected String toRepository;
    protected String[] parseOptions;
    protected final List<NutsId> ids = new ArrayList<>();

    public AbstractNutsDeployCommand(NutsWorkspace ws) {
        super(ws, "deploy");
    }

    public String[] getParseOptions() {
        return parseOptions;
    }

    public NutsDeployCommand setParseOptions(String[] parseOptions) {
        this.parseOptions = parseOptions;
        return this;
    }

    public NutsDeployCommand parseOptions(String[] parseOptions) {
        this.parseOptions = parseOptions;
        return this;
    }

    @Override
    public NutsDeployCommand setContent(InputStream stream) {
        content = stream;
        return this;
    }

    @Override
    public NutsDeployCommand setContent(String path) {
        content = path;
        return this;
    }

    @Override
    public NutsDeployCommand setContent(File file) {
        content = file;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setContent(Path file) {
        content = file;
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

    public Object getContent() {
        return content;
    }

    @Override
    public NutsDeployCommand setContent(URL url) {
        content = url;
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
        return targetRepository(repository);
    }

    @Override
    public NutsDeployCommand targetRepository(String repository) {
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
        return sourceRepository(repository);
    }

    @Override
    public NutsDeployCommand sourceRepository(String repository) {
        return setSourceRepository(repository);
    }

    @Override
    public NutsDeployCommand setSourceRepository(String repository) {
        this.fromRepository = repository;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand content(InputStream value) {
        return setContent(value);
    }

    @Override
    public NutsDeployCommand content(String path) {
        return setContent(path);
    }

    @Override
    public NutsDeployCommand content(File file) {
        return setContent(file);
    }

    @Override
    public NutsDeployCommand content(Path file) {
        return setContent(file);
    }

    @Override
    public NutsDeployCommand descriptor(InputStream stream) {
        return setDescriptor(stream);
    }

    @Override
    public NutsDeployCommand descriptor(Path path) {
        return setDescriptor(path);
    }

    @Override
    public NutsDeployCommand setDescriptor(Path path) {
        this.descriptor = path;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand descriptor(String path) {
        return setDescriptor(path);
    }

    @Override
    public NutsDeployCommand descriptor(File file) {
        return setDescriptor(file);
    }

    @Override
    public NutsDeployCommand descriptor(URL url) {
        return setDescriptor(url);
    }

    @Override
    public NutsDeployCommand sha1(String sha1) {
        return setSha1(sha1);
    }

    @Override
    public NutsDeployCommand descSha1(String descSha1) {
        return setDescSha1(descSha1);
    }

    @Override
    public NutsDeployCommand content(URL url) {
        return setContent(url);
    }

    @Override
    public NutsDeployCommand descriptor(NutsDescriptor descriptor) {
        return setDescriptor(descriptor);
    }

    @Override
    public NutsDeployCommand repository(String repository) {
        return setTargetRepository(repository);
    }

    @Override
    public NutsId[] getResult() {
        if (result == null) {
            run();
        }
        return result.toArray(new NutsId[0]);
    }

    @Override
    protected void invalidateResult() {
        result = null;
    }

    protected void addResult(NutsId nid) {
        if (result == null) {
            result = new ArrayList<>();
        }
        result.add(nid);
        if (getValidSession().isPlainTrace()) {
            getValidSession().getTerminal().out().printf("Nuts %s deployed successfully to ==%s==%n", new NutsString(ws.id().value(nid).format()), toRepository == null ? "<default-repo>" : toRepository);
        }
    }

    @Override
    public NutsDeployCommand ids(String... values) {
        return addIds(values);
    }

    @Override
    public NutsDeployCommand addIds(String... values) {
        if (values != null) {
            for (String s : values) {
                if (!CoreStringUtils.isBlank(s)) {
                    ids.add(ws.id().parseRequired(s));
                }
            }
        }
        return this;
    }

    @Override
    public NutsDeployCommand ids(NutsId... values) {
        return addIds(values);
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
    public NutsDeployCommand id(NutsId id) {
        return addId(id);
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
    public NutsDeployCommand id(String id) {
        return addId(id);
    }

    @Override
    public NutsDeployCommand removeId(String id) {
        ids.remove(ws.id().parse(id));
        return this;
    }

    @Override
    public NutsDeployCommand addId(String id) {
        if (!CoreStringUtils.isBlank(id)) {
            ids.add(ws.id().parseRequired(id));
        }
        return this;
    }

    @Override
    public NutsId[] getIds() {
        return this.ids.toArray(new NutsId[0]);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "-d":
            case "--desc": {
                setDescriptor(cmdLine.nextString().getStringValue());
                return true;
            }
            case "-s":
            case "--source":
            case "--from": {
                from(cmdLine.nextString().getStringValue());
                return true;
            }
            case "-r":
            case "--target":
            case "--to": {
                to(cmdLine.nextString().getStringValue());
                return true;
            }
            case "--desc-sha1": {
                this.setDescSha1(cmdLine.nextString().getStringValue());
                return true;
            }
            case "--desc-sha1-file": {
                try {
                    this.setDescSha1(new String(Files.readAllBytes(Paths.get(cmdLine.nextString().getStringValue()))));
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                return true;
            }
            case "--sha1": {
                this.setSha1(cmdLine.nextString().getStringValue());
                return true;
            }
            case "--sha1-file": {
                try {
                    this.setSha1(new String(Files.readAllBytes(Paths.get(cmdLine.nextString().getStringValue()))));
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                return true;
            }
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
                if (a.isOption()) {
                    cmdLine.unexpectedArgument();
                } else {
                    cmdLine.skip();
                    String idOrPath = a.getString();
                    if (idOrPath.indexOf('/') >= 0 || idOrPath.indexOf('\\') >= 0) {
                        setContent(idOrPath);
                    } else {
                        id(idOrPath);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
