package net.thevpc.nuts;

/**
 * @category Format
 */
public interface NutsTextCode extends NutsText{
    /**
     * return a parsed instance of this code
     * @param  session session
     * @return return
     */
    NutsText parse(NutsSession session);
}
