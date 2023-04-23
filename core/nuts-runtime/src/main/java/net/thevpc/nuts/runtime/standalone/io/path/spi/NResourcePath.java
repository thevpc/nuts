package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNClassLoader;
import net.thevpc.nuts.runtime.standalone.io.path.NCompressedPath;
import net.thevpc.nuts.runtime.standalone.io.path.NCompressedPathHelper;
import net.thevpc.nuts.runtime.standalone.io.util.NPathParts;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NPathFactory;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class NResourcePath implements NPathSPI {

    private final List<NId> ids;
    private String path;
    private String location;
    private boolean urlPathLookedUp = false;
    private URL[] urls = null;
    private NPath urlPath = null;
    private NSession session;

    public NResourcePath(String path, NSession session) {
        this.path = path;
        this.session = session;
        String idsStr;
        if (path.startsWith("nuts-resource://(")) {
            int x = path.indexOf(')');
            if (x > 0) {
                idsStr = path.substring("nuts-resource://(".length(), x);
                location = path.substring(x + 1);
            } else {
                throw new NIllegalArgumentException(session, NMsg.ofC("invalid path %s", path));
            }
        } else if (path.startsWith("nuts-resource://")) {
            int x = path.indexOf('/', "nuts-resource://".length());
            if (x > 0) {
                idsStr = path.substring("nuts-resource://".length(), x);
                location = path.substring(x);
            } else {
                throw new NIllegalArgumentException(session, NMsg.ofC("invalid path %s", path));
            }
        } else {
            throw new NIllegalArgumentException(session, NMsg.ofC("invalid path %s", path));
        }
        this.ids = StringTokenizerUtils.splitSemiColon(idsStr).stream().map(x -> {
            x = x.trim();
            if (x.length() > 0) {
                return NId.of(x).get(session);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    protected static String rebuildURL(String location, NId[] ids) {
        StringBuilder sb = new StringBuilder("nuts-resource://");
        boolean complex = Arrays.stream(ids).map(Object::toString).anyMatch(x -> x.contains(";") || x.contains("/"));
        if (complex) {
            sb.append("(");
            sb.append(Arrays.stream(ids).map(Object::toString).collect(Collectors.joining(";")));
            sb.append(")");
        } else {
            sb.append(Arrays.stream(ids).map(Object::toString).collect(Collectors.joining(";")));
        }
        if (!location.startsWith("/")) {
            sb.append("/");
        }
        sb.append(location);
        return sb.toString();
    }
    protected static NString rebuildURL2(NString location, NId[] ids, NSession session) {
        NTexts txt = NTexts.of(session);
        NTextBuilder sb = txt.ofBuilder();
        sb.append("nuts-resource://", NTextStyle.path());
        boolean complex = Arrays.stream(ids).map(Object::toString).anyMatch(x -> x.contains(";") || x.contains("/"));
        if (complex) {
            sb.append("(", NTextStyle.separator());
            sb.appendJoined(
                    txt.ofStyled(";", NTextStyle.separator()),
                    Arrays.asList(ids)
            );
            sb.append(")", NTextStyle.separator());
        } else {
            sb.appendJoined(
                    txt.ofStyled(";", NTextStyle.separator()),
                    Arrays.asList(ids)
            );
        }
        if (!location.filteredText().startsWith("/")) {
            sb.append("/", NTextStyle.path());
        }
        sb.append(location);
        return sb.build();
    }

    public NPath toURLPath() {
        if (!urlPathLookedUp) {
            urlPathLookedUp = true;
            try {
                String loc = location;
                ClassLoader resultClassLoader = NSearchCommand.of(getSession()).addIds(
                                this.ids.toArray(new NId[0])
                        ).setLatest(true).setContent(true).setDependencies(true)
                        .setDependencyFilter(
                                NDependencyFilters.of(getSession())
                                        .byRunnable()
                        )
                        .setOptional(false).getResultClassLoader();
                urls = ((DefaultNClassLoader) resultClassLoader).getURLs();
                //class loader do not expect leading '/'
                if (loc.length() > 1 && loc.startsWith("/")) {
                    loc = loc.substring(1);
                }
                URL resource = resultClassLoader.getResource(loc);
                if (resource != null) {
                    urlPath = NPath.of(resource, getSession());
                }
            } catch (Exception e) {
                //e.printStackTrace();
                //ignore...
            }
        }
        return urlPath;
    }

    @Override
    public NStream<NPath> list(NPath basePath) {
        return toURLPath().stream();
    }

    @Override
    public NFormatSPI formatter(NPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getName(NPath basePath) {
        String loc = getLocation(basePath);
        return loc == null ? "" : Paths.get(loc).getFileName().toString();
    }

    @Override
    public String getProtocol(NPath basePath) {
        return "nuts-resource";
    }

    @Override
    public NPath resolve(NPath basePath, String path) {
        return NPath.of(new NResourcePath(rebuildURL(
                NPath.of(location, session).resolve(path).toString()
                , ids.toArray(new NId[0])), getSession()), session);
    }

    @Override
    public NPath resolve(NPath basePath, NPath path) {
        return NPath.of(new NResourcePath(rebuildURL(
                NPath.of(location, session).resolve(path).toString()
                , ids.toArray(new NId[0])), getSession()), session);
    }

    @Override
    public NPath resolveSibling(NPath basePath, String path) {
        return NPath.of(new NResourcePath(rebuildURL(
                NPath.of(location, session).resolveSibling(path).toString()
                , ids.toArray(new NId[0])), getSession()), session);
    }

    @Override
    public NPath resolveSibling(NPath basePath, NPath path) {
        return NPath.of(new NResourcePath(rebuildURL(
                NPath.of(location, session).resolveSibling(path).toString()
                , ids.toArray(new NId[0])), getSession()), session);
    }

    @Override
    public NPath toCompressedForm(NPath basePath) {
        return new NCompressedPath(basePath, new NResourceCompressedPath());
    }

    @Override
    public NOptional<URL> toURL(NPath basePath) {
        NPath up = toURLPath();
        if (up != null) {
            return up.toURL();
        }
        throw new NIOException(getSession(), NMsg.ofC("unable to resolve url from %s", toString()));
    }

    @Override
    public NOptional<Path> toPath(NPath basePath) {
        NPath up = toURLPath();
        if (up != null) {
            return up.toPath();
        }
        throw new NIOException(getSession(), NMsg.ofC("unable to resolve file from %s", toString()));
    }

    @Override
    public boolean isSymbolicLink(NPath basePath) {
        NPath up = toURLPath();
        return up != null && up.isSymbolicLink();
    }

    @Override
    public boolean isOther(NPath basePath) {
        NPath up = toURLPath();
        return up != null && up.isOther();
    }

    @Override
    public boolean isDirectory(NPath basePath) {
        NPath up = toURLPath();
        return up != null && up.isDirectory();
    }

    @Override
    public boolean isLocal(NPath basePath) {
        return toURLPath().isLocal();
    }

    @Override
    public boolean isRegularFile(NPath basePath) {
        NPath up = toURLPath();
        return up != null && up.isRegularFile();
    }

    @Override
    public boolean exists(NPath basePath) {
        NPath up = toURLPath();
        if (up == null) {
            return false;
        }
        return up.exists();
    }

    @Override
    public long getContentLength(NPath basePath) {
        NPath up = toURLPath();
        if (up == null) {
            return -1;
        }
        return up.getContentLength();
    }

    @Override
    public String getContentEncoding(NPath basePath) {
        NPath up = toURLPath();
        if (up != null) {
            return up.getContentEncoding();
        }
        return null;
    }

    @Override
    public String getContentType(NPath basePath) {
        NPath up = toURLPath();
        if (up != null) {
            return up.getContentType();
        }
        return null;
    }

    @Override
    public String getLocation(NPath basePath) {
        return location;
    }

    @Override
    public InputStream getInputStream(NPath basePath, NPathOption... options) {
        NPath up = toURLPath();
        if (up == null) {
            throw new NIOException(getSession(), NMsg.ofC("unable to resolve input stream %s", toString()));
        }
        return up.getInputStream();
    }

    @Override
    public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
        NPath up = toURLPath();
        if (up == null) {
            throw new NIOException(getSession(), NMsg.ofC("unable to resolve output stream %s", toString()));
        }
        return up.getOutputStream();
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public void delete(NPath basePath, boolean recurse) {
        NPath up = toURLPath();
        if (up == null) {
            throw new NIOException(getSession(), NMsg.ofC("unable to delete %s", toString()));
        }
        up.delete(recurse);
    }

    @Override
    public void mkdir(boolean parents, NPath basePath) {
        NPath up = toURLPath();
        if (up == null) {
            throw new NIOException(getSession(), NMsg.ofC("unable to mkdir %s", toString()));
        }
        up.mkdir(parents);
    }

    @Override
    public Instant getLastModifiedInstant(NPath basePath) {
        NPath up = toURLPath();
        if (up == null) {
            return null;
        }
        return up.getLastModifiedInstant();
    }

    @Override
    public Instant getLastAccessInstant(NPath basePath) {
        NPath up = toURLPath();
        return up != null ? up.getLastAccessInstant() : null;
    }

    @Override
    public Instant getCreationInstant(NPath basePath) {
        NPath up = toURLPath();
        return up != null ? up.getCreationInstant() : null;
    }

    @Override
    public NPath getParent(NPath basePath) {
        String ppath = URLPath.getURLParentPath(location);
        if (ppath == null) {
            return null;
        }
        return NPath.of(rebuildURL(ppath, ids.toArray(new NId[0])), getSession());
    }

    @Override
    public NPath toAbsolute(NPath basePath, NPath rootPath) {
        return basePath;
    }

    @Override
    public NPath normalize(NPath basePath) {
        return basePath;
    }

    @Override
    public boolean isAbsolute(NPath basePath) {
        return true;
    }

    @Override
    public String owner(NPath basePath) {
        NPath up = toURLPath();
        return up != null ? up.owner() : null;
    }

    @Override
    public String group(NPath basePath) {
        NPath up = toURLPath();
        return up != null ? up.group() : null;
    }

    @Override
    public Set<NPathPermission> getPermissions(NPath basePath) {
        NPath up = toURLPath();
        return up != null ? up.getPermissions() : new LinkedHashSet<>();
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
        return toURLPath().walk(maxDepth, options);
    }

    @Override
    public NPath subpath(NPath basePath, int beginIndex, int endIndex) {
        return NPath.of(new NResourcePath(rebuildURL(
                NPath.of(location, getSession()).subpath(beginIndex, endIndex).toString()
                , ids.toArray(new NId[0])), getSession()), session);
    }

    @Override
    public List<String> getLocationItems(NPath basePath) {
        return NPath.of(location, getSession()).getLocationItems();
    }

    @Override
    public void moveTo(NPath basePath, NPath other, NPathOption... options) {
        throw new NIOException(session, NMsg.ofC("unable to move %s", this));
    }

    @Override
    public void copyTo(NPath basePath, NPath other, NPathOption... options) {
        NCp.of(session).from(basePath).to(other).run();
    }

    @Override
    public void walkDfs(NPath basePath, NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {
        toURLPath().walkDfs(visitor, maxDepth, options);
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
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NResourcePath that = (NResourcePath) o;
        return urlPathLookedUp == that.urlPathLookedUp && Objects.equals(path, that.path) && Objects.equals(ids, that.ids) && Objects.equals(location, that.location) && Objects.equals(urlPath, that.urlPath);
    }

    @Override
    public String toString() {
        return String.valueOf(path);
    }

    private static class MyPathFormat implements NFormatSPI {

        private NResourcePath p;

        public MyPathFormat(NResourcePath p) {
            this.p = p;
        }
        @Override
        public String getName() {
            return "path";
        }

        public NString asFormattedString() {
            String path = p.path;
            NTexts text = NTexts.of(p.getSession());
            NTextBuilder tb = text.ofBuilder();
            tb.append("nuts-resource://", NTextStyle.primary1());
            if (path.startsWith("nuts-resource://(")) {
                tb.append("(", NTextStyle.separator());
                int x = path.indexOf(')');
                if (x > 0) {
                    String idsStr = path.substring("nuts-resource://(".length(), x);
                    tb.appendJoined(
                            NTexts.of(p.getSession()).ofStyled(";", NTextStyle.separator()),
                            StringTokenizerUtils.splitSemiColon(idsStr).stream().map(xi -> {
                                xi = xi.trim();
                                if (xi.length() > 0) {
                                    NId pp = NId.of(xi).get(p.getSession());
                                    if (pp == null) {
                                        return xi;
                                    }
                                    return pp;
                                }
                                return null;
                            }).filter(Objects::nonNull).collect(Collectors.toList()));
                    tb.append(")", NTextStyle.separator());
                    tb.append(path.substring(x + 1), NTextStyle.path());
                } else {
                    return text.ofText(path);
                }
            } else if (path.startsWith("nuts-resource://")) {
                int x = path.indexOf('/', "nuts-resource://".length());
                if (x > 0) {
                    String sid = path.substring("nuts-resource://".length(), x);
                    NId ii = NId.of(sid).get(p.getSession());
                    if (ii == null) {
                        tb.append(sid);
                    } else {
                        tb.append(ii);
                    }
                    tb.append(path.substring(x), NTextStyle.path());
                } else {
                    return text.ofText(path);
                }
            } else {
                return text.ofText(path);
            }
            return tb.toText();
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

    public static class NResourceFactory implements NPathFactory {
        NWorkspace ws;

        public NResourceFactory(NWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NSupported<NPathSPI> createPath(String path, NSession session, ClassLoader classLoader) {
            NSessionUtils.checkSession(ws, session);
            try {
                if (path.startsWith("nuts-resource:")) {
                    return NSupported.of(DEFAULT_SUPPORT, () -> new NResourcePath(path, session));
                }
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }

        @Override
        public int getSupportLevel(NSupportLevelContext context) {
            String path= context.getConstraints();
            if (path.startsWith("nuts-resource:")) {
                return DEFAULT_SUPPORT;
            }
            return NO_SUPPORT;
        }
    }

    private class NResourceCompressedPath implements NCompressedPathHelper {
        @Override
        public NString toCompressedString(NPath base, NSession session) {
            return rebuildURL2(NPathParts.compressPath(location, session),
                    ids.stream().map(x -> NId.of(x.getArtifactId()).get(session)).toArray(NId[]::new)
                    ,session
            );
        }
    }

    @Override
    public byte[] getDigest(NPath basePath, String algo) {
        return null;
    }
}
