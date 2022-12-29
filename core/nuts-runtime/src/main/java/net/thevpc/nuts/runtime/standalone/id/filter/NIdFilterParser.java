package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.NTypedFiltersParser;

public class NIdFilterParser extends NTypedFiltersParser<NIdFilter> {
    public NIdFilterParser(String str, NSession session) {
        super(str,session);
    }

    @Override
    protected NIdFilters getTManager() {
        return NIdFilters.of(getSession());
    }

    protected NIdFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
