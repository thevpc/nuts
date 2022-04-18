package net.thevpc.nuts.toolbox.noapi;

import net.thevpc.nuts.*;

public class NoapiMain implements NutsApplication, NutsAppCmdProcessor {

    private NOpenAPIService service;
    String command;
    String path;
    String target;
    boolean openAPI;
    boolean keep;
    String openAPIFormat;

    public static void main(String[] args) {
        new NoapiMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        this.service = new NOpenAPIService(appContext);
        NutsCommandLine cmdLine = appContext.getCommandLine();
        appContext.processCommandLine(this);
    }

    @Override
    public boolean onCmdNextOption(NutsArgument option, NutsCommandLine commandline, NutsApplicationContext context) {
        switch (option.getString()) {
            case "--yaml": {
                commandline.nextBoolean();
                openAPIFormat = "yaml";
                return true;
            }
            case "--keep": {
                commandline.nextBoolean();
                keep = true;
                return true;
            }
            case "--json": {
                commandline.nextBoolean();
                openAPIFormat = "json";
                return true;
            }
            case "--open-api": {
                commandline.nextBoolean();
                openAPI = true;
                return true;
            }
            case "--pdf": {
                commandline.nextBoolean();
                command = "pdf";
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCmdNextNonOption(NutsArgument nonOption, NutsCommandLine commandline, NutsApplicationContext context) {
        if (path == null) {
            path = commandline.nextString().getKey().getString();
            return true;
        } else if (target == null) {
            target = commandline.nextString().getKey().getString();
            return true;
        }
        return false;
    }

    @Override
    public void onCmdFinishParsing(NutsCommandLine commandline, NutsApplicationContext context) {
        if (path == null) {
            commandline.required(NutsMessage.cstyle("missing path"));
        }
        if (command == null) {
            command = "pdf";
        }
    }

    @Override
    public void onCmdExec(NutsCommandLine commandline, NutsApplicationContext context) {
        switch (command) {
            case "pdf": {
                NOpenAPIService service = new NOpenAPIService(context);
                service.run(path, target, keep);
            }
        }
    }
}
