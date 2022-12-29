package net.thevpc.nuts.runtime.standalone.shell;

public class KshNShellHelper extends ShNShellHelper {
    public static final NShellHelper KSH=new KshNShellHelper();
    public KshNShellHelper() {
    }

    public String getSysRcName() {
        return ".kshrc";
    }
}
