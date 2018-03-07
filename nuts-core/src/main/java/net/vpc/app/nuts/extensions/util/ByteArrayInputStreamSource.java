/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.extensions.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 *
 * @author vpc
 */
class ByteArrayInputStreamSource implements InputStreamSource {
    
    private final byte[] bytes;
    private final String name;
    private final Object source;

    public ByteArrayInputStreamSource(byte[] bytes, String name, Object source) {
        this.bytes = bytes;
        this.name = name;
        this.source = source;
    }

    @Override
    public InputStream openStream() {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "ByteArrayInputStreamSource{" + "bytes=" + bytes + ", name=" + name + ", source=" + source + '}';
    }
    
    
}
