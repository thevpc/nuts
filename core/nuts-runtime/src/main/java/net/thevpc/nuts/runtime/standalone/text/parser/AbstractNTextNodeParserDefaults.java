package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.text.NTextParser;
import net.thevpc.nuts.runtime.standalone.text.parser.v2.NTFParser2;

public class AbstractNTextNodeParserDefaults {
    public static NTextParser createDefault(NWorkspace workspace) {
        return new NTFParser2(workspace);
    }
}
