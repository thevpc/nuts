package net.thevpc.nuts.toolbox.noapi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLineContext;
import net.thevpc.nuts.cmdline.NCommandLineProcessor;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.toolbox.noapi.model.NoapiCmdData;
import net.thevpc.nuts.toolbox.noapi.service.NOpenAPIService;
import net.thevpc.nuts.util.NAssert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NoapiMain implements NApplication {

    private NOpenAPIService service;
    private NoapiCmdData ref = new NoapiCmdData();

    private List<NoapiCmdData> data = new ArrayList<>();

    public static void main(String[] args) {
        new NoapiMain().runAndExit(args);
    }

    @Override
    public void run(NApplicationContext appContext) {
        this.service = new NOpenAPIService(appContext);
        ref.setCommand("pdf");
        appContext.processCommandLine(new NCommandLineProcessor() {
            @Override
            public boolean onCmdNextOption(NArgument option, NCommandLine commandLine, NCommandLineContext context) {
                NSession session = commandLine.getSession();
                switch (option.asString().get(session)) {
                    case "--yaml": {
                        commandLine.nextBoolean();
                        ref.setOpenAPIFormat("yaml");
                        if (!data.isEmpty()) {
                            data.get(data.size() - 1).setOpenAPIFormat("yaml");
                        }
                        return true;
                    }
                    case "--json": {
                        commandLine.nextBoolean();
                        ref.setOpenAPIFormat("json");
                        if (!data.isEmpty()) {
                            data.get(data.size() - 1).setOpenAPIFormat("json");
                        }
                        return true;
                    }
                    case "--keep": {
                        commandLine.nextBoolean();
                        ref.setKeep(true);
                        if (!data.isEmpty()) {
                            data.get(data.size() - 1).setKeep(true);
                        }
                        return true;
                    }
                    case "--vars": {
                        NArgument a = commandLine.nextString().get();
                        if (a.isActive()) {
                            String vars = a.getStringValue().get();
                            ref.setVars(vars);
                            if (!data.isEmpty()) {
                                data.get(data.size() - 1).setVars(vars);
                            }
                        }
                        return true;
                    }
                    case "--var": {
                        NArgument a = commandLine.nextString().get();
                        if (a.isActive()) {
                            String vars = a.getStringValue().get();
                            NArgument b = NArgument.of(vars);
                            if (b.isActive()) {
                                ref.getVarsMap().put(b.getKey().toStringLiteral(), b.getValue().toStringLiteral());
                                if (!data.isEmpty()) {
                                    data.get(data.size() - 1).getVarsMap().put(b.getKey().toStringLiteral(), b.getValue().toStringLiteral());
                                }
                            }
                        }
                        return true;
                    }
                    case "--open-api": {
                        commandLine.nextBoolean();
                        ref.setOpenAPI(true);
                        if (!data.isEmpty()) {
                            data.get(data.size() - 1).setOpenAPI(true);
                        }
                        return true;
                    }
                    case "--pdf": {
                        commandLine.nextBoolean();
                        ref.setCommand("pdf");
                        if (!data.isEmpty()) {
                            data.get(data.size() - 1).setCommand("pdf");
                        }
                        return true;
                    }
                    case "--target": {
                        NArgument a = commandLine.nextString().get();
                        if (a.isActive()) {
                            String target = a.getStringValue().get();
                            if (target.contains("*")) {
                                ref.setTarget(target);
                            }
                            if (!data.isEmpty()) {
                                data.get(data.size() - 1).setTarget(target);
                            }
                        }
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean onCmdNextNonOption(NArgument nonOption, NCommandLine commandLine, NCommandLineContext context) {
                NSession session = commandLine.getSession();
                NoapiCmdData c = new NoapiCmdData();
                c.setCommand(ref.getCommand());
                c.setKeep(ref.isKeep());
                c.setOpenAPI(ref.isOpenAPI());
                c.setTarget(ref.getTarget());
                c.setVars(ref.getVars());
                c.setVarsMap(new HashMap<>(ref.getVarsMap()));
                NArgument pathArg = commandLine.next().get(session);
                c.setPath(pathArg.key());
                data.add(c);
                return true;
            }

            @Override
            public void onCmdFinishParsing(NCommandLine commandLine, NCommandLineContext context) {
                NSession session = commandLine.getSession();
                if (data.isEmpty()) {
                    commandLine.throwMissingArgument();
                }
                for (NoapiCmdData d : data) {
                    NAssert.requireNonBlank(d.getPath(), "path", session);
                    if (!"pdf".equals(d.getCommand())) {
                        throw new NIllegalArgumentException(session, NMsg.ofCstyle("unsupported command %s", d.getCommand()));
                    }
                }
            }

            @Override
            public void onCmdExec(NCommandLine commandLine, NCommandLineContext context) {
                for (NoapiCmdData d : data) {
                    switch (d.getCommand()) {
                        case "pdf": {
                            NOpenAPIService service = new NOpenAPIService(appContext);
                            service.run(d.getPath(), d.getTarget(), d.getVars(), d.getVarsMap(), d.isKeep());
                            break;
                        }
                    }
                }
            }

        });
    }


}
