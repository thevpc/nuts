package net.thevpc.nuts.toolbox.noapi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.util.NutsUtils;

import java.util.ArrayList;
import java.util.List;

public class NoapiMain implements NutsApplication, NutsAppCmdProcessor {

    private NOpenAPIService service;
    private CmdData ref = new CmdData();

    class CmdData {
        String command;
        String path;
        String target;
        boolean openAPI;
        boolean keep;
        String openAPIFormat;
    }

    private List<CmdData> data = new ArrayList<>();

    public static void main(String[] args) {
        new NoapiMain().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        this.service = new NOpenAPIService(appContext);
        NutsCommandLine cmdLine = appContext.getCommandLine();
        ref.command = "pdf";
        appContext.processCommandLine(this);
    }

    @Override
    public boolean onCmdNextOption(NutsArgument option, NutsCommandLine commandline, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        switch (option.asString().get(session)) {
            case "--yaml": {
                commandline.nextBoolean();
                ref.openAPIFormat = "yaml";
                if (!data.isEmpty()) {
                    data.get(data.size() - 1).openAPIFormat = "yaml";
                }
                return true;
            }
            case "--json": {
                commandline.nextBoolean();
                ref.openAPIFormat = "json";
                if (!data.isEmpty()) {
                    data.get(data.size() - 1).openAPIFormat = "json";
                }
                return true;
            }
            case "--keep": {
                commandline.nextBoolean();
                ref.keep = true;
                if (!data.isEmpty()) {
                    data.get(data.size() - 1).keep = true;
                }
                return true;
            }
            case "--open-api": {
                commandline.nextBoolean();
                ref.openAPI = true;
                if (!data.isEmpty()) {
                    data.get(data.size() - 1).openAPI = true;
                }
                return true;
            }
            case "--pdf": {
                commandline.nextBoolean();
                ref.command = "pdf";
                if (!data.isEmpty()) {
                    data.get(data.size() - 1).command = "pdf";
                }
                return true;
            }
            case "--target": {
                String target = commandline.nextString().get().getStringValue().get();
                if (target.contains("*")) {
                    ref.target = target;
                }
                if (!data.isEmpty()) {
                    data.get(data.size() - 1).target = target;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCmdNextNonOption(NutsArgument nonOption, NutsCommandLine commandline, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        CmdData c = new CmdData();
        c.command = ref.command;
        c.keep = ref.keep;
        c.openAPI = ref.openAPI;
        c.target = ref.target;
        NutsArgument pathArg = commandline.next().get(session);
        c.path = pathArg.getKey().asString().get(session);
        data.add(c);
        return true;
    }

    @Override
    public void onCmdFinishParsing(NutsCommandLine commandline, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        if (data.isEmpty()) {
            commandline.throwMissingArgument(session);
        }
        for (CmdData d : data) {
            NutsUtils.requireNonBlank(d.path, session, "path");
            if (!"pdf".equals(d.command)) {
                throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("unsupported command %s", d.command));
            }
        }
    }

    @Override
    public void onCmdExec(NutsCommandLine commandline, NutsApplicationContext context) {
        for (CmdData d : data) {
            switch (d.command) {
                case "pdf": {
                    NOpenAPIService service = new NOpenAPIService(context);
                    service.run(d.path, d.target, d.keep);
                    break;
                }
            }
        }
    }
}
