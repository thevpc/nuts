package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.text.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;

public abstract class AbstractNTextNodeParser implements NTextParser {
    protected int bufferSize = 4096;
    protected NWorkspace workspace;
    public AbstractNTextNodeParser(NWorkspace workspace) {
        this.workspace = workspace;
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public long parseIncremental(char buf, NTextVisitor visitor) {
        return parseIncremental(new char[]{buf}, visitor);
    }

    @Override
    public long parse(InputStream in, NTextVisitor visitor) {
        return parse(new BufferedReader(new InputStreamReader(in)), visitor);
    }

    public long parse(Reader in, NTextVisitor visitor) {
        int count = 0;
        char[] buffer = new char[bufferSize];
        int r;
        while (true) {
            try {
                if (!((r = in.read(buffer)) > 0)) break;
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
            parseIncremental(buffer, 0, r, visitor);
        }
        parseRemaining(visitor);
        return count;
    }

    @Override
    public NText parse(InputStream in) {
        NTextNodeCollector doc = new NTextNodeCollector(workspace);
        parse(in, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NText parse(Reader in) {
        NTextNodeCollector doc = new NTextNodeCollector(workspace);
        parse(in, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NText parse(File in) {
        return parse(NPath.of(in));
    }

    @Override
    public NText parse(Path in) {
        return parse(NPath.of(in));
    }

    @Override
    public NText parse(URL in) {
        return parse(NPath.of(in));
    }

    @Override
    public NText parse(NInputSource in) {
        NTextNodeCollector doc = new NTextNodeCollector(workspace);
        try (InputStream is = in.getInputStream()) {
            parse(is, doc);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return doc.getRootOrEmpty();
    }

    @Override
    public long parseIncremental(byte[] buf, NTextVisitor visitor) {
        return parseIncremental(buf, 0, buf.length, visitor);
    }

    @Override
    public long parseIncremental(char[] buf, NTextVisitor visitor) {
        return parseIncremental(new String(buf), visitor);
    }

    @Override
    public long parseIncremental(String buf, NTextVisitor visitor) {
        return parseIncremental(buf.getBytes(), visitor);
    }

    @Override
    public NText parseIncremental(byte[] buf) {
        NTextNodeCollector doc = new NTextNodeCollector(workspace);
        parseIncremental(buf, 0, buf.length, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NText parseIncremental(char[] buf) {
        NTextNodeCollector doc = new NTextNodeCollector(workspace);
        parseIncremental(new String(buf), doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NText parseIncremental(String buf) {
        NTextNodeCollector doc = new NTextNodeCollector(workspace);
        parseIncremental(buf.getBytes(), doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NText parseIncremental(char buf) {
        NTextNodeCollector doc = new NTextNodeCollector(workspace);
        parseIncremental(buf, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NText parseIncremental(byte[] buf, int off, int len) {
        NTextNodeCollector doc = new NTextNodeCollector(workspace);
        parseIncremental(buf, off, len, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NText parseIncremental(char[] buf, int off, int len) {
        NTextNodeCollector doc = new NTextNodeCollector(workspace);
        parseIncremental(buf, off, len, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NText parseRemaining() {
        NTextNodeCollector doc = new NTextNodeCollector(workspace);
        parseRemaining(doc);
        return doc.getRootOrNull();
    }

    @Override
    public long parseIncremental(byte[] buf, int off, int len, NTextVisitor visitor) {
        if (len == 0) {
            return 0;
        }
        String raw = new String(buf, off, len);
        char[] c = raw.toCharArray();
        return parseIncremental(c, 0, c.length, visitor);
    }

}
