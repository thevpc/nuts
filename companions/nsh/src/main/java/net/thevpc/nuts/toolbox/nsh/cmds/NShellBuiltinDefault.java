package net.thevpc.nuts.toolbox.nsh.cmds;

import java.util.function.Supplier;

public abstract class NShellBuiltinDefault extends NShellBuiltinBase {
    public NShellBuiltinDefault(String name, int supportLevel, Class<?> optionsSupplier) {
        super(name, supportLevel, optionsSupplier);
    }

    public NShellBuiltinDefault(String name, Class<?> optionsSupplier) {
        super(name, optionsSupplier);
    }

    public NShellBuiltinDefault(String name, int supportLevel, Supplier<?> optionsSupplier) {
        super(name, supportLevel, optionsSupplier);
    }

    public NShellBuiltinDefault(String name, Supplier<?> optionsSupplier) {
        super(name, optionsSupplier);
    }
}
