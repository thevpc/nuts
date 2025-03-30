package net.thevpc.nuts.runtime.standalone.web;

import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.web.NWebCookie;
import net.thevpc.nuts.web.NWebResponse;
import net.thevpc.nuts.web.NWebResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NWebResponseImpl implements NWebResponse {
    private int code;
    private NMsg msg;
    private DefaultNWebHeaders headers=new DefaultNWebHeaders();
    private NInputSource content;
    private NMsg userMessage;

    public NWebResponseImpl(int code, NMsg msg, Map<String, List<String>> headers, NInputSource content) {
        this.code = code;
        this.msg = msg;
        this.headers.addHeadersMulti(headers, DefaultNWebHeaders.Mode.ALWAYS);
        this.content = content;
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
    public List<String> getHeaders(String name) {
        List<String> u = headers.getOrEmpty(name);
        if (u == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(u);
        }
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers.toMap();
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
    public <T> T getContentAs(Class<T> clz, NContentType type) {
        if (content == null) {
            return null;
        }
        try (InputStream in = content.getInputStream()) {
            return NElements.of()
                    .setContentType(type).parse(in, clz);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
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

    public NWebCookie[] getCookies() {
        return getHeaders("Set-Cookie").stream().map(DefaultNWebCookie::new).toArray(NWebCookie[]::new);
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
            List<String> list = headers.getOrEmpty("Content-Type");
            for (String s : list) {
                if (s != null && !s.isEmpty()) {
                    return s;
                }
            }
        }
        return null;
    }
}
