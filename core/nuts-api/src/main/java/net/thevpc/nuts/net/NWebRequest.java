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

public interface NWebRequest extends NMsgFormattable {
    boolean isOneWay();

    @NSetter
    NWebRequest oneWay(boolean oneWay);

    String uri();

    NWebRequest uri(String url, Object... vars);

    NWebRequest uri(String url);

    NHttpMethod method();

    NWebRequest method(NHttpMethod method);

    NWebRequest GET();

    NWebRequest POST();

    NWebRequest PATCH();

    NWebRequest OPTIONS();

    NWebRequest HEAD();

    NWebRequest CONNECT();

    NWebRequest TRACE();

    NWebRequest TRACE(String url);

    NWebRequest PUT();

    NWebRequest DELETE();

    NWebRequest GET(String url);

    NWebRequest POST(String url);

    NWebRequest PATCH(String url);

    NWebRequest OPTIONS(String url);

    NWebRequest HEAD(String url);

    NWebRequest CONNECT(String url);

    NWebRequest PUT(String url);

    NWebRequest DELETE(String url);

    String header(String name);

    List<String> headers(String name);

    Map<String, List<String>> headers();

    NWebRequest headers(Map<String, List<String>> headers);

    NWebRequest addHeaders(Map<String, List<String>> headers);

    NWebRequest addParameters(Map<String, List<String>> parameters);

    NWebRequest propsFileHeaders(NPath path);

    NWebRequest addPropsFileHeaders(NPath path);

    NWebRequest addJsonFileHeaders(NPath path);

    NWebRequest jsonFileHeaders(NPath path);

    NWebRequest propsFileParameters(NPath path);

    NWebRequest addPropsFileParameters(NPath path);

    NWebRequest addJsonFileParameters(NPath path);

    NWebRequest psonFileParameters(NPath path);

    /**
     * equivalent to set header, to match JDK's method
     *
     * @param name  name
     * @param value value
     * @return this instance
     */
    NWebRequest header(String name, String value);

    NWebRequest addHeader(String name, String value);

    Map<String, List<String>> parameters();

    NWebRequest parameters(Map<String, List<String>> parameters);

    NWebRequest doWith(Consumer<NWebRequest> any);

    NWebRequest parameter(String name, String value);

    NWebRequest addParameter(String name, String value);

    NInputSource requestBody();

    NWebRequest jsonRequestBody(Object body);

    NWebRequest requestBody(String body);

    NWebRequest requestBody(byte[] body);

    NWebRequest requestBody(NInputSource body);

    NWebRequest contentLanguage(String contentLanguage);

    NWebRequest authorizationBearer(String authorizationBearer);

    NWebRequest authorizationBasic(String username, String password);

    NWebRequest authorization(String authorization);

    String authorization();

    String authorizationBearer();

    String contentLanguage();

    NWebRequest addFormUrlEncoded(String key, String value);

    NWebRequest addFormUrlEncoded(Map<String, String> value);

    NWebRequest formData(String key, NInputContentProvider value);

    NWebRequest formData(String key, String value);

    NWebRequest formUrlEncoded(Map<String, String> m);

    String contentType();

    NWebRequest contentTypeFormUrlEncoded();

    NWebRequest contentType(String contentType);

    NDuration readTimeout();

    NWebRequest timeout(NDuration readTimeout);

    NWebRequest readTimeout(NDuration readTimeout);

    NDuration connectTimeout();

    NWebRequest connectTimeout(NDuration duration);

    List<NWebRequestBody> parts();

    NWebRequest addPart(NWebRequestBody body);

    NWebRequestBody addPart(String name);

    NWebRequestBody addPart();

    NWebRequest addPart(String name, String value);

    NWebRequest addPart(String name, String fileName, String contentType, NInputSource body);

    NWebRequest addPart(String name, File file);

    NWebRequest addPart(String name, Path file);

    NWebRequest addPart(String name, NPath file);

    NWebRequest addPart(File file);

    NWebRequest addPart(Path file);

    NWebRequest addPart(NPath file);

    String effectiveUri();

    NWebResponse run();

    CompletableFuture<NWebResponse> runAsync();

    CompletableFuture<NWebResponse> runAsync(Executor executor);

}
