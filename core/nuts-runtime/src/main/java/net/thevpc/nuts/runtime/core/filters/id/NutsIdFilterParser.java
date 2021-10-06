package net.thevpc.nuts.runtime.core.filters.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.NutsTypedFiltersParser;

public class NutsIdFilterParser extends NutsTypedFiltersParser<NutsIdFilter> {
    public NutsIdFilterParser(String str, NutsSession session) {
        super(str,session);
    }

    @Override
    protected NutsIdFilterManager getTManager() {
        return getSession().filters().id();
    }

    protected NutsIdFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
