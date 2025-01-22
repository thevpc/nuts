/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.err;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.NExecutionException;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.toolbox.nsh.util.bundles._StringUtils;
import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;

/**
 * @author thevpc
 */
public class DefaultErrorHandler implements NShellErrorHandler {

    @Override
    public boolean isQuitException(Throwable th) {
        return th instanceof NShellQuitException;
    }

    @Override
    public int errorToCode(Throwable th) {
        if (th instanceof NShellException) {
            return ((NShellException) th).getExitCode();
        }
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
    public void onError(String message, Throwable th, NShellContext context) {
        if (context.getSession() != null && context.getSession().err() != null) {
            NPrintStream err = context.getSession().err();
            err.println(NMsg.ofStyledError(message));
            err.flush();
        }else{
            th.printStackTrace();
        }
    }

}
