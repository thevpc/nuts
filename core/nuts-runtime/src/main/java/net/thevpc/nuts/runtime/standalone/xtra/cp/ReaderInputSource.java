package net.thevpc.nuts.runtime.standalone.xtra.cp;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.io.NContentMetadata;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.io.util.AbstractNInputSource;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.io.ReaderInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

class ReaderInputSource extends AbstractNInputSource {
    private Reader reader;
    private InputStream in;
    private NContentMetadata md;

    public ReaderInputSource(NWorkspace workspace, Reader reader) {
        super();
        this.reader = reader;
        this.in = new ReaderInputStream(reader, null);
    }

    @Override
    public boolean isMultiRead() {
        return false;
    }

    @Override
    public boolean isKnownContentLength() {
        return false;
    }

    @Override
    public long contentLength() {
        return -1;
    }

    @Override
    public InputStream getInputStream() {
        return in;
    }

    @Override
    public NContentMetadata getMetaData() {
        return md;
    }

    @Override
    public Reader getReader() {
        return reader;
    }

    @Override
    public String readString(Charset cs) {
        try (Reader in = reader) {
            return new String(NIOUtils.readChars(in));
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }
}
