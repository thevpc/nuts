package net.thevpc.nuts.runtime.format.text.parser;

import net.thevpc.nuts.NutsTextNode;

public interface BlocTextFormatter {
    NutsTextNode toNode(String text);
}
