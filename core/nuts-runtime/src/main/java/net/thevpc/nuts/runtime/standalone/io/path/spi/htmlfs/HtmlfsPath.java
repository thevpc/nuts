package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.path.spi.AbstractPathSPIAdapter;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsPathSPI;
import net.thevpc.nuts.spi.NutsUseDefault;

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

    public HtmlfsPath(String url, NutsSession session) {
        super(NutsPath.of(url.substring(PREFIX.length()), session), session);
        if (!url.startsWith(PREFIX)) {
            throw new NutsUnsupportedArgumentException(session, NutsMessage.cstyle("expected prefix '%s'", PREFIX));
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
    public NutsStream<NutsPath> list(NutsPath basePath) {
        try (InputStream q = ref.getInputStream()) {
            return NutsStream.of(parseHtml(q).stream().map(
                    x -> {
                        if (x.endsWith("/")) {
                            String a = PREFIX + ref.resolve(x);
                            if (!a.endsWith("/")) {
                                a += "/";
                            }
                            return NutsPath.of(new HtmlfsPath(a, session), session);
                        }
                        return ref.resolve(x);
                    }), session);
        } catch (IOException e) {
            throw new NutsIOException(session, e);
        }
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
        if (NutsBlankable.isBlank(path)) {
            return basePath;
        }
        if (!path.endsWith("/")) {
            return ref.resolve(path);
        }
        String a = PREFIX + ref.resolve(path);
        if(!a.endsWith("/")){
            a+="/";
        }
        return NutsPath.of(new HtmlfsPath(a,session), session);
    }

    @Override
    public NutsPath resolve(NutsPath basePath, NutsPath path) {
        if (NutsBlankable.isBlank(path)) {
            return basePath;
        }
        if (!path.toString().endsWith("/")) {
            return ref.resolve(path);
        }
        return NutsPath.of(PREFIX + ref.resolve(path), session);
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, String path) {
        if (NutsBlankable.isBlank(path)) {
            return basePath;
        }
        if (!path.endsWith("/")) {
            return ref.resolve(path);
        }
        String a = PREFIX + ref.resolveSibling(path);
        if(!a.endsWith("/")){
            a+="/";
        }
        return NutsPath.of(new HtmlfsPath(a,session), session);
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, NutsPath path) {
        if (NutsBlankable.isBlank(path)) {
            return basePath;
        }
        if (!path.toString().endsWith("/")) {
            return ref.resolveSibling(path);
        }
        return NutsPath.of(PREFIX + ref.resolveSibling(path), session);
    }

    public boolean isSymbolicLink(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isOther(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isDirectory(NutsPath basePath) {
        if (NutsBlankable.isBlank(basePath.getLocation()) || basePath.getLocation().endsWith("/")
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
    public boolean isRegularFile(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean exists(NutsPath basePath) {
        String t = getContentType(basePath);
        return "text/html".equals(t);
    }

    @Override
    public NutsPath getParent(NutsPath basePath) {
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

    @NutsUseDefault
    @Override
    public void walkDfs(NutsPath basePath, NutsTreeVisitor<NutsPath> visitor, int maxDepth, NutsPathOption... options) {

    }

    public List<String> parseHtml(InputStream html) {
        byte[] bytes = NutsCp.of(session).from(html).getByteArrayResult();
        NutsSupported<List<String>> best = Arrays.stream(PARSERS).map(p -> {
                    try {
                        return p.parseHtmlTomcat(bytes, session);
                    } catch (Exception ex) {
                        NutsLoggerOp.of(HtmlfsPath.class, session)
                                .verb(NutsLogVerb.FAIL)
                                .level(Level.FINEST)
                                .error(ex)
                                .log(NutsMessage.cstyle("failed to parse using %s", p.getClass().getSimpleName()));
                    }
                    return null;
                }).filter(p -> NutsSupported.isValid(p)).max(Comparator.comparing(NutsSupported::getSupportLevel))
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
    public boolean isLocal(NutsPath basePath) {
        return ref.isLocal();
    }

//    public static void main(String[] args) {
//        try {
//            System.out.println(new WebHtmlListParser().parse(new URL("http://thevpc.net/maven/net/thevpc/nuts/nuts/").openStream()));
////            System.out.println(new WebHtmlListParser().parse(new URL("http://thevpc.net/maven/net/thevpc/nuts/nuts/0.8.1/").openStream()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static class HtmlfsFactory implements NutsPathFactory {
        private final NutsWorkspace ws;

        public HtmlfsFactory(NutsWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsWorkspaceUtils.checkSession(ws, session);
            if (path.startsWith(PREFIX)) {
                return NutsSupported.of(10, () -> new HtmlfsPath(path, session));
            }
            return null;
        }
    }

    private static class MyPathFormat implements NutsFormatSPI {

        private final HtmlfsPath p;

        public MyPathFormat(HtmlfsPath p) {
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
}
