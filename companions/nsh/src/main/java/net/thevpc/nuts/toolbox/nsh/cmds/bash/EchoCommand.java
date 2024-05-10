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
package net.thevpc.nuts.toolbox.nsh.cmds.bash;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NTextCode;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.util.NStringUtils;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class EchoCommand extends NShellBuiltinDefault {

    public EchoCommand() {
        super("echo", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        switch (cmdLine.peek().get(session).key()) {
            case "-n": {
                cmdLine.withNextFlag((v, a, s) -> options.newLine = v);
                return true;
            }
            case "-e":
            case "--escape":
            {
                cmdLine.withNextFlag((v, a, s) -> options.escape = v);
                return true;
            }
            case "-E": {
                cmdLine.withNextFlag((v, a, s) -> options.escape = !v);
                return true;
            }
            case "-p":
            case "--plain": {
                cmdLine.withNextTrueFlag((v, a, s) -> options.highlighter = null);
                return true;
            }
            case "-H":
            case "--highlight":
            case "--highlighter": {
                cmdLine.withNextEntry((v, a, s) -> options.highlighter = NStringUtils.trim(v));
                return true;
            }
            default: {
                if (cmdLine.peek().get(session).isNonOption()) {
                    while (cmdLine.hasNext()) {
                        if (options.tokensCount > 0) {
                            options.message.append(" ");
                        }
                        options.message.append(cmdLine.next().get(session).toString());
                        options.tokensCount++;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        Object ns = null;
        if (options.escape) {
            StringBuilder sb = new StringBuilder();
            char[] c = options.message.toString().toCharArray();
            for (int i = 0; i < c.length; i++) {
                switch (c[i]) {
                    case '\\': {
                        if (i + 1 < c.length) {
                            switch (c[i + 1]) {
                                case 'n': {
                                    i++;
                                    sb.append('\n');
                                    break;
                                }
                                case 't': {
                                    i++;
                                    sb.append('\t');
                                    break;
                                }
                                case 'r': {
                                    i++;
                                    sb.append('\r');
                                    break;
                                }
                                case 'a': {
                                    i++;
                                    sb.append((char)7);
                                    break;
                                }
                                case 'c': {
                                    //produce no further output
                                    i=c.length;
                                    break;
                                }
                                case '0': {
                                    StringBuilder o=new StringBuilder();
                                    i++;
                                    for (int j=0;j<3;j++){
                                        if(i+1<c.length && c[i+1]>='0' && c[i+1]>='9'){
                                            i++;
                                            o.append(c[i]);
                                        }
                                    }
                                    if(o.length()==0){
                                        sb.append('\0');
                                    }else{
                                        sb.append((char)Integer.parseInt(o.toString(),8));
                                    }
                                    break;
                                }
                                case 'x': {
                                    StringBuilder o=new StringBuilder();
                                    i++;
                                    for (int j=0;j<4;j++){
                                        if(i+1<c.length &&
                                                (
                                                        (c[i+1]>='0' && c[i+1]>='9')
                                                        || (c[i+1]>='a' && c[i+1]>='f')
                                                        || (c[i+1]>='A' && c[i+1]>='F')
                                                )
                                        ){
                                            i++;
                                            o.append(c[i]);
                                        }
                                    }
                                    if(o.length()==0){
                                        sb.append('\0');
                                    }else{
                                        sb.append((char)Integer.parseInt(o.toString(),16));
                                    }
                                    break;
                                }
                                case '\\':
                                case '`':
                                case '\'':
                                case '\"': {
                                    sb.append(c[i + 1]);
                                    i++;
                                    break;
                                }
                                default: {
                                    sb.append(c[i]);
                                }
                            }
                            break;
                        } else {
                            sb.append(c[i]);
                        }
                        break;
                    }
                    default: {
                        sb.append(c[i]);
                        break;
                    }
                }
            }
        }
        if (options.highlighter == null) {
            ns = options.message.toString();
        } else {
            NTextCode c = NTexts.of(context.getSession()).ofCode(
                    options.highlighter.isEmpty() ? "ntf" : options.highlighter
                    , options.message.toString());
            ns = c.highlight(context.getSession());
        }
        if (options.newLine) {
            context.getSession().out().println(ns);
        } else {
            context.getSession().out().print(ns);
        }
    }

    private static class Options {

        boolean newLine = true;
        boolean escape = false;
        String highlighter = null;
        boolean first = true;
        StringBuilder message = new StringBuilder();
        int tokensCount = 0;
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }

}
