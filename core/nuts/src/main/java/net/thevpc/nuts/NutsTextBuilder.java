package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

import java.util.Collection;
import java.util.List;

/**
 * @app.category Format
 */
public interface NutsTextBuilder extends NutsString {
    static NutsTextBuilder of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return NutsTexts.of(session).builder();
    }

    NutsTextStyleGenerator getStyleGenerator();

    NutsTextBuilder setStyleGenerator(NutsTextStyleGenerator styleGenerator);

    NutsTextWriteConfiguration getConfiguration();

    NutsTextBuilder setConfiguration(NutsTextWriteConfiguration writeConfiguration);

    NutsTextBuilder appendCommand(NutsTerminalCommand command);

    NutsTextBuilder appendCode(String lang, String text);

    NutsTextBuilder appendHash(Object text);

    NutsTextBuilder appendRandom(Object text);

    NutsTextBuilder appendHash(Object text, Object hash);

    NutsTextBuilder append(Object text, NutsTextStyle style);

    NutsTextBuilder append(Object text, NutsTextStyles styles);

    NutsTextBuilder append(Object node);

    NutsTextBuilder append(NutsText node);

    NutsTextBuilder appendJoined(Object separator, Collection<?> others);

    NutsTextBuilder appendAll(Collection<?> others);

    NutsText build();

    NutsTextParser parser();

    List<NutsText> getChildren();

    NutsText subChildren(int from, int to);

    NutsText substring(int from, int to);

    NutsTextBuilder insert(int at, NutsText... newTexts);
    NutsTextBuilder replace(int from, int to, NutsText... newTexts);

    NutsTextBuilder replaceChildren(int from, int to, NutsText... newTexts);

    String toString();

    int size();

    NutsText get(int index);

    Iterable<NutsText> items();

    /**
     * replaces the builder content with the simplest text in the form of suite of plain or styled text elements.
     * the possible returned types are plain text (NutsTextPlain) if there is no styling or
     * styled plain (NutsTextStyled) if any style is detected.
     *
     * Compound nodes are flattened so than the returned instance is one of the following:
     * - a single line plain text (plain text than either does not include any newline or is a single newline)
     * - a styled plain (style nodes that have a single line plain text child)
     * @return {@code this} instance with flattened children
     */
    NutsTextBuilder flatten() ;

    NutsTextBuilder removeAt(int index);

}
