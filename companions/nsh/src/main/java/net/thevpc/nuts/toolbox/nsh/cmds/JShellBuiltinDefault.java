package net.thevpc.nuts.toolbox.nsh.cmds;

import java.util.function.Supplier;

public abstract class JShellBuiltinDefault extends JShellBuiltinBase {
    public JShellBuiltinDefault(String name, int supportLevel, Class<?> optionsSupplier) {
        super(name, supportLevel, optionsSupplier);
    }

    public JShellBuiltinDefault(String name, Class<?> optionsSupplier) {
        super(name, optionsSupplier);
    }

    public JShellBuiltinDefault(String name, int supportLevel, Supplier<?> optionsSupplier) {
        super(name, supportLevel, optionsSupplier);
    }

    public JShellBuiltinDefault(String name, Supplier<?> optionsSupplier) {
        super(name, optionsSupplier);
    }
}
