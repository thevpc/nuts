/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

/**
 * @author thevpc
 */
public interface NContentMetadata extends NBlankable {
    NOptional<Long> contentLength();

    NOptional<NMsg> message();


    NOptional<String> charset();

    NOptional<String> contentType();

    NOptional<String> name();

    NOptional<String> kind();

    NContentMetadata kind(String userKind);

    NContentMetadata name(String name);

    NContentMetadata charset(String name);

    NContentMetadata message(NMsg message);


    NContentMetadata contentType(String contentType);

    NContentMetadata contentLength(Long contentLength);
}
