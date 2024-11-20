package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.NSession;

import java.io.IOException;
import java.io.InputStream;
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
    private NSession session;

    public SimpleHttpClient(URL url, NSession session) {
        this.url = url;
        this.session = session;
    }

    public SimpleHttpClient(String url, NSession session) {
        try {
            this.url = new URL(url);
            this.session=session;
        } catch (MalformedURLException e) {
            throw new NIOException(e);
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
        NPath info = NPath.of(url);
        this.contentEncoding = info.getContentEncoding();
        this.contentType = info.getContentType();
        this.contentLength = info.getContentLength();
        this.lastModified = info.getLastModifiedInstant();
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

    public Instant getLastModifiedInstant() {
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
            throw new NIOException(e);
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
            throw new NIOException(ex);
        }
    }
}
