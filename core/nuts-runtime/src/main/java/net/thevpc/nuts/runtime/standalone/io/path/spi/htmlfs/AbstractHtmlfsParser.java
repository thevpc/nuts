package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.concurrent.NScoredCallable;

import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractHtmlfsParser implements HtmlfsParser{

    public AbstractHtmlfsParser() {
    }

    protected NScoredCallable<List<String>> toSupported(int level, List<String> li) {
        Supplier<NMsg> msg = () -> NMsg.ofInvalidValue("htmlfs list");
        if (li == null || li.isEmpty()) {
            return NScoredCallable.ofInvalid(msg);
        }
        return NScoredCallable.of(level, () -> li,msg);
    }

}
