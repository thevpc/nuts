package net.thevpc.nuts.web;

import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.util.NMsg;

import java.util.List;
import java.util.Map;

public interface NWebResponse {
    int getCode();

    NMsg getMsg();

    Map<String, List<String>> getHeaders();

    NInputSource getContent();

    <K, V> Map<K, V> getContentMapAsJson();

    <K> List<K> getContentListAsJson();

    <T> List<T> getContentArrayAsJson();

    <T> T getContentAsJson(Class<T> clz);

    String getContentAsString();

    byte[] getContentAsBytes();

    boolean isError();

    NWebResponse failFast();

    NMsg getUserMessage();

    NWebResponse setUserMessage(NMsg userMessage);

    String getContentType();
}
