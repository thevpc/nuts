package net.thevpc.nuts.runtime.standalone.xtra.shell;

public class CshNShellHelper extends NixNShellHelper {
    public static final NShellHelper CSH=new CshNShellHelper();
    public static final ReplaceString SHEBAN_SH = new ReplaceString("#!/bin/csh", "#!.*");
    public CshNShellHelper() {
    }
    public ReplaceString getShebanSh() {
        return SHEBAN_SH;
    }

    public String getSysRcName() {
        return ".cshrc";
    }
}
