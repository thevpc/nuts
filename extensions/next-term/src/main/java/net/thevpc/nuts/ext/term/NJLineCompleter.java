package net.thevpc.nuts.ext.term;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgCandidate;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineAutoCompleteResolver;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

class NJLineCompleter implements Completer {

    private final NSession session;
    private final NJLineTerminal nutsJLineTerminal;

    public NJLineCompleter(NSession session, NJLineTerminal nutsJLineTerminal) {
        this.session = session;
        this.nutsJLineTerminal = nutsJLineTerminal;
    }

    @Override
    public void complete(LineReader reader, final ParsedLine line, List<Candidate> candidates) {
        NCmdLineAutoCompleteResolver autoCompleteResolver = nutsJLineTerminal.getAutoCompleteResolver();
        if (autoCompleteResolver != null) {

            NCmdLine cmdLine = NCmdLine.of(line.words());
            if (line.words().size() > 0) {
                cmdLine.setCommandName(line.words().get(0));
            }
            List<NArgCandidate> nArgCandidates = autoCompleteResolver.resolveCandidates(cmdLine, line.wordIndex(), session);
            if (nArgCandidates != null) {
                for (NArgCandidate cmdCandidate : nArgCandidates) {
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
