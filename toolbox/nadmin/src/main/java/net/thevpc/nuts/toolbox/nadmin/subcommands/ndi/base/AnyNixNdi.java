package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptOptions;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.WorkspaceAndApiVersion;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AnyNixNdi extends BaseSystemNdi {
    public AnyNixNdi(NutsApplicationContext appContext) {
        super(appContext);
    }

    @Override
    public String createNutsScriptCommand(NutsId fnutsId, NdiScriptOptions options) {
        StringBuilder command = new StringBuilder();
        command.append(getExecFileName("nuts")).append(" $NUTS_OPTIONS ");
        if (options.getExecType() != null) {
            command.append("--").append(options.getExecType().id());
        }
        command.append(" \"").append(fnutsId).append("\"");
        command.append(" \"$@\"");
        return command.toString();
    }

    @Override
    public String toCommentLine(String line) {
        return "# " + line;
    }

    @Override
    protected String getCallScriptCommand(String path) {
        return "source \"" + path + "\"";
    }

    @Override
    public String getExecFileName(String name) {
        return name;
    }

    @Override
    protected String getTemplateBodyName() {
        return "linux_template_body.text";
    }

    @Override
    protected String getTemplateNutsName() {
        return "linux_template_nuts.text";
    }

    public String getBashrcName() {
        return ".bashrc";
    }


    public UpdatedPaths persistConfig2(NutsWorkspaceBootConfig bootConfig,
                                       NutsId nutsId,
                                       String fileName,
                                       NutsSession session) {
        Path sysrcFile=null;
        if(fileName==null){
            sysrcFile = Paths.get(System.getProperty("user.home")).resolve(getBashrcName());
        }else{
            if(fileName.contains("%v")){
                fileName=fileName.replace("%v",nutsId.getVersion().toString());
            }
            sysrcFile=Paths.get(fileName);
        }
        NutsWorkspace ws = context.getWorkspace();
        Path apiAppsFolder =
                bootConfig != null ? Paths.get(bootConfig.getStoreLocation(nutsId, NutsStoreLocation.APPS)) :
                        Paths.get(ws.locations().getStoreLocation(nutsId, NutsStoreLocation.APPS));
        Path apiConfigFile = apiAppsFolder.resolve(getExecFileName(".nuts-bashrc"));

        boolean force = session.isYes();
        //old configs
        sysrcFile=sysrcFile.toAbsolutePath();
        removeFileCommented2Lines(sysrcFile, "net.thevpc.nuts.toolbox.ndi configuration", force);
        removeFileCommented2Lines(sysrcFile, "net.vpc.app.nuts configuration", force);

        if (addFileLine(sysrcFile, "net.thevpc.nuts configuration",
                getCallScriptCommand(apiConfigFile.toString()),
                force, "#!.*", "#!/bin/sh")) {
            return new UpdatedPaths(new String[]{sysrcFile.toString()},new String[0]);
            //updatedNames.add("~/.bashrc");
        }
        return new UpdatedPaths(new String[0],new String[]{sysrcFile.toString()});
    }

    @Override
    public void configurePath(NutsSession session, boolean persistentConfig) {
        Path ndiAppsFolder = Paths.get(context.getAppsFolder());
        final NutsWorkspace ws = context.getWorkspace();
        NutsWorkspaceConfigManager wsconfig = ws.config();
        Path apiAppsFolder = Paths.get(ws.locations().getStoreLocation(ws.getApiId(), NutsStoreLocation.APPS));
        Path apiConfigFile = apiAppsFolder.resolve(getExecFileName(".nuts-bashrc"));
        Path ndiConfigFile = ndiAppsFolder.resolve(getExecFileName(".nadmin-bashrc"));
        List<String> updatedNames = new ArrayList<>();
        boolean force = session.isYes();

        if (persistentConfig) {
            WorkspaceAndApiVersion r = persistConfig(null, null, null, session);
            for (String updatedPath : r.getUpdatedPaths()) {
                String ss = System.getProperty("user.home") + File.separator;
                if(updatedPath.startsWith(ss)){
                    updatedNames.add("~"+File.separator + updatedPath.substring(ss.length()));
                }
            }
        }

        removeFileCommented2Lines(apiConfigFile, "net.thevpc.nuts.toolbox.ndi configuration", force);
        removeFileCommented2Lines(apiConfigFile, "net.vpc.app.nuts configuration", force);

        if (addFileLine(apiConfigFile, "net.thevpc.nuts configuration",
                getCallScriptCommand(ndiConfigFile.toString()),
                force, "#!.*", "#!/bin/sh")) {
            updatedNames.add(".nuts-bashrc");
        }

        String NUTS_JAR_PATH = ws.search()
                .setSession(context.getSession().copy().setTrace(false))
                .addId(ws.getApiId()).getResultPaths().required();

        StringBuilder goodNdiRc =new StringBuilder(
                "#!/bin/sh\n" +
                        "# This File is generated by nuts nadmin companion tool.\n" +
                        "# Do not edit it manually. All changes will be lost when nadmin runs again\n" +
                        "# This file aims to prepare bash environment against current nuts\n" +
                        "# workspace installation.\n" +
                        "#\n" );
        goodNdiRc.append("NUTS_VERSION='" + ws.getApiVersion() + "'\n");
        goodNdiRc.append("NUTS_WORKSPACE='" + ws.locations().getWorkspaceLocation().toString() + "'\n");
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            goodNdiRc.append("NUTS_WORKSPACE_"+value+"='" + ws.locations().getStoreLocation(value) + "'\n");
        }
        if(NUTS_JAR_PATH.startsWith(ws.locations().getStoreLocation(NutsStoreLocation.LIB))) {
            goodNdiRc.append("NUTS_JAR=\"${NUTS_WORKSPACE_LIB}" + NUTS_JAR_PATH.toString().substring(
                    ws.locations().getStoreLocation(NutsStoreLocation.LIB).length()
            ) + "\"\n");
        }else{
            goodNdiRc.append("NUTS_JAR='" + NUTS_JAR_PATH + "'\n");
        }

        if(ndiAppsFolder.toString().startsWith(ws.locations().getStoreLocation(NutsStoreLocation.APPS))) {
            goodNdiRc.append("PATH=\"${NUTS_WORKSPACE_APPS}" + ndiAppsFolder.toString().substring(
                    ws.locations().getStoreLocation(NutsStoreLocation.APPS).length()
            ) + ":${PATH}\"\n");
        }else{
            goodNdiRc.append("PATH=\"" + ndiAppsFolder + ":${PATH}\"\n");
        }

        //this test will be removed because if the path is define in later position, it wont be applied! So rather
        //put it twice than having this trouble to manage.
        //"[[ \":$PATH:\" != *\":" + ndiAppsFolder + ":\"* ]] && PATH=\"" + ndiAppsFolder + ":${PATH}\"\n"

        goodNdiRc.append("export PATH NUTS_VERSION NUTS_JAR NUTS_WORKSPACE \n");

        if (saveFile(ndiConfigFile, goodNdiRc.toString(), force)) {
            updatedNames.add(".nadmin-bashrc");
        }
        NutsTextManager factory = context.getWorkspace().formats().text();

        if (!updatedNames.isEmpty() && session.isTrace()) {
            if (!updatedNames.isEmpty()) {
                if (context.getSession().isPlainTrace()) {
                    context.getSession().out().printf((context.getSession().isPlainTrace() ? "force " : "") + "updating %s to point to workspace %s%n",
                            factory.styled(String.join(", ", updatedNames),NutsTextNodeStyle.primary(3)),
                            factory.styled(ws.locations().getWorkspaceLocation(),NutsTextNodeStyle.path())
                            );
                }
                context.getSession().getTerminal().ask()
                        .forBoolean(
                                "```error ATTENTION``` You may need to re-run terminal or issue \"%s\" in your current terminal for new environment to take effect.%n"
                                        + "Please type 'ok' if you agree, 'why' if you need more explanation or 'cancel' to cancel updates.",
                                factory.styled(". ~/" + getBashrcName(),NutsTextNodeStyle.path())
                        )
                        .setHintMessage("")
                        .setSession(context.getSession())
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
                                    throw new NutsValidationException(context.getSession(), "Sorry... but you need to type 'ok', 'why' or 'cancel'");
                                }
                                String r = response.toString();
                                if ("ok".equalsIgnoreCase(r)) {
                                    return true;
                                }
                                if ("why".equalsIgnoreCase(r)) {
                                    PrintStream out = context.getSession().out();
                                    out.printf("\\\"%s\\\" is a special file in your home that is invoked upon each interactive terminal launch.%n", factory.styled(getBashrcName(),NutsTextNodeStyle.path()));
                                    out.print("It helps configuring environment variables. ```sh nuts``` make usage of such facility to update your **PATH** env variable\n");
                                    out.print("to point to current ```sh nuts``` workspace, so that when you call a ```sh nuts``` command it will be resolved correctly...\n");
                                    out.printf("However updating \\\"%s\\\" does not affect the running process/terminal. So you have basically two choices :%n", factory.styled(getBashrcName(),NutsTextNodeStyle.path()));
                                    out.print(" - Either to restart the process/terminal (konsole, term, xterm, sh, bash, ...)%n");
                                    out.printf(" - Or to run by your self the \\\"%s\\\" script (don\\'t forget the leading dot)%n", factory.styled(". ~/" + getBashrcName(),NutsTextNodeStyle.path()));
                                    throw new NutsValidationException(context.getSession(), "Try again...'");
                                } else if ("cancel".equalsIgnoreCase(r) || "cancel!".equalsIgnoreCase(r)) {
                                    throw new NutsUserCancelException(context.getSession());
                                } else {
                                    throw new NutsValidationException(context.getSession(), "Sorry... but you need to type 'ok', 'why' or 'cancel'");
                                }
                            }
                        })
                        .getValue();
            }
        }
    }

}
