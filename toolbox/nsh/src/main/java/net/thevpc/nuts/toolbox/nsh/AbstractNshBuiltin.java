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

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.common.io.IOUtils;
import net.thevpc.common.strings.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.thevpc.nuts.NutsCommandAutoComplete;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsSupportLevelContext;

/**
 * Created by vpc on 1/7/17.
 */
public abstract class AbstractNshBuiltin implements NshBuiltin {

    private static final Logger LOG = Logger.getLogger(AbstractNshBuiltin.class.getName());
    private final String name;
    private final int supportLevel;
    private String help;
    private boolean enabled = true;

    public AbstractNshBuiltin(String name, int supportLevel) {
        this.name = name;
        this.supportLevel = supportLevel;
    }

    protected NutsCommandLine cmdLine(String[] args, NshExecutionContext context) {
        return context.getWorkspace().commandLine().create(args)
                .setAutoComplete(context.getGlobalContext().getAutoComplete())
                .setCommandName(getName());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsJavaShell> param) {
        return supportLevel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHelpHeader() {
        String h = getHelp();
        BufferedReader r = new BufferedReader(new StringReader(h));
        while (true) {
            String line = null;
            try {
                line = r.readLine();
            } catch (IOException e) {
                //
            }
            if (line == null) {
                break;
            }
            if (!StringUtils.isBlank(line)) {
                return line;
            }
        }
        return "No help";
    }

    @Override
    public String getHelp() {
        if (help == null) {
            try {
                URL resource = getClass().getResource("/net/thevpc/nuts/toolbox/nsh/cmd/" + getName() + ".help");
                if (resource != null) {
                    help = IOUtils.loadString(resource);
                }
            } catch (Exception e) {
                LOG.log(Level.CONFIG, "Unable to load help for " + getName(), e);
            }
            if (help == null) {
                help = "####no help``` found for command " + getName();
            }
        }
        return help;
    }

    @Override
    public void autoComplete(NshExecutionContext context, NutsCommandAutoComplete autoComplete) {
        NutsCommandAutoComplete oldAutoComplete = context.getGlobalContext().getAutoComplete();
        context.getGlobalContext().setAutoComplete(autoComplete);
        try {
            if (autoComplete == null) {
                throw new NutsIllegalArgumentException(context.getWorkspace(), "Missing Auto Complete");
            }
            NutsCommandAutoCompleteComponent best = context.getWorkspace().extensions().createServiceLoader(NutsCommandAutoCompleteComponent.class, NshBuiltin.class, NutsCommandAutoCompleteComponent.class.getClassLoader(), context.session())
                    .loadBest(AbstractNshBuiltin.this);
            if (best != null) {
                best.autoComplete(this, context);
            } else {
                String[] args = autoComplete.getWords().toArray(new String[0]);
                try {
                    exec(args, context);
                } catch (Exception ex) {
                    //ignore
                }
            }
        } finally {
            context.getGlobalContext().setAutoComplete(oldAutoComplete);
        }
    }

    @Override
    public abstract void exec(String[] args, NshExecutionContext context);

}
