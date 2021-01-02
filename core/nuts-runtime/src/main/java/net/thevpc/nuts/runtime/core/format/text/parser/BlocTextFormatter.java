package net.thevpc.nuts.runtime.core.format.text.parser;

import net.thevpc.nuts.NutsTextNode;

public interface BlocTextFormatter {
    NutsTextNode toNode(String text);
}
