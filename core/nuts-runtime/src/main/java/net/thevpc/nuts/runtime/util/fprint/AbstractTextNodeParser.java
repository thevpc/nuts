package net.thevpc.nuts.runtime.util.fprint;

import net.thevpc.nuts.runtime.util.fprint.parser.TextNode;

import java.io.*;

public abstract class AbstractTextNodeParser implements TextNodeParser {
    protected int bufferSize=4096;
    @Override
    public long parseIncremental(char buf, TextNodeVisitor visitor) {
        return parseIncremental(new char[]{buf}, visitor);
    }

    @Override
    public long parse(InputStream in, TextNodeVisitor visitor) {
        return parse(new BufferedReader(new InputStreamReader(in)),visitor);
    }
    public long parse(Reader in, TextNodeVisitor visitor) {
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
    public TextNode parse(InputStream in) {
        TextNodeCollector doc = new TextNodeCollector();
        parse(in, doc);
        return doc.getRootOrNull();
    }

    @Override
    public TextNode parse(Reader in) {
        TextNodeCollector doc = new TextNodeCollector();
        parse(in, doc);
        return doc.getRootOrNull();
    }

    @Override
    public long parseIncremental(byte[] buf, TextNodeVisitor visitor) {
        return parseIncremental(buf, 0, buf.length, visitor);
    }

    @Override
    public long parseIncremental(char[] buf, TextNodeVisitor visitor) {
        return parseIncremental(new String(buf), visitor);
    }

    @Override
    public long parseIncremental(String buf, TextNodeVisitor visitor) {
        return parseIncremental(buf.getBytes(), visitor);
    }

    @Override
    public TextNode parseIncremental(byte[] buf) {
        TextNodeCollector doc = new TextNodeCollector();
        parseIncremental(buf, 0, buf.length, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public TextNode parseIncremental(char[] buf) {
        TextNodeCollector doc = new TextNodeCollector();
        parseIncremental(new String(buf), doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public TextNode parseIncremental(String buf) {
        TextNodeCollector doc = new TextNodeCollector();
        parseIncremental(buf.getBytes(), doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public TextNode parseIncremental(char buf) {
        TextNodeCollector doc = new TextNodeCollector();
        parseIncremental(buf, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public TextNode parseIncremental(byte[] buf, int off, int len) {
        TextNodeCollector doc = new TextNodeCollector();
        parseIncremental(buf,off,len, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public TextNode parseIncremental(char[] buf, int off, int len) {
        TextNodeCollector doc = new TextNodeCollector();
        parseIncremental(buf,off,len, doc);
        return doc.getRootOrEmpty();
    }

    @Override
    public TextNode parseRemaining() {
        TextNodeCollector doc = new TextNodeCollector();
        parseRemaining(doc);
        return doc.getRootOrNull();
    }
}
