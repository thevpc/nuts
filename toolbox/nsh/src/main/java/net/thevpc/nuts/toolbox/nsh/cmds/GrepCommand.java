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
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nsh.AbstractNshBuiltin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.FileInfo;

/**
 * Created by vpc on 1/7/17.
 */
public class GrepCommand extends AbstractNshBuiltin {

    public GrepCommand() {
        super("grep", DEFAULT_SUPPORT);
    }

    public static String simpexpToRegexp(String pattern, boolean contains) {
        if (pattern == null) {
            pattern = "*";
        }
        int i = 0;
        char[] cc = pattern.toCharArray();
        StringBuilder sb = new StringBuilder();
        while (i < cc.length) {
            char c = cc[i];
            switch (c) {
                case '.':
                case '!':
                case '$':
                case '[':
                case ']':
                case '(':
                case ')':
                case '?':
                case '^':
                case '|':
                case '\\': {
                    sb.append('\\').append(c);
                    break;
                }
                case '*': {
//                    if (i + 1 < cc.length && cc[i + 1] == '*') {
//                        i++;
//                        sb.append("[a-zA-Z_0-9_$.-]*");
//                    } else {
//                        sb.append("[a-zA-Z_0-9_$-]*");
//                    }
                    sb.append(".*");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
            i++;
        }
        if (!contains) {
            sb.insert(0, '^');
            sb.append('$');
        }
        return sb.toString();
    }

    public int execImpl(String[] args, JShellExecutionContext context) {
        NutsCommandLine commandLine = cmdLine(args, context);
        Options options = new Options();
        List<FileInfo> files = new ArrayList<>();
        String expression = null;
        NutsPrintStream out = context.out();
        NutsArgument a;
        while (commandLine.hasNext()) {
            if (commandLine.next("-") != null) {
                files.add(null);
            } else if (commandLine.next("-e", "--regexp") != null) {
                //options.regexp = true;
            } else if (commandLine.next("-v", "--invert-match") != null) {
                options.invertMatch = true;
            } else if (commandLine.next("-w", "--word-regexp") != null) {
                options.word = true;
            } else if (commandLine.next("-x", "--line-regexp") != null) {
                options.lineRegexp = true;
            } else if (commandLine.next("-i", "--ignore-case") != null) {
                options.ignoreCase = true;
            } else if ((a = commandLine.next("-H", "--highlighter")) != null) {
                options.highlighter = NutsUtilStrings.trim(a.getValue().getString());
            } else if (commandLine.next("--version") != null) {
                out.printf("%s\n", "1.0");
                return 0;
            } else if (commandLine.next("-n") != null) {
                options.n = true;
            } else if (commandLine.next("--help") != null) {
                out.printf("%s\n", getHelp());
                return 0;
            } else if (commandLine.peek().isNonOption()) {
                if (expression == null) {
                    expression = commandLine.next().getString();
                } else {
                    String path = commandLine.next().getString();
                    files.add(new FileInfo(NutsPath.of(path, context.getSession()), options.highlighter));
                }
            } else {
                context.configureLast(commandLine);
            }
        }
        if (files.isEmpty()) {
            files.add(null);
        }
        if (expression == null) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("missing Expression"), 2);
        }
        String baseExpr = simpexpToRegexp(expression, true);
        if (options.word) {
            baseExpr = "\\b" + baseExpr + "\\b";
        }
        if (options.lineRegexp) {
            baseExpr = "^" + baseExpr + "$";
        }
        if (options.ignoreCase) {
            baseExpr = "(?i)" + baseExpr;
        }
        Pattern p = Pattern.compile(baseExpr);
        //text mode
        boolean prefixFileName = files.size() > 1;
        int x = 0;
        List<ResultItem> results = new ArrayList<>();
        for (FileInfo f : files) {
            x = grepFile(f, p, options, context, prefixFileName, results);
        }
        switch (context.getSession().getOutputFormat()) {
            case PLAIN: {
                for (ResultItem result : results) {
                    if (options.n) {
                        if (result.path != null && prefixFileName) {
                            out.print(result.path);
                            out.print(":");
                        }
                        out.print(result.number);
                        out.print(":");
                    }
                    out.println(result.line);
                }
                break;
            }
            default: {
                if (options.n) {
                    out.printlnf(results);
                } else {
                    out.printlnf(results.stream().map(r -> r.line).collect(Collectors.toList()));
                }
            }
        }
        if (x != 0) {
            throwExecutionException("error", x, context.getSession());
        }
        return x;
    }

