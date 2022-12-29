package net.thevpc.nuts.runtime.standalone.shell;

public class FishNShellHelper extends ShNShellHelper {
    public static final NShellHelper FISH=new FishNShellHelper();

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
