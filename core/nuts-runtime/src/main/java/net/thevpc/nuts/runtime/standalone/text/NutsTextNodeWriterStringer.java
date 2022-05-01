package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.parser.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NutsTextNodeWriterStringer extends AbstractNutsTextNodeWriter {

    private OutputStream out;
    private NutsSession session;

    public NutsTextNodeWriterStringer(OutputStream out, NutsSession session) {
        this.out = out;
        this.session = session;
    }

    public static String toString(NutsText n, NutsSession ws) {
        if (n == null) {
            return "";
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new NutsTextNodeWriterStringer(bos, ws).writeNode(n, new NutsTextWriteConfiguration());
        return bos.toString();
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
    public void writeRaw(char[] buf, int off, int len) {
        writeRaw(new String(buf, off, len));
    }


    @Override
    public boolean flush() {
        try {
            out.flush();
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
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
//                    writeEscaped(p.getText());
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
//                    writeNode(s.getChild(), ctx.copy().setFiltered(false));
                    writeNode(s.getChild(), ctx);
                } else {
                    NutsTextStyles styles = s.getStyles();
                    if (s.getChild().getType() == NutsTextType.PLAIN) {
                        writeRaw("##:"+styles.id()+":");
                        writeNode(s.getChild(), ctx);
                        writeRaw("##");
                        writeRaw(NutsConstants.Ntf.SILENT);
                    } else {
                        writeRaw("##{"+styles.id()+":");
                        writeNode(s.getChild(), ctx);
                        writeRaw("}##");
                        writeRaw(NutsConstants.Ntf.SILENT);
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
                        seq = NutsTexts.of(session).ofNumbering();
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
//                    writeRaw(s.getKind());
                    writeEscapedSpecial(s.getCommand().getName());
                    if (!NutsBlankable.isBlank(s.getCommand().getArgs())) {
                        writeEscapedSpecial(" ");
                        writeEscapedSpecial(s.getCommand().getArgs());
                    }
                    writeRaw(s.getEnd());
                    writeRaw(NutsConstants.Ntf.SILENT);
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
                    writeRaw(NutsConstants.Ntf.SILENT);
                }
                break;
            }
            case LINK: {
                DefaultNutsTextLink s = (DefaultNutsTextLink) node;
                if (!ctx.isFiltered()) {
                    writeRaw(s.getStart());
                    writeRaw(s.getKind());
                    writeRaw(s.getSeparator());
                    writeEscaped(s.getValue());
                    writeRaw(s.getEnd());
                    writeRaw(NutsConstants.Ntf.SILENT);
                } else {
                    writeRaw(s.getValue());
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
                    writeRaw(NutsConstants.Ntf.SILENT);
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
            if (i <= cc.length - 3 && cc[i] == '`' && cc[i + 1] == '`' && cc[i + 2] == '`') {
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
                case NutsConstants.Ntf.SILENT: {
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
                case '#': {
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
    public final void writeRaw(char rawChar) {
        writeRaw(String.valueOf(rawChar));
    }

    public final void writeRaw(String rawString) {
        try {
            out.write(rawString.getBytes());
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    private void writeStyledStart(NutsTextStyles styles, boolean complex) {
        StringBuilder sb=new StringBuilder();
        if(complex){
            sb.append("##{");
        }else{
            sb.append("##:");
        }
        sb.append(styles.id());
        sb.append(":");
        writeRaw(sb.toString());
    }
}
