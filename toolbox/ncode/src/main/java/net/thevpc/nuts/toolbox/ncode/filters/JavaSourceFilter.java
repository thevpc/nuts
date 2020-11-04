package net.thevpc.nuts.toolbox.ncode.filters;

import net.thevpc.nuts.toolbox.ncode.sources.JavaTypeSource;
import net.thevpc.common.strings.StringComparator;
import net.thevpc.nuts.toolbox.ncode.Source;
import net.thevpc.nuts.toolbox.ncode.SourceFilter;

public class JavaSourceFilter implements SourceFilter {

    private final StringComparator type;
    private final StringComparator file;

    public JavaSourceFilter(StringComparator type, StringComparator file) {
        this.type = type;
        this.file = file;
    }

    @Override
    public boolean accept(Source source) {
        if (source instanceof JavaTypeSource) {
            JavaTypeSource s = (JavaTypeSource) source;
            if (file == null || file.matches(source.getExternalPath())) {
                String n = s.getClassName();
                if (type == null || type.matches(n)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean lookInto(Source source) {
        return true;
    }
}
