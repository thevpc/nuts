package net.thevpc.nuts.ext.term;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgumentCandidate;
import net.thevpc.nuts.cmdline.NutsCommandAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

class NutsJLineCompleter implements Completer {

    private final NutsSession session;
    private final NutsJLineTerminal nutsJLineTerminal;

    public NutsJLineCompleter(NutsSession session, NutsJLineTerminal nutsJLineTerminal) {
        this.session = session;
        this.nutsJLineTerminal = nutsJLineTerminal;
    }

    @Override
    public void complete(LineReader reader, final ParsedLine line, List<Candidate> candidates) {
        NutsCommandAutoCompleteResolver autoCompleteResolver = nutsJLineTerminal.getAutoCompleteResolver();
        if (autoCompleteResolver != null) {

            NutsCommandLine commandLine = NutsCommandLine.of(line.words());
            if (line.words().size() > 0) {
                commandLine.setCommandName(line.words().get(0));
            }
            List<NutsArgumentCandidate> nutsArgumentCandidates = autoCompleteResolver.resolveCandidates(commandLine, line.wordIndex(), session);
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
