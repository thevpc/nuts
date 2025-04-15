package net.thevpc.nuts.runtime.standalone.xtra.shell;

public class ZshNShellHelper extends ShNShellHelper {
    public static final NShellHelper ZSH = new ZshNShellHelper();

    public ZshNShellHelper() {
    }

    public String getSysRcName() {
        return ".zshenv";
    }
}
