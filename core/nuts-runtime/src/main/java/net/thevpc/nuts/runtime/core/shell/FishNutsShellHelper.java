package net.thevpc.nuts.runtime.core.shell;

public class FishNutsShellHelper extends ShNutsShellHelper {
    public static final NutsShellHelper FISH=new FishNutsShellHelper();

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
