package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.format.NTreeVisitResult;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.lib.common.str.NConnexionString;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        try (SShConnection c = prepareSshConnexionGrab()) {
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
                                }

                        ).withDesc(NEDesc.of("NPath::of"))
                );
            }
        } catch (Exception e) {
            //return false;
        }
        return NStream.ofEmpty(session);
    }

    private SShConnection prepareSshConnexion() {
        return new SShConnection(path
                , getSession().in()
                , getSession().out().asOutputStream()
                , getSession().err().asOutputStream()
                , getSession()
        )
                .addListener(listener);
    }

    private SShConnection prepareSshConnexionGrab() {
        return new SShConnection(path
                , getSession().in()
                , getSession().out().asOutputStream()
                , NIO.of(session).ofNullRawOutputStream()
                , getSession()
        ).grabOutputString()
                .addListener(listener);
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
                if (!NBlankable.isBlank(user)) {
                    sb.append(user);
                    if (!NBlankable.isBlank(password)) {
                        sb.append(text.ofStyled(":", _sep));
                        sb.append(password);
                    }
                    sb.append(text.ofStyled("@", _sep));
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
                if (keyFile != null) {
                    sb.append(text.ofStyled("?", _sep));
                    boolean first = true;
                    if (keyFile != null) {
                        if (first) {
                            first = false;
                        } else {
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
        NConnexionString c = this.path.copy();
        if (NBlankable.isBlank(path)) {
            return basePath;
        }
        if (isAbsolutePathString(path)) {
            c.setPath(path);
            return NPath.of(c.toString(), getSession());
        }
        List<String> a = splitPath(c.getPath());
        a.addAll(splitPath(path));
        a = normalize(a);
        c.setPath(joinPathString(a));
        return NPath.of(c.toString(), getSession());
    }


    @Override
    public NPath resolve(NPath basePath, NPath path) {
        return resolve(basePath, path.getLocation());
    }

    @Override
    public NPath resolveSibling(NPath basePath, String path) {
        NConnexionString c = this.path.copy();
        if (NBlankable.isBlank(path)) {
            return basePath;
        }

        if (isAbsolutePathString(path)) {
            c.setPath(path);
            return NPath.of(c.toString(), getSession());
        }
        List<String> a = splitPath(c.getPath());
        a.addAll(splitPath(path));
        a = normalize(a);
        if (!a.isEmpty()) {
            a.remove(a.size() - 1);
        }
        c.setPath(joinPathString(a));
        return NPath.of(this.path.toString(), getSession());
    }

    @Override
    public NPath resolveSibling(NPath basePath, NPath path) {
        return resolveSibling(basePath, path.getLocation());
    }

    @Override
    public NPath toCompressedForm(NPath basePath) {
        return null;
    }

    @Override
    public NOptional<URL> toURL(NPath basePath) {
        return NOptional.ofNamedError(NMsg.ofC("not an url %s", path));
    }

    @Override
    public NOptional<Path> toPath(NPath basePath) {
        return NOptional.ofNamedError(NMsg.ofC("not a file %s", path));
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
        return detectType(basePath) == SshFileType.SYMBOLIC_LINK;
    }

    @Override
    public boolean isOther(NPath basePath) {
        SshFileType t = detectType(basePath);
        if (t == null) {
            return false;
        }
        switch (t) {
            case DIRECTORY:
            case FILE:
            case SYMBOLIC_LINK: {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isDirectory(NPath basePath) {
        return detectType(basePath) == SshFileType.DIRECTORY;
    }

    @Override
    public boolean isLocal(NPath basePath) {
        return false;
    }

    public SshFileType detectType(NPath basePath) {
        try (SShConnection c = prepareSshConnexionGrab()) {
            int i = c.execStringCommand("file -b -E " + path.getPath());
            if (i > 0) {
                return null;
            }
            String s = c.getOutputString();
            s = s.trim();
            if (s.startsWith("directory")) {
                return SshFileType.DIRECTORY;
            }
            if (s.startsWith("fifo (named pipe)")) {
                return SshFileType.NAMED_PIPE;
            }
            if (s.startsWith("character special")) {
                return SshFileType.CHARACTER;
            }
            if (s.startsWith("symbolic link")) {
                return SshFileType.SYMBOLIC_LINK;
            }
            if (s.startsWith("block special")) {
                return SshFileType.BLOCK;
            }
            return SshFileType.FILE;
        } catch (Exception e) {
            return null;
        }
    }

    private static enum SshFileType {
        DIRECTORY,
        NAMED_PIPE,
        CHARACTER,
        SYMBOLIC_LINK,
        BLOCK,
        FILE,
        UNKNOWN
    }

    @Override
    public boolean isRegularFile(NPath basePath) {
        return "file".equals(detectType(basePath));
    }

    @Override
    public boolean exists(NPath basePath) {
        return detectType(basePath) != null;
    }

    @Override
    public long getContentLength(NPath basePath) {
        try (SShConnection c = prepareSshConnexionGrab()) {
            c.grabOutputString();
            int i = c.execStringCommand("ls -l " + path.getPath());
            if (i != 0) {
                return -1;
            }
            String outputString = c.getOutputString();
            String[] r = NStringUtils.trim(outputString).split(" ");
            if (r.length > 4) {
                NLiteral size = NLiteral.of(r[4]);
                if (size.isLong()) {
                    return size.asLong().get();
                }
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public String getContentEncoding(NPath basePath) {
        try (SShConnection c = prepareSshConnexion()) {
            c.grabOutputString();
            int i = c.execStringCommand("file -bi " + path.getPath());
            if (i != 0) {
                return null;
            }
            String outputString = NStringUtils.trim(c.getOutputString());
            Pattern p = Pattern.compile(".*charset=(?<cs>\\S*).*");
            Matcher m = p.matcher(outputString);
            if (m.find()) {
                return m.group("cs");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getContentType(NPath basePath) {
        try (SShConnection c = prepareSshConnexion()) {
            c.grabOutputString();
            int i = c.execStringCommand("file -bi " + path.getPath());
            if (i != 0) {
                return null;
            }
            String outputString = c.getOutputString();
            String[] r = Arrays.stream(NStringUtils.trim(outputString).split("[ ;]")).map(String::trim).filter(x -> x.length() > 0).toArray(String[]::new);
            if (r.length > 0) {
                return NStringUtils.trim(r[0]);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getCharset(NPath basePath) {
        try (SShConnection c = prepareSshConnexion()) {
            c.grabOutputString();
            int i = c.execStringCommand("file -bi " + path.getPath());
            if (i != 0) {
                return null;
            }
            String outputString = c.getOutputString();
            String[] r = Arrays.stream(NStringUtils.trim(outputString).split("[ ;]")).map(String::trim).filter(x -> x.length() > 0).toArray(String[]::new);
            if (r.length > 1) {
                String v = NStringUtils.trim(r[1]);
                if (v.startsWith("charset=")) {
                    v = v.substring("charset=".length()).trim();
                }
                return v;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getLocation(NPath basePath) {
        return path.getPath();
    }

    @Override
    public InputStream getInputStream(NPath basePath, NPathOption... options) {
        SshFileType ft = detectType(basePath);
        if (ft == null) {
            throw new NIOException(getSession(), NMsg.ofC("path not found %s", basePath));
        }
        if (ft == SshFileType.DIRECTORY) {
            throw new NIOException(getSession(), NMsg.ofC("cannot open directory %s", basePath));
        }

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
        try (SShConnection session = prepareSshConnexion()) {
            session.rm(path.getPath(), recurse);
        }
    }

    public void mkdir(boolean parents, NPath basePath) {
        try (SShConnection c = prepareSshConnexion()) {
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
        return NPath.of(path.copy().setPath(loc).toString(), getSession());
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
    public int getLocationItemsCount(NPath basePath) {
        String location = getLocation(basePath);
        if (NBlankable.isBlank(location)) {
            return 0;
        }
        return NPath.of(location, getSession()).getLocationItemsCount();
    }

    @Override
    public boolean isRoot(NPath basePath) {
        String loc = getLocation(basePath);
        if (NBlankable.isBlank(loc)) {
            return false;
        }
        if (isAbsolutePathString(loc)) {
            return splitPath(loc).isEmpty();
        }
        return false;
    }

    @Override
    public NPath getRoot(NPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        return NPath.of(path.copy().setPath("/").toString(), session);
    }

    @Override
    public NStream<NPath> walk(NPath basePath, int maxDepth, NPathOption[] options) {
        EnumSet<NPathOption> optionsSet = EnumSet.noneOf(NPathOption.class);
        optionsSet.addAll(Arrays.asList(options));
        try (SShConnection c = prepareSshConnexionGrab()) {
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
                                }

                        ).withDesc(NEDesc.of("NPath::of"))
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
    public List<String> getLocationItems(NPath basePath) {
        return NPath.of(getLocation(basePath), getSession()).getLocationItems();
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
                try (SShConnection c = prepareSshConnexionGrab()) {
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

    @Override
    public byte[] getDigest(NPath basePath, String algo) {
        switch (algo) {
            case "SHA-1": {
                return getDigestWithCommand("sha1sum", basePath, algo);
            }
            case "SHA-256": {
                return getDigestWithCommand("sha256sum", basePath, algo);
            }
            case "SHA-224": {
                return getDigestWithCommand("sha224sum", basePath, algo);
            }
            case "SHA-512": {
                return getDigestWithCommand("sha512sum", basePath, algo);
            }
            case "MD5": {
                return getDigestWithCommand("md5sum", basePath, algo);
            }
        }
        return null;
    }

    public byte[] getDigestWithCommand(String cmd, NPath basePath, String algo) {
        try (SShConnection c = prepareSshConnexionGrab()) {
            int r = c.execStringCommand(cmd + " " + path.getPath());
            if (r == 0) {
                String z = NStringUtils.trim(c.getOutputString());
                int i = z.indexOf(' ');
                if (i > 0) {
                    z = z.substring(0, i);
                    return NStringUtils.fromHexString(z);
                }
            }
        }
        return null;
    }

    private boolean isAbsolutePathString(String a) {
        return a.startsWith("/");
    }

    private List<String> splitPath(String a) {
        return NStringUtils.split(a, "/\\", false, true);
    }

    private List<String> normalize(List<String> a) {
        List<String> b = new ArrayList<>();
        for (String s : a) {
            if (".".equals(s)) {
                //ignore
            } else if ("..".equals(s)) {
                //ignore
                if (b.size() > 0) {
                    b.remove(b.size() - 1);
                }
            } else if (s.length() > 0) {
                b.add(s);
            }
        }
        return b;
    }

    private static String joinPathString(List<String> a) {
        return "/" + String.join("/", a);
    }
}
