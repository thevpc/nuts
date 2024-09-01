/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
package net.thevpc.nuts.toolbox.nsh.cmds.core;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinCore;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.eval.NShellResult;
import net.thevpc.nuts.util.NLiteral;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class ShowerrCommand extends NShellBuiltinCore {

    public ShowerrCommand() {
        super("showerr", NConstants.Support.DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a = cmdLine.peek().get(session);
        if (!a.isOption()) {
            if (options.login == null) {
                options.login = cmdLine.next(NArgName.of("username", session)).flatMap(NLiteral::asString).get(session);
                return true;
            } else if (options.password == null) {
                options.password = cmdLine.next(NArgName.of("password", session))
                        .flatMap(NLiteral::asString).get(session).toCharArray();
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArg a = cmdLine.peek().get(session);
        if (!a.isOption()) {
            if (options.login == null) {
                options.login = cmdLine.next(NArgName.of("username", session)).flatMap(NLiteral::asString).get(session);
                return true;
            } else if (options.password == null) {
                options.password = cmdLine.next(NArgName.of("password", session))
                        .flatMap(NLiteral::asString).get(session).toCharArray();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        NShellResult r = context.getShellContext().getLastResult();
        NPrintStream out = context.getSession().out();
        switch (context.getSession().getOutputFormat().orDefault()) {
            case PLAIN: {
                if (r.getCode() == 0) {
                    out.println(
                            NTexts.of(context.getSession()).ofStyled(
                                    "last command ended successfully with no errors.", NTextStyle.success()
                            ));
                } else {
                    out.println(
                            NTexts.of(context.getSession())
                                    .ofStyled("last command ended abnormally with the following error :", NTextStyle.error())
                    );
                    if (r.getMessage() != null) {
                        out.println(NTexts.of(context.getSession())
                                .ofStyled(r.getMessage(), NTextStyle.error()
                                ));
                    }
                    if (r.getStackTrace() != null) {
                        context.err().println(
                                NTexts.of(context.getSession())
                                        .ofStyled(r.getStackTrace(), NTextStyle.error())
                        );
                    }
                }
                break;
            }
            default: {
                out.println(r);
            }
        }
    }

    private static class Options {

        String login;
        char[] password;
    }

}
