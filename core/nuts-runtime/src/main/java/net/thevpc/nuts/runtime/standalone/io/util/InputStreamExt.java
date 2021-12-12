/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.NutsStreamMetadataAware;
import net.thevpc.nuts.NutsStreamMetadata;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author thevpc
 */
public class InputStreamExt extends InputStream implements NutsStreamMetadataAware,Interruptible {

    private InputStream in;
    private Runnable onClose;
    private boolean interrupted;

    public InputStreamExt(InputStream in, Runnable onClose) {
        this.in = in;
        this.onClose = onClose;
    }

    @Override
    public void interrupt() throws InterruptException {
        this.interrupted=true;
    }

    @Override
    public int read() throws IOException {
        if(interrupted){
            throw new IOException(new InterruptException("Interrupted"));
        }
        return in.read();
    }

    @Override
    public void close() throws IOException {
        if(interrupted){
            throw new IOException(new InterruptException("Interrupted"));
        }
        in.close();
        if (onClose != null) {
            onClose.run();
        }
    }

    @Override
    public int available() throws IOException {
        if(interrupted){
            throw new IOException(new InterruptException("Interrupted"));
        }
        return in.available();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if(interrupted){
            throw new IOException(new InterruptException("Interrupted"));
        }
        return in.read(b, off, len);
    }

    @Override
    public NutsStreamMetadata getStreamMetadata() {
        return NutsStreamMetadata.of(in);
    }

}
