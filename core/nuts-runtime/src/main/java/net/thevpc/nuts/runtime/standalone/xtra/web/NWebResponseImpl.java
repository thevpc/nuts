package net.thevpc.nuts.runtime.standalone.xtra.web;

import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.util.NCallOnceSupplier;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NMsgCode;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.web.NHttpCode;
import net.thevpc.nuts.web.NWebCookie;
import net.thevpc.nuts.web.NWebResponse;
import net.thevpc.nuts.web.NWebResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class NWebResponseImpl implements NWebResponse {
    private NHttpCode httpCode;
    private NMsg msg;
    private DefaultNWebHeaders headers = new DefaultNWebHeaders();
    private NCallOnceSupplier<NInputSource> content;
    private NMsgCode msgCode;

    public NWebResponseImpl(NHttpCode code, NMsg msg, Map<String, List<String>> headers, Supplier<NInputSource> content) {
        this.httpCode = code;
        this.msg = msg;
        this.headers.addHeadersMulti(headers, DefaultNWebHeaders.Mode.ALWAYS);
        this.content = new NCallOnceSupplier<>(content);
    }

    @Override
    public NOptional<String> getHeader(String name) {
        return NOptional.ofNamedFirst(getHeaders(name), name);
    }

    @Override
    public int getIntCode() {
        return httpCode.getCode();
    }

    public NHttpCode getCode() {
        return httpCode;
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
        return content.get();
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
        NInputSource content1 = getContent();
        if (content1 == null) {
            return null;
        }
        try (InputStream in = content1.getInputStream()) {
            return NElementParser.ofJson().parse(in, clz);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public <T> T getContentAsJson(Class<T> clz) {
        if (content == null) {
            return null;
        }
        NInputSource content1 = getContent();
        if (content1 == null) {
            return null;
        }
        try (InputStream in = content1.getInputStream()) {
            return NElementParser.ofJson().parse(in, clz);
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
        NInputSource content1 = getContent();
        if (content1 == null) {
            return null;
        }
        return new String(content1.readBytes());
    }

    @Override
    public byte[] getContentAsBytes() {
        if (content == null) {
            return null;
        }
        NInputSource content1 = getContent();
        if (content1 == null) {
            return null;
        }
        return content1.readBytes();
    }

    public NWebCookie[] getCookies() {
        return getHeaders("Set-Cookie").stream().map(DefaultNWebCookie::new).toArray(NWebCookie[]::new);
    }

    @Override
    public boolean isError() {
        return httpCode.getCode() >= 400;
    }

    @Override
    public boolean isOk() {
        int ic = httpCode.getCode();
        return
                ic >= 200
                        && ic < 300
                ;
    }

    @Override
    public NWebResponse failFast() {
        if (isError()) {
            throw new NWebResponseException(msg, msgCode, httpCode);
        }
        return this;
    }

    public NMsgCode getMsgCode() {
        return msgCode;
    }

    public NWebResponse setMsgCode(NMsgCode msgCode) {
        this.msgCode = msgCode;
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
