package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.win;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.PathInfo;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.FreeDesktopEntryWriter;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base.BaseSystemNdi;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.script.ReplaceString;

import java.util.Arrays;
import java.util.stream.Collectors;

public class WindowsNdi extends BaseSystemNdi {

    public WindowsNdi(NutsSession session) {
        super(session);
    }

    public String[] getSysRcNames() {
        return null;
    }

//    @Override
//    public NdiScriptInfo getNutsTerm(NdiScriptOptions options) {
//        return new NdiScriptInfo() {
//            @Override
//            public Path path() {
//                return options.resolveBinFolder().resolve(getExecFileName("nuts-term"));
//            }
//
//            @Override
//            public PathInfo create() {
//                Path apiConfigFile = path();
//                return addFileLine("nuts-term",
//                        options.resolveNutsApiId(),
//                        apiConfigFile, getCommentLineConfigHeader(),
//                        "@ECHO OFF" + newlineString() +
//                                createNutsEnvString(options, true, true) + newlineString()
//                                + "cmd.exe /K " + getExecFileName("nuts") + " welcome " + newlineString()
//                        ,
//                        getShebanSh());
//            }
//        };
//    }

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
    protected String createNutsScriptContent(NutsId fnutsId, NdiScriptOptions options) {
        StringBuilder command = new StringBuilder();
        command.append(getExecFileName("nuts")).append(" ").append(varRef("NUTS_OPTIONS")).append(" ");
        if (options.getLauncher().getNutsOptions() != null) {
            for (String o : options.getLauncher().getNutsOptions()) {
                command.append(" ").append(o);
            }
        }
        command.append(" \"").append(fnutsId).append("\"");
        command.append(" %*");
        return command.toString();
    }

    public void onPostGlobal(NdiScriptOptions options, PathInfo[] updatedPaths) {

    }

    public String newlineString() {
        return "\r\n";
    }

    @Override
    public String toCommentLine(String line) {
        return ":: " + line;
    }

    @Override
    protected String getExportCommand(String[] names) {
        return null;
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
    public String getCallScriptCommand(String path, String... args) {
        return "@CALL \"" + path + "\"" + " " + Arrays.stream(args).map(a -> dblQte(a)).collect(Collectors.joining(" "));
    }

    @Override
    public String getExecFileName(String name) {
        return name + ".cmd";
    }

    @Override
    protected FreeDesktopEntryWriter createFreeDesktopEntryWriter() {
        return new WindowFreeDesktopEntryWriter(
                session.env().getDesktopPath()
                , session);
    }

    protected int resolveIconExtensionPriority(String extension) {
        extension = extension.toLowerCase();
        switch (extension) {
            //support only ico
            case "ico":
                return 3;
        }
        return -1;
    }

    public boolean isShortcutFieldNameUserFriendly() {
        return true;
    }

    public ReplaceString getShebanSh() {
        return null;
    }

    public ReplaceString getCommentLineConfigHeader() {
        return COMMENT_LINE_CONFIG_HEADER;
    }

    @Override
    public String getTemplateName(String name) {
        return "windows-template-" + name + ".text";
    }

    @Override
    public String varRef(String v) {
        return "%" + v + "%";
    }

    public String getPathVarSep() {
        return ";";
    }
}
