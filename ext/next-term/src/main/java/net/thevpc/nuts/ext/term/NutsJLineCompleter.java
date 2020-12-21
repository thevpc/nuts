package net.thevpc.nuts.ext.term;

import net.thevpc.nuts.NutsArgumentCandidate;
import net.thevpc.nuts.NutsCommandAutoCompleteProcessor;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.jshell.JShellAutoCompleteCandidate;
import net.thevpc.common.strings.StringUtils;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

class NutsJLineCompleter implements Completer {

    private final NutsWorkspace workspace;
    private final NutsJLineTerminal nutsJLineTerminal;

    public NutsJLineCompleter(NutsWorkspace workspace,NutsJLineTerminal nutsJLineTerminal) {
        this.workspace = workspace;
        this.nutsJLineTerminal = nutsJLineTerminal;
    }

    @Override
    public void complete(LineReader reader, final ParsedLine line, List<Candidate> candidates) {
        NutsCommandAutoCompleteProcessor autoCompleteResolver = nutsJLineTerminal.getAutoCompleteResolver();
        if (autoCompleteResolver != null) {

            NutsCommandLine commandline = workspace.commandLine().create(line.words());
            if(line.words().size()>0){
                commandline.setCommandName(line.words().get(0));
            }
            List<NutsArgumentCandidate> nutsArgumentCandidates = autoCompleteResolver.resolveCandidates(commandline, line.wordIndex(),workspace);
            if(nutsArgumentCandidates!=null) {
                for (Object cmdCandidate0 : nutsArgumentCandidates) {
                    JShellAutoCompleteCandidate cmdCandidate = (JShellAutoCompleteCandidate) cmdCandidate0;
                    if (cmdCandidate != null) {
                        String value = cmdCandidate.getValue();
                        if (!StringUtils.isBlank(value)) {
                            String display = cmdCandidate.getDisplay();
                            if (StringUtils.isBlank(display)) {
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
