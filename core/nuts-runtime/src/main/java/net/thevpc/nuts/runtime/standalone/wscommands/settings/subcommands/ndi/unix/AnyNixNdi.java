package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.unix;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.PathInfo;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.FreeDesktopEntryWriter;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base.BaseSystemNdi;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.script.ReplaceString;

import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class AnyNixNdi extends BaseSystemNdi {
    public static final ReplaceString SHEBAN_SH = new ReplaceString("#!/bin/sh", "#!.*");

    public AnyNixNdi(NutsSession session) {
        super(session);
    }

    public String getBashrcName() {
        return ".bashrc";
    }

    public String getPathVarSep() {
        return ":";
    }

    @Override
    public boolean isComments(String line) {
        line = line.trim();
        return line.startsWith("#");
    }

    public boolean isShortcutFieldNameUserFriendly() {
        return false;
    }

    @Override
    public String trimComments(String line) {
        line = line.trim();
        if (line.startsWith("#")) {
            while (line.startsWith("#")) {
                line = line.substring(1);
            }
            return line.trim();
        }
        throw new IllegalArgumentException("not a comment: " + line);
    }

    @Override
    public String toCommentLine(String line) {
        return "# " + line;
    }

    @Override
    public String createNutsScriptContent(NutsId fnutsId, NdiScriptOptions options) {
        StringBuilder command = new StringBuilder();
        command.append(getExecFileName("nuts")).append(" ").append(varRef("NUTS_OPTIONS")).append(" ");
        if (options.getLauncher().getNutsOptions() != null) {
            for (String no : options.getLauncher().getNutsOptions()) {
                command.append(" ").append(no);
            }
        }
        command.append(" \"").append(fnutsId).append("\"");
        command.append(" \"$@\"");
        return command.toString();
    }

    public String newlineString() {
        return "\n";
    }

    public void onPostGlobal(NdiScriptOptions options, PathInfo[] updatedPaths) {
        NutsTextManager factory = session.getWorkspace().text();
        if (Arrays.stream(updatedPaths).anyMatch(x -> x.getStatus() != PathInfo.Status.DISCARDED) && session.isTrace()) {
            if (session.isPlainTrace()) {
                session.out().resetLine().printf("%s %s to point to workspace %s%n",
                        session.isYes() ?
                                factory.forStyled("force updating", NutsTextStyle.warn().append(NutsTextStyle.underlined())) :
                                factory.forStyled("force updating", NutsTextStyle.warn())
                        ,
                        factory.builder().appendJoined(", ",
                                Arrays.stream(updatedPaths).map(x ->
                                        factory.forStyled(x.getPath().getFileName().toString(), NutsTextStyle.path())).collect(Collectors.toList())),
                        factory.forStyled(session.getWorkspace().locations().getWorkspaceLocation(), NutsTextStyle.path())
                );
            }
            session.getTerminal().ask()
                    .resetLine()
                    .forBoolean(
                            "```error ATTENTION``` You may need to re-run terminal or issue \"%s\" in your current terminal for new environment to take effect.%n"
                                    + "Please type 'ok' if you agree, 'why' if you need more explanation or 'cancel' to cancel updates.",
                            factory.forStyled(". ~/" + getBashrcName(), NutsTextStyle.path())
                    )
                    .setHintMessage("")
                    .setSession(session)
                    .setParser(new NutsQuestionParser<Boolean>() {
                        @Override
                        public Boolean parse(Object response, Boolean defaultValue, NutsQuestion<Boolean> question) {
                            if (response instanceof Boolean) {
                                return (Boolean) response;
                            }
                            if (response == null || ((response instanceof String) && response.toString().length() == 0)) {
                                response = defaultValue;
                            }
                            if (response == null) {
                                throw new NutsValidationException(session, NutsMessage.cstyle("sorry... but you need to type 'ok', 'why' or 'cancel'"));
                            }
                            String r = response.toString();
                            if ("ok".equalsIgnoreCase(r)) {
                                return true;
                            }
                            if ("why".equalsIgnoreCase(r)) {
                                NutsPrintStream out = session.out();
                                out.resetLine();
                                out.printf("\\\"%s\\\" is a special file in your home that is invoked upon each interactive terminal launch.%n", factory.forStyled(getBashrcName(), NutsTextStyle.path()));
                                out.print("It helps configuring environment variables. ```sh nuts``` make usage of such facility to update your **PATH** env variable\n");
                                out.print("to point to current ```sh nuts``` workspace, so that when you call a ```sh nuts``` command it will be resolved correctly...\n");
                                out.printf("However updating \\\"%s\\\" does not affect the running process/terminal. So you have basically two choices :%n", factory.forStyled(getBashrcName(), NutsTextStyle.path()));
                                out.print(" - Either to restart the process/terminal (konsole, term, xterm, sh, bash, ...)%n");
                                out.printf(" - Or to run by your self the \\\"%s\\\" script (don\\'t forget the leading dot)%n", factory.forStyled(". ~/" + getBashrcName(), NutsTextStyle.path()));
                                throw new NutsValidationException(session, NutsMessage.cstyle("Try again..."));
                            } else if ("cancel".equalsIgnoreCase(r) || "cancel!".equalsIgnoreCase(r)) {
                                throw new NutsUserCancelException(session);
                            } else {
                                throw new NutsValidationException(session, NutsMessage.cstyle("sorry... but you need to type 'ok', 'why' or 'cancel'"));
                            }
                        }
                    })
                    .getValue();

        }
    }

    @Override
    public String getCallScriptCommand(String path, String... args) {
        return "source \"" + path + "\" " + Arrays.stream(args).map(a -> dblQte(a)).collect(Collectors.joining(" "));
    }

    @Override
    protected String getExportCommand(String[] names) {
        return "export " + String.join(" ", names);
    }

    @Override
    public String getSetVarCommand(String name, String value) {
        return name +"=\"" + value + "\"";
    }

    @Override
    public String getSetVarStaticCommand(String name, String value) {
        return name + "='" + value + "'";
    }

    @Override
    public String getExecFileName(String name) {
        return name;
    }

    @Override
    protected FreeDesktopEntryWriter createFreeDesktopEntryWriter() {
        return new UnixFreeDesktopEntryWriter(session,
                session.getWorkspace().env().getDesktopPath()
        );
    }

    protected ReplaceString getShebanSh() {
        return SHEBAN_SH;
    }

    protected ReplaceString getCommentLineConfigHeader() {
        return COMMENT_LINE_CONFIG_HEADER;
    }

    @Override
    public String getTemplateName(String name) {
        return "linux_template_" + name + ".text";
    }

    @Override
    public String varRef(String v) {
        return "${" + v + "}";
    }

    protected int resolveIconExtensionPriority(String extension) {
        extension = extension.toLowerCase();
        switch (extension) {
            case "svg":
                return 10;
            case "png":
                return 8;
            case "jpg":
                return 6;
            case "jpeg":
                return 5;
            case "gif":
                return 4;
            case "ico":
                return 3;
        }
        return -1;
    }
}
