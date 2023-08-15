package net.thevpc.nuts.toolbox.noapi.model;

import net.thevpc.nuts.util.NMsg;

import java.util.Map;

public class Vars {
    private Map<String, String> m;

    public Vars(Map<String, String> m) {
        this.m = m;
    }

    public void putDefault(String a,String b) {
        if(!m.containsKey(a)){
            m.put(a,b);
        }
    }

    public String format(String a) {
        return NMsg.ofV(a, s -> m.get(s)).toString();
    }
}
