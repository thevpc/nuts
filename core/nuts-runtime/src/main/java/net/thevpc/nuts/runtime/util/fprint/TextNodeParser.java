package net.thevpc.nuts.runtime.util.fprint;

import net.thevpc.nuts.runtime.util.fprint.parser.TextNode;

import java.io.InputStream;
import java.io.Reader;

public interface TextNodeParser {
//    TextNode parse(String text);

    String escapeText(String text);

    String filterText(String text);

    long parseIncremental(char buf, TextNodeVisitor visitor);

    long parse(InputStream in, TextNodeVisitor visitor);

    long parse(Reader in, TextNodeVisitor visitor);

    TextNode parse(InputStream in);

    TextNode parse(Reader in);

    long parseIncremental(byte[] buf, int off, int len, TextNodeVisitor visitor);

    long parseIncremental(char[] buf, int off, int len, TextNodeVisitor visitor);

    long parseIncremental(byte[] buf, TextNodeVisitor visitor);

    long parseIncremental(char[] buf, TextNodeVisitor visitor);

    long parseIncremental(String buf, TextNodeVisitor visitor);

    long parseRemaining(TextNodeVisitor visitor);

    boolean isIncomplete();

    TextNode parseIncremental(byte[] buf);

    TextNode parseIncremental(char[] buf);

    TextNode parseIncremental(String buf);

    TextNode parseIncremental(char buf);

    TextNode parseIncremental(byte[] buf, int off, int len);

    TextNode parseIncremental(char[] buf, int off, int len);

    TextNode parseRemaining();
}
