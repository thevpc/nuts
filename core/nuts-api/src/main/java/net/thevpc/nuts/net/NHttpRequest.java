package net.thevpc.nuts.net;

import net.thevpc.nuts.text.NMsgFormattable;
import net.thevpc.nuts.io.NInputContentProvider;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NSetter;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * NWebRequest interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NHttpRequest extends NMsgFormattable {
    /**
     * Checks if is one way.
     *
     * @return is one way result
     */
    boolean isOneWay();

    /**
     * One way.
     *
     * @param oneWay one way
     * @return one way result
     */
    @NSetter
    NHttpRequest oneWay(boolean oneWay);

    /**
     * Uri.
     *
     * @return uri result
     */
    String uri();

    /**
     * Uri.
     *
     * @param url url
     * @param vars vars
     * @return uri result
     */
    NHttpRequest uri(String url, Object... vars);

    /**
     * Uri.
     *
     * @param url url
     * @return uri result
     */
    NHttpRequest uri(String url);

    /**
     * Method.
     *
     * @return method result
     */
    NHttpMethod method();

    /**
     * Method.
     *
     * @param method method
     * @return method result
     */
    NHttpRequest method(NHttpMethod method);

    /**
     * Get.
     *
     * @return get result
     */
    NHttpRequest GET();

    /**
     * Post.
     *
     * @return post result
     */
    NHttpRequest POST();

    /**
     * Patch.
     *
     * @return patch result
     */
    NHttpRequest PATCH();

    /**
     * Options.
     *
     * @return options result
     */
    NHttpRequest OPTIONS();

    /**
     * Head.
     *
     * @return head result
     */
    NHttpRequest HEAD();

    /**
     * Connect.
     *
     * @return connect result
     */
    NHttpRequest CONNECT();

    /**
     * Trace.
     *
     * @return trace result
     */
    NHttpRequest TRACE();

    /**
     * Trace.
     *
     * @param url url
     * @return trace result
     */
    NHttpRequest TRACE(String url);

    /**
     * Put.
     *
     * @return put result
     */
    NHttpRequest PUT();

    /**
     * Delete.
     *
     * @return delete result
     */
    NHttpRequest DELETE();

    /**
     * Get.
     *
     * @param url url
     * @return get result
     */
    NHttpRequest GET(String url);

    /**
     * Post.
     *
     * @param url url
     * @return post result
     */
    NHttpRequest POST(String url);

    /**
     * Patch.
     *
     * @param url url
     * @return patch result
     */
    NHttpRequest PATCH(String url);

    /**
     * Options.
     *
     * @param url url
     * @return options result
     */
    NHttpRequest OPTIONS(String url);

    /**
     * Head.
     *
     * @param url url
     * @return head result
     */
    NHttpRequest HEAD(String url);

    /**
     * Connect.
     *
     * @param url url
     * @return connect result
     */
    NHttpRequest CONNECT(String url);

    /**
     * Put.
     *
     * @param url url
     * @return put result
     */
    NHttpRequest PUT(String url);

    /**
     * Delete.
     *
     * @param url url
     * @return delete result
     */
    NHttpRequest DELETE(String url);

    /**
     * Header.
     *
     * @param name name
     * @return header result
     */
    String header(String name);

    /**
     * Headers.
     *
     * @param name name
     * @return headers result
     */
    List<String> headers(String name);

    /**
     * Headers.
     *
     * @return headers result
     */
    Map<String, List<String>> headers();

    /**
     * Headers.
     *
     * @param headers headers
     * @return headers result
     */
    NHttpRequest headers(Map<String, List<String>> headers);

    /**
     * Adds the specified headers.
     *
     * @param headers headers
     * @return add headers result
     */
    NHttpRequest addHeaders(Map<String, List<String>> headers);

    /**
     * Adds the specified parameters.
     *
     * @param parameters parameters
     * @return add parameters result
     */
    NHttpRequest addParameters(Map<String, List<String>> parameters);

    /**
     * Props file headers.
     *
     * @param path path
     * @return props file headers result
     */
    NHttpRequest propsFileHeaders(NPath path);

    /**
     * Adds the specified props file headers.
     *
     * @param path path
     * @return add props file headers result
     */
    NHttpRequest addPropsFileHeaders(NPath path);

    /**
     * Adds the specified json file headers.
     *
     * @param path path
     * @return add json file headers result
     */
    NHttpRequest addJsonFileHeaders(NPath path);

    /**
     * Json file headers.
     *
     * @param path path
     * @return json file headers result
     */
    NHttpRequest jsonFileHeaders(NPath path);

    /**
     * Props file parameters.
     *
     * @param path path
     * @return props file parameters result
     */
    NHttpRequest propsFileParameters(NPath path);

    /**
     * Adds the specified props file parameters.
     *
     * @param path path
     * @return add props file parameters result
     */
    NHttpRequest addPropsFileParameters(NPath path);

    /**
     * Adds the specified json file parameters.
     *
     * @param path path
     * @return add json file parameters result
     */
    NHttpRequest addJsonFileParameters(NPath path);

    /**
     * Pson file parameters.
     *
     * @param path path
     * @return pson file parameters result
     */
    NHttpRequest psonFileParameters(NPath path);

    /**
     * equivalent to set header, to match JDK's method
     *
     * @param name  name
     * @param value value
     * @return this instance
     */
    NHttpRequest header(String name, String value);

    /**
     * Adds the specified header.
     *
     * @param name name
     * @param value value
     * @return add header result
     */
    NHttpRequest addHeader(String name, String value);

    /**
     * Parameters.
     *
     * @return parameters result
     */
    Map<String, List<String>> parameters();

    /**
     * Parameters.
     *
     * @param parameters parameters
     * @return parameters result
     */
    NHttpRequest parameters(Map<String, List<String>> parameters);

    /**
     * Do with.
     *
     * @param any any
     * @return do with result
     */
    NHttpRequest doWith(Consumer<NHttpRequest> any);

    /**
     * Parameter.
     *
     * @param name name
     * @param value value
     * @return parameter result
     */
    NHttpRequest parameter(String name, String value);

    /**
     * Adds the specified parameter.
     *
     * @param name name
     * @param value value
     * @return add parameter result
     */
    NHttpRequest addParameter(String name, String value);

    /**
     * Request body.
     *
     * @return request body result
     */
    NInputSource requestBody();

    /**
     * Json request body.
     *
     * @param body body
     * @return json request body result
     */
    NHttpRequest jsonRequestBody(Object body);

    /**
     * Request body.
     *
     * @param body body
     * @return request body result
     */
    NHttpRequest requestBody(String body);

    /**
     * Request body.
     *
     * @param body body
     * @return request body result
     */
    NHttpRequest requestBody(byte[] body);

    /**
     * Request body.
     *
     * @param body body
     * @return request body result
     */
    NHttpRequest requestBody(NInputSource body);

    /**
     * Content language.
     *
     * @param contentLanguage content language
     * @return content language result
     */
    NHttpRequest contentLanguage(String contentLanguage);

    /**
     * Authorization bearer.
     *
     * @param authorizationBearer authorization bearer
     * @return authorization bearer result
     */
    NHttpRequest authorizationBearer(String authorizationBearer);

    /**
     * Authorization basic.
     *
     * @param username username
     * @param password password
     * @return authorization basic result
     */
    NHttpRequest authorizationBasic(String username, String password);

    /**
     * Authorization.
     *
     * @param authorization authorization
     * @return authorization result
     */
    NHttpRequest authorization(String authorization);

    /**
     * Authorization.
     *
     * @return authorization result
     */
    String authorization();

    /**
     * Authorization bearer.
     *
     * @return authorization bearer result
     */
    String authorizationBearer();

    /**
     * Content language.
     *
     * @return content language result
     */
    String contentLanguage();

    /**
     * Adds the specified form url encoded.
     *
     * @param key key
     * @param value value
     * @return add form url encoded result
     */
    NHttpRequest addFormUrlEncoded(String key, String value);

    /**
     * Adds the specified form url encoded.
     *
     * @param value value
     * @return add form url encoded result
     */
    NHttpRequest addFormUrlEncoded(Map<String, String> value);

    /**
     * Form data.
     *
     * @param key key
     * @param value value
     * @return form data result
     */
    NHttpRequest formData(String key, NInputContentProvider value);

    /**
     * Form data.
     *
     * @param key key
     * @param value value
     * @return form data result
     */
    NHttpRequest formData(String key, String value);

    /**
     * Form url encoded.
     *
     * @param m m
     * @return form url encoded result
     */
    NHttpRequest formUrlEncoded(Map<String, String> m);

    /**
     * Content type.
     *
     * @return content type result
     */
    String contentType();

    /**
     * Content type form url encoded.
     *
     * @return content type form url encoded result
     */
    NHttpRequest contentTypeFormUrlEncoded();

    /**
     * Content type.
     *
     * @param contentType content type
     * @return content type result
     */
    NHttpRequest contentType(String contentType);

    /**
     * Read timeout.
     *
     * @return read timeout result
     */
    NDuration readTimeout();

    /**
     * Timeout.
     *
     * @param readTimeout read timeout
     * @return timeout result
     */
    NHttpRequest timeout(NDuration readTimeout);

    /**
     * Read timeout.
     *
     * @param readTimeout read timeout
     * @return read timeout result
     */
    NHttpRequest readTimeout(NDuration readTimeout);

    /**
     * Connect timeout.
     *
     * @return connect timeout result
     */
    NDuration connectTimeout();

    /**
     * Connect timeout.
     *
     * @param duration duration
     * @return connect timeout result
     */
    NHttpRequest connectTimeout(NDuration duration);

    /**
     * Parts.
     *
     * @return parts result
     */
    List<NHttpRequestBody> parts();

    /**
     * Adds the specified part.
     *
     * @param body body
     * @return add part result
     */
    NHttpRequest addPart(NHttpRequestBody body);

    /**
     * Adds the specified part.
     *
     * @param name name
     * @return add part result
     */
    NHttpRequestBody addPart(String name);

    /**
     * Adds the specified part.
     *
     * @return add part result
     */
    NHttpRequestBody addPart();

    /**
     * Adds the specified part.
     *
     * @param name name
     * @param value value
     * @return add part result
     */
    NHttpRequest addPart(String name, String value);

    /**
     * Adds the specified part.
     *
     * @param name name
     * @param fileName file name
     * @param contentType content type
     * @param body body
     * @return add part result
     */
    NHttpRequest addPart(String name, String fileName, String contentType, NInputSource body);

    /**
     * Adds the specified part.
     *
     * @param name name
     * @param file file
     * @return add part result
     */
    NHttpRequest addPart(String name, File file);

    /**
     * Adds the specified part.
     *
     * @param name name
     * @param file file
     * @return add part result
     */
    NHttpRequest addPart(String name, Path file);

    /**
     * Adds the specified part.
     *
     * @param name name
     * @param file file
     * @return add part result
     */
    NHttpRequest addPart(String name, NPath file);

    /**
     * Adds the specified part.
     *
     * @param file file
     * @return add part result
     */
    NHttpRequest addPart(File file);

    /**
     * Adds the specified part.
     *
     * @param file file
     * @return add part result
     */
    NHttpRequest addPart(Path file);

    /**
     * Adds the specified part.
     *
     * @param file file
     * @return add part result
     */
    NHttpRequest addPart(NPath file);

    /**
     * Effective uri.
     *
     * @return effective uri result
     */
    String effectiveUri();

    /**
     * Run.
     *
     * @return run result
     */
    NHttpResponse run();

    /**
     * Run async.
     *
     * @return run async result
     */
    CompletableFuture<NHttpResponse> runAsync();

    /**
     * Run async.
     *
     * @param executor executor
     * @return run async result
     */
    CompletableFuture<NHttpResponse> runAsync(Executor executor);

}
