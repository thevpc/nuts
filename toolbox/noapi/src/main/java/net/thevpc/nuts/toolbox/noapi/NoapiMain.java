package net.thevpc.nuts.toolbox.noapi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineProcessor;
import net.thevpc.nuts.cmdline.NArg;
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
        appContext.processCommandLine(new NCmdLineProcessor() {
            @Override
            public boolean onCmdNextOption(NArg option, NCmdLine commandLine, NCmdLineContext context) {
                NSession session = commandLine.getSession();
                switch (option.asString().get(session)) {
                    case "--yaml": {
                        commandLine.nextFlag();
                        ref.setOpenAPIFormat("yaml");
                        if (!data.isEmpty()) {
                            data.get(data.size() - 1).setOpenAPIFormat("yaml");
                        }
                        return true;
                    }
                    case "--json": {
                        commandLine.nextFlag();
                        ref.setOpenAPIFormat("json");
                        if (!data.isEmpty()) {
                            data.get(data.size() - 1).setOpenAPIFormat("json");
                        }
                        return true;
                    }
                    case "--keep": {
                        commandLine.nextFlag();
                        ref.setKeep(true);
                        if (!data.isEmpty()) {
                            data.get(data.size() - 1).setKeep(true);
                        }
                        return true;
                    }
                    case "--vars": {
                        NArg a = commandLine.nextEntry().get();
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
                        NArg a = commandLine.nextEntry().get();
                        if (a.isActive()) {
                            String vars = a.getStringValue().get();
                            NArg b = NArg.of(vars);
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
                        commandLine.nextFlag();
                        ref.setOpenAPI(true);
                        if (!data.isEmpty()) {
                            data.get(data.size() - 1).setOpenAPI(true);
                        }
                        return true;
                    }
                    case "--pdf": {
                        commandLine.nextFlag();
                        ref.setCommand("pdf");
                        if (!data.isEmpty()) {
                            data.get(data.size() - 1).setCommand("pdf");
                        }
                        return true;
                    }
                    case "--target": {
                        NArg a = commandLine.nextEntry().get();
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
            public boolean onCmdNextNonOption(NArg nonOption, NCmdLine commandLine, NCmdLineContext context) {
                NSession session = commandLine.getSession();
                NoapiCmdData c = new NoapiCmdData();
                c.setCommand(ref.getCommand());
                c.setKeep(ref.isKeep());
                c.setOpenAPI(ref.isOpenAPI());
                c.setTarget(ref.getTarget());
                c.setVars(ref.getVars());
                c.setVarsMap(new HashMap<>(ref.getVarsMap()));
                NArg pathArg = commandLine.next().get(session);
                c.setPath(pathArg.key());
                data.add(c);
                return true;
            }

            @Override
            public void onCmdFinishParsing(NCmdLine commandLine, NCmdLineContext context) {
                NSession session = commandLine.getSession();
                if (data.isEmpty()) {
                    commandLine.throwMissingArgument();
                }
                for (NoapiCmdData d : data) {
                    NAssert.requireNonBlank(d.getPath(), "path", session);
                    if (!"pdf".equals(d.getCommand())) {
                        throw new NIllegalArgumentException(session, NMsg.ofC("unsupported command %s", d.getCommand()));
                    }
                }
            }

            @Override
            public void onCmdExec(NCmdLine commandLine, NCmdLineContext context) {
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
