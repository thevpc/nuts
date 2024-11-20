package net.thevpc.nuts.web;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.util.function.Function;

public interface NWebCli extends NComponent {
    static NWebCli of() {
        return NExtensions.of().createComponent(NWebCli.class).get();
    }

    Function<NWebResponse, NWebResponse> getResponsePostProcessor();

    NWebCli setResponsePostProcessor(Function<NWebResponse, NWebResponse> responsePostProcessor);

    String getPrefix();

    NWebCli setPrefix(String prefix);

    NWebRequest req(NHttpMethod method);

    NWebRequest req();

    NWebRequest get();

    NWebRequest post();

    NWebRequest put();

    NWebRequest delete();

    NWebRequest patch();

    NWebRequest options();

    NWebRequest head();

    NWebRequest connect();

    NWebRequest trace();

    Integer getReadTimeout();

    NWebCli setReadTimeout(Integer readTimeout);

    Integer getConnectTimeout();

    NWebCli setConnectTimeout(Integer connectTimeout);
}
