package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

public class SettingsRepoUtils {
    public static void showRepo(NSession session, NRepository repository, String prefix) {
        boolean active = repository.isEnabled(session);
        boolean enabled = repository.config().isEnabled();
        String disabledString = active ? "" : enabled ? "<ENABLED>" : " <DISABLED>";
        NPrintStream out = session.out();
        out.print(prefix);
        NTexts factory = NTexts.of(session);
        if (enabled) {
            out.print(factory.ofStyled(repository.getName() + disabledString, NTextStyle.primary2()));
        } else {
            out.print("```error " + repository.getName() + disabledString + "```");
        }
        out.print(" : " + repository.getRepositoryType() + " " + repository.config().getLocation());
        out.println();

    }

    public static void showRepoTree(NSession session, NRepository repository, String prefix) {
        showRepo(session, repository, prefix);
        String prefix1 = prefix + "  ";
        if (repository.config().isSupportedMirroring()) {
            for (NRepository c : repository.config().getMirrors()) {
                showRepoTree(session, c, prefix1);
            }
        }
    }
}
