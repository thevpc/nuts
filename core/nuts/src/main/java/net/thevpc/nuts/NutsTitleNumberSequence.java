package net.thevpc.nuts;

/**
 * @category Format
 */
public interface NutsTitleNumberSequence {
    NutsTitleNumber[] getPattern();

    NutsTitleNumberSequence newLevel(int level);

    NutsTitleNumber getNumber(int index);

    int size();

    NutsTitleNumber[] getValue();

    String getSeparator(int index);

    String toString();
}
