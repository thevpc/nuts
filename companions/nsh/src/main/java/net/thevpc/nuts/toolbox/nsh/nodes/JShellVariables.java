package net.thevpc.nuts.toolbox.nsh.nodes;

import net.thevpc.nuts.toolbox.nsh.jshell.JShellContext;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellVarListener;

import java.util.*;

/**
 * Created by vpc on 11/4/16.
 */
public class JShellVariables {

    private Map<String, JShellVar> vars = new HashMap<>();
    private List<JShellVarListener> listeners = new ArrayList<>();
    private JShellContext shellContext;

    public JShellVariables(JShellContext shellContext) {
        this.shellContext=shellContext;
    }

    public void addVarListener(JShellVarListener listener) {
        this.listeners.add(listener);
    }

    public void removeVarListener(JShellVarListener listener) {
        this.listeners.add(listener);
    }

    public JShellVarListener[] getVarListeners() {
        return listeners.toArray(new JShellVarListener[0]);
    }

    public void setParent(JShellVariables parent) {
        if (parent != null) {
            for (JShellVar value : parent.vars.values()) {
                this.vars.put(value.getName(), new JShellVar(this, value.getName(), value.getValue(), value.isExported()));
            }
        }
    }

    public JShellVar getVar(String name) {
        JShellVar v = findVar(name);
        if (v == null) {
            throw new NoSuchElementException("not found " + name);
        }
        return v;
    }

    public JShellVar findVar(String name) {
        JShellVar t = vars.get(name);
        if (t != null) {
            return t;
        }
        return null;
    }

    public String get(String name) {
        return get(name, null);
    }

    public String get(String name, String defaultValue) {
        JShellVar v = findVar(name);
        if (v != null) {
            return v.getValue();
        }
        return defaultValue;
    }

    public Properties getExported() {
        Properties all = new Properties();
        for (JShellVar value : vars.values()) {
            if (value.isExported()) {
                all.put(value.getName(), value.getValue());
            }
        }
        return all;
    }

    public Properties getAll() {
        Properties all = new Properties();
        for (JShellVar value : vars.values()) {
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
        JShellVar b = findVar(var);
        if (value == null) {
            value = var;
        }
        if (b == null) {
            vars.put(var, new JShellVar(this, var, value, true));
        } else {
            b.setValue(value);
            b.setExported(true);
        }
    }

    public void set(String var, String value) {
        set(var, value, false);
    }

    public void set(String var, String value, boolean defaultExport) {
        JShellVar b = findVar(var);
        if (b == null && value == null) {
            return;
        }
        if (b == null) {
            JShellVar jvar = new JShellVar(this, var, value, defaultExport);
            vars.put(var, jvar);
            for (JShellVarListener listener : getVarListeners()) {
                listener.varAdded(jvar,this,shellContext);
            }
            for (JShellVarListener listener : shellContext.getShell().getVarListeners()) {
                listener.varAdded(jvar,this,shellContext);
            }
        } else {
            String oldValue = b.getValue();
            if(!Objects.equals(oldValue,value)) {
                b.setValue(value);
                for (JShellVarListener listener : getVarListeners()) {
                    listener.varValueUpdated(b,oldValue,this,shellContext);
                }
                for (JShellVarListener listener : shellContext.getShell().getVarListeners()) {
                    listener.varValueUpdated(b,oldValue,this,shellContext);
                }
            }
        }
    }

    public void export(String var) {
        JShellVar b = findVar(var);
        if (b == null) {
            set(var, var, true);
        } else {
            if (!b.isExported()) {
                b.setExported(true);
                for (JShellVarListener listener : getVarListeners()) {
                    listener.varExportUpdated(b,false,this,shellContext);
                }
                for (JShellVarListener listener : shellContext.getShell().getVarListeners()) {
                    listener.varExportUpdated(b,false,this,shellContext);
                }
            }
        }
    }

    public void unexport(String var) {
        if (vars.containsKey(var)) {
            JShellVar jvar = getVar(var);
            if(jvar.isExported()) {
                jvar.setExported(false);
            }
            for (JShellVarListener listener : getVarListeners()) {
                listener.varExportUpdated(jvar,false,this,shellContext);
            }
            for (JShellVarListener listener : shellContext.getShell().getVarListeners()) {
                listener.varExportUpdated(jvar,false,this,shellContext);
            }
        } else {
            throw new NoSuchElementException("Unable to unexport env var " + var + " . Not found");
        }
    }

    public boolean isExported(String var) {
        JShellVar v = findVar(var);
        return v != null && v.isExported();
    }

    public void set(JShellVariables other) {
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

    void varValueChanged(JShellVar svar, String oldValue) {
        if (svar.getValue() == null) {
            vars.remove(svar.getName());
        }
    }

    void varEnabledChanged(JShellVar aThis) {

    }

}
