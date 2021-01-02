package net.thevpc.nuts;

/**
 * @category Format
 */
public interface NutsTitleNumber {
    NutsTitleNumber next();

    NutsTitleNumber first();

    NutsTitleNumber none();

    boolean isNone();

    String toString();
}
