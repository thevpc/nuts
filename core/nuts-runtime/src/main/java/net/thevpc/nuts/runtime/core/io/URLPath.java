package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.FixedInputStreamMetadata;
import net.thevpc.nuts.runtime.bundles.io.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
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
    public Path toFilePath() {
        File f = CoreIOUtils.toFile(toURL());
        if (f != null) {
            return f.toPath();
        }
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve file %s", toString()));
    }

    @Override
    public NutsInput input() {
        return new URLPathInput();
    }

    @Override
    public NutsOutput output() {
        return new NutsPathOutput(null, this, getSession()) {
            @Override
            public OutputStream open() {
                return outputStream();
            }
        };
    }

    @Override
    public void delete(boolean recurse) {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to delete %s", toString()));
    }

    @Override
    public void mkdir(boolean parents) {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to mkdir %s", toString()));
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
    public NutsFormat formatter() {
        return new MyPathFormat(this)
                .setSession(getSession())
                ;
    }

    @Override
    public NutsFormatSPI getFormatterSPI() {
        return new NutsFormatSPIFromNutsFormat(formatter());
    }

    public InputStream inputStream() {
        try {
            if (url == null) {
                throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve input stream %s", toString()));
            }
            return url.openStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public OutputStream outputStream() {
        try {
            if (url == null) {
                throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to resolve output stream %s", toString()));
            }
            return url.openConnection().getOutputStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class MyPathFormat extends DefaultFormatBase<NutsFormat> {
        private URLPath p;

        public MyPathFormat(URLPath p) {
            super(p.getSession().getWorkspace(), "path");
            this.p = p;
        }

        public NutsString asFormattedString() {
            if (p.url == null) {
                return getSession().getWorkspace().text().forPlain("");
            }
            return getSession().getWorkspace().text().toText(p.url);
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

    private class URLPathInput extends NutsPathInput {
        public URLPathInput() {
            super(URLPath.this);
        }

        @Override
        public InputStream open() {
            return new InputStreamMetadataAwareImpl(inputStream(), new FixedInputStreamMetadata(getNutsPath().toString(),
                    getNutsPath().getContentLength()));
        }

        @Override
        public URL getURL() {
            return super.getURL();
        }
    }
}
