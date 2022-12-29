package net.thevpc.nuts.runtime.standalone.shell;

public class CshNShellHelper extends ShNShellHelper {
    public static final NShellHelper CSH=new CshNShellHelper();
    public CshNShellHelper() {
    }

    public String getSysRcName() {
        return ".cshrc";
    }
}
