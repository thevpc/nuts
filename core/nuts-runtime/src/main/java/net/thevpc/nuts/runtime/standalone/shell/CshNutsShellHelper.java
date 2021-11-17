package net.thevpc.nuts.runtime.standalone.shell;

public class CshNutsShellHelper extends ShNutsShellHelper {
    public static final NutsShellHelper CSH=new CshNutsShellHelper();
    public CshNutsShellHelper() {
    }

    public String getSysRcName() {
        return ".cshrc";
    }
}
