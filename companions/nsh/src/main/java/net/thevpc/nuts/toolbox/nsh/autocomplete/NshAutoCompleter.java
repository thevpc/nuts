package net.thevpc.nuts.toolbox.nsh.autocomplete;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.DefaultNArgCandidate;
import net.thevpc.nuts.cmdline.NArgCandidate;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineAutoCompleteResolver;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NshAutoCompleter implements NCmdLineAutoCompleteResolver {

    @Override
    public List<NArgCandidate> resolveCandidates(NCmdLine commandLine, int wordIndex, NSession session) {
        List<NArgCandidate> candidates = new ArrayList<>();
        NShellContext fileContext = (NShellContext) NEnvs.of(session).getProperties().get(NShellContext.class.getName());

        if (wordIndex == 0) {
            for (NShellBuiltin command : fileContext.builtins().getAll()) {
                candidates.add(new DefaultNArgCandidate(command.getName()));
            }
        } else {
            List<String> autoCompleteWords = new ArrayList<>(Arrays.asList(commandLine.toStringArray()));
            int x = commandLine.getCommandName().length();

            List<NShellAutoCompleteCandidate> autoCompleteCandidates
                    = fileContext.resolveAutoCompleteCandidates(commandLine.getCommandName(), autoCompleteWords, wordIndex, commandLine.toString());
            for (Object cmdCandidate0 : autoCompleteCandidates) {
                NShellAutoCompleteCandidate cmdCandidate = (NShellAutoCompleteCandidate) cmdCandidate0;
                if (cmdCandidate != null) {
                    String value = cmdCandidate.getValue();
                    if (!NBlankable.isBlank(value)) {
                        String display = cmdCandidate.getDisplay();
                        if (NBlankable.isBlank(display)) {
                            display = value;
                        }
                        candidates.add(new DefaultNArgCandidate(value,display));
                    }
                }
            }
        }
        return candidates;
    }
}
