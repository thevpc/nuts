/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;

/**
 * @author thevpc
 */
public interface NInputSourceMetadata {
    NOptional<Long> getContentLength();

    NOptional<NMsg> getMessage();


    NOptional<String> getContentType();

    NOptional<String> getName();

    NOptional<String> getKind();

    NInputSourceMetadata setKind(String userKind);

    NInputSourceMetadata setName(String name);

    NInputSourceMetadata setContentType(String contentType);

    NInputSourceMetadata setContentLength(Long contentLength);
}
