package net.vpc.nuts.toolbox.feenoo.filters;

import net.vpc.common.strings.StringComparator;
import net.vpc.nuts.toolbox.feenoo.Source;
import net.vpc.nuts.toolbox.feenoo.SourceFilter;

public class PathSourceFilter implements SourceFilter {
    private final StringComparator comparator;

    public PathSourceFilter(StringComparator comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean accept(Source source) {
        if (comparator.matches(source.getExternalPath())) {
            return true;

        }
        return false;
    }

    @Override
    public boolean lookInto(Source source) {
        return true;
    }
}
