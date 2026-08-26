package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.artifact.NIdFilter;
import net.thevpc.nuts.internal.rpi.NIdFilterRPI;
import net.thevpc.nuts.runtime.standalone.util.filters.NTypedFiltersParser;

public class NIdFilterParser extends NTypedFiltersParser<NIdFilter> {
    public NIdFilterParser(String str) {
        super(str);
    }

    @Override
    protected NIdFilterRPI getTManager() {
        return NIdFilterRPI.of();
    }

    protected NIdFilter wordToPredicate(String word){
        switch (word.toLowerCase()){
            default:{
                return super.wordToPredicate(word);
            }
        }
    }
}
