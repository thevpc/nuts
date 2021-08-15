package net.thevpc.nuts;

/**
 * @app.category Format
 */
public interface NutsTextTitle extends NutsText {

    NutsText getChild();

    int getLevel();
}
