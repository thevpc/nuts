/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode.sources;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author vpc
 */
public class NonClosableInputStream extends InputStream {

    private InputStream in;

    public NonClosableInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int read(byte[] b) throws IOException {
        return in.read(b); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean markSupported() {
        return in.markSupported(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int available() throws IOException {
        return in.available(); //To change body of generated methods, choose Tools | Templates.
    }

}
