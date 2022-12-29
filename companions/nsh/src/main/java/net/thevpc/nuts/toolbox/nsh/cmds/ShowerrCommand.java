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
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NArgumentName;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NStream;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NComponentScopeType;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellResult;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NComponentScopeType.WORKSPACE)
public class ShowerrCommand extends SimpleJShellBuiltin {

    public ShowerrCommand() {
        super("showerr", DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean configureFirst(NCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        NArgument a = commandLine.peek().get(session);
        if (!a.isOption()) {
            if (options.login == null) {
                options.login = commandLine.next(NArgumentName.of("username", session)).flatMap(NValue::asString).get(session);
                return true;
            } else if (options.password == null) {
                options.password = commandLine.next(NArgumentName.of("password", session))
                        .flatMap(NValue::asString).get(session).toCharArray();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void execBuiltin(NCommandLine commandLine, JShellExecutionContext context) {
        JShellResult r = context.getShellContext().getLastResult();
        NStream out = context.getSession().out();
        switch (context.getSession().getOutputFormat()) {
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
                out.printlnf(r);
            }
        }
    }

    private static class Options {

        String login;
        char[] password;
    }

}
