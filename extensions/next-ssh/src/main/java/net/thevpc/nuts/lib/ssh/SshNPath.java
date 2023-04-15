package net.thevpc.nuts.lib.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NTreeVisitResult;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NConnexionString;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NStream;
import net.thevpc.nuts.util.NStringMapFormat;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

class SshNPath implements NPathSPI {

    private final NConnexionString path;
    private final NSession session;
    private SshListener listener;

    public SshNPath(NConnexionString path, NSession session) {
        this.path = path;
        this.session = session;
    }

    public static String getURLParentPath(String ppath) {
        if (ppath == null) {
            return null;
        }
        while (ppath.endsWith("/")) {
            ppath = ppath.substring(0, ppath.length() - 1);
        }
        if (ppath.isEmpty()) {
            return null;
        }
        int i = ppath.lastIndexOf('/');
        if (i <= 0) {
            ppath = "/";
        } else {
            ppath = ppath.substring(0, i + 1);
        }
        return ppath;
    }

    @Override
    public NStream<NPath> list(NPath basePath) {
        try (SShConnection c = new SShConnection(path, getSession())
                .addListener(listener)) {
            c.grabOutputString();
            int i = c.execStringCommand("ls " + path.getPath());
            if (i == 0) {
                String[] s = c.getOutputString().split("[\n|\r]");
                return NStream.of(s, session).map(
                        NFunction.of(
                                x -> {
                                    String cc = path.getPath();
                                    if (!cc.endsWith("/")) {
                                        cc += "/";
                                    }
                                    cc += x;
                                    return NPath.of(path.setPath(cc).toString(), getSession());
                                }, "NPath::of"

                        )
                );
            }
        } catch (Exception e) {
            //return false;
        }
        return NStream.ofEmpty(session);
    }

    @Override
    public NFormatSPI formatter(NPath basePath) {
        return new NFormatSPI() {
            @Override
            public String getName() {
                return "path";
            }

            @Override
            public void print(NPrintStream out) {
                //should implement better formatting...
                NTextStyle _sep = NTextStyle.separator();
                NTextStyle _path = NTextStyle.path();
                NTextStyle _nbr = NTextStyle.number();
//        if(true) {
                NTexts text = NTexts.of(session);
                NTextBuilder sb = text.ofBuilder();
                String user = path.getUser();
                String host = path.getHost();
                int port = NLiteral.of(path.getPort()).asInt().orElse(-1);
                String path0 = path.getPath();
                String password = path.getPassword();
                String keyFile = NStringMapFormat.URL_FORMAT.parse(path.getQueryString())
                        .orElse(Collections.emptyMap()).get("key-file");

                sb.append(text.ofStyled("ssh://", _sep));
                if (!(user == null || user.trim().length() == 0)) {
                    sb.append(user)
                            .append(text.ofStyled("@", _sep));
                }
                sb.append(host);
                if (port >= 0) {
                    sb.append(text.ofStyled(":", _sep))
                            .append(text.ofStyled(String.valueOf(port), _nbr));
                }
                if (!path0.startsWith("/")) {
                    sb.append(text.ofStyled('/' + path0, _path));
                } else {
                    sb.append(text.ofStyled(path0, _path));
                }
                if (password != null || keyFile != null) {
                    sb.append(text.ofStyled("?", _sep));
                    boolean first = true;
                    if (password != null) {
                        first = false;
                        sb
                                .append("password")
                                .append(text.ofStyled("=", _sep))
                                .append(password);
                    }
                    if (keyFile != null) {
                        if (!first) {
                            sb.append(text.ofStyled(",", _sep));
                        }
                        sb
                                .append("key-file")
                                .append(text.ofStyled("=", _sep))
                                .append(keyFile);
                    }
                }
                out.print(sb.toText());
            }

            @Override
            public boolean configureFirst(NCmdLine cmdLine) {
                return false;
            }
        };
    }

    @Override
    public String getName(NPath basePath) {
        String loc = getLocation(basePath);
        return loc == null ? "" : Paths.get(loc).getFileName().toString();
    }

    @Override
    public String getProtocol(NPath basePath) {
        return "ssh";
    }

    @Override
    public NPath resolve(NPath basePath, String path) {
        return NPath.of(this.path.toString(), getSession());
    }

    @Override
    public NPath resolve(NPath basePath, NPath path) {
        return NPath.of(this.path.toString(), getSession());
    }

    @Override
    public NPath resolveSibling(NPath basePath, String path) {
        return NPath.of(this.path.toString(), getSession());
    }

    @Override
    public NPath resolveSibling(NPath basePath, NPath path) {
        return NPath.of(this.path.toString(), getSession());
    }

    @Override
    public NPath toCompressedForm(NPath basePath) {
        return null;
    }

    @Override
    public URL toURL(NPath basePath) {
        throw new NIOException(getSession(), NMsg.ofC("unable to resolve url from %s", toString()));
    }

