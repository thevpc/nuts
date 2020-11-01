package net.vpc.app.nuts;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NutsDefaultTerminalSpec implements NutsTerminalSpec {
    private Boolean autoComplete;
    private NutsTerminalBase parent;
    private NutsSession session;
    private final Map<String,Object> other=new HashMap<>();

    @Override
    public NutsTerminalBase getParent() {
        return parent;
    }

    @Override
    public NutsTerminalSpec setParent(NutsTerminalBase parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public Boolean getAutoComplete() {
        return autoComplete;
    }

    @Override
    public NutsTerminalSpec setAutoComplete(Boolean autoComplete) {
        this.autoComplete = autoComplete;
        return this;
    }

    @Override
    public Object get(String name){
        return other.get(name);
    }

    @Override
    public NutsTerminalSpec put(String name, Object o){
        other.put(name,o);
        return this;
    }

    @Override
    public NutsTerminalSpec copyFrom(NutsTerminalSpec other){
        this.autoComplete=other.getAutoComplete();
        putAll(other.getProperties());
        return this;
    }

    @Override
    public NutsTerminalSpec putAll(Map<String, Object> other){
        if(other!=null){
            for (Map.Entry<String, Object> e : other.entrySet()) {
                put(e.getKey(),e.getValue());
            }
        }
        return this;
    }

    @Override
    public Map<String, Object> getProperties() {
        return other;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsDefaultTerminalSpec that = (NutsDefaultTerminalSpec) o;
        return Objects.equals(autoComplete, that.autoComplete) && Objects.equals(parent, that.parent) && Objects.equals(session, that.session) && Objects.equals(other, that.other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(autoComplete, parent, session, other);
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsDefaultTerminalSpec setSession(NutsSession session) {
        this.session = session;
        return this;
    }
}
