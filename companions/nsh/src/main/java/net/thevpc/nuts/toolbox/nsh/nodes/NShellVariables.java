package net.thevpc.nuts.toolbox.nsh.nodes;

import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;

import java.util.*;

/**
 * Created by vpc on 11/4/16.
 */
public class NShellVariables {

    private Map<String, NShellVar> vars = new HashMap<>();
    private List<NShellVarListener> listeners = new ArrayList<>();
    private NShellContext shellContext;

    public NShellVariables(NShellContext shellContext) {
        this.shellContext=shellContext;
    }

    public void addVarListener(NShellVarListener listener) {
        this.listeners.add(listener);
    }

    public void removeVarListener(NShellVarListener listener) {
        this.listeners.add(listener);
    }

    public NShellVarListener[] getVarListeners() {
        return listeners.toArray(new NShellVarListener[0]);
    }

    public void setParent(NShellVariables parent) {
        if (parent != null) {
            for (NShellVar value : parent.vars.values()) {
                this.vars.put(value.getName(), new NShellVar(this, value.getName(), value.getValue(), value.isExported()));
            }
        }
    }

    public NShellVar getVar(String name) {
        NShellVar v = findVar(name);
        if (v == null) {
            throw new NoSuchElementException("not found " + name);
        }
        return v;
    }

    public NShellVar findVar(String name) {
        NShellVar t = vars.get(name);
        if (t != null) {
            return t;
        }
        return null;
    }

    public String get(String name) {
        return get(name, null);
    }

    public String get(String name, String defaultValue) {
        NShellVar v = findVar(name);
        if (v != null) {
            return v.getValue();
        }
        return defaultValue;
    }

    public Properties getExported() {
        Properties all = new Properties();
        for (NShellVar value : vars.values()) {
            if (value.isExported()) {
                all.put(value.getName(), value.getValue());
            }
        }
        return all;
    }

    public Properties getAll() {
        Properties all = new Properties();
        for (NShellVar value : vars.values()) {
            all.put(value.getName(), value.getValue());
        }
        return all;
    }

    public void set(Map<String, String> env) {
        for (Map.Entry<String, String> entry : env.entrySet()) {
            String key = (String) entry.getKey();
            set(key, (String) entry.getValue());
            if (vars.containsKey(key)) {
                export(key);
            }
        }
    }

    public void export(String var, String value) {
        NShellVar b = findVar(var);
        if (value == null) {
            value = var;
        }
        if (b == null) {
            vars.put(var, new NShellVar(this, var, value, true));
        } else {
            b.setValue(value);
            b.setExported(true);
        }
    }

    public void set(String var, String value) {
        set(var, value, false);
    }

    public void set(String var, String value, boolean defaultExport) {
        NShellVar b = findVar(var);
        if (b == null && value == null) {
            return;
        }
        if (b == null) {
            NShellVar jvar = new NShellVar(this, var, value, defaultExport);
            vars.put(var, jvar);
            for (NShellVarListener listener : getVarListeners()) {
                listener.varAdded(jvar,this,shellContext);
            }
            for (NShellVarListener listener : shellContext.getShell().getVarListeners()) {
                listener.varAdded(jvar,this,shellContext);
            }
        } else {
            String oldValue = b.getValue();
            if(!Objects.equals(oldValue,value)) {
                b.setValue(value);
                for (NShellVarListener listener : getVarListeners()) {
                    listener.varValueUpdated(b,oldValue,this,shellContext);
                }
                for (NShellVarListener listener : shellContext.getShell().getVarListeners()) {
                    listener.varValueUpdated(b,oldValue,this,shellContext);
                }
            }
        }
    }

    public void export(String var) {
        NShellVar b = findVar(var);
        if (b == null) {
            set(var, var, true);
        } else {
            if (!b.isExported()) {
                b.setExported(true);
                for (NShellVarListener listener : getVarListeners()) {
                    listener.varExportUpdated(b,false,this,shellContext);
                }
                for (NShellVarListener listener : shellContext.getShell().getVarListeners()) {
                    listener.varExportUpdated(b,false,this,shellContext);
                }
            }
        }
    }

    public void unexport(String var) {
        if (vars.containsKey(var)) {
            NShellVar jvar = getVar(var);
            if(jvar.isExported()) {
                jvar.setExported(false);
            }
            for (NShellVarListener listener : getVarListeners()) {
                listener.varExportUpdated(jvar,false,this,shellContext);
            }
            for (NShellVarListener listener : shellContext.getShell().getVarListeners()) {
                listener.varExportUpdated(jvar,false,this,shellContext);
            }
        } else {
            throw new NoSuchElementException("Unable to unexport env var " + var + " . Not found");
        }
    }

    public boolean isExported(String var) {
        NShellVar v = findVar(var);
        return v != null && v.isExported();
    }

    public void set(NShellVariables other) {
        for (Map.Entry<Object, Object> entry : other.getAll().entrySet()) {
            String key = (String) entry.getKey();
            set(key, (String) entry.getValue());
            if (other.isExported(key)) {
                export(key);
            }
        }
    }

    public void clear() {

    }

    void varValueChanged(NShellVar svar, String oldValue) {
        if (svar.getValue() == null) {
            vars.remove(svar.getName());
        }
    }

    void varEnabledChanged(NShellVar aThis) {

    }

}
