package net.thevpc.nuts.runtime.standalone.xtra.expr.template;

import net.thevpc.nuts.expr.NExprCompiledTemplate;
import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprTemplate;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.io.*;

public class NExprDeclarationsTemplateImpl implements NExprTemplate {
    private String start;
    private String stop;
    private String escape;
    protected TagStreamProcessor processor;
    protected NExprDeclarations declarations;

    public NExprDeclarationsTemplateImpl(NExprDeclarations declarations) {
        this.declarations = declarations;
        this.withMoustacheStyle();
    }

    @Override
    public NExprTemplate withJspStyle() {
        return withBoundaries("<%", "%>");
    }

    @Override
    public NExprTemplate withMoustacheStyle() {
        return withBoundaries("{{", "}}");
    }

    @Override
    public NExprTemplate withBashStyle() {
        return withBoundaries("${", "}");
    }


    @Override
    public NExprTemplate withBoundaries(String start, String stop) {
        return configureBoundaries(start, stop, null);
    }

    public NExprTemplate configureBoundaries(String start, String stop, String escape) {
        if (escape == null) {
            escape = "\\" + stop;
        }
        this.start = NAssert.requireNonBlank(NStringUtils.trim(start), "start");
        this.stop = NAssert.requireNonBlank(NStringUtils.trim(stop), "stop");
        this.escape = NStringUtils.trimToNull(escape);
        this.processor = null;
        return this;
    }


    private TagStreamProcessor processorRequired() {
        TagStreamProcessor p = processor();
        if (p == null) {
            throw new IllegalArgumentException("invalid processor " + start + " ..." + stop);
        }
        return p;
    }

    private TagStreamProcessor processor() {
        if (processor == null) {
            if (NBlankable.isBlank(start)) {
                return null;
            }
            if (NBlankable.isBlank(stop)) {
                return null;
            }
            return processor = new TagStreamProcessor(start, stop, escape);
        }
        return processor;
    }

    @Override
    public NExprTemplate process(InputStream inputStream, OutputStream outputStream) {
        processorRequired().processStream(inputStream, outputStream, declarations);
        return this;
    }

    @Override
    public NExprTemplate process(Reader inputStream, Writer outputStream) {
        processorRequired().processStream(inputStream, outputStream, declarations);
        return this;
    }

    @Override
    public NExprTemplate process(Reader inputStream, PrintStream outputStream) {
        processorRequired().processStream(inputStream, new OutputStreamWriter(outputStream), declarations);
        return this;
    }

    @Override
    public NExprCompiledTemplate compile(InputStream inputStream) {
        return processorRequired().compile(inputStream, declarations);
    }

    @Override
    public NExprCompiledTemplate compile(Reader inputStream) {
        return processorRequired().compile(inputStream, declarations);
    }

    @Override
    public NExprCompiledTemplate compile(String string) {
        return compile(new StringReader(string == null ? "" : string));
    }

    @Override
    public NExprCompiledTemplate compile(NInputSource source) {
        try (BufferedReader r = source.getBufferedReader()) {
            return compile(source.getBufferedReader());
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    @Override
    public String processString(String string) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        process(new StringReader(string == null ? "" : string), writer);
        try {
            writer.flush();
        } catch (IOException e) {
            throw new NIOException(e);
        }
        return out.toString();
    }
}
