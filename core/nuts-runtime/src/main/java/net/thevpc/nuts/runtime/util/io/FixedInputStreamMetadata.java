/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.util.io;

/**
 *
 * @author vpc
 */
public class FixedInputStreamMetadata implements InputStreamMetadata {

    private long length;
    private String name;

    public FixedInputStreamMetadata(String name, long length) {
        this.length = length;
        this.name = name;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public String getName() {
        return name;
    }

}
