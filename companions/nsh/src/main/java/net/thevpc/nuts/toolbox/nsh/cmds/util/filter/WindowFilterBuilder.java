package net.thevpc.nuts.toolbox.nsh.cmds.util.filter;

import net.thevpc.nuts.toolbox.nsh.cmds.util.WindowFilter;

import java.util.ArrayList;
import java.util.List;

public class WindowFilterBuilder<T> {
    private List<AndWindowFilter<T>> all=new ArrayList<>();
    private boolean and=true;

    public boolean isEmpty(){
        return all.isEmpty();
    }

    public void and(){
        this.and=true;
    }
    public void or(){
        this.and=true;
    }
    public void add(WindowFilter<T> a){
        if(and){
            and(a);
        }else{
            or(a);
        }
    }

    public void and(WindowFilter<T> a){
        if(a!=null) {
            if (all.isEmpty()) {
                all.add(new AndWindowFilter<T>());
            }
            all.get(all.size() - 1).and(a);
        }
    }

    public void or(WindowFilter<T> a){
        if(a!=null) {
            all.add(new AndWindowFilter<T>());
            and(a);
        }
    }

    public WindowFilter<T> build(){
        if(all.isEmpty()){
            return new AndWindowFilter<T>();
        }
        if(all.size()==1){
            return all.get(0);
        }
        OrWindowFilter<T> o = new OrWindowFilter<T>();
        for (AndWindowFilter<T> i : all) {
            o.or(i);
        }
        return o;
    }
}
