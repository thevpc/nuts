package net.thevpc.nuts.runtime.util.fprint;

import net.thevpc.nuts.runtime.util.fprint.parser.*;
import net.thevpc.nuts.runtime.util.fprint.renderer.AnsiUnixTermPrintRenderer;
import net.thevpc.nuts.runtime.util.fprint.renderer.StyleRenderer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TextNodeWriterRenderer implements TextNodeWriter {
    private byte[] buffer = new byte[1024];
    private int bufferSize = 0;
    private boolean enableBuffering = false;
    private byte[] later = null;
    private FormattedPrintStreamRenderer renderer;
    private OutputStream rawOutput;
    private RenderedRawStream renderedRawStream = new RenderedRawStream() {
        @Override
        public void writeRaw(byte[] buf, int off, int len) {
            TextNodeWriterRenderer.this.writeRaw(new String(buf, off, len));
        }

        @Override
        public void writeLater(byte[] buf) {
            TextNodeWriterRenderer.this.writeLater(buf);
        }
    };

    public TextNodeWriterRenderer(OutputStream rawOutput, FormattedPrintStreamRenderer renderer) {
        this.renderer = renderer == null ? AnsiUnixTermPrintRenderer.ANSI_RENDERER : renderer;
        this.rawOutput = rawOutput;
    }

    @Override
    public void writeNode(TextNode node, TextNodeWriterContext ctx) {
        writeNode(new TextFormat[0], node, ctx);
    }

    @Override
    public final void writeRaw(byte[] buf, int off, int len) {
        try {
            rawOutput.write(buf, off, len);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public final boolean flush() {
        if (bufferSize > 0) {
            try {
                rawOutput.write(buffer, 0, bufferSize);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            bufferSize = 0;
            return true;
        }
        return false;
    }


    private void writeNode(TextFormat[] formats, TextNode node, TextNodeWriterContext ctx) {
        if (formats == null) {
            formats = new TextFormat[0];
        }
        if (node instanceof TextNodePlain) {
            TextNodePlain p = (TextNodePlain) node;
            writeRaw(TextFormats.list(formats), p.getValue(), ctx.isFiltered());
        }else if (node instanceof TextNodeEscaped) {
            TextNodeEscaped p = (TextNodeEscaped) node;
            writeRaw(TextFormats.list(formats), p.getValue(), ctx.isFiltered());
        } else if (node instanceof TextNodeList) {
            TextNodeList s = (TextNodeList) node;
            for (TextNode n : s) {
                writeNode(formats, n, ctx);
            }
        } else if (node instanceof TextNodeStyled) {
            TextNodeStyled s = (TextNodeStyled) node;
            TextFormat[] s2 = _appendFormats(formats, s.getStyle());
            writeNode(s2, s.getChild(), ctx);
        } else if (node instanceof TextNodeTitle) {
            TextNodeTitle s = (TextNodeTitle) node;
            TextFormat[] s2 = _appendFormats(formats, s.getStyle());
            if(ctx.isNumberTitles()){
                TextNodeWriterContext.TextNumberSeq seq = ctx.getSeq();
                if(seq==null){
                    seq=new TextNodeWriterContext.DefaultTextNumberSeq(new TextNodeWriterContext.IntTextNumber(0));
                    ctx.setSeq(seq);
                }
                TextNodeWriterContext.TextNumberSeq a = seq.newLevel(s.getStyleCode().length());
                writeNode(new TextNodeStyled(s.getStyleCode(),s.getStyleCode(),s.getStyle(),new TextNodeEscaped(a.getString(".")+" ")),ctx);
            }
            writeNode(s2, s.getChild(), ctx);
        } else if (node instanceof TextNodeUnStyled) {
            TextNodeUnStyled s = (TextNodeUnStyled) node;
            writeNode(formats, new TextNodePlain(s.getStart()), ctx);
            writeNode(formats, s.getChild(), ctx);
            writeNode(formats, new TextNodePlain(s.getEnd()), ctx);
        } else if (node instanceof TextNodeCommand) {
            TextNodeCommand s = (TextNodeCommand) node;
            TextFormat[] s2 = _appendFormats(formats, s.getStyle());
            writeRaw(TextFormats.list(s2), "", ctx.isFiltered());
        } else if (node instanceof TextNodeAnchor) {
            //ignore!!
        } else {
            throw new UnsupportedOperationException("Unsupported TextNode type "+node.getClass().getSimpleName());
        }
    }

    protected void writeRaw(TextFormat format, String rawString, boolean filterFormat) {
        if (!filterFormat && format != null) {
            StyleRenderer f = null;
            f = renderer.createStyleRenderer(simplifyFormat(format));
            try {
                f.startFormat(renderedRawStream);
                if (rawString.length() > 0) {
                    writeRaw(rawString);
                }
            } finally {
                f.endFormat(renderedRawStream);
            }
        } else {
            if (rawString.length() > 0) {
                writeRaw(rawString);
            }
        }
    }

    public final void writeRaw(String rawString) {
        flushLater();
        try {
            byte[] b = rawString.getBytes();
            if (enableBuffering) {
                if (b.length + bufferSize < buffer.length) {
                    System.arraycopy(b, 0, buffer, bufferSize, b.length);
                    bufferSize += b.length;
                } else {
                    flush();
                    if (b.length >= buffer.length) {
                        rawOutput.write(b, 0, b.length);
                    } else {
                        System.arraycopy(b, 0, buffer, bufferSize, b.length);
                        bufferSize += b.length;
                    }
                }
            } else {
                rawOutput.write(b, 0, b.length);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected TextFormat simplifyFormat(TextFormat f) {
        if (f instanceof TextFormatList) {
            TextFormat[] o = ((TextFormatList) f).getChildren();
            List<TextFormat> ok = new ArrayList<>();
            if (o != null) {
                for (TextFormat v : o) {
                    if (v != null) {
                        v = simplifyFormat(v);
                        if (v != null) {
                            ok.add(v);
                        }
                    }
                }
            }
            if (ok.isEmpty()) {
                return null;
            }
            if (ok.size() == 1) {
                return simplifyFormat(ok.get(0));
            }
            return TextFormats.list(ok.toArray(new TextFormat[0]));
        }
        return f;
    }

    private TextFormat[] _appendFormats(TextFormat[] old, TextFormat v) {
        List<TextFormat> list = new ArrayList<TextFormat>((old == null ? 0 : old.length) + 1);
        if (old != null) {
            list.addAll(Arrays.asList(old));
        }
        list.add(v);
        return list.toArray(new TextFormat[0]);
    }

    public final void writeLater(byte[] later){
        this.later = later;
        try {
            rawOutput.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public final void flushLater() {
        try {
            byte[] b = later;
            if (b != null) {
                later = null;
                if (enableBuffering) {
                    if (b.length + bufferSize < buffer.length) {
                        System.arraycopy(b, 0, buffer, bufferSize, b.length);
                        bufferSize += b.length;
                    } else {
                        flush();
                        if (b.length >= buffer.length) {
                            rawOutput.write(b, 0, b.length);
                        } else {
                            System.arraycopy(b, 0, buffer, bufferSize, b.length);
                            bufferSize += b.length;
                        }
                    }
                } else {
                    rawOutput.write(b, 0, b.length);
                    rawOutput.flush();
                }
                //flush();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return "Printer(" + rawOutput + (this.later != null ? ";withLater" : "") + ")";
    }
}
