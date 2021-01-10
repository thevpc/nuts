package net.thevpc.nuts.runtime.core.filters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.AbstractFilterParser2;

public abstract class NutsTypedFiltersParser<T extends NutsFilter> extends AbstractFilterParser2<T> {
    protected NutsWorkspace ws;
    public NutsTypedFiltersParser(String str, NutsWorkspace ws) {
        super(str==null?"":str);
        this.ws=ws;
        addBoolOps();
    }



    protected T nextDefault(){
        return getTManager().always();
    }

    protected abstract NutsTypedFilters<T> getTManager();

    @Override
    protected T buildPreOp(String op, T a) {
        if (a == null) {
            //error
            return getTManager().never();
        }
        return (T) a.neg();
    }

    protected T buildBinOp(String op, T a, T r) {
        if(r==null){
            throw new IllegalArgumentException("expected second operand");
        }
        switch (op){
            case "&":
            case "&&":{
                return (T) a.and(r);
            }
            case "|":
            case "||":{
                return (T) a.or(r);
            }
        }
        throw new IllegalArgumentException("unexpected bin op "+op);
    }

    protected T worldToPredicate(String word){
        switch (word.toLowerCase()){
            case "any":
            case "always":
                return getTManager().always();
            case "none":
            case "never":
                return getTManager().never();
            default:{
                throw new IllegalArgumentException("unsupported predicate name "+word);
            }
        }
    }
}
