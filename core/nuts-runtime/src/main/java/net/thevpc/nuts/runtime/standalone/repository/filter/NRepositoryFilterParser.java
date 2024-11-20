package net.thevpc.nuts.runtime.standalone.repository.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.NTypedFiltersParser;

public class NRepositoryFilterParser extends NTypedFiltersParser<NRepositoryFilter> {
    public NRepositoryFilterParser(String str, NWorkspace workspace) {
        super(str,workspace);
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
