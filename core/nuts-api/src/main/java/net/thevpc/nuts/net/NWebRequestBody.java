package net.thevpc.nuts.net;

import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

/**
 * NWebRequestBody interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NWebRequestBody {
    /**
     * Body.
     *
     * @return body result
     */
    @NGetter
    NInputSource body();

    /**
     * Content type.
     *
     * @return content type result
     */
    @NGetter
    String contentType();

    /**
     * Encoding.
     *
     * @return encoding result
     */
    @NGetter
    String encoding();

    /**
     * Name.
     *
     * @return name result
     */
    @NGetter
    String name();

    /**
     * File name.
     *
     * @return file name result
     */
    @NGetter
    String fileName();

    /**
     * String value.
     *
     * @return string value result
     */
    @NGetter
    String stringValue();

    /**
     * Content disposition.
     *
     * @return content disposition result
     */
    @NGetter
    String contentDisposition();

    /**
     * String value.
     *
     * @param source source
     * @return string value result
     */
    @NSetter
    NWebRequestBody stringValue(String source);

    /**
     * Body.
     *
     * @param source source
     * @return body result
     */
    @NSetter
    NWebRequestBody body(NInputSource source);

    /**
     * Content type.
     *
     * @param contentType content type
     * @return content type result
     */
    @NSetter
    NWebRequestBody contentType(String contentType);

    /**
     * Encoding.
     *
     * @param encoding encoding
     * @return encoding result
     */
    @NSetter
    NWebRequestBody encoding(String encoding);

    /**
     * Name.
     *
     * @param name name
     * @return name result
     */
    @NSetter
    NWebRequestBody name(String name);

    /**
     * File name.
     *
     * @param fileName file name
     * @return file name result
     */
    @NSetter
    NWebRequestBody fileName(String fileName);

    //return parent NWebRequest
    /**
     * End.
     *
     * @return end result
     */
    NWebRequest end();

}
