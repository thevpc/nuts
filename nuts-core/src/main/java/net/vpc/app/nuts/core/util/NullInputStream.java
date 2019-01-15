/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import java.io.InputStream;

/**
 * Simple reference implementation of Null (do nothing) Inputstream
 * @author vpc
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
     * @return -1
     */
    @Override
    public int read() {
        return -1;
    }

    /**
     * return 0
     * @return 0
     */
    @Override
    public int available() {
        return 0;
    }
}
