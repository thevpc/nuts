package net.thevpc.nuts.runtime.core.shell;

public class CshNutsShellHelper extends ShNutsShellHelper {
    public static final NutsShellHelper CSH=new CshNutsShellHelper();
    public CshNutsShellHelper() {
    }

    public String getSysRcName() {
        return ".cshrc";
    }
}
