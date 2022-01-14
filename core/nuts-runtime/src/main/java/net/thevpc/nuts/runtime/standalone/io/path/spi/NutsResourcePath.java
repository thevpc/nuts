package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNutsClassLoader;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class NutsResourcePath implements NutsPathSPI {

    private String path;
    private final List<NutsId> ids;
    private String location;
    private boolean urlPathLookedUp = false;
    private URL[] urls = null;
    private NutsPath urlPath = null;
    private NutsSession session;

    public NutsResourcePath(String path, NutsSession session) {
        this.path = path;
        this.session = session;
        String idsStr;
        if (path.startsWith("nuts-resource://(")) {
            int x = path.indexOf(')');
            if (x > 0) {
                idsStr = path.substring("nuts-resource://(".length(), x);
                location = path.substring(x + 1);
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid path %s", path));
            }
        } else if (path.startsWith("nuts-resource://")) {
            int x = path.indexOf('/', "nuts-resource://".length());
            if (x > 0) {
                idsStr = path.substring("nuts-resource://".length(), x);
                location = path.substring(x);
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid path %s", path));
            }
        } else {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid path %s", path));
        }
        NutsIdParser nutsIdParser = NutsIdParser.of(session);
        this.ids = StringTokenizerUtils.splitSemiColon(idsStr).stream().map(x -> {
            x = x.trim();
            if (x.length() > 0) {
                return nutsIdParser.parse(x);
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    protected String rebuildURL(String location, NutsId[] ids) {
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

    public NutsPath toURLPath() {
        if (!urlPathLookedUp) {
            urlPathLookedUp = true;
            try {
                String loc = location;
                ClassLoader resultClassLoader = getSession().search().addIds(
                        this.ids.toArray(new NutsId[0])
                ).setLatest(true).setContent(true).setDependencies(true)
                        .setDependencyFilter(
                                NutsDependencyFilters.of(getSession())
                                        .byRunnable()
                        )
                        .setOptional(false).getResultClassLoader();
                urls = ((DefaultNutsClassLoader) resultClassLoader).getURLs();
                //class loader do not expect leading '/'
                if (loc.length() > 1 && loc.startsWith("/")) {
                    loc = loc.substring(1);
                }
                URL resource = resultClassLoader.getResource(loc);
                if (resource != null) {
                    urlPath = NutsPath.of(resource, getSession());
                }
            } catch (Exception e) {
                //e.printStackTrace();
                //ignore...
            }
        }
        return urlPath;
    }

    @Override
    public NutsPath normalize(NutsPath basePath) {
        return basePath;
    }

    @Override
    public NutsStream<NutsPath> list(NutsPath basePath) {
        return toURLPath().list();
    }

    @Override
    public NutsStream<NutsPath> walk(NutsPath basePath, int maxDepth, NutsPathOption[] options) {
        return toURLPath().walk(maxDepth, options);
    }

    @Override
    public void walkDfs(NutsPath basePath, NutsTreeVisitor<NutsPath> visitor, int maxDepth, NutsPathOption... options) {
        toURLPath().walkDfs(visitor,maxDepth, options);
    }

    @Override
    public NutsFormatSPI formatter(NutsPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getName(NutsPath basePath) {
        String loc = getLocation(basePath);
        return loc == null ? "" : Paths.get(loc).getFileName().toString();
    }

    @Override
    public String getProtocol(NutsPath basePath) {
        return "nuts-resource";
    }

    @Override
    public NutsPath resolve(NutsPath basePath, String path) {
        return NutsPath.of(new NutsResourcePath(rebuildURL(
                NutsPath.of(location,session).resolve(path).toString()
                ,ids.toArray(new NutsId[0])), getSession()),session);
    }

    @Override
    public NutsPath resolve(NutsPath basePath, NutsPath path) {
        return NutsPath.of(new NutsResourcePath(rebuildURL(
                NutsPath.of(location,session).resolve(path).toString()
                ,ids.toArray(new NutsId[0])), getSession()),session);
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, String path) {
        return NutsPath.of(new NutsResourcePath(rebuildURL(
                NutsPath.of(location,session).resolveSibling(path).toString()
                ,ids.toArray(new NutsId[0])), getSession()),session);
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, NutsPath path) {
        return NutsPath.of(new NutsResourcePath(rebuildURL(
                NutsPath.of(location,session).resolveSibling(path).toString()
                ,ids.toArray(new NutsId[0])), getSession()),session);
    }


    @Override
    public NutsPath subpath(NutsPath basePath, int beginIndex, int endIndex) {
        return NutsPath.of(new NutsResourcePath(rebuildURL(
                NutsPath.of(location,getSession()).subpath(beginIndex,endIndex).toString()
                ,ids.toArray(new NutsId[0])), getSession()),session);
    }

    @Override
    public String[] getItems(NutsPath basePath) {
        return NutsPath.of(location,getSession()).getItems();
    }

    @Override
    public URL toURL(NutsPath basePath) {
        NutsPath up = toURLPath();
        if (up != null) {
            return up.toURL();
        }
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve url from %s", toString()));
    }

    @Override
    public Path toFile(NutsPath basePath) {
        NutsPath up = toURLPath();
        if (up != null) {
            return up.toFile();
        }
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve file from %s", toString()));
    }

    @Override
    public boolean isDirectory(NutsPath basePath) {
        NutsPath up = toURLPath();
        return up != null && up.isDirectory();
    }

    @Override
    public boolean isRegularFile(NutsPath basePath) {
        NutsPath up = toURLPath();
        return up != null && up.isRegularFile();
    }

    @Override
    public boolean isSymbolicLink(NutsPath basePath) {
        NutsPath up = toURLPath();
        return up != null && up.isSymbolicLink();
    }

    @Override
    public boolean isOther(NutsPath basePath) {
        NutsPath up = toURLPath();
        return up != null && up.isOther();
    }

    @Override
    public Instant getLastAccessInstant(NutsPath basePath) {
        NutsPath up = toURLPath();
        return up != null ? up.getLastAccessInstant() : null;
    }

    @Override
    public Instant getCreationInstant(NutsPath basePath) {
        NutsPath up = toURLPath();
        return up != null ? up.getCreationInstant() : null;
    }

    @Override
    public String owner(NutsPath basePath) {
        NutsPath up = toURLPath();
        return up != null ? up.owner() : null;
    }

    @Override
    public String group(NutsPath basePath) {
        NutsPath up = toURLPath();
        return up != null ? up.group() : null;
    }

    @Override
    public Set<NutsPathPermission> getPermissions(NutsPath basePath) {
        NutsPath up = toURLPath();
        return up != null ? up.getPermissions() : new LinkedHashSet<>();
    }

    @Override
    public boolean exists(NutsPath basePath) {
        NutsPath up = toURLPath();
        if (up == null) {
            return false;
        }
        return up.exists();
    }

    @Override
    public long getContentLength(NutsPath basePath) {
        NutsPath up = toURLPath();
        if (up == null) {
            return -1;
        }
        return up.getContentLength();
    }

    @Override
    public String getContentEncoding(NutsPath basePath) {
        NutsPath up = toURLPath();
        if (up != null) {
            return up.getContentEncoding();
        }
        return null;
    }

    @Override
    public String getContentType(NutsPath basePath) {
        NutsPath up = toURLPath();
        if (up != null) {
            return up.getContentType();
        }
        return null;
    }

    @Override
    public String getLocation(NutsPath basePath) {
        return location;
    }

    @Override
    public NutsPath getParent(NutsPath basePath) {
        String ppath = URLPath.getURLParentPath(location);
        if (ppath == null) {
            return null;
        }
        return NutsPath.of(rebuildURL(ppath, ids.toArray(new NutsId[0])), getSession());
    }

    @Override
    public InputStream getInputStream(NutsPath basePath) {
        NutsPath up = toURLPath();
        if (up == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve input stream %s", toString()));
        }
        return up.getInputStream();
    }

    @Override
    public OutputStream getOutputStream(NutsPath basePath) {
        NutsPath up = toURLPath();
        if (up == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve output stream %s", toString()));
        }
        return up.getOutputStream();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public void delete(NutsPath basePath, boolean recurse) {
        NutsPath up = toURLPath();
        if (up == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to delete %s", toString()));
        }
        up.delete(recurse);
    }

    @Override
    public void mkdir(boolean parents, NutsPath basePath) {
        NutsPath up = toURLPath();
        if (up == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to mkdir %s", toString()));
        }
        up.mkdir(parents);
    }

    @Override
    public Instant getLastModifiedInstant(NutsPath basePath) {
        NutsPath up = toURLPath();
        if (up == null) {
            return null;
        }
        return up.getLastModifiedInstant();
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
        NutsResourcePath that = (NutsResourcePath) o;
        return urlPathLookedUp == that.urlPathLookedUp && Objects.equals(path, that.path) && Objects.equals(ids, that.ids) && Objects.equals(location, that.location) && Objects.equals(urlPath, that.urlPath);
    }

    @Override
    public String toString() {
        return String.valueOf(path);
    }

    private static class MyPathFormat implements NutsFormatSPI {

        private NutsResourcePath p;

        public MyPathFormat(NutsResourcePath p) {
            this.p = p;
        }

        public NutsString asFormattedString() {
            String path = p.path;
            NutsTexts text = NutsTexts.of(p.getSession());
            NutsTextBuilder tb = text.builder();
            tb.append("nuts-resource://", NutsTextStyle.primary1());
            if (path.startsWith("nuts-resource://(")) {
                tb.append("(", NutsTextStyle.separator());
                int x = path.indexOf(')');
                if (x > 0) {
                    String idsStr = path.substring("nuts-resource://(".length(), x);
                    NutsIdParser nutsIdParser = NutsIdParser.of(p.getSession());
                    tb.appendJoined(
                            NutsTexts.of(p.getSession()).ofStyled(";", NutsTextStyle.separator()),
                            StringTokenizerUtils.splitSemiColon(idsStr).stream().map(xi -> {
                                xi = xi.trim();
                                if (xi.length() > 0) {
                                    NutsId pp = nutsIdParser.parse(xi);
                                    if (pp == null) {
                                        return xi;
                                    }
                                    return pp;
                                }
                                return null;
                            }).filter(Objects::nonNull).collect(Collectors.toList()));
                    tb.append(")", NutsTextStyle.separator());
                    tb.append(path.substring(x + 1), NutsTextStyle.path());
                } else {
                    return text.toText(path);
                }
            } else if (path.startsWith("nuts-resource://")) {
                int x = path.indexOf('/', "nuts-resource://".length());
                if (x > 0) {
                    String sid = path.substring("nuts-resource://".length(), x);
                    NutsId ii = NutsId.of(sid, p.getSession());
                    if (ii == null) {
                        tb.append(sid);
                    } else {
                        tb.append(ii);
                    }
                    tb.append(path.substring(x), NutsTextStyle.path());
                } else {
                    return text.toText(path);
                }
            } else {
                return text.toText(path);
            }
            return tb.toText();
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
    public NutsPath toAbsolute(NutsPath basePath, NutsPath rootPath) {
        return basePath;
    }

    @Override
    public boolean isAbsolute(NutsPath basePath) {
        return true;
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
    public NutsPath toCompressedForm(NutsPath basePath) {
        return null;
    }

    @Override
    public void moveTo(NutsPath basePath, NutsPath other, NutsPathOption... options) {
        throw new NutsIOException(session,NutsMessage.cstyle("unable to move %s",this));
    }

    @Override
    public void copyTo(NutsPath basePath, NutsPath other, NutsPathOption... options) {
        NutsCp.of(session).from(basePath).to(other).run();
    }

    @Override
    public NutsPath getRoot(NutsPath basePath) {
        if(isRoot(basePath)){
            return basePath;
        }
        return basePath.getParent().getRoot();
    }

    public static class NutsResourceFactory implements NutsPathFactory {
        NutsWorkspace ws;

        public NutsResourceFactory(NutsWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsWorkspaceUtils.checkSession(ws, session);
            try {
                if (path.startsWith("nuts-resource:")) {
                    return NutsSupported.of(10,()->new NutsResourcePath(path, session));
                }
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }
    }

    @Override
    public boolean isLocal(NutsPath basePath) {
        return toURLPath().isLocal();
    }

    @Override
    public NutsPath toRelativePath(NutsPath basePath, NutsPath parentPath) {
        String child=basePath.getLocation();
        String parent=parentPath.getLocation();
        if (child.startsWith(parent)) {
            child = child.substring(parent.length());
            if (child.startsWith("/") || child.startsWith("\\")) {
                child = child.substring(1);
            }
            return NutsPath.of(child,session);
        }
        return null;
    }
}
