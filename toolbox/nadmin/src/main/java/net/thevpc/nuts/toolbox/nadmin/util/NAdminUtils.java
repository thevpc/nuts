package net.thevpc.nuts.toolbox.nadmin.util;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.NutsTextNodeStyle;

import java.io.PrintStream;
import net.thevpc.nuts.NutsTextManager;

public class NAdminUtils {
    public static void showRepo(NutsApplicationContext context, NutsRepository repository, String prefix) {
        boolean enabled = repository.config().isEnabled();
        String disabledString = enabled ? "" : " <DISABLED>";
        PrintStream out = context.getSession().out();
        out.print(prefix);
        NutsTextManager factory = context.getWorkspace().formats().text();
        if (enabled) {
            out.print(factory.styled(repository.getName() + disabledString, NutsTextNodeStyle.primary(2)));
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
            for (NutsRepository c : repository.config().getMirrors(context.getSession())) {
                showRepoTree(context, c, prefix1);
            }
        }
    }
}
