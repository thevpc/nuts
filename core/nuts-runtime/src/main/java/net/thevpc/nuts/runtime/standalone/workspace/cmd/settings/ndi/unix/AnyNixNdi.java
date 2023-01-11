package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.unix;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.runtime.standalone.shell.NShellHelper;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.FreeDesktopEntryWriter;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base.BaseSystemNdi;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NQuestion;
import net.thevpc.nuts.util.NQuestionParser;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AnyNixNdi extends BaseSystemNdi {

    public AnyNixNdi(NSession session) {
        super(session);
    }

    protected NShellFamily[] getShellGroups() {
        Set<NShellFamily> all=new LinkedHashSet<>(NEnvs.of(session).getShellFamilies());
        all.retainAll(Arrays.asList(NShellFamily.SH, NShellFamily.FISH));
        return all.toArray(new NShellFamily[0]);
    }

    public boolean isShortcutFileNameUserFriendly() {
        return false;
    }

    @Override
    public String createNutsScriptContent(NId fnutsId, NdiScriptOptions options, NShellFamily shellFamily) {
        StringBuilder command = new StringBuilder();
        command.append(getExecFileName("nuts")).append(" ").append(
                NShellHelper.of(shellFamily).varRef("NUTS_OPTIONS")).append(" ");
        if (options.getLauncher().getNutsOptions() != null) {
            for (String no : options.getLauncher().getNutsOptions()) {
                command.append(" ").append(no);
            }
        }
        command.append(" \"").append(fnutsId).append("\"");
        command.append(" \"$@\"");
        return command.toString();
    }

    public void onPostGlobal(NdiScriptOptions options, PathInfo[] updatedPaths) {
        NTexts factory = NTexts.of(session);
        if (Arrays.stream(updatedPaths).anyMatch(x -> x.getStatus() != PathInfo.Status.DISCARDED) && session.isTrace()) {
            if (session.isPlainTrace()) {
                session.out().resetLine().println(NMsg.ofC("%s %s to point to workspace %s",
                        session.isYes() ?
                                factory.ofStyled("force updating", NTextStyle.warn().append(NTextStyle.underlined())) :
                                factory.ofStyled("force updating", NTextStyle.warn())
                        ,
                        factory.ofBuilder().appendJoined(", ",
                                Arrays.stream(updatedPaths).map(x ->
                                        factory.ofStyled(x.getPath().getName(), NTextStyle.path())).collect(Collectors.toList())),
                        NLocations.of(session).getWorkspaceLocation()
                ));
            }
            final String sysRcName = NShellHelper.of(NEnvs.of(session).getShellFamily()).getSysRcName();
            session.getTerminal().ask()
                    .resetLine()
                    .forBoolean(NMsg.ofC(
                            "```error ATTENTION``` You may need to re-run terminal or issue \"%s\" in your current terminal for new environment to take effect.%n"
                                    + "Please type %s if you agree, %s if you need more explanation or %s to cancel updates.",
                            factory.ofStyled(". ~/" + sysRcName, NTextStyle.path()),
                            factory.ofStyled("ok", NTextStyle.success()),
                            factory.ofStyled("why", NTextStyle.warn()),
                            factory.ofStyled("cancel!", NTextStyle.comments())
                    ))
                    .setHintMessage(NMsg.ofPlain("you must enter your confirmation"))
                    .setSession(session)
                    .setParser(new NQuestionParser<Boolean>() {
                        @Override
                        public Boolean parse(Object response, Boolean defaultValue, NQuestion<Boolean> question) {
                            if (response instanceof Boolean) {
                                return (Boolean) response;
                            }
                            if (response == null || ((response instanceof String) && response.toString().length() == 0)) {
                                response = defaultValue;
                            }
                            if (response == null) {
                                throw new NValidationException(session, NMsg.ofPlain("sorry... but you need to type 'ok', 'why' or 'cancel'"));
                            }
                            String r = response.toString();
                            if ("ok".equalsIgnoreCase(r)) {
                                return true;
                            }
                            if ("why".equalsIgnoreCase(r)) {
                                NOutputStream out = session.out();
                                out.resetLine();
                                out.println(NMsg.ofC("\\\"%s\\\" is a special file in your home that is invoked upon each interactive terminal launch.", factory.ofStyled(sysRcName, NTextStyle.path())));
                                out.print("It helps configuring environment variables. ```sh nuts``` make usage of such facility to update your **PATH** env variable\n");
                                out.print("to point to current ```sh nuts``` workspace, so that when you call a ```sh nuts``` command it will be resolved correctly...\n");
                                out.println(NMsg.ofC("However updating \\\"%s\\\" does not affect the running process/terminal. So you have basically two choices :", factory.ofStyled(sysRcName, NTextStyle.path())));
                                out.print(" - Either to restart the process/terminal (konsole, term, xterm, sh, bash, ...)%n");
                                out.println(NMsg.ofC(" - Or to run by your self the \\\"%s\\\" script (don\\'t forget the leading dot)", factory.ofStyled(". ~/" + sysRcName, NTextStyle.path())));
                                throw new NValidationException(session, NMsg.ofPlain("Try again..."));
                            } else if ("cancel".equalsIgnoreCase(r) || "cancel!".equalsIgnoreCase(r)) {
                                throw new NCancelException(session);
                            } else {
                                throw new NValidationException(session, NMsg.ofPlain("sorry... but you need to type 'ok', 'why' or 'cancel'"));
                            }
                        }
                    })
                    .getValue();

        }
    }

    @Override
    public String getExecFileName(String name) {
        return name;
    }

    @Override
    protected FreeDesktopEntryWriter createFreeDesktopEntryWriter() {
        return new UnixFreeDesktopEntryWriter(session,
                NEnvs.of(session).getDesktopPath()==null?null: NPath.of(NEnvs.of(session).getDesktopPath(),getSession())
        );
    }


    public String getTemplateName(String name, NShellFamily shellFamily) {
        switch (shellFamily){
            case SH:
            case BASH:
            case CSH:
            case ZSH:
            case KSH:
            {
                return "template-" + name + ".sh";
            }
            case FISH:
            {
                return "template-" + name + ".fish";
            }
        }
        return "template-" + name + ".sh";
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

    public NdiScriptInfo[] getNutsTerm(NdiScriptOptions options) {
//        return Arrays.stream(getShellGroups())
//                .map(x -> getNutsTerm(options, x))
//                .filter(Objects::nonNull)
//                .toArray(NdiScriptInfo[]::new);
        return Arrays.stream(new NShellFamily[]{NShellFamily.SH})
                .map(x -> getNutsTerm(options, x))
                .filter(Objects::nonNull)
                .toArray(NdiScriptInfo[]::new);

    }

    public NdiScriptInfo getNutsTerm(NdiScriptOptions options, NShellFamily shellFamily) {
        switch (shellFamily){
            case SH:
            case BASH:
            case ZSH:
            case CSH:
            case KSH:
            {
                return new NdiScriptInfo() {
                    @Override
                    public NPath path() {
                        return options.resolveBinFolder().resolve(getExecFileName("nuts-term"));
                    }

                    @Override
                    public PathInfo create() {
                        return scriptBuilderTemplate("nuts-term", NShellFamily.SH, "nuts-term", options.resolveNutsApiId(), options)
                                .setPath(path())
                                .build();
                    }
                };
            }
            case FISH:
            {
                return new NdiScriptInfo() {
                    @Override
                    public NPath path() {
                        return options.resolveBinFolder().resolve(getExecFileName("nuts-term"));
                    }

                    @Override
                    public PathInfo create() {
                        return scriptBuilderTemplate("nuts-term", NShellFamily.FISH, "nuts-term", options.resolveNutsApiId(), options)
                                .setPath(path())
                                .build();
                    }
                };
            }
        }
        return null;
    }

    public NdiScriptInfo getIncludeNutsEnv(NdiScriptOptions options, NShellFamily shellFamily) {
        switch (shellFamily) {
            case SH:
            case BASH:
            case CSH:
            case KSH:
            case ZSH: {
                return new NdiScriptInfo() {
                    @Override
                    public NPath path() {
                        return options.resolveIncFolder().resolve(".nuts-env.sh");
                    }

                    @Override
                    public PathInfo create() {
                        return scriptBuilderTemplate("nuts-env", NShellFamily.SH, "nuts-env", options.resolveNutsApiId(), options)
                                .setPath(path())
                                .build();
                    }
                };
            }
            case FISH: {
                return new NdiScriptInfo() {
                    @Override
                    public NPath path() {
                        return options.resolveIncFolder().resolve(".nuts-env.fish");
                    }

                    @Override
                    public PathInfo create() {
                        return scriptBuilderTemplate("nuts-env", NShellFamily.FISH, "nuts-env", options.resolveNutsApiId(), options)
                                .setPath(path())
                                .build();
                    }
                };
            }
        }
        return null;
    }
    public NdiScriptInfo getIncludeNutsTermInit(NdiScriptOptions options, NShellFamily shellFamily) {
        switch (shellFamily) {
            case FISH: {
                return
                        new NdiScriptInfo() {
                            @Override
                            public NPath path() {
                                return options.resolveIncFolder().resolve(".nuts-term-init.fish");
                            }

                            @Override
                            public PathInfo create() {
                                return scriptBuilderTemplate("nuts-term-init", NShellFamily.FISH, "nuts-term-init", options.resolveNutsApiId(), options)
                                        .setPath(path())
                                        .build();
                            }
                        }
                        ;
            }
            case SH:
            case BASH:
            case CSH:
            case KSH:
            case ZSH: {
                return
                        new NdiScriptInfo() {
                            @Override
                            public NPath path() {
                                return options.resolveIncFolder().resolve(".nuts-term-init.sh");
                            }

                            @Override
                            public PathInfo create() {
                                return scriptBuilderTemplate("nuts-term-init", NShellFamily.SH, "nuts-term-init", options.resolveNutsApiId(), options)
                                        .setPath(path())
                                        .build();
                            }
                        }
                        ;
            }
        }
        return null;
    }

    public NdiScriptInfo getIncludeNutsInit(NdiScriptOptions options, NShellFamily shellFamily) {
        switch (shellFamily) {
            case SH:
            case BASH:
            case CSH:
            case KSH:
            case ZSH: {
                return new NdiScriptInfo() {
                    @Override
                    public NPath path() {
                        return options.resolveIncFolder().resolve(".nuts-init.sh");
                    }

                    @Override
                    public PathInfo create() {
                        NPath apiConfigFile = path();
                        return scriptBuilderTemplate("nuts-init", NShellFamily.SH, "nuts-init", options.resolveNutsApiId(), options)
                                .setPath(apiConfigFile)
                                .buildAddLine(AnyNixNdi.this);
                    }
                };
            }
            case FISH: {
                return new NdiScriptInfo() {
                    @Override
                    public NPath path() {
                        return options.resolveIncFolder().resolve(".nuts-init.fish");
                    }

                    @Override
                    public PathInfo create() {
                        NPath apiConfigFile = path();
                        return scriptBuilderTemplate("nuts-init", NShellFamily.FISH, "nuts-init", options.resolveNutsApiId(), options)
                                .setPath(apiConfigFile)
                                .buildAddLine(AnyNixNdi.this);
                    }
                };
            }
        }
        return null;
    }

}
