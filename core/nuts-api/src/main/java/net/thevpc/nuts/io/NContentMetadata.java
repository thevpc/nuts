/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

/**
 * @author thevpc
 */
public interface NContentMetadata extends NBlankable {
    NOptional<Long> getContentLength();

    NOptional<NMsg> getMessage();


    NOptional<String> getCharset();

    NOptional<String> getContentType();

    NOptional<String> getName();

    NOptional<String> getKind();

    NContentMetadata setKind(String userKind);

    NContentMetadata setName(String name);

    NContentMetadata setCharset(String name);

    NContentMetadata setMessage(NMsg message);


    NContentMetadata setContentType(String contentType);

    NContentMetadata setContentLength(Long contentLength);
}
