package net.vpc.app.nuts.runtime.util.fprint;

import net.vpc.app.nuts.runtime.util.fprint.parser.*;
import net.vpc.app.nuts.runtime.util.fprint.renderer.AnsiUnixTermPrintRenderer;
import net.vpc.app.nuts.runtime.util.fprint.renderer.StyleRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FormatNodeHelper {
    private byte[] buffer = new byte[1024];
    private int bufferSize = 0;
    private boolean enableBuffering = false;
    private FormattedPrintStreamParser parser=new FormattedPrintStreamNodePartialParser();
    private FormattedPrintStreamRenderer renderer=AnsiUnixTermPrintRenderer.ANSI_RENDERER;
    private RawOutputStream rawer;
    private RenderedRawStream renderedRawStream =new RenderedRawStream() {
        @Override
        public void writeRaw(byte[] buf, int off, int len)throws IOException {
            FormatNodeHelper.this.writeRaw(new String(buf,off,len));
        }

        @Override
        public void writeLater(byte[] buf)throws IOException {
            FormatNodeHelper.this.later(buf);
        }
    };
    private boolean formatEnabled = true;
    private byte[] later=null;

    public FormatNodeHelper() {

    }
    public FormatNodeHelper(FormattedPrintStreamRenderer renderer) {
        this.renderer = renderer;
    }

    public FormatNodeHelper setParser(FormattedPrintStreamParser parser) {
        this.parser = parser == null ? new FormattedPrintStreamNodePartialParser() : parser;
        return this;
    }

    public FormattedPrintStreamRenderer getRenderer() {
        return renderer;
    }

    public FormatNodeHelper setRenderer(FormattedPrintStreamRenderer renderer) {
        this.renderer = renderer == null ? AnsiUnixTermPrintRenderer.ANSI_RENDERER : renderer;
        return this;
    }

    public FormattedPrintStreamParser getParser() {
        return parser;
    }

    public RawOutputStream getRawer() {
        return rawer;
    }

    public void setRawer(RawOutputStream rawer) {
        this.rawer = rawer;
    }

    //    @Override
//    public void println(String text) {
//        print(text);
//        println();
//    }

   public interface RawOutputStream {
        void writeRaw(byte[] buf, int off, int len)  throws IOException;

       void flushRaw() throws IOException;
   }

    public boolean isFormatEnabled() {
        return formatEnabled;
    }

    public void setFormatEnabled(boolean formatEnabled) {
        this.formatEnabled = formatEnabled;
    }


    protected void writeRaw(TextFormat format, String rawString) throws IOException{
        if (isFormatEnabled() && format != null) {
            StyleRenderer f=null;
            f = renderer.createStyleRenderer(simplifyFormat(format));
            try {
                f.startFormat(renderedRawStream);
                if(rawString.length()>0) {
                    writeRaw(rawString);
                }
            } finally {
                f.endFormat(renderedRawStream);
            }
        } else {
            if(rawString.length()>0) {
                writeRaw(rawString);
            }
        }
    }

    public boolean consumeNodes(boolean greedy) throws IOException{
        boolean some=false;
        TextNode n = null;
        while ((n = parser.consumeNode()) != null) {
            print(n);
            some=true;
        }
        if (greedy) {
            if(parser.forceEnding()) {
                while ((n = parser.consumeNode()) != null) {
                    print(n);
                    some = true;
                }
            }
        }
        return some;
    }

    public void print(TextNode node) throws IOException{
        if (node == null) {
            node = TextNodePlain.NULL;
        }
        print(new TextFormat[0], node);
    }
    private void print(TextFormat[] formats, TextNode node) throws IOException{
        if (formats == null) {
            formats = new TextFormat[0];
        }
        if (node instanceof TextNodePlain) {
            TextNodePlain p = (TextNodePlain) node;
            writeRaw(TextFormats.list(formats), p.getValue());
        } else if (node instanceof TextNodeList) {
            TextNodeList s = (TextNodeList) node;
            for (TextNode n : s) {
                print(formats, n);
            }
        } else if (node instanceof TextNodeStyled) {
            TextNodeStyled s = (TextNodeStyled) node;
            TextFormat[] s2 = _appendFormats(formats, s.getStyle());
            print(s2, s.getChild());
        } else if (node instanceof TextNodeCommand) {
            TextNodeCommand s = (TextNodeCommand) node;
            TextFormat[] s2 = _appendFormats(formats, s.getStyle());
            writeRaw(TextFormats.list(s2), "");
        } else {
            writeRaw(TextFormats.list(formats), String.valueOf(node));
        }
    }

    protected TextFormat simplifyFormat(TextFormat f) {
        if (f instanceof TextFormatList) {
            TextFormatList l = (TextFormatList) f;
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

    public void processByte(int oneByte) throws IOException{
        processBytes(new byte[]{(byte) oneByte},0,1 );
    }

    public void processBytes(byte[] buf, int off, int len) throws IOException{
        if (!isFormatEnabled()) {
            rawer.writeRaw(buf, off, len);
            return;
        }
        if (len == 0) {
            //do nothing!!!
        } else {
            String raw = new String(buf, off, len);
            try {
                parser.take(raw);
                consumeNodes(false);
            } catch (Exception ex) {
                ex.printStackTrace();
                //
            }
        }
    }

    public final void later(byte[] later) throws IOException{
        this.later=later;
    }

    public final void flushLater() throws IOException{
        byte[] b = later;
        if(b!=null) {
            later=null;
            if (enableBuffering) {
                if (b.length + bufferSize < buffer.length) {
                    System.arraycopy(b, 0, buffer, bufferSize, b.length);
                    bufferSize += b.length;
                } else {
                    flushBuffer();
                    if (b.length >= buffer.length) {
                        rawer.writeRaw(b, 0, b.length);
                    } else {
                        System.arraycopy(b, 0, buffer, bufferSize, b.length);
                        bufferSize += b.length;
                    }
                }
            } else {
                rawer.writeRaw(b, 0, b.length);
                rawer.flushRaw();
            }
            //flush();
        }
    }


    public final void writeRaw(String rawString) throws IOException{
        flushLater();
        byte[] b = rawString.getBytes();
        if (enableBuffering) {
            if (b.length + bufferSize < buffer.length) {
                System.arraycopy(b, 0, buffer, bufferSize, b.length);
                bufferSize += b.length;
            } else {
                flushBuffer();
                if (b.length >= buffer.length) {
                    rawer.writeRaw(b, 0, b.length);
                } else {
                    System.arraycopy(b, 0, buffer, bufferSize, b.length);
                    bufferSize += b.length;
                }
            }
        } else {
            rawer.writeRaw(b, 0, b.length);
        }
    }

    private final boolean flushBuffer() throws IOException{
        if (bufferSize > 0) {
            rawer.writeRaw(buffer, 0, bufferSize);
            bufferSize = 0;
            return true;
        }
        return false;
    }

    public void reset() throws IOException{
        boolean some=false;
        some|=flushBuffer();
        try {
            some|=consumeNodes(true);
        } catch (Exception ex) {
            //
        }
//        if(!some) {
//            flushLater();
//        }
        flushBuffer();
    }
    public void flush() throws IOException{
        //flushLater();
        boolean some=false;
        some|=flushBuffer();
        try {
            some|=consumeNodes(false);
        } catch (Exception ex) {
            //
        }
//        if(!some) {
//            flushLater();
//        }
        flushBuffer();
    }
}
