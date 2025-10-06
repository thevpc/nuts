package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.artifact.NDefinitionFilters;
import net.thevpc.nuts.runtime.standalone.util.filters.NTypedFiltersParser;

public class NDefinitionFilterParser extends NTypedFiltersParser<NDefinitionFilter> {
    public NDefinitionFilterParser(String str) {
        super(str);
    }

    @Override
    protected NDefinitionFilters getTManager() {
        return NDefinitionFilters.of();
    }

    protected NDefinitionFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
