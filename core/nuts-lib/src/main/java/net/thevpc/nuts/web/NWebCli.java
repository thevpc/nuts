package net.thevpc.nuts.web;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface NWebCli extends NComponent {
    static NWebCli of() {
        return NExtensions.of(NWebCli.class);
    }

    Function<NWebResponse, NWebResponse> getResponsePostProcessor();

    NWebCli setResponsePostProcessor(Function<NWebResponse, NWebResponse> responsePostProcessor);

    String getPrefix();

    NWebCli setPrefix(String prefix);

    NWebRequest req(NHttpMethod method);

    NWebCookie[] getCookies();


    NWebCli setHeader(String name, String value);

    NWebCli addHeader(String name, String value);

    NWebCli removeHeader(String name, String value);

    NWebCli removeHeader(String name);

    boolean containsHeader(String name);

    Map<String, List<String>> getHeaders();

    NWebCli clearHeaders();

    NWebCli clearCookies();

    NWebCli removeCookies(NWebCookie[] cookies);

    NWebCli removeCookie(NWebCookie cookie);

    boolean containsCookie(String cookieName);
    NWebCli removeCookie(String cookieName);

    NWebCli addCookies(NWebCookie[] cookies);

    NWebCli addCookie(NWebCookie cookie);

    NWebRequest req();

    NWebRequest GET();

    NWebRequest GET(String path);

    NWebRequest POST();

    NWebRequest POST(String path);

    NWebRequest PUT();

    NWebRequest PUT(String path);

    NWebRequest DELETE();

    NWebRequest DELETE(String path);

    NWebRequest PATCH();

    NWebRequest PATCH(String path);

    NWebRequest OPTIONS();

    NWebRequest OPTIONS(String path);

    NWebRequest HEAD();

    NWebRequest HEAD(String path);

    NWebRequest CONNECT();

    NWebRequest CONNECT(String path);

    NWebRequest TRACE();

    NWebRequest TRACE(String path);

    Integer getReadTimeout();

    NWebCli setReadTimeout(Integer readTimeout);

    Integer getConnectTimeout();

    NWebCli setConnectTimeout(Integer connectTimeout);
}
