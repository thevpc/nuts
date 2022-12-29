/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.toolbox.njob;

import java.util.Arrays;
import java.util.List;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.DefaultNCommandAutoComplete;
import net.thevpc.nuts.cmdline.NArgumentCandidate;
import net.thevpc.nuts.cmdline.NCommandAutoCompleteResolver;
import net.thevpc.nuts.cmdline.NCommandLine;

/**
 *
 * @author thevpc
 */
public class JobAutoCompleter implements NCommandAutoCompleteResolver {

    public JobAutoCompleter() {
    }

    @Override
    public List<NArgumentCandidate> resolveCandidates(NCommandLine commandLine, int wordIndex, NSession session) {
        JobServiceCmd fileContext = (JobServiceCmd) session.env().getProperties().get(JobServiceCmd.class.getName());
        DefaultNCommandAutoComplete autoComplete = new DefaultNCommandAutoComplete()
                .setSession(session)
                .setCurrentWordIndex(wordIndex)
                .setLine(commandLine.toString()).setWords(
                Arrays.asList(commandLine.toStringArray())
        );
        commandLine.setAutoComplete(autoComplete);
        fileContext.runCommands(commandLine);
        return autoComplete.getCandidates();
    }

}
