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
package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineAutoComplete;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.bundles._IOUtils;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public abstract class AbstractJShellBuiltin implements JShellBuiltin {

    private static final Logger LOG = Logger.getLogger(AbstractJShellBuiltin.class.getName());
    private final String name;
    private final int supportLevel;
    private String help;
    private boolean enabled = true;

    public AbstractJShellBuiltin(String name, int supportLevel) {
        this.name = name;
        this.supportLevel = supportLevel;
    }

    protected NCmdLine cmdLine(String[] args, JShellExecutionContext context) {
        NSession session = context.getSession();
        return NCmdLine.of(args)
                .setAutoComplete(context.getShellContext().getAutoComplete())
                .setCommandName(getName());
    }

    @Override
    public int getSupportLevel(NSupportLevelContext param) {
        return supportLevel;
    }

    @Override
    public void autoComplete(JShellExecutionContext context, NCmdLineAutoComplete autoComplete) {
        NCmdLineAutoComplete oldAutoComplete = context.getShellContext().getAutoComplete();
        context.getShellContext().setAutoComplete(autoComplete);
        try {
            if (autoComplete == null) {
                throw new NIllegalArgumentException(context.getSession(),  NMsg.ofPlain("missing auto-complete"));
            }
            NCommandAutoCompleteComponent best = context.getSession().extensions().createServiceLoader(NCommandAutoCompleteComponent.class, JShellBuiltin.class, NCommandAutoCompleteComponent.class.getClassLoader())
                    .loadBest(AbstractJShellBuiltin.this);
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
            context.getShellContext().setAutoComplete(oldAutoComplete);
        }
    }

    protected abstract void execImpl(String[] args, JShellExecutionContext context);

    public final void exec(String[] args, JShellExecutionContext context) {
        try {
            execImpl(args, context);
        } catch (NExecutionException ex) {
//            if(ex.getExitCode()!=0) {
//                context.err().println(ex.toString());
//            }
            throw ex;
        } catch (NException ex) {
            throw new NExecutionException(context.getSession(),ex.getFormattedMessage(),ex,254);
        } catch (Exception ex) {
            throw new NExecutionException(context.getSession(),
                    NMsg.ofNtf(NTexts.of(context.getSession()).ofText(ex).toString())
                    ,ex,254);
        }
    }

    @Override
    public String getHelp() {
        if (help == null) {
            try {
                URL resource = getClass().getResource("/net/thevpc/nuts/toolbox/nsh/cmd/" + getName() + ".ntf");
                if (resource != null) {
                    help = _IOUtils.loadString(resource);
                }
            } catch (Exception e) {
                LOG.log(Level.CONFIG, "Unable to load help for " + getName(), e);
            }
            if (help == null) {
                help = "```error no help found for command " + getName() + "```";
            }
        }
        return help;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
            if (!NBlankable.isBlank(line)) {
                return line;
            }
        }
        return "No help";
    }

    protected void throwExecutionException(Object errObject, int errorCode, NSession session) {
        session=session.copy();
        NPrintStream printStream = NMemoryPrintStream.of(session);
        if (errObject != null) {
            printStream.print(errObject);
        }else{
            printStream.println(NMsg.ofC("%s: command failed with code %s",getName(),errorCode));
        }
        throw new NExecutionException(session, NMsg.ofNtf(printStream.toString()), errorCode);
    }

}
