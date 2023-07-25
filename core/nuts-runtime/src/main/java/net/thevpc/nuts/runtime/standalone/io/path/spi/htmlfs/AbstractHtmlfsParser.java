package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NCallableSupport;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractHtmlfsParser implements HtmlfsParser{
    protected NCallableSupport<List<String>> toSupported(int level, List<String> li) {
        Function<NSession, NMsg> msg = session -> NMsg.ofInvalidValue("htmlfs list");
        if (li == null || li.isEmpty()) {
            return NCallableSupport.invalid(msg);
        }
        return NCallableSupport.of(level, () -> li,msg);
    }

}
