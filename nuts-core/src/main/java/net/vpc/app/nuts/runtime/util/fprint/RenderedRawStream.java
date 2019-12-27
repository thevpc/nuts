package net.vpc.app.nuts.runtime.util.fprint;

import java.io.IOException;

public interface RenderedRawStream {
     void writeRaw(byte[] buf, int off, int len) throws IOException;
     void writeLater(byte[] buf)throws IOException ;
 }
