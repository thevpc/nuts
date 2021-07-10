package net.thevpc.nuts.lib.md.docusaurus;

import net.thevpc.nuts.lib.md.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class DocusaurusMdParserOld implements MdParser {

    private List<String> buffer = new ArrayList<>();
    private BufferedReader reader;

    public DocusaurusMdParserOld(Reader r) {
        this.reader = (r instanceof BufferedReader) ? (BufferedReader) r : new BufferedReader(r);
    }

    private static String mul(char s, int count) {
        char[] cc = new char[count];
        Arrays.fill(cc, s);
        return new String(cc);
    }

    private String nextLine() {
        if (this.buffer.isEmpty()) {
            try {
                return this.reader.readLine();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return this.buffer.remove(this.buffer.size() - 1);
    }

    private void pushBack(String line) {
        this.buffer.add(line);
    }

    private MdElement parseNextLine(Predicate<String> stopPredicate) {
        String line = nextLine();
        if (line == null) {
            return null;
        }
        if (stopPredicate != null && stopPredicate.test(line)) {
            pushBack(line);
            return null;
        }
        if (line.startsWith("---") && line.trim().equals("---")) {
            return (MdElement) new MdLineSeparator("---", line.substring(3));
        }
        if (line.startsWith(mul('\t', 5) + "-")) {
            return (MdElement) MdFactory.ul(6, new DocusaurusInlineParser(line.substring(6).trim()).parse());
        }
        if (line.startsWith("\t-")) {
            return (MdElement) MdFactory.ul(1, new DocusaurusInlineParser(line.substring(1).trim()).parse());
        }
        for (int i = 6; i > 0; i--) {
            String id = mul('#', i);
            if (line.startsWith(id)) {
                return (MdElement) new MdTitle(id, new MdText(line.substring(i).trim()), i);
            }
            id = mul('*', i);
            if (line.startsWith(id + " ")) {
                return (MdElement) MdFactory.ul(i, new DocusaurusInlineParser(line.substring(i).trim()).parse());
                //return (MdElement) new MdTitle(id, line.substring(i).trim(), i);
            }
            id = mul('+', i);
            if (line.startsWith(id + " ")) {
                return (MdElement) MdFactory.ul(i, new DocusaurusInlineParser(line.substring(i).trim()).parse());
            }
            id = mul('-', i);
            if (line.startsWith(id + " ")) {
                return (MdElement) MdFactory.ul(i, new DocusaurusInlineParser(line.substring(i).trim()).parse());
            }
            if (i > 1) {
                id = mul('\t', i - 1) + "-";
                if (line.startsWith(id + " ")) {
                    return (MdElement) MdFactory.ul(i, new DocusaurusInlineParser(line.substring(i).trim()).parse());
                }
            }
        }
        if (line.matches("[0-9]+[.] .*")) {
            int x = line.indexOf('.');
            return (MdElement) MdFactory.ol(Integer.parseInt(line.substring(0, x)), 1, new DocusaurusInlineParser(line.substring(x + 1)).parse());
        }
        if (line.startsWith("```")) {
            String id = line.substring(3).trim();
            if (!id.contains("```")) {
                StringBuilder code = new StringBuilder();
                boolean first = true;
                while ((line = nextLine()) != null) {
                    if (first) {
                        first = false;
                    } else {
                        code.append("\n");
                    }
                    if (line.startsWith("```")) {
                        break;
                    }
                    code.append(line);
                }
                return (MdElement) new MdCode(id, code.toString(), false);
            }
        }
        if (line.startsWith(":::")) {
            String type = line.substring(3).trim();
            MdElement t = parse(x -> x.startsWith(":::"));
            String threeDots = nextLine();
            if (!threeDots.trim().equals(":::")) {
                System.err.println("Expected :::");
                pushBack(threeDots);
            }
            return (MdElement) new MdAdmonition(type, MdAdmonitionType.valueOf(type.toUpperCase()), t);
        }
        if (line.startsWith("|") && line.endsWith("|") && line.length() > 1) {
            pushBack(line);
            return readTable();
        }
        if (line.matches("[<][a-zA-Z-]+( .*)?")) {
            pushBack(line);
            return readXml();
        }
        return new DocusaurusInlineParser(line).parse();
    }

    public MdElement parse() {
        return parse(null);
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private MdElement parse(Predicate<String> stopPredicate) {
        List<MdElement> all = new ArrayList<>();
        while (true) {
            MdElement pl = parseNextLine(stopPredicate);
            if (pl == null) {
                break;
            }
            all.add(pl);
        }
        if (all.isEmpty()) {
            return null;
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        return (MdElement) new MdSequence("", all.<MdElement>toArray(new MdElement[0]), false);
    }

    private MdElement readXml() {
        String line = nextLine();
        if (line.matches("[<][a-zA-Z-]+( .*)?")) {
            int s = line.indexOf(' ');
            if (s < 0) {
                s = line.indexOf('\t');
            }
            String tagName = (s < 0) ? line.substring(1) : line.substring(1, s);
            StringBuilder startTag = new StringBuilder();
            startTag.append(line);
            if (!line.endsWith(">")) {
                do {
                    line = nextLine();
                    if (line == null) {
                        System.err.println("invalid xml tag");
                        break;
                    }
                    startTag.append("\n");
                    startTag.append(line);
                } while (!line.endsWith(">"));
            }
            MdElement t = parse(x -> x.startsWith("</" + tagName + ">"));
            nextLine();
            return (MdElement) new MdXml(MdXml.XmlTagType.OPEN, tagName, startTag.substring(1 + tagName.length() + 1, startTag.length() - 1), t);
        }
        return null;
    }

    private MdElement readTable() {
        MdRow headers = readRow();
        MdRow sorts = readRow();
        List<MdColumn> columns = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            String t = sorts.get(i).asText().getText().trim();
            MdHorizontalAlign ha
                    = t.matches(":[-]+:") ? MdHorizontalAlign.CENTER
                    : t.matches("[-]+:") ? MdHorizontalAlign.RIGHT
                    : MdHorizontalAlign.LEFT;
            columns.add(new MdColumn(headers.get(i), ha));
        }
        List<MdRow> rows = new ArrayList<>();
        while (true) {
            MdRow t = readRow();
            if (t == null) {
                break;
            }
            rows.add(t);
        }
        return (MdElement) new MdTable(columns.toArray(new MdColumn[0]), rows.toArray(new MdRow[0]));
    }

    private MdRow readRow() {
        String line = nextLine();
        if (line != null) {
            line = line.trim();
            if (line.isEmpty()) {
                return null;
            }
            MdElement e = new DocusaurusInlineParser(line).parse();
            if (e == null) {
                return null;
            }
            MdSequence seq = (e instanceof MdSequence) ? ((MdSequence) e) : new MdSequence("", new MdElement[]{e}, true);

            List<MdElement> cells = new ArrayList<>();
            List<MdElement> currentCol = new ArrayList<>();
            boolean firstItem = true;
            for (MdElement elem : new ArrayList<MdElement>(Arrays.asList(seq.getElements()))) {
                if (elem.isText() && elem.asText().getText().equals("|")) {
                    if (!firstItem || !currentCol.isEmpty()) {
                        if (currentCol.isEmpty()) {
                            currentCol.add(new MdText(" "));
                        }
                        cells.add(MdFactory.seqInline(currentCol));
                        currentCol.clear();
                    }
                } else {
                    currentCol.add(elem);
                }
                if (firstItem) {
                    firstItem = false;
                }
            }
            if (!currentCol.isEmpty()) {
                cells.add(MdFactory.seqInline(currentCol));
            }
            return new MdRow(cells.toArray(new MdElement[0]), false);
        }
        return null;
    }

}
