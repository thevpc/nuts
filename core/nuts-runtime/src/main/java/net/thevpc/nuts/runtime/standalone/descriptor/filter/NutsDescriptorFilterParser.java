package net.thevpc.nuts.runtime.standalone.descriptor.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.NutsTypedFiltersParser;

public class NutsDescriptorFilterParser extends NutsTypedFiltersParser<NutsDescriptorFilter> {
    public NutsDescriptorFilterParser(String str, NutsSession session) {
        super(str,session);
    }

    @Override
    protected NutsDescriptorFilters getTManager() {
        return NutsDescriptorFilters.of(getSession());
    }

    protected NutsDescriptorFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
