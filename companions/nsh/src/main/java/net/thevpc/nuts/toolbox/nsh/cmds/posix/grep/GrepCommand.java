/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds.posix.grep;

import net.thevpc.nuts.*;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.cmds.util.*;
import net.thevpc.nuts.toolbox.nsh.cmds.util.filter.JavaExceptionWindowFilter;
import net.thevpc.nuts.toolbox.nsh.cmds.util.filter.WindowFilterBuilder;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ColumnRuler;
import net.thevpc.nuts.toolbox.nsh.util.FileInfo;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NGlob;
import net.thevpc.nuts.util.NStringUtils;

/**
 * Created by vpc on 1/7/17.
 */
public class GrepCommand extends NShellBuiltinDefault {

    public GrepCommand() {
        super("grep", DEFAULT_SUPPORT, Options.class);
    }


    @Override
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        NSession session = context.getSession();
        Options options = context.getOptions();
        if (!options.withNutsOptions && options.expression == null) {
            options.expression = cmdLine.next().flatMap(NLiteral::asString).get(session);
        } else {
            String path = cmdLine.next().flatMap(NLiteral::asString).get(session);
            options.files.add(new FileInfo(NPath.of(path, session), options.highlighter));
        }
        return true;
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        NSession session = context.getSession();
        Options options = context.getOptions();
        NArg a;
        if (cmdLine.next("-").orNull() != null) {
            options.files.add(null);
            return true;
        } else if ((a = cmdLine.next("-e", "--regexp").orNull()) != null) {
            //options.regexp = true;
            return true;
        } else if ((a = cmdLine.nextEntry("--expr", "--like").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.expression = a.getStringValue().get();
            return true;
        } else if (cmdLine.next("-v", "--invert-match").orNull() != null) {
            options.invertMatch = true;
            return true;
        } else if (cmdLine.next("-w", "--word-regexp").orNull() != null) {
            options.word = true;
            return true;
        } else if (cmdLine.next("-x", "--line-regexp").orNull() != null) {
            options.lineRegexp = true;
            return true;
        } else if (cmdLine.next("-i", "--ignore-case").orNull() != null) {
            options.ignoreCase = true;
            return true;
        } else if (cmdLine.next("-r", "--recursive").orNull() != null) {
            options.recursive = true;
            options.followSymbolicLinks = false;
            return true;
        } else if (cmdLine.next("-R", "--dereference-recursive").orNull() != null) {
            options.recursive = true;
            options.followSymbolicLinks = true;
            return true;
        } else if ((a = cmdLine.next("--nx").orNull()) != null) {
            options.withNutsOptions = true;
            return true;
        } else if ((a = cmdLine.nextEntry("--file-name").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.fileNames.add(a.getStringValue().get());
            return true;
        } else if ((a = cmdLine.nextEntry("--file-iname").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.fileNamesIgnoreCase.add(a.getStringValue().get());
            return true;
        } else if ((a = cmdLine.next("--from").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.from = NLiteral.of(a).asLong().orElse(null);
            return true;
        } else if ((a = cmdLine.next("--to").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.to = NLiteral.of(a).asLong().orElse(null);
            return true;
        } else if ((a = cmdLine.next("--range").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            NumberRangeList rl = NumberRangeList.parse(a.getStringValue().get());
            if (rl != null) {
                NumberRange r = rl.toRange();
                options.from = r.getFrom();
                options.to = r.getTo();
            }
            return true;
        } else if ((a = cmdLine.next("--jex", "--java-exception").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.windowFilter.add(new JavaExceptionWindowFilter());
            return true;
        } else if ((a = cmdLine.next("-H", "--highlight", "--highlighter").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.highlighter = NStringUtils.trim(a.getStringValue().get(session));
            return true;
        } else if ((a = cmdLine.next("-S", "--selection-style").orNull()) != null) {
            processRequireNutsOption(a, cmdLine, options);
            options.selectionStyle = NStringUtils.trimToNull(a.getStringValue().get(session));
            return true;
        } else if (cmdLine.next("-n").isPresent()) {
            options.n = true;
            return true;
        } else {
            return false;
        }
    }

    private static void processRequireNutsOption(NArg a, NCmdLine cmdLine, Options options) {
        if (!options.withNutsOptions) {
            if (options.requireNutsOptions) {
                cmdLine.throwUnexpectedArgument(NMsg.ofC(" option can be used along with --nx", a));
            } else {
                options.withNutsOptions = true;
            }
        }
    }

    @Override
    protected void onCmdExec(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NPrintStream out = context.out();
        if (options.files.isEmpty()) {
            options.files.add(null);
        }
        NSession session = context.getSession();
        GrepFilter p = null;
        if (options.withNutsOptions && NBlankable.isBlank(options.expression)) {
            p = new NoGrepFilter();
        } else {
            NAssert.requireNonBlank(options.expression, "expression", session);
            p = new SimpleGrepFilter(options.expression, options.word, options.lineRegexp, options.ignoreCase);
        }


        //text mode
        boolean prefixFileName = false;
        if((options.files.size() > 1) || (
                options.files.size()==1
                        && options.files.get(0).getFile()!=null
                        && options.files.get(0).getFile().isDirectory())){
            prefixFileName=true;
        }
        MyConsumer myConsumer = new MyConsumer(session, options, out, prefixFileName, 1024);
        Predicate<NPath> fileName = new Predicate<NPath>() {
            List<Pattern> patterns = new ArrayList<>();

            {
                for (String fileName : options.fileNames) {
                    patterns.add(NGlob.of(session).toPattern(fileName));
                }
                for (String fileName : options.fileNames) {
                    patterns.add(
                            Pattern.compile(NGlob.of(session).toPatternString(fileName))
                    );
                }
            }

            @Override
            public boolean test(NPath nPath) {
                if (patterns.isEmpty()) {
                    return true;
                }
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(nPath.getName()).matches()) {
                        return true;
                    }
                }
                return false;
            }
        };
        long x = 0;
        for (FileInfo f : options.files) {
            if (f.getFile() == null) {
                x += grepFile(f, p, options, context, prefixFileName, myConsumer);
            } else if (f.getFile().isFile()) {
                x += grepFile(f, p, options, context, prefixFileName, myConsumer);
            } else if (f.getFile().isDirectory()) {
                if (options.recursive) {
                    Stack<FileInfo> stack = new Stack<>();
                    stack.add(f);
                    while (!stack.isEmpty()) {
                        FileInfo ff = stack.pop();
                        if (ff.getFile().isFile()) {

                            x += grepFile(f, p, options, context, prefixFileName, myConsumer);
                        } else if (ff.getFile().isDirectory()) {
                            for (NPath nPath : ff.getFile().list()) {
                                if (nPath.isDirectory() || (nPath.isFile() && fileName.test(nPath))) {
                                    stack.push(
                                            new FileInfo(
                                                    nPath, f.getHighlighter()
                                            )
                                    );
                                }
                            }
                        }
                    }
                }
            } else {

            }
        }
        myConsumer.flush();
//        if (x != 0) {
//            throwExecutionException("no occurraence found", x, session);
//        }
    }

    private static class MyConsumer implements Consumer<GrepResultItem> {
        List<GrepResultItem> results = new ArrayList<>();
        NSession session;
        Options options;
        NPrintStream out;
        boolean prefixFileName;
        int max;

        public MyConsumer(NSession session, Options options, NPrintStream out, boolean prefixFileName, int max) {
            this.session = session;
            this.options = options;
            this.out = out;
            this.prefixFileName = prefixFileName;
            this.max = max;
        }

        @Override
        public void accept(GrepResultItem grepResultItem) {
            results.add(grepResultItem);
            if (results.size() >= max) {
                flush();
            }
        }

        public void flush() {
            if (results.isEmpty()) {
                return;
            }
            switch (session.getOutputFormat()) {
                case PLAIN: {
                    ColumnRuler ruler = new ColumnRuler();
                    for (GrepResultItem result : results) {
                        if (options.n) {
                            if (result.path != null && prefixFileName) {
                                out.print(result.path);
                                out.print(":");
                            }
                            out.print(ruler.nextNum(result.number, session));
                        }
                        out.println(result.line);
                    }
                    break;
                }
                default: {
                    if (options.n) {
                        out.println(results);
                    } else {
                        out.println(results.stream().map(r -> r.line).collect(Collectors.toList()));
                    }
                }
            }
            results.clear();
        }
    }

    protected int grepFile(FileInfo f, GrepFilter p, Options options, NShellExecutionContext context, boolean prefixFileName, Consumer<GrepResultItem> results) {

        Reader reader = null;
        boolean closeReader = false;
        NSession session = context.getSession();
        try {
            if (f == null) {
                processByLine(options, p, f, results, context, session);
            } else if (f.getFile().isDirectory()) {
                for (NPath ff : f.getFile().stream()) {
                    grepFile(new FileInfo(ff, f.getHighlighter()), p, options, context, true, results);
                }
                return 0;
            } else {
                if (f.getHighlighter() == null) {
                    processByLine(options, p, f, results, context, session);
                } else {
                    processByText(options, p, f, results, session);
                }
            }
        } catch (IOException ex) {
            throw new NExecutionException(session, NMsg.ofC("%s", ex), ex, 100);
        }
        return 0;
    }

    private boolean isNewLine(NText t) {
        if (t.getType() == NTextType.PLAIN) {
            String txt = ((NTextPlain) t).getText();
            return (txt.equals("\n") || txt.equals("\r\n"));
        }
        return false;
    }

    private NTextBuilder readLine(NTextBuilder flattened, NSession session) {
        if (flattened.size() == 0) {
            return null;
        }
        List<NText> r = new ArrayList<>();
        while (flattened.size() > 0) {
            NText t = flattened.get(0);
            flattened.removeAt(0);
            if (isNewLine(t)) {
                break;
            }
            r.add(t);
        }
        return NTexts.of(session).ofBuilder().appendAll(r);
    }

    private void processByLine(Options options, GrepFilter p, FileInfo f, Consumer<GrepResultItem> results, NShellExecutionContext context, NSession session) throws IOException {
        try (Reader reader = (f == null ? new InputStreamReader(context.in()) : f.getFile().getReader())) {
            processByText0(reader, null, options, p, f, results, session);
        }
    }

    private List<GrepResultItem> createResult(WindowObject<NNumberedObject<String>> wline, NTextBuilder coloredLine, Options options, GrepFilter p, FileInfo f, NSession session) {
        List<GrepResultItem> result = new LinkedList<>();
        List<NNumberedObject<String>> items = wline.getItems();
        for (int i = 0; i < items.size(); i++) {
            NNumberedObject<String> line = items.get(i);
            //long nn, String line
            NTextBuilder coloredLine0 = coloredLine;
            if (coloredLine0 == null) {
                coloredLine0 = NTexts.of(session).ofCode(f.getHighlighter(), line.getObject()).highlight(session).builder();
            }
            if (i == wline.getPivotIndex()) {
                boolean anyMatch = p.processPivot(line.getObject(), coloredLine0, selectionStyle(options), session);
                if (anyMatch != options.invertMatch) {
                    result.add(new GrepResultItem(f.getFile(), line.getNumber(), coloredLine0.build(), true));
                }
            } else {
                result.add(new GrepResultItem(f.getFile(), line.getNumber(), coloredLine0.build(), false));
            }
        }
        return result;
    }

    private void processByText(Options options, GrepFilter p, FileInfo f, Consumer<GrepResultItem> results, NSession session) throws IOException {
        String text = new String(NCp.of(session).from(f.getFile()).getByteArrayResult());
        if (NBlankable.isBlank(f.getHighlighter())) {
            f.setHighlighter(f.getFile().getContentType());
        }
        NTextBuilder flattened = NTexts.of(session).ofCode(f.getHighlighter(), text).highlight(session)
                .builder()
                .flatten();
        try (Reader in = f.getFile().getReader()) {
            processByText0(in, flattened, options, p, f, results, session);
        }
    }

    private void processByText0(Reader reader, NTextBuilder flattened, Options options, GrepFilter p, FileInfo f, Consumer<GrepResultItem> results, NSession session) {
        Iterator<NNumberedObject<String>> li = new BufferedLineIterator(reader, options.from, options.to);
        Iterator<WindowObject<NNumberedObject<String>>> it;
        if (options.windowFilter.isEmpty()) {
            it = new Iterator<WindowObject<NNumberedObject<String>>>() {
                @Override
                public boolean hasNext() {
                    return li.hasNext();
                }

                @Override
                public WindowObject<NNumberedObject<String>> next() {
                    NNumberedObject<String> next = li.next();
                    return next == null ? null : new WindowObject<>(Arrays.asList(next), 0);
                }
            };
        } else {
            it = new WindowFilterIterator<>(li, options.windowFilter.build(), 0, 0);
        }
        while (it.hasNext()) {
            WindowObject<NNumberedObject<String>> line = it.next();
            NTextBuilder coloredLine = flattened == null ? null : readLine(flattened, session);
            for (GrepResultItem grepResultItem : createResult(line, coloredLine, options, p, f, session)) {
                results.accept(grepResultItem);
            }
        }
    }


    public NTextStyles selectionStyle(Options options) {
        String s = options.selectionStyle;
        NTextStyles def = NTextStyles.of(NTextStyle.secondary(2));
        if (NBlankable.isBlank(s)) {
            return def;
        }
        return NTextStyles.parse(s).orElse(def);
    }

    private static class Options {

        //        boolean regexp = false;
        boolean requireNutsOptions = false;
        boolean withNutsOptions = false;
        boolean invertMatch = false;
        boolean recursive = false;
        boolean followSymbolicLinks = true;
        List<String> fileNames = new ArrayList<>();
        List<String> fileNamesIgnoreCase = new ArrayList<>();
        boolean word = false;
        boolean lineRegexp = false;
        boolean ignoreCase = false;
        String highlighter;
        String selectionStyle;
        WindowFilterBuilder<NNumberedObject<String>> windowFilter = new WindowFilterBuilder<>();
        boolean n = false;
        int windowBefore = 0;
        int windowAfter = 0;
        Long from;
        Long to;
        List<FileInfo> files = new ArrayList<>();
        String expression = null;
    }

    private static class GrepResultItem {
        NPath path;
        long number;
        NText line;
        Boolean match;

        public GrepResultItem(NPath path, long number, NText line, Boolean match) {
            this.path = path;
            this.number = number;
            this.line = line;
            this.match = match;
        }
    }


}
