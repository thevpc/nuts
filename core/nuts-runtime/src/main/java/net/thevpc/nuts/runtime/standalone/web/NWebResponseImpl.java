package net.thevpc.nuts.runtime.standalone.web;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.web.NWebResponse;
import net.thevpc.nuts.web.NWebResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

public class NWebResponseImpl implements NWebResponse {
    private int code;
    private NMsg msg;
    private Map<String, List<String>> headers;
    private NInputSource content;
    private NMsg userMessage;
    private NSession session;

    public NWebResponseImpl(int code, NMsg msg, Map<String, List<String>> headers, NInputSource content, NSession session) {
        this.code = code;
        this.msg = msg;
        this.headers = headers;
        this.content = content;
        this.session = session;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public NMsg getMsg() {
        return msg;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public NInputSource getContent() {
        return content;
    }

    @Override
    public <K, V> Map<K, V> getContentMapAsJson() {
        return getContentAsJson(Map.class);
    }

    @Override
    public <K> List<K> getContentListAsJson() {
        return getContentAsJson(List.class);
    }

    @Override
    public <T> List<T> getContentArrayAsJson() {
        return getContentAsJson(List.class);
    }

    @Override
    public <T> T getContentAsJson(Class<T> clz) {
        if (content == null) {
            return null;
        }
        try (InputStream in = content.getInputStream()) {
            return NElements.of()
                    .json().parse(in, clz);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public Map<?, ?> getContentAsJsonMap() {
        return getContentAsJson(Map.class);
    }

    @Override
    public List<?> getContentAsJsonList() {
        return getContentAsJson(List.class);
    }

    @Override
    public String getContentAsString() {
        if (content == null) {
            return null;
        }
        return new String(content.readBytes());
    }

    @Override
    public byte[] getContentAsBytes() {
        if (content == null) {
            return null;
        }
        return content.readBytes();
    }

    @Override
    public boolean isError() {
        return code >= 400;
    }

    @Override
    public NWebResponse failFast() {
        if (isError()) {
            throw new NWebResponseException(msg, userMessage, code);
        }
        return this;
    }

    @Override
    public NMsg getUserMessage() {
        return userMessage;
    }

    @Override
    public NWebResponse setUserMessage(NMsg userMessage) {
        this.userMessage = userMessage;
        return this;
    }

    @Override
    public String getContentType() {
        if (headers != null) {
            List<String> list = headers.get("Content-Type");
            if (list != null) {
                for (String s : list) {
                    if (s != null && !s.isEmpty()) {
                        return s;
                    }
                }
            }
        }
        return null;
    }
}