    @Override
    public Path toFile(NPath basePath) {
        throw new NIOException(getSession(), NMsg.ofC("unable to resolve file from %s", toString()));
    }

    //    @Override
//    public NPath resolve(String path) {
//        String[] others = Arrays.stream(NutsUtilStrings.trim(path).split("[/\\\\]"))
//                .filter(x -> x.length() > 0).toArray(String[]::new);
//        if (others.length > 0) {
//            StringBuilder loc = new StringBuilder(this.path.getPath());
//            if (loc.length() == 0 || loc.charAt(loc.length() - 1) != '/') {
//                loc.append('/');
//            }
//            loc.append(String.join("/", others));
//            return
//                    NPath.of(
//                            SshPath.toString(
//                                    this.path.getHost(),
//                                    this.path.getPort(),
//                                    loc.toString(),
//                                    this.path.getUser(),
//                                    this.path.getPassword(),
//                                    this.path.getKeyFile()
//                            ), getSession());
//        }
//        return NPath.of(toString(), getSession());
//    }
    @Override
    public boolean isSymbolicLink(NPath basePath) {
        return false;
    }

    @Override
    public boolean isOther(NPath basePath) {
        return false;
    }

    @Override
    public boolean isDirectory(NPath basePath) {
        try (SShConnection c = new SShConnection(path, getSession())
                .addListener(listener)) {
            c.grabOutputString();
            int i = c.execStringCommand("file " + path.getPath());
            if (i > 0) {
                return false;
            }
            String s = c.getOutputString();
            int ii = s.indexOf(':');
            if (ii > 0) {
                return s.substring(i + 1).trim().equals("directory");
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isLocal(NPath basePath) {
        return false;
    }

    @Override
    public boolean isRegularFile(NPath basePath) {
        try (SShConnection c = new SShConnection(path, getSession())
                .addListener(listener)) {
            c.grabOutputString();
            int i = c.execStringCommand("file " + path.getPath());
            if (i > 0) {
                return false;
            }
            String s = c.getOutputString();
            int ii = s.indexOf(':');
            if (ii > 0) {
                return !s.substring(i + 1).trim().equals("directory");
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean exists(NPath basePath) {
        throw new NIOException(getSession(), NMsg.ofC("not supported exists for %s", toString()));
    }

    @Override
    public long getContentLength(NPath basePath) {
        return -1;
    }

    @Override
    public String getContentEncoding(NPath basePath) {
        return null;
    }

    @Override
    public String getContentType(NPath basePath) {
        return null;
    }

    @Override
    public String getLocation(NPath basePath) {
        return path.getPath();
    }

    @Override
    public InputStream getInputStream(NPath basePath, NPathOption... options) {
        return new SshFileInputStream(path, session);
    }

    @Override
    public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
        return new SshFileOutputStream2(path, session, false);
    }

    @Override
    public NSession getSession() {
        return session;
    }

    public void delete(NPath basePath, boolean recurse) {
        try (SShConnection session = new SShConnection(path, getSession())
                .addListener(listener)) {
            session.rm(path.getPath(), recurse);
        }
    }

    public void mkdir(boolean parents, NPath basePath) {
        try (SShConnection c = new SShConnection(path, getSession())
                .addListener(listener)) {
            c.mkdir(path.getPath(), parents);
        }
    }

    @Override
    public Instant getLastModifiedInstant(NPath basePath) {
        return null;
    }

    @Override
    public Instant getLastAccessInstant(NPath basePath) {
        return null;
    }

    @Override
    public Instant getCreationInstant(NPath basePath) {
        return null;
    }

    @Override
    public NPath getParent(NPath basePath) {
        String loc = getURLParentPath(this.path.getPath());
        if (loc == null) {
            return null;
        }
        return NPath.of(path.toString(), getSession());
    }

    @Override
    public NPath toAbsolute(NPath basePath, NPath rootPath) {
        return basePath;
    }

    @Override
    public NPath normalize(NPath basePath) {
        return NPath.of(toString(), getSession());
    }

    @Override
    public boolean isAbsolute(NPath basePath) {
        return true;
    }

    @Override
    public String owner(NPath basePath) {
        return null;
    }

    @Override
    public String group(NPath basePath) {
        return null;
    }

    @Override
    public Set<NPathPermission> getPermissions(NPath basePath) {
        return Collections.emptySet();
    }

    @Override
    public void setPermissions(NPath basePath, NPathPermission... permissions) {
    }

    @Override
    public void addPermissions(NPath basePath, NPathPermission... permissions) {
    }

    @Override
    public void removePermissions(NPath basePath, NPathPermission... permissions) {
    }

    @Override
    public boolean isName(NPath basePath) {
        return false;
    }

    @Override
    public int getPathCount(NPath basePath) {
        String location = getLocation(basePath);
        if (NBlankable.isBlank(location)) {
            return 0;
        }
        return NPath.of(location, getSession()).getPathCount();
    }

    @Override
    public boolean isRoot(NPath basePath) {
        String loc = getLocation(basePath);
        if (NBlankable.isBlank(loc)) {
            return false;
        }
        switch (loc) {
            case "/":
            case "\\\\":
                return true;
        }
        return NPath.of(loc, getSession()).isRoot();
    }

    @Override
    public NPath getRoot(NPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        return basePath.getParent().getRoot();
    }

    @Override
    public NStream<NPath> walk(NPath basePath, int maxDepth, NPathOption[] options) {
        EnumSet<NPathOption> optionsSet = EnumSet.noneOf(NPathOption.class);
        optionsSet.addAll(Arrays.asList(options));
        try (SShConnection c = new SShConnection(path, getSession())
                .addListener(listener)) {
            c.grabOutputString();
            StringBuilder cmd = new StringBuilder();
            cmd.append("ls");
            if (optionsSet.contains(NPathOption.FOLLOW_LINKS)) {
                //all
            } else {
                cmd.append(" -type d,f");
            }
            if (maxDepth > 0 && maxDepth != Integer.MAX_VALUE) {
                cmd.append(" -maxdepth ").append(maxDepth);
            }
            cmd.append(" ").append(path.getPath());
            int i = c.execStringCommand(cmd.toString());
            if (i == 0) {
                String[] s = c.getOutputString().split("[\n|\r]");
                return NStream.of(s, session).map(
                        NFunction.of(
                                x -> {
                                    String cc = path.getPath();
                                    if (!cc.endsWith("/")) {
                                        cc += "/";
                                    }
                                    cc += x;
                                    return NPath.of(path.setPath(cc).toString(), getSession());
                                },
                                "NPath::of"
                        )
                );
            }
        } catch (Exception e) {
            //return false;
        }
        return NStream.ofEmpty(session);
    }

    @Override
    public NPath subpath(NPath basePath, int beginIndex, int endIndex) {
        return NPath.of(this.path.toString(), getSession());
    }

    @Override
    public List<String> getItems(NPath basePath) {
        return NPath.of(getLocation(basePath), getSession()).getItems();
    }

    @Override
    public void moveTo(NPath basePath, NPath other, NPathOption... options) {
        if (other.toString().startsWith("ssh:")) {
            NConnexionString sp = NConnexionString.of(other.toString()).get();
            if (
                    Objects.equals(sp.getHost(), path.getHost())
                            && Objects.equals(sp.getUser(), path.getUser())
            ) {
                int r = -1;
                try (SShConnection c = new SShConnection(path, getSession())
                        .addListener(listener)) {
                    c.grabOutputString();
                    r = c.execStringCommand("mv " + path.getPath() + " " + sp);
                }
                if (r != 0) {
                    throw new NIOException(session, NMsg.ofC("unable to move %s", this));
                }
                return;
            }
        }
        copyTo(basePath, other, options);
        delete(basePath, true);
    }

    @Override
    public void copyTo(NPath basePath, NPath other, NPathOption... options) {
        NCp.of(session).from(basePath).to(other).run();
    }

    @Override
    public void walkDfs(NPath basePath, NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {
        for (NPath x : walk(basePath, maxDepth, options)) {
            if (x.isDirectory()) {
                NTreeVisitResult r = visitor.preVisitDirectory(x, session);
                switch (r) {
                    case CONTINUE: {
                        break;
                    }
                    case TERMINATE: {
                        return;
                    }
                    case SKIP_SIBLINGS:
                    case SKIP_SUBTREE: {
                        throw new NIllegalArgumentException(session, NMsg.ofC("unsupported %s", r));
                    }
                }
            } else if (x.isRegularFile()) {
                NTreeVisitResult r = visitor.visitFile(x, session);
                switch (r) {
                    case CONTINUE: {
                        break;
                    }
                    case TERMINATE: {
                        return;
                    }
                    case SKIP_SIBLINGS:
                    case SKIP_SUBTREE: {
                        throw new NIllegalArgumentException(session, NMsg.ofC("unsupported %s", r));
                    }
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SshNPath that = (SshNPath) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public NPath toRelativePath(NPath basePath, NPath parentPath) {
        String child = basePath.getLocation();
        String parent = parentPath.getLocation();
        if (child.startsWith(parent)) {
            child = child.substring(parent.length());
            if (child.startsWith("/") || child.startsWith("\\")) {
                child = child.substring(1);
            }
            return NPath.of(child, session);
        }
        return null;
    }

    @Override
    public String toString() {
        NConnexionString c = path.copy();
        c.setQueryString(null);
        c.setPath(null);
        StringBuilder sb = new StringBuilder();
        sb.append(c);
        sb.append(':');
        sb.append(path.getPath());
        if (!NBlankable.isBlank(path.getQueryString())) {
            sb.append('?');
            sb.append(path.getQueryString());
        }
        return sb.toString();
    }
}
