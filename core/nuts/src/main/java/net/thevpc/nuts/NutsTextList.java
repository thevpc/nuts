package net.thevpc.nuts;

/**
 * @app.category Format
 */
public interface NutsTextList extends NutsText, Iterable<NutsText> {

    int size();

    NutsText get(int index);

    NutsText simplify();
}
