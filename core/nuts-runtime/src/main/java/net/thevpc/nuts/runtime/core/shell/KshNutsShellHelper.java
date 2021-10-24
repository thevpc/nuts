package net.thevpc.nuts.runtime.core.shell;

public class KshNutsShellHelper extends ShNutsShellHelper {
    public static final NutsShellHelper KSH=new KshNutsShellHelper();
    public KshNutsShellHelper() {
    }

    public String getSysRcName() {
        return ".kshrc";
    }
}
