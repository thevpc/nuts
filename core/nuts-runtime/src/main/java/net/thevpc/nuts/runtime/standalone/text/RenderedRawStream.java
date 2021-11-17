package net.thevpc.nuts.runtime.standalone.text;

public interface RenderedRawStream {
     Object baseOutput();
     void writeRaw(byte[] buf, int off, int len);
     void writeLater(byte[] buf) ;
 }
