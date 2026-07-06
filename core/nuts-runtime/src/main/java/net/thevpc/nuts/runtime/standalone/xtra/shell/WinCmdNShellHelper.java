package net.thevpc.nuts.runtime.standalone.xtra.shell;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class WinCmdNShellHelper extends AbstractWinNShellHelper {
    public static final NShellHelper WIN_CMD=new WinCmdNShellHelper();

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
        return ":: " + line;
    }


    @Override
    public String getSetVarCommand(String name, String value) {
        return "SET \"" + name + "=" + value + "\"";
    }

    @Override
    public String getSetVarStaticCommand(String name, String value) {
        return "SET \"" + name + "=" + value + "\"";
    }

    @Override
    public String getCallScriptCommand(String VAR_NAME, String path, String... args) {
        String v = NStringUtils.firstNonBlankStripped(VAR_NAME, "_V");
        String argsString = Arrays.stream(args).map(a -> dblQte(a)).collect(Collectors.joining(" "));
        if (!argsString.isEmpty()) {
            argsString = " " + argsString;
        }
        // SET _V="..."; IF EXIST %_V% CALL %_V%
        return NMsg.ofM("SET {{v}}={{qp}}& IF EXIST {{qv}} CALL {{qv}}{{args}}",
                NMaps.of(
                        "v", v,
                        "qp", dblQte(path),
                        "qv", "%" + v + "%",
                        "args", argsString
                )
        ).toString();
    }

    @Override
    public boolean isComments(String line) {
        return line.startsWith(":: ");
    }

    @Override
    public String trimComments(String line) {
        if (line.startsWith(":: ")) {
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
        return "%" + v + "%";
    }

    @Override
    public String getPathVarSep() {
        return ";";
    }


}
