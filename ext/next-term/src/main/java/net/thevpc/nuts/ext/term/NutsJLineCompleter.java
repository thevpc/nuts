package net.thevpc.nuts.ext.term;

import net.thevpc.nuts.NutsArgumentCandidate;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsWorkspace;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import net.thevpc.nuts.NutsCommandAutoCompleteResolver;

class NutsJLineCompleter implements Completer {

    private final NutsWorkspace workspace;
    private final NutsJLineTerminal nutsJLineTerminal;

    public NutsJLineCompleter(NutsWorkspace workspace, NutsJLineTerminal nutsJLineTerminal) {
        this.workspace = workspace;
        this.nutsJLineTerminal = nutsJLineTerminal;
    }

    @Override
    public void complete(LineReader reader, final ParsedLine line, List<Candidate> candidates) {
        NutsCommandAutoCompleteResolver autoCompleteResolver = nutsJLineTerminal.getAutoCompleteResolver();
        if (autoCompleteResolver != null) {

            NutsCommandLine commandline = workspace.commandLine().create(line.words());
            if (line.words().size() > 0) {
                commandline.setCommandName(line.words().get(0));
            }
            List<NutsArgumentCandidate> nutsArgumentCandidates = autoCompleteResolver.resolveCandidates(commandline, line.wordIndex(), workspace.createSession());
            if (nutsArgumentCandidates != null) {
                for (NutsArgumentCandidate cmdCandidate : nutsArgumentCandidates) {
                    if (cmdCandidate != null) {
                        String value = cmdCandidate.getValue();
                        if (value != null && value.length() > 0) {
                            String display = cmdCandidate.getDisplay();
                            if (display == null || display.length() == 0) {
                                display = value;
                            }
                            candidates.add(new Candidate(
                                    value,
                                    display,
                                    null, null, null, null, true
                            ));
                        }
                    }
                }
            }
        }
    }
}
