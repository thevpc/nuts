package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

public class SettingsRepoUtils {
    public static void showRepo(NApplicationContext context, NRepository repository, String prefix) {
        boolean enabled = repository.config().isEnabled();
        String disabledString = enabled ? "" : " <DISABLED>";
        NOutputStream out = context.getSession().out();
        out.print(prefix);
        NTexts factory = NTexts.of(context.getSession());
        if (enabled) {
            out.print(factory.ofStyled(repository.getName() + disabledString, NTextStyle.primary2()));
        } else {
            out.print("```error " + repository.getName() + disabledString + "```");
        }
        out.print(" : " + repository.getRepositoryType() + " " + repository.config().getLocation());
        out.println();

    }

    public static void showRepoTree(NApplicationContext context, NRepository repository, String prefix) {
        showRepo(context, repository, prefix);
        String prefix1 = prefix + "  ";
        if (repository.config().isSupportedMirroring()) {
            for (NRepository c : repository.config().getMirrors()) {
                showRepoTree(context, c, prefix1);
            }
        }
    }
}
