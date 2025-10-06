package net.thevpc.nuts.runtime.standalone.repository.filter;

import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.core.NRepositoryFilters;
import net.thevpc.nuts.runtime.standalone.util.filters.NTypedFiltersParser;

public class NRepositoryFilterParser extends NTypedFiltersParser<NRepositoryFilter> {
    public NRepositoryFilterParser(String str) {
        super(str);
    }

    @Override
    protected NRepositoryFilters getTManager() {
        return NRepositoryFilters.of();
    }

    protected NRepositoryFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }


}
