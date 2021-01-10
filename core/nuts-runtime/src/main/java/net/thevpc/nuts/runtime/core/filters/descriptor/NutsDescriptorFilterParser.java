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

    protected NutsDescriptorFilter worldToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.worldToPredicate(word);
            }
        }
    }
}
