package net.thevpc.nuts.runtime.standalone.xtra.shell;

public class BashNShellHelper extends NixNShellHelper {
    public static final NShellHelper BASH=new BashNShellHelper();
    public static final ReplaceString SHEBAN_SH = new ReplaceString("#!/bin/bash", "#!.*");
    public BashNShellHelper() {
    }
    public ReplaceString getShebanSh() {
        return SHEBAN_SH;
    }

    public String getSysRcName() {
        return ".bashrc";
    }
}
