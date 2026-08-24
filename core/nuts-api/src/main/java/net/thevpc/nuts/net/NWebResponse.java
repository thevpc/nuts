package net.thevpc.nuts.net;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgCode;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;

/**
 * NWebResponse interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NWebResponse {
    /**
     * Status code.
     *
     * @return status code result
     */
    NHttpCode statusCode();

    /**
     * Int status code.
     *
     * @return int status code result
     */
    int intStatusCode();

    /**
     * Status message.
     *
     * @return status message result
     */
    NMsg statusMessage();

    /**
     * Headers.
     *
     * @param name name
     * @return headers result
     */
    List<String> headers(String name);

    /**
     * Header.
     *
     * @param name name
     * @return header result
     */
    NOptional<String> header(String name);

    /**
     * Headers.
     *
     * @return headers result
     */
    Map<String, List<String>> headers();

    /**
     * First headers.
     *
     * @return first headers result
     */
    Map<String, String> firstHeaders();

    /**
     * Content.
     *
     * @return content result
     */
    NInputSource content();

    /**
     * Content map as json.
     *
     * @return content map as json result
     */
    <K, V> Map<K, V> contentMapAsJson();

    /**
     * Content list as json.
     *
     * @return content list as json result
     */
    <K> List<K> contentListAsJson();

    /**
     * Content array as json.
     *
     * @return content array as json result
     */
    <T> List<T> contentArrayAsJson();

    /**
     * Content as json.
     *
     * @param clz clz
     * @return content as json result
     */
    <T> T contentAsJson(Class<T> clz);

    /**
     * Content as json.
     *
     * @return content as json result
     */
    NElement contentAsJson();

    /**
     * Content as.
     *
     * @param clz clz
     * @param type type
     * @return content as result
     */
    <T> T contentAs(Class<T> clz, NContentType type);

    /**
     * Content as json map.
     *
     * @return content as json map result
     */
    Map<?, ?> contentAsJsonMap();

    /**
     * Content as json list.
     *
     * @return content as json list result
     */
    List<?> contentAsJsonList();

    /**
     * Content as string.
     *
     * @return content as string result
     */
    String contentAsString();

    /**
     * Content as bytes.
     *
     * @return content as bytes result
     */
    byte[] contentAsBytes();

    /**
     * Cookies.
     *
     * @return cookies result
     */
    List<NWebCookie> cookies();

    /**
     * Checks if is error.
     *
     * @return is error result
     */
    boolean isError();

    /**
     * Checks if is ok.
     *
     * @return is ok result
     */
    boolean isOk();

    /**
     * If error throw.
     *
     * @return if error throw result
     */
    NWebResponse ifErrorThrow();

    /**
     * Checks if is client error.
     *
     * @return is client error result
     */
    boolean isClientError();

    /**
     * Checks if is server error.
     *
     * @return is server error result
     */
    boolean isServerError();

    /**
     * Checks if is redirect.
     *
     * @return is redirect result
     */
    boolean isRedirect();

    /**
     * Content type.
     *
     * @return content type result
     */
    String contentType();

    /**
     * User message.
     *
     * @return user message result
     */
    NMsgCode userMessage();

    /**
     * User message.
     *
     * @param msgCode msg code
     * @return user message result
     */
    NWebResponse userMessage(NMsgCode msgCode);
}
