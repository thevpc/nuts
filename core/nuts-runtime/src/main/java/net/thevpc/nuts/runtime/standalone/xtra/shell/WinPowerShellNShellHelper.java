package net.thevpc.nuts.runtime.standalone.xtra.shell;

import java.util.Arrays;
import java.util.stream.Collectors;

public class WinPowerShellNShellHelper extends AbstractWinNShellHelper {
    public static final NShellHelper WIN_POWER_SHELL = new WinPowerShellNShellHelper();

    @Override
    public String getExportCommand(String[] names) {
        return null;
    }

    @Override
    public String getSysRcName() {
        return null;
    }

    public String dblQte(String line) {
        return "\"" + line + "\"";
    }

    @Override
    public String toCommentLine(String line) {
        return "# " + line;
    }


    @Override
    public String getSetVarCommand(String name, String value) {
        return "$" + name + "=\"" + value + "\"";
    }

    @Override
    public String getSetVarStaticCommand(String name, String value) {
        return "$" + name + "=\"" + value + "\"";
    }

    @Override
    public String getCallScriptCommand(String path, String... args) {
        return "\"" + path + "\"" + " " + Arrays.stream(args).map(a -> dblQte(a)).collect(Collectors.joining(" "));
    }

    @Override
    public boolean isComments(String line) {
        return line.startsWith("# ");
    }

    @Override
    public String trimComments(String line) {
        if (line.startsWith("# ")) {
            return line.substring(2).trim();
        }
        throw new IllegalArgumentException("not a comment");
    }

    @Override
    public ReplaceString getShebanSh() {
        return null;
    }

    @Override
    public String varRef(String v) {
        return "$" + v;
    }

    @Override
    public String getPathVarSep() {
        return ";";
    }


}
