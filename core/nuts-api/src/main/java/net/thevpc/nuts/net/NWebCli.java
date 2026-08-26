package net.thevpc.nuts.net;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * NWebCli interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NWebCli extends NComponent {

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NWebCli of() {
        return NExtensions.of(NWebCli.class);
    }

    /**
     * Response post processor.
     *
     * @return response post processor result
     */
    Function<NWebResponse, NWebResponse> responsePostProcessor();

    /**
     * Response post processor.
     *
     * @param responsePostProcessor response post processor
     * @return response post processor result
     */
    NWebCli responsePostProcessor(Function<NWebResponse, NWebResponse> responsePostProcessor);

    /**
     * Base uri.
     *
     * @return base uri result
     */
    @NGetter
    String baseUri();

    /**
     * Base uri.
     *
     * @param prefix prefix
     * @return base uri result
     */
    NWebCli baseUri(String prefix);

    /**
     * Req.
     *
     * @param method method
     * @return req result
     */
    NWebRequest req(NHttpMethod method);

    /**
     * Cookies.
     *
     * @return cookies result
     */
    @NGetter
    List<NWebCookie> cookies();

    /**
     * Executor.
     *
     * @return executor result
     */
    @NGetter
    Executor executor();

    /**
     * Executor.
     *
     * @param executor executor
     * @return executor result
     */
    @NSetter
    NWebCli executor(Executor executor);

    /**
     * Header.
     *
     * @param name name
     * @param value value
     * @return header result
     */
    NWebCli header(String name, String value);

    /**
     * Adds the specified header.
     *
     * @param name name
     * @param value value
     * @return add header result
     */
    NWebCli addHeader(String name, String value);

    /**
     * Removes the specified header.
     *
     * @param name name
     * @param value value
     * @return remove header result
     */
    NWebCli removeHeader(String name, String value);

    /**
     * Removes the specified header.
     *
     * @param name name
     * @return remove header result
     */
    NWebCli removeHeader(String name);

    /**
     * Contains header.
     *
     * @param name name
     * @return contains header result
     */
    boolean containsHeader(String name);

    /**
     * Headers.
     *
     * @return headers result
     */
    @NGetter
    Map<String, List<String>> headers();

    /**
     * Clear headers.
     *
     * @return clear headers result
     */
    NWebCli clearHeaders();

    /**
     * Clear cookies.
     *
     * @return clear cookies result
     */
    NWebCli clearCookies();

    /**
     * Removes the specified cookies.
     *
     * @param cookies cookies
     * @return remove cookies result
     */
    NWebCli removeCookies(NWebCookie[] cookies);

    /**
     * Removes the specified cookie.
     *
     * @param cookie cookie
     * @return remove cookie result
     */
    NWebCli removeCookie(NWebCookie cookie);

    /**
     * Contains cookie.
     *
     * @param cookieName cookie name
     * @return contains cookie result
     */
    boolean containsCookie(String cookieName);

    /**
     * Removes the specified cookie.
     *
     * @param cookieName cookie name
     * @return remove cookie result
     */
    NWebCli removeCookie(String cookieName);

    /**
     * Adds the specified cookies.
     *
     * @param cookies cookies
     * @return add cookies result
     */
    NWebCli addCookies(NWebCookie... cookies);

    /**
     * Adds the specified cookie.
     *
     * @param cookie cookie
     * @return add cookie result
     */
    NWebCli addCookie(NWebCookie cookie);

    /**
     * Get.
     *
     * @return get result
     */
    NWebRequest GET();

    /**
     * Get.
     *
     * @param path path
     * @return get result
     */
    NWebRequest GET(String path);

    /**
     * Post.
     *
     * @return post result
     */
    NWebRequest POST();

    /**
     * Post.
     *
     * @param path path
     * @return post result
     */
    NWebRequest POST(String path);

    /**
     * Put.
     *
     * @return put result
     */
    NWebRequest PUT();

    /**
     * Put.
     *
     * @param path path
     * @return put result
     */
    NWebRequest PUT(String path);

    /**
     * Delete.
     *
     * @return delete result
     */
    NWebRequest DELETE();

    /**
     * Delete.
     *
     * @param path path
     * @return delete result
     */
    NWebRequest DELETE(String path);

    /**
     * Patch.
     *
     * @return patch result
     */
    NWebRequest PATCH();

    /**
     * Patch.
     *
     * @param path path
     * @return patch result
     */
    NWebRequest PATCH(String path);

    /**
     * Options.
     *
     * @return options result
     */
    NWebRequest OPTIONS();

    /**
     * Options.
     *
     * @param path path
     * @return options result
     */
    NWebRequest OPTIONS(String path);

    /**
     * Head.
     *
     * @return head result
     */
    NWebRequest HEAD();

    /**
     * Head.
     *
     * @param path path
     * @return head result
     */
    NWebRequest HEAD(String path);

    /**
     * Connect.
     *
     * @return connect result
     */
    NWebRequest CONNECT();

    /**
     * Connect.
     *
     * @param path path
     * @return connect result
     */
    NWebRequest CONNECT(String path);

    /**
     * Trace.
     *
     * @return trace result
     */
    NWebRequest TRACE();

    /**
     * Trace.
     *
     * @param path path
     * @return trace result
     */
    NWebRequest TRACE(String path);

    /**
     * Read timeout.
     *
     * @return read timeout result
     */
    NDuration readTimeout();

    /**
     * Timeout.
     *
     * @param timeout timeout
     * @return timeout result
     */
    NWebCli timeout(NDuration timeout);

    /**
     * Read timeout.
     *
     * @param readTimeout read timeout
     * @return read timeout result
     */
    NWebCli readTimeout(NDuration readTimeout);

    /**
     * Connect timeout.
     *
     * @return connect timeout result
     */
    NDuration connectTimeout();

    /**
     * Connect timeout.
     *
     * @param connectTimeout connect timeout
     * @return connect timeout result
     */
    NWebCli connectTimeout(NDuration connectTimeout);
}
