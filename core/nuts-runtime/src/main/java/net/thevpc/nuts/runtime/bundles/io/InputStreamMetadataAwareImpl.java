/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.bundles.io;

import net.thevpc.nuts.NutsInputStreamMetadataAware;
import net.thevpc.nuts.NutsInputStreamMetadata;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 *
 * @author thevpc
 */
public class InputStreamMetadataAwareImpl extends FilterInputStream implements NutsInputStreamMetadataAware {

    private NutsInputStreamMetadata metadata;


    public static InputStreamMetadataAwareImpl of(InputStream in, NutsInputStreamMetadata metadata) {
        if(in instanceof InputStreamMetadataAwareImpl){
            return new InputStreamMetadataAwareImpl(((InputStreamMetadataAwareImpl) in).in,metadata);
        }else{
            return new InputStreamMetadataAwareImpl(in,metadata);
        }
    }

    public InputStreamMetadataAwareImpl(InputStream in, NutsInputStreamMetadata metadata) {
        super(in);
        this.metadata = metadata;
    }

    @Override
    public NutsInputStreamMetadata getInputStreamMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        NutsInputStreamMetadata md = getInputStreamMetadata();
        if(md!=null) {
            String n = md.getName();
            if (n != null) {
                return n;
            }
        }
        return super.toString();
    }
}
