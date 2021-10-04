/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts;

import java.io.InputStream;

/**
 * @author thevpc
 */
public interface NutsInputStreamMetadata {
    static NutsInputStreamMetadata resolve(InputStream is) {
        if (is instanceof NutsInputStreamMetadataAware) {
            NutsInputStreamMetadata a = ((NutsInputStreamMetadataAware) is).getInputStreamMetadata();
            return a;
        }
        return null;
    }

    static NutsInputStreamMetadata of(InputStream is) {
        NutsInputStreamMetadata a = resolve(is);
        if (a != null) {
            return a;
        }
        return new NutsDefaultInputStreamMetadata();
    }

    long getContentLength();

    NutsString getFormattedName();

    String getContentType();

    String getName();

    String getUserKind();

    NutsInputStreamMetadata setUserKind(String userKind);
}
