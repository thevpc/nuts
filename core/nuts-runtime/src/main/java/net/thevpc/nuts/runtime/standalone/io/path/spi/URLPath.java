package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.concurrent.NScoredCallable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.util.NPathParts;
import net.thevpc.nuts.concurrent.NCachedSupplier;
import net.thevpc.nuts.runtime.standalone.xtra.web.DefaultNWebCli;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NPathFactorySPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.net.NWebCli;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class URLPath implements NPathSPI {
    public static final Pattern MOSTLY_URL_PATTERN = Pattern.compile("([a-zA-Z][a-zA-Z0-9_-]+):.*");

    protected URL url;
    protected static final NLRUMap<URL, NCachedSupplier<CacheInfo>> cacheManager = new NLRUMap<URL, NCachedSupplier<CacheInfo>>(1024);


    public URLPath(URL url) {
        this(url, false);
    }

    protected URLPath(URL url, boolean acceptNull) {
        if (url == null) {
            if (!acceptNull) {
                throw new IllegalArgumentException("invalid url");
            }
        }
        this.url = url;
    }

    private NCachedSupplier<CacheInfo> cachedHeader() {
        NCachedSupplier<CacheInfo> o = cacheManager.get(url);
        if (o == null) {
            o = new NCachedSupplier<>(
                    () -> loadCacheInfo(url), 5000
            );
            cacheManager.put(url, o);
        }
        return o;
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

    public static String getURLName(String path) {
        String name;
        int index = path.lastIndexOf('/');
        if (index < 0) {
            name = path;
        } else {
            name = path.substring(index + 1);
        }
        index = name.indexOf('?');
        if (index >= 0) {
            name = name.substring(0, index);
        }
        name = name.trim();
        return name;
    }

    public static File _toFile(URL url) {
        if (url == null) {
            return null;
        }
        if ("file".equals(url.getProtocol())) {
            try {
                return Paths.get(url.toURI()).toFile();
            } catch (URISyntaxException e) {
                //
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        URLPath urlPath = (URLPath) o;
        return Objects.equals(url, urlPath.url);
    }

    @Override
    public String toString() {
        return url == null ? ("broken-url") : url.toString();
    }

    @Override
    public NStream<NPath> list(NPath basePath) {
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.stream();
        }
        //should we implement other protocols ?
        return NStream.ofEmpty();
    }

    @Override
    public NFormatSPI formatter(NPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getName(NPath basePath) {
        return new NPathParts(toString()).getName();
    }

    @Override
    public String getProtocol(NPath basePath) {
        return url == null ? null : url.getProtocol();
    }

    @Override
    public NPath resolve(NPath basePath, String path) {
        if (url == null) {
            NPathParts p = new NPathParts(toString());
            String spath = path.toString().replace("\\", "/");
            String u = p.getFile();
            if (!u.endsWith("/") && !spath.startsWith("/")) {
                u += "/";
            }
            u += spath;
            return rebuildURLPath(rebuildURLString(p.getProtocol(), p.getAuthority(), u, p.getRef()));
        }
        String spath = path.toString().replace("\\", "/");
        String u = url.getFile();
        if (!u.endsWith("/") && !spath.startsWith("/")) {
            u += "/";
        }
        u += spath;
        return rebuildURLPath(rebuildURLString(url.getProtocol(), url.getAuthority(), u, url.getRef()));
    }


    public NPath resolveSibling(NPath basePath, String path) {
        if (url == null) {
            NPathParts p = new NPathParts(toString());
            String u = _parent(p.getFile());
            String spath = path.replace("\\", "/");
            if (u == null || u.isEmpty()) {
                u = spath;
            } else {
                if (!u.endsWith("/") && !spath.startsWith("/")) {
                    u += "/";
                }
                u += spath;
            }
            return rebuildURLPath(rebuildURLString(p.getProtocol(), p.getAuthority(), u, p.getRef()));
        }
        String u = _parent(url.getFile());
        String spath = path.replace("\\", "/");
        if (u == null || u.isEmpty()) {
            u = spath;
        } else {
            if (!u.endsWith("/") && !spath.startsWith("/")) {
                u += "/";
            }
            u += spath;
        }
        return rebuildURLPath(rebuildURLString(url.getProtocol(), url.getAuthority(), u, url.getRef()));
    }

    @Override
    public NPath toCompressedForm(NPath basePath) {
        return null;
    }

    @Override
    public NOptional<URL> toURL(NPath basePath) {
        if (url == null) {
            return NOptional.ofEmpty(() -> NMsg.ofC("unable to resolve url %s", toString()));
        }
        return NOptional.of(url);
    }

    @Override
    public NOptional<Path> toPath(NPath basePath) {
        return toURL(basePath).flatMap(x -> {
            File f = _toFile(x);
            if (f != null) {
                return NOptional.of(f.toPath());
            }
            return NOptional.ofEmpty(() -> NMsg.ofC("unable to resolve url %s", toString()));
        });
    }

    @Override
    public NPathType type(NPath basePath) {
        if (toString().endsWith("/")) {
            //if (exists(basePath)) {
            return NPathType.DIRECTORY;
            //}
            //return NPathType.NOT_FOUND;
        }
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.type();
        }
        if (exists(basePath)) {
            return NPathType.FILE;
        }
        return NPathType.NOT_FOUND;
    }

    @Override
    public boolean isLocal(NPath basePath) {
        String urlString = url.toString();
        int x = urlString.indexOf(':');
        if (x >= 0) {
            switch (urlString.substring(0, x)) {
                case "file":
                case "classpath":
                case "resource":
                case "jar":
                    return true;
                case "http":
                case "https":
                case "ftp":
                case "ftps":
                case "sftp":
                case "ssh":
                    return false;
            }
        }
        NPath f = asFilePath(basePath);
        return f != null && f.isLocal();
    }

    @Override
    public boolean exists(NPath basePath) {
        if (url == null) {
            return false;
        }
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.exists();
        }
        try {
            CacheInfo a = cachedHeader().getValue();
            if (a != null) {
                int r = a.responseCode;
                return r >= 200 && r < 300;
            }
        } catch (Exception e) {
            //
        }
        try (InputStream is = DefaultNWebCli.prepareGlobalOpenStream(url)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public long contentLength(NPath basePath) {
        if (url == null) {
            return -1;
        }
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.contentLength();
        }
        try {
            CacheInfo a = cachedHeader().getValue();
            if (a != null) {
                return a.contentLength;
            }
        } catch (Exception e) {
            //
        }
        return -1;
    }

    public String getContentEncoding(NPath basePath) {
        try {
            CacheInfo a = cachedHeader().getValue();
            if (a != null) {
                return a.contentEncoding;
            }
        } catch (Exception e) {
            //
        }
        return null;
    }

    public String getContentType(NPath basePath) {
        if (url == null) {
            return null;
        }
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.getContentType();
        }
        try {
            CacheInfo a = cachedHeader().getValue();
            if (a != null) {
                return a.contentType;
            }
        } catch (Exception e) {
            //
        }
        return NIO.of().probeContentType(basePath);
    }

    @Override
    public String getCharset(NPath basePath) {
        if (url == null) {
            return null;
        }
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.getContentType();
        }
        try {
            CacheInfo a = cachedHeader().getValue();
            if (a != null) {
                return a.contentEncoding;
            }
        } catch (Exception e) {
            //
        }
        return NIO.of().probeCharset(basePath);
    }

    @Override
    public String getLocation(NPath basePath) {
        return url == null ? null : url.getFile();
    }

    public InputStream getInputStream(NPath basePath, NPathOption... options) {
        if (url == null) {
            throw new NIOException(NMsg.ofC("unable to resolve input stream %s", toString()));
        }
        if ("file".equals(url.getProtocol())) {
            try {
                return Files.newInputStream(CoreIOUtils.resolveLocalPathFromURL(url));
            } catch (IOException e) {
                throw new NIOException(NMsg.ofC("unable to resolve input stream %s", toString()));
            }
        }
        if ("http".equals(url.getProtocol()) || "https".equals(url.getProtocol())) {
            NWebCli best = NExtensions.of().createComponent(NWebCli.class, url).get();
            return best.req().GET().setUrl(url.toString()).run().getContent().getInputStream();
        }
        try {
            return DefaultNWebCli.prepareGlobalOpenStream(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
        try {
            if (url == null) {
                throw new NIOException(NMsg.ofC("unable to resolve output stream %s", toString()));
            }
            URLConnection c = url.openConnection();
            DefaultNWebCli.prepareGlobalConnection(c);
            return c.getOutputStream();
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    @Override
    public void delete(NPath basePath, boolean recurse) {
        if (url != null) {
            NPath f = asFilePath(basePath);
            if (f != null) {
                f.delete(recurse);
                return;
            }
        }
        throw new NIOException(NMsg.ofC("unable to delete %s", toString()));
    }

    @Override
    public void mkdir(boolean parents, NPath basePath) {
        if (url != null) {
            NPath f = asFilePath(basePath);
            if (f != null) {
                f.mkdir(parents);
                return;
            }
        }
        throw new NIOException(NMsg.ofC("unable to mkdir %s", toString()));
    }

    @Override
    public Instant getLastModifiedInstant(NPath basePath) {
        if (url == null) {
            return null;
        }
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.lastModifiedInstant();
        }
        try {
            CacheInfo a = cachedHeader().getValue();
            if (a != null) {
                return a.lastModified;
            }
        } catch (Exception e) {
            //
        }
        return null;
    }

    @Override
    public Instant getLastAccessInstant(NPath basePath) {
        NPath f = asFilePath(basePath);
        return (f != null) ? f.lastAccessInstant() : null;
    }

    @Override
    public Instant getCreationInstant(NPath basePath) {
        NPath f = asFilePath(basePath);
        return (f != null) ? f.getCreationInstant() : null;
    }

    @Override
    public NPath getParent(NPath basePath) {
        if (url == null) {
            return null;
        }
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.getParent();
        }
        try {
            String ppath = getURLParentPath(url.getPath());
            if (ppath == null) {
                return null;
            }
            URL url = CoreIOUtils.urlOf(
                    new NPathParts(NPathParts.Type.URL,
                            this.url.getProtocol(),
                            this.url.getAuthority(),
                            ppath,
                            this.url.getQuery(),
                            this.url.getRef()
                    ).toString()
            );
            return NPath.of(url);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public NPath toAbsolute(NPath basePath, NPath rootPath) {
        return basePath;
    }

    @Override
    public NPath normalize(NPath basePath) {
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.normalize();
        }
        return basePath;
    }

    @Override
    public boolean isAbsolute(NPath basePath) {
        return true;
    }

    @Override
    public String owner(NPath basePath) {
        NPath f = asFilePath(basePath);
        return (f != null) ? f.owner() : null;
    }

    @Override
    public String group(NPath basePath) {
        NPath f = asFilePath(basePath);
        return (f != null) ? f.group() : null;
    }

    @Override
    public Set<NPathPermission> getPermissions(NPath basePath) {
        NPath f = asFilePath(basePath);
        return (f != null) ? f.getPermissions() : Collections.emptySet();
    }

    @Override
    public void setPermissions(NPath basePath, NPathPermission... permissions) {
        NPath f = asFilePath(basePath);
        if (f != null) {
            f.setPermissions(permissions);
        }
    }

    @Override
    public void addPermissions(NPath basePath, NPathPermission... permissions) {
        NPath f = asFilePath(basePath);
        if (f != null) {
            f.addPermissions(permissions);
        }
    }

    @Override
    public void removePermissions(NPath basePath, NPathPermission... permissions) {
        NPath f = asFilePath(basePath);
        if (f != null) {
            f.removePermissions(permissions);
        }
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
        switch (loc) {
            case "/":
            case "\\\\":
                return true;
        }
        return NPath.of(loc).isRoot();
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
        NPath f = asFilePath(basePath);
        if (f != null) {
            return f.walk(maxDepth, options);
        }
        // fallback to default implementation
        return null;
    }

    @Override
    public NPath subpath(NPath basePath, int beginIndex, int endIndex) {
        return rebuildURLPath(
                NPath.of(getLocation(basePath)).subpath(beginIndex, endIndex).toString()
        );
    }

    @Override
    public List<String> getNames(NPath basePath) {
        return NPath.of(getLocation(basePath)).getNames();
    }

    @Override
    public boolean moveTo(NPath basePath, NPath other, NPathOption... options) {
        throw new NIOException(NMsg.ofC("unable to move %s", this));
    }

    private static CacheInfo loadCacheInfo(URL url) {
        NChronometer chrono = NChronometer.startNow();
        boolean success = true;
        try {
            URLConnection c = url.openConnection();
            DefaultNWebCli.prepareGlobalConnection(c);
            c.setDoOutput(false);
            CacheInfo cc = new CacheInfo();
            if (c instanceof HttpURLConnection) {
                HttpURLConnection hc = (HttpURLConnection) c;
                hc.setRequestMethod("HEAD");
                cc.responseCode = hc.getResponseCode();
            } else {
                cc.responseCode = 200;
            }
            cc.contentLength = c.getContentLengthLong();
            cc.contentEncoding = c.getContentEncoding();
            cc.contentType = c.getContentType();

            long z = c.getLastModified();
            if (z > 0) {
                cc.lastModified = Instant.ofEpochMilli(z);
            }
            success = cc.responseCode >= 200 && cc.responseCode < 300;
            return cc;
        } catch (Exception ex) {
            success = false;
            //
        } finally {
            NLog.of(URLPath.class)
                    .log(NMsg.ofC("load url info %s", url).withLevel(Level.FINEST)
                            .withIntent(success ? NMsgIntent.SUCCESS : NMsgIntent.FAIL)
                            .withDurationMillis(chrono.stop().getDurationMs())
                    );
        }
        return null;
    }

    private String _parent(String p) {
        while (p.endsWith("/") || p.endsWith("\\")) {
            p = p.substring(0, p.length() - 1);
        }
        if (p.isEmpty()) {
            return null;
        }
        int x = p.lastIndexOf('/');
        int y = p.lastIndexOf('\\');
        if (x < 0) {
            x = y;
        } else if (y >= 0) {
            if (y > x) {
                x = y;
            }
        }
        if (x < 0) {
            return "";
        }
        return p.substring(0, x);
    }

    protected NPath rebuildURLPath(String other) {
        return NPath.of(other);
    }

    protected String rebuildURLString(String protocol, String authority, String file, String ref) {
        int len = protocol.length() + 1;
        if (authority != null && authority.length() > 0) {
            len += 2 + authority.length();
        }
        if (file != null) {
            len += file.length();
        }
        if (ref != null) {
            len += 1 + ref.length();
        }
        StringBuilder result = new StringBuilder(len);
        result.append(protocol);
        result.append(":");
        if (authority != null && authority.length() > 0) {
            result.append("//");
            result.append(authority);
        }
        if (file != null) {
            result.append(file);
        }
        if (ref != null) {
            result.append("#");
            result.append(ref);
        }
        return result.toString();
    }

    public NPath asFilePath(NPath basePath) {
        return toURL(basePath).flatMap(x -> {
            File f = _toFile(x);
            if (f != null) {
                return NOptional.of(NPath.of(f));
            }
            return NOptional.ofEmpty(() -> NMsg.ofC("not a local file %s", toString()));
        }).orNull();
    }

    private static class CacheInfo {
        long contentLength;
        String contentEncoding;
        String contentType;
        int responseCode;
        Instant lastModified;
    }

    private static class MyPathFormat implements NFormatSPI {

        private final URLPath p;

        public MyPathFormat(URLPath p) {
            this.p = p;
        }

        @Override
        public String getName() {
            return "path";
        }

        public NText asFormattedString() {
            if (p.url == null) {
                return NText.ofBlank();
            }
            return NText.of(p.url);
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

    public static class URLPathFactory implements NPathFactorySPI {

        public URLPathFactory() {
        }

        @Override
        public NScoredCallable<NPathSPI> createPath(String path, String protocol, ClassLoader classLoader) {
            try {
                if (path != null && path.length() > 0) {
                    char s = path.charAt(0);
                    if (Character.isAlphabetic(s)) {
                        URL url = CoreIOUtils.urlOf(path);
                        return NScoredCallable.of(5, () -> new URLPath(url));
                    }
                }
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }

        @Override
        public int getScore(NScorableContext context) {
            Object c = context.getCriteria();
            if (c instanceof String) {
                String path = (String) c;
                if (path.length() > 0) {
                    char s = path.charAt(0);
                    if (Character.isAlphabetic(s)) {
                        try {
                            URL url = CoreIOUtils.urlOf(path);
                            return 5;
                        } catch (Exception e) {
                            //
                        }
                    }
                }
            }
            return UNSUPPORTED_SCORE;
        }
    }

}
