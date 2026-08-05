package net.thevpc.nuts.runtime.standalone.xtra.shell;

public class KshNShellHelper extends NixNShellHelper {
    public static final NShellHelper KSH=new KshNShellHelper();
    public static final ReplaceString SHEBAN_SH = new ReplaceString("#!/bin/ksh", "#!.*");
    public KshNShellHelper() {
    }
    public ReplaceString getShebanSh() {
        return SHEBAN_SH;
    }

    public String getSysRcName() {
        return ".kshrc";
    }
}
