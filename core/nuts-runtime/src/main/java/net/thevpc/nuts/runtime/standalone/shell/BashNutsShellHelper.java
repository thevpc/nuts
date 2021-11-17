package net.thevpc.nuts.runtime.standalone.shell;

public class BashNutsShellHelper extends ShNutsShellHelper {
    public static final NutsShellHelper BASH=new BashNutsShellHelper();
    public BashNutsShellHelper() {
    }

    public String getSysRcName() {
        return ".bashrc";
    }
}
