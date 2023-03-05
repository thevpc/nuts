/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.util;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thevpc
 */
public class IOPipe {

    private PipedOutputStream out;
    private PipedInputStream in;

    public static IOPipe createPipe(int size) throws IOException {
        IOPipe p = new IOPipe();
        p.in = new PipedInputStream(size);
        p.out = new PipedOutputStream(p.in);
        return p;
    }

    public PipedOutputStream getOut() {
        return out;
    }

    public PipedInputStream getIn() {
        return in;
    }

    public static void asyncCopy(final InputStream in, final OutputStream out) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    copy(in, out);
                } catch (IOException ex) {
                    Logger.getLogger(IOPipe.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }
}
