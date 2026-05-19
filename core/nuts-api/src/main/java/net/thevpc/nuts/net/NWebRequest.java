package net.thevpc.nuts.net;

import net.thevpc.nuts.text.NMsgFormattable;
import net.thevpc.nuts.io.NInputContentProvider;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.time.NDuration;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface NWebRequest extends NMsgFormattable {
    boolean isOneWay();

    NWebRequest setOneWay(boolean oneWay);

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

    String getHeader(String name);

    List<String> headers(String name);

    Map<String, List<String>> headers();

    NWebRequest setHeaders(Map<String, List<String>> headers);

    NWebRequest addHeaders(Map<String, List<String>> headers);

    NWebRequest addParameters(Map<String, List<String>> parameters);

    NWebRequest setPropsFileHeaders(NPath path);

    NWebRequest addPropsFileHeaders(NPath path);

    NWebRequest addJsonFileHeaders(NPath path);

    NWebRequest setJsonFileHeaders(NPath path);

    NWebRequest setPropsFileParameters(NPath path);

    NWebRequest addPropsFileParameters(NPath path);

    NWebRequest addJsonFileParameters(NPath path);

    NWebRequest setJsonFileParameters(NPath path);

    /**
     * equivalent to set header, to match JDK's method
     *
     * @param name  name
     * @param value value
     * @return this instance
     */
    NWebRequest header(String name, String value);

    NWebRequest addHeader(String name, String value);

    NWebRequest setHeader(String name, String value);

    Map<String, List<String>> parameters();

    NWebRequest setParameters(Map<String, List<String>> parameters);

    NWebRequest doWith(Consumer<NWebRequest> any);

    NWebRequest parameter(String name, String value);

    NWebRequest addParameter(String name, String value);

    NWebRequest setParameter(String name, String value);

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

    NWebRequest setFormData(String key, NInputContentProvider value);

    NWebRequest addFormData(String key, NInputContentProvider value);

    NWebRequest setFormData(String key, String value);

    NWebRequest addFormData(String key, String value);

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

    String effectiveUrl();

    NWebResponse run();

}
