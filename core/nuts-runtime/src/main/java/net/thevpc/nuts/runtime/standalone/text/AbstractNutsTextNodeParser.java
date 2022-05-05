package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.text.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;

public abstract class AbstractNutsTextNodeParser implements NutsTextParser {
    protected int bufferSize = 4096;
    protected NutsSession session;
    public AbstractNutsTextNodeParser(NutsSession session) {
        this.session = session;
    }

    public NutsWorkspace getWorkspace() {
        return session.getWorkspace();
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public long parseIncremental(char buf, NutsTextVisitor visitor) {
        return parseIncremental(new char[]{buf}, visitor);
    }

    @Override
    public long parse(InputStream in, NutsTextVisitor visitor) {
        return parse(new BufferedReader(new InputStreamReader(in)), visitor);
    }

    public long parse(Reader in, NutsTextVisitor visitor) {
        int count = 0;
        char[] buffer = new char[bufferSize];
        int r;
        while (true) {
            try {
                if (!((r = in.read(buffer)) > 0)) break;
            } catch (IOException ex) {
                throw new NutsIOException(session, ex);
            }
            parseIncremental(buffer, 0, r, visitor);
        }
        parseRemaining(visitor);
        return count;
    }

    @Override
    public NutsText parse(InputStream in) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(session);
        parse(in, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsText parse(Reader in) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(session);
        parse(in, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsText parse(File in) {
        return parse(NutsPath.of(in, getSession()));
    }

    @Override
    public NutsText parse(Path in) {
        return parse(NutsPath.of(in, getSession()));
    }

    @Override
    public NutsText parse(URL in) {
        return parse(NutsPath.of(in, getSession()));
    }

    @Override
    public NutsText parse(NutsInputSource in) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(session);
        try (InputStream is = in.getInputStream()) {
            parse(is, doc);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return doc.getRootOrEmpty();
    }

    @Override
    public long parseIncremental(byte[] buf, NutsTextVisitor visitor) {
        return parseIncremental(buf, 0, buf.length, visitor);
    }

    @Override
    public long parseIncremental(char[] buf, NutsTextVisitor visitor) {
        return parseIncremental(new String(buf), visitor);
    }

    @Override
    public long parseIncremental(String buf, NutsTextVisitor visitor) {
        return parseIncremental(buf.getBytes(), visitor);
    }

    @Override
    public NutsText parseIncremental(byte[] buf) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(session);
        parseIncremental(buf, 0, buf.length, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsText parseIncremental(char[] buf) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(session);
        parseIncremental(new String(buf), doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsText parseIncremental(String buf) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(session);
        parseIncremental(buf.getBytes(), doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsText parseIncremental(char buf) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(session);
        parseIncremental(buf, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsText parseIncremental(byte[] buf, int off, int len) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(session);
        parseIncremental(buf, off, len, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsText parseIncremental(char[] buf, int off, int len) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(session);
        parseIncremental(buf, off, len, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsText parseRemaining() {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(session);
        parseRemaining(doc);
        return doc.getRootOrNull();
    }

    @Override
    public long parseIncremental(byte[] buf, int off, int len, NutsTextVisitor visitor) {
        if (len == 0) {
            return 0;
        }
        String raw = new String(buf, off, len);
        char[] c = raw.toCharArray();
        return parseIncremental(c, 0, c.length, visitor);
    }

}
