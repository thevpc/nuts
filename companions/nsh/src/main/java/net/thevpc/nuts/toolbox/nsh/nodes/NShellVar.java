package net.thevpc.nuts.toolbox.nsh.nodes;

import java.util.Objects;

/**
 * Created by vpc on 11/4/16.
 */
public class NShellVar {

    private final String name;
    private String value;
    private boolean exported;
    private NShellVariables parent;

    public NShellVar(NShellVariables parent, String name, String value, boolean exported) {
        this.name = name;
        this.parent = parent;
        this.value = value;
        this.exported = exported;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        String old = this.value;
        if (!Objects.equals(old, value)) {
            this.value = value;
            parent.varValueChanged(this, old);
        }
    }

    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        boolean old = this.exported;
        if (!Objects.equals(old, exported)) {
            this.exported = exported;
            parent.varEnabledChanged(this);
        }
    }

}
