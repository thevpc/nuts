package net.thevpc.nuts.runtime.standalone.shell;

public class ZshNutsShellHelper extends ShNutsShellHelper {
    public static final NutsShellHelper ZSH = new ZshNutsShellHelper();

    public ZshNutsShellHelper() {
    }

    public String getSysRcName() {
        return ".zshenv";
    }
}
