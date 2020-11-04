package net.thevpc.nuts.toolbox.ncode.filters;

import net.thevpc.common.strings.StringComparator;
import net.thevpc.nuts.toolbox.ncode.Source;
import net.thevpc.nuts.toolbox.ncode.SourceFilter;

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
