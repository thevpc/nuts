package net.thevpc.nuts;

/**
 * @app.category Format
 */
public enum NutsTextType implements NutsEnum{
    PLAIN,
    LIST,
    TITLE,
    COMMAND,
    LINK,
    STYLED,
    ANCHOR,
    CODE;
    private String id;

    NutsTextType() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    @Override
    public String id() {
        return id;
    }

}
