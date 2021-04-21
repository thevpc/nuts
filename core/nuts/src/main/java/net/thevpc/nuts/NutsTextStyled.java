package net.thevpc.nuts;

/**
 * @category Format
 */
public interface NutsTextStyled extends NutsText {
    NutsText getChild();
    NutsTextNodeStyles getStyles();
}
