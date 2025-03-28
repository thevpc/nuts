package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.NCallableSupport;

import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractHtmlfsParser implements HtmlfsParser{

    public AbstractHtmlfsParser() {
    }

    protected NCallableSupport<List<String>> toSupported(int level, List<String> li) {
        Supplier<NMsg> msg = () -> NMsg.ofInvalidValue("htmlfs list");
        if (li == null || li.isEmpty()) {
            return NCallableSupport.invalid(msg);
        }
        return NCallableSupport.of(level, () -> li,msg);
    }

}
