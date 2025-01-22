package net.thevpc.nuts.lib.doc.processor.base;

import net.thevpc.nuts.io.NCharReader;

import java.io.IOException;

public class TagTokenReader {
    String startTag;
    String endTag;
    String escape;
    String exprLang;
    NCharReader br;
    TagToken pushBack;
    boolean startOfLine;

    public TagTokenReader(String startTag, String endTag, String escape, String exprLang, NCharReader br) {
        this.startTag = startTag;
        this.endTag = endTag;
        this.escape = escape;
        this.exprLang = exprLang;
        this.br = br;
    }

    public void pushBack(TagToken t) {
        pushBack = t;
    }

    public TagToken peek() {
        if (pushBack != null) {
            return pushBack;
        }
        TagToken u = next();
        pushBack(u);
        return u;
    }

    public TagToken next() {
        try {
            if (pushBack != null) {
                TagToken c = pushBack;
                pushBack = null;
                return c;
            }
            StringBuilder plain = new StringBuilder();
            while (true) {
                if (br.read(escape)) {
                    plain.append(startTag);
                } else if (this.br.peek(startTag)) {
                    if (plain.length() > 0) {
                        String t = plain.toString();
                        this.startOfLine = t.endsWith("\n");
                        plain.setLength(0);
                        return new TagToken(TagTokenType.PLAIN, t);
                    }
                    return readSpecialToken();
                } else {
                    int rr = this.br.read();
                    if (rr >= 0) {
                        plain.append((char) rr);
                    } else {
                        break;
                    }
                }
            }
            if (plain.length() == 0) {
                return null;
            }
            String t = plain.toString();
            this.startOfLine = t.endsWith("\n");
            return new TagToken(TagTokenType.PLAIN, t);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class StringBuilderImage{
        StringBuilder buffer = new StringBuilder();
        StringBuilder image = new StringBuilder();
        public StringBuilderImage append(char c) {
            buffer.append(c);
            image.append(c);
            return this;
        }
        public StringBuilderImage appendImageOnly(char c) {
            image.append(c);
            return this;
        }
        public StringBuilderImage append(String c) {
            buffer.append(c);
            image.append(c);
            return this;
        }
        public StringBuilderImage appendImageOnly(String c) {
            image.append(c);
            return this;
        }
    }

    private TagToken readSpecialToken() throws IOException {
        int brackets = 0;
        if (!br.read(startTag)) {
            throw new IllegalArgumentException("expected " + startTag);
        }
        StringBuilderImage buffer = new StringBuilderImage();
        buffer.appendImageOnly(startTag);
        boolean end = false;
        while (!end) {
            int c = br.peek();
            if (c < 0) {
                break;
            }
            switch (c) {
                case '/': {
                    if (br.read("//")) {
                        buffer.append("//");
                        while (true) {
                            int cc = br.read();
                            if (cc < 0) {
                                break;
                            } else if (cc == '\n') {
                                buffer.append((char) cc);
                                break;
                            } else {
                                buffer.append((char) cc);
                            }
                        }
                    } else {
                        buffer.append((char) c);
                    }
                    break;
                }
                case '"': {
                    buffer.append(br.readChar());
                    while (true) {
                        int cc = br.read();
                        if (cc < 0) {
                            break;
                        } else if (cc == '"') {
                            buffer.append((char) cc);
                            break;
                        } else if (cc == '\\') {
                            buffer.append((char) cc);
                            cc = br.read();
                            if (cc >= 0) {
                                buffer.append((char) cc);
                            }
                        } else {
                            buffer.append((char) cc);
                        }
                    }
                    break;
                }
                case '\'': {
                    buffer.append(br.readChar());
                    while (true) {
                        int cc = br.read();
                        if (cc < 0) {
                            break;
                        } else if (cc == '\'') {
                            buffer.append((char) cc);
                            break;
                        } else if (cc == '\\') {
                            buffer.append((char) cc);
                            cc = br.read();
                            if (cc >= 0) {
                                buffer.append((char) cc);
                            }
                        } else {
                            buffer.append((char) cc);
                        }
                    }
                    break;
                }
                case '`': {
                    buffer.append(br.readChar());
                    while (true) {
                        int cc = br.read();
                        if (cc < 0) {
                            break;
                        } else if (cc == '`') {
                            buffer.append((char) cc);
                            break;
                        } else if (cc == '\\') {
                            buffer.append((char) cc);
                            cc = br.read();
                            if (cc >= 0) {
                                buffer.append((char) cc);
                            }
                        } else {
                            buffer.append((char) cc);
                        }
                    }
                    break;
                }
                case '{': {
                    brackets++;
                    if(isReadEndOfTag(buffer)){
                        end = true;
                    }else{
                        buffer.append(br.readChar());
                    }
                    break;
                }
                case '}': {
                    brackets--;
                    boolean peek = br.peek(endTag);
                    if (brackets <= 0) {
                        if(isReadEndOfTag(buffer)){
                            end = true;
                        }else{
                            buffer.append(br.readChar());
                        }
                    } else {
                        buffer.append(br.readChar());
                    }
                    break;
                }
                default: {
                    buffer.append(br.readChar());
                }
            }
        }
        String ss = buffer.buffer.toString().trim();
        if (ss.startsWith(":")) {
            if (TagStreamProcessor.startsWithWord(ss, ":if")) {
                return new TagToken(TagTokenType.IF, ss.substring(":if".length()).trim());

            } else if (TagStreamProcessor.startsWithWord(ss, ":else if")) {
                return new TagToken(TagTokenType.CTRL_ELSE_IF, ss.substring(":else if".length()).trim());

            } else if (TagStreamProcessor.startsWithWord(ss, ":else")) {
                return new TagToken(TagTokenType.CTRL_ELSE, ss.substring(":else".length()).trim());

            } else if (TagStreamProcessor.startsWithWord(ss, ":for")) {
                return new TagToken(TagTokenType.FOR, ss.substring(":for".length()).trim());

            } else if (TagStreamProcessor.startsWithWord(ss, ":include")) {
                return new TagToken(TagTokenType.INCLUDE, ss.substring(":include".length()).trim());

            } else if (TagStreamProcessor.startsWithWord(ss, ":end")) {
                return new TagToken(TagTokenType.CTRL_END, ss.substring(":end".length()).trim());

            } else if (ss.startsWith("::")) {
                return new TagToken(TagTokenType.STATEMENT, ss.substring(2));

            } else if (ss.startsWith(":")) {
                return new TagToken(TagTokenType.STATEMENT, ss.substring(1));

            } else {
                return new TagToken(TagTokenType.CTRL_OTHER, ss);
            }
        }
        return new TagToken(TagTokenType.EXPR, ss);
    }

    private boolean isReadEndOfTag(StringBuilderImage image) {
        try {
            if (startOfLine && br.read(endTag + "\r\n")) {
                image.appendImageOnly(endTag + "\r\n");
                return true;
            } else if (startOfLine && br.read(endTag + "\n")) {
                image.appendImageOnly(endTag + "\n");
                return true;
//            } else if (br.read(endTag + "\n")) {
//                image.appendImageOnly(endTag);
//                return true;
            } else if (br.read(endTag)) {
                image.appendImageOnly(endTag);
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}
