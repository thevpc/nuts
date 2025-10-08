package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import net.thevpc.nuts.util.NException;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.text.NMsg;

import java.io.UncheckedIOException;
import java.util.Locale;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.net.NWebCli;
import net.thevpc.nuts.net.NWebRequest;

public class SimpleRecommendationConnector extends AbstractRecommendationConnector {
    public SimpleRecommendationConnector() {
        super();
    }

    @Override
    public <T> T post(String url, RequestQueryInfo ri, Class<T> resultType) {
        validateRequest(ri);
        try {
            NWebCli cli = NWebCli.of();
            cli.setConnectTimeout(500);
            cli.setReadTimeout(500);
            NWebRequest post = cli.POST(ri.server + url)
                    .setContentType("application/json; charset=UTF-8")
                    .setHeader("Accept", "*/*");
            String loc = NSession.of().getLocale().orDefault();
            if (loc == null) {
                loc = Locale.getDefault().toString();
            }
            post.setHeader("Accept-Language", loc);
            String out = NElementWriter.ofJson().toString(ri.q);
            post.setRequestBody(out.getBytes());
            return post.run().getContentAs(resultType, NContentType.JSON);
        } catch (NException ex) {
            throw ex;
        } catch (UncheckedIOException ex) {
            throw new NIOException(NMsg.ofC("recommendations are not available : %s", ex.toString()), ex);
        } catch (Exception ex) {
            throw new NIllegalArgumentException(NMsg.ofC("unexpected error : %s", ex.toString()), ex);
        }
    }

}
