package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.text.NTextParser;
import net.thevpc.nuts.runtime.standalone.text.parser.v2.NTFParser2;

public class AbstractNTextNodeParserDefaults {
    public static NTextParser createDefault() {
        return new NTFParser2();
    }
}
