/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.io;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 *
 * @author vpc
 */
public class InputStreamMetadataAwareImpl extends FilterInputStream implements InputStreamMetadataAware {

    private InputStreamMetadata metadata;

    public InputStreamMetadataAwareImpl(InputStream in, InputStreamMetadata metadata) {
        super(in);
        this.metadata = metadata;
    }

    @Override
    public InputStreamMetadata getMetaData() {
        return metadata;
    }

}
