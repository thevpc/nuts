package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.parser.*;
import net.thevpc.nuts.runtime.core.format.text.renderer.AnsiUnixTermPrintRenderer;
import net.thevpc.nuts.runtime.core.format.text.renderer.StyleRenderer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

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
        writeNode(new AnsiEscapeCommand[0], node, ctx);
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


    private void writeNode(AnsiEscapeCommand[] formats, NutsTextNode node, NutsTextNodeWriteConfiguration ctx) {
        if (formats == null) {
            formats = new AnsiEscapeCommand[0];
        }
        switch (node.getType()) {
            case PLAIN: {
                NutsTextNodePlain p = (NutsTextNodePlain) node;
                writeRaw(AnsiEscapeCommands.list(formats), p.getText(), ctx.isFiltered());
//        }else if (node instanceof TextNodeEscaped) {
//            TextNodeEscaped p = (TextNodeEscaped) node;
//            writeRaw(AnsiEscapeCommands.list(formats), p.getValue(), ctx.isFiltered());
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
                NutsTextNodeFactory factory0 = ws.formats().text().factory();
                NutsTextNodeStyle style = s.getStyle();
                NutsTextNodeStyle[] format=factory0.toBasicStyles(style);
                AnsiEscapeCommand[] s2 = _appendFormats(formats, format);
                writeNode(s2, s.getChild(), ctx);
                break;
            }
            case TITLE: {
                DefaultNutsTextNodeTitle s = (DefaultNutsTextNodeTitle) node;
                DefaultNutsTextNodeFactory factory0 = (DefaultNutsTextNodeFactory) ws.formats().text().factory();
                AnsiEscapeCommand[] s2 = _appendFormats(formats, factory0.toBasicStyles(NutsTextNodeStyle.title(s.getLevel())));
                if (ctx.isTitleNumberEnabled()) {
                    NutsTitleNumberSequence seq = ctx.getTitleNumberSequence();
                    if (seq == null) {
                        seq = ws.formats().text().createTitleNumberSequence();
                        ctx.setTitleNumberSequence(seq);
                    }
                    NutsTitleNumberSequence a = seq.newLevel(s.getLevel());
                    NutsTextNode sWithTitle = factory0.list(
                            ws.formats().text().factory().plain(a.toString() + " "),
                            s.getChild()
                    );
                    writeNode(s2,sWithTitle, ctx);
                }else {
                    writeNode(s2, s.getChild(), ctx);
                }
//        } else if (node instanceof TextNodeUnStyled) {
//            TextNodeUnStyled s = (TextNodeUnStyled) node;
//            writeNode(formats, new NutsTextNodePlain(s.getStart()), ctx);
//            writeNode(formats, s.getChild(), ctx);
//            writeNode(formats, new NutsTextNodePlain(s.getEnd()), ctx);
                break;
            }
            case COMMAND: {
                DefaultNutsTextNodeCommand s = (DefaultNutsTextNodeCommand) node;
                AnsiEscapeCommand yy = DefaultNutsTextNodeCommand.parseAnsiEscapeCommand(s.getKind(),ws);
                AnsiEscapeCommand[] s2 = _appendFormats(formats, yy);
                writeRaw(AnsiEscapeCommands.list(s2), "", ctx.isFiltered());
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
                                ws.formats().text().factory().plain(
                                        ((NutsTextNodeLink) node).getValue()
                                ),
                                NutsTextNodeStyle.underlined(),
                                true
                        ),
                        ctx
                );
                writeRaw(AnsiEscapeCommands.list(formats), "see: " + ((NutsTextNodeLink) node).getValue(), ctx.isFiltered());
                break;
            }
            case CODE: {
                DefaultNutsTextNodeCode node1 = (DefaultNutsTextNodeCode) node;
                NutsTextNode cn = ws.formats().text().factory().parseBloc(node1.getKind(), node1.getText());
                writeNode(formats, cn, ctx);
                break;
            }
            default: {
                throw new UnsupportedOperationException("unsupported NutsTextNode type " + node.getClass().getSimpleName());
            }
        }
    }



    protected void writeRaw(AnsiEscapeCommand format, String rawString, boolean filterFormat) {
        if (!filterFormat && format != null) {
            StyleRenderer f = null;
            f = renderer.createStyleRenderer(simplifyFormat(format), ws);
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

    protected AnsiEscapeCommand simplifyFormat(AnsiEscapeCommand f) {
        if (f instanceof AnsiEscapeCommandList) {
            AnsiEscapeCommand[] o = ((AnsiEscapeCommandList) f).getChildren();
            List<AnsiEscapeCommand> ok = new ArrayList<>();
            if (o != null) {
                for (AnsiEscapeCommand v : o) {
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
            return AnsiEscapeCommands.list(ok.toArray(new AnsiEscapeCommand[0]));
        }
        return f;
    }

    private AnsiEscapeCommand[] _appendFormats(AnsiEscapeCommand[] old, AnsiEscapeCommand v) {
        List<AnsiEscapeCommand> list = new ArrayList<AnsiEscapeCommand>((old == null ? 0 : old.length) + 1);
        if (old != null) {
            list.addAll(Arrays.asList(old));
        }
        list.add(v);
        return list.toArray(new AnsiEscapeCommand[0]);
    }

    private AnsiEscapeCommand[] _appendFormats(AnsiEscapeCommand[] old, AnsiEscapeCommand... v) {
        List<AnsiEscapeCommand> list = new ArrayList<AnsiEscapeCommand>((old == null ? 0 : old.length) + 1);
        if (old != null) {
            list.addAll(Arrays.asList(old));
        }
        for (AnsiEscapeCommand ansiEscapeCommand : v) {
            if(ansiEscapeCommand !=null) {
                list.add(ansiEscapeCommand);
            }
        }
        return list.toArray(new AnsiEscapeCommand[0]);
    }
    private AnsiEscapeCommand[] _appendFormats(AnsiEscapeCommand[] old, NutsTextNodeStyle ... v) {
        List<AnsiEscapeCommand> list = new ArrayList<AnsiEscapeCommand>((old == null ? 0 : old.length) + 1);
        if (old != null) {
            list.addAll(Arrays.asList(old));
        }
        for (NutsTextNodeStyle textFormat : v) {
            if(textFormat!=null) {
                list.add(AnsiEscapeCommandFromNodeStyle.of(textFormat));
            }
        }
        return list.toArray(new AnsiEscapeCommand[0]);
    }

    public final void writeLater(byte[] later){
        ws.log().of(NutsTextNodeWriterRenderer.class)
                .with()
                .session(ws.createSession())
                .level(Level.FINEST)
                .verb(NutsLogVerb.DEBUG)
                .log("store Later on "+System.identityHashCode(this));
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
                ws.log().of(NutsTextNodeWriterRenderer.class)
                        .with()
                        .session(ws.createSession())
                        .level(Level.FINEST)
                        .verb(NutsLogVerb.DEBUG)
                        .log("process Later on "+System.identityHashCode(this));
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
