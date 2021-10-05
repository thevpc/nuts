package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NutsDefaultInputStreamMetadata;
import net.thevpc.nuts.runtime.bundles.io.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.bundles.io.URLBuilder;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

public class URLPath extends NutsPathBase implements NutsPathSPI {
    protected URL url;

    public URLPath(URL url, NutsSession session) {
        this(url, session, false);
    }

    protected URLPath(URL url, NutsSession session, boolean acceptNull) {
        super(session);
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URLPath urlPath = (URLPath) o;
        return Objects.equals(url, urlPath.url);
    }

    @Override
    public String toString() {
        return url == null ? ("broken-url") : url.toString();
    }

    public String getContentEncoding() {
        try {
            return url.openConnection().getContentEncoding();
        } catch (IOException e) {
            return null;
        }
    }

    public String getContentType() {
        try {
            return url.openConnection().getContentType();
        } catch (IOException e) {
            return null;
        }
    }

    public String getName() {
        return url == null ? "" : CoreIOUtils.getURLName(url);
    }

    @Override
    public String asString() {
        return url == null ? "" : url.toString();
    }

    @Override
    public String getLocation() {
        return url == null ? null : url.getFile();
    }

    @Override
    public NutsPath resolve(String other) {
        String[] others = Arrays.stream(NutsUtilStrings.trim(other).split("[/\\\\]"))
                .filter(x -> x.length() > 0).toArray(String[]::new);
        if (others.length > 0) {
            StringBuilder file2 = new StringBuilder(url.getFile());
            for (String s : others) {
                if (file2.length() == 0 || file2.charAt(file2.length() - 1) != '/') {
                    file2.append("/");
                }
                file2.append(s);
            }
            return rebuildURLPath(rebuildURLString(url.getProtocol(), url.getAuthority(), file2.toString(), url.getRef()));
        }
        return this;
    }

    @Override
    public String getProtocol() {
        return url == null ? null : url.getProtocol();
    }

    @Override
    public NutsPath toCompressedForm() {
        return new NutsCompressedPath(this);
    }

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

//    @Override
//    public NutsInput input() {
//        return new URLPathInput();
//    }

    @Override
    public NutsPath[] getChildren() {
        try {
            Path f = toFile();
            return getSession().io().path(f).getChildren();
        } catch (Exception e) {
            //
        }
        return new NutsPath[0];
    }

    public InputStream getInputStream() {
        try {
            if (url == null) {
                throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve input stream %s", toString()));
            }
            return InputStreamMetadataAwareImpl.of(url.openStream()
                    , new NutsDefaultInputStreamMetadata(this))
                    ;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public OutputStream getOutputStream() {
        try {
            if (url == null) {
                throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve output stream %s", toString()));
            }
            return url.openConnection().getOutputStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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

    @Override
    public void delete(boolean recurse) {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to delete %s", toString()));
    }

    @Override
    public void mkdir(boolean parents) {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to mkdir %s", toString()));
    }

    @Override
    public boolean isDirectory() {
        if (url.toString().endsWith("/")) {
            return exists();
        }
        try {
            Path f = toFile();
            return Files.isDirectory(f);
        } catch (Exception e) {
            //
        }
        return false;
    }

    @Override
    public boolean isRegularFile() {
        if (!url.toString().endsWith("/")) {
            return exists();
        }
        try {
            Path f = toFile();
            return Files.isRegularFile(f);
        } catch (Exception e) {
            //
        }
        return false;
    }

    @Override
    public boolean exists() {
        if (url == null) {
            return false;
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
        try {
            return url.openConnection().getContentLengthLong();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public Instant getLastModifiedInstant() {
        if (url == null) {
            return null;
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
    public NutsPathBuilder builder() {
        return new DefaultPathBuilder(getSession(), this);
    }

    @Override
    public NutsPath getParent() {
        if (url == null) {
            return null;
        }
        try {
            File f = CoreIOUtils.toFile(toURL());
            if (f != null) {
                return getSession().io().path(f.toPath().getParent());
            }
            String ppath = CoreIOUtils.getURLParentPath(url.getPath());
            if(ppath==null){
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
            return getSession().io().path(url);
        } catch (IOException e) {
            return null;
        }
    }

    protected NutsPath rebuildURLPath(String other) {
        try {
            return new URLPath(new URL(other), getSession());
        } catch (MalformedURLException e) {
            throw new NutsIOException(getSession(), e);
        }
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

    @Override
    public NutsFormat formatter() {
        return new MyPathFormat(this)
                .setSession(getSession())
                ;
    }

    @Override
    public NutsFormatSPI getFormatterSPI() {
        return new NutsFormatSPIFromNutsFormat(formatter());
    }

    private static class MyPathFormat extends DefaultFormatBase<NutsFormat> {
        private final URLPath p;

        public MyPathFormat(URLPath p) {
            super(p.getSession().getWorkspace(), "path");
            this.p = p;
        }

        public NutsString asFormattedString() {
            if (p.url == null) {
                return getSession().text().ofPlain("");
            }
            return getSession().text().toText(p.url);
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

//    private class URLPathInput extends NutsPathInput {
//        public URLPathInput() {
//            super(URLPath.this);
//        }
//
//        @Override
//        public InputStream open() {
//            return new InputStreamMetadataAwareImpl(inputStream(), new NutsDefaultInputStreamMetadata(getNutsPath().toString(),
//                    getNutsPath().getContentLength()));
//        }
//
//        @Override
//        public URL getURL() {
//            return super.getURL();
//        }
//    }
}
