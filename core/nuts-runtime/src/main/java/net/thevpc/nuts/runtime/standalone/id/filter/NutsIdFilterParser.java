package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.NutsTypedFiltersParser;

public class NutsIdFilterParser extends NutsTypedFiltersParser<NutsIdFilter> {
    public NutsIdFilterParser(String str, NutsSession session) {
        super(str,session);
    }

    @Override
    protected NutsIdFilters getTManager() {
        return NutsIdFilters.of(getSession());
    }

    protected NutsIdFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
