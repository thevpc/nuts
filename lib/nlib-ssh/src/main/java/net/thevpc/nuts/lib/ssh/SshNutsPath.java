package net.thevpc.nuts.lib.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

class SshNutsPath implements NutsPathSPI {

    private final SshPath path;
    private final NutsSession session;
    private SshListener listener;

    public SshNutsPath(SshPath path, NutsSession session) {
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
    public NutsStream<NutsPath> list(NutsPath basePath) {
        try (SShConnection c = new SShConnection(path.toAddress(), getSession())
                .addListener(listener)) {
            c.grabOutputString();
            int i = c.execStringCommand("ls " + path.getPath());
            if (i == 0) {
                String[] s = c.getOutputString().split("[\n|\r]");
                return NutsStream.of(s, session).map(
                        NutsFunction.of(
                                x -> {
                                    String cc = path.getPath();
                                    if (!cc.endsWith("/")) {
                                        cc += "/";
                                    }
                                    cc += x;
                                    return NutsPath.of(path.setPath(cc).toString(), getSession());
                                }, "NutsPath::of"

                        )
                );
            }
        } catch (Exception e) {
            //return false;
        }
        return NutsStream.ofEmpty(session);
    }

    @Override
    public NutsFormatSPI formatter(NutsPath basePath) {
        return new NutsFormatSPI() {
            @Override
            public void print(NutsPrintStream out) {
                //should implement better formatting...
                NutsTextStyle _sep = NutsTextStyle.separator();
                NutsTextStyle _path = NutsTextStyle.path();
                NutsTextStyle _nbr = NutsTextStyle.number();
//        if(true) {
                NutsTexts text = NutsTexts.of(session);
                NutsTextBuilder sb = text.builder();
                String user = path.getUser();
                String host = path.getHost();
                int port = path.getPort();
                String path0 = path.getPath();
                String password = path.getPassword();
                String keyFile = path.getKeyFile();

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
            public boolean configureFirst(NutsCommandLine commandLine) {
                return false;
            }
        };
    }

    @Override
    public String getName(NutsPath basePath) {
        String loc = getLocation(basePath);
        return loc == null ? "" : Paths.get(loc).getFileName().toString();
    }

    @Override
    public String getProtocol(NutsPath basePath) {
        return "ssh";
    }

    @Override
    public NutsPath resolve(NutsPath basePath, String path) {
        return NutsPath.of(
                SshPath.toString(
                        this.path.getHost(),
                        this.path.getPort(),
                        NutsPath.of(this.path.getPath(), session).resolve(path).toString(),
                        this.path.getUser(),
                        this.path.getPassword(),
                        this.path.getKeyFile()
                ), getSession());
    }

    @Override
    public NutsPath resolve(NutsPath basePath, NutsPath path) {
        return NutsPath.of(
                SshPath.toString(
                        this.path.getHost(),
                        this.path.getPort(),
                        NutsPath.of(this.path.getPath(), session).resolve(path).toString(),
                        this.path.getUser(),
                        this.path.getPassword(),
                        this.path.getKeyFile()
                ), getSession());
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, String path) {
        return NutsPath.of(
                SshPath.toString(
                        this.path.getHost(),
                        this.path.getPort(),
                        NutsPath.of(this.path.getPath(), session).resolveSibling(path).toString(),
                        this.path.getUser(),
                        this.path.getPassword(),
                        this.path.getKeyFile()
                ), getSession());
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, NutsPath path) {
        return NutsPath.of(
                SshPath.toString(
                        this.path.getHost(),
                        this.path.getPort(),
                        NutsPath.of(this.path.getPath(), session).resolveSibling(path).toString(),
                        this.path.getUser(),
                        this.path.getPassword(),
                        this.path.getKeyFile()
                ), getSession());
    }

    @Override
    public NutsPath toCompressedForm(NutsPath basePath) {
        return null;
    }

    @Override
    public URL toURL(NutsPath basePath) {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve url from %s", toString()));
    }

    @Override
    public Path toFile(NutsPath basePath) {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve file from %s", toString()));
    }

    //    @Override
//    public NutsPath resolve(String path) {
//        String[] others = Arrays.stream(NutsUtilStrings.trim(path).split("[/\\\\]"))
//                .filter(x -> x.length() > 0).toArray(String[]::new);
//        if (others.length > 0) {
//            StringBuilder loc = new StringBuilder(this.path.getPath());
//            if (loc.length() == 0 || loc.charAt(loc.length() - 1) != '/') {
//                loc.append('/');
//            }
//            loc.append(String.join("/", others));
//            return
//                    NutsPath.of(
//                            SshPath.toString(
//                                    this.path.getHost(),
//                                    this.path.getPort(),
//                                    loc.toString(),
//                                    this.path.getUser(),
//                                    this.path.getPassword(),
//                                    this.path.getKeyFile()
//                            ), getSession());
//        }
//        return NutsPath.of(toString(), getSession());
//    }
    @Override
    public boolean isSymbolicLink(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isOther(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isDirectory(NutsPath basePath) {
        try (SShConnection c = new SShConnection(path.toAddress(), getSession())
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
    public boolean isLocal(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isRegularFile(NutsPath basePath) {
        try (SShConnection c = new SShConnection(path.toAddress(), getSession())
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
    public boolean exists(NutsPath basePath) {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("not supported exists for %s", toString()));
    }

    @Override
    public long getContentLength(NutsPath basePath) {
        return -1;
    }

    @Override
    public String getContentEncoding(NutsPath basePath) {
        return null;
    }

    @Override
    public String getContentType(NutsPath basePath) {
        return null;
    }

    @Override
    public String getLocation(NutsPath basePath) {
        return path.getPath();
    }

    @Override
    public InputStream getInputStream(NutsPath basePath) {
        return new SshFileInputStream(path, session);
    }

    @Override
    public OutputStream getOutputStream(NutsPath basePath) {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("not supported output stream for %s", toString()));
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    public void delete(NutsPath basePath, boolean recurse) {
        try (SShConnection session = new SShConnection(path.toAddress(), getSession())
                .addListener(listener)) {
            session.rm(path.getPath(), recurse);
        }
    }

    public void mkdir(boolean parents, NutsPath basePath) {
        try (SShConnection c = new SShConnection(path.toAddress(), getSession())
                .addListener(listener)) {
            c.mkdir(path.getPath(), parents);
        }
    }

    @Override
    public Instant getLastModifiedInstant(NutsPath basePath) {
        return null;
    }

    @Override
    public Instant getLastAccessInstant(NutsPath basePath) {
        return null;
    }

    @Override
    public Instant getCreationInstant(NutsPath basePath) {
        return null;
    }

    @Override
    public NutsPath getParent(NutsPath basePath) {
        String loc = getURLParentPath(this.path.getPath());
        if (loc == null) {
            return null;
        }
        return NutsPath.of(
                SshPath.toString(
                        this.path.getHost(),
                        this.path.getPort(),
                        loc,
                        this.path.getUser(),
                        this.path.getPassword(),
                        this.path.getKeyFile()
                ), getSession());
    }

    @Override
    public NutsPath toAbsolute(NutsPath basePath, NutsPath rootPath) {
        return basePath;
    }

    @Override
    public NutsPath normalize(NutsPath basePath) {
        return NutsPath.of(toString(), getSession());
    }

    @Override
    public boolean isAbsolute(NutsPath basePath) {
        return true;
    }

    @Override
    public String owner(NutsPath basePath) {
        return null;
    }

    @Override
    public String group(NutsPath basePath) {
        return null;
    }

    @Override
    public Set<NutsPathPermission> getPermissions(NutsPath basePath) {
        return Collections.emptySet();
    }

    @Override
    public void setPermissions(NutsPath basePath, NutsPathPermission... permissions) {
    }

    @Override
    public void addPermissions(NutsPath basePath, NutsPathPermission... permissions) {
    }

    @Override
    public void removePermissions(NutsPath basePath, NutsPathPermission... permissions) {
    }

    @Override
    public boolean isName(NutsPath basePath) {
        return false;
    }

    @Override
    public int getPathCount(NutsPath basePath) {
        String location = getLocation(basePath);
        if (NutsBlankable.isBlank(location)) {
            return 0;
        }
        return NutsPath.of(location, getSession()).getPathCount();
    }

    @Override
    public boolean isRoot(NutsPath basePath) {
        String loc = getLocation(basePath);
        if (NutsBlankable.isBlank(loc)) {
            return false;
        }
        switch (loc) {
            case "/":
            case "\\\\":
                return true;
        }
        return NutsPath.of(loc, getSession()).isRoot();
    }

    @Override
    public NutsPath getRoot(NutsPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        return basePath.getParent().getRoot();
    }

    @Override
    public NutsStream<NutsPath> walk(NutsPath basePath, int maxDepth, NutsPathOption[] options) {
        EnumSet<NutsPathOption> optionsSet = EnumSet.noneOf(NutsPathOption.class);
        optionsSet.addAll(Arrays.asList(options));
        try (SShConnection c = new SShConnection(path.toAddress(), getSession())
                .addListener(listener)) {
            c.grabOutputString();
            StringBuilder cmd = new StringBuilder();
            cmd.append("ls");
            if (optionsSet.contains(NutsPathOption.FOLLOW_LINKS)) {
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
                return NutsStream.of(s, session).map(
                        NutsFunction.of(
                                x -> {
                                    String cc = path.getPath();
                                    if (!cc.endsWith("/")) {
                                        cc += "/";
                                    }
                                    cc += x;
                                    return NutsPath.of(path.setPath(cc).toString(), getSession());
                                },
                                "NutsPath::of"
                        )
                );
            }
        } catch (Exception e) {
            //return false;
        }
        return NutsStream.ofEmpty(session);
    }

    @Override
    public NutsPath subpath(NutsPath basePath, int beginIndex, int endIndex) {
        return NutsPath.of(
                SshPath.toString(
                        this.path.getHost(),
                        this.path.getPort(),
                        NutsPath.of(getLocation(basePath), getSession()).subpath(beginIndex, endIndex).toString(),
                        this.path.getUser(),
                        this.path.getPassword(),
                        this.path.getKeyFile()
                ), getSession());
    }

    @Override
    public String[] getItems(NutsPath basePath) {
        return NutsPath.of(getLocation(basePath), getSession()).getItems();
    }

    @Override
    public void moveTo(NutsPath basePath, NutsPath other, NutsPathOption... options) {
        if (other.toString().startsWith("ssh:")) {
            SshPath sp = new SshPath(other.toString());
            if (
                    Objects.equals(sp.getHost(), path.getHost())
                            && Objects.equals(sp.getUser(), path.getUser())
            ) {
                int r = -1;
                try (SShConnection c = new SShConnection(path.toAddress(), getSession())
                        .addListener(listener)) {
                    c.grabOutputString();
                    r = c.execStringCommand("mv " + path.getPath() + " " + sp);
                }
                if (r != 0) {
                    throw new NutsIOException(session, NutsMessage.cstyle("unable to move %s", this));
                }
                return;
            }
        }
        copyTo(basePath, other, options);
        delete(basePath, true);
    }

    @Override
    public void copyTo(NutsPath basePath, NutsPath other, NutsPathOption... options) {
        NutsCp.of(session).from(basePath).to(other).run();
    }

    @Override
    public void walkDfs(NutsPath basePath, NutsTreeVisitor<NutsPath> visitor, int maxDepth, NutsPathOption... options) {
        for (NutsPath x : walk(basePath, maxDepth, options)) {
            if (x.isDirectory()) {
                NutsTreeVisitResult r = visitor.preVisitDirectory(x, session);
                switch (r) {
                    case CONTINUE: {
                        break;
                    }
                    case TERMINATE: {
                        return;
                    }
                    case SKIP_SIBLINGS:
                    case SKIP_SUBTREE: {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported %s", r));
                    }
                }
            } else if (x.isRegularFile()) {
                NutsTreeVisitResult r = visitor.visitFile(x, session);
                switch (r) {
                    case CONTINUE: {
                        break;
                    }
                    case TERMINATE: {
                        return;
                    }
                    case SKIP_SIBLINGS:
                    case SKIP_SUBTREE: {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported %s", r));
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
        SshNutsPath that = (SshNutsPath) o;
        return Objects.equals(path, that.path);
    }
}
