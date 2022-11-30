package net.thevpc.nuts.toolbox.nsh.jshell;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.DefaultNutsArgumentCandidate;
import net.thevpc.nuts.cmdline.NutsArgumentCandidate;
import net.thevpc.nuts.cmdline.NutsCommandAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NutsCommandLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class NshAutoCompleter implements NutsCommandAutoCompleteResolver {

    @Override
    public List<NutsArgumentCandidate> resolveCandidates(NutsCommandLine commandLine, int wordIndex, NutsSession session) {
        List<NutsArgumentCandidate> candidates = new ArrayList<>();
        JShellContext fileContext = (JShellContext) session.env().getProperties().get(JShellContext.class.getName());

        if (wordIndex == 0) {
            for (JShellBuiltin command : fileContext.builtins().getAll()) {
                candidates.add(new DefaultNutsArgumentCandidate(command.getName()));
            }
        } else {
            List<String> autoCompleteWords = new ArrayList<>(Arrays.asList(commandLine.toStringArray()));
            int x = commandLine.getCommandName().length();

            List<JShellAutoCompleteCandidate> autoCompleteCandidates
                    = fileContext.resolveAutoCompleteCandidates(commandLine.getCommandName(), autoCompleteWords, wordIndex, commandLine.toString());
            for (Object cmdCandidate0 : autoCompleteCandidates) {
                JShellAutoCompleteCandidate cmdCandidate = (JShellAutoCompleteCandidate) cmdCandidate0;
                if (cmdCandidate != null) {
                    String value = cmdCandidate.getValue();
                    if (!NutsBlankable.isBlank(value)) {
                        String display = cmdCandidate.getDisplay();
                        if (NutsBlankable.isBlank(display)) {
                            display = value;
                        }
                        candidates.add(new DefaultNutsArgumentCandidate(value,display));
                    }
                }
            }
        }
        return candidates;
    }
}
