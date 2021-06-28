/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.docusaurus;

import net.thevpc.nuts.lib.md.*;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class DocusaurusInlineParser {

    private String line;

    public DocusaurusInlineParser(String line) {
        this.line = line;
    }

    public MdElement parse() {
        return parseInlineText(line);
    }

    private MdElement parseInlineText(String line) {
        PushbackReader pbr = new PushbackReader(new StringReader(line), 1024);
        List<MdElement> a = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        try {
            while (true) {
                String code;
                if ((code = parseFramedText(pbr, "```", "```")) != null) {
                    parseInlineText_helper(sb, a, new MdCode("", code, true));
                } else if ((code = parseFramedText(pbr, "**", "**")) != null) {
                    parseInlineText_helper(sb, a, new MdBold("**", parseInlineText(code)));
                } else if ((code = parseFramedText(pbr, "[", "]")) != null) {
                    String linkTitle = code;
                    if ((code = parseFramedText(pbr, "(", ")")) != null) {
                        parseInlineText_helper(sb, a, new MdLink("", linkTitle, code));
                        continue;
                    }
                    parseInlineText_helper(sb, a, new MdText("["));
                    parseInlineText_helper(sb, a, parseInlineText(linkTitle));
                    parseInlineText_helper(sb, a, new MdText("["));
                    continue;
                } else if ((code = parseFramedText(pbr, "![", "]")) != null) {
                    String linkTitle = code;
                    if ((code = parseFramedText(pbr, "(", ")")) != null) {
                        parseInlineText_helper(sb, a, new MdImage("", linkTitle, code));
                        continue;
                    }
                    parseInlineText_helper(sb, a, (MdElement) new MdText("["));
                    parseInlineText_helper(sb, a, parseInlineText(linkTitle));
                    parseInlineText_helper(sb, a, new MdText("["));
                    continue;
                } else {
                    int c = pbr.read();
                    if (c < 0) {
                        break;
                    }
                    if (c == '|') {
                        parseInlineText_helper(sb, a, new MdText("|"));
                        continue;
                    }
                    sb.append((char) c);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        if (sb.length() > 0) {
            a.add(new MdText(sb.toString()));
            sb.setLength(0);
        }
        if (a.size() == 0) {
            return (MdElement) new MdText("");
        }
        if (a.size() == 1) {
            return a.get(0);
        }
        return (MdElement) new MdSequence("", a.<MdElement>toArray(new MdElement[0]), true);
    }

    private boolean expect(PushbackReader pbr, String value) {
        if (consume(pbr, value)) {
            try {
                pbr.unread(value.length());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return true;
        }
        return false;
    }

    private boolean consume(PushbackReader pbr, String value) {
        try {
            int count = value.length();
            char[] c = new char[count];
            int r = pbr.read(c);
            if (r <= 0) {
                return false;
            }
            if (r < count) {
                pbr.unread(c, 0, r);
                return false;
            }
            if (!value.equals(new String(c, 0, r))) {
                pbr.unread(c, 0, r);
                return false;
            }
            return true;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void parseInlineText_helper(StringBuilder sb, List<MdElement> a, MdElement elem) {
        if (sb.length() > 0) {
            a.add(new MdText(sb.toString()));
            sb.setLength(0);
        }
        if (elem != null) {
            a.add(elem);
        }
    }

    private String parseFramedText(PushbackReader pbr, String start, String stop) {
        try {
            if (consume(pbr, start)) {
                StringBuilder code = new StringBuilder();
                while (!consume(pbr, stop)) {
                    int c = pbr.read();
                    if (c < 0) {
                        break;
                    }
                    code.append((char) c);
                }
                return code.toString();
            }
            return null;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
