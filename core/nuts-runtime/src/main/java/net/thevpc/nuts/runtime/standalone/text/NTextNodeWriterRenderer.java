package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNSystemTerminalBase;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNTextCommand;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNTextStyled;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNTextTitle;
import net.thevpc.nuts.runtime.standalone.io.printstream.NPrintStreamHelper;
import net.thevpc.nuts.runtime.standalone.io.outputstream.OutputHelper;
import net.thevpc.nuts.runtime.standalone.io.outputstream.OutputStreamHelper;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.*;

import java.io.IOException;
import java.io.OutputStream;

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
    private NSession session;
    private NSystemTerminalBase term;

//    public NTextNodeWriterRenderer(NPrintStream rawOutput, NSession session) {
//        this(new NPrintStreamHelper(rawOutput), session, rawOutput.getTerminal());
//    }
//
//    public NTextNodeWriterRenderer(OutputStream rawOutput, NSession session, NSystemTerminalBase term) {
//        this(new OutputStreamHelper(rawOutput, session), session, term);
//    }

    public NTextNodeWriterRenderer(NPrintStream rawOutput, NSession session, NSystemTerminalBase term) {
        this.rawOutput = rawOutput;
        this.session = session;
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
        writeNode(NTextStyles.of(), node, ctx);
    }

    private void writeNode(NTextStyles formats, NText node, NTextTransformConfig ctx) {
        if (formats == null) {
            formats = NTextStyles.of();
        }
        NTexts txt = NTexts.of(session);
        switch (node.getType()) {
            case PLAIN: {
                NTextPlain p = (NTextPlain) node;
                writeRaw(formats, p.getText(), ctx.isFiltered());
                break;
            }
            case LIST: {
                NTextList s = (NTextList) node;
                for (NText n : s) {
                    writeNode(formats, n, ctx);
                }
                break;
            }
            case STYLED: {
                DefaultNTextStyled s = (DefaultNTextStyled) node;
                NTextStyles styles = s.getStyles();
                NTextStyles format = txt.getTheme().toBasicStyles(styles, session);
                NTextStyles s2 = formats.append(format);
                writeNode(s2, s.getChild(), ctx);
                break;
            }
            case TITLE: {
                DefaultNTextTitle s = (DefaultNTextTitle) node;
                NTextStyles s2 = formats.append(txt.getTheme().toBasicStyles(
                        NTextStyles.of(NTextStyle.title(s.getLevel())), session
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
                    writeNode(s2, sWithTitle, ctx);
                    writeRaw("\n");
                } else {
                    NText sWithTitle = txt.ofList(
                            txt.ofPlain(CoreStringUtils.fillString('#', s.getLevel()) + ") "),
                            s.getChild()
                    );
                    writeNode(s2, sWithTitle, ctx);
                    writeRaw("\n");
                }
                break;
            }
            case COMMAND: {
                DefaultNTextCommand s = (DefaultNTextCommand) node;
                if (term != null) {
                    if (!ctx.isFiltered()) {
                        term.run(s.getCommand(), rawOutput, session);
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
                NTextPlain child = txt.ofPlain(((NTextLink) node).getText());
                writeNode(formats,
                        txt.ofStyled(child,
                                NTextStyles.of(NTextStyle.underlined())
                        ), ctx);
                writeRaw(formats, "see: " + ((NTextLink) node).getText(), ctx.isFiltered());
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
                        ), ctx);
                break;
            }
            case CODE: {
                NTextCode node1 = (NTextCode) node;
                if (ctx.isFiltered()) {
                    writeRaw(formats, node1.getText(), true);
                } else {
                    NText cn = node1.highlight(session);
                    writeNode(formats, cn, ctx);
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
                        term.setStyles(format, rawOutput, session);
                    }
                    try {
                        writeRaw(rawString);
                    } finally {
                        if (term != null) {
                            term.setStyles(null, rawOutput, session);
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
            try {
                DefaultNSystemTerminalBase.TERM.write(b, 0, b.length);
                DefaultNSystemTerminalBase.TERM.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
