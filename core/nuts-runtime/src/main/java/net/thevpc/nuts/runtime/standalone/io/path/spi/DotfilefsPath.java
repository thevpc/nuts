package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.format.NutsTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsConstants;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsPathSPI;
import net.thevpc.nuts.spi.NutsUseDefault;
import net.thevpc.nuts.text.NutsTextBuilder;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.util.NutsStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class DotfilefsPath extends AbstractPathSPIAdapter {

    public static final String PROTOCOL = "dotfilefs";
    public static final String PREFIX = PROTOCOL + ":";
    public static class DotfilefsFactory implements NutsPathFactory {
        private NutsWorkspace ws;

        public DotfilefsFactory(NutsWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsSessionUtils.checkSession(ws, session);
            if(path.startsWith(PREFIX)) {
                return NutsSupported.of(10, () -> new DotfilefsPath(path, session));
            }
            return null;
        }
    }
    public DotfilefsPath(String url, NutsSession session) {
        super(NutsPath.of(url.substring(PREFIX.length()), session), session);
        if (!url.startsWith(PREFIX)) {
            throw new NutsUnsupportedArgumentException(session, NutsMessage.cstyle("expected prefix '" + PREFIX + "'"));
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
    public NutsStream<NutsPath> list(NutsPath basePath) {
        return NutsStream.of(parseHtml(ref.toString()).stream().map(
                x -> {
                    if (x.endsWith("/")) {
                        return NutsPath.of(PREFIX + ref.resolve(x), session);
                    }
                    return ref.resolve(x);
                }), session);
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
        return false;
    }

    @Override
    public boolean isOther(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isDirectory(NutsPath basePath) {
        if(NutsBlankable.isBlank(basePath.getLocation()) || basePath.getLocation().endsWith("/")){
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


    private List<String> parseHtml(String baseUrl) {
        boolean folders=true;
        boolean files=true;
        List<String> all = new ArrayList<>();
        InputStream foldersFileStream = null;
        String dotFilesUrl = baseUrl + "/" + CoreNutsConstants.Files.DOT_FILES;
        NutsVersion versionString = NutsVersion.of("0.5.5").get(session);
        try {
            session.getTerminal().printProgress("%-8s %s", "browse",NutsPath.of(baseUrl,session).toCompressedForm());
            foldersFileStream = NutsInputStreamMonitor.of(session).setSource(dotFilesUrl).create();
            List<String> splitted = StringTokenizerUtils.splitNewLine(CoreIOUtils.loadString(foldersFileStream, true,session));
            for (String s : splitted) {
                s = s.trim();
                if (s.length() > 0) {
                    if (s.startsWith("#")) {
                        if (all.isEmpty()) {
                            s = s.substring(1).trim();
                            if (s.startsWith("version=")) {
                                versionString = NutsVersion.of(s.substring("version=".length()).trim()).get(session);
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
                                        if(!s.endsWith("/")){
                                            s=s+"/";
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
        } catch (UncheckedIOException | NutsIOException ex) {
            NutsLoggerOp.of(DotfilefsPath.class,session).level(Level.FINE).verb(NutsLoggerVerb.FAIL)
                    .log(NutsMessage.jstyle("unable to navigate : file not found {0}", dotFilesUrl));
        }
        if (versionString.compareTo("0.5.7") < 0) {
            if (folders) {
                String[] foldersFileContent = null;
                String dotFolderUrl = baseUrl + "/" + CoreNutsConstants.Files.DOT_FOLDERS;
                try (InputStream stream = NutsInputStreamMonitor.of(session).setSource(dotFolderUrl)
                        .create()) {
                    foldersFileContent = StringTokenizerUtils.splitNewLine(CoreIOUtils.loadString(stream, true,session))
                            .stream().map(x -> x.trim()).filter(x -> x.length() > 0).toArray(String[]::new);
                } catch (IOException | UncheckedIOException | NutsIOException ex) {
                    NutsLoggerOp.of(DotfilefsPath.class,session).level(Level.FINE).verb(NutsLoggerVerb.FAIL)
                            .log(NutsMessage.jstyle("unable to navigate : file not found {0}", dotFolderUrl));
                }
                if (foldersFileContent != null) {
                    for (String folder : foldersFileContent) {
                        if(!folder.endsWith("/")){
                            folder=folder+"/";
                        }
                        all.add(folder);
                    }
                }
            }
        }
        return all;
    }


    private static class MyPathFormat implements NutsFormatSPI {

        private final DotfilefsPath p;

        public MyPathFormat(DotfilefsPath p) {
            this.p = p;
        }

        public NutsString asFormattedString() {
            NutsTextBuilder sb = NutsTextBuilder.of(p.getSession());
            sb.append("html", NutsTextStyle.primary1());
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

    @Override
    public boolean isLocal(NutsPath basePath) {
        return ref.isLocal();
    }
}
