package net.thevpc.nuts.runtime.standalone.xtra.shell;

public class FishNShellHelper extends PosixNShellHelper {
    public static final NShellHelper FISH=new FishNShellHelper();
    public static final ReplaceString SHEBAN_SH = new ReplaceString("#!/bin/fish", "#!.*");
    public ReplaceString getShebanSh() {
        return SHEBAN_SH;
    }

    @Override
    public String getSysRcName() {
        return ".config/fish/config.fish";
    }

    @Override
    public String getExportCommand(String[] names) {
        return "export " + String.join(" ", names);
    }

    @Override
    public String getSetVarCommand(String name, String value) {
        return "set "+name + dblQte(value);
    }

    @Override
    public String getSetVarStaticCommand(String name, String value) {
        return "set "+name + smpQte(value);
    }

    @Override
    public String varRef(String v) {
        return "$" + v;
    }

}
