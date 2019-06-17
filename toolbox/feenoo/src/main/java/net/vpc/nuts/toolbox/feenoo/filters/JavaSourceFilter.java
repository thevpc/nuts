package net.vpc.nuts.toolbox.feenoo.filters;

import net.vpc.common.strings.StringComparator;
import net.vpc.nuts.toolbox.feenoo.Source;
import net.vpc.nuts.toolbox.feenoo.SourceFilter;
import net.vpc.nuts.toolbox.feenoo.sources.JavaTypeSource;

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
