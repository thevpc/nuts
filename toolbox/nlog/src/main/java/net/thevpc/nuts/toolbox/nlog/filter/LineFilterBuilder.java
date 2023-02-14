package net.thevpc.nuts.toolbox.nlog.filter;

import net.thevpc.nuts.toolbox.nlog.model.LineFilter;

import java.util.ArrayList;
import java.util.List;

public class LineFilterBuilder {
    private List<AndLineFilter> all=new ArrayList<>();
    private boolean and=true;

    public void and(){
        this.and=true;
    }
    public void or(){
        this.and=true;
    }
    public void add(LineFilter a){
        if(and){
            and(a);
        }else{
            or(a);
        }
    }

    public void and(LineFilter a){
        if(a!=null) {
            if (all.isEmpty()) {
                all.add(new AndLineFilter());
            }
            all.get(all.size() - 1).and(a);
        }
    }

    public void or(LineFilter a){
        if(a!=null) {
            all.add(new AndLineFilter());
            and(a);
        }
    }

    public LineFilter build(){
        if(all.isEmpty()){
            return new AndLineFilter();
        }
        if(all.size()==1){
            return all.get(0);
        }
        OrLineFilter o = new OrLineFilter();
        for (AndLineFilter i : all) {
            o.or(i);
        }
        return o;
    }
}
