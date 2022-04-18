package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.unix;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.shell.NutsShellHelper;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.FreeDesktopEntryWriter;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base.BaseSystemNdi;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AnyNixNdi extends BaseSystemNdi {

    public AnyNixNdi(NutsSession session) {
        super(session);
    }

    protected NutsShellFamily[] getShellGroups() {
        Set<NutsShellFamily> all=new LinkedHashSet<>(session.env().getShellFamilies());
        all.retainAll(Arrays.asList(NutsShellFamily.SH,NutsShellFamily.FISH));
        return all.toArray(new NutsShellFamily[0]);
    }

    public boolean isShortcutFileNameUserFriendly() {
        return false;
    }

    @Override
    public String createNutsScriptContent(NutsId fnutsId, NdiScriptOptions options, NutsShellFamily shellFamily) {
        StringBuilder command = new StringBuilder();
        command.append(getExecFileName("nuts")).append(" ").append(
                NutsShellHelper.of(shellFamily).varRef("NUTS_OPTIONS")).append(" ");
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
        NutsTexts factory = NutsTexts.of(session);
        if (Arrays.stream(updatedPaths).anyMatch(x -> x.getStatus() != PathInfo.Status.DISCARDED) && session.isTrace()) {
            if (session.isPlainTrace()) {
                session.out().resetLine().printf("%s %s to point to workspace %s%n",
                        session.isYes() ?
                                factory.ofStyled("force updating", NutsTextStyle.warn().append(NutsTextStyle.underlined())) :
                                factory.ofStyled("force updating", NutsTextStyle.warn())
                        ,
                        factory.builder().appendJoined(", ",
                                Arrays.stream(updatedPaths).map(x ->
                                        factory.ofStyled(x.getPath().getName(), NutsTextStyle.path())).collect(Collectors.toList())),
                        session.locations().getWorkspaceLocation()
                );
            }
            final String sysRcName = NutsShellHelper.of(session.env().getShellFamily()).getSysRcName();
            session.getTerminal().ask()
                    .resetLine()
                    .forBoolean(
                            "```error ATTENTION``` You may need to re-run terminal or issue \"%s\" in your current terminal for new environment to take effect.%n"
                                    + "Please type 'ok' if you agree, 'why' if you need more explanation or 'cancel' to cancel updates.",
                            factory.ofStyled(". ~/" + sysRcName, NutsTextStyle.path())
                    )
                    .setHintMessage(NutsMessage.plain(""))
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
                                out.printf("\\\"%s\\\" is a special file in your home that is invoked upon each interactive terminal launch.%n", factory.ofStyled(sysRcName, NutsTextStyle.path()));
                                out.print("It helps configuring environment variables. ```sh nuts``` make usage of such facility to update your **PATH** env variable\n");
                                out.print("to point to current ```sh nuts``` workspace, so that when you call a ```sh nuts``` command it will be resolved correctly...\n");
                                out.printf("However updating \\\"%s\\\" does not affect the running process/terminal. So you have basically two choices :%n", factory.ofStyled(sysRcName, NutsTextStyle.path()));
                                out.print(" - Either to restart the process/terminal (konsole, term, xterm, sh, bash, ...)%n");
                                out.printf(" - Or to run by your self the \\\"%s\\\" script (don\\'t forget the leading dot)%n", factory.ofStyled(". ~/" + sysRcName, NutsTextStyle.path()));
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
    public String getExecFileName(String name) {
        return name;
    }

    @Override
    protected FreeDesktopEntryWriter createFreeDesktopEntryWriter() {
        return new UnixFreeDesktopEntryWriter(session,
                session.env().getDesktopPath()==null?null:NutsPath.of(session.env().getDesktopPath(),getSession())
        );
    }


    public String getTemplateName(String name, NutsShellFamily shellFamily) {
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
        return Arrays.stream(new NutsShellFamily[]{NutsShellFamily.SH})
                .map(x -> getNutsTerm(options, x))
                .filter(Objects::nonNull)
                .toArray(NdiScriptInfo[]::new);

    }

    public NdiScriptInfo getNutsTerm(NdiScriptOptions options,NutsShellFamily shellFamily) {
        switch (shellFamily){
            case SH:
            case BASH:
            case ZSH:
            case CSH:
            case KSH:
            {
                return new NdiScriptInfo() {
                    @Override
                    public NutsPath path() {
                        return options.resolveBinFolder().resolve(getExecFileName("nuts-term"));
                    }

                    @Override
                    public PathInfo create() {
                        return scriptBuilderTemplate("nuts-term",NutsShellFamily.SH, "nuts-term", options.resolveNutsApiId(), options)
                                .setPath(path())
                                .build();
                    }
                };
            }
            case FISH:
            {
                return new NdiScriptInfo() {
                    @Override
                    public NutsPath path() {
                        return options.resolveBinFolder().resolve(getExecFileName("nuts-term"));
                    }

                    @Override
                    public PathInfo create() {
                        return scriptBuilderTemplate("nuts-term",NutsShellFamily.FISH, "nuts-term", options.resolveNutsApiId(), options)
                                .setPath(path())
                                .build();
                    }
                };
            }
        }
        return null;
    }

    public NdiScriptInfo getIncludeNutsEnv(NdiScriptOptions options, NutsShellFamily shellFamily) {
        switch (shellFamily) {
            case SH:
            case BASH:
            case CSH:
            case KSH:
            case ZSH: {
                return new NdiScriptInfo() {
                    @Override
                    public NutsPath path() {
                        return options.resolveIncFolder().resolve(".nuts-env.sh");
                    }

                    @Override
                    public PathInfo create() {
                        return scriptBuilderTemplate("nuts-env", NutsShellFamily.SH, "nuts-env", options.resolveNutsApiId(), options)
                                .setPath(path())
                                .build();
                    }
                };
            }
            case FISH: {
                return new NdiScriptInfo() {
                    @Override
                    public NutsPath path() {
                        return options.resolveIncFolder().resolve(".nuts-env.fish");
                    }

                    @Override
                    public PathInfo create() {
                        return scriptBuilderTemplate("nuts-env", NutsShellFamily.FISH, "nuts-env", options.resolveNutsApiId(), options)
                                .setPath(path())
                                .build();
                    }
                };
            }
        }
        return null;
    }
    public NdiScriptInfo getIncludeNutsTermInit(NdiScriptOptions options, NutsShellFamily shellFamily) {
        switch (shellFamily) {
            case FISH: {
                return
                        new NdiScriptInfo() {
                            @Override
                            public NutsPath path() {
                                return options.resolveIncFolder().resolve(".nuts-term-init.fish");
                            }

                            @Override
                            public PathInfo create() {
                                return scriptBuilderTemplate("nuts-term-init", NutsShellFamily.FISH, "nuts-term-init", options.resolveNutsApiId(), options)
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
                            public NutsPath path() {
                                return options.resolveIncFolder().resolve(".nuts-term-init.sh");
                            }

                            @Override
                            public PathInfo create() {
                                return scriptBuilderTemplate("nuts-term-init", NutsShellFamily.SH, "nuts-term-init", options.resolveNutsApiId(), options)
                                        .setPath(path())
                                        .build();
                            }
                        }
                        ;
            }
        }
        return null;
    }

    public NdiScriptInfo getIncludeNutsInit(NdiScriptOptions options, NutsShellFamily shellFamily) {
        switch (shellFamily) {
            case SH:
            case BASH:
            case CSH:
            case KSH:
            case ZSH: {
                return new NdiScriptInfo() {
                    @Override
                    public NutsPath path() {
                        return options.resolveIncFolder().resolve(".nuts-init.sh");
                    }

                    @Override
                    public PathInfo create() {
                        NutsPath apiConfigFile = path();
                        return scriptBuilderTemplate("nuts-init", NutsShellFamily.SH, "nuts-init", options.resolveNutsApiId(), options)
                                .setPath(apiConfigFile)
                                .buildAddLine(AnyNixNdi.this);
                    }
                };
            }
            case FISH: {
                return new NdiScriptInfo() {
                    @Override
                    public NutsPath path() {
                        return options.resolveIncFolder().resolve(".nuts-init.fish");
                    }

                    @Override
                    public PathInfo create() {
                        NutsPath apiConfigFile = path();
                        return scriptBuilderTemplate("nuts-init", NutsShellFamily.FISH, "nuts-init", options.resolveNutsApiId(), options)
                                .setPath(apiConfigFile)
                                .buildAddLine(AnyNixNdi.this);
                    }
                };
            }
        }
        return null;
    }

}
