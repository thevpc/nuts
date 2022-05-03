/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;

/**
 * @author thevpc
 */
public interface NutsInputSourceMetadata {
    NutsOptional<Long> getContentLength();

    NutsOptional<NutsMessage> getMessage();


    NutsOptional<String> getContentType();

    NutsOptional<String> getName();

    NutsOptional<String> getKind();

    NutsInputSourceMetadata setKind(String userKind);

    NutsInputSourceMetadata setName(String name);

    NutsInputSourceMetadata setContentType(String contentType);

    NutsInputSourceMetadata setContentLength(Long contentLength);
}
