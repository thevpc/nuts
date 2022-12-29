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

import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.jshell.*;
import net.thevpc.nuts.NExecutionException;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.toolbox.nsh.bundles._StringUtils;

/**
 *
 * @author thevpc
 */
public class NErrorHandler implements JShellErrorHandler {
    
    @Override
    public boolean isQuitException(Throwable th) {
        return th instanceof JShellQuitException;
    }

    @Override
    public int errorToCode(Throwable th) {
        if (th instanceof NExecutionException) {
            return ((NExecutionException) th).getExitCode();
        }
        return 1;
    }

    @Override
    public String errorToMessage(Throwable th) {
        return _StringUtils.exceptionToString(th);
    }

    @Override
    public void onError(String message, Throwable th, JShellContext context) {
        NStream err = context.getSession().getTerminal().err();
        err.printf("%s\n",
                NTexts.of(context.getSession()).ofStyled(message, NTextStyle.error())
                );
        err.flush();
    }
    
}
