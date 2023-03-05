package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.toolbox.nsh.cmds.JShellBuiltinBase;

import java.util.function.Supplier;

public abstract class JShellBuiltinCore extends JShellBuiltinBase {
    public JShellBuiltinCore(String name, int supportLevel, Class<?> optionsSupplier) {
        super(name, supportLevel, optionsSupplier);
    }

    public JShellBuiltinCore(String name, Class<?> optionsSupplier) {
        super(name, optionsSupplier);
    }

    public JShellBuiltinCore(String name, int supportLevel, Supplier<?> optionsSupplier) {
        super(name, supportLevel, optionsSupplier);
    }

    public JShellBuiltinCore(String name, Supplier<?> optionsSupplier) {
        super(name, optionsSupplier);
    }
}