    protected int grepFile(FileInfo f, Pattern p, Options options, JShellExecutionContext context, boolean prefixFileName, List<ResultItem> results) {

        Reader reader = null;
        boolean closeReader = false;
        try {
            try {
                if (f == null) {
                    closeReader = false;
                    reader = new InputStreamReader(context.in());
                    processByLine(reader, options, p, f, results, context.getSession());
                } else if (f.getFile().isDirectory()) {
                    NutsPath[] files = f.getFile().getChildren();
                    if (files != null) {
                        for (NutsPath ff : files) {
                            grepFile(new FileInfo(ff, f.getHighlighter()), p, options, context, true, results);
                        }
                    }
                    return 0;
                } else {
                    closeReader = true;
                    reader = new InputStreamReader(f.getFile().getInputStream());
                    if (f.getHighlighter() == null) {
                        processByLine(reader, options, p, f, results, context.getSession());
                    } else {
                        String text = new String(context.getSession().io().copy().from(f.getFile()).getByteArrayResult());
                        processByText(text, options, p, f, results, context.getSession());
                    }
                }
            } finally {
                if (reader != null && closeReader) {
                    reader.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("%s", ex), ex, 100);
        }
        return 0;
    }

    private boolean isNewLine(NutsText t) {
        if (t.getType() == NutsTextType.PLAIN) {
            String txt = ((NutsTextPlain) t).getText();
            return (txt.equals("\n") || txt.equals("\r\n"));
        }
        return false;
    }

    private NutsTextBuilder readLine(NutsTextBuilder flattened, NutsSession session) {
        if (flattened.size() == 0) {
            return null;
        }
        List<NutsText> r = new ArrayList<>();
        while (flattened.size() > 0) {
            NutsText t = flattened.get(0);
            flattened.removeAt(0);
            if (isNewLine(t)) {
                break;
            }
            r.add(t);
        }
        return session.text().builder().appendAll(r);
    }

    private void processByLine(Reader reader, Options options, Pattern p, FileInfo f, List<ResultItem> results, NutsSession session) throws IOException {
        try (BufferedReader r = new BufferedReader(reader)) {
            String line = null;
            long nn = 1;
            while ((line = r.readLine()) != null) {
                boolean matches = p.matcher(line).matches();
                if (matches != options.invertMatch) {
                    NutsText cl = session.text().ofCode(f.getHighlighter(), line).highlight(session);
                    results.add(new ResultItem(f.getFile(), nn, cl));
                }
                nn++;
            }
        }
    }

    private void processByText(String text, Options options, Pattern p, FileInfo f, List<ResultItem> results, NutsSession session) throws IOException {
        NutsTextBuilder flattened = session.text().ofCode(f.getHighlighter(), text).highlight(session)
                .builder()
                .flatten();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(f.getFile().getInputStream()))) {
            String line = null;
            long nn = 1;
            while ((line = r.readLine()) != null) {
                NutsTextBuilder coloredLine = readLine(flattened, session);
                if (coloredLine == null) {
                    coloredLine = session.text().ofCode(f.getHighlighter(), line).highlight(session).builder();
                }
                Matcher matcher = p.matcher(line);
                boolean anyMatch = false;
                while (matcher.find()) {
                    anyMatch = true;
                    int pos = matcher.start();
                    int end = matcher.end();
                    NutsText toh = coloredLine.substring(pos, end);
                    coloredLine.replace(pos, end, session.text().ofStyled(toh, NutsTextStyle.underlined()));
                }
                if (anyMatch != options.invertMatch) {
                    results.add(new ResultItem(f.getFile(), nn, coloredLine.build()));
                }
                nn++;
            }
        }
    }

    private static class Options {

//        boolean regexp = false;
        boolean invertMatch = false;
        boolean word = false;
        boolean lineRegexp = false;
        boolean ignoreCase = false;
        String highlighter;
        boolean n = false;
    }

    private static class ResultItem {
        NutsPath path;
        long number;
        NutsText line;

        public ResultItem(NutsPath path, long number, NutsText line) {
            this.path = path;
            this.number = number;
            this.line = line;
        }
    }

}
