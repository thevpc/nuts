package net.thevpc.nuts.runtime.util.fprint;

import java.io.IOException;

public interface RenderedRawStream {
     void writeRaw(byte[] buf, int off, int len);
     void writeLater(byte[] buf) ;
 }
