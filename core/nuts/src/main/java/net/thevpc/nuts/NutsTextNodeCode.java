package net.thevpc.nuts;

/**
 * @category Format
 */
public interface NutsTextNodeCode extends NutsTextNode{
    /**
     * return a parsed instance of this code
     * @param  session session
     * @return return
     */
    NutsTextNode parse(NutsSession session);
}
