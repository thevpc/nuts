package net.thevpc.nuts;

/**
 * @category Format
 */
public interface NutsTextList extends NutsText, Iterable<NutsText> {

    int size();

    NutsText get(int index);

    NutsText simplify();
}
