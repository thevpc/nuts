package net.thevpc.nuts.runtime.standalone.format.tson.bundled.impl.parser;

import net.thevpc.nuts.runtime.standalone.format.tson.bundled.TsonLexicalAnalyzer;

public interface TsonLexicalAnalyzerExt extends TsonLexicalAnalyzer {
    String currentStreamPart();

    String currentText();

}
