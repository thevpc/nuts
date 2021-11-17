package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util;

import net.thevpc.nuts.*;

public class SettingsRepoUtils {
    public static void showRepo(NutsApplicationContext context, NutsRepository repository, String prefix) {
        boolean enabled = repository.config().isEnabled();
        String disabledString = enabled ? "" : " <DISABLED>";
        NutsPrintStream out = context.getSession().out();
        out.print(prefix);
        NutsTexts factory = NutsTexts.of(context.getSession());
        if (enabled) {
            out.print(factory.ofStyled(repository.getName() + disabledString, NutsTextStyle.primary2()));
        } else {
            out.print("```error " + repository.getName() + disabledString + "```");
        }
        out.print(" : " + repository.getRepositoryType() + " " + repository.config().getLocation(false));
        out.println();

    }

    public static void showRepoTree(NutsApplicationContext context, NutsRepository repository, String prefix) {
        showRepo(context, repository, prefix);
        String prefix1 = prefix + "  ";
        if (repository.config().isSupportedMirroring()) {
            for (NutsRepository c : repository.config().getMirrors()) {
                showRepoTree(context, c, prefix1);
            }
        }
    }
}
