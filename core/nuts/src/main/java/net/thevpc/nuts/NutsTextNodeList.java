package net.thevpc.nuts;

/**
 * @category Format
 */
public interface NutsTextNodeList extends NutsTextNode, Iterable<NutsTextNode> {

    int size();

    NutsTextNode get(int index);

    NutsTextNode simplify();
}
