package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

public class SettingsRepoUtils {
    public static void showRepo(NSession session, NRepository repository, String prefix) {
        boolean active = repository.isEnabled();
        boolean enabled = repository.config().isEnabled();
        String disabledString = active ? "" : enabled ? "<ENABLED>" : " <DISABLED>";
        NPrintStream out = session.out();
        out.print(prefix);
        NTexts factory = NTexts.of();
        if (enabled) {
            out.print(factory.ofStyled(repository.name() + disabledString, NTextStyle.primary2()));
        } else {
            out.print("```error " + repository.name() + disabledString + "```");
        }
        out.print(" : " + repository.repositoryType() + " " + repository.config().location());
        out.println();

    }

    public static void showRepoTree(NSession session, NRepository repository, String prefix) {
        showRepo(session, repository, prefix);
        String prefix1 = prefix + "  ";
        if (repository.config().isSupportedMirroring()) {
            for (NRepository c : repository.config().mirrors()) {
                showRepoTree(session, c, prefix1);
            }
        }
    }
}
