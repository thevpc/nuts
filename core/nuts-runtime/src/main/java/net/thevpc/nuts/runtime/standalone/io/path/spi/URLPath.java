package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.SimpleHttpClient;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.standalone.io.util.URLBuilder;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class URLPath implements NutsPathSPI {
    public static final Pattern MOSTLY_URL_PATTERN = Pattern.compile("([a-zA-Z][a-zA-Z0-9_-]+):.*");

    private final NutsSession session;
    protected URL url;

    public URLPath(URL url, NutsSession session) {
        this(url, session, false);
    }

    protected URLPath(URL url, NutsSession session, boolean acceptNull) {
        this.session = session;
        if (url == null) {
            if (!acceptNull) {
                throw new IllegalArgumentException("invalid url");
            }
        }
        this.url = url;
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
    public NutsStream<NutsPath> list(NutsPath basePath) {
        NutsPath f = asFilePath(basePath);
        if (f != null) {
            return f.list();
        }
        //should we implement other protocols ?
        return NutsStream.ofEmpty(getSession());
    }

    @Override
    public NutsFormatSPI formatter(NutsPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getProtocol(NutsPath basePath) {
        return url == null ? null : url.getProtocol();
    }

    @Override
    public NutsPath resolve(NutsPath basePath, String path) {
        String u=url.getFile();
        if(!u.endsWith("/") && !path.startsWith("/")){
            u+="/";
        }
        u+=path;
        return rebuildURLPath(rebuildURLString(url.getProtocol(), url.getAuthority(),u, url.getRef()));
    }
    private String _parent(String p){
        while (p.endsWith("/") || p.endsWith("\\")){
            p=p.substring(0,p.length()-1);
        }
        if(p.isEmpty()){
            return null;
        }
        int x=p.lastIndexOf('/');
        int y=p.lastIndexOf('\\');
        if(x<0){
            x=y;
        }else if(y>=0){
            if(y>x){
                x=y;
            }
        }
        if(x<0){
            return "";
        }
        return p.substring(0,x);
    }

    @Override
    public NutsPath resolve(NutsPath basePath, NutsPath path) {
        String spath=path.toString().replace("\\","/");
        String u=url.getFile();
        if(!u.endsWith("/") && !spath.startsWith("/")){
            u+="/";
        }
        u+=spath;
        return rebuildURLPath(rebuildURLString(url.getProtocol(), url.getAuthority(),u, url.getRef()));
    }


    @Override
    public NutsPath resolveSibling(NutsPath basePath, String path) {
        String u = _parent(url.getFile());
        String spath=path.replace("\\","/");
        if(u==null || u.isEmpty()){
            u=spath;
        }else {
            if(!u.endsWith("/") && !spath.startsWith("/")){
                u+="/";
            }
            u+=spath;
        }
        return rebuildURLPath(rebuildURLString(url.getProtocol(), url.getAuthority(),u, url.getRef()));
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, NutsPath path) {
        return resolveSibling(basePath,path.toString());
    }


    @Override
    public NutsPath subpath(NutsPath basePath, int beginIndex, int endIndex) {
        return rebuildURLPath(
                        NutsPath.of(getLocation(basePath),getSession()).subpath(beginIndex,endIndex).toString()
                );
    }

    @Override
    public String[] getItems(NutsPath basePath) {
        return NutsPath.of(getLocation(basePath),getSession()).getItems();
    }


//    @Override
//    public NutsPath resolve(String other) {
//        String[] others = Arrays.stream(NutsUtilStrings.trim(other).split("[/\\\\]"))
//                .filter(x -> x.length() > 0).toArray(String[]::new);
//        if (others.length > 0) {
//            StringBuilder file2 = new StringBuilder(url.getFile());
//            for (String s : others) {
//                if (file2.length() == 0 || file2.charAt(file2.length() - 1) != '/') {
//                    file2.append("/");
//                }
//                file2.append(s);
//            }
//            return rebuildURLPath(rebuildURLString(url.getProtocol(), url.getAuthority(), file2.toString(), url.getRef()));
//        }
//        return null;
//    }
    @Override
    public URL toURL(NutsPath basePath) {
        if (url == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve url %s", toString()));
        }
        return url;
    }

    @Override
    public Path toFile(NutsPath basePath) {
        File f = CoreIOUtils.toFile(toURL(basePath));
        if (f != null) {
            return f.toPath();
        }
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve file %s", toString()));
    }

    public boolean isSymbolicLink(NutsPath basePath) {
        NutsPath f = asFilePath(basePath);
        return f != null && f.isSymbolicLink();
    }

    @Override
    public boolean isOther(NutsPath basePath) {
        NutsPath f = asFilePath(basePath);
        return f != null && f.isOther();
    }

//    @Override
//    public NutsInput input() {
//        return new URLPathInput();
//    }
    @Override
    public boolean isDirectory(NutsPath basePath) {
        if (url.toString().endsWith("/")) {
            return exists(basePath);
        }
        NutsPath f = asFilePath(basePath);
        if (f != null) {
            return f.isDirectory();
        }
        return false;
    }

    @Override
    public boolean isRegularFile(NutsPath basePath) {
        NutsPath f = asFilePath(basePath);
        if (f != null) {
            return f.isRegularFile();
        }
        if (!url.toString().endsWith("/")) {
            return exists(basePath);
        }
        return false;
    }

    @Override
    public boolean exists(NutsPath basePath) {
        if (url == null) {
            return false;
        }
        NutsPath f = asFilePath(basePath);
        if (f != null) {
            return f.exists();
        }
        try {
            url.openConnection().getContentLengthLong();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public long getContentLength(NutsPath basePath) {
        if (url == null) {
            return -1;
        }
        NutsPath f = asFilePath(basePath);
        if (f != null) {
            return f.getContentLength();
        }
        try {
            return url.openConnection().getContentLengthLong();
        } catch (IOException e) {
            return -1;
        }
    }

    public String getContentEncoding(NutsPath basePath) {
        try {
            return url.openConnection().getContentEncoding();
        } catch (IOException e) {
            return null;
        }
    }

//    @Override
//    public NutsOutput output() {
//        return new NutsPathOutput(null, this, getSession()) {
//            @Override
//            public OutputStream open() {
//                return getOutputStream();
//            }
//        };
//    }
    public String getContentType(NutsPath basePath) {
        if (url == null) {
            return null;
        }
        NutsPath f = asFilePath(basePath);
        if (f != null) {
            return f.getContentType();
        }
        try {
            String c = url.openConnection().getContentType();
            if(c!=null) {
                return c;
            }
        } catch (IOException e) {
            //
        }
        return NutsContentTypes.of(session).probeContentType(basePath);
    }

    @Override
    public String getLocation(NutsPath basePath) {
        return url == null ? null : url.getFile();
    }

    public InputStream getInputStream(NutsPath basePath) {
        if (url == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve input stream %s", toString()));
        }
        return InputStreamMetadataAwareImpl.of(
                new SimpleHttpClient(url, session).openStream(),
                new NutsPathInputStreamMetadata(basePath));
    }

    public OutputStream getOutputStream(NutsPath basePath) {
        try {
            if (url == null) {
                throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve output stream %s", toString()));
            }
            return url.openConnection().getOutputStream();
        } catch (IOException e) {
            throw new NutsIOException(session, e);
        }
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public void delete(NutsPath basePath, boolean recurse) {
        if (url != null) {
            NutsPath f = asFilePath(basePath);
            if (f != null) {
                f.delete(recurse);
                return;
            }
        }
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to delete %s", toString()));
    }

    @Override
    public void mkdir(boolean parents, NutsPath basePath) {
        if (url != null) {
            NutsPath f = asFilePath(basePath);
            if (f != null) {
                f.mkdir(parents);
                return;
            }
        }
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to mkdir %s", toString()));
    }

    @Override
    public Instant getLastModifiedInstant(NutsPath basePath) {
        if (url == null) {
            return null;
        }
        NutsPath f = asFilePath(basePath);
        if (f != null) {
            return f.getLastModifiedInstant();
        }
        try {
            long z = url.openConnection().getLastModified();
            if (z == -1) {
                return null;
            }
            return Instant.ofEpochMilli(z);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Instant getLastAccessInstant(NutsPath basePath) {
        NutsPath f = asFilePath(basePath);
        return (f != null) ? f.getLastAccessInstant() : null;
    }

    @Override
    public Instant getCreationInstant(NutsPath basePath) {
        NutsPath f = asFilePath(basePath);
        return (f != null) ? f.getCreationInstant() : null;
    }

    @Override
    public NutsPath getParent(NutsPath basePath) {
        if (url == null) {
            return null;
        }
        NutsPath f = asFilePath(basePath);
        if (f != null) {
            return f.getParent();
        }
        try {
            String ppath = CoreIOUtils.getURLParentPath(url.getPath());
            if (ppath == null) {
                return null;
            }
            URL url = new URL(
                    URLBuilder.buildURLString(
                            this.url.getProtocol(),
                            this.url.getAuthority(),
                            ppath,
                            this.url.getQuery(),
                            this.url.getRef()
                    )
            );
            return NutsPath.of(url, getSession());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public NutsPath toAbsolute(NutsPath basePath, NutsPath rootPath) {
        return basePath;
    }

    @Override
    public NutsPath normalize(NutsPath basePath) {
        NutsPath f = asFilePath(basePath);
        if (f != null) {
            return f.normalize();
        }
        return basePath;
    }

    @Override
    public boolean isAbsolute(NutsPath basePath) {
        return true;
    }

    @Override
    public String owner(NutsPath basePath) {
        NutsPath f = asFilePath(basePath);
        return (f != null) ? f.owner() : null;
    }

    @Override
    public String group(NutsPath basePath) {
        NutsPath f = asFilePath(basePath);
        return (f != null) ? f.group() : null;
    }

    @Override
    public Set<NutsPathPermission> getPermissions(NutsPath basePath) {
        NutsPath f = asFilePath(basePath);
        return (f != null) ? f.getPermissions() : Collections.emptySet();
    }

    @Override
    public void setPermissions(NutsPath basePath, NutsPathPermission... permissions) {
        NutsPath f = asFilePath(basePath);
        if (f != null) {
            f.setPermissions(permissions);
        }
    }

    @Override
    public void addPermissions(NutsPath basePath, NutsPathPermission... permissions) {
        NutsPath f = asFilePath(basePath);
        if (f != null) {
            f.addPermissions(permissions);
        }
    }

    @Override
    public void removePermissions(NutsPath basePath, NutsPathPermission... permissions) {
        NutsPath f = asFilePath(basePath);
        if (f != null) {
            f.removePermissions(permissions);
        }
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
    public NutsStream<NutsPath> walk(NutsPath basePath, int maxDepth, NutsPathOption[] options) {
        NutsPath f = asFilePath(basePath);
        if (f != null) {
            return f.walk(maxDepth, options);
        }
        //should we implement other protocols ?
        return NutsStream.ofEmpty(getSession());
    }

    protected NutsPath rebuildURLPath(String other) {
        return NutsPath.of(other, getSession());
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

    public NutsPath asFilePath(NutsPath basePath) {
        File f = CoreIOUtils.toFile(toURL(basePath));
        return (f != null) ? NutsPath.of(f, getSession()) : null;
    }

    private static class MyPathFormat implements NutsFormatSPI {

        private final URLPath p;

        public MyPathFormat(URLPath p) {
            this.p = p;
        }

        public NutsString asFormattedString() {
            if (p.url == null) {
                return NutsTexts.of(p.getSession()).ofPlain("");
            }
            return NutsTexts.of(p.getSession()).toText(p.url);
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
    public NutsPath toCompressedForm(NutsPath basePath) {
        return null;
    }
    
    @Override
    public String getName(NutsPath basePath) {
        String loc = getLocation(basePath);
        return loc == null ? "" : Paths.get(loc).getFileName().toString();
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

    public static class URLPathFactory implements NutsPathFactory {
        NutsWorkspace ws;

        public URLPathFactory(NutsWorkspace ws) {
            this.ws = ws;
        }

        @Override
        public NutsSupported<NutsPathSPI> createPath(String path, NutsSession session, ClassLoader classLoader) {
            NutsWorkspaceUtils.checkSession(ws, session);
            try {
                URL url = new URL(path);
                return NutsSupported.of(5,()->new URLPath(url, session));
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }
    }

    @Override
    public boolean isLocal(NutsPath basePath) {
        NutsPath f = asFilePath(basePath);
        return f !=null && f.isLocal();
    }

}
