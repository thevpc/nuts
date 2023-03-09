package net.thevpc.nuts.toolbox.nsh.cmds;

import java.util.function.Supplier;

public abstract class NShellBuiltinCore extends NShellBuiltinBase {
    public NShellBuiltinCore(String name, int supportLevel, Class<?> optionsSupplier) {
        super(name, supportLevel, optionsSupplier);
    }

    public NShellBuiltinCore(String name, Class<?> optionsSupplier) {
        super(name, optionsSupplier);
    }

    public NShellBuiltinCore(String name, int supportLevel, Supplier<?> optionsSupplier) {
        super(name, supportLevel, optionsSupplier);
    }

    public NShellBuiltinCore(String name, Supplier<?> optionsSupplier) {
        super(name, optionsSupplier);
    }
}
