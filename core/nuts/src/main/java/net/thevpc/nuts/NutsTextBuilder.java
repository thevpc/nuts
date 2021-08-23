package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

import java.util.Collection;

/**
 * @app.category Format
 */
public interface NutsTextBuilder extends NutsString {
    static NutsTextBuilder of(NutsSession session){
        NutsApiUtils.checkSession(session);
        return session.getWorkspace().text().builder();
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

    String toString();

    int size();

}
