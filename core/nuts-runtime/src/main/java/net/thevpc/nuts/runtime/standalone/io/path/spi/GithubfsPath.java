package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsPathSPI;
import net.thevpc.nuts.spi.NutsUseDefault;

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

    public GithubfsPath(String url, NutsSession session) {
        this(url, null, session);
    }

    private GithubfsPath(String url, Info info, NutsSession session) {
        super(NutsPath.of(url.substring(PREFIX.length()), session), session);
        if (!url.startsWith(PREFIX)) {
            throw new NutsUnsupportedArgumentException(session, NutsMessage.cstyle("expected prefix '" + PREFIX + "'"));
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
    public NutsStream<NutsPath> list(NutsPath basePath) {
        Object q = load();
        if (q instanceof Info[]) {
            return NutsStream.of((Info[]) q, session)
                    .map(NutsFunction.of(
                            x -> NutsPath.of(new GithubfsPath(
                                    PREFIX + ref.resolve(x.name).toString(),
                                    x, session), session)
                            , "GithubfsPath::of")
                    );
        }
        return NutsStream.ofEmpty(session);
    }

    @Override
    public NutsFormatSPI formatter(NutsPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getProtocol(NutsPath basePath) {
        return PROTOCOL;
    }

    @Override
    public NutsPath resolve(NutsPath basePath, String path) {
        return NutsPath.of(PREFIX + ref.resolve(path), session);
    }

    @Override
    public NutsPath resolve(NutsPath basePath, NutsPath path) {
        return NutsPath.of(PREFIX + ref.resolve(path), session);
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, String path) {
        return NutsPath.of(PREFIX + ref.resolveSibling(path), session);
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, NutsPath path) {
        return NutsPath.of(PREFIX + ref.resolveSibling(path), session);
    }

    public boolean isSymbolicLink(NutsPath basePath) {
        return "symlink".equals(_type());
    }

    @Override
    public boolean isOther(NutsPath basePath) {
        switch (_type()) {
            case "dir":
            case "file":
            case "symlink":
            case "": {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isDirectory(NutsPath basePath) {
        return "dir".equals(_type());
    }

    @Override
    public boolean isRegularFile(NutsPath basePath) {
        return "file".equals(_type());
    }

    @Override
    public boolean exists(NutsPath basePath) {
        if (info != null) {
            return true;
        }
        return load() != null;
    }

    @Override
    public long getContentLength(NutsPath basePath) {
        Info o = _fileInfo();
        if (o != null) {
            return o.size;
        }
        return -1;
    }

    @Override
    public String getContentEncoding(NutsPath basePath) {
        NutsPath p = getDownloadPath();
        return p == null ? null : p.getContentEncoding();
    }

    @Override
    public String getContentType(NutsPath basePath) {
        NutsPath p = getDownloadPath();
        return p == null ? null : p.getContentType();
    }

    @Override
    public InputStream getInputStream(NutsPath basePath) {
        NutsPath p = getDownloadPath();
        if (p != null) {
            return p.getInputStream();
        }
        throw new NutsIOException(session, NutsMessage.cstyle("not a file %s", basePath));
    }

    @Override
    public OutputStream getOutputStream(NutsPath basePath) {
        throw new NutsIOException(session, NutsMessage.cstyle("not writable %s", basePath));
    }

    @Override
    public Instant getLastModifiedInstant(NutsPath basePath) {
        NutsPath p = getDownloadPath();
        return p == null ? null : p.getLastModifiedInstant();
    }

    @Override
    public Instant getLastAccessInstant(NutsPath basePath) {
        NutsPath p = getDownloadPath();
        return p == null ? null : p.getLastAccessInstant();
    }

    @Override
    public Instant getCreationInstant(NutsPath basePath) {
        NutsPath p = getDownloadPath();
        return p == null ? null : p.getCreationInstant();
    }

    @Override
    public NutsPath getParent(NutsPath basePath) {
        if (isRoot(basePath)) {
            return null;
        }
        NutsPath p = ref.getParent();
        if (p == null) {
            return null;
        }
        return NutsPath.of(PREFIX + p, session);
    }

    @Override
    public NutsPath toAbsolute(NutsPath basePath, NutsPath rootPath) {
        if (isAbsolute(basePath)) {
            return basePath;
        }
        return NutsPath.of(PREFIX + basePath.toAbsolute(rootPath), session);
    }

    @Override
    public NutsPath normalize(NutsPath basePath) {
        return NutsPath.of(PREFIX + ref.normalize(), session);
    }

    @Override
    public boolean isName(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isRoot(NutsPath basePath) {
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
    public NutsPath getRoot(NutsPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        return NutsPath.of(PREFIX + ref.getRoot(), session);
    }

    @NutsUseDefault
    @Override
    public NutsStream<NutsPath> walk(NutsPath basePath, int maxDepth, NutsPathOption[] options) {
        return null;
    }

    @Override
    public void copyTo(NutsPath basePath, NutsPath other, NutsPathOption... options) {
        NutsPath p = getDownloadPath();
        if (p != null) {
            p.copyTo(other, options);
        } else {
            NutsCp.of(session).from(basePath).to(other).run();
        }
    }

    @NutsUseDefault
    @Override
    public void walkDfs(NutsPath basePath, NutsTreeVisitor<NutsPath> visitor, int maxDepth, NutsPathOption... options) {

    }

    private Object load() {
        if (loaded == null) {
            loaded = load(ref);
        }
        return loaded;
    }

    private Object load(NutsPath p) {
        NutsElements elems = NutsElements.of(session);
        NutsElement e = elems.json().parse(ref);
        if (e != null) {
            if (e.isArray()) {
                return NutsStream.of(elems.convert(e, Info[].class), session).toArray(Info[]::new);
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

    private String _type() {
        if (info != null) {
            return NutsUtilStrings.trim(info.type);
        }
        Object a = load();
        if (a != null) {
            if (a instanceof Info) {
                return NutsUtilStrings.trim(((Info) a).type);
            }
            if (a instanceof Info[]) {
                return "dir";
            }
        }
        return "";
    }

    private NutsPath getDownloadPath() {
        Info i = _fileInfo();
        if (i != null) {
            if (_type().equals("file")) {
                return NutsPath.of(i.download_url, session);
            }
        }
        return null;
    }

    @Override
    public boolean isLocal(NutsPath basePath) {
        return ref.isLocal();
    }

    public static class GithubfsFactory implements NutsPathFactory {
        private final NutsWorkspace ws;

        public GithubfsFactory(NutsWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsSessionUtils.checkSession(ws, session);
            if (path.startsWith(PREFIX)) {
                return NutsSupported.of(10, () -> new GithubfsPath(path, session));
            }
            return null;
        }
    }

    private static class MyPathFormat implements NutsFormatSPI {

        private final GithubfsPath p;

        public MyPathFormat(GithubfsPath p) {
            this.p = p;
        }

        public NutsString asFormattedString() {
            NutsTextBuilder sb = NutsTextBuilder.of(p.getSession());
            sb.append(PROTOCOL, NutsTextStyle.primary1());
            sb.append(":", NutsTextStyle.separator());
            sb.append(p.ref);
            return sb.build();
        }

        @Override
        public void print(NutsPrintStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NutsCommandLine commandLine) {
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
