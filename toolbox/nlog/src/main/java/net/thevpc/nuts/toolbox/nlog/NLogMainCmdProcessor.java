package net.thevpc.nuts.toolbox.nlog;

import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.NLiteral;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineProcessor;
import net.thevpc.nuts.toolbox.nlog.filter.*;
import net.thevpc.nuts.toolbox.nlog.model.NLogFilterConfig;

import java.util.ArrayList;
import java.util.List;

class NLogMainCmdProcessor implements NCmdLineProcessor {
    private List<String> paths = new ArrayList<>();
    private NLogFilterConfig config = new NLogFilterConfig();
    private LineFilterBuilder filter = new LineFilterBuilder();
    private NApplicationContext applicationContext;
    private boolean caseSensitive;

    public NLogMainCmdProcessor(NApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onCmdInitParsing(NCmdLine commandLine, NCmdLineContext context) {
        commandLine.setExpandSimpleOptions(true);
    }

    @Override
    public boolean onCmdNextOption(NArg option, NCmdLine commandLine, NCmdLineContext context) {
        if (commandLine.withNextFlag((v, a, s) -> {
            config.setCaseInsensitive(v);
        }, "-i", "--case-insensitive")) {
            return true;
        } else if (commandLine.withNextFlag((v, a, s) -> {
            config.setLineNumber(v);
        }, "-n", "--line-number")) {
            return true;
        } else if (commandLine.withNextEntry((v, a, s) -> {
            String[] n = v.split("[:]");
            long from = -1;
            long to = -1;
            if (n.length == 0) {
                //
            } else if (n.length == 1) {
                from = NLiteral.of(n[0]).asLong().orElse(-1L);
                to = from;
            } else if (n.length == 2) {
                from = NLiteral.of(n[0]).asLong().orElse(-1L);
                to = NLiteral.of(n[1]).asLong().orElse(-1L);
            } else {
                commandLine.pushBack(a);
                commandLine.throwUnexpectedArgument();
            }
            config.setFrom(from);
            config.setTo(to);
        }, "--range")) {
            return true;
        } else if (commandLine.withNextEntry((v, a, s) -> {
            String[] n = v.split("[:]");
            int from = 0;
            int to = 0;
            if (n.length == 0) {
                //
            } else if (n.length == 1) {
                from = NLiteral.of(n[0]).asInt().orElse(0);
                to = from;
            } else if (n.length == 2) {
                from = NLiteral.of(n[0]).asInt().orElse(0);
                to = NLiteral.of(n[1]).asInt().orElse(0);
            } else {
                commandLine.pushBack(a);
                commandLine.throwUnexpectedArgument();
            }
            config.setWindowMin(from);
            config.setWindowMax(to);
        }, "--window")) {
            return true;
        } else if (commandLine.withNextEntry((v, a, s) -> {
            int value = NLiteral.of(v).asInt().orElse(0);
            config.setWindowMin(value);
        }, "--previous")) {
            return true;
        } else if (commandLine.withNextEntry((v, a, s) -> {
            int value = NLiteral.of(v).asInt().orElse(0);
            config.setWindowMax(value);
        }, "--next")) {
            return true;
        } else if (commandLine.withNextFlag((v, a, s) -> {
            if (v) {
                filter.and();
            }
        }, "--and")) {
            return true;
        } else if (commandLine.withNextFlag((v, a, s) -> {
            if (v) {
                filter.or();
            }
        }, "--or")) {
            return true;
        } else if (commandLine.withNextFlag((v, a, s) -> {
            caseSensitive = !v;
        }, "-i", "ignore-case")) {
            return true;
        } else if (commandLine.withNextEntry((v, a, s) -> {
            filter.add(new ContainsLineFilter(v, caseSensitive));
        }, "--contains")) {
            return true;
        } else if (commandLine.withNextEntry((v, a, s) -> {
            filter.add(new StartsWithLineFilter(v, caseSensitive));
        }, "--start-with")) {
            return true;
        } else if (commandLine.withNextEntry((v, a, s) -> {
            filter.add(new EndsWithLineFilter(v, caseSensitive));
        }, "--ends-with")) {
            return true;
        } else if (commandLine.withNextEntry((v, a, s) -> {
            filter.add(new RegexpLineFilter(v, caseSensitive));
        }, "--regexp", "--regex")) {
            return true;
        } else if (commandLine.withNextEntry((v, a, s) -> {
            filter.add(new GlobLineFilter(v, caseSensitive));
        }, "--glob", "--like")) {
            return true;
        } else if (commandLine.withNextFlag((v, a, s) -> {
            if(v) {
                filter.add(new JavaExceptionLineFilter());
            }
        }, "--java-exception", "--exception")) {
            return true;
        }
        return false;
    }


    @Override
    public boolean onCmdNextNonOption(NArg nonOption, NCmdLine commandLine, NCmdLineContext context) {
        if (paths.size() == 0 && nonOption.toString().equals("and")) {
            commandLine.next();
            filter.and();
        } else if (paths.size() == 0 && nonOption.toString().equals("or")) {
            commandLine.next();
            filter.or();
        } else {
            NSession session = applicationContext.getSession();
            paths.add(commandLine.next().flatMap(NLiteral::asString).get(session));
        }
        return true;
    }

    @Override
    public void onCmdExec(NCmdLine commandLine, NCmdLineContext context) {
        NSession session = applicationContext.getSession();
        if (paths.isEmpty()) {
            commandLine.throwMissingArgumentByName("path");
        }
        boolean showFileName = false;
        if (paths.size() > 1) {
            showFileName = true;
        }
        NLogCommand cmd = new NLogCommand();
        config.setFilter(this.filter.build());
        cmd.run(paths,config, session,showFileName);
    }

}
