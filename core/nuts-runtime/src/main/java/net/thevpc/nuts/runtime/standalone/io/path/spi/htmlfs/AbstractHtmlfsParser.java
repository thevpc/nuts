package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.concurrent.NScorableCallable;

import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractHtmlfsParser implements HtmlfsParser{

    public AbstractHtmlfsParser() {
    }

    protected NScorableCallable<List<String>> toSupported(int level, List<String> li) {
        Supplier<NMsg> msg = () -> NMsg.ofInvalidValue("htmlfs list");
        if (li == null || li.isEmpty()) {
            return NScorableCallable.ofInvalid(msg);
        }
        return NScorableCallable.of(level, () -> li,msg);
    }

}
