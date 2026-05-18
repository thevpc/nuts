package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.io.NContentMetadata;
import net.thevpc.nuts.io.NIOException;

import java.io.IOException;
import java.io.InputStream;

public class NInputStreamSource extends AbstractSingleReadNInputSource {
    private InputStream inputStream;
    private NContentMetadata md;

    public NInputStreamSource(InputStream inputStream, NContentMetadata md) {
        super();
        this.inputStream = inputStream;
        this.md = CoreIOUtils.createContentMetadata(md, inputStream);
    }

    @Override
    public long contentLength() {
        return this.md.contentLength().orElse(-1L);
    }

    @Override
    public InputStream inputStream() {
        return inputStream;
    }

    @Override
    public NContentMetadata metaData() {
        return md;
    }

    @Override
    public String name() {
        return metaData().name().orNull();
    }

    @Override
    public String contentType() {
        return metaData().contentType().orNull();
    }

    @Override
    public String charset() {
        return metaData().charset().orNull();
    }

    @Override
    public void dispose() {
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

}
