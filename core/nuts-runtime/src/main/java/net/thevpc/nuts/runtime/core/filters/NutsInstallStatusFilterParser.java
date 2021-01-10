package net.thevpc.nuts.runtime.core.filters;

import net.thevpc.nuts.NutsInstallStatus;
import net.thevpc.nuts.NutsInstallStatusFilter;
import net.thevpc.nuts.NutsPredicates;

import java.util.function.Predicate;

public class NutsInstallStatusFilterParser extends AbstractFilterParser2<Predicate<NutsInstallStatus>>{
    public NutsInstallStatusFilterParser(String str) {
        super(str);
        addBoolOps();
    }



    protected Predicate<NutsInstallStatus> nextDefault(){
        return NutsInstallStatusFilter.ANY;
    }

    @Override
    protected Predicate<NutsInstallStatus> buildPreOp(String op, Predicate<NutsInstallStatus> a) {
        if (a == null) {
            //error
            return NutsPredicates.never();
        }
        return a.negate();
    }

    protected Predicate<NutsInstallStatus> buildBinOp(String op, Predicate<NutsInstallStatus> a, Predicate<NutsInstallStatus> r) {
        if(r==null){
            throw new IllegalArgumentException("expected second operand");
        }
        switch (op){
            case "&":
            case "&&":{
                return a.and(r);
            }
            case "|":
            case "||":{
                return a.or(r);
            }
        }
        throw new IllegalArgumentException("Unexpected bin op "+op);
    }

    protected Predicate<NutsInstallStatus> worldToPredicate(String word){
        switch (word.toLowerCase()){
            case "any":
            case "always":
                return NutsInstallStatusFilter.ANY;
            case "none":
            case "never":
                return NutsPredicates.never();
            case "installed":return NutsInstallStatusFilter.INSTALLED;
            case "default":
            case "defaultvalue":
                return NutsInstallStatusFilter.DEFAULT_VALUE;
            case "required":
                return NutsInstallStatusFilter.REQUIRED;
            case "obsolete":
                return NutsInstallStatusFilter.OBSOLETE;
            default:{
                throw new IllegalArgumentException("unsupported predicate name "+word);
            }
        }
    }

    public static Predicate<NutsInstallStatus> parse(String s){
        if(s==null){
            s="";
        }
        NutsInstallStatusFilterParser x = new NutsInstallStatusFilterParser(s);
        return x.parse();
    }
}
