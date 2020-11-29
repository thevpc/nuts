package net.thevpc.nuts.runtime.format.text;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeList;
import net.thevpc.nuts.NutsTextNodePlain;
import net.thevpc.nuts.NutsTextNodeWriteConfiguration;
import net.thevpc.nuts.runtime.format.text.parser.*;

import java.io.*;

public class NutsTextNodeWriterStringer extends AbstractNutsTextNodeWriter {
    private OutputStream out;

    public static String toString(NutsTextNode n){
        if(n==null){
            return "";
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new NutsTextNodeWriterStringer(bos).writeNode(n, new NutsTextNodeWriteConfiguration());
        return bos.toString();
    }

    public NutsTextNodeWriterStringer(OutputStream out) {
        this.out = out;
    }

    @Override
    public void writeNode(NutsTextNode node) {
        writeNode(node,getWriteConfiguration());
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

    public void writeNode(NutsTextNode node, NutsTextNodeWriteConfiguration ctx) {
        if (node == null) {
            return;
        }
        if(ctx==null){
            ctx=new NutsTextNodeWriteConfiguration();
        }
        switch (node.getType()) {
            case PLAIN:
                NutsTextNodePlain p = (NutsTextNodePlain) node;
                writeRaw(p.getText());
                break;
            case LIST: {
                NutsTextNodeList s = (NutsTextNodeList) node;
                for (NutsTextNode n : s) {
                    writeNode(n, ctx);
                }
                break;
            }
            case STYLED: {
                DefaultNutsTextNodeStyled s = (DefaultNutsTextNodeStyled) node;
                if (ctx.isFiltered()) {
                    writeNode(s.getChild(), ctx);
                } else {
                    writeRaw(s.getStart());
                    writeNode(s.getChild(), ctx);
                    writeRaw(s.getEnd());
                    writeRaw("ø");
                }
                break;
            }
            case TITLE: {
                DefaultNutsTextNodeTitle s = (DefaultNutsTextNodeTitle) node;
                if (!ctx.isFiltered()) {
                    writeRaw(s.getStart());
                }
                if (ctx.isNumberTitles()) {
                    NutsTextNodeWriteConfiguration.Seq seq = ctx.getSeq();
                    if (seq == null) {
                        NutsTextNodeWriteConfiguration.IntTextNumber intTextNumber = new NutsTextNodeWriteConfiguration.IntTextNumber(0);
                        seq = new NutsTextNodeWriteConfiguration.DefaultSeq(intTextNumber);
                        ctx.setSeq(seq);
                    }
                    NutsTextNodeWriteConfiguration.Seq a = seq.newLevel(s.getStyleCode().length());
                    String ts = a.getString(".") + " ";
//                if(startWritten){
//                    ts=" "+ts;
//                }
                    writeRaw(ts);
                }
                writeNode(s.getChild(), ctx);
//        } else if (node instanceof TextNodeUnStyled) {
//            TextNodeUnStyled s = (TextNodeUnStyled) node;
//            writeRaw(s.getStart());
//            writeNode(s.getChild(), ctx);
//            writeRaw(s.getEnd());
                break;
            }
            case COMMAND: {
                DefaultNutsTextNodeCommand s = (DefaultNutsTextNodeCommand) node;
                if (!ctx.isFiltered()) {
                    writeRaw(s.getStart());
                    writeEscapedSpecial(s.getText());
                    writeRaw(s.getEnd());
                    writeRaw("ø");
                }
                break;
            }
            case ANCHOR: {
                DefaultNutsTextNodeAnchor s = (DefaultNutsTextNodeAnchor) node;
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
                DefaultNutsTextNodeLink s = (DefaultNutsTextNodeLink) node;
                if (!ctx.isFiltered()) {
                    writeRaw(s.getStart());
                    writeRaw(s.getKind());
                    writeRaw(s.getSeparator());
                    writeEscapedSpecial(s.getValue());
                    writeRaw(s.getEnd());
                    writeRaw("ø");
                } else {
                    writeRaw(s.getValue());
                }
                break;
            }
            case CODE: {
                DefaultNutsTextNodeCode s = (DefaultNutsTextNodeCode) node;
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
                throw new UnsupportedOperationException("Invalid node type : " + node.getClass().getSimpleName());
        }
    }

    public final void writeEscapedSpecial(String rawString) {
        char[] cc=rawString.toCharArray();
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < cc.length; i++) {
            if(cc[i]=='\\' || (i<cc.length-3 && cc[i]=='`' && cc[i+1]=='`' && cc[i+2]=='`')){
                sb.append('\\');
            }
            sb.append(cc[i]);
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
}
