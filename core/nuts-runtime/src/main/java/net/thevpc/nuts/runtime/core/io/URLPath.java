package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.bundles.io.URLBuilder;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
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

public class URLPath implements NutsPathSPI {

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
    public NutsStream<NutsPath> list() {
        NutsPath f = asFilePath();
        if (f != null) {
            return f.list();
        }
        //should we implement other protocols ?
        return NutsStream.ofEmpty(getSession());
    }

    @Override
    public NutsFormatSPI getFormatterSPI() {
        return new MyPathFormat(this);
    }

    @Override
    public String getProtocol() {
        return url == null ? null : url.getProtocol();
    }

    @Override
    public NutsPath resolve(String[] others, boolean trailingSeparator) {
        if (others.length > 0) {
            StringBuilder loc=new StringBuilder(url.getFile());
            if (loc.length() == 0 || loc.charAt(loc.length() - 1) != '/') {
                loc.append('/');
            }
            loc.append(String.join("/", others));
            if (trailingSeparator) {
                loc.append('/');
            }
            return rebuildURLPath(rebuildURLString(url.getProtocol(), url.getAuthority(), loc.toString(), url.getRef()));
        }
        return null;
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
    public URL toURL() {
        if (url == null) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve url %s", toString()));
        }
        return url;
    }

    @Override
    public Path toFile() {
        File f = CoreIOUtils.toFile(toURL());
        if (f != null) {
            return f.toPath();
        }
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve file %s", toString()));
    }

    public boolean isSymbolicLink() {
        NutsPath f = asFilePath();
        return f != null && f.isSymbolicLink();
    }

    @Override
    public boolean isOther() {
        NutsPath f = asFilePath();
        return f != null && f.isOther();
    }

//    @Override
//    public NutsInput input() {
//        return new URLPathInput();
//    }
    @Override
    public boolean isDirectory() {
        if (url.toString().endsWith("/")) {
            return exists();
        }
        NutsPath f = asFilePath();
        if (f != null) {
            return f.isDirectory();
        }
        return false;
    }

    @Override
    public boolean isRegularFile() {
        NutsPath f = asFilePath();
        if (f != null) {
            return f.isRegularFile();
        }
        if (!url.toString().endsWith("/")) {
            return exists();
        }
        return false;
    }

    @Override
    public boolean exists() {
        if (url == null) {
            return false;
        }
        NutsPath f = asFilePath();
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
    public long getContentLength() {
        if (url == null) {
            return -1;
        }
        NutsPath f = asFilePath();
        if (f != null) {
            return f.getContentLength();
        }
        try {
            return url.openConnection().getContentLengthLong();
        } catch (IOException e) {
            return -1;
        }
    }

    public String getContentEncoding() {
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
    public String getContentType() {
        if (url == null) {
            return null;
        }
        NutsPath f = asFilePath();
        if (f != null) {
            return f.getContentType();
        }
        try {
            return url.openConnection().getContentType();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getLocation() {
        return url == null ? null : url.getFile();
    }

    public InputStream getInputStream() {
        try {
            if (url == null) {
                throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve input stream %s", toString()));
            }
            return InputStreamMetadataAwareImpl.of(url.openStream(),
                    new NutsDefaultInputStreamMetadata(wrapperNutsPath()));
        } catch (IOException e) {
            throw new NutsIOException(session, e);
        }
    }

    public OutputStream getOutputStream() {
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
    public void delete(boolean recurse) {
        if (url != null) {
            NutsPath f = asFilePath();
            if (f != null) {
                f.delete(recurse);
                return;
            }
        }
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to delete %s", toString()));
    }

    @Override
    public void mkdir(boolean parents) {
        if (url != null) {
            NutsPath f = asFilePath();
            if (f != null) {
                f.mkdir(parents);
                return;
            }
        }
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to mkdir %s", toString()));
    }

    @Override
    public Instant getLastModifiedInstant() {
        if (url == null) {
            return null;
        }
        NutsPath f = asFilePath();
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
    public Instant getLastAccessInstant() {
        NutsPath f = asFilePath();
        return (f != null) ? f.getLastAccessInstant() : null;
    }

    @Override
    public Instant getCreationInstant() {
        NutsPath f = asFilePath();
        return (f != null) ? f.getCreationInstant() : null;
    }

    @Override
    public NutsPath getParent() {
        if (url == null) {
            return null;
        }
        NutsPath f = asFilePath();
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
    public NutsPath toAbsolute(NutsPath basePath) {
        return new NutsPathFromSPI(this);
    }

    @Override
    public NutsPath normalize() {
        NutsPath f = asFilePath();
        if (f != null) {
            return f.normalize();
        }
        return new NutsPathFromSPI(this);
    }

    @Override
    public boolean isAbsolute() {
        return true;
    }

    @Override
    public String owner() {
        NutsPath f = asFilePath();
        return (f != null) ? f.owner() : null;
    }

    @Override
    public String group() {
        NutsPath f = asFilePath();
        return (f != null) ? f.group() : null;
    }

    @Override
    public Set<NutsPathPermission> permissions() {
        NutsPath f = asFilePath();
        return (f != null) ? f.getPermissions() : Collections.emptySet();
    }

    @Override
    public void setPermissions(NutsPathPermission... permissions) {
        NutsPath f = asFilePath();
        if (f != null) {
            f.setPermissions(permissions);
        }
    }

    @Override
    public void addPermissions(NutsPathPermission... permissions) {
        NutsPath f = asFilePath();
        if (f != null) {
            f.addPermissions(permissions);
        }
    }

    @Override
    public void removePermissions(NutsPathPermission... permissions) {
        NutsPath f = asFilePath();
        if (f != null) {
            f.removePermissions(permissions);
        }
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
    public NutsStream<NutsPath> walk(int maxDepth, NutsPathVisitOption[] options) {
        NutsPath f = asFilePath();
        if (f != null) {
            return f.walk(maxDepth, options);
        }
        //should we implement other protocols ?
        return NutsStream.ofEmpty(getSession());
    }

    private NutsPath wrapperNutsPath() {
        return NutsPath.of(url, getSession());
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

    public NutsPath asFilePath() {
        File f = CoreIOUtils.toFile(toURL());
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
    public NutsPath toCompressedForm() {
        return null;
    }
    
    @Override
    public String getName() {
        String loc = getLocation();
        return loc == null ? "" : Paths.get(loc).getFileName().toString();
    }
    
}
