package net.thevpc.nuts.toolbox.noapi;

import net.thevpc.nuts.*;

import java.nio.file.Paths;

public class NOpenAPIMain extends NutsApplication {

    private NOpenAPIService service;

    public static void main(String[] args) {
        new NOpenAPIMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        String[] args = appContext.getArguments();
        this.service = new NOpenAPIService(appContext);
        NutsCommandLine cmdLine = appContext.getCommandLine();
        appContext.processCommandLine(new NutsCommandLineProcessor() {
            String command;
            String path;
            String target;
            boolean openAPI;
            boolean keep;
            String openAPIFormat;

            @Override
            public boolean nextOption(NutsArgument option, NutsCommandLine commandline) {
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
                    case "--openAPI": {
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
            public boolean nextNonOption(NutsArgument nonOption, NutsCommandLine commandline) {
                if (path == null) {
                    path = commandline.nextString().getStringKey();
                    return true;
                } else if (target == null) {
                    target = commandline.nextString().getStringKey();
                    return true;
                }
                return false;
            }

            @Override
            public void prepare(NutsCommandLine commandline) {
                if (path == null) {
                    commandline.required("missing path");
                }
                if (command == null) {
                    command = "pdf";
                }
            }

            @Override
            public void exec() {
                switch (command) {
                    case "pdf": {
                        NOpenAPIService service = new NOpenAPIService(appContext);
                        service.run(path, target,keep);
                    }
                }
            }
        });
    }

}
