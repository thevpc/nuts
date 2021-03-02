package net.thevpc.nuts.runtime.core.filters.dependency;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.NutsTypedFiltersParser;

public class NutsDependencyFilterParser extends NutsTypedFiltersParser<NutsDependencyFilter> {
    public NutsDependencyFilterParser(String str, NutsWorkspace ws) {
        super(str,ws);
    }

    @Override
    protected NutsDependencyFilterManager getTManager() {
        return ws.filters().dependency();
    }

    protected NutsDependencyFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
