package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NSupported;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractHtmlfsParser implements HtmlfsParser{
    protected NSupported<List<String>> toSupported(int level, List<String> li) {
        Function<NSession, NMsg> msg = session -> NMsg.ofInvalidValue("htmlfs list");
        if (li == null || li.isEmpty()) {
            return NSupported.invalid(msg);
        }
        return NSupported.of(level, () -> li,msg);
    }

}
