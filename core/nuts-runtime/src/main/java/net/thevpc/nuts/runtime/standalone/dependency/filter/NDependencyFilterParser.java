package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.NTypedFiltersParser;

public class NDependencyFilterParser extends NTypedFiltersParser<NDependencyFilter> {
    public NDependencyFilterParser(String str, NWorkspace workspace) {
        super(str,workspace);
    }

    @Override
    protected NDependencyFilters getTManager() {
        return NDependencyFilters.of();
    }

    protected NDependencyFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
