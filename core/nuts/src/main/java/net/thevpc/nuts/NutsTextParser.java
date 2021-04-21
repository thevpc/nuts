package net.thevpc.nuts;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

/**
 * @category Format
 */
public interface NutsTextParser {
//    NutsText parse(String text);

    String escapeText(String text);

    String filterText(String text);

    long parseIncremental(char buf, NutsTextNodeVisitor visitor);

    long parse(InputStream in, NutsTextNodeVisitor visitor);

    long parse(Reader in, NutsTextNodeVisitor visitor);

    NutsText parse(InputStream in);

    NutsText parse(Reader in);

    long parseIncremental(byte[] buf, int off, int len, NutsTextNodeVisitor visitor);

    long parseIncremental(char[] buf, int off, int len, NutsTextNodeVisitor visitor);

    long parseIncremental(byte[] buf, NutsTextNodeVisitor visitor);

    long parseIncremental(char[] buf, NutsTextNodeVisitor visitor);

    long parseIncremental(String buf, NutsTextNodeVisitor visitor);

    long parseRemaining(NutsTextNodeVisitor visitor);

    boolean isIncomplete();

    void reset();

    NutsText parseIncremental(byte[] buf);

    NutsText parseIncremental(char[] buf);

    NutsText parseIncremental(String buf);

    NutsText parseIncremental(char buf);

    NutsText parseIncremental(byte[] buf, int off, int len);

    NutsText parseIncremental(char[] buf, int off, int len);

    NutsText parseRemaining();

    NutsText parseResource(String resourceName, NutsTextFormatLoader loader);

    NutsText parseResource(String resourceName, Reader reader, NutsTextFormatLoader loader);

    NutsTextFormatLoader createLoader(ClassLoader loader);

    NutsTextFormatLoader createLoader(File root);


}
