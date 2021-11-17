package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.NutsTypedFiltersParser;

public class NutsDependencyFilterParser extends NutsTypedFiltersParser<NutsDependencyFilter> {
    public NutsDependencyFilterParser(String str, NutsSession session) {
        super(str,session);
    }

    @Override
    protected NutsDependencyFilters getTManager() {
        return NutsDependencyFilters.of(getSession());
    }

    protected NutsDependencyFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
