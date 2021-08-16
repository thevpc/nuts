package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.sys.win;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.optional.oswindows.OptionalWindows;
import net.thevpc.nuts.toolbox.nadmin.optional.oswindows.WinShellHelper;
import net.thevpc.nuts.toolbox.nadmin.optional.oswindows.WindowFreeDesktopEntryWriter;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.*;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base.BaseSystemNdi;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base.NutsEnvInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util.ReplaceString;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class WindowsNdi extends BaseSystemNdi {

    public WindowsNdi(NutsApplicationContext appContext) {
        super(appContext);
    }

    public String getBashrcName() {
        return null;
    }

    public String getPathVarSep() {
        return ";";
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
    public String toCommentLine(String line) {
        return ":: " + line;
    }

    @Override
    protected String createNutsScriptContent(NutsId fnutsId, NdiScriptOptions options) {
        StringBuilder command = new StringBuilder();
        command.append(getExecFileName("nuts")).append(" ").append(varRef("NUTS_OPTIONS")).append(" ");
        if (options.getExecType() != null) {
            command.append("--").append(options.getExecType().id());
        }
        command.append(" \"").append(fnutsId).append("\"");
        command.append(" %*");
        return command.toString();
    }

    protected String newlineString() {
        return "\r\n";
    }

    public void onPostGlobal(NutsEnvInfo env, PathInfo[] updatedPaths) {

    }



    @Override
    protected String getCallScriptCommand(String path,String... args) {
        return "@CALL \"" + path + "\"" + "\" "+Arrays.stream(args).map(a->dblQte(a)).collect(Collectors.joining(" "));
    }

    @Override
    protected String getSetVarCommand(String name, String value) {
        return "SET \"name="+value+"\"";
    }

    @Override
    protected String getSetVarStaticCommand(String name, String value) {
        return "SET \"name="+value+"\"";
    }

    @Override
    public String getExecFileName(String name) {
        return name + ".cmd";
    }

    @Override
    protected FreeDesktopEntryWriter createFreeDesktopEntryWriter() {
        return new WindowFreeDesktopEntryWriter(getOsDesktopPath(), context.getSession());
    }

    protected ReplaceString getShebanSh() {
        return null;
    }

    protected ReplaceString getCommentLineConfigHeader() {
        return COMMENT_LINE_CONFIG_HEADER;
    }

    @Override
    protected String getTemplateName(String name) {
        return "windows_template_"+name+".text";
    }

    @Override
    protected String varRef(String v){
        return "%"+v+"%";
    }

    private Path getOsDesktopPath() {
        return new File(System.getProperty("user.home"), "Desktop").toPath();
    }

    @Override
    public NdiScriptInfo getNutsTerm(NutsEnvInfo env) {
        return new NdiScriptInfo() {
            @Override
            public Path path() {
                return env.getBinFolder().resolve(getExecFileName("nuts-term"));
            }

            @Override
            public PathInfo create() {
                Path apiConfigFile = path();
                return addFileLine(NdiScriptInfoType.NUTS_TERM,
                        env.getNutsApiId(),
                        apiConfigFile, getCommentLineConfigHeader(),
                        createNutsEnvString(env, true, true)+newlineString()
                                +getExecFileName("nuts")+" welcome "+newlineString()
                                +"cmd.exe"+newlineString()
                        ,
                        getShebanSh());
            }
        };
    }

    protected int resolveIconExtensionPriority(String extension) {
        extension = extension.toLowerCase();
        switch (extension) {
            //support only ico
            case "ico":
                return 3;
        }
        return -1;
    }}
