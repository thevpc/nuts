package net.thevpc.nuts.runtime.standalone.tson.impl.parser;

import net.thevpc.nuts.runtime.standalone.tson.TsonLexicalAnalyzer;

public interface TsonLexicalAnalyzerExt extends TsonLexicalAnalyzer {
    String currentStreamPart();

    String currentText();

}
