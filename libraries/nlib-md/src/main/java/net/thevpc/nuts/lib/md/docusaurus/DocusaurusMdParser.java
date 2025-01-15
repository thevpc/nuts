package net.thevpc.nuts.lib.md.docusaurus;

import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.lib.md.util.MdElementAndChildrenList;
import net.thevpc.nuts.util.NStringBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocusaurusMdParser implements MdParser {

    //    boolean wasNewline = true;
    private DocusaurusTextReader reader;

    public DocusaurusMdParser(Reader r) {
        this.reader = new DocusaurusTextReader(r);
    }

    public MdElement parse() {
        MdElement n;
        SectionPathHolder path = new SectionPathHolder();
        MdElementAndChildrenList list = new MdElementAndChildrenList();
        Object frontMatter = parseFrontMatter(path);
        list.setFrontMatter(frontMatter);
        while (reader.hasMore()) {
            n = readLine(new Cond().setConsumeNewline(NewLineAction.SPACE)
                            .setWasNewline(true)
                    , path);
            if (n != null) {
                list.add(n);
            }
        }
        return list.build();
    }

    private Object parseFrontMatter(SectionPathHolder path) {
        NStringBuilder frontMatter = new NStringBuilder();
        reader.readSpacesOrNewline();
        if (reader.hasMore()) {
            String line = reader.readSingleLineRegexp("---.*");
            if (line != null) {
                while (reader.hasMore()) {
                    String s = reader.readLine(true);
                    if (s.startsWith("---")) {
                        break;
                    }
                    frontMatter.println(s);
                }
            }
        }
        try (Reader is = new StringReader(frontMatter.toString())) {
            return new Yaml().load(is);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void close() {
        reader.close();
    }

    public MdElement peekImage(Cond cond) {
        return readOrPeekImage(cond, true);
    }

    public MdElement readImage(Cond cond) {
        return readOrPeekImage(cond, false);
    }

    public MdElement readOrPeekImage(Cond cond, boolean peek) {
        String regexp1 = "!\\[(?<caption>[^()\\[\\]]*)\\]\\((?<path>[^()\\[\\]]+)\\)";
        String r = peek ? reader.peekRegexp(regexp1) : reader.readRegexp(regexp1);
        if (r != null) {
            Matcher m = Pattern.compile(regexp1).matcher(r);
            m.find();
            String c = m.group("caption");
            String p = m.group("path");
            return new MdImage("![]()", MdImage.ImageFormat.PATH, c, p);
        }
        String regexp2 = "!\\[(?<caption>[^()\\[\\]]*)\\]\\[(?<path>[^()\\[\\]]+)\\]";
        r = peek ? reader.peekRegexp(regexp2) : reader.readRegexp(regexp2);
        if (r != null) {
            Matcher m = Pattern.compile(regexp2).matcher(r);
            m.find();
            String c = m.group("caption");
            String p = m.group("id");
            return new MdImage("![][]", MdImage.ImageFormat.ID, c, p);
        }
        return null;
    }

    public MdElement readBold(Cond cond) {
        String s = reader.readChars('*', 1, 2);
        switch (s.length()) {
            case 0:
                return null;
            case 1: {
                MdElement e = readLine(cond.copy().setExitOnStar1(true).setConsumeNewline(NewLineAction.STOP), new SectionPathHolder());
                if (e == null) {
                    e = MdText.empty();
                }
                if (reader.readChars('*', 1, 1).length() == 1) {
                    return new MdBold("*", e);
                }
                return new MdBold("*", e);
            }
            case 2: {
                MdElement e = readLine(cond.copy().setExitOnStar2(true).setConsumeNewline(NewLineAction.STOP), new SectionPathHolder());
                if (e == null) {
                    e = MdText.empty();
                }
                if (reader.readChars('*', 2, 2).length() == 2) {
                    return new MdBold("**", e);
                }
                return new MdBold("**", e);
            }
        }
        return null;
    }

    public MdElement readItalic(Cond cond) {
        String s = reader.readChars('_', 1, 2);
        switch (s.length()) {
            case 0:
                return null;
            case 1: {
                MdElement e = readLine(cond.copy().setExitOnUnderscore1(true).setConsumeNewline(NewLineAction.STOP), new SectionPathHolder());
                if (e == null) {
                    e = MdText.empty();
                }
                if (reader.readChars('_', 1, 1).length() == 1) {
                    return new MdItalic("_", e);
                }
                return new MdItalic("_", e);
            }
            case 2: {
                MdElement e = readLine(cond.copy().setExitOnUnderscore2(true).setConsumeNewline(NewLineAction.STOP), new SectionPathHolder());
                if (e == null) {
                    e = MdText.empty();
                }
                if (reader.readChars('_', 2, 2).length() == 2) {
                    return new MdBold("__", e);
                }
                return new MdBold("__", e);
            }
        }
        return null;
    }

    public MdElement readBacktics() {
        String s = reader.readChars('`', 3, 3);
        if (s.length() == 3) {
            String n = reader.readTagName();
            String c = reader.readStringOrEmpty(new TextReader.Globber() {
                int backtics = 0;

                @Override
                public TextReader.GlobberRet accept(StringBuilder curr, char next) {
                    if (backtics >= 3) {
                        return TextReader.GlobberRet.REJECT_LAST;
                    }
                    if (next == '`') {
                        backtics++;
                        if (backtics == 3) {
                            return TextReader.GlobberRet.ACCEPT;
                        }
                        return TextReader.GlobberRet.WAIT_FOR_MORE;
                    } else {
                        backtics = 0;
                        return TextReader.GlobberRet.WAIT_FOR_MORE;
                    }
                }
            });
            if (c != null && c.endsWith("```")) {
                c = c.substring(0, c.length() - 3);
            }
            if (c == null || c.isEmpty()) {
                c = n;
                n = "";
            }
            return MdFactory.codeBacktick3(n, c == null ? "" : c);
        }
        s = reader.readChars('`', 1, 2);
        if (s.length() == 1) {
            String c = reader.readStringOrEmpty(new TextReader.Globber() {
                boolean accepted = false;

                @Override
                public TextReader.GlobberRet accept(StringBuilder curr, char next) {
                    if (next == '`') {
                        return TextReader.GlobberRet.ACCEPT_END;
                    } else {
                        return TextReader.GlobberRet.WAIT_FOR_MORE;
                    }
                }
            });
            if (c != null && c.endsWith("`")) {
                c = c.substring(0, c.length() - 1);
            }
            return MdFactory.codeBacktick1("", c == null ? "" : c);
        } else if (s.length() == 2) {
            return MdFactory.codeBacktick1("", "");
        }
        return null;
    }

    public MdElement readAdmonition() {
        String s = reader.readChars(':', 3, 3);
        if (s.length() == 3) {
            String n = reader.readTagName();
            String c = reader.readStringOrEmpty(new TextReader.Globber() {
                int colons = 0;

                @Override
                public TextReader.GlobberRet accept(StringBuilder curr, char next) {
                    if (colons >= 3) {
                        return TextReader.GlobberRet.REJECT_LAST;
                    }
                    if (next == ':') {
                        colons++;
                        if (colons == 3) {
                            return TextReader.GlobberRet.ACCEPT;
                        }
                        return TextReader.GlobberRet.WAIT_FOR_MORE;
                    } else {
                        colons = 0;
                        return TextReader.GlobberRet.WAIT_FOR_MORE;
                    }
                }
            });
            if (c != null && c.endsWith(":::")) {
                c = c.substring(0, c.length() - 3);
            }
            if (c == null || c.isEmpty()) {
                c = n;
                n = "";
            }
            return new MdAdmonition(":::",
                    MdAdmonitionType.valueOf(n.toUpperCase()),
                    new DocusaurusMdParser(new StringReader(c == null ? "" : c)).parse()
            );
        }
        return null;
    }

    public MdText readText(Cond cond) {
        StringBuilder sb = new StringBuilder();
        int r;
        int last = -1;
        boolean doLoop = true;
        boolean inline = true;
        String addSep = "";
        while (doLoop && (r = reader.peekChar()) != -1) {
            char c = (char) r;
            switch (c) {
                case '\n':
                case '\r': {
                    switch (cond.getConsumeNewline()) {
                        case NOTHING:
                        case SPACE: {
                            if (reader.readRegexp("\\R\\R") != null) {
                                //multiple new lines! should exit in all cases!
                                //sb.append(" ");//nl
                                reader.readNewline();//read first newline
                                inline = false;
                                doLoop = false;
                                addSep = "";
                            } else if (reader.peekRegexp("\\R\\s*(---)") != null) {
                                inline = false;
                                doLoop = false;
                                addSep = "";
                            } else if (reader.peekRegexp("\\R\\s*(```)[a-z]*\\s*\\R") != null) {
                                inline = false;
                                doLoop = false;
                                addSep = "";
                            } else if (reader.peekRegexp("\\R\\s*`\\s*\\R") != null) {
                                inline = false;
                                doLoop = false;
                                addSep = "";
                            } else if (reader.peekRegexp("\\R\\s*[-+*#]\\s") != null) {
                                inline = false;
                                doLoop = false;
                                addSep = "";
                            } else if (reader.peekRegexp("\\R\\s*(([-]+)|([+]+)|([*]+)|([#]+)|([.]\\d))") != null) {
                                inline = false;
                                doLoop = false;
                                addSep = "";
                            } else {
                                reader.readNewline();
                                addSep = " ";
                            }
                            break;
                        }
                        case STOP: {
                            addSep = "";
                            doLoop = false;
                            break;
                        }
//                        case CONSUME:{
//                            String nl = reader.readNewline();
//                            if (reader.peekNewlineChar()) {
//                                //multiple new lines! should exit in all cases!
//                                nl = reader.readNewline();
//                                //sb.append(" ");//nl
//                                inline=true;
//                                doLoop = false;
//                            }else if(reader.peekRegexp("\\s*(---)")!=null){
//                                doLoop=false;
//                            }else if(reader.peekRegexp("\\s*[-+*#]\\s")!=null){
//                                doLoop=false;
//                            }else if(reader.peekRegexp("\\s*(([-]+)|([+]+)|([*]+)|([#]+)|([.]\\d))")!=null){
//                                doLoop=false;
//                            }else{
//                                sb.append(" ");//nl
//                            }
//                            break;
//                        }
                    }
//                    if (cond.isExitOnNewLine()) {
//                        if (cond.isConsumeNewline()) {
//                            String nl = reader.readNewline();
//                            sb.append(nl);
//                        }
//                        if (sb.length() == 0) {
//                            return null;
//                        }
//                        doLoop = false;
//                    } else {
//                        String nl = reader.readNewline();
//                        sb.append(nl);
//                        if (reader.peekNewlineChar()) {
//                            //multiple new lines! should exit in all cases!
//                            if (cond.isConsumeNewline()) {
//                                nl = reader.readNewline();
//                                sb.append(nl);
//                            }
//                            inline=false;
//                            doLoop = false;
//                        }
//                    }
                    break;
                }
                case '\\': {
                    reader.readChar();
                    int rr = reader.readChar();
                    if (rr < 0) {
                        sb.append(addSep);
                        sb.append(c);
                    } else {
                        sb.append(addSep);
                        sb.append((char) rr);
                    }
                    addSep = "";
                    break;
                }
                case '`': {
                    if (sb.length() == 0) {
                        return null;
                    }
                    doLoop = false;
                    addSep = "";
                    break;
                }
                case '*':
                case '<':
                case '_': {
                    if (cond.exit(c, reader)) {
                        addSep = "";
                        if (sb.length() == 0) {
                            return null;
                        }
                        doLoop = false;
                    } else {
                        if (last <= 0 || !isWord(last)) {
//                            String ss = reader.peekString(2);
//                            if (ss.length() < 2 || !isWord(ss.charAt(1))) {
//                                reader.readChar();
//                                sb.append(c);
//                            } else {
                            doLoop = false;
//                            }
                            addSep = "";
                        } else {
                            reader.readChar();
                            sb.append(addSep);
                            sb.append(c);
                            addSep = "";
                        }
                    }
                    break;
                }
                case ':': {
                    if (last <= 0 || !isWord(last)) {
                        if (reader.peekString(2).equals("::")) {
                            addSep = "";
                            if (sb.length() == 0) {
                                return null;
                            }
                            doLoop = false;
                        } else {
                            reader.readChar();
                            sb.append(addSep);
                            sb.append(c);
                            addSep = "";
                        }
                    } else {
                        reader.readChar();
                        sb.append(addSep);
                        sb.append(c);
                        addSep = "";
                    }
                    break;
                }
                case '|': {
                    if (cond.exitOnPipe) {
                        addSep = "";
                        if (sb.length() == 0) {
                            return null;
                        }
                        doLoop = false;
                    } else {
                        reader.readChar();
                        sb.append(addSep);
                        sb.append(c);
                        addSep = "";
                    }
                    break;
                }
                case '!': {
                    if (peekImage(cond) != null) {
                        addSep = "";
                        doLoop = false;
                    } else {
                        reader.readChar();
                        sb.append(addSep);
                        sb.append(c);
                        addSep = "";
                    }
                    break;
                }
                default: {
                    reader.readChar();
                    sb.append(addSep);
                    sb.append(c);
                    addSep = "";
                    break;
                }
            }
            last = r;
        }
        return new MdText(sb.toString(), inline);
    }

    public boolean isWord(int w) {
        return Character.isLetterOrDigit(w);
    }

    public MdElement readLine(Cond cond, SectionPathHolder path) {
        List<MdElement> all = new ArrayList<>();
        while (reader.hasMore()) {
            SectionPath pathBefore = path.path;
            MdElement z = readNext(cond, path);
            if (z != null) {
                if (!z.isInline()) {
//                    if (z.isText()) {
//                        all.add(MdText.phrase(z.asText().getText()));
//                    } else {
//                        all.add(z);
//                    }
                    all.add(z);
                    break;
                } else {
                    all.add(z);
                }
            } else if (reader.peekNewlineChar()) {
                NewLineAction a = cond.getConsumeNewline();
                if (/*a==NewLineAction.CONSUME || */a == NewLineAction.SPACE || a == NewLineAction.NOTHING) {
                    reader.readLine(true);
                }
                break;
            } else {
                break;
            }
        }
        if (all.size() == 0) {
            return null;
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return MdFactory.ofListOrEmpty(all.toArray(new MdElement[0]));
    }

    public MdElement readTitleSharp(SectionPathHolder path) {
        String s = reader.readChars('#', 1, 40);
        if (s.length() > 0) {
            int c = reader.peekChar();
            if (c < 0 || c == ' ') {
                MdElement ln = readLine(new Cond().setExitChar((c1, reader) -> c1 == '#')
                        .setConsumeNewline(NewLineAction.STOP), path);
                if (ln == null) {
                    ln = MdText.empty();
                }
                SectionPath newPath = path.path.resolveNext(MdElementTypeGroup.TITLE, s.length());
                path.path = newPath;
                return new MdTitle(s, ln, newPath.last().effDepth, new MdElement[0]);
            }
            return null;
        } else {
            return null;
        }
    }

    public MdElement readXml() {
        if (reader.peekChar() == '<') {
            MdXml t = reader.readXmlTag();
            if (t != null) {
                if (t.getTagType() == MdXml.XmlTagType.AUTO_CLOSE) {
                    return t;
                }
                if (t.getTagType() == MdXml.XmlTagType.CLOSE) {
                    //close without open?
                    return t;
                }
                List<MdElement> all = new ArrayList<>();
                while (reader.hasMore()) {
                    MdElement q = readNext(new Cond(), new SectionPathHolder());
                    if (q == null) {
                        if (reader.peekNewlineChar()) {
                            reader.readNewline();
                        }
                    } else if (q instanceof MdXml) {
                        MdXml x = (MdXml) q;
                        if (x.getTagType() == MdXml.XmlTagType.CLOSE && x.getTag().equals(t.getTag())) {
                            break;
                        }
                        all.add(q);
                    } else {
                        all.add(q);
                    }
                }
                return new MdXml(
                        MdXml.XmlTagType.OPEN, t.getTag(), t.getProperties(), MdFactory.seq(all.toArray(new MdElement[0]))
                );
            }
        }
        return null;
    }

    public MdElement readUnNumberedListItem(SectionPathHolder path) {
        int r = reader.peekChar();
        if (r < 0) {
            return null;
        }
        if (r == '+' || r == '-' || r == '*') {
            String s = reader.readChars((char) r, 1, 10);
            if (s.length() > 0) {
                int c = reader.readChar();
                if (c == ' ') {
                    MdElement ln = readLine(new Cond(), new SectionPathHolder());
                    if (ln == null) {
                        ln = MdText.empty();
                    }
                    ln = requireInline(ln);
                    SectionPath newPath = path.path.resolveNext(MdElementTypeGroup.UNNUMBERED_ITEM, s.length());
                    path.path = newPath;
                    return new MdUnNumberedItem(s, newPath.last().effDepth, ln, new MdElement[0]);
                }
                if (c >= 0) {
                    reader.unread(c);
                }
                reader.unread(s);
                return null;
            } else {
                reader.unread(s);
                return null;
            }
        } else {
            String p = reader.peekRegexp("[ ]+[*+-] ");
            if (p != null) {
                //remove last
                char cc = p.charAt(p.length() - 2);
                int depth = p.substring(0, p.length() - 2).length();
                reader.readString(p);
                MdElement ln = readLine(new Cond(), new SectionPathHolder());
                if (ln == null) {
                    ln = MdText.empty();
                }
                ln = requireInline(ln);
                SectionPath newPath = path.path.resolveNext(MdElementTypeGroup.UNNUMBERED_ITEM, depth + 1);
                path.path = newPath;
                return new MdUnNumberedItem(String.valueOf(cc), newPath.last().effDepth, ln, new MdElement[0]);
            }
        }
        return null;
    }

    public MdElement requireInline(MdElement e) {
        if (e.isInline()) {
            return e;
        }
        if (e.isPhrase()) {
            return e.asPhrase().toInline();
        }
        if (e.isText()) {
            return e.asText().toInline();
        }
        throw new IllegalArgumentException("expected inline able element. got: " + e.type());
    }

    public MdElement readNext(Cond cond, SectionPathHolder path) {
        int q = reader.peekChar();
        if (q < 0) {
            return null;
        }
//        String lookahead = reader.peekString(20);
        char c = (char) q;
        if (c == '\n' || c == '\r') {
            reader.readChar();
            cond.setWasNewline(true);
            return null;
        } else {
            boolean wasNewline0 = cond.isWasNewline();
            if (wasNewline0) {
                cond = cond.copy().setWasNewline(false);
            }
            if (wasNewline0) {
                if (!cond.exitOnStar1 && !cond.exitOnStar2 && reader.peekRegexp("[ ]+[*] ") != null) {
                    MdElement t = readUnNumberedListItem(path);
                    if (t != null) {
                        return t;
                    }
                } else if (/*!cond.exitOnStar1 && !cond.exitOnStar2 && */reader.peekRegexp("[ ]+[+] ") != null) {
                    MdElement t = readUnNumberedListItem(path);
                    if (t != null) {
                        return t;
                    }
                } else if (/*!cond.exitOnStar1 && !cond.exitOnStar2 && */reader.peekRegexp("[ ]+[-] ") != null) {
                    MdElement t = readUnNumberedListItem(path);
                    if (t != null) {
                        return t;
                    }
                } else if (/*!cond.exitOnStar1 && !cond.exitOnStar2 && */reader.peekRegexp("[ ]*[-]{3,}[ ]*(\\R|$)") != null) {
                    String rule = reader.readRegexp("[ ]*[-]{3,}[ ]*");
                    return new MdHr(rule.trim());
                }
            }
            switch (c) {
                case '#': {
                    if (cond.exit(c, reader)) {
                        return null;
                    }
                    if (wasNewline0) {
                        MdElement t = readTitleSharp(path);
                        if (t != null) {
                            return t;
                        }
                    }
//                    return readText(cond.copy().setConsumeNewline(NewLineAction.STOP));
                    reader.readChar();
                    return new MdText("" + c, true);
                }
                case '*': {
                    if (cond.exit(c, reader)) {
                        return null;
                    }
                    if (wasNewline0) {
                        MdElement t = readUnNumberedListItem(path);
                        if (t != null) {
                            return t;
                        }
                    } else {
                        if (cond.exit('*', reader)) {
                            return null;
                        }
                    }
                    MdElement b = readBold(cond);
                    if (b != null) {
                        return b;
                    }
//                    return readText(new Cond());
                    reader.readChar();
                    return new MdText("" + c, true);
                }
                case '-':
                case '+': {
                    if (cond.exit(c, reader)) {
                        return null;
                    }
                    if (wasNewline0) {
                        MdElement t = readUnNumberedListItem(path);
                        if (t != null) {
                            return t;
                        }
                    }
//                    return readText(cond.copy().setConsumeNewline(NewLineAction.STOP));
                    reader.readChar();
                    return new MdText("" + c, true);
                }
                case '!': {
                    if (cond.exit(c, reader)) {
                        return null;
                    }
                    MdElement t = readImage(cond);
                    if (t != null) {
                        return t;
                    }
                    reader.readChar();
                    return new MdText("!", true);
                }
                case '_': {
                    if (cond.exit(c, reader)) {
                        return null;
                    }
                    MdElement b = readItalic(cond);
                    if (b != null) {
                        return b;
                    }
                    reader.readChar();
                    return new MdText("_", true);
                }
                case ':': {
                    if (cond.exit(c, reader)) {
                        return null;
                    }
                    MdElement b = readAdmonition();
                    if (b != null) {
                        return b;
                    }
                    reader.readChar();
                    return new MdText(":", true);
                }
                case '`': {
                    if (cond.exit(c, reader)) {
                        return null;
                    }
                    MdElement t = readBacktics();
                    if (t != null) {
                        return t;
                    }
                    reader.readChar();
                    return new MdText("`", true);
                }
                case '<': {
                    if (cond.exit(c, reader)) {
                        return null;
                    }
                    MdElement t = readXml();
                    if (t != null) {
                        return t;
                    }
                    reader.readChar();
                    return new MdText("<", true);
                }
                case '|': {
                    if (cond.exit(c, reader)) {
                        return null;
                    }
                    if (wasNewline0) {
                        return readTable();
                    }
                    if (cond.exitOnPipe) {
                        return null;
                    }
                    //reader.readChar();//skip pipe!
                    reader.readChar();
                    return new MdText("|", true);
                }
                default: {
                    if (cond.exit(c, reader)) {
                        return null;
                    }
                    return readText(cond/*.copy().setExitOnNewLine(true)*/);
                }
            }
        }
    }

    private MdElement readTable() {
        MdRow headers = readRow();
        if (headers == null) {
            return null;
        }
        MdRow sorts = readRow();
        List<MdColumn> columns = new ArrayList<>();
        boolean isSortRow = sorts != null;
        List<MdHorizontalAlign> sortAligns = new ArrayList<>();
        if (sorts != null) {
            for (int i = 0; i < headers.size(); i++) {
                if (i < sorts.size()) {
                    MdElement ee = sorts.get(i);
                    String t = "";
                    if (ee.isText()) {
                        t = ee.asText().getText().trim();
                        MdHorizontalAlign ha;
                        if (t.matches(":[-]+:")) {
                            ha = MdHorizontalAlign.CENTER;
                        } else if (t.matches("[-]+:")) {
                            ha = MdHorizontalAlign.RIGHT;
                        } else if (t.isEmpty() || t.matches(":[-]+") || t.matches("[-]+")) {
                            ha = MdHorizontalAlign.LEFT;
                        } else {
                            isSortRow = false;
                            break;
                        }
                        sortAligns.add(ha);
                    } else {
                        isSortRow = false;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        List<MdRow> rows = new ArrayList<>();
        if (isSortRow) {
            for (int i = 0; i < headers.size(); i++) {
                MdHorizontalAlign ha = i < sortAligns.size() ? sortAligns.get(i) : MdHorizontalAlign.LEFT;
                columns.add(new MdColumn(headers.get(i), ha));
            }
        } else {
            for (int i = 0; i < headers.size(); i++) {
                columns.add(new MdColumn(headers.get(i), MdHorizontalAlign.LEFT));
            }
            if (sorts != null) {
                rows.add(sorts);
            }
        }
        while (true) {
            MdRow t = readRow();
            if (t == null) {
                break;
            }
            rows.add(t);
        }
        return new MdTable(columns.toArray(new MdColumn[0]), rows.toArray(new MdRow[0]));
    }

    private MdRow readRow() {
        StringBuilder sb = new StringBuilder();
        sb.append(reader.readSpaces());
        if (reader.peekChar('|')) {
            String line = reader.peekLine().trim();
            if (line.length() > 0 && line.charAt(0) == '|' && line.charAt(line.length() - 1) == '|') {
                List<MdElement> cells = new ArrayList<>();
                boolean lastEmptyCol = false;
                while (reader.hasMore() && !reader.peekNewlineChar()) {
                    lastEmptyCol = false;
                    reader.readSpaces();
                    if (reader.readChar('|')) {
                        MdElement cell = readLine(new Cond()
                                        .setExitOnPipe(true)
                                        .setConsumeNewline(NewLineAction.STOP)
                                , new SectionPathHolder());
                        if (cell == null) {
                            lastEmptyCol = true;
                            cell = MdText.empty();
                        }
                        cells.add(cell);
                    } else {
                        break;
                    }
                }
                if (lastEmptyCol) {
                    cells.remove(cells.size() - 1);
                }
                if (reader.peekNewlineChar()) {
                    reader.readNewline();
                }
                if (cells.size() > 1) {
                    boolean emptyLast = false;
                    MdElement last = cells.get(cells.size() - 1);
                    if (last.isText()) {
                        String t = last.asText().getText();
                        if (t.trim().isEmpty()) {
                            emptyLast = true;
                        }
                    }
                    if (emptyLast) {
                        cells.remove(cells.size() - 1);
                    }
                }
                return new MdRow(cells.toArray(new MdElement[0]), false);
            }
            reader.unread(sb.toString());
            return null;
        } else {
            reader.unread(sb.toString());
            return null;
        }
    }

    private enum NewLineAction {
        //        CONSUME,
        STOP,
        SPACE,
        NOTHING,
    }


    public interface DocExit {
        boolean exit(char c, TextReader reader);
    }

    public static class SectionPathHolder {
        SectionPath path = new SectionPath();
    }

    public static class SectionPath {
        List<SectionLevel> path = new ArrayList<>();

        public SectionPath() {

        }

        public SectionPath(SectionLevel path) {
            this.path.add(path);
        }

        public SectionPath(SectionPath parent, SectionLevel path) {
            this.path.addAll(parent.path);
            this.path.add(path);
        }

        public SectionPath(List<SectionLevel> path) {
            this.path.addAll(path);
        }

        public SectionPath resolveNext(MdElementTypeGroup type, int userDepth) {
            List<SectionLevel> path2 = new ArrayList<>(this.path);
            while (!path2.isEmpty()) {
                SectionLevel last = path2.get(path2.size() - 1);
                int c = last.compareTo(type, userDepth);
                if (c >= 0) {
                    path2.remove(path2.size() - 1);
                } else {
                    break;
                }
            }
            if (path2.isEmpty()) {
                return new SectionPath(new SectionLevel(type, userDepth, userDepth/*1*/));
            }
            path2.add(path2.get(path2.size() - 1).child(type, userDepth));
            return new SectionPath(path2);
        }

        public SectionLevel last() {
            if (path.isEmpty()) {
                return null;
            }
            return path.get(path.size() - 1);
        }
    }

    public static class SectionLevel implements Comparable<SectionLevel> {
        MdElementTypeGroup type;
        int userDepth;
        int effDepth;

        public SectionLevel(MdElementTypeGroup type, int userDepth, int effDepth) {
            this.type = type;
            this.userDepth = userDepth;
            this.effDepth = effDepth;
            switch (type) {
                case TITLE:
                case UNNUMBERED_ITEM:
                case NUMBERED_ITEM: {
                    break;
                }
                default: {
                    throw new IllegalArgumentException("unexpected section " + type);
                }
            }
        }

        public SectionLevel child(MdElementTypeGroup type, int userDepth) {
            if (this.type == MdElementTypeGroup.TITLE) {
                if (type == MdElementTypeGroup.TITLE) {
                    if (userDepth > this.userDepth) {
                        return new SectionLevel(type, userDepth, this.effDepth + 1);
                    } else {
                        throw new IllegalArgumentException("illegal depth");
                    }
                } else {
                    return new SectionLevel(type, userDepth, 1);
                }
            } else if (this.type == MdElementTypeGroup.UNNUMBERED_ITEM || this.type == MdElementTypeGroup.NUMBERED_ITEM) {
                if (type == MdElementTypeGroup.TITLE) {
                    throw new IllegalArgumentException("illegal title under item");
                } else {
                    return new SectionLevel(type, userDepth, this.effDepth + 1);
                }
            } else {
                throw new IllegalArgumentException("illegal type");
            }
        }

        @Override
        public int compareTo(SectionLevel o) {
            if (o == null) {
                return 1;
            }
            if (this.type == MdElementTypeGroup.TITLE) {
                if (o.type != MdElementTypeGroup.TITLE) {
                    return -1;
                } else {
                    return Integer.compare(this.userDepth, o.userDepth);
                }
            }
            if (o.type == MdElementTypeGroup.TITLE) {
                return 1;
            }
            return Integer.compare(this.userDepth, o.userDepth);
        }

        public int compareTo(MdElementTypeGroup type, int userDepth) {
            if (this.type == MdElementTypeGroup.TITLE) {
                if (type != MdElementTypeGroup.TITLE) {
                    return -1;
                } else {
                    return Integer.compare(this.userDepth, userDepth);
                }
            }
            if (type == MdElementTypeGroup.TITLE) {
                return 1;
            }
            return Integer.compare(this.userDepth, userDepth);
        }
    }

    private static class Cond implements Cloneable {
        DocExit exitChar;
        NewLineAction consumeNewline;
        boolean exitOnPipe;
        boolean wasNewline;
        //        boolean exitOnNewLine;
        boolean exitOnUnderscore1;
        boolean exitOnUnderscore2;
        boolean exitOnStar1;
        boolean exitOnStar2;

        public boolean isExitOnPipe() {
            return exitOnPipe;
        }

        public Cond setExitOnPipe(boolean exitOnPipe) {
            this.exitOnPipe = exitOnPipe;
            return this;
        }

        public boolean isExitOnStar1() {
            return exitOnStar1;
        }

        public Cond setExitOnStar1(boolean exitOnStar1) {
            this.exitOnStar1 = exitOnStar1;
            return this;
        }

        public boolean isExitOnStar2() {
            return exitOnStar2;
        }

        public Cond setExitOnStar2(boolean exitOnStar2) {
            this.exitOnStar2 = exitOnStar2;
            return this;
        }

        public boolean isExitOnUnderscore1() {
            return exitOnUnderscore1;
        }

        public Cond setExitOnUnderscore1(boolean exitOnUnderscore1) {
            this.exitOnUnderscore1 = exitOnUnderscore1;
            return this;
        }

        public boolean isExitOnUnderscore2() {
            return exitOnUnderscore2;
        }

        public Cond setExitOnUnderscore2(boolean exitOnUnderscore2) {
            this.exitOnUnderscore2 = exitOnUnderscore2;
            return this;
        }

        public NewLineAction getConsumeNewline() {
            return consumeNewline == null ? NewLineAction.NOTHING : consumeNewline;
        }

        public Cond setConsumeNewline(NewLineAction consumeNewline) {
            this.consumeNewline = consumeNewline;
            return this;
        }

        public boolean isWasNewline() {
            return wasNewline;
        }

        public Cond setWasNewline(boolean wasNewline) {
            this.wasNewline = wasNewline;
            return this;
        }

        public Cond setExitChar(DocExit exitChar) {
            this.exitChar = exitChar;
            return this;
        }

        boolean exitOnNewLine(TextReader reader) {
            return exit('\n', reader);
        }

        boolean exitOnStar(TextReader reader) {
            return exit('*', reader);
        }

        boolean exitOnUnderscore(TextReader reader) {
            return exit('_', reader);
        }

        boolean exitOnPipe(TextReader reader) {
            return exit('|', reader);
        }

//        public boolean isExitOnNewLine() {
//            return exitOnNewLine;
//        }
//
//        public Cond setExitOnNewLine(boolean exitOnNewLine) {
//            this.exitOnNewLine = exitOnNewLine;
//            return this;
//        }

        boolean exit(int peekedChar, TextReader reader) {
            switch (peekedChar) {
                case '\n':
                case '\r': {
                    if ((exitChar != null && exitChar.exit((char) peekedChar, reader))) {
                        return true;
                    }
                    switch (getConsumeNewline()) {
                        case NOTHING: {
                            break;
                        }
                        default: {
                            return true;
                        }
                    }
                    break;
                }
                case '*': {
                    if (exitOnStar2) {
                        String n = reader.peekString(2);
                        if (n.equals("**") || n.length() < 2) {
                            return true;
                        }
                    }
                    if (exitOnStar1) {
                        String n = reader.peekString(2);
                        if (n.length() < 2) {
                            return true;
                        }
                        if (n.charAt(1) == '*') {
                            return false;
                        }
                        return true;
                    }
                    if (exitChar != null && exitChar.exit((char) peekedChar, reader)) {
                        return true;
                    }
                    break;
                }
                case '_': {
                    if (exitOnUnderscore2) {
                        String n = reader.peekString(2);
                        if (n.equals("__") || n.length() < 2) {
                            return true;
                        }
                    }
                    if (exitOnUnderscore1) {
                        String n = reader.peekString(2);
                        if (n.length() < 2) {
                            return true;
                        }
                        if (n.charAt(1) == '_') {
                            return false;
                        }
                        return true;
                    }
                    if (exitChar != null && exitChar.exit((char) peekedChar, reader)) {
                        return true;
                    }
                    break;
                }
                case '|': {
                    if (exitOnPipe || (exitChar != null && exitChar.exit((char) peekedChar, reader))) {
                        return true;
                    }
                    break;
                }
                default: {
                    if (exitChar != null && exitChar.exit((char) peekedChar, reader)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Cond copy() {
            try {
                return (Cond) clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }


}
