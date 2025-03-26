package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.util.NConnexionString;
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
    private final NWorkspace workspace;
    private SshListener listener;

    public SshNPath(NConnexionString path, NWorkspace workspace) {
        this.path = path;
        this.workspace = workspace;
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
                return NStream.of(s).map(
                        NFunction.of(
                                x -> {
                                    String cc = path.getPath();
                                    if (!cc.endsWith("/")) {
                                        cc += "/";
                                    }
                                    cc += x;
                                    return NPath.of(path.setPath(cc).toString());
                                }

                        ).withDesc(NEDesc.of("NPath::of"))
                );
            }
        } catch (Exception e) {
            //return false;
        }
        return NStream.ofEmpty();
    }

    private SShConnection prepareSshConnexion() {
        NSession session = workspace.currentSession();
        return new SShConnection(path
                , session.in()
                , NOut.asOutputStream()
                , session.err().asOutputStream()
                , session
        )
                .addListener(listener);
    }

    private SShConnection prepareSshConnexionGrab() {
        NSession session = workspace.currentSession();
        return new SShConnection(path
                , session.in()
                , NOut.asOutputStream()
                , NIO.ofNullRawOutputStream()
                , session
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
                NTexts text = NTexts.of();
                NTextBuilder sb = text.ofBuilder();
                String user = path.getUser();
                String host = path.getHost();
                int port = NLiteral.of(path.getPort()).asIntValue().orElse(-1);
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
                out.print(sb.build());
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

    public NPath resolve(NPath basePath, String path) {
        NConnexionString c = this.path.copy();
        if (NBlankable.isBlank(path)) {
            return basePath;
        }
        if (isAbsolutePathString(path)) {
            c.setPath(path);
            return NPath.of(c.toString());
        }
        List<String> a = splitPath(c.getPath());
        a.addAll(splitPath(path));
        a = normalize(a);
        c.setPath(joinPathString(a));
        return NPath.of(c.toString());
    }


    public NPath resolveSibling(NPath basePath, String path) {
        NConnexionString c = this.path.copy();
        if (NBlankable.isBlank(path)) {
            return basePath;
        }

        if (isAbsolutePathString(path)) {
            c.setPath(path);
            return NPath.of(c.toString());
        }
        List<String> a = splitPath(c.getPath());
        a.addAll(splitPath(path));
        a = normalize(a);
        if (!a.isEmpty()) {
            a.remove(a.size() - 1);
        }
        c.setPath(joinPathString(a));
        return NPath.of(this.path.toString());
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
//                            ), session);
//        }
//        return NPath.of(toString(), session);
//    }

    @Override
    public boolean isLocal(NPath basePath) {
        return false;
    }

    public NPathType type(NPath basePath) {
        try (SShConnection c = prepareSshConnexionGrab()) {
            int i = c.execStringCommand("file -b -E " + path.getPath());
            if (i > 0) {
                return null;
            }
            String s = c.getOutputString();
            s = s.trim();
            if (s.startsWith("directory")) {
                return NPathType.DIRECTORY;
            }
            if (s.startsWith("fifo (named pipe)")) {
                return NPathType.NAMED_PIPE;
            }
            if (s.startsWith("character special")) {
                return NPathType.CHARACTER;
            }
            if (s.startsWith("symbolic link")) {
                return NPathType.SYMBOLIC_LINK;
            }
            if (s.startsWith("block special")) {
                return NPathType.BLOCK;
            }
            return NPathType.FILE;
        } catch (Exception e) {
            return NPathType.NOT_FOUND;
        }
    }

    @Override
    public boolean exists(NPath basePath) {
        return type(basePath) != NPathType.NOT_FOUND;
    }

    @Override
    public long contentLength(NPath basePath) {
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
                    return size.asLongValue().get();
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
        NPathType ft = type(basePath);
        if (ft == NPathType.NOT_FOUND) {
            throw new NIOException(NMsg.ofC("path not found %s", basePath));
        }
        if (ft == NPathType.DIRECTORY) {
            throw new NIOException(NMsg.ofC("cannot open directory %s", basePath));
        }

        return new SshFileInputStream(path, workspace);
    }

    @Override
    public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
        return new SshFileOutputStream2(path, workspace, false);
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
        return NPath.of(path.copy().setPath(loc).toString());
    }

    @Override
    public NPath toAbsolute(NPath basePath, NPath rootPath) {
        return basePath;
    }

    @Override
    public NPath normalize(NPath basePath) {
        return NPath.of(toString());
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
    public Boolean isName(NPath basePath) {
        return false;
    }

    @Override
    public Integer getNameCount(NPath basePath) {
        String location = getLocation(basePath);
        if (NBlankable.isBlank(location)) {
            return 0;
        }
        return NPath.of(location).getNameCount();
    }

    @Override
    public Boolean isRoot(NPath basePath) {
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
        return NPath.of(path.copy().setPath("/").toString());
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
                return NStream.of(s).map(
                        NFunction.of(
                                x -> {
                                    String cc = path.getPath();
                                    if (!cc.endsWith("/")) {
                                        cc += "/";
                                    }
                                    cc += x;
                                    return NPath.of(path.setPath(cc).toString());
                                }

                        ).withDesc(NEDesc.of("NPath::of"))
                );
            }
        } catch (Exception e) {
            //return false;
        }
        return NStream.ofEmpty();
    }

    @Override
    public NPath subpath(NPath basePath, int beginIndex, int endIndex) {
        return NPath.of(this.path.toString());
    }

    @Override
    public List<String> getNames(NPath basePath) {
        return NPath.of(getLocation(basePath)).getNames();
    }

    @Override
    public boolean moveTo(NPath basePath, NPath other, NPathOption... options) {
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
                    throw new NIOException(NMsg.ofC("unable to move %s", this));
                }
                return true;
            }
        }
        copyTo(basePath, other, options);
        delete(basePath, true);
        return true;
    }

    @Override
    public boolean copyTo(NPath basePath, NPath other, NPathOption... options) {
        //TODO check if the two files are on the same ssh filesystem ?
        NCp.of().from(basePath).to(other).addOptions(options).run();
        return true;
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
                    return NHex.toBytes(z);
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
