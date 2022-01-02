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
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles._IOUtils;
import net.thevpc.nuts.toolbox.nsh.bundles._StringUtils;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ColumnRuler;
import net.thevpc.nuts.toolbox.nsh.util.FileInfo;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class CatCommand extends SimpleJShellBuiltin {

    public CatCommand() {
        super("cat", DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsArgument a;

        if (commandLine.next("-") != null) {
            options.files.add(null);
            return true;
        } else if ((a = commandLine.nextBoolean("-n", "--number")) != null) {
            options.n = a.getBooleanValue();
            return true;
        } else if ((a = commandLine.nextBoolean("-t", "--show-tabs")) != null) {
            options.T = a.getBooleanValue();
            return true;
        } else if ((a = commandLine.nextBoolean("-E", "--show-ends")) != null) {
            options.E = a.getBooleanValue();
            return true;
        } else if ((a = commandLine.next("-H", "--highlight", "--highlighter")) != null) {
            options.highlighter = NutsUtilStrings.trim(a.getValue().getString());
            return true;
        } else if (!commandLine.peek().isOption()) {
            String path = commandLine.next().getString();
            options.files.add(new FileInfo(NutsPath.of(path, context.getSession()), options.highlighter));
            return true;
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.files.isEmpty()) {
            options.files.add(null);
        }
        NutsPrintStream out = context.getSession().out();
        try {
            options.currentNumber = 1;

            OutputStream os = null;
            boolean plain = true;
            if (context.getSession().getOutputFormat() == NutsContentType.PLAIN) {
                os = out.asOutputStream();
            } else {
                plain = false;
            }
            List<CatResult> results = new ArrayList<>();
            //text mode
            for (FileInfo f : options.files) {
                boolean close = false;
                InputStream in = null;
                if (f.getFile() == null) {
                    in = context.in();
                    if (f.getHighlighter() == null) {
                        f.setHighlighter("plain");
                    }else if(f.getHighlighter().isEmpty()){
                        f.setHighlighter("ntf");
                    }
                } else {
                    in = f.getFile().getInputStream();
                    if (f.getHighlighter() == null) {
                        f.setHighlighter("plain");
                    } else if (f.getHighlighter().isEmpty()) {
                        f.setHighlighter(f.getFile().getContentType());
                        if (f.getHighlighter() == null) {
                            f.setHighlighter("plain");
                        }
                    }
                    close = true;
                }
                try {
                    if (plain) {
                        catText(in, os, options, context, f);
                    } else {
                        catText2(in, options, context, f, results);
                    }
                } finally {
                    if (close) {
                        in.close();
                    }
                }
            }
            if (!plain) {
                out.printf(results);
            }
        } catch (IOException ex) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("%s", ex), ex, 100);
        }
    }

    private void catText2(InputStream in, Options options, JShellExecutionContext context, FileInfo info, List<CatResult> results) throws IOException {
        boolean whole = true;
        NutsSession session = context.getSession();
        if (whole && info.getHighlighter() != null) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            NutsCp.of(session).from(in).to(bout).run();
            String text = bout.toString();
            NutsTextBuilder nutsText = NutsTexts.of(session).ofCode(info.getHighlighter(), text).highlight(session)
                    .builder()
                    .flatten();
            List<NutsText> children = nutsText.getChildren();
            Tracker tracker = new Tracker();
            boolean n = options.n;
            options.n = false;
            while (true) {
                NutsText line = nextLine(children, session, tracker, options, true);
                if (line != null) {
                    CatResult r = new CatResult();
                    if (n) {
                        r.number = tracker.line;
                    }
                    r.line = line;
                    results.add(r);
                } else {
                    break;
                }
            }
            options.n = n;
        } else {
            //do not close!!
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = reader.readLine()) != null) {
                CatResult r = new CatResult();
                if (options.n) {
                    r.number = options.currentNumber;
                }
                if (options.T) {
                    line = line.replace("\t", "^I");
                }
                NutsTextCode c = NutsTexts.of(session).ofCode(info.getHighlighter(), line);
                line = c.highlight(session).toString();
                if (options.E) {
                    line += "$";
                }
                r.line = NutsTexts.of(session).ofPlain(line);
                options.currentNumber++;
            }
        }
    }

    private void catText(InputStream in, OutputStream os, Options options, JShellExecutionContext context, FileInfo info) throws IOException {
        if (info.getHighlighter() == null/* || "plain".equalsIgnoreCase(info.getHighlighter()) || "text".equalsIgnoreCase(info.getHighlighter())*/) {
            if (!options.n && !options.T && !options.E) {
                _IOUtils.copy(in, os, 4096 * 2);
                return;
            }
        }
        boolean whole = true;
        NutsSession session = context.getSession();
        if (whole && info.getHighlighter() != null) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            NutsCp.of(session).from(in).to(bout).run();
            String text = bout.toString();
            NutsTextBuilder nutsText = NutsTexts.of(session).ofCode(info.getHighlighter(), text).highlight(session)
                    .builder()
                    .flatten();
            NutsPrintStream out = NutsPrintStream.of(os, session);
            List<NutsText> children = nutsText.getChildren();
            Tracker tracker = new Tracker();
            while (true) {
                NutsText line = nextLine(children, session, tracker, options, false);
                if (line != null) {
                    out.printf(line);
                } else {
                    break;
                }
            }
        } else {
            NutsPrintStream out = NutsPrintStream.of(os, session);
            try {

                //do not close!!
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (options.n) {
                        out.print(_StringUtils.formatRight(String.valueOf(options.currentNumber), 6));
                        out.print("  ");
                    }
                    if (options.T) {
                        line = line.replace("\t", "^I");
                    }

                    NutsTextCode c = NutsTexts.of(session).ofCode(info.getHighlighter(), line);
                    line = c.highlight(session).toString();

                    out.print(line);

                    if (options.E) {
                        out.println("$");
                    }
                    out.println();
                    options.currentNumber++;
                }
            } finally {
                out.flush();
            }
        }
    }

    private NutsText nextLine(List<NutsText> t, NutsSession session, Tracker tracker, Options options, boolean skipNewline) {
        NutsTextBuilder b = NutsTexts.of(session).builder();
        while (!t.isEmpty()) {
            NutsText ii = t.remove(0);
            NutsText n = nextNode(ii, session, tracker, options);
            if (tracker.wasNewline) {
                if (!skipNewline) {
                    b.append(n);
                }
                return b.build();
            } else {
                b.append(n);
            }
        }
        return null;
    }

    private NutsText nextNode(NutsText t, NutsSession session, Tracker tracker, Options options) {
        switch (t.getType()) {
            case PLAIN: {
                String text = ((NutsTextPlain) t).getText();
                NutsTextBuilder tb = NutsTexts.of(session).builder();
                if (options.n && tracker.wasNewline) {
                    tb.append(tracker.ruler.nextNum(tracker.line, session));
                }
                if (text.charAt(0) == '\n' || text.charAt(0) == '\r') {
                    //this is a new line
                    if (options.E) {
                        tb.append("$", NutsTextStyle.separator());
                    }
                    tb.append(text);
                    tracker.wasNewline = true;
                    tracker.line++;
                } else {
                    for (String s : ShellHelper.splitOn(text, '\t')) {
                        if (s.startsWith("\t")) {
                            tb.append("^I", NutsTextStyle.separator());
                        } else {
                            tb.append(s);
                        }
                    }
                    tracker.wasNewline = false;
                }
                return tb.build();
            }
            case STYLED: {
                NutsTextStyled tt = (NutsTextStyled) t;
                NutsTextPlain pt = (NutsTextPlain) tt.getChild();

                String text = pt.getText();
                NutsTextBuilder tb = NutsTexts.of(session).builder();
                if (options.n && tracker.wasNewline) {
                    tb.append(tracker.ruler.nextNum(tracker.line, session));
                }
                for (String s : ShellHelper.splitOn(text, '\t')) {
                    if (s.startsWith("\t")) {
                        tb.append("^I", NutsTextStyle.separator());
                    } else {
                        tb.append(s, tt.getStyles());
                    }
                }
                tracker.wasNewline = false;
                return tb.build();
            }
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported"));
    }

    public static class CatResult {
        Long number;
        NutsString line;
    }

    private static class Options {

        String highlighter = null;
        boolean n = false;
        boolean T = false;
        boolean E = false;
        List<FileInfo> files = new ArrayList<>();
        long currentNumber;
    }

    private class Tracker {
        ColumnRuler ruler = new ColumnRuler(6);
        long line;
        boolean wasNewline = true;
    }

}
