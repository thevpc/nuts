package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsPathSPI;
import net.thevpc.nuts.spi.NutsUseDefault;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlfsPath extends AbstractPathSPIAdapter {

    public static final String PROTOCOL = "htmlfs";
    public static final String PREFIX = PROTOCOL + ":";

    public HtmlfsPath(String url, NutsSession session) {
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
                            return NutsPath.of(PREFIX + ref.resolve(x), session);
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
        if(NutsBlankable.isBlank(path)){
            return basePath;
        }
        if(!path.endsWith("/")) {
            return ref.resolve(path);
        }
        return NutsPath.of(PREFIX + ref.resolve(path), session);
    }

    @Override
    public NutsPath resolve(NutsPath basePath, NutsPath path) {
        if(NutsBlankable.isBlank(path)){
            return basePath;
        }
        if(!path.toString().endsWith("/")) {
            return  ref.resolve(path);
        }
        return NutsPath.of(PREFIX + ref.resolve(path), session);
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, String path) {
        if(NutsBlankable.isBlank(path)){
            return basePath;
        }
        if(!path.endsWith("/")) {
            return ref.resolve(path);
        }
        return NutsPath.of(PREFIX + ref.resolveSibling(path), session);
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, NutsPath path) {
        if(NutsBlankable.isBlank(path)){
            return basePath;
        }
        if(!path.toString().endsWith("/")) {
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
        if (NutsBlankable.isBlank(basePath.getLocation()) || basePath.getLocation().endsWith("/")) {
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
        List<String> t = parseHtmlTomcat(bytes);
        if (t != null) {
            return t;
        }
        t = parseHtmlApache(bytes);
        if (t != null) {
            return t;
        }
        return new ArrayList<>();
    }

    public List<String> parseHtmlApache(byte[] bytes) {
        int expected = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains("<!DOCTYPE HTML PUBLIC")) {
                    expected++;
                }else if(line.contains("<h1>Index of ")){
                    expected++;
                }else if(line.contains("<a href=\"?C=M;O=A\">Last modified</a>")){
                    expected++;
                }
            }
        } catch (Exception e) {
            //ignore
        }
        if (expected<2) {
            return null;
        }
        List<String> found = new ArrayList<>();
        Pattern pattern = Pattern.compile("<img src=\"/icons/[a-z.]+\" alt=\"\\[[a-zA-Z ]+]\"> +<a href=\"(?<href>[^\"]+)\">(?<hname>[^>]+)</a> +(?<d>[^ ]+) (?<h>[^ ]+) +(?<s>[^ ]+)");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                Matcher m = pattern.matcher(line);
                if(m.find()){
                    found.add(m.group("href"));
                }
            }
        } catch (Exception e) {
            //ignore
        }
        return found;
    }

    public List<String> parseHtmlTomcat(byte[] bytes) {
        boolean expectTomcat = false;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.contains("<hr class=\"line\"><h3>Apache Tomcat")) {
                    expectTomcat = true;
                }
            }
        } catch (Exception e) {
            //ignore
        }
        if (!expectTomcat) {
            return null;
        }
        InputStream html = new ByteArrayInputStream(bytes);
        List<String> found = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(html));
            State s = State.EXPECT_DOCTYPE;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                switch (s) {
                    case EXPECT_DOCTYPE: {
                        if (!line.isEmpty()) {
                            if (line.toLowerCase().startsWith("<!DOCTYPE html".toLowerCase())) {
                                s = State.EXPECT_BODY;
                            } else if (
                                    line.toLowerCase().startsWith("<html>".toLowerCase())
                                            || line.toLowerCase().startsWith("<html ".toLowerCase())
                            ) {
                                s = State.EXPECT_BODY;
                            } else {
                                return found;
                            }
                        }
                        break;
                    }
                    case EXPECT_BODY: {
                        if (!line.isEmpty()) {
                            if (
                                    line.toLowerCase()
                                            .startsWith("<body>".toLowerCase())
                                            || line.toLowerCase()
                                            .startsWith("<body ".toLowerCase())
                            ) {
                                s = State.EXPECT_PRE;
                            }
                        }
                        break;
                    }
                    case EXPECT_PRE: {
                        if (!line.isEmpty()) {
                            String lowLine = line;
                            if (
                                    lowLine.toLowerCase()
                                            .startsWith("<pre>".toLowerCase())
                                            || lowLine.toLowerCase()
                                            .startsWith("<pre ".toLowerCase())
                            ) {
                                //spring.io
                                if (lowLine.toLowerCase().startsWith("<pre>") && lowLine.toLowerCase().matches("<pre>name[ ]+last modified[ ]+size</pre>(<hr/>)?")) {
                                    //just ignore
                                } else if (lowLine.toLowerCase().startsWith("<pre>") && lowLine.toLowerCase().matches("<pre>[ ]*<a href=.*")) {
                                    lowLine = lowLine.substring("<pre>".length()).trim();
                                    if (lowLine.toLowerCase().startsWith("<a href=\"")) {
                                        int i0 = "<a href=\"".length();
                                        int i1 = lowLine.indexOf('\"', i0);
                                        if (i1 > 0) {
                                            found.add(lowLine.substring(i0, i1));
                                            s = State.EXPECT_HREF;
                                        } else {
                                            return found;
                                        }
                                    }
                                } else if (lowLine.toLowerCase().startsWith("<pre ")) {
                                    s = State.EXPECT_HREF;
                                } else {
                                    //ignore
                                }
                            } else if (lowLine.toLowerCase().matches("<td .*<strong>last modified</strong>.*</td>")) {
                                s = State.EXPECT_HREF;
                            }
                        }
                        break;
                    }
                    case EXPECT_HREF: {
                        if (!line.isEmpty()) {
                            String lowLine = line;
                            if (lowLine.toLowerCase().startsWith("</pre>".toLowerCase())) {
                                return found;
                            }
                            if (lowLine.toLowerCase().startsWith("</html>".toLowerCase())) {
                                return found;
                            }
                            if (lowLine.toLowerCase().startsWith("<a href=\"")) {
                                int i0 = "<a href=\"".length();
                                int i1 = lowLine.indexOf('\"', i0);
                                if (i1 > 0) {
                                    found.add(lowLine.substring(i0, i1));
                                } else {
                                    //ignore
                                }
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            NutsLoggerOp.of(HtmlfsPath.class, session)
                    .verb(NutsLogVerb.FAIL)
                    .level(Level.FINE)
                    .error(ex)
                    .log(NutsMessage.cstyle("unable to parse html"));

            //System.err.println(ex);
            //ignore
        }
        return found;
    }

    private enum State {
        EXPECT_DOCTYPE,
        EXPECT_BODY,
        EXPECT_PRE,
        EXPECT_HREF,
    }

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

//    public static void main(String[] args) {
//        try {
//            System.out.println(new WebHtmlListParser().parse(new URL("http://thevpc.net/maven/net/thevpc/nuts/nuts/").openStream()));
////            System.out.println(new WebHtmlListParser().parse(new URL("http://thevpc.net/maven/net/thevpc/nuts/nuts/0.8.1/").openStream()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

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

    @Override
    public boolean isLocal(NutsPath basePath) {
        return ref.isLocal();
    }
}
