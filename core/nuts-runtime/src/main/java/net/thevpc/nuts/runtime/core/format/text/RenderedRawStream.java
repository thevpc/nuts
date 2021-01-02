package net.thevpc.nuts.runtime.core.format.text;

public interface RenderedRawStream {
     void writeRaw(byte[] buf, int off, int len);
     void writeLater(byte[] buf) ;
 }
