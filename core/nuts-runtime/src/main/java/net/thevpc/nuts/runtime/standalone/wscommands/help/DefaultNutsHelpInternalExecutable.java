/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.exec.DefaultInternalNutsExecutableCommand;
//import net.thevpc.nuts.runtime.standalone.util.fprint.FormattedPrintStream;


/**
 *
 * @author thevpc
 */
public class DefaultNutsHelpInternalExecutable extends DefaultInternalNutsExecutableCommand {
    private final NutsLogger LOG;
    public DefaultNutsHelpInternalExecutable(String[] args, NutsSession session) {
        super("help", args, session);
        LOG=NutsLogger.of(DefaultNutsHelpInternalExecutable.class,session);
    }

    @Override
    public void execute() {
        if (CoreNutsUtils.isIncludesHelpOption(args)) {
            showDefaultHelp();
            return;
        }
        List<String> helpFor = new ArrayList<>();
        NutsSession session = getSession();
        NutsCommandLine cmdLine = NutsCommandLine.of(args,session);
        NutsContentType outputFormat = NutsContentType.PLAIN;
        boolean helpColors=false;
        while (cmdLine.hasNext()) {
            NutsContentType of = CoreNutsUtils.readOptionOutputFormat(cmdLine);
            if (of != null) {
                outputFormat = of;
            } else {
                NutsArgument a = cmdLine.peek();
                if (a.isOption()) {
                    switch (a.getKey().getString()) {
                        case "--colors":
                        case "--ntf":{
                            NutsArgument c = cmdLine.nextBoolean();
                            if(c.isEnabled()) {
                                helpColors = c.getValue().getBoolean();
                            }
                            break;
                        }
                        default: {
                            session.configureLast(cmdLine);
                        }
                    }
                } else {
                    cmdLine.skip();
                    helpFor.add(a.getString());
                    helpFor.addAll(Arrays.asList(cmdLine.toStringArray()));
                    cmdLine.skipAll();
                }
            }
        }

        if(helpColors){
            NutsTexts txt = NutsTexts.of(session);
            NutsText n = txt.parser().parseResource("/net/thevpc/nuts/runtime/ntf-help.ntf",
                    txt.parser().createLoader(getClass().getClassLoader())
            );
            session.getTerminal().out().print(
                    n==null?("no help found for " + name):n.toString()
            );
        }
        switch (outputFormat) {
            case PLAIN: {
                NutsPrintStream fout = session.out();
                if (!helpColors && helpFor.isEmpty()) {
                    fout.println(NutsWorkspaceExt.of(session.getWorkspace()).getHelpText(session));
                    fout.flush();
                }
                for (String arg : helpFor) {
                    NutsExecutableInformation w = null;
                    if (arg.equals("help")) {
                        fout.println(arg + " :");
                        showDefaultHelp();
                        fout.flush();
                    } else {
                        try {
                            w = session.exec().addCommand(arg).which();
                        } catch (Exception ex) {
                            LOG.with().session(session).level(Level.FINE).error(ex).log( NutsMessage.jstyle("failed to execute : {0}", arg));
                            //ignore
                        }
                        if (w != null) {
                            fout.println(arg + " :");
                            fout.println(w.getHelpText());
                            fout.flush();
                        } else {
                            session.getTerminal().err().println(arg + " : Not found");
                        }
                    }
                }
                break;
            }
            default: {
                session=session.copy().setOutputFormat(outputFormat);
                NutsPrintStream fout = NutsPrintStream.ofInMemory(session);
                if (!helpColors && helpFor.isEmpty()) {
                    fout.println(NutsWorkspaceExt.of(session.getWorkspace()).getHelpText(session));
                    fout.flush();
                }
                for (String arg : helpFor) {
                    NutsExecutableInformation w = null;
                    if (arg.equals("help")) {
                        fout.println(arg + " :");
                        showDefaultHelp();
                        fout.flush();
                    } else {
                        try {
                            w = session.exec().addCommand(arg).which();
                        } catch (Exception ex) {
                            LOG.with().session(session).level(Level.FINE).error(ex).log( NutsMessage.jstyle("failed to execute : {0}", arg));
                            //ignore
                        }
                        if (w != null) {
                            fout.println(arg + " :");
                            fout.println(w.getHelpText());
                            fout.flush();
                        } else {
                            session.getTerminal().err().println(arg + " : Not found");
                        }
                    }
                }
                switch (outputFormat){
                    case XML:
                    case JSON:
                    case TSON:
                    case YAML:{
                        NutsTextBuilder builder = NutsTexts.of(session).parse(fout.toString())
                                .builder();
                        Object[] r = builder.lines().map(x -> {
                            if (true) {
                                return x.filteredText();
                            }
                            return (Object) x.filteredText();
                        }).toArray(Object[]::new);
                        session.out().printlnf(r);
                        break;
                    }
                    case TABLE:
                    case PROPS:
                    case TREE:
                    {
                        NutsTextBuilder builder = NutsTexts.of(session).parse(fout.toString())
                                .builder();
                        Object[] r = builder.lines().toArray(Object[]::new);
                        session.out().printlnf(r);
                        break;
                    }
                }

            }
        }

    }

}
