package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class DotfilefsPath extends AbstractPathSPIAdapter {

    public static final String PROTOCOL = "dotfilefs";
    public static final String PREFIX = PROTOCOL + ":";

    public static class DotfilefsFactory implements NPathFactory {
        private NWorkspace ws;

        public DotfilefsFactory(NWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NCallableSupport<NPathSPI> createPath(String path, NSession session, ClassLoader classLoader) {
            NSessionUtils.checkSession(ws, session);
            if (path.startsWith(PREFIX)) {
                return NCallableSupport.of(10, () -> new DotfilefsPath(path, session));
            }
            return null;
        }

        @Override
        public int getSupportLevel(NSupportLevelContext context) {
            String path= context.getConstraints();
            if (path.startsWith(PREFIX)) {
                return NConstants.Support.DEFAULT_SUPPORT;
            }
            return NConstants.Support.NO_SUPPORT;
        }
    }

    public DotfilefsPath(String url, NSession session) {
        super(NPath.of(url.substring(PREFIX.length()), session), session);
        if (!url.startsWith(PREFIX)) {
            throw new NUnsupportedArgumentException(session, NMsg.ofC("expected prefix '%s'", PREFIX));
        }
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
        if (!super.equals(o)) return false;
        return true;
    }

    @Override
    public String toString() {
        return PREFIX + ref.toString();
    }

    @Override
    public NStream<NPath> list(NPath basePath) {
        return NStream.of(parseHtml(ref.toString()).stream().map(
                x -> {
                    if (x.endsWith("/")) {
                        return NPath.of(PREFIX + ref.resolve(x), session);
                    }
                    return ref.resolve(x);
                }), session);
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
        return NPath.of(PREFIX + ref.resolve(path), session);
    }

    @Override
    public NPath resolve(NPath basePath, NPath path) {
        return NPath.of(PREFIX + ref.resolve(path), session);
    }

    @Override
    public NPath resolveSibling(NPath basePath, String path) {
        return NPath.of(PREFIX + ref.resolveSibling(path), session);
    }

    @Override
    public NPath resolveSibling(NPath basePath, NPath path) {
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
        if (NBlankable.isBlank(basePath.getLocation()) || basePath.getLocation().endsWith("/")) {
            return true;
        }
        String t = getContentType(basePath);
        //text/html;charset=UTF-8
        if (t != null) {
            if (t.endsWith("text/html")) {
                return true;
            }
            if (t.startsWith("text/html;")) {
                return true;
            }
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


    private List<String> parseHtml(String baseUrl) {
        boolean folders = true;
        boolean files = true;
        List<String> all = new ArrayList<>();
        InputStream foldersFileStream = null;
        String dotFilesUrl = baseUrl + "/" + CoreNConstants.Files.DOT_FILES;
        NVersion versionString = NVersion.of("0.5.5").get(session);
        try {
            session.getTerminal().printProgress(NMsg.ofC("%-8s %s", "browse", NPath.of(baseUrl, session).toCompressedForm()));
            foldersFileStream = NInputStreamMonitor.of(session).setSource(NPath.of(dotFilesUrl, session)).create();
            List<String> splitted = StringTokenizerUtils.splitNewLine(CoreIOUtils.loadString(foldersFileStream, true, session));
            for (String s : splitted) {
                s = s.trim();
                if (s.length() > 0) {
                    if (s.startsWith("#")) {
                        if (all.isEmpty()) {
                            s = s.substring(1).trim();
                            if (s.startsWith("version=")) {
                                versionString = NVersion.of(s.substring("version=".length()).trim()).get(session);
                            }
                        }
                    } else {
                        if (versionString.compareTo("0.5.7") < 0) {
                            if (files) {
                                all.add(s);
                            } else {
                                //ignore the rest
                                break;
                            }
                        } else {
                            //version 0.5.7 or later
                            if (s.endsWith("/")) {
                                s = s.substring(0, s.length() - 1);
                                int y = s.lastIndexOf('/');
                                if (y > 0) {
                                    s = s.substring(y + 1);
                                }
                                if (s.length() > 0 && !s.equals("..")) {
                                    if (folders) {
                                        if (!s.endsWith("/")) {
                                            s = s + "/";
                                        }
                                        all.add(s);
                                    }
                                }
                            } else {
                                if (files) {
                                    int y = s.lastIndexOf('/');
                                    if (y > 0) {
                                        s = s.substring(y + 1);
                                    }
                                    all.add(s);
                                }
                            }
                        }
                    }
                }
            }
        } catch (UncheckedIOException | NIOException ex) {
            NLogOp.of(DotfilefsPath.class, session).level(Level.FINE).verb(NLogVerb.FAIL)
                    .log(NMsg.ofJ("unable to navigate : file not found {0}", dotFilesUrl));
        }
        if (versionString.compareTo("0.5.7") < 0) {
            if (folders) {
                String[] foldersFileContent = null;
                String dotFolderUrl = baseUrl + "/" + CoreNConstants.Files.DOT_FOLDERS;
                try (InputStream stream = NInputStreamMonitor.of(session).setSource(NPath.of(dotFolderUrl, session))
                        .create()) {
                    foldersFileContent = StringTokenizerUtils.splitNewLine(CoreIOUtils.loadString(stream, true, session))
                            .stream().map(x -> x.trim()).filter(x -> x.length() > 0).toArray(String[]::new);
                } catch (IOException | UncheckedIOException | NIOException ex) {
                    NLogOp.of(DotfilefsPath.class, session).level(Level.FINE).verb(NLogVerb.FAIL)
                            .log(NMsg.ofJ("unable to navigate : file not found {0}", dotFolderUrl));
                }
                if (foldersFileContent != null) {
                    for (String folder : foldersFileContent) {
                        if (!folder.endsWith("/")) {
                            folder = folder + "/";
                        }
                        all.add(folder);
                    }
                }
            }
        }
        return all;
    }


    private static class MyPathFormat implements NFormatSPI {

        private final DotfilefsPath p;

        public MyPathFormat(DotfilefsPath p) {
            this.p = p;
        }

        public NString asFormattedString() {
            NTextBuilder sb = NTextBuilder.of(p.getSession());
            sb.append("html", NTextStyle.primary1());
            sb.append(":", NTextStyle.separator());
            sb.append(p.ref);
            return sb.build();
        }
        @Override
        public String getName() {
            return "path";
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

    @Override
    public boolean isLocal(NPath basePath) {
        return ref.isLocal();
    }
}
