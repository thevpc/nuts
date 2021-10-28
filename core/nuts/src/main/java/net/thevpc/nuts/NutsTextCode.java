package net.thevpc.nuts;

/**
 * @app.category Format
 */
public interface NutsTextCode extends NutsText {
    /**
     * return a parsed instance of this code
     *
     * @param session session
     * @return return
     */
    NutsText highlight(NutsSession session);
}
