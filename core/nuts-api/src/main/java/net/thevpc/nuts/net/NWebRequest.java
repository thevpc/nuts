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
public interface NWebRequest extends NMsgFormattable {
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
    NWebRequest oneWay(boolean oneWay);

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
    NWebRequest uri(String url, Object... vars);

    /**
     * Uri.
     *
     * @param url url
     * @return uri result
     */
    NWebRequest uri(String url);

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
    NWebRequest method(NHttpMethod method);

    /**
     * Get.
     *
     * @return get result
     */
    NWebRequest GET();

    /**
     * Post.
     *
     * @return post result
     */
    NWebRequest POST();

    /**
     * Patch.
     *
     * @return patch result
     */
    NWebRequest PATCH();

    /**
     * Options.
     *
     * @return options result
     */
    NWebRequest OPTIONS();

    /**
     * Head.
     *
     * @return head result
     */
    NWebRequest HEAD();

    /**
     * Connect.
     *
     * @return connect result
     */
    NWebRequest CONNECT();

    /**
     * Trace.
     *
     * @return trace result
     */
    NWebRequest TRACE();

    /**
     * Trace.
     *
     * @param url url
     * @return trace result
     */
    NWebRequest TRACE(String url);

    /**
     * Put.
     *
     * @return put result
     */
    NWebRequest PUT();

    /**
     * Delete.
     *
     * @return delete result
     */
    NWebRequest DELETE();

    /**
     * Get.
     *
     * @param url url
     * @return get result
     */
    NWebRequest GET(String url);

    /**
     * Post.
     *
     * @param url url
     * @return post result
     */
    NWebRequest POST(String url);

    /**
     * Patch.
     *
     * @param url url
     * @return patch result
     */
    NWebRequest PATCH(String url);

    /**
     * Options.
     *
     * @param url url
     * @return options result
     */
    NWebRequest OPTIONS(String url);

    /**
     * Head.
     *
     * @param url url
     * @return head result
     */
    NWebRequest HEAD(String url);

    /**
     * Connect.
     *
     * @param url url
     * @return connect result
     */
    NWebRequest CONNECT(String url);

    /**
     * Put.
     *
     * @param url url
     * @return put result
     */
    NWebRequest PUT(String url);

    /**
     * Delete.
     *
     * @param url url
     * @return delete result
     */
    NWebRequest DELETE(String url);

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
    NWebRequest headers(Map<String, List<String>> headers);

    /**
     * Adds the specified headers.
     *
     * @param headers headers
     * @return add headers result
     */
    NWebRequest addHeaders(Map<String, List<String>> headers);

    /**
     * Adds the specified parameters.
     *
     * @param parameters parameters
     * @return add parameters result
     */
    NWebRequest addParameters(Map<String, List<String>> parameters);

    /**
     * Props file headers.
     *
     * @param path path
     * @return props file headers result
     */
    NWebRequest propsFileHeaders(NPath path);

    /**
     * Adds the specified props file headers.
     *
     * @param path path
     * @return add props file headers result
     */
    NWebRequest addPropsFileHeaders(NPath path);

    /**
     * Adds the specified json file headers.
     *
     * @param path path
     * @return add json file headers result
     */
    NWebRequest addJsonFileHeaders(NPath path);

    /**
     * Json file headers.
     *
     * @param path path
     * @return json file headers result
     */
    NWebRequest jsonFileHeaders(NPath path);

    /**
     * Props file parameters.
     *
     * @param path path
     * @return props file parameters result
     */
    NWebRequest propsFileParameters(NPath path);

    /**
     * Adds the specified props file parameters.
     *
     * @param path path
     * @return add props file parameters result
     */
    NWebRequest addPropsFileParameters(NPath path);

    /**
     * Adds the specified json file parameters.
     *
     * @param path path
     * @return add json file parameters result
     */
    NWebRequest addJsonFileParameters(NPath path);

    /**
     * Pson file parameters.
     *
     * @param path path
     * @return pson file parameters result
     */
    NWebRequest psonFileParameters(NPath path);

    /**
     * equivalent to set header, to match JDK's method
     *
     * @param name  name
     * @param value value
     * @return this instance
     */
    NWebRequest header(String name, String value);

    /**
     * Adds the specified header.
     *
     * @param name name
     * @param value value
     * @return add header result
     */
    NWebRequest addHeader(String name, String value);

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
    NWebRequest parameters(Map<String, List<String>> parameters);

    /**
     * Do with.
     *
     * @param any any
     * @return do with result
     */
    NWebRequest doWith(Consumer<NWebRequest> any);

