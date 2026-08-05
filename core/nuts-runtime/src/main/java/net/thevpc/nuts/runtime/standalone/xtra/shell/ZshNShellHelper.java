package net.thevpc.nuts.runtime.standalone.xtra.shell;

public class ZshNShellHelper extends NixNShellHelper {
    public static final NShellHelper ZSH = new ZshNShellHelper();
    public static final ReplaceString SHEBAN_SH = new ReplaceString("#!/bin/zsh", "#!.*");

    public ZshNShellHelper() {
    }
    public ReplaceString getShebanSh() {
        return SHEBAN_SH;
    }

    public String getSysRcName() {
        return ".zshenv";
    }
}
