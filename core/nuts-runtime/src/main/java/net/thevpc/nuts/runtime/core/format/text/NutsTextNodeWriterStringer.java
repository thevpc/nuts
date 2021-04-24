package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.parser.*;

import java.io.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

public class NutsTextNodeWriterStringer extends AbstractNutsTextNodeWriter {

    private OutputStream out;
    private NutsWorkspace ws;

    public static String toString(NutsText n, NutsWorkspace ws) {
        if (n == null) {
            return "";
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new NutsTextNodeWriterStringer(bos, ws).writeNode(n, new NutsTextWriteConfiguration());
        return bos.toString();
    }

    public NutsTextNodeWriterStringer(OutputStream out, NutsWorkspace ws) {
        this.out = out;
        this.ws = ws;
    }

    @Override
    public void writeNode(NutsText node) {
        writeNode(node, getWriteConfiguration());
    }

    @Override
    public void writeRaw(byte[] buf, int off, int len) {
        writeRaw(new String(buf, off, len));
    }

    @Override
    public boolean flush() {
        try {
            out.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return true;
    }

    public void writeNode(NutsText node, NutsTextWriteConfiguration ctx) {
        if (node == null) {
            return;
        }
        if (ctx == null) {
            ctx = new NutsTextWriteConfiguration();
        }
        switch (node.getType()) {
            case PLAIN:
                NutsTextPlain p = (NutsTextPlain) node;
                if (ctx.isFiltered()) {
                    writeRaw(p.getText());
                } else {
                    writeEscaped(p.getText());
                }
                break;
            case LIST: {
                NutsTextList s = (NutsTextList) node;
                for (NutsText n : s) {
                    writeNode(n, ctx);
                }
                break;
            }
            case STYLED: {
                DefaultNutsTextStyled s = (DefaultNutsTextStyled) node;
                if (ctx.isFiltered()) {
                    writeNode(s.getChild(), ctx);
                } else {
                    if (s.getChild().getType() == NutsTextType.PLAIN) {
                        NutsTextStyles styles = s.getStyles();
                        writeStyledStart(s.getStyles().get(0), false);
                        if (styles.size() <= 1) {
                            writeNode(s.getChild(), ctx);
                        } else {
                            writeNode(
                                    ws.formats().text().forStyled(s.getChild(), s.getStyles().removeFirst()),
                                    ctx);
                        }
                        writeRaw(s.getEnd());
                        writeRaw("ø");
                    } else {
                        NutsTextStyles styles = s.getStyles();
                        writeStyledStart(s.getStyles().get(0), true);
                        if (styles.size() <= 1) {
                            writeNode(s.getChild(), ctx);
                        } else {
                            writeNode(
                                    ws.formats().text().forStyled(s.getChild(), s.getStyles().removeFirst()),
                                    ctx);
                        }
                        writeRaw(s.getEnd());
                        writeRaw("}##ø");
                    }
                }
                break;
            }
            case TITLE: {
                DefaultNutsTextTitle s = (DefaultNutsTextTitle) node;
                if (!ctx.isFiltered()) {
                    writeRaw(s.getStart());
                }
                if (ctx.isTitleNumberEnabled()) {
                    NutsTextNumbering seq = ctx.getTitleNumberSequence();
                    if (seq == null) {
                        seq = ws.formats().text().forNumbering();
                        ctx.setTitleNumberSequence(seq);
                    }
                    NutsTextNumbering a = seq.newLevel(s.getTextStyleCode().length());
                    String ts = a.toString() + " ";
//                if(startWritten){
//                    ts=" "+ts;
//                }
                    writeRaw(ts);
                }
                writeNode(s.getChild(), ctx);
//        } else if (text instanceof TextNodeUnStyled) {
//            TextNodeUnStyled s = (TextNodeUnStyled) text;
//            writeRaw(s.getStart());
//            writeNode(s.getChild(), ctx);
//            writeRaw(s.getEnd());
                break;
            }
            case COMMAND: {
                DefaultNutsTextCommand s = (DefaultNutsTextCommand) node;
                if (!ctx.isFiltered()) {
                    writeRaw(s.getStart());
                    writeRaw(s.getKind());
                    writeEscapedSpecial(s.getCommand().getName());
                    if (!CoreStringUtils.isBlank(s.getCommand().getArgs())) {
                        writeEscapedSpecial(" ");
                        writeEscapedSpecial(s.getCommand().getArgs());
                    }
                    writeRaw(s.getEnd());
                    writeRaw("ø");
                }
                break;
            }
            case ANCHOR: {
                DefaultNutsTextAnchor s = (DefaultNutsTextAnchor) node;
                if (!ctx.isFiltered()) {
                    writeRaw(s.getStart());
                    writeRaw(s.getKind());
                    writeRaw(s.getSeparator());
                    writeEscapedSpecial(s.getValue());
                    writeRaw(s.getEnd());
                    writeRaw("ø");
                }
                break;
            }
            case LINK: {
                DefaultNutsTextLink s = (DefaultNutsTextLink) node;
                if (!ctx.isFiltered()) {
                    writeRaw(s.getStart());
                    writeRaw(s.getKind());
                    writeRaw(s.getSeparator());
                    writeNode(s.getChild());
                    writeRaw(s.getEnd());
                    writeRaw("ø");
                } else {
                    writeNode(s.getChild());
                }
                break;
            }
            case CODE: {
                DefaultNutsTextCode s = (DefaultNutsTextCode) node;
                if (!ctx.isFiltered()) {
                    writeRaw(s.getStart());
                    writeRaw(s.getKind());
                    writeRaw(s.getSeparator());
                    writeEscapedSpecial(s.getText());
                    writeRaw(s.getEnd());
                    writeRaw("ø");
                } else {
                    writeRaw(s.getText());
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("invalid node type : " + node.getClass().getSimpleName());
        }
    }

    public final void writeEscapedSpecial(String rawString) {
        char[] cc = rawString.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cc.length; i++) {
            if (cc[i] == '\\' || (i < cc.length - 3 && cc[i] == '`' && cc[i + 1] == '`' && cc[i + 2] == '`')) {
                sb.append('\\');
            }
            sb.append(cc[i]);
        }
        writeRaw(sb.toString());
    }

    public final void writeEscaped(String rawString) {
        char[] cc = rawString.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cc.length; i++) {
            switch (cc[i]) {
                case '\\':
                case 'ø': {
                    sb.append('\\');
                    sb.append(cc[i]);
                    break;
                }
                case '`': {
                    if (i < cc.length - 3) {
                        if (cc[i] == '`' && cc[i + 1] == '`' && cc[i + 2] == '`') {
                            sb.append('\\');
                            sb.append(cc[i]);
                        } else {
                            sb.append(cc[i]);
                        }
                    } else if (i < cc.length - 1) {
                        if (cc[i] == '`' && cc[i + 1] == '`') {
                            sb.append('\\');
                            sb.append(cc[i]);
                        } else {
                            sb.append(cc[i]);
                        }
                    } else {
                        sb.append('\\');
                        sb.append(cc[i]);
                    }
                    break;
                }
                case '#':
                case '@':
                case '~': {
                    if (i < cc.length - 1 && cc[i + 1] != cc[i]) {
                        sb.append(cc[i]);
                    } else {
                        sb.append('\\');
                        sb.append(cc[i]);
                    }
                    break;
                }
                default: {
                    sb.append(cc[i]);
                }
            }
        }
        writeRaw(sb.toString());
    }

//    public final void writeEscaped(String rawString) {
//        writeRaw(DefaultNutsTextNodeParser.escapeText0(rawString));
//    }
    public final void writeRaw(String rawString) {
        try {
            out.write(rawString.getBytes());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void writeStyledStart(NutsTextStyle style, boolean complex) {
        String h = complex ? "##{" : "##:";
        switch (style.getType()) {
            case FORE_COLOR: {
                writeRaw(h + "f" + style.getVariant() + ":");
                break;
            }
            case BACK_COLOR: {
                writeRaw(h + "b" + style.getVariant() + ":");
                break;
            }
            case FORE_TRUE_COLOR: {
                String s = Integer.toString(0, style.getVariant());
                while (s.length() < 8) {
                    s = "0" + s;
                }
                writeRaw(h + "fx" + s + ":");
                break;
            }
            case BACK_TRUE_COLOR: {
                String s = Integer.toString(0, style.getVariant());
                while (s.length() < 8) {
                    s = "0" + s;
                }
                writeRaw(h + "bx" + style.getVariant() + ":");
                break;
            }
            case UNDERLINED: {
                writeRaw(h + "_:");
                break;
            }
            case ITALIC: {
                writeRaw(h + "/:");
                break;
            }
            case STRIKED: {
                writeRaw(h + "-:");
                break;
            }
            case REVERSED: {
                writeRaw(h + "!:");
                break;
            }
            case BOLD: {
                writeRaw(h + "+:");
                break;
            }
            case BLINK: {
                writeRaw(h + "%:");
                break;
            }
            case PRIMARY: {
                writeRaw(h + "p" + style.getVariant() + ":");
                break;
            }
            case SECONDARY: {
                writeRaw(h + "s" + style.getVariant() + ":");
                break;
            }
            default: {
                writeRaw(h
                        + style.getType().toString().toLowerCase() + style.getVariant()
                        + ":");
                break;
            }
        }
    }
}
