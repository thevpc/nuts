/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.io.InputStream;

/**
 *
 * @author vpc
 */
public final class NullInputStream extends InputStream {

    public static final NullInputStream INSTANCE = new NullInputStream();

    private NullInputStream() {
    }

    @Override
    public int read() {
        return -1;
    }

    @Override
    public int available() {
        return 0;
    }
}
