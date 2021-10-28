package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.DefaultNutsClassLoader;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class NutsResourcePath implements NutsPathSPI {
    private String path;
    private List<NutsId> ids;
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
        NutsIdParser nutsIdParser = session.id().parser().setLenient(false);
        this.ids = Arrays.stream(idsStr.split(";")).map(x -> {
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
                                getSession().filters().dependency()
                                        .byScope(NutsDependencyScopePattern.RUN)
                        )
                        .setOptional(false).getResultClassLoader();
                urls = ((DefaultNutsClassLoader) resultClassLoader).getURLs();
                //class loader do not expect
                if (loc.length() > 1 && loc.startsWith("/")) {
                    loc = loc.substring(1);
                }
                URL resource = resultClassLoader.getResource(loc);
                if (resource != null) {
                    urlPath = getSession().io().path(resource);
                }
            } catch (Exception e) {
                //e.printStackTrace();
                //ignore...
            }
        }
        return urlPath;
    }

    @Override
    public NutsPath normalize() {
        return new NutsPathFromSPI(this);
    }


    @Override
    public NutsPath[] getChildren() {
        //TODO : parse urls for children!
        return new NutsPath[0];
    }

    @Override
    public NutsFormatSPI getFormatterSPI() {
        return new MyPathFormat(this);
    }

    public String getName() {
        return CoreIOUtils.getURLName(path);
    }

    @Override
    public String getProtocol() {
        return "nuts-resource";
    }

    @Override
    public NutsPath resolve(String path) {
        String[] others = Arrays.stream(NutsUtilStrings.trim(path).split("[/\\\\]"))
                .filter(x -> x.length() > 0).toArray(String[]::new);
        if (others.length > 0) {
            StringBuilder loc = new StringBuilder(location);
            if (loc.length() == 0 || loc.charAt(loc.length() - 1) != '/') {
                loc.append('/');
            }
            loc.append(String.join("/", others));
            return new NutsPathFromSPI(new NutsResourcePath(loc.toString(), getSession()));
        }
        return new NutsPathFromSPI(this);
    }

    @Override
    public URL toURL() {
        NutsPath up = toURLPath();
        if (up == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve url %s", toString()));
        }
        return up.toURL();
    }

    @Override
    public Path toFile() {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve file %s", toString()));
    }

    @Override
    public boolean isDirectory() {
        NutsPath up = toURLPath();
        return up != null && up.isDirectory();
    }

    @Override
    public boolean isRegularFile() {
        NutsPath up = toURLPath();
        return up != null && up.isRegularFile();
    }

    @Override
    public boolean isSymbolicLink() {
        NutsPath up = toURLPath();
        return up != null && up.isSymbolicLink();
    }

    @Override
    public boolean isOther() {
        NutsPath up = toURLPath();
        return up != null && up.isOther();
    }

    @Override
    public Instant getLastAccessInstant() {
        NutsPath up = toURLPath();
        return up != null ? up.getLastAccessInstant():null;
    }

    @Override
    public Instant getCreationInstant() {
        NutsPath up = toURLPath();
        return up != null ? up.getCreationInstant():null;
    }

    @Override
    public String owner() {
        NutsPath up = toURLPath();
        return up != null ? up.owner():null;
    }

    @Override
    public String group() {
        NutsPath up = toURLPath();
        return up != null ? up.group():null;
    }

    @Override
    public Set<NutsPathPermission> permissions() {
        NutsPath up = toURLPath();
        return up != null ? up.getPermissions():new LinkedHashSet<>();
    }

    @Override
    public boolean exists() {
        NutsPath up = toURLPath();
        if (up == null) {
            return false;
        }
        return up.exists();
    }

    @Override
    public long getContentLength() {
        NutsPath up = toURLPath();
        if (up == null) {
            return -1;
        }
        return up.getContentLength();
    }

    @Override
    public String getContentEncoding() {
        NutsPath up = toURLPath();
        if (up != null) {
            return up.getContentEncoding();
        }
        return null;
    }

    @Override
    public String getContentType() {
        NutsPath up = toURLPath();
        if (up != null) {
            return up.getContentType();
        }
        return null;
    }

    @Override
    public String asString() {
        return path;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public NutsPath getParent() {
        String ppath = CoreIOUtils.getURLParentPath(location);
        if(ppath==null){
            return null;
        }
        return session.io().path(rebuildURL(ppath,ids.toArray(new NutsId[0])));
    }

    @Override
    public InputStream getInputStream() {
        NutsPath up = toURLPath();
        if (up == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve input stream %s", toString()));
        }
        return up.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
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
    public void delete(boolean recurse) {
        NutsPath up = toURLPath();
        if (up == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to delete %s", toString()));
        }
        up.delete(recurse);
    }

    @Override
    public void mkdir(boolean parents) {
        NutsPath up = toURLPath();
        if (up == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to mkdir %s", toString()));
        }
        up.mkdir(parents);
    }

    @Override
    public Instant getLastModifiedInstant() {
        NutsPath up = toURLPath();
        if (up == null) {
            return null;
        }
        return up.getLastModifiedInstant();
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, session);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsResourcePath that = (NutsResourcePath) o;
        return urlPathLookedUp == that.urlPathLookedUp && Objects.equals(path, that.path) && Objects.equals(ids, that.ids) && Objects.equals(location, that.location) && Objects.equals(urlPath, that.urlPath) && Objects.equals(session, that.session);
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
            NutsTextManager text = p.getSession().text();
            NutsTextBuilder tb = text.builder();
            tb.append("nuts-resource://", NutsTextStyle.primary1());
            if (path.startsWith("nuts-resource://(")) {
                tb.append("(", NutsTextStyle.separator());
                int x = path.indexOf(')');
                if (x > 0) {
                    String idsStr = path.substring("nuts-resource://(".length(), x);
                    NutsIdParser nutsIdParser = p.getSession().id().parser().setLenient(false);
                    tb.appendJoined(
                            p.getSession().text().ofStyled(";",NutsTextStyle.separator()),
                            Arrays.stream(idsStr.split(";")).map(xi -> {
                        xi = xi.trim();
                        if (xi.length() > 0) {
                            NutsId pp = nutsIdParser.parse(xi);
                            if(pp==null){
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
                    NutsId ii = p.getSession().id().parser().setLenient(true).parse(sid);
                    if(ii==null) {
                        tb.append(sid);
                    }else{
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
    public NutsPath toAbsolute(NutsPath basePath) {
        return new NutsPathFromSPI(this);
    }

    @Override
    public boolean isAbsolute() {
        return true;
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

}
