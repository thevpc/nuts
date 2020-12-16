package net.thevpc.nuts.runtime.standalone.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.format.text.parser.*;
import net.thevpc.nuts.runtime.standalone.format.text.renderer.AnsiUnixTermPrintRenderer;
import net.thevpc.nuts.runtime.standalone.format.text.renderer.StyleRenderer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NutsTextNodeWriterRenderer extends AbstractNutsTextNodeWriter {
    private byte[] buffer = new byte[1024];
    private int bufferSize = 0;
    private boolean enableBuffering = false;
    private byte[] later = null;
    private FormattedPrintStreamRenderer renderer;
    private OutputStream rawOutput;
    private RenderedRawStream renderedRawStream = new RenderedRawStream() {
        @Override
        public void writeRaw(byte[] buf, int off, int len) {
            NutsTextNodeWriterRenderer.this.writeRaw(new String(buf, off, len));
        }

        @Override
        public void writeLater(byte[] buf) {
            NutsTextNodeWriterRenderer.this.writeLater(buf);
        }
    };
    private NutsWorkspace ws;
    public NutsTextNodeWriterRenderer(OutputStream rawOutput, FormattedPrintStreamRenderer renderer, NutsWorkspace ws) {
        this.renderer = renderer == null ? AnsiUnixTermPrintRenderer.ANSI_RENDERER : renderer;
        this.rawOutput = rawOutput;
        this.ws = ws;
    }

    @Override
    public void writeNode(NutsTextNode node) {
        writeNode(node,getWriteConfiguration());
    }


    public void writeNode(NutsTextNode node, NutsTextNodeWriteConfiguration ctx) {
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
        try {
            rawOutput.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return false;
    }


    private void writeNode(TextFormat[] formats, NutsTextNode node, NutsTextNodeWriteConfiguration ctx) {
        if (formats == null) {
            formats = new TextFormat[0];
        }
        switch (node.getType()) {
            case PLAIN: {
                NutsTextNodePlain p = (NutsTextNodePlain) node;
                writeRaw(TextFormats.list(formats), p.getText(), ctx.isFiltered());
//        }else if (node instanceof TextNodeEscaped) {
//            TextNodeEscaped p = (TextNodeEscaped) node;
//            writeRaw(TextFormats.list(formats), p.getValue(), ctx.isFiltered());
                break;
            }
            case LIST: {
                NutsTextNodeList s = (NutsTextNodeList) node;
                for (NutsTextNode n : s) {
                    writeNode(formats, n, ctx);
                }
                break;
            }
            case STYLED: {
                DefaultNutsTextNodeStyled s = (DefaultNutsTextNodeStyled) node;
                TextFormat[] s2 = _appendFormats(formats, s.getTextFormat());
                writeNode(s2, s.getChild(), ctx);
                break;
            }
            case TITLE: {
                DefaultNutsTextNodeTitle s = (DefaultNutsTextNodeTitle) node;
                TextFormat[] s2 = _appendFormats(formats, s.getStyle());
                if (ctx.isNumberTitles()) {
                    NutsTextNodeWriteConfiguration.Seq seq = ctx.getSeq();
                    if (seq == null) {
                        seq = new NutsTextNodeWriteConfiguration.DefaultSeq(new NutsTextNodeWriteConfiguration.IntTextNumber(0));
                        ctx.setSeq(seq);
                    }
                    NutsTextNodeWriteConfiguration.Seq a = seq.newLevel(s.getTextStyleCode().length());
                    DefaultNutsTextNodeFactory factory0 = (DefaultNutsTextNodeFactory) ws.formats().text().factory();
                    writeNode(factory0.createStyled(
                            s.getTextStyleCode(), s.getTextStyleCode(),
                            ws.formats().text().factory().plain(a.getString(".") + " "),
                            s.getTextStyle(),
                            true), ctx
                    );
                }
                writeNode(s2, s.getChild(), ctx);
//        } else if (node instanceof TextNodeUnStyled) {
//            TextNodeUnStyled s = (TextNodeUnStyled) node;
//            writeNode(formats, new NutsTextNodePlain(s.getStart()), ctx);
//            writeNode(formats, s.getChild(), ctx);
//            writeNode(formats, new NutsTextNodePlain(s.getEnd()), ctx);
                break;
            }
            case COMMAND: {
                DefaultNutsTextNodeCommand s = (DefaultNutsTextNodeCommand) node;
                TextFormat[] s2 = _appendFormats(formats, s.getStyle());
                writeRaw(TextFormats.list(s2), "", ctx.isFiltered());
                break;
            }
            case ANCHOR: {
                //ignore!!
                break;
            }
            case LINK: {
                //ignore!!
                DefaultNutsTextNodeFactory factory0 = (DefaultNutsTextNodeFactory) ws.formats().text().factory();
                writeNode(
                        formats,
                        factory0.createStyled(
                                "~~", "~~",
                                ws.formats().text().factory().plain(
                                        ((NutsTextNodeLink) node).getValue()
                                ),
                                NutsTextNodeStyle.UNDERLINED,
                                true
                        ),
                        ctx
                );
                writeRaw(TextFormats.list(formats), "see: " + ((NutsTextNodeLink) node).getValue(), ctx.isFiltered());
                break;
            }
            case CODE: {
                DefaultNutsTextNodeCode node1 = (DefaultNutsTextNodeCode) node;
                NutsTextNode cn = ws.formats().text().factory().parseBloc(node1.getKind(), node1.getText());
                writeNode(formats, cn, ctx);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unsupported NutsTextNode type " + node.getClass().getSimpleName());
            }
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
