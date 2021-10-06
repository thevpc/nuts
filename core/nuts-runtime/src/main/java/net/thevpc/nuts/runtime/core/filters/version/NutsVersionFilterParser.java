package net.thevpc.nuts.runtime.core.filters.version;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.NutsTypedFiltersParser;

public class NutsVersionFilterParser extends NutsTypedFiltersParser<NutsVersionFilter> {
    public NutsVersionFilterParser(String str, NutsSession session) {
        super(str,session);
    }

    @Override
    protected NutsVersionFilterManager getTManager() {
        return getSession().filters().version();
    }

    protected NutsVersionFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
