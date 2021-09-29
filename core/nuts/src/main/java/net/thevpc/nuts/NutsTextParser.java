package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

/**
 * @app.category Format
 */
public interface NutsTextParser {
    static NutsTextParser of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.text().parser();
    }

    String escapeText(String text);

    String filterText(String text);

    long parseIncremental(char buf, NutsTextVisitor visitor);

    long parse(InputStream in, NutsTextVisitor visitor);

    long parse(Reader in, NutsTextVisitor visitor);

    NutsText parse(InputStream in);

    NutsText parse(Reader in);

    long parseIncremental(byte[] buf, int off, int len, NutsTextVisitor visitor);

    long parseIncremental(char[] buf, int off, int len, NutsTextVisitor visitor);

    long parseIncremental(byte[] buf, NutsTextVisitor visitor);

    long parseIncremental(char[] buf, NutsTextVisitor visitor);

    long parseIncremental(String buf, NutsTextVisitor visitor);

    long parseRemaining(NutsTextVisitor visitor);

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
