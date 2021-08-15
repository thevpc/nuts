package net.thevpc.nuts;

/**
 * @app.category Format
 */
public interface NutsTitleNumber {
    NutsTitleNumber next();

    NutsTitleNumber first();

    NutsTitleNumber none();

    boolean isNone();

    String toString();
}
