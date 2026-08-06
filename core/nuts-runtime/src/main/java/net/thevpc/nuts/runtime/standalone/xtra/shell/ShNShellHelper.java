package net.thevpc.nuts.runtime.standalone.xtra.shell;

public class ShNShellHelper extends PosixNShellHelper {
    public static final NShellHelper SH=new ShNShellHelper();
    public static final ReplaceString SHEBAN_SH = new ReplaceString("#!/bin/sh", "#!.*");
    public ShNShellHelper() {
    }
    public ReplaceString getShebanSh() {
        return SHEBAN_SH;
    }

    public String getSysRcName() {
        return ".profile";
    }
}
