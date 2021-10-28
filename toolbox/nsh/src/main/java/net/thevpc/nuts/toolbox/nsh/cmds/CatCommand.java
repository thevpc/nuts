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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.spi.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles._IOUtils;
import net.thevpc.nuts.toolbox.nsh.bundles._StringUtils;
import net.thevpc.nuts.toolbox.nsh.util.FileInfo;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class CatCommand extends SimpleNshBuiltin {

    public CatCommand() {
        super("cat", DEFAULT_SUPPORT);
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a;

        if (commandLine.next("-") != null) {
            options.files.add(null);
            return true;
        } else if ((a = commandLine.next("-n", "--number")) != null) {
            options.n = a.getValue().getBoolean();
            return true;
        } else if ((a = commandLine.next("-t", "--show-tabs")) != null) {
            options.T = a.getValue().getBoolean();
            return true;
        } else if ((a = commandLine.next("-E", "--show-ends")) != null) {
            options.E = a.getValue().getBoolean();
            return true;
        } else if ((a = commandLine.next("-H", "--highlighter")) != null) {
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
    protected void execBuiltin(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.files.isEmpty()) {
            options.files.add(null);
        }
        NutsPrintStream out = context.getSession().out();
        try {
            options.currentNumber = 1;
            //text mode
            for (FileInfo f : options.files) {
                boolean close = false;
                InputStream in = null;
                if (f.getFile() == null) {
                    in = context.in();
                    if (f.getHighlighter() == null) {
                        f.setHighlighter("plain");
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
                    catText(in, out.asOutputStream(), options, context, f);
                } finally {
                    if (close) {
                        in.close();
                    }
                }
            }
        } catch (IOException ex) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("%s", ex), ex, 100);
        }
    }

    private void catText(InputStream in, OutputStream os, Options options, SimpleNshCommandContext context, FileInfo info) throws IOException {
        if (info.getHighlighter() == null || "plain".equalsIgnoreCase(info.getHighlighter()) || "text".equalsIgnoreCase(info.getHighlighter())) {
            if (!options.n && !options.T && !options.E) {
                _IOUtils.copy(in, os, 4096 * 2);
                return;
            }
        }
        boolean whole = true;
        if (whole && info.getHighlighter() != null) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            context.getSession().io().copy().from(in).to(bout).run();
            String text = bout.toString();
            NutsTextBuilder nutsText = context.getSession().text().ofCode(options.highlighter, text).highlight(context.getSession())
                    .builder()
                    .flatten();
            NutsPrintStream out = context.getSession().io().createPrintStream(os);
            writeNode(nutsText.build(), context.getSession(), new Tracker(), out, options);
        } else {
            NutsPrintStream out = context.getSession().io().createPrintStream(os);
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

                    NutsTextCode c = context.getSession().text().ofCode(options.highlighter, line);
                    line = c.highlight(context.getSession()).toString();

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


    private void writeNode(NutsText t, NutsSession session, Tracker tracker, NutsPrintStream out, Options options) {
        switch (t.getType()) {
            case PLAIN: {
                String text = ((NutsTextPlain) t).getText();
                NutsTextBuilder tb = session.text().builder();
                if (options.n && tracker.wasNewline) {
                    String ruleText = String.valueOf(tracker.line);
                    if (tracker.ruleWidth <= ruleText.length()) {
                        tracker.ruleWidth = ruleText.length() + 1;
                    }
                    tb.append(_StringUtils.formatRight(ruleText, tracker.ruleWidth), NutsTextStyle.number());
                    tb.append("  ");
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
                out.printf(tb.build());
                break;
            }
            case LIST: {
                NutsTextList tt = (NutsTextList) t;
                for (NutsText n : tt) {
                    writeNode(n, session, tracker, out, options);
                }
                break;
            }
            case STYLED: {
                NutsTextStyled tt = (NutsTextStyled) t;
                NutsTextPlain pt = (NutsTextPlain) tt.getChild();

                String text = pt.getText();
                NutsTextBuilder tb = session.text().builder();
                if (options.n && tracker.wasNewline) {
                    String ruleText = String.valueOf(tracker.line);
                    if (tracker.ruleWidth <= ruleText.length()) {
                        tracker.ruleWidth = ruleText.length() + 1;
                    }
                    tb.append(_StringUtils.formatRight(ruleText, tracker.ruleWidth), NutsTextStyle.number());
                    tb.append("  ");
                }
                for (String s : ShellHelper.splitOn(text, '\t')) {
                    if (s.startsWith("\t")) {
                        tb.append("^I", NutsTextStyle.separator());
                    } else {
                        tb.append(s, tt.getStyles());
                    }
                }
                tracker.wasNewline = false;
                out.printf(tb.build());
                break;
            }
        }
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
        int ruleWidth = 6;
        long line;
        boolean wasNewline = true;
    }

}
