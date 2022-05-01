package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextParser;
import net.thevpc.nuts.runtime.standalone.text.parser.v1.DefaultNutsTextNodeParser;
import net.thevpc.nuts.runtime.standalone.text.parser.v2.NTFParser2;

public class AbstractNutsTextNodeParserDefaults {
    public static NutsTextParser createDefault(NutsSession session) {
        return new NTFParser2(session);
//        return new DefaultNutsTextNodeParser(session);
    }
}
