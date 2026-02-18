package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.concurrent.NCachedValue;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.spi.NPathSPIAware;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.spi.NObjectWriterSPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

class SshNPath implements NPathSPI {

    private final NConnectionString path;
    private SshListener listener;
    private NCachedValue<NPathType> cachedType;

    public SshNPath(NConnectionString path) {
        this.path = path;
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
        try (SshConnection c = prepareSshConnection()) {
            return NStream.ofStream(c.list(path.getPath())
                    .stream()).map(
                    NFunction.of((String cc) -> NPath.of(path.builder().setPath(cc).build().toString()))
                            .withDescription(NDescribables.ofDesc("NPath::of"))
            );
        } catch (Exception e) {
            //return false;
        }
        return NStream.ofEmpty();
    }

    private SshConnection prepareSshConnection() {
        return SshConnectionPool.of().acquire(path)
                .addListener(listener);
    }


    @Override
    public NObjectWriterSPI formatter(NPath basePath) {
        return new NObjectWriterSPI() {
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
                String user = path.getUserName();
                String host = path.getHost();
                int port = NLiteral.of(path.getPort()).asInt().orElse(-1);
                String path0 = path.getPath();
                String password = path.getPassword();
                String keyFile = path.builder().getQueryParam(SshConnection.IDENTITY_FILE).orNull();

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
                                .append(SshConnection.IDENTITY_FILE)
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
        if (loc == null) {
            return "";
        } else {
            Path fileName = Paths.get(loc).getFileName();
            return fileName == null ? "" : fileName.toString();
        }
    }

    @Override
    public String getProtocol(NPath basePath) {
        return "ssh";
    }

    public NPath resolve(NPath basePath, String path) {
        NConnectionStringBuilder c = this.path.builder();
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
        NConnectionStringBuilder c = this.path.builder();
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

    public NPathType getType(NPath basePath) {
        if (cachedType == null) {
            cachedType = NCachedValue.of(() -> {
                try (SshConnection c = prepareSshConnection()) {
                    return c.type(path.getPath());
                } catch (Exception e) {
                    return NPathType.NOT_FOUND;
                }
            }).setExpiry(NDuration.ofSeconds(60));
        }
        return cachedType.get();
    }

    @Override
    public boolean exists(NPath basePath) {
        return getType(basePath) != NPathType.NOT_FOUND;
    }

    @Override
    public long getContentLength(NPath basePath) {
        try (SshConnection c = prepareSshConnection()) {
            return c.getContentLength(path.getPath());
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public String getContentEncoding(NPath basePath) {
        try (SshConnection c = prepareSshConnection()) {
            return c.getContentEncoding(path.getPath());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getContentType(NPath basePath) {
        try (SshConnection c = prepareSshConnection()) {
            return c.getContentType(path.getPath());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getCharset(NPath basePath) {
        try (SshConnection c = prepareSshConnection()) {
            return c.getCharset(path.getPath());
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
        try (SshConnection session = prepareSshConnection()) {
            return session.getInputStream(path.getPath());
        }
    }

    @Override
    public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
        try (SshConnection session = prepareSshConnection()) {
            return session.getOutputStream(path.getPath());
        }
    }

    public void delete(NPath basePath, boolean recurse) {
        try (SshConnection session = prepareSshConnection()) {
            session.rm(path.getPath(), recurse);
        }
    }

    public void mkdir(boolean parents, NPath basePath) {
        try (SshConnection c = prepareSshConnection()) {
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
        return NPath.of(path.builder().setPath(loc).build().toString());
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
    public String getOwner(NPath basePath) {
        return null;
    }

    @Override
    public String getGroup(NPath basePath) {
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
        return NPath.of(path.builder().setPath("/").build().toString());
    }

    @Override
    public NStream<NPath> walk(NPath basePath, int maxDepth, NPathOption[] options) {
//        Set<NPathOption> optionsSet = new HashSet<>();
//        optionsSet.addAll(Arrays.asList(options));
        try (SshConnection c = prepareSshConnection()) {
            List<String> ss = c.walk(path.getPath(), true, maxDepth);
            return NStream.ofIterable(ss).map(
                    NFunction.of(
                            (String x) -> {
                                return NPath.of(path.builder().setPath(x).build().toString());
                            }

                    ).withDescription(NDescribables.ofDesc("NPath::of"))
            );
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
            NConnectionStringBuilder sp = NConnectionStringBuilder.of(other.toString());
            if (
                    Objects.equals(sp.getHost(), path.getHost())
                            && Objects.equals(sp.getUserName(), path.getUserName())
            ) {
                int r = -1;
                try (SshConnection c = prepareSshConnection()) {
                    r = c.mv(path.getPath(), sp.getPath());
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
        if (basePath instanceof NPathSPIAware && other instanceof NPathSPIAware) {
            NPathSPI spi1 = ((NPathSPIAware) basePath).spi();
            NPathSPI spi2 = ((NPathSPIAware) other).spi();
            if (spi1 instanceof SshNPath && spi2 instanceof SshNPath) {
                SshNPath ssh1 = (SshNPath) spi1;
                SshNPath ssh2 = (SshNPath) spi2;
                if (ssh1.path.withPath("/").equals(ssh2.path.withPath("/"))) {
                    // same filesystem
                    try (SshConnection session = prepareSshConnection()) {
                        session.cp(ssh1.path.getPath(), ssh2.path.getPath(), true);
                        return true;
                    }
                }
            }
        }
        return false;
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
        NConnectionStringBuilder c = path.builder();
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
        try (SshConnection c = prepareSshConnection()) {
            return c.getDigestWithCommand(algo, path.getPath());
        }
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

    @Override
    public NPathInfo getInfo(NPath basePath) {
        try (SshConnection c = prepareSshConnection()) {
            return c.getInfo(path.getPath());
        } catch (Exception e) {
            return DefaultNPathInfo.ofNotFound(path.getPath());
        }
    }

    @Override
    public List<NPathInfo> listInfos(NPath basePath) {
        try (SshConnection c = prepareSshConnection()) {
            return c.listInfos(path.getPath());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
