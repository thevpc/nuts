/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author vpc
 */
public class InputStreamTee extends InputStream implements InputStreamMetadataAware {

    private InputStream in;
    private OutputStream out;
    private Runnable onClose;

    public InputStreamTee(InputStream in, OutputStream out, Runnable onClose) {
        this.in = in;
        this.out = out;
        this.onClose = onClose;
    }

    @Override
    public int read() throws IOException {
        int x = in.read();
        if (x >= 0) {
            out.write(x);
        }
        return x;
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        if (onClose != null) {
            onClose.run();
        }
    }

    @Override
    public int available() throws IOException {
        return in.available(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        final int p = in.read(b, off, len);
        if (p > 0) {
            out.write(b, off, p);
        }
        return p;
    }

    @Override
    public InputStreamMetadata getMetaData() {
        if (in instanceof InputStreamMetadataAware) {
            return ((InputStreamMetadataAware) in).getMetaData();
        }
        return null;
    }

}
