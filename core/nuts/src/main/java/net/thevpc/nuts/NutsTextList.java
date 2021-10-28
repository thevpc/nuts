package net.thevpc.nuts;

import java.util.List;

/**
 * @app.category Format
 */
public interface NutsTextList extends NutsText, Iterable<NutsText> {

    int size();

    NutsText get(int index);
    List<NutsText> getChildren();

    NutsText simplify();
}
