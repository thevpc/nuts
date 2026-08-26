package net.thevpc.nuts.runtime.standalone.xtra.web;

import net.thevpc.nuts.concurrent.NOnceValue;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgCode;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.net.NHttpCode;
import net.thevpc.nuts.net.NWebCookie;
import net.thevpc.nuts.net.NWebResponse;
import net.thevpc.nuts.net.NWebResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class NWebResponseImpl implements NWebResponse {
    private final NHttpCode httpCode;
    private final NMsg msg;
    private final DefaultNWebHeaders headers = new DefaultNWebHeaders();
    private final NOnceValue<NInputSource> content;
    private NMsgCode msgCode;

    public NWebResponseImpl(NHttpCode code, NMsg msg, Map<String, List<String>> headers, Supplier<NInputSource> content) {
        this.httpCode = code;
        this.msg = msg;
        this.headers.addHeadersMulti(headers, DefaultNWebHeaders.Mode.ALWAYS);
        this.content = NOnceValue.ofSupplier(content);
    }

    @Override
    public NOptional<String> header(String name) {
        return NOptional.ofNamedFirst(headers(name), name);
    }

    @Override
    public int intStatusCode() {
        return httpCode.code();
    }

    public NHttpCode statusCode() {
        return httpCode;
    }

    @Override
    public NMsg statusMessage() {
        return msg;
    }

    @Override
    public List<String> headers(String name) {
        List<String> u = headers.getOrEmpty(name);
        if (u == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(u);
        }
    }

    @Override
    public Map<String, List<String>> headers() {
        return headers.toMap();
    }

    @Override
    public Map<String, String> firstHeaders() {
        return headers.toFirstMap();
    }

    @Override
    public NInputSource content() {
        return content.get();
    }

    @Override
    public <K, V> Map<K, V> contentMapAsJson() {
        return contentAsJson(Map.class);
    }

    @Override
    public <K> List<K> contentListAsJson() {
        return contentAsJson(List.class);
    }

    @Override
    public <T> List<T> contentArrayAsJson() {
        return contentAsJson(List.class);
    }

    @Override
    public <T> T contentAs(Class<T> clz, NContentType type) {
        if (content == null) {
            return null;
        }
        NInputSource content1 = content();
        if (content1 == null) {
            return null;
        }
        try (InputStream in = content1.inputStream()) {
            return NElementReader.ofJson().read(in, clz);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public NElement contentAsJson() {
        return contentAs(NElement.class, NContentType.JSON);
    }

    @Override
    public <T> T contentAsJson(Class<T> clz) {
        if (content == null) {
            return null;
        }
        NInputSource content1 = content();
        if (content1 == null) {
            return null;
        }
        try (InputStream in = content1.inputStream()) {
            return NElementReader.ofJson().read(in, clz);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public Map<?, ?> contentAsJsonMap() {
        return contentAsJson(Map.class);
    }

    @Override
    public List<?> contentAsJsonList() {
        return contentAsJson(List.class);
    }

    @Override
    public String contentAsString() {
        if (content == null) {
            return null;
        }
        NInputSource content1 = content();
        if (content1 == null) {
            return null;
        }
        return new String(content1.readBytes());
    }

    @Override
    public byte[] contentAsBytes() {
        if (content == null) {
            return null;
        }
        NInputSource content1 = content();
        if (content1 == null) {
            return null;
        }
        return content1.readBytes();
    }

    public List<NWebCookie> cookies() {
        return headers("Set-Cookie").stream().map(DefaultNWebCookie::new).collect(Collectors.toList());
    }

    @Override
    public boolean isError() {
        return httpCode.code() >= 400;
    }

    @Override
    public boolean isOk() {
        int ic = httpCode.code();
        return
                ic >= 200
                        && ic < 300
                ;
    }

    @Override
    public NWebResponse ifErrorThrow() {
        if (isError()) {
            throw new NWebResponseException(msg, msgCode, httpCode);
        }
        return this;
    }

    public boolean isClientError() {
        int code = httpCode.code();

        return code >= 400 && code < 500;
    }

    public boolean isServerError() {
        int code = httpCode.code();
        return code >= 500;
    }

    public boolean isRedirect() {
        int code = httpCode.code();
        return code >= 300 && code < 400;
    }

    public NMsgCode userMessage() {
        return msgCode;
    }

    public NWebResponse userMessage(NMsgCode msgCode) {
        this.msgCode = msgCode;
        return this;
    }

    @Override
    public String contentType() {
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
