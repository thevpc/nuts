package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import net.thevpc.nuts.NutsSupported;

import java.util.List;

public abstract class AbstractHtmlfsParser implements HtmlfsParser{
    protected NutsSupported<List<String>> toSupported(int level,List<String> li) {
        if (li == null || li.isEmpty()) {
            return NutsSupported.invalid();
        }
        return NutsSupported.of(level, () -> li);
    }

}
