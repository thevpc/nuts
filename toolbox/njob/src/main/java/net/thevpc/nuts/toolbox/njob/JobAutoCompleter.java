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
import net.thevpc.nuts.cmdline.DefaultNCmdLineAutoComplete;
import net.thevpc.nuts.cmdline.NArgCandidate;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineAutoCompleteResolver;

/**
 *
 * @author thevpc
 */
public class JobAutoCompleter implements NCmdLineAutoCompleteResolver {

    public JobAutoCompleter() {
    }

    @Override
    public List<NArgCandidate> resolveCandidates(NCmdLine commandLine, int wordIndex, NSession session) {
        JobServiceCmd fileContext = (JobServiceCmd) NEnvs.of(session).getProperties().get(JobServiceCmd.class.getName());
        DefaultNCmdLineAutoComplete autoComplete = new DefaultNCmdLineAutoComplete()
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
