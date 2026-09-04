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
public interface NHttpClient extends NComponent {

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NHttpClient of() {
        return NExtensions.of(NHttpClient.class);
    }

    /**
     * Response post processor.
     *
     * @return response post processor result
     */
    Function<NHttpResponse, NHttpResponse> responsePostProcessor();

    /**
     * Response post processor.
     *
     * @param responsePostProcessor response post processor
     * @return response post processor result
     */
    NHttpClient responsePostProcessor(Function<NHttpResponse, NHttpResponse> responsePostProcessor);

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
    NHttpClient baseUri(String prefix);

    /**
     * Req.
     *
     * @param method method
     * @return req result
     */
    NHttpRequest req(NHttpMethod method);

    /**
     * Cookies.
     *
     * @return cookies result
     */
    @NGetter
    List<NHttpCookie> cookies();

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
    NHttpClient executor(Executor executor);

    /**
     * Header.
     *
     * @param name name
     * @param value value
     * @return header result
     */
    NHttpClient header(String name, String value);

    /**
     * Adds the specified header.
     *
     * @param name name
     * @param value value
     * @return add header result
     */
    NHttpClient addHeader(String name, String value);

    /**
     * Removes the specified header.
     *
     * @param name name
     * @param value value
     * @return remove header result
     */
    NHttpClient removeHeader(String name, String value);

    /**
     * Removes the specified header.
     *
     * @param name name
     * @return remove header result
     */
    NHttpClient removeHeader(String name);

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
    NHttpClient clearHeaders();

    /**
     * Clear cookies.
     *
     * @return clear cookies result
     */
    NHttpClient clearCookies();

    /**
     * Removes the specified cookies.
     *
     * @param cookies cookies
     * @return remove cookies result
     */
    NHttpClient removeCookies(NHttpCookie[] cookies);

    /**
     * Removes the specified cookie.
     *
     * @param cookie cookie
     * @return remove cookie result
     */
    NHttpClient removeCookie(NHttpCookie cookie);

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
    NHttpClient removeCookie(String cookieName);

    /**
     * Adds the specified cookies.
     *
     * @param cookies cookies
     * @return add cookies result
     */
    NHttpClient addCookies(NHttpCookie... cookies);

    /**
     * Adds the specified cookie.
     *
     * @param cookie cookie
     * @return add cookie result
     */
    NHttpClient addCookie(NHttpCookie cookie);

    /**
     * Get.
     *
     * @return get result
     */
    NHttpRequest GET();

    /**
     * Get.
     *
     * @param path path
     * @return get result
     */
    NHttpRequest GET(String path);

    /**
     * Post.
     *
     * @return post result
     */
    NHttpRequest POST();

    /**
     * Post.
     *
     * @param path path
     * @return post result
     */
    NHttpRequest POST(String path);

    /**
     * Put.
     *
     * @return put result
     */
    NHttpRequest PUT();

    /**
     * Put.
     *
     * @param path path
     * @return put result
     */
    NHttpRequest PUT(String path);

    /**
     * Delete.
     *
     * @return delete result
     */
    NHttpRequest DELETE();

    /**
     * Delete.
     *
     * @param path path
     * @return delete result
     */
    NHttpRequest DELETE(String path);

    /**
     * Patch.
     *
     * @return patch result
     */
    NHttpRequest PATCH();

    /**
     * Patch.
     *
     * @param path path
     * @return patch result
     */
    NHttpRequest PATCH(String path);

    /**
     * Options.
     *
     * @return options result
     */
    NHttpRequest OPTIONS();

    /**
     * Options.
     *
     * @param path path
     * @return options result
     */
    NHttpRequest OPTIONS(String path);

    /**
     * Head.
     *
     * @return head result
     */
    NHttpRequest HEAD();

    /**
     * Head.
     *
     * @param path path
     * @return head result
     */
    NHttpRequest HEAD(String path);

    /**
     * Connect.
     *
     * @return connect result
     */
    NHttpRequest CONNECT();

    /**
     * Connect.
     *
     * @param path path
     * @return connect result
     */
    NHttpRequest CONNECT(String path);

    /**
     * Trace.
     *
     * @return trace result
     */
    NHttpRequest TRACE();

    /**
     * Trace.
     *
     * @param path path
     * @return trace result
     */
    NHttpRequest TRACE(String path);

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
    NHttpClient timeout(NDuration timeout);

    /**
     * Read timeout.
     *
     * @param readTimeout read timeout
     * @return read timeout result
     */
    NHttpClient readTimeout(NDuration readTimeout);

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
    NHttpClient connectTimeout(NDuration connectTimeout);
}
