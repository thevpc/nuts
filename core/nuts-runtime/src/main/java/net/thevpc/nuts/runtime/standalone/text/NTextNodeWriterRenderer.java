package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNTextCommand;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNTextStyled;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNTextTitle;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.*;

public class NTextNodeWriterRenderer extends AbstractNTextNodeWriter {

    private byte[] buffer = new byte[1024];
    private int bufferSize = 0;
    private boolean enableBuffering = false;
    private byte[] later = null;
    private NPrintStream rawOutput;
    private RenderedRawStream renderedRawStream = new RenderedRawStream() {

        public NPrintStream baseOutput() {
            return rawOutput;
        }

        @Override
        public void writeRaw(byte[] buf, int off, int len) {
            NTextNodeWriterRenderer.this.writeRaw(new String(buf, off, len));
        }

        @Override
        public void writeLater(byte[] buf) {
            NTextNodeWriterRenderer.this.writeLater(buf);
        }
    };
    private NSystemTerminalBase term;

    public NTextNodeWriterRenderer(NPrintStream rawOutput, NSystemTerminalBase term) {
        this.rawOutput = rawOutput;
        this.term = term;
    }

    @Override
    public void writeNode(NText node) {
        writeNode(node, getWriteConfiguration());
    }

    @Override
    public final void writeRaw(byte[] buf, int off, int len) {
        rawOutput.write(buf, off, len);
    }

    @Override
    public void writeRaw(char[] buf, int off, int len) {
        writeRaw(new String(buffer, off, len));
    }

    @Override
    public final boolean flush() {
        if (bufferSize > 0) {
            rawOutput.write(buffer, 0, bufferSize);
            bufferSize = 0;
            return true;
        }
        rawOutput.flush();
        return false;
    }

    public void writeNode(NText node, NTextTransformConfig ctx) {
        writeNode(NTextStyles.of(), node, ctx, null);
    }

    private void writeNode(NTextStyles formats, NText node, NTextTransformConfig ctx, NTexts txt) {
        if (formats == null) {
            formats = NTextStyles.of();
        }
        if (txt == null) {
            txt = NTexts.of();
        }
        switch (node.getType()) {
            case PLAIN: {
                NTextPlain p = (NTextPlain) node;
                writeRaw(formats, p.getText(), ctx.isFiltered());
                break;
            }
            case LIST: {
                NTextList s = (NTextList) node;
                for (NText n : s) {
                    writeNode(formats, n, ctx, txt);
                }
                break;
            }
            case BUILDER: {
                NTextBuilder s = (NTextBuilder) node;
                for (NText n : s.getChildren()) {
                    writeNode(formats, n, ctx, txt);
                }
                break;
            }
            case STYLED: {
                DefaultNTextStyled s = (DefaultNTextStyled) node;
                NTextStyles styles = s.getStyles();
                NTextStyles format = txt.getTheme().toBasicStyles(styles,ctx.isBasicTrueStyles());
                NTextStyles s2 = formats.append(format);
                writeNode(s2, s.getChild(), ctx, txt);
                break;
            }
            case TITLE: {
                DefaultNTextTitle s = (DefaultNTextTitle) node;
                NTextStyles s2 = formats.append(txt.getTheme().toBasicStyles(
                        NTextStyles.of(NTextStyle.title(s.getLevel())),ctx.isBasicTrueStyles()
                ));
                if (ctx.isProcessTitleNumbers()) {
                    NTitleSequence seq = ctx.getTitleNumberSequence();
                    if (seq == null) {
                        seq = txt.ofNumbering();
                        ctx.setTitleNumberSequence(seq);
                    }
                    NTitleSequence a = seq.next(s.getLevel());
                    NText sWithTitle = txt.ofList(
                            txt.ofPlain(a.toString() + " "),
                            s.getChild()
                    );
                    writeNode(s2, sWithTitle, ctx, txt);
                    writeRaw("\n");
                } else {
                    NText sWithTitle = txt.ofList(
                            txt.ofPlain(CoreStringUtils.fillString('#', s.getLevel()) + ") "),
                            s.getChild()
                    );
                    writeNode(s2, sWithTitle, ctx, txt);
                    writeRaw("\n");
                }
                break;
            }
            case COMMAND: {
                DefaultNTextCommand s = (DefaultNTextCommand) node;
                if (term != null) {
                    if (!ctx.isFiltered()) {
                        term.run(s.getCommand(), rawOutput);
                    }
                }
                break;
            }
            case ANCHOR: {
                //ignore!!
                break;
            }
            case LINK: {
                //ignore!!
                NTextPlain child = txt.ofPlain(((NTextLink) node).getValue());
                writeNode(formats,
                        txt.ofStyled(child,
                                NTextStyles.of(NTextStyle.underlined())
                        ), ctx, txt);
                writeRaw(formats, "see: " + ((NTextLink) node).getValue(), ctx.isFiltered());
                break;
            }
            case INCLUDE: {
                //ignore!!
                NTextPlain child = txt.ofPlain(((NTextInclude) node).getText());
                writeNode(formats,
                        txt.ofStyled(
                                txt.ofList(
                                        txt.ofPlain("include: "),
                                        child
                                )
                                ,
                                NTextStyles.of(NTextStyle.warn())
                        ), ctx, txt);
                break;
            }
            case CODE: {
                NTextCode node1 = (NTextCode) node;
                if (ctx.isFiltered()) {
                    writeRaw(formats, node1.getValue(), true);
                } else {
                    NText cn = node1.highlight();
                    writeNode(formats, cn, ctx, txt);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("unsupported NutsTextNode type " + node.getClass().getSimpleName());
            }
        }
    }

    protected void writeRaw(NTextStyles format, String rawString, boolean filterFormat) {
        if (!filterFormat && format != null) {
            if (rawString.length() > 0) {
                if (format.isPlain()) {
                    writeRaw(rawString);
                } else {
                    flush();
                    if (term != null) {
                        term.setStyles(format, rawOutput);
                    }
                    try {
                        writeRaw(rawString);
                    } finally {
                        if (term != null) {
                            term.setStyles(null, rawOutput);
                        }
                    }
                }
            }
        } else {
            if (rawString.length() > 0) {
                writeRaw(rawString);
            }
        }
    }

    public final void writeRaw(String rawString) {
        flushLater();
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
    }


    public final void writeLater(byte[] later) {
        this.later = later;
        rawOutput.flush();
    }

    public final void flushLater() {
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
    }

    @Override
    public String toString() {
        return "Printer(" + rawOutput + (this.later != null ? ";withLater" : "") + ")";
    }

}
