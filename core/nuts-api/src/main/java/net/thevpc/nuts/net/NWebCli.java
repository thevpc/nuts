package net.thevpc.nuts.net;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NGetter;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface NWebCli extends NComponent {

    static NWebCli of() {
        return NExtensions.of(NWebCli.class);
    }

    Function<NWebResponse, NWebResponse> responsePostProcessor();

    NWebCli responsePostProcessor(Function<NWebResponse, NWebResponse> responsePostProcessor);

    @NGetter
    String prefix();

    NWebCli prefix(String prefix);

    NWebRequest req(NHttpMethod method);

    @NGetter
    List<NWebCookie> cookies();

    NWebCli header(String name, String value);

    NWebCli addHeader(String name, String value);

    NWebCli removeHeader(String name, String value);

    NWebCli removeHeader(String name);

    boolean containsHeader(String name);

    @NGetter
    Map<String, List<String>> headers();

    NWebCli clearHeaders();

    NWebCli clearCookies();

    NWebCli removeCookies(NWebCookie[] cookies);

    NWebCli removeCookie(NWebCookie cookie);

    boolean containsCookie(String cookieName);

    NWebCli removeCookie(String cookieName);

    NWebCli addCookies(NWebCookie... cookies);

    NWebCli addCookie(NWebCookie cookie);

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

    NDuration readTimeout();

    NWebCli timeout(NDuration timeout);

    NWebCli readTimeout(NDuration readTimeout);

    NDuration connectTimeout();

    NWebCli connectTimeout(NDuration connectTimeout);
}
