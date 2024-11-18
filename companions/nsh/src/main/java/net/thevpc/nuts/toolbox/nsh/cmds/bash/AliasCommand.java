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
package net.thevpc.nuts.toolbox.nsh.cmds.bash;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.nshell.NShell;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class AliasCommand extends NShellBuiltinDefault {

    public AliasCommand() {
        super("alias", NConstants.Support.DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        final NArg a = cmdLine.peek().get();
        if (a.isOption()) {
            if (a.key().equals("--sort")) {
                cmdLine.skip();
                options.displayOptions.add(a.toString());
                return true;
            }
        } else if (a.isKeyValue()) {
            cmdLine.skip();
            options.add.put(a.key(), a.getStringValue().get());
            return true;
        } else {
            cmdLine.skip();
            options.show.add(a.asString().get());
            return true;
        }
        return false;
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NShell shell = context.getShell();
        if (options.add.isEmpty() && options.show.isEmpty()) {
            options.show.addAll(context.aliases().getAll());
        }
        for (Map.Entry<String, String> entry : options.add.entrySet()) {
            context.aliases().set(entry.getKey(), entry.getValue());
        }
        List<ResultItem> outRes = new ArrayList<>();
        List<ResultItem> errRes = new ArrayList<>();
        for (String a : options.show) {
            final String v = context.aliases().get(a);
            if (v == null) {
                errRes.add(new ResultItem(a, v));
            } else {
                outRes.add(new ResultItem(a, v));
            }
        }
        switch (context.getSession().getOutputFormat().orDefault()) {
            case PLAIN: {
                for (ResultItem resultItem : outRes) {
                    if (resultItem.value == null) {
                        context.getSession().err().println(NMsg.ofC("alias : %s ```error not found```", resultItem.name));
                    } else {
                        context.getSession().out().println(NMsg.ofC("alias : %s ='%s'", resultItem.name, resultItem.value));
                    }
                }
                break;
            }
            default: {
                context.getSession().out().println(outRes);
            }
        }
        if (!errRes.isEmpty()) {
            throwExecutionException(errRes, 1, context.getSession());
        }
    }

    private static class Options {

        LinkedHashMap<String, String> add = new LinkedHashMap<String, String>();
        Set<String> show = new LinkedHashSet<String>();
        List<String> displayOptions = new ArrayList<String>();
    }

    private static class ResultItem {

        String name;
        String value;

        public ResultItem(String name, String value) {
            this.name = name;
            this.value = value;
        }

    }


}
