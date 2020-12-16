package net.thevpc.nuts.runtime.standalone.format.text.parser;

import net.thevpc.nuts.NutsTextNode;

public interface BlocTextFormatter {
    NutsTextNode toNode(String text);
}
