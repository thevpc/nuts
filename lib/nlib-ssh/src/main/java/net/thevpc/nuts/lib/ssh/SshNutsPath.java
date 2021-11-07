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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

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
    public NutsStream<NutsPath> list() {
        try (SShConnection c = new SShConnection(path.toAddress(), getSession())
                .addListener(listener)) {
            c.grabOutputString();
            int i = c.execStringCommand("ls " + path.getPath());
            if (i == 0) {
                String[] s = c.getOutputString().split("[\n|\r]");
                return NutsStream.of(s, session).map(
                        x -> {
                            String cc = path.getPath();
                            if (!cc.endsWith("/")) {
                                cc += "/";
                            }
                            cc += x;
                            return NutsPath.of(path.setPath(cc).toString(), getSession());
                        }
                );
            }
        } catch (Exception e) {
            //return false;
        }
        return NutsStream.ofEmpty(session);
    }

    @Override
    public NutsStream<NutsPath> walk(int maxDepth, NutsPathVisitOption[] options) {
        EnumSet<NutsPathVisitOption> optionsSet = EnumSet.noneOf(NutsPathVisitOption.class);
        optionsSet.addAll(Arrays.asList(options));
        try (SShConnection c = new SShConnection(path.toAddress(), getSession())
                .addListener(listener)) {
            c.grabOutputString();
            StringBuilder cmd = new StringBuilder();
            cmd.append("ls");
            if (optionsSet.contains(NutsPathVisitOption.FOLLOW_LINKS)) {
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
                        x -> {
                            String cc = path.getPath();
                            if (!cc.endsWith("/")) {
                                cc += "/";
                            }
                            cc += x;
                            return NutsPath.of(path.setPath(cc).toString(), getSession());
                        }
                );
            }
        } catch (Exception e) {
            //return false;
        }
        return NutsStream.ofEmpty(session);
    }

    @Override
    public NutsFormatSPI getFormatterSPI() {
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
    public String getProtocol() {
        return "ssh";
    }

    @Override
    public NutsPath resolve(String[] others, boolean trailingSeparator) {
        if (others.length > 0) {
            StringBuilder loc = new StringBuilder(this.path.getPath());
            if (loc.length() == 0 || loc.charAt(loc.length() - 1) != '/') {
                loc.append('/');
            }
            loc.append(String.join("/", others));
            if (trailingSeparator) {
                loc.append('/');
            }
            return NutsPath.of(
                    SshPath.toString(
                            this.path.getHost(),
                            this.path.getPort(),
                            loc.toString(),
                            this.path.getUser(),
                            this.path.getPassword(),
                            this.path.getKeyFile()
                    ), getSession());
        }
        return NutsPath.of(toString(), getSession());
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
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public boolean isDirectory() {
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
    public boolean isRegularFile() {
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
    public boolean exists() {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("not supported exists for %s", toString()));
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getLocation() {
        return path.getPath();
    }

    @Override
    public InputStream getInputStream() {
        return new SshFileInputStream(path, session);
    }

    @Override
    public OutputStream getOutputStream() {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("not supported output stream for %s", toString()));
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    public void delete(boolean recurse) {
        try (SShConnection session = new SShConnection(path.toAddress(), getSession())
                .addListener(listener)) {
            session.rm(path.getPath(), recurse);
        }
    }

    public void mkdir(boolean parents) {
        try (SShConnection c = new SShConnection(path.toAddress(), getSession())
                .addListener(listener)) {
            c.mkdir(path.getPath(), parents);
        }
    }

    @Override
    public Instant getLastModifiedInstant() {
        return null;
    }

    @Override
    public Instant getLastAccessInstant() {
        return null;
    }

    @Override
    public Instant getCreationInstant() {
        return null;
    }

    @Override
    public NutsPath getParent() {
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
    public NutsPath toAbsolute(NutsPath basePath) {
        return NutsPath.of(toString(), getSession());
    }

    @Override
    public NutsPath normalize() {
        return NutsPath.of(toString(), getSession());
    }

    @Override
    public boolean isAbsolute() {
        return true;
    }

    @Override
    public String owner() {
        return null;
    }

    @Override
    public String group() {
        return null;
    }

    @Override
    public Set<NutsPathPermission> permissions() {
        return Collections.emptySet();
    }

    @Override
    public void setPermissions(NutsPathPermission... permissions) {
    }

    @Override
    public void addPermissions(NutsPathPermission... permissions) {
    }

    @Override
    public void removePermissions(NutsPathPermission... permissions) {
    }

    @Override
    public boolean isName() {
        return false;
    }

    @Override
    public int getPathCount() {
        String location = getLocation();
        if (NutsBlankable.isBlank(location)) {
            return 0;
        }
        return NutsPath.of(location, getSession()).getPathCount();
    }

    @Override
    public boolean isRoot() {
        String loc = getLocation();
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
    public NutsPath toCompressedForm() {
        return null;
    }

    @Override
    public String getName() {
        String loc = getLocation();
        return loc == null ? "" : Paths.get(loc).getFileName().toString();
    }

    @Override
    public URL toURL() {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve url from %s", toString()));
    }

    @Override
    public Path toFile() {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve file from %s", toString()));
    }

}
