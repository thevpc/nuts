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
package net.thevpc.nuts.toolbox.nsh.cmds.posix;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.bundles._IOUtils;
import net.thevpc.nuts.toolbox.nsh.util.bundles._StringUtils;
import net.thevpc.nuts.toolbox.nsh.util.ColumnRuler;
import net.thevpc.nuts.toolbox.nsh.util.FileInfo;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public class CatCommand extends NShellBuiltinDefault {

    public CatCommand() {
        super("cat", DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        NSession session = context.getSession();
        Options options = context.getOptions();
        NArg a;

        if (cmdLine.next("-").orNull()!= null) {
            options.files.add(null);
            return true;
        } else if ((a = cmdLine.nextFlag("-n", "--number").orNull()) != null) {
            options.n = a.getBooleanValue().get(session);
            return true;
        } else if ((a = cmdLine.nextFlag("-t", "--show-tabs").orNull()) != null) {
            options.T = a.getBooleanValue().get(session);
            return true;
        } else if ((a = cmdLine.nextFlag("-E", "--show-ends").orNull()) != null) {
            options.E = a.getBooleanValue().get(session);
            return true;
        } else if ((a = cmdLine.next("-H", "--highlight", "--highlighter").orNull()) != null) {
            options.highlighter = NStringUtils.trim(a.getStringValue().orNull());
            return true;
        }
        return false;
    }

    @Override
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        NSession session = context.getSession();
        Options options = context.getOptions();
        String path = cmdLine.next().flatMap(NLiteral::asString).get(session);
        options.files.add(new FileInfo(NPath.of(path, session), options.highlighter));
        return true;
    }

    @Override
    protected void onCmdExec(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        if (options.files.isEmpty()) {
            options.files.add(null);
        }
        NPrintStream out = context.getSession().out();
        try {
            options.currentNumber = 1;

            OutputStream os = null;
            boolean plain = true;
            if (context.getSession().getOutputFormat() == NContentType.PLAIN) {
                os = out.asOutputStream();
            } else {
                plain = false;
            }
            List<CatResult> results = new ArrayList<>();
            //text mode
            for (FileInfo f : options.files) {
                boolean close = false;
                InputStream in = null;
                if (f.getPath() == null) {
                    in = context.in();
                    if (f.getHighlighter() == null) {
                        f.setHighlighter("plain");
                    } else if (f.getHighlighter().isEmpty()) {
                        f.setHighlighter("ntf");
                    }
                } else {
                    in = f.getPath().getInputStream();
                    if (f.getHighlighter() == null) {
                        f.setHighlighter("plain");
                    } else if (f.getHighlighter().isEmpty()) {
                        f.setHighlighter(f.getPath().getContentType());
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
                out.print(results);
            }
        } catch (IOException ex) {
            throw new NExecutionException(context.getSession(), NMsg.ofC("%s", ex), ex, NExecutionException.ERROR_3);
        }
    }

    private void catText2(InputStream in, Options options, NShellExecutionContext context, FileInfo info, List<CatResult> results) throws IOException {
        boolean whole = true;
        NSession session = context.getSession();
        if (whole && info.getHighlighter() != null) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            NCp.of(session).from(in).to(bout).run();
            String text = bout.toString();
            NTextBuilder nutsText = NTexts.of(session).ofCode(info.getHighlighter(), text).highlight(session)
                    .builder()
                    .flatten();
            List<NText> children = nutsText.getChildren();
            Tracker tracker = new Tracker();
            boolean n = options.n;
            options.n = false;
            while (true) {
                NText line = nextLine(children, session, tracker, options, true);
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
                NTextCode c = NTexts.of(session).ofCode(info.getHighlighter(), line);
                line = c.highlight(session).toString();
                if (options.E) {
                    line += "$";
                }
                r.line = NTexts.of(session).ofPlain(line);
                options.currentNumber++;
            }
        }
    }

    private void catText(InputStream in, OutputStream os, Options options, NShellExecutionContext context, FileInfo info) throws IOException {
        if (info.getHighlighter() == null/* || "plain".equalsIgnoreCase(info.getHighlighter()) || "text".equalsIgnoreCase(info.getHighlighter())*/) {
            if (!options.n && !options.T && !options.E) {
                _IOUtils.copy(in, os, 4096 * 2);
                return;
            }
        }
        boolean whole = true;
        NSession session = context.getSession();
        if (whole && info.getHighlighter() != null) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            NCp.of(session).from(in).to(bout).run();
            String text = bout.toString();
            NTextBuilder nutsText = NTexts.of(session).ofCode(info.getHighlighter(), text).highlight(session)
                    .builder()
                    .flatten();
            NPrintStream out = NPrintStream.of(os, session);
            List<NText> children = nutsText.getChildren();
            Tracker tracker = new Tracker();
            while (true) {
                NText line = nextLine(children, session, tracker, options, false);
                if (line != null) {
                    out.print(line);
                } else {
                    break;
                }
            }
        } else {
            NPrintStream out = NPrintStream.of(os, session);
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

                    NTextCode c = NTexts.of(session).ofCode(info.getHighlighter(), line);
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

    private NText nextLine(List<NText> t, NSession session, Tracker tracker, Options options, boolean skipNewline) {
        NTextBuilder b = NTexts.of(session).ofBuilder();
        while (!t.isEmpty()) {
            NText ii = t.remove(0);
            NText n = nextNode(ii, session, tracker, options);
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

    private NText nextNode(NText t, NSession session, Tracker tracker, Options options) {
        switch (t.getType()) {
            case PLAIN: {
                String text = ((NTextPlain) t).getText();
                NTextBuilder tb = NTexts.of(session).ofBuilder();
                if (options.n && tracker.wasNewline) {
                    tb.append(tracker.ruler.nextNum(tracker.line, session));
                }
                if (text.charAt(0) == '\n' || text.charAt(0) == '\r') {
                    //this is a new line
                    if (options.E) {
                        tb.append("$", NTextStyle.separator());
                    }
                    tb.append(text);
                    tracker.wasNewline = true;
                    tracker.line++;
                } else {
                    for (String s : ShellHelper.splitOn(text, '\t')) {
                        if (s.startsWith("\t")) {
                            tb.append("^I", NTextStyle.separator());
                        } else {
                            tb.append(s);
                        }
                    }
                    tracker.wasNewline = false;
                }
                return tb.build();
            }
            case STYLED: {
                NTextStyled tt = (NTextStyled) t;
                NTextBuilder tb = NTexts.of(session).ofBuilder();
                if (options.n && tracker.wasNewline) {
                    tb.append(tracker.ruler.nextNum(tracker.line, session));
                }
                if(tt.getChild() instanceof NTextPlain) {
                    NTextPlain pt = (NTextPlain) tt.getChild();
                    String text = pt.getText();
                    for (String s : ShellHelper.splitOn(text, '\t')) {
                        if (s.startsWith("\t")) {
                            tb.append("^I", NTextStyle.separator());
                        } else {
                            tb.append(s, tt.getStyles());
                        }
                    }
                    tracker.wasNewline = false;
                    return tb.build();
                }else if(tt.getChild() instanceof NTextStyled){
                    NTextStyled pt = (NTextStyled) tt.getChild();
                    tb.append(nextNode(pt,session, tracker, options), tt.getStyles());
                    return tb.build();
                }else{
                    throw new NUnsupportedOperationException(session);
                }
            }
        }
        throw new NUnsupportedOperationException(session);
    }

    public static class CatResult {
        Long number;
        NString line;
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
