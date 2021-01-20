package net.thevpc.nuts.runtime.bundles.http;

import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsURLHeader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;

public class SimpleHttpClient {
    private URL url;
    private long timeout = -1;
    private boolean headerRead = false;

    /**
     * url content length (file size)
     *
     * @return url content length (file size)
     */
    private long contentLength;

    /**
     * url content type (file type)
     *
     * @return url content type (file type)
     */
    private String contentType;

    /**
     * url content encoding
     *
     * @return url content encoding
     */
    private String contentEncoding;

    /**
     * url content last modified
     *
     * @return url content last modified
     */
    private Instant lastModified;
    private int connectTimeout = 5000;
    private int readTimeout = 5000;

    public SimpleHttpClient(URL url) {
        this.url = url;
    }

    public SimpleHttpClient(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    public long getTimeout() {
        return timeout;
    }

    public SimpleHttpClient setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    private void tryReadHeader() {
        if (!headerRead) {
            readHeader();
            headerRead = true;
        }
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public SimpleHttpClient setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public SimpleHttpClient setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    private void readHeader() {
        if (url.getProtocol().equals("file")) {
            File f = CoreIOUtils.toFile(url);
            DefaultNutsURLHeader info = new DefaultNutsURLHeader(url.toString());
            this.contentEncoding = null;
            this.contentType = null;
            this.contentLength = f.length();
            this.lastModified = (Instant.ofEpochMilli(f.lastModified()));
        } else {
            URLConnection conn = null;
            try {
                try {
                    conn = prepareConnection();
                    if (conn instanceof HttpURLConnection) {
                        ((HttpURLConnection) conn).setRequestMethod("HEAD");
                    }
                    conn.getInputStream();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                this.contentType = conn.getContentType();
                this.contentEncoding = conn.getContentEncoding();
                this.contentLength = conn.getContentLengthLong();
                String hf = conn.getHeaderField("last-modified");
                long m = (hf == null) ? 0 : conn.getLastModified();
                this.lastModified = m == 0 ? null : Instant.ofEpochMilli(m);
            } finally {
                if (conn instanceof HttpURLConnection) {
                    ((HttpURLConnection) conn).disconnect();
                }
            }
        }
    }

    public long getContentLength() {
        tryReadHeader();
        return contentLength;
    }

    public String getContentType() {
        tryReadHeader();
        return contentType;
    }

    public String getContentEncoding() {
        tryReadHeader();
        return contentEncoding;
    }

    public Instant getLastModified() {
        tryReadHeader();
        return lastModified;
    }

    public URL getURL() {
        return url;
    }

    public URLConnection prepareConnection() {
        try {
            URLConnection conn = null;
            conn = url.openConnection();
            if (connectTimeout > 0 || readTimeout > 0) {
                if (connectTimeout > 0) {
                    conn.setConnectTimeout(connectTimeout);
                }
                if (readTimeout > 0) {
                    conn.setReadTimeout(readTimeout);
                }
            }
            return conn;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public InputStream openStream() {
        try {
            if (connectTimeout > 0 || readTimeout > 0) {
                URLConnection conn = prepareConnection();
                return conn.getInputStream();
            } else {
                return url.openStream();
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
