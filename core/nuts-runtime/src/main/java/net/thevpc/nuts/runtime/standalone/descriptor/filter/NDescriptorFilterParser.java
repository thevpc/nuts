package net.thevpc.nuts.runtime.standalone.descriptor.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.NTypedFiltersParser;

public class NDescriptorFilterParser extends NTypedFiltersParser<NDescriptorFilter> {
    public NDescriptorFilterParser(String str, NSession session) {
        super(str,session);
    }

    @Override
    protected NDescriptorFilters getTManager() {
        return NDescriptorFilters.of(getSession());
    }

    protected NDescriptorFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
