package net.thevpc.nuts.lib.md.docusaurus;

import net.thevpc.nuts.lib.md.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class DocusaurusMdParser implements MdParser {

    //    boolean wasNewline = true;
    private DocusaurusTextReader reader;

    public DocusaurusMdParser(Reader r) {
        this.reader = new DocusaurusTextReader(r);
    }


//    private String nextLine() {
//        if (this.buffer.isEmpty()) {
//            try {
//                return this.reader.readLine();
//            } catch (IOException ex) {
//                throw new UncheckedIOException(ex);
//            }
//        }
//        return this.buffer.remove(this.buffer.size() - 1);
//    }

//    private void pushBack(String line) {
//        this.buffer.add(line);
//    }

//    private static String mul(char s, int count) {
//        char[] cc = new char[count];
//        Arrays.fill(cc, s);
//        return new String(cc);
//    }

//    private MdElement parseNextLine(Predicate<String> stopPredicate) {
//        String line = readLine();
//        if (line == null) {
//            return null;
//        }
//        if (stopPredicate != null && stopPredicate.test(line)) {
//            reader.unread(line);
//            return null;
//        }
//        if (line.startsWith("---") && line.trim().equals("---")) {
//            return (MdElement) new MdLineSeparator("---", line.substring(3));
//        }
//        if (line.startsWith(mul('\t', 5) + "-")) {
//            return (MdElement) MdFactory.ul(6, new DocusaurusInlineParser(line.substring(6).trim()).parse());
//        }
//        if (line.startsWith("\t-")) {
//            return (MdElement) MdFactory.ul(1, new DocusaurusInlineParser(line.substring(1).trim()).parse());
//        }
//        for (int i = 6; i > 0; i--) {
//            String id = mul('#', i);
//            if (line.startsWith(id)) {
//                return (MdElement) new MdTitle(id, line.substring(i).trim(), i);
//            }
//            id = mul('*', i);
//            if (line.startsWith(id + " ")) {
//                return (MdElement) MdFactory.ul(i, new DocusaurusInlineParser(line.substring(i).trim()).parse());
//                //return (MdElement) new MdTitle(id, line.substring(i).trim(), i);
//            }
//            id = mul('+', i);
//            if (line.startsWith(id + " ")) {
//                return (MdElement) MdFactory.ul(i, new DocusaurusInlineParser(line.substring(i).trim()).parse());
//            }
//            id = mul('-', i);
//            if (line.startsWith(id + " ")) {
//                return (MdElement) MdFactory.ul(i, new DocusaurusInlineParser(line.substring(i).trim()).parse());
//            }
//            if (i > 1) {
//                id = mul('\t', i - 1) + "-";
//                if (line.startsWith(id + " ")) {
//                    return (MdElement) MdFactory.ul(i, new DocusaurusInlineParser(line.substring(i).trim()).parse());
//                }
//            }
//        }
//        if (line.matches("[0-9]+[.] .*")) {
//            int x = line.indexOf('.');
//            return (MdElement) MdFactory.ol(Integer.parseInt(line.substring(0, x)), 1, new DocusaurusInlineParser(line.substring(x + 1)).parse());
//        }
//        if (line.startsWith("```")) {
//            String id = line.substring(3).trim();
//            if (!id.contains("```")) {
//                StringBuilder code = new StringBuilder();
//                boolean first = true;
//                while ((line = readLine()) != null) {
//                    if (first) {
//                        first = false;
//                    } else {
//                        code.append("\n");
//                    }
//                    if (line.startsWith("```")) {
//                        break;
//                    }
//                    code.append(line);
//                }
//                return (MdElement) new MdCode(id, code.toString(), false);
//            }
//        }
//        if (line.startsWith(":::")) {
//            String type = line.substring(3).trim();
//            MdElement t = parse(x -> x.startsWith(":::"));
//            String threeDots = readLine();
//            if (!threeDots.trim().equals(":::")) {
//                System.err.println("Expected :::");
//                pushBack(threeDots);
//            }
//            return (MdElement) new MdAdmonition(type, MdAdmonitionType.valueOf(type.toUpperCase()), t);
//        }
//        if (line.startsWith("|") && line.endsWith("|") && line.length() > 1) {
//            pushBack(line);
//            return readTable();
//        }
//        if (line.matches("[<][a-zA-Z-]+( .*)?")) {
//            pushBack(line);
//            return readXml();
//        }
//        return new DocusaurusInlineParser(line).parse();
//    }

    public MdElement parse() {
        List<MdElement> all = new ArrayList<>();
        MdElement n;
        while (reader.hasMore()) {
            n = readLine(new Cond().setConsumeNewline(true).setWasNewline(true));
            if (n != null) {
                all.add(n);
            }
        }
        if (all.isEmpty()) {
            return null;
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return new MdSequence("", all.toArray(new MdElement[0]), false);
    }

    @Override
    public void close() {
        reader.close();
    }

    public MdElement readBold(Cond cond) {
        String s = reader.readChars('*', 1, 2);
        switch (s.length()) {
            case 0:
                return null;
            case 1: {
                MdElement e = readLine(cond.copy().setExitOnStar1(true));
                if (e == null) {
                    e = new MdText("");
                }
                if (reader.readChars('*', 1, 1).length() == 1) {
                    return new MdBold("*", e);
                }
                return new MdBold("*", e);
            }
            case 2: {
                MdElement e = readLine(cond.copy().setExitOnStar2(true));
                if (e == null) {
                    e = new MdText("");
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
                MdElement e = readLine(cond.copy().setExitOnUnderscore1(true));
                if (e == null) {
                    e = new MdText("");
                }
                if (reader.readChars('_', 1, 1).length() == 1) {
                    return new MdItalic("_", e);
                }
                return new MdItalic("_", e);
            }
            case 2: {
                MdElement e = readLine(cond.copy().setExitOnUnderscore2(true));
                if (e == null) {
                    e = new MdText("");
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
                        return TextReader.GlobberRet.HIT_END;
                    } else {
                        backtics = 0;
                        return TextReader.GlobberRet.HIT_END;
                    }
                }
            });
            if (c!=null && c.endsWith("```")) {
                c = c.substring(0, c.length() - 3);
            }
            if (c==null || c.isEmpty()) {
                c = n;
                n = "";
            }
            boolean inline = c==null || c.indexOf('\n') < 0;
            return new MdCode(n, c==null?"":c, inline);
        }
        String c = reader.readStringOrEmpty((curr, next) -> {
            if (next == '`') {
                return TextReader.GlobberRet.ACCEPT;
            } else {
                return TextReader.GlobberRet.HIT_END;
            }
        });
        if (c!=null && c.endsWith("`")) {
            c = c.substring(0, c.length() - 3);
        }
        boolean inline = c==null || c.indexOf('\n') < 0;
        return new MdCode("", c==null?"":c, inline);
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
                        return TextReader.GlobberRet.HIT_END;
                    } else {
                        colons = 0;
                        return TextReader.GlobberRet.HIT_END;
                    }
                }
            });
            if (c!=null && c.endsWith(":::")) {
                c = c.substring(0, c.length() - 3);
            }
            if (c==null || c.isEmpty()) {
                c = n;
                n = "";
            }
            return new MdAdmonition(":::",
                    MdAdmonitionType.valueOf(n.toUpperCase()),
                    new DocusaurusMdParser(new StringReader(c==null?"":c)).parse()
            );
        }
        return null;
    }


    public MdText readText(Cond cond) {
        StringBuilder sb = new StringBuilder();
        int r;
        int last=-1;
        boolean doLoop = true;
        while (doLoop && (r = reader.peekChar()) != -1) {
            char c = (char) r;
            switch (c) {
                case '\n':
                case '\r': {
                    if (cond.exitOnNewLine(reader)) {
                        if (cond.isConsumeNewline()) {
                            String nl = reader.readNewline();
                            sb.append(nl);
                        }
                        if(sb.length()==0){
                            return null;
                        }
                        doLoop = false;
                    } else {
                        reader.readChar();
                        sb.append(c);
                    }
                    break;
                }
                case '\\': {
                    reader.readChar();
                    int rr=reader.readChar();
                    if(rr<0) {
                        sb.append(c);
                    }else {
                        sb.append((char)rr);
                    }
                    break;
                }
                case '`': {
                    if(sb.length()==0){
                        return null;
                    }
                    doLoop = false;
                    break;
                }
                case '*':
                case '<':
                case '_': {
                    if (cond.exit(c,reader)) {
                        if(sb.length()==0){
                            return null;
                        }
                        doLoop = false;
                    }else {
                        if (last <= 0 || !isWord(last)) {
//                            String ss = reader.peekString(2);
//                            if (ss.length() < 2 || !isWord(ss.charAt(1))) {
//                                reader.readChar();
//                                sb.append(c);
//                            } else {
                                doLoop = false;
//                            }
                        } else {
                            reader.readChar();
                            sb.append(c);
                        }
                    }
                    break;
                }
                case ':': {
                    if(last<=0 || !isWord(last)) {
                        if (reader.peekString(2).equals("::")) {
                            if(sb.length()==0){
                                return null;
                            }
                            doLoop = false;
                        } else {
                            reader.readChar();
                            sb.append(c);
                        }
                    }else{
                        reader.readChar();
                        sb.append(c);
                    }
                    break;
                }
                case '|': {
                    if (cond.exitOnPipe) {
                        if(sb.length()==0){
                            return null;
                        }
                        doLoop = false;
                    } else {
                        reader.readChar();
                        sb.append(c);
                    }
                    break;
                }
                default: {
                    reader.readChar();
                    sb.append(c);
                    break;
                }
            }
            last=r;
        }
        return new MdText(sb.toString());
    }

    public boolean isWord(int w) {
        return Character.isLetterOrDigit(w);
    }

    public MdElement readLine(Cond cond) {
        List<MdElement> all = new ArrayList<>();
        while (reader.hasMore()) {
            MdElement z = readNext(cond.copy().setConsumeNewline(false));
            if (z != null) {
                all.add(z);
            } else if (reader.peekNewlineChar()) {
                if (cond.isConsumeNewline()) {
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
        return new MdSequence("", all.toArray(new MdElement[0]), true);
    }

    public MdElement readTitleSharp() {
        String s = reader.readChars('#', 1, 10);
        if (s.length() > 0) {
            int c = reader.peekChar();
            if (c < 0 || c == ' ') {
                MdElement ln = readLine(new Cond().setExitChar((c1, reader) -> c1 == '#').setExitOnNewLine(true));
                if (ln == null) {
                    ln = new MdText("");
                }
                return new MdTitle(s, ln, s.length());
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
                    MdElement q = readNext(new Cond());
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

    public MdElement nextUnNumberedListItem() {
        int r = reader.peekChar();
        if (r < 0) {
            return null;
        }
        if (r == '+' || r == '-' || r == '*') {
            String s = reader.readChars((char) r, 1, 10);
            if (s.length() > 0) {
                int c = reader.readChar();
                if (c == ' ') {
                    MdElement ln = readLine(new Cond());
                    if (ln == null) {
                        ln = new MdText("");
                    }
                    return new MdUnNumberedItem(s,s.length(),ln,new MdElement[0]);
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
        }else {
            String p=reader.peekRegexp("[ ]+[*+-] ");
            if(p!=null){
                //remove last
                char cc=p.charAt(p.length()-2);
                int depth=p.substring(0,p.length()-2).length();
                reader.readString(p);
                MdElement ln = readLine(new Cond());
                if (ln == null) {
                    ln = new MdText("");
                }
                return new MdUnNumberedItem(String.valueOf(cc),depth+1,ln,new MdElement[0]);
            }
        }
        return null;
    }

    public MdElement readNext(Cond cond) {
        int q = reader.peekChar();
        if (q < 0) {
            return null;
        }
        char c = (char) q;
        if (c == '\n' || c == '\r') {
            return null;
        } else {
            boolean wasNewline0 = cond.isWasNewline();
            if (wasNewline0) {
                cond = cond.copy().setWasNewline(false);
            }
            if(wasNewline0){
                if(!cond.exitOnStar1 && !cond.exitOnStar2 && reader.peekRegexp("[ ]+[*] ")!=null){
                    MdElement t = nextUnNumberedListItem();
                    if (t != null) {
                        return t;
                    }
                }else if(/*!cond.exitOnStar1 && !cond.exitOnStar2 && */reader.peekRegexp("[ ]+[+] ")!=null){
                    MdElement t = nextUnNumberedListItem();
                    if (t != null) {
                        return t;
                    }
                }else if(/*!cond.exitOnStar1 && !cond.exitOnStar2 && */reader.peekRegexp("[ ]+[-] ")!=null){
                    MdElement t = nextUnNumberedListItem();
                    if (t != null) {
                        return t;
                    }
                }
            }
            switch (c) {
                case '#': {
                    if(cond.exit(c,reader)){
                        return null;
                    }
                    if (wasNewline0) {
                        MdElement t = readTitleSharp();
                        if (t != null) {
                            return t;
                        }
                    }
                    return readText(cond.copy().setExitOnNewLine(true));
                }
                case '*': {
                    if(cond.exit(c,reader)){
                        return null;
                    }
                    if (wasNewline0) {
                        MdElement t = nextUnNumberedListItem();
                        if (t != null) {
                            return t;
                        }
                    }else{
                        if (cond.exit('*', reader)) {
                            return null;
                        }
                    }
                    MdElement b = readBold(cond);
                    if (b != null) {
                        return b;
                    }
                    return readText(new Cond());
                }
                case '-':
                case '+': {
                    if(cond.exit(c,reader)){
                        return null;
                    }
                    if (wasNewline0) {
                        MdElement t = nextUnNumberedListItem();
                        if (t != null) {
                            return t;
                        }
                    }
                    return readText(cond.copy().setExitOnNewLine(true));
                }
                case '_': {
                    if(cond.exit(c,reader)){
                        return null;
                    }
                    MdElement b = readItalic(cond);
                    if (b != null) {
                        return b;
                    }
                    return readText(cond.copy().setExitOnNewLine(true));
                }
                case ':': {
                    if(cond.exit(c,reader)){
                        return null;
                    }
                    MdElement b = readAdmonition();
                    if (b != null) {
                        return b;
                    }
                    return readText(cond.copy().setExitOnNewLine(true));
                }
                case '`': {
                    if(cond.exit(c,reader)){
                        return null;
                    }
                    MdElement t = readBacktics();
                    if (t != null) {
                        return t;
                    }
                    return readText(cond.copy().setExitOnNewLine(true));
                }
                case '<': {
                    if(cond.exit(c,reader)){
                        return null;
                    }
                    MdElement t = readXml();
                    if (t != null) {
                        return t;
                    }
                    return readText(cond.copy().setExitOnNewLine(true));
                }
                case '|': {
                    if(cond.exit(c,reader)){
                        return null;
                    }
                    if (wasNewline0) {
                        return readTable();
                    }
                    if (cond.exitOnPipe) {
                        return null;
                    }
                    //reader.readChar();//skip pipe!
                    return readText(cond.copy().setExitOnNewLine(true));
                }
                default: {
                    if(cond.exit(c,reader)){
                        return null;
                    }
                    return readText(cond.copy().setExitOnNewLine(true));
                }
            }
        }
    }


    private MdElement readTable() {
        MdRow headers = readRow();
        if(headers==null){
            return null;
        }
        MdRow sorts = readRow();
        List<MdColumn> columns = new ArrayList<>();
        boolean isSortRow = sorts!=null;
        List<MdHorizontalAlign> sortAligns = new ArrayList<>();
        if(sorts!=null) {
            for (int i = 0; i < headers.size(); i++) {
                if (i < sorts.size()) {
                    MdElement ee = sorts.get(i);
                    String t = "";
                    if (ee.isText()) {
                        t = ee.asText().getText().trim();
                        MdHorizontalAlign ha;
                        if(t.matches(":[-]+:")) {
                            ha = MdHorizontalAlign.CENTER;
                        }else if(t.matches("[-]+:")){
                            ha=MdHorizontalAlign.RIGHT;
                        }else if(t.matches(":[-]+")){
                            ha=MdHorizontalAlign.LEFT;
                        }else{
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
            rows.add(sorts);
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
                while (reader.hasMore() && !reader.peekNewlineChar()) {
                    reader.readSpaces();
                    if (reader.readChar('|')) {
                        MdElement cell = readLine(new Cond().setExitOnPipe(true));
                        if (cell == null) {
                            cell = new MdText("");
                        }
                        cells.add(cell);
                    } else {
                        break;
                    }
                }
                if(reader.peekNewlineChar()){
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

    public interface DocExit {
        boolean exit(char c, TextReader reader);
    }

    private static class Cond implements Cloneable {
        DocExit exitChar;
        boolean consumeNewline;
        boolean exitOnPipe;
        boolean wasNewline;
        boolean exitOnNewLine;
        boolean exitOnUnderscore1;
        boolean exitOnUnderscore2;
        boolean exitOnStar1;
        boolean exitOnStar2;

        public boolean isExitOnUnderscore2() {
            return exitOnUnderscore2;
        }

        public Cond setExitOnUnderscore2(boolean exitOnUnderscore2) {
            this.exitOnUnderscore2 = exitOnUnderscore2;
            return this;
        }

        public boolean isConsumeNewline() {
            return consumeNewline;
        }

        public Cond setConsumeNewline(boolean consumeNewline) {
            this.consumeNewline = consumeNewline;
            return this;
        }

        public Cond setExitOnStar2(boolean exitOnStar2) {
            this.exitOnStar2 = exitOnStar2;
            return this;
        }

        public boolean isWasNewline() {
            return wasNewline;
        }

        public Cond setWasNewline(boolean wasNewline) {
            this.wasNewline = wasNewline;
            return this;
        }

        public Cond setExitOnPipe(boolean exitOnPipe) {
            this.exitOnPipe = exitOnPipe;
            return this;
        }

        public Cond setExitChar(DocExit exitChar) {
            this.exitChar = exitChar;
            return this;
        }

        public Cond setExitOnNewLine(boolean exitOnNewLine) {
            this.exitOnNewLine = exitOnNewLine;
            return this;
        }

        public Cond setExitOnUnderscore1(boolean exitOnUnderscore1) {
            this.exitOnUnderscore1 = exitOnUnderscore1;
            return this;
        }

        public Cond setExitOnStar1(boolean exitOnStar1) {
            this.exitOnStar1 = exitOnStar1;
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

        boolean exit(int peekedChar, TextReader reader) {
            switch (peekedChar) {
                case '\n':
                case '\r': {
                    if (exitOnNewLine || (exitChar != null && exitChar.exit((char) peekedChar, reader))) {
                        return true;
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
