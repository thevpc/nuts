package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.artifact.NIdFilter;
import net.thevpc.nuts.artifact.NIdFilters;
import net.thevpc.nuts.runtime.standalone.util.filters.NTypedFiltersParser;

public class NIdFilterParser extends NTypedFiltersParser<NIdFilter> {
    public NIdFilterParser(String str) {
        super(str);
    }

    @Override
    protected NIdFilters getTManager() {
        return NIdFilters.of();
    }

    protected NIdFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
