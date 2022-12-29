package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.NSupported;

import java.util.List;

public abstract class AbstractHtmlfsParser implements HtmlfsParser{
    protected NSupported<List<String>> toSupported(int level, List<String> li) {
        if (li == null || li.isEmpty()) {
            return NSupported.invalid();
        }
        return NSupported.of(level, () -> li);
    }

}
