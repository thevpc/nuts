package net.thevpc.nuts.runtime.standalone.shell;

public class ZshNShellHelper extends ShNShellHelper {
    public static final NShellHelper ZSH = new ZshNShellHelper();

    public ZshNShellHelper() {
    }

    public String getSysRcName() {
        return ".zshenv";
    }
}
