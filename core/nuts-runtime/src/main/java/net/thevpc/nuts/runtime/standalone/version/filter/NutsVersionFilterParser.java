package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.NutsTypedFiltersParser;

public class NutsVersionFilterParser extends NutsTypedFiltersParser<NutsVersionFilter> {
    public NutsVersionFilterParser(String str, NutsSession session) {
        super(str,session);
    }

    @Override
    protected NutsVersionFilters getTManager() {
        return NutsVersionFilters.of(getSession());
    }

    protected NutsVersionFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
