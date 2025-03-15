package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStream;
import net.thevpc.nuts.util.NStringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public class GithubfsPath extends AbstractPathSPIAdapter {
    //"https://api.github.com/repos/" + userName + "/" + repo + "/contents"
    public static final String PROTOCOL = "githubfs";
    public static final String PREFIX = PROTOCOL + ":";
    private final Info info;
    private Object loaded;

    public GithubfsPath(String url, NWorkspace workspace) {
        this(url, null, workspace);
    }

    private GithubfsPath(String url, Info info, NWorkspace workspace) {
        super(NPath.of(url.substring(PREFIX.length())), workspace);
        if (!url.startsWith(PREFIX)) {
            throw new NUnsupportedArgumentException(NMsg.ofC("expected prefix '%s'",PREFIX));
        }
        this.info = info;
    }

    @Override
    public int hashCode() {
        return Objects.hash(PROTOCOL, super.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return super.equals(o);
    }

    @Override
    public String toString() {
        return PREFIX + ref.toString();
    }

    @Override
    public NStream<NPath> list(NPath basePath) {
        Object q = load();
        if (q instanceof Info[]) {
            return NStream.of((Info[]) q)
                    .map(NFunction.of(
                            (Info x) -> NPath.of(new GithubfsPath(
                                    PREFIX + ref.resolve(x.name).toString(),
                                    x, workspace))
                            ).withDesc(NEDesc.of("GithubfsPath::of"))
                    );
        }
        return NStream.ofEmpty();
    }

    @Override
    public NFormatSPI formatter(NPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getProtocol(NPath basePath) {
        return PROTOCOL;
    }


    @Override
    public NPath resolve(NPath basePath, NPath path) {
        return NPath.of(PREFIX + ref.resolve(path));
    }

    @Override
    public NPath resolveSibling(NPath basePath, NPath path) {
        return NPath.of(PREFIX + ref.resolveSibling(path));
    }

    @Override
    public boolean exists(NPath basePath) {
        if (info != null) {
            return true;
        }
        return load() != null;
    }

    @Override
    public long contentLength(NPath basePath) {
        Info o = _fileInfo();
        if (o != null) {
            return o.size;
        }
        return -1;
    }

    @Override
    public String getContentEncoding(NPath basePath) {
        NPath p = getDownloadPath();
        return p == null ? null : p.getContentEncoding();
    }

    @Override
    public String getContentType(NPath basePath) {
        NPath p = getDownloadPath();
        return p == null ? null : p.getContentType();
    }

    @Override
    public InputStream getInputStream(NPath basePath, NPathOption... options) {
        NPath p = getDownloadPath();
        if (p != null) {
            return p.getInputStream(options);
        }
        throw new NIOException(NMsg.ofC("not a file %s", basePath));
    }

    @Override
    public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
        throw new NIOException(NMsg.ofC("not writable %s", basePath));
    }

    @Override
    public Instant getLastModifiedInstant(NPath basePath) {
        NPath p = getDownloadPath();
        return p == null ? null : p.getLastModifiedInstant();
    }

    @Override
    public Instant getLastAccessInstant(NPath basePath) {
        NPath p = getDownloadPath();
        return p == null ? null : p.getLastAccessInstant();
    }

    @Override
    public Instant getCreationInstant(NPath basePath) {
        NPath p = getDownloadPath();
        return p == null ? null : p.getCreationInstant();
    }

    @Override
    public NPath getParent(NPath basePath) {
        if (isRoot(basePath)) {
            return null;
        }
        NPath p = ref.getParent();
        if (p == null) {
            return null;
        }
        return NPath.of(PREFIX + p);
    }

    @Override
    public NPath toAbsolute(NPath basePath, NPath rootPath) {
        if (isAbsolute(basePath)) {
            return basePath;
        }
        return NPath.of(PREFIX + basePath.toAbsolute(rootPath));
    }

    @Override
    public NPath normalize(NPath basePath) {
        return NPath.of(PREFIX + ref.normalize());
    }

    @Override
    public Boolean isName(NPath basePath) {
        return false;
    }

    @Override
    public Boolean isRoot(NPath basePath) {
        Info f = _fileInfo();
        if (f != null) {
            if (!"dir".equals(f.type)) {
                return false;
            }
            return "".equals(f.path);
        }
        Object a = load();
        if (a instanceof Info[]) {
            for (Info i : (Info[]) a) {
                return !i.path.contains("/");
            }
        }
        return false;
    }

    @Override
    public NPath getRoot(NPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        return NPath.of(PREFIX + ref.getRoot());
    }

    @Override
    public boolean copyTo(NPath basePath, NPath other, NPathOption... options) {
        NPath p = getDownloadPath();
        if (p != null) {
            p.copyTo(other, options);
        } else {
            NCp.of().from(basePath).to(other).addOptions(options).run();
        }
        return true;
    }

    private Object load() {
        if (loaded == null) {
            loaded = load(ref);
        }
        return loaded;
    }

    private Object load(NPath p) {
        NElements elems = NElements.of();
        NElement e = elems.json().parse(ref);
        if (e != null) {
            if (e.isArray()) {
                return NStream.of(elems.convert(e, Info[].class)).toArray(Info[]::new);
            } else if (e.isObject()) {
                return elems.convert(e, Info.class);
            }
        }
        return null;
    }

    private Info _fileInfo() {
        if (info != null) {
            return info;
        }
        Object o = load();
        if (o instanceof Info) {
            return (Info) o;
        }
        return null;
    }

    @Override
    public NPathType type(NPath basePath) {
        switch (_type()){
            case "dir":return NPathType.DIRECTORY;
            case "file":return NPathType.FILE;
            case "symlink":return NPathType.SYMBOLIC_LINK;
            case "":return NPathType.NOT_FOUND;
        }
        return NPathType.OTHER;
    }

    private String _type() {
        if (info != null) {
            return NStringUtils.trim(info.type);
        }
        Object a = load();
        if (a != null) {
            if (a instanceof Info) {
                return NStringUtils.trim(((Info) a).type);
            }
            if (a instanceof Info[]) {
                return "dir";
            }
        }
        return "";
    }

    private NPath getDownloadPath() {
        Info i = _fileInfo();
        if (i != null) {
            if (_type().equals("file")) {
                return NPath.of(i.download_url);
            }
        }
        return null;
    }

    @Override
    public boolean isLocal(NPath basePath) {
        return ref.isLocal();
    }

    public static class GithubfsFactory implements NPathFactorySPI {
        private final NWorkspace workspace;

        public GithubfsFactory(NWorkspace workspace) {
            this.workspace = workspace;
        }

        @Override
        public NCallableSupport<NPathSPI> createPath(String path, ClassLoader classLoader) {
            if (path.startsWith(PREFIX)) {
                return NCallableSupport.of(NConstants.Support.DEFAULT_SUPPORT, () -> new GithubfsPath(path, workspace));
            }
            return null;
        }

        @Override
        public int getSupportLevel(NSupportLevelContext context) {
            String path= context.getConstraints();
            try {
                if (path.startsWith(PREFIX)) {
                    return NConstants.Support.DEFAULT_SUPPORT;
                }
            } catch (Exception ex) {
                //ignore
            }
            return NConstants.Support.NO_SUPPORT;
        }
    }

    private static class MyPathFormat implements NFormatSPI {

        private final GithubfsPath p;

        public MyPathFormat(GithubfsPath p) {
            this.p = p;
        }
        @Override
        public String getName() {
            return "path";
        }

        public NText asFormattedString() {
            NTextBuilder sb = NTextBuilder.of();
            sb.append(PROTOCOL, NTextStyle.primary1());
            sb.append(":", NTextStyle.separator());
            sb.append(p.ref);
            return sb.build();
        }

        @Override
        public void print(NPrintStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NCmdLine cmdLine) {
            return false;
        }
    }

    private static class Info {
        String name;
        String path;
        String target;
        String sha;
        long size;
        String url;
        String html_url;
        String git_url;
        String download_url;
        String type;
        String content;
        String encoding;
        Map<String, String> self;
    }
}
