package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.path.spi.AbstractPathSPIAdapter;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NLogOp;
import net.thevpc.nuts.util.NLogVerb;
import net.thevpc.nuts.util.NStream;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class HtmlfsPath extends AbstractPathSPIAdapter {

    public static final String PROTOCOL = "htmlfs";
    public static final String PREFIX = PROTOCOL + ":";
    public static final HtmlfsParser[] PARSERS = {
            new MavenCentralHtmlfsParser(),
            new ApacheReposHtmlfsParser(),
            new TomcatWebServerHtmlfsParser(),
            new JettyWebServerHtmlfsParser(),
    };
    private String url;

    public HtmlfsPath(String url, NSession session) {
        super(NPath.of(url.substring(PREFIX.length()), session), session);
        if (!url.startsWith(PREFIX)) {
            throw new NUnsupportedArgumentException(session, NMsg.ofC("expected prefix '%s'", PREFIX));
        }
        this.url = url;
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
        try (InputStream q = ref.getInputStream()) {
            return NStream.of(parseHtml(q).stream().map(
                    x -> {
                        if (x.endsWith("/")) {
                            String a = PREFIX + ref.resolve(x);
                            if (!a.endsWith("/")) {
                                a += "/";
                            }
                            return NPath.of(new HtmlfsPath(a, session), session);
                        }
                        return ref.resolve(x);
                    }), session);
        } catch (IOException|NIOException e) {
            return NStream.ofEmpty(getSession());
        }
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
    public NPath resolve(NPath basePath, String path) {
        if (NBlankable.isBlank(path)) {
            return basePath;
        }
        if (!path.endsWith("/")) {
            return ref.resolve(path);
        }
        String a = PREFIX + ref.resolve(path);
        if (!a.endsWith("/")) {
            a += "/";
        }
        return NPath.of(new HtmlfsPath(a, session), session);
    }

    @Override
    public NPath resolve(NPath basePath, NPath path) {
        if (NBlankable.isBlank(path)) {
            return basePath;
        }
        if (!path.toString().endsWith("/")) {
            return ref.resolve(path);
        }
        return NPath.of(PREFIX + ref.resolve(path), session);
    }

    @Override
    public NPath resolveSibling(NPath basePath, String path) {
        if (NBlankable.isBlank(path)) {
            return basePath;
        }
        if (!path.endsWith("/")) {
            return ref.resolve(path);
        }
        String a = PREFIX + ref.resolveSibling(path);
        if (!a.endsWith("/")) {
            a += "/";
        }
        return NPath.of(new HtmlfsPath(a, session), session);
    }

    @Override
    public NPath resolveSibling(NPath basePath, NPath path) {
        if (NBlankable.isBlank(path)) {
            return basePath;
        }
        if (!path.toString().endsWith("/")) {
            return ref.resolveSibling(path);
        }
        return NPath.of(PREFIX + ref.resolveSibling(path), session);
    }

    public boolean isSymbolicLink(NPath basePath) {
        return false;
    }

    @Override
    public boolean isOther(NPath basePath) {
        return false;
    }

    @Override
    public boolean isDirectory(NPath basePath) {
        if (NBlankable.isBlank(basePath.getLocation()) || basePath.getLocation().endsWith("/")
                || this.url.endsWith("/")
        ) {
            return true;
        }
        String t = getContentType(basePath);
        //text/html;charset=UTF-8
        if (t != null) {
            if (t.endsWith("text/html")) {
                return true;
            }
            return t.startsWith("text/html;");
        }
        return false;
    }

    @Override
    public boolean isRegularFile(NPath basePath) {
        return false;
    }

    @Override
    public boolean exists(NPath basePath) {
        String t = getContentType(basePath);
        return "text/html".equals(t);
    }

    @Override
    public NPath getParent(NPath basePath) {
        NPath p = ref.getParent();
        if (p == null) {
            return null;
        }
        return NPath.of(PREFIX + p, session);
    }

    @Override
    public NPath toAbsolute(NPath basePath, NPath rootPath) {
        if (isAbsolute(basePath)) {
            return basePath;
        }
        return NPath.of(PREFIX + basePath.toAbsolute(rootPath), session);
    }

    @Override
    public NPath normalize(NPath basePath) {
        return NPath.of(PREFIX + ref.normalize(), session);
    }

    @Override
    public boolean isName(NPath basePath) {
        return false;
    }

    @Override
    public NPath getRoot(NPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        return NPath.of(PREFIX + ref.getRoot(), session);
    }

    @NUseDefault
    @Override
    public NStream<NPath> walk(NPath basePath, int maxDepth, NPathOption[] options) {
        return null;
    }

    @NUseDefault
    @Override
    public void walkDfs(NPath basePath, NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {

    }

    public List<String> parseHtml(InputStream html) {
        byte[] bytes = NCp.of(session).from(html).getByteArrayResult();
        NSupported<List<String>> best = Arrays.stream(PARSERS).map(p -> {
                    try {
                        return p.parseHtmlTomcat(bytes, session);
                    } catch (Exception ex) {
                        NLogOp.of(HtmlfsPath.class, session)
                                .verb(NLogVerb.FAIL)
                                .level(Level.FINEST)
                                .error(ex)
                                .log(NMsg.ofC("failed to parse using %s", p.getClass().getSimpleName()));
                    }
                    return null;
                }).filter(p -> NSupported.isValid(p)).max(Comparator.comparing(NSupported::getSupportLevel))
                .orElse(null);
        if (best != null) {
            List<String> value = best.getValue();
            if (value != null) {
                return value;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isLocal(NPath basePath) {
        return ref.isLocal();
    }

//    public static void main(String[] args) {
//        try {
//            System.out.println(new WebHtmlListParser().parse(new URL("https://raw.githubusercontent.com/thevpc/nuts-preview/master/net/thevpc/nuts/nuts/").openStream()));
////            System.out.println(new WebHtmlListParser().parse(new URL("https://raw.githubusercontent.com/thevpc/nuts-preview/master/net/thevpc/nuts/nuts/0.8.1/").openStream()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static class HtmlfsFactory implements NPathFactory {
        private final NWorkspace ws;

        public HtmlfsFactory(NWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NSupported<NPathSPI> createPath(String path, NSession session, ClassLoader classLoader) {
            NSessionUtils.checkSession(ws, session);
            if (path.startsWith(PREFIX)) {
                return NSupported.of(DEFAULT_SUPPORT, () -> new HtmlfsPath(path, session));
            }
            return null;
        }

        @Override
        public int getSupportLevel(NSupportLevelContext context) {
            String path = context.getConstraints();
            try {
                if (path.startsWith(PREFIX)) {
                    return DEFAULT_SUPPORT;
                }
            } catch (Exception ex) {
                //ignore
            }
            return NO_SUPPORT;
        }
    }

    private static class MyPathFormat implements NFormatSPI {

        private final HtmlfsPath p;

        public MyPathFormat(HtmlfsPath p) {
            this.p = p;
        }

        @Override
        public String getName() {
            return "path";
        }

        public NString asFormattedString() {
            NTextBuilder sb = NTextBuilder.of(p.getSession());
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
}
