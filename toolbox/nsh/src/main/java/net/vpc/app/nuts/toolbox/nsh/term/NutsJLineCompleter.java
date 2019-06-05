package net.vpc.app.nuts.toolbox.nsh.term;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.javashell.AutoCompleteCandidate;
import net.vpc.common.strings.StringUtils;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.ArrayList;
import java.util.List;
import net.vpc.common.javashell.JShellCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsShellContext;

class NutsJLineCompleter implements Completer {
    private final NutsWorkspace workspace;

    public NutsJLineCompleter(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public void complete(LineReader reader, final ParsedLine line, List<Candidate> candidates) {
        NutsShellContext nutsConsoleContext = (NutsShellContext) workspace.getUserProperties().get(NutsShellContext.class.getName());
        if (nutsConsoleContext != null) {
            if (line.wordIndex() == 0) {
                for (JShellCommand command : nutsConsoleContext.builtins().getAll()) {
                    candidates.add(new Candidate(command.getName()));
                }
            } else {
                String commandName = line.words().get(0);
                int wordIndex = line.wordIndex() - 1;
                List<String> autoCompleteWords = new ArrayList<>(line.words().subList(1, line.words().size()));
                int x = commandName.length();
                String autoCompleteLine = line.line().substring(x);
                List<AutoCompleteCandidate> autoCompleteCandidates =
                        nutsConsoleContext.resolveAutoCompleteCandidates(commandName, autoCompleteWords, wordIndex, autoCompleteLine);
                for (Object cmdCandidate0 : autoCompleteCandidates) {
                    AutoCompleteCandidate cmdCandidate = (AutoCompleteCandidate) cmdCandidate0;
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
