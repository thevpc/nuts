package net.thevpc.nuts.runtime.standalone.repository.filter;

import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.internal.rpi.NRepositoryFilterRPI;
import net.thevpc.nuts.runtime.standalone.util.filters.NTypedFiltersParser;

public class NRepositoryFilterParser extends NTypedFiltersParser<NRepositoryFilter> {
    public NRepositoryFilterParser(String str) {
        super(str);
    }

    @Override
    protected NRepositoryFilterRPI getTManager() {
        return NRepositoryFilterRPI.of();
    }

    protected NRepositoryFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }


}
