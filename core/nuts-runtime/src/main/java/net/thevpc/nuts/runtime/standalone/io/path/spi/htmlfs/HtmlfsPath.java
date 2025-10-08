package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.concurrent.NScorableCallable;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.io.path.spi.AbstractPathSPIAdapter;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NStream;
import net.thevpc.nuts.util.NUnsupportedArgumentException;

import java.io.*;
import java.util.*;

public class HtmlfsPath extends AbstractPathSPIAdapter {

    public static final String PROTOCOL = "htmlfs";
    public static final String PREFIX = PROTOCOL + ":";
    private static final HtmlfsParser[] PARSERS = {
            new MavenCentralHtmlfsParser(),
            new ApacheReposHtmlfsParser(),
            new TomcatWebServerHtmlfsParser(),
            new JettyWebServerHtmlfsParser(),
    };

    private String url;

    public HtmlfsPath(String url) {
        super(NPath.of(url.substring(PREFIX.length())));
        if (!url.startsWith(PREFIX)) {
            throw new NUnsupportedArgumentException(NMsg.ofC("expected prefix '%s'", PREFIX));
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
            return NStream.ofStream(parseHtml(q).stream().map(
                    x -> {
                        if (x.endsWith("/")) {
                            String a = PREFIX + ref.resolve(x);
                            if (!a.endsWith("/")) {
                                a += "/";
                            }
                            return NPath.of(new HtmlfsPath(a));
                        }
                        return ref.resolve(x);
                    }));
        } catch (IOException | NIOException | UncheckedIOException e) {
            return NStream.ofEmpty();
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
        return NPath.of(PREFIX + ref.resolve(path));
    }

    @Override
    public NPath resolveSibling(NPath basePath, String path) {
        if (NBlankable.isBlank(path)) {
            return basePath;
        }
        if (!path.endsWith("/")) {
            return ref.resolveSibling(path);
        }
        return NPath.of(PREFIX + ref.resolveSibling(path));
    }


    @Override
    public NPathType type(NPath basePath) {
        if (NBlankable.isBlank(basePath.getLocation()) || basePath.getLocation().endsWith("/")
                || this.url.endsWith("/")
        ) {
            return NPathType.DIRECTORY;
        }
        String t = getContentType(basePath);
        //text/html;charset=UTF-8
        if (t != null) {
            if (t.endsWith("text/html")) {
                return NPathType.DIRECTORY;
            }
            if (t.startsWith("text/html;")) {
                return NPathType.DIRECTORY;
            }
            return NPathType.FILE;
        }
        return NPathType.OTHER;
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
    public NPath getRoot(NPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        return NPath.of(PREFIX + ref.getRoot());
    }


    public List<String> parseHtml(InputStream html) {
        byte[] bytes = NCp.of().from(html).getByteArrayResult();
        return NScorable.<NScorableCallable<List<String>>>query()
                .withName(NMsg.ofC("html parser"))
                .fromStream(Arrays.stream(PARSERS)
                        .map(p -> {
                            try {
                                return p.parseHtmlTomcat(bytes);
                            } catch (Exception ex) {
                                NLog.of(HtmlfsPath.class)
                                        .log(NMsg.ofC("failed to parse using %s", p.getClass().getSimpleName()).asFinestFail(ex));
                            }
                            return null;
                        })
                ).getBest().map(NScorableCallable::call).orElse(Collections.emptyList());
    }

    @Override
    public boolean isLocal(NPath basePath) {
        return ref.isLocal();
    }

    public static class HtmlfsFactory implements NPathFactorySPI {

        public HtmlfsFactory() {
        }

        @Override
        public NScorableCallable<NPathSPI> createPath(String path, String protocol, ClassLoader classLoader) {
            if (path.startsWith(PREFIX)) {
                return NScorableCallable.of(DEFAULT_SCORE, () -> new HtmlfsPath(path));
            }
            return null;
        }

        @Override
        public int getScore(NScorableContext context) {
            String path = context.getCriteria();
            try {
                if (path.startsWith(PREFIX)) {
                    return DEFAULT_SCORE;
                }
            } catch (Exception ex) {
                //ignore
            }
            return UNSUPPORTED_SCORE;
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
}
