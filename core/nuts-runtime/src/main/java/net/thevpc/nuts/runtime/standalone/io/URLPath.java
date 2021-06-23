package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

public class URLPath extends NutsPathBase {
    private URL url;

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

    public long length() {
        if (url == null) {
            return -1;
        }
        try {
            return url.openConnection().getContentLengthLong();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
        return url==null?("broken-url"):url.toString();
    }

    public String name() {
        return url==null?"":CoreIOUtils.getURLName(url);
    }

    @Override
    public String location() {
        return url==null?"":url.toString();
    }

    @Override
    public NutsPath compressedForm() {
        return new NutsCompressedPath(this);
    }

    @Override
    public URL toURL() {
        if(url==null){
            throw new IllegalArgumentException("unable to resolve url : "+toString());
        }
        return url;
    }

    @Override
    public Path toFilePath() {
        throw new IllegalArgumentException("unable to resolve file path : " + toString());
    }

    public InputStream inputStream() {
        try {
            if(url==null){
                throw new IOException("unable to resolve input stream : "+toString());
            }
            return url.openStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public OutputStream outputStream() {
        try {
            if(url==null){
                throw new IOException("unable to resolve output stream : "+toString());
            }
            return url.openConnection().getOutputStream();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
