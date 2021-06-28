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
package net.thevpc.nuts.toolbox.nsh.cmds;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShell;
import net.thevpc.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class AliasCommand extends SimpleNshBuiltin {

    public AliasCommand() {
        super("alias", DEFAULT_SUPPORT);
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

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        final NutsArgument a = commandLine.peek();
        if (a.isOption()) {
            if (a.getStringKey().equals("--sort")) {
                commandLine.skip();
                options.displayOptions.add(a.toString());
                return true;
            }
        } else if (a.isKeyValue()) {
            commandLine.skip();
            options.add.put(a.getStringKey(), a.getStringValue());
            return true;
        } else {
            commandLine.skip();
            options.show.add(a.getString());
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        JShell shell = context.getShell();
        if (options.add.isEmpty() && options.show.isEmpty()) {
            options.show.addAll(context.getRootContext().aliases().getAll());
        }
        for (Map.Entry<String, String> entry : options.add.entrySet()) {
            context.getRootContext().aliases().set(entry.getKey(), entry.getValue());
        }
        List<ResultItem> outRes = new ArrayList<>();
        List<ResultItem> errRes = new ArrayList<>();
        for (String a : options.show) {
            final String v = context.getRootContext().aliases().get(a);
            if (v == null) {
                errRes.add(new ResultItem(a, v));
            } else {
                outRes.add(new ResultItem(a, v));
            }
        }
        context.setPrintlnOutObject(outRes);
        if (!errRes.isEmpty()) {
            context.setErrObject(errRes);
        }
    }

    @Override
    protected void printPlainObject(SimpleNshCommandContext context, NutsSession session) {
        List<ResultItem> r = context.getResult();
        for (ResultItem resultItem : r) {
            if (resultItem.value == null) {
                context.out().printf("alias : %s ```error not found```%n", resultItem.name);
            } else {
                context.out().printf("alias : %s ='%s'%n", resultItem.name, resultItem.value);
            }
        }
    }

}
