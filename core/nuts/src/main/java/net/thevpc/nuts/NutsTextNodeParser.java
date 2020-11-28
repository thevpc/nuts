package net.thevpc.nuts;

import java.io.InputStream;
import java.io.Reader;

public interface NutsTextNodeParser {
//    NutsTextNode parse(String text);

    String escapeText(String text);

    String filterText(String text);

    long parseIncremental(char buf, NutsTextNodeVisitor visitor);

    long parse(InputStream in, NutsTextNodeVisitor visitor);

    long parse(Reader in, NutsTextNodeVisitor visitor);

    NutsTextNode parse(InputStream in);

    NutsTextNode parse(Reader in);

    long parseIncremental(byte[] buf, int off, int len, NutsTextNodeVisitor visitor);

    long parseIncremental(char[] buf, int off, int len, NutsTextNodeVisitor visitor);

    long parseIncremental(byte[] buf, NutsTextNodeVisitor visitor);

    long parseIncremental(char[] buf, NutsTextNodeVisitor visitor);

    long parseIncremental(String buf, NutsTextNodeVisitor visitor);

    long parseRemaining(NutsTextNodeVisitor visitor);

    boolean isIncomplete();

    NutsTextNode parseIncremental(byte[] buf);

    NutsTextNode parseIncremental(char[] buf);

    NutsTextNode parseIncremental(String buf);

    NutsTextNode parseIncremental(char buf);

    NutsTextNode parseIncremental(byte[] buf, int off, int len);

    NutsTextNode parseIncremental(char[] buf, int off, int len);

    NutsTextNode parseRemaining();
}