    /**
     * Parameter.
     *
     * @param name name
     * @param value value
     * @return parameter result
     */
    NWebRequest parameter(String name, String value);

    /**
     * Adds the specified parameter.
     *
     * @param name name
     * @param value value
     * @return add parameter result
     */
    NWebRequest addParameter(String name, String value);

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
    NWebRequest jsonRequestBody(Object body);

    /**
     * Request body.
     *
     * @param body body
     * @return request body result
     */
    NWebRequest requestBody(String body);

    /**
     * Request body.
     *
     * @param body body
     * @return request body result
     */
    NWebRequest requestBody(byte[] body);

    /**
     * Request body.
     *
     * @param body body
     * @return request body result
     */
    NWebRequest requestBody(NInputSource body);

    /**
     * Content language.
     *
     * @param contentLanguage content language
     * @return content language result
     */
    NWebRequest contentLanguage(String contentLanguage);

    /**
     * Authorization bearer.
     *
     * @param authorizationBearer authorization bearer
     * @return authorization bearer result
     */
    NWebRequest authorizationBearer(String authorizationBearer);

    /**
     * Authorization basic.
     *
     * @param username username
     * @param password password
     * @return authorization basic result
     */
    NWebRequest authorizationBasic(String username, String password);

    /**
     * Authorization.
     *
     * @param authorization authorization
     * @return authorization result
     */
    NWebRequest authorization(String authorization);

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
    NWebRequest addFormUrlEncoded(String key, String value);

    /**
     * Adds the specified form url encoded.
     *
     * @param value value
     * @return add form url encoded result
     */
    NWebRequest addFormUrlEncoded(Map<String, String> value);

    /**
     * Form data.
     *
     * @param key key
     * @param value value
     * @return form data result
     */
    NWebRequest formData(String key, NInputContentProvider value);

    /**
     * Form data.
     *
     * @param key key
     * @param value value
     * @return form data result
     */
    NWebRequest formData(String key, String value);

    /**
     * Form url encoded.
     *
     * @param m m
     * @return form url encoded result
     */
    NWebRequest formUrlEncoded(Map<String, String> m);

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
    NWebRequest contentTypeFormUrlEncoded();

    /**
     * Content type.
     *
     * @param contentType content type
     * @return content type result
     */
    NWebRequest contentType(String contentType);

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
    NWebRequest timeout(NDuration readTimeout);

    /**
     * Read timeout.
     *
     * @param readTimeout read timeout
     * @return read timeout result
     */
    NWebRequest readTimeout(NDuration readTimeout);

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
    NWebRequest connectTimeout(NDuration duration);

    /**
     * Parts.
     *
     * @return parts result
     */
    List<NWebRequestBody> parts();

    /**
     * Adds the specified part.
     *
     * @param body body
     * @return add part result
     */
    NWebRequest addPart(NWebRequestBody body);

    /**
     * Adds the specified part.
     *
     * @param name name
     * @return add part result
     */
    NWebRequestBody addPart(String name);

    /**
     * Adds the specified part.
     *
     * @return add part result
     */
    NWebRequestBody addPart();

    /**
     * Adds the specified part.
     *
     * @param name name
     * @param value value
     * @return add part result
     */
    NWebRequest addPart(String name, String value);

    /**
     * Adds the specified part.
     *
     * @param name name
     * @param fileName file name
     * @param contentType content type
     * @param body body
     * @return add part result
     */
    NWebRequest addPart(String name, String fileName, String contentType, NInputSource body);

    /**
     * Adds the specified part.
     *
     * @param name name
     * @param file file
     * @return add part result
     */
    NWebRequest addPart(String name, File file);

    /**
     * Adds the specified part.
     *
     * @param name name
     * @param file file
     * @return add part result
     */
    NWebRequest addPart(String name, Path file);

    /**
     * Adds the specified part.
     *
     * @param name name
     * @param file file
     * @return add part result
     */
    NWebRequest addPart(String name, NPath file);

    /**
     * Adds the specified part.
     *
     * @param file file
     * @return add part result
     */
    NWebRequest addPart(File file);

    /**
     * Adds the specified part.
     *
     * @param file file
     * @return add part result
     */
    NWebRequest addPart(Path file);

    /**
     * Adds the specified part.
     *
     * @param file file
     * @return add part result
     */
    NWebRequest addPart(NPath file);

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
    NWebResponse run();

    /**
     * Run async.
     *
     * @return run async result
     */
    CompletableFuture<NWebResponse> runAsync();

    /**
     * Run async.
     *
     * @param executor executor
     * @return run async result
     */
    CompletableFuture<NWebResponse> runAsync(Executor executor);

}
