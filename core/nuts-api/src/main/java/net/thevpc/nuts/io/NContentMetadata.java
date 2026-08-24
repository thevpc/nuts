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
    /**
     * Content length.
     *
     * @return content length result
     */
    NOptional<Long> contentLength();

    /**
     * Message.
     *
     * @return message result
     */
    NOptional<NMsg> message();


    /**
     * Charset.
     *
     * @return charset result
     */
    NOptional<String> charset();

    /**
     * Content type.
     *
     * @return content type result
     */
    NOptional<String> contentType();

    /**
     * Name.
     *
     * @return name result
     */
    NOptional<String> name();

    /**
     * Kind.
     *
     * @return kind result
     */
    NOptional<String> kind();

    /**
     * Kind.
     *
     * @param userKind user kind
     * @return kind result
     */
    NContentMetadata kind(String userKind);

    /**
     * Name.
     *
     * @param name name
     * @return name result
     */
    NContentMetadata name(String name);

    /**
     * Charset.
     *
     * @param name name
     * @return charset result
     */
    NContentMetadata charset(String name);

    /**
     * Message.
     *
     * @param message message
     * @return message result
     */
    NContentMetadata message(NMsg message);


    /**
     * Content type.
     *
     * @param contentType content type
     * @return content type result
     */
    NContentMetadata contentType(String contentType);

    /**
     * Content length.
     *
     * @param contentLength content length
     * @return content length result
     */
    NContentMetadata contentLength(Long contentLength);
}
