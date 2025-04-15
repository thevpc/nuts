package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.io.InputStream;
import java.io.Reader;

public interface TsonLexicalAnalyzer {
    int nextToken();

    Reader currentReader();

    InputStream currentInputStream();

    String currentImage();

    String currentString();

    char currentChar();
}
