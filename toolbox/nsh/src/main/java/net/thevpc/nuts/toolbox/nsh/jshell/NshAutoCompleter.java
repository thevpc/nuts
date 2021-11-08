package net.thevpc.nuts.toolbox.nsh.jshell;

import net.thevpc.nuts.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class NshAutoCompleter implements NutsCommandAutoCompleteResolver {

    @Override
    public List<NutsArgumentCandidate> resolveCandidates(NutsCommandLine commandline, int wordIndex, NutsSession session) {
        List<NutsArgumentCandidate> candidates = new ArrayList<>();
        JShellContext fileContext = (JShellContext) session.env().getProperty(JShellContext.class.getName()).getObject();

        if (wordIndex == 0) {
            for (JShellBuiltin command : fileContext.builtins().getAll()) {
                candidates.add(new NutsArgumentCandidate(command.getName()));
            }
        } else {
            List<String> autoCompleteWords = new ArrayList<>(Arrays.asList(commandline.toStringArray()));
            int x = commandline.getCommandName().length();

            List<JShellAutoCompleteCandidate> autoCompleteCandidates
                    = fileContext.resolveAutoCompleteCandidates(commandline.getCommandName(), autoCompleteWords, wordIndex, commandline.toString());
            for (Object cmdCandidate0 : autoCompleteCandidates) {
                JShellAutoCompleteCandidate cmdCandidate = (JShellAutoCompleteCandidate) cmdCandidate0;
                if (cmdCandidate != null) {
                    String value = cmdCandidate.getValue();
                    if (!NutsBlankable.isBlank(value)) {
                        String display = cmdCandidate.getDisplay();
                        if (NutsBlankable.isBlank(display)) {
                            display = value;
                        }
                        candidates.add(new NutsArgumentCandidate(value,display));
                    }
                }
            }
        }
        return candidates;
    }
}
