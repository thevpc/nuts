package net.thevpc.nuts.ext.term;

import net.thevpc.nuts.cmdline.*;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.Set;

class NJLineCompleter implements Completer {

    private final NJLineTerminal nutsJLineTerminal;

    public NJLineCompleter(NJLineTerminal nutsJLineTerminal) {
        this.nutsJLineTerminal = nutsJLineTerminal;
    }

    @Override
    public void complete(LineReader reader, final ParsedLine line, List<Candidate> candidates) {
        NArgCompleteResolver autoCompleteResolver = nutsJLineTerminal.autoCompleteResolver();
        if (autoCompleteResolver != null) {

            NCmdLine cmdLine = NCmdLine.of(line.words());
            if (line.words().size() > 0) {
                cmdLine.commandName(line.words().get(0));
            }
            NArgCompleteResult nArgCompleteCandidates2 = autoCompleteResolver.resolveCandidates(cmdLine, new NArgCompletePos(
                    line.wordIndex(),
                    line.wordCursor(),
                    line.cursor()
            ));
            if (nArgCompleteCandidates2 != null) {
                Set<NArgCompleteFlag> flags = nArgCompleteCandidates2.flags();
                for (NArgCompleteCandidate cmdCandidate : nArgCompleteCandidates2.candidates()) {
                    if (cmdCandidate != null) {
                        String value = cmdCandidate.value();
                        if (value != null && !value.isEmpty()) {
                            String display = cmdCandidate.display();
                            if (display == null || display.isEmpty()) {
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
