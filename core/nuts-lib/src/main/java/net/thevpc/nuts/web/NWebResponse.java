package net.thevpc.nuts.web;

import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.List;
import java.util.Map;

public interface NWebResponse {
    NHttpCode getCode();
    int getIntCode();

    NMsg getMsg();

    List<String> getHeaders(String name);
    NOptional<String> getHeader(String name);

    Map<String, List<String>> getHeaders();

    NInputSource getContent();

    <K, V> Map<K, V> getContentMapAsJson();

    <K> List<K> getContentListAsJson();

    <T> List<T> getContentArrayAsJson();

    <T> T getContentAsJson(Class<T> clz);

    <T> T getContentAs(Class<T> clz, NContentType type);

    Map<?, ?> getContentAsJsonMap();

    List<?> getContentAsJsonList();

    String getContentAsString();

    byte[] getContentAsBytes();

    NWebCookie[] getCookies();

    boolean isError();
    boolean isOk();

    NWebResponse failFast();

    NMsg getUserMessage();

    NWebResponse setUserMessage(NMsg userMessage);

    String getContentType();
}
