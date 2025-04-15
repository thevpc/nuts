package net.thevpc.nuts.runtime.standalone.xtra.shell;

public class BashNShellHelper extends ShNShellHelper {
    public static final NShellHelper BASH=new BashNShellHelper();
    public BashNShellHelper() {
    }

    public String getSysRcName() {
        return ".bashrc";
    }
}
