package net.thevpc.nuts.net;

import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgCode;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;

public interface NWebResponse {
    NHttpCode statusCode();

    int intStatusCode();

    NMsg statusMessage();

    List<String> headers(String name);

    NOptional<String> header(String name);

    Map<String, List<String>> headers();

    NInputSource content();

    <K, V> Map<K, V> contentMapAsJson();

    <K> List<K> contentListAsJson();

    <T> List<T> contentArrayAsJson();

    <T> T contentAsJson(Class<T> clz);

    <T> T contentAs(Class<T> clz, NContentType type);

    Map<?, ?> contentAsJsonMap();

    List<?> contentAsJsonList();

    String contentAsString();

    byte[] contentAsBytes();

    List<NWebCookie> cookies();

    boolean isError();

    boolean isOk();

    NWebResponse ifErrorThrow();

    boolean isClientError();

    boolean isServerError();

    boolean isRedirect();

    String contentType();

    NMsgCode userMessage();

    NWebResponse userMessage(NMsgCode msgCode);
}
