package net.thevpc.nuts.runtime.core.format.text;

import java.io.OutputStream;

public interface RenderedRawStream {
     OutputStream baseOutput();
     void writeRaw(byte[] buf, int off, int len);
     void writeLater(byte[] buf) ;
 }
