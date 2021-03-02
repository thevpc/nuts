package net.thevpc.nuts.runtime.core.filters.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.NutsTypedFiltersParser;

public class NutsIdFilterParser extends NutsTypedFiltersParser<NutsIdFilter> {
    public NutsIdFilterParser(String str, NutsWorkspace ws) {
        super(str,ws);
    }

    @Override
    protected NutsIdFilterManager getTManager() {
        return ws.filters().id();
    }

    protected NutsIdFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
