package net.thevpc.nuts.runtime.core.filters.descriptor;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.NutsTypedFiltersParser;

public class NutsDescriptorFilterParser extends NutsTypedFiltersParser<NutsDescriptorFilter> {
    public NutsDescriptorFilterParser(String str, NutsWorkspace ws) {
        super(str,ws);
    }

    @Override
    protected NutsDescriptorFilterManager getTManager() {
        return ws.filters().descriptor();
    }

    protected NutsDescriptorFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
