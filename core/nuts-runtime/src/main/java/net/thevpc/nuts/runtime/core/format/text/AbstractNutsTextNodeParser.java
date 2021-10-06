package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeResourceParserHelper;

import java.io.*;

public abstract class AbstractNutsTextNodeParser implements NutsTextParser {
    protected int bufferSize=4096;
    protected NutsSession session;
    protected DefaultNutsTextNodeResourceParserHelper rp;

    public AbstractNutsTextNodeParser(NutsSession session) {
        this.session = session;
        rp=new DefaultNutsTextNodeResourceParserHelper(this, session);
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
        return parse(new BufferedReader(new InputStreamReader(in)),visitor);
    }

    public long parse(Reader in, NutsTextVisitor visitor) {
        int count = 0;
        char[] buffer = new char[bufferSize];
        int r;
        while (true) {
            try {
                if (!((r = in.read(buffer)) > 0)) break;
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            parseIncremental(buffer,0,r,visitor);
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
        parseIncremental(buf,off,len, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsText parseIncremental(char[] buf, int off, int len) {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(session);
        parseIncremental(buf,off,len, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public NutsText parseRemaining() {
        NutsTextNodeCollector doc = new NutsTextNodeCollector(session);
        parseRemaining(doc);
        return doc.getRootOrNull();
    }

    @Override
    public NutsText parseResource(String resourceName, NutsTextFormatLoader loader) {
        return rp.parseResource(resourceName, loader);
    }

    @Override
    public NutsText parseResource(String resourceName, Reader reader, NutsTextFormatLoader loader) {
        return rp.parseResource(resourceName, reader, loader);
    }

    @Override
    public NutsTextFormatLoader createLoader(ClassLoader loader) {
        return rp.createClassPathLoader(loader);
    }

    @Override
    public NutsTextFormatLoader createLoader(File root) {
        return rp.createFileLoader(root);
    }
}
