package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.NFormat;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NContentMetadata;
import net.thevpc.nuts.io.NContentMetadataProviderFormatSPI;
import net.thevpc.nuts.io.NIOException;

import java.io.IOException;
import java.io.InputStream;

public class NInputStreamSource extends AbstractSingleReadNInputSource {
    private InputStream inputStream;
    private NContentMetadata md;

    public NInputStreamSource(InputStream inputStream, NContentMetadata md, NSession session) {
        super(session);
        this.inputStream = inputStream;
        this.md = CoreIOUtils.createContentMetadata(md, inputStream);
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public NContentMetadata getMetaData() {
        return md;
    }

    @Override
    public NFormat formatter(NSession session) {
        return NFormat.of(session, new NContentMetadataProviderFormatSPI(this, null, "input-stream"));
    }

    @Override
    public String getName() {
        return getMetaData().getName().orNull();
    }

    @Override
    public String getContentType() {
        return getMetaData().getContentType().orNull();
    }

    @Override
    public String getCharset() {
        return getMetaData().getCharset().orNull();
    }

    @Override
    public void dispose() {
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new NIOException(getSession(), e);
        }
    }

}
