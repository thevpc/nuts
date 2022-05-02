package net.thevpc.nuts.toolbox.ncode;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.toolbox.ncode.bundles.strings.StringComparator;
import net.thevpc.nuts.toolbox.ncode.bundles.strings.StringComparators;
import net.thevpc.nuts.toolbox.ncode.filters.JavaSourceFilter;
import net.thevpc.nuts.toolbox.ncode.filters.PathSourceFilter;
import net.thevpc.nuts.toolbox.ncode.processors.JavaSourceFormatter;
import net.thevpc.nuts.toolbox.ncode.processors.PathSourceFormatter;
import net.thevpc.nuts.toolbox.ncode.sources.SourceFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.thevpc.nuts.toolbox.ncode.SourceNavigator.navigate;

class NCodeMainCmdProcessor implements NutsAppCmdProcessor {
    List<String> paths = new ArrayList<>();
    List<StringComparator> typeComparators = new ArrayList<>();
    List<StringComparator> fileComparators = new ArrayList<>();
    boolean caseInsensitive = false;

    @Override
    public void onCmdInitParsing(NutsCommandLine commandline, NutsApplicationContext context) {
        commandline.setExpandSimpleOptions(true);
    }

    @Override
    public boolean onCmdNextOption(NutsArgument option, NutsCommandLine commandline, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        switch (option.getStringKey().get(session)) {
            case "-i": {
                option = commandline.nextBoolean().get(session);
                caseInsensitive = option.getBooleanValue().get(session);
                return true;
            }
            case "-t": {
                typeComparators.add(comp(commandline.nextString().get(session).getStringValue().get(session)));
                return true;
            }
            case "-f": {
                fileComparators.add(comp(commandline.nextString().get(session).getStringValue().get(session)));
                return true;
            }
        }
        return false;
    }

    private StringComparator comp(String x) {
        boolean negated = false;
        if (x.startsWith("!")) {
            negated = true;
            x = x.substring(1);
        }
        if (!x.startsWith("^")) {
            x = "*" + x;
        }
        if (!x.startsWith("$")) {
            x = x + "*";
        }
        StringComparator c = caseInsensitive ? StringComparators.ilike(x) : StringComparators.like(x);
        if (negated) {
            c = StringComparators.not(c);
        }
        return c;
    }

    @Override
    public boolean onCmdNextNonOption(NutsArgument nonOption, NutsCommandLine commandline, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        paths.add(commandline.next().flatMap(NutsValue::asString).get(session));
        return true;
    }

    @Override
    public void onCmdExec(NutsCommandLine commandline, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        if (paths.isEmpty()) {
            paths.add(".");
        }
        if(typeComparators.isEmpty() && fileComparators.isEmpty()){
            commandline.throwError(NutsMessage.plain("missing filter"),session);
        }
        List<Object> results=new ArrayList<>();
        if(!typeComparators.isEmpty()) {
            for (String path : paths) {
                navigate(SourceFactory.create(new File(path)), new JavaSourceFilter(typeComparators, fileComparators), new JavaSourceFormatter(), session, results);
            }
        }else{
            for (String path : paths) {
                navigate(SourceFactory.create(new File(path)), new PathSourceFilter(fileComparators), new PathSourceFormatter(), session, results);
            }
        }
        session.out().printlnf(results);
    }

}
