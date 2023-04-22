/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Simple reference implementation of Null (do nothing) Inputstream
 *
 * @author thevpc
 */
public final class NullInputStream extends InputStream {

    /**
     * singleton reference
     */
    public static final NullInputStream INSTANCE = new NullInputStream();

    /**
     * private empty constructor
     */
    private NullInputStream() {
    }

    /**
     * return -1
     *
     * @return -1
     */
    @Override
    public int read() {
        return -1;
    }

    /**
     * return 0
     *
     * @return 0
     */
    @Override
    public int available() {
        return 0;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return 0;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return 0;
    }

    @Override
    public long skip(long n) throws IOException {
        return 0;
    }

}
