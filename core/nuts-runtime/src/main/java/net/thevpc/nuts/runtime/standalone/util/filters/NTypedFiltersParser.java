package net.thevpc.nuts.runtime.standalone.util.filters;

import net.thevpc.nuts.artifact.NTypedFilters;
import net.thevpc.nuts.util.NFilter;

public abstract class NTypedFiltersParser<T extends NFilter> extends AbstractFilterParser2<T> {
    public NTypedFiltersParser(String str) {
        super(str==null?"":str);
        addBoolOps();
    }

    protected T nextDefault(){
        return getTManager().always();
    }

    protected abstract NTypedFilters<T> getTManager();

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

    protected T wordToPredicate(String word){
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
