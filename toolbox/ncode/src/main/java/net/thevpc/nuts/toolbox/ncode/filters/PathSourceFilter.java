package net.thevpc.nuts.toolbox.ncode.filters;

import net.thevpc.nuts.toolbox.ncode.Source;
import net.thevpc.nuts.toolbox.ncode.SourceFilter;
import net.thevpc.nuts.toolbox.ncode.bundles.strings.StringComparator;

import java.util.List;

public class PathSourceFilter implements SourceFilter {

    private final List<StringComparator> files;

    public PathSourceFilter(List<StringComparator> files) {
        this.files = files;
    }
    private boolean matchesFile(String s){
        if(files.isEmpty()){
            return true;
        }
        for (StringComparator file : files) {
            if(file.matches(s)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean accept(Source source) {
        return matchesFile(source.getExternalPath());
    }

    @Override
    public boolean lookInto(Source source) {
        return true;
    }
}
