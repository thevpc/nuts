/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.jshell.*;
import net.thevpc.nuts.NutsExecutionException;
import net.thevpc.common.strings.StringUtils;
import net.thevpc.nuts.NutsTextNodeStyle;

/**
 *
 * @author thevpc
 */
public class NutsErrorHandler implements JShellErrorHandler {
    
    @Override
    public boolean isRequireExit(Throwable th) {
        return th instanceof JShellQuitException;
    }

    @Override
    public int errorToCode(Throwable th) {
        if (th instanceof NutsExecutionException) {
            return ((NutsExecutionException) th).getExitCode();
        }
        if (th instanceof JShellException) {
            return ((JShellException) th).getResult();
        }
        return 1;
    }

    @Override
    public String errorToMessage(Throwable th) {
        return StringUtils.exceptionToString(th);
    }

    @Override
    public void onErrorImpl(String message, Throwable th, JShellFileContext context) {
        ((NutsShellContext) (context.getShellContext())).getSession().getTerminal().err().printf(
                "%s\n",
                ((NutsShellContext)context.getShellContext()).getWorkspace().formats().text().builder().append(message, NutsTextNodeStyle.error())
                );
    }
    
}
