package net.thevpc.nuts.runtime.util.fprint;

import net.thevpc.nuts.runtime.util.fprint.parser.*;

import java.io.*;

public class TextNodeWriterStringer implements TextNodeWriter {
    private OutputStream out;

    public static String toString(TextNode n){
        if(n==null){
            return "";
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new TextNodeWriterStringer(bos).writeNode(n, new TextNodeWriterContext());
        return bos.toString();
    }

    public TextNodeWriterStringer(OutputStream out) {
        this.out = out;
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

    @Override
    public void writeNode(TextNode node, TextNodeWriterContext ctx) {
        if (node == null) {
            return;
        }
        if (node instanceof TextNodePlain) {
            TextNodePlain p = (TextNodePlain) node;
            writeRaw(p.getValue());
        } else if (node instanceof TextNodeEscaped) {
            TextNodeEscaped p = (TextNodeEscaped) node;
            writeEscaped(p.getValue());
        } else if (node instanceof TextNodeList) {
            TextNodeList s = (TextNodeList) node;
            for (TextNode n : s) {
                writeNode(n, ctx);
            }
        } else if (node instanceof TextNodeStyled) {
            TextNodeStyled s = (TextNodeStyled) node;
            if (ctx.isFiltered()) {
                writeNode(s.getChild(), ctx);
            } else {
                writeRaw(s.getStart());
                writeNode(s.getChild(), ctx);
                writeRaw(s.getEnd());
            }
        } else if (node instanceof TextNodeTitle) {
            TextNodeTitle s = (TextNodeTitle) node;
            boolean startWritten=false;
            if (!ctx.isFiltered()) {
                writeRaw(s.getStart());
                startWritten=true;
            }
            if(ctx.isNumberTitles()){
                TextNodeWriterContext.TextNumberSeq seq = ctx.getSeq();
                if(seq==null){
                    seq=new TextNodeWriterContext.DefaultTextNumberSeq(new TextNodeWriterContext.IntTextNumber(0));
                    ctx.setSeq(seq);
                }
                TextNodeWriterContext.TextNumberSeq a = seq.newLevel(s.getStyleCode().length());
                String ts = a.getString(".") + " ";
//                if(startWritten){
//                    ts=" "+ts;
//                }
                writeRaw(ts);
            }
            writeNode(s.getChild(), ctx);
        } else if (node instanceof TextNodeUnStyled) {
            TextNodeUnStyled s = (TextNodeUnStyled) node;
            writeRaw(s.getStart());
            writeNode(s.getChild(), ctx);
            writeRaw(s.getEnd());
        } else if (node instanceof TextNodeAnchor) {
            TextNodeAnchor s = (TextNodeAnchor) node;
            if (!ctx.isFiltered()) {
                writeRaw(s.getStart());
                writeEscaped(s.getValue());
                writeRaw(s.getEnd());
            }
        } else if (node instanceof TextNodeCommand) {
            TextNodeCommand s = (TextNodeCommand) node;
            if (!ctx.isFiltered()) {
                writeRaw(s.getStart());
                writeEscaped(s.getText());
                writeRaw(s.getEnd());
            }
        } else {
            throw new UnsupportedOperationException("Invalid node type : " + node.getClass().getSimpleName());
        }
    }

    public final void writeEscaped(String rawString) {
        writeRaw(DefaultTextNodeParser.escapeText0(rawString));
    }

    public final void writeRaw(String rawString) {
        try {
            out.write(rawString.getBytes());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
