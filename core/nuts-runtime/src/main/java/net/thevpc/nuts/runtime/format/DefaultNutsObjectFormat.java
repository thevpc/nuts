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
package net.thevpc.nuts.runtime.format;

import java.io.File;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Path;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsException;
import net.thevpc.nuts.NutsObjectFormat;
import net.thevpc.nuts.NutsOutputFormat;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTerminal;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.format.tree.NutsObjectFormatTree;
import net.thevpc.nuts.runtime.format.xml.NutsObjectFormatXml;
import net.thevpc.nuts.runtime.format.json.NutsObjectFormatJson;
import net.thevpc.nuts.runtime.format.plain.NutsObjectFormatPlain;
import net.thevpc.nuts.runtime.format.props.NutsObjectFormatProps;
import net.thevpc.nuts.runtime.format.table.NutsObjectFormatTable;

/**
 *
 * @author vpc
 */
public class DefaultNutsObjectFormat extends NutsObjectFormatBase {

    private NutsOutputFormat outputFormat;
    private NutsObjectFormat base;

    public DefaultNutsObjectFormat(NutsWorkspace ws) {
        super(ws, "object-format");
    }

    @Override
    public NutsObjectFormat setSession(NutsSession session) {
        super.setSession(session);
        if (base != null) {
            base.setSession(getValidSession());
        }
        return this;
    }

    @Override
    public NutsObjectFormat setValue(Object value) {
        super.setValue(value);
        if (base != null) {
            base.setValue(value);
        }
        return this;
    }

    public NutsOutputFormat getOutputFormat() {
        NutsOutputFormat t = getValidSession().getOutputFormat();
        return t == null ? NutsOutputFormat.PLAIN : t;
    }

    public NutsObjectFormat getBase() {
        NutsOutputFormat nextOutputFormat = getOutputFormat();
        if (base == null || this.outputFormat != nextOutputFormat) {
            this.outputFormat = nextOutputFormat;
            base = createObjectFormat();
            base.setValue(getValue());
            base.setSession(getValidSession());
            base.configure(true, getWorkspace().config().options().getOutputFormatOptions());
            base.configure(true, getValidSession().getOutputFormatOptions());
        }
        return base;
    }

    public NutsObjectFormat createObjectFormat() {
        switch (getOutputFormat()) {
            case JSON: {
                return new NutsObjectFormatJson(getWorkspace());
            }
            case PROPS: {
                return new NutsObjectFormatProps(getWorkspace());
            }
            case TREE: {
                return new NutsObjectFormatTree(getWorkspace());
            }
            case PLAIN: {
                return new NutsObjectFormatPlain(getWorkspace());
            }
            case XML: {
                return new NutsObjectFormatXml(getWorkspace());
            }
            case TABLE: {
                return new NutsObjectFormatTable(getWorkspace());
            }
        }
        throw new NutsException(getWorkspace(), "Unsupported");
    }

    @Override
    public NutsSession getSession() {
        return base != null ? base.getSession() : super.getSession();
    }

    @Override
    public String format() {
        return getBase().format();
    }

    @Override
    public void print(PrintStream out) {
        getBase().print(out);
    }

    @Override
    public void print(Writer out) {
        getBase().print(out);
    }

    @Override
    public void print(Path out) {
        getBase().print(out);
    }

    @Override
    public void print(File out) {
        getBase().print(out);
    }

    @Override
    public void print() {
        getBase().print();
    }

    @Override
    public void print(NutsTerminal terminal) {
        getBase().print(terminal);
    }

    @Override
    public void println(Writer w) {
        getBase().println(w);
    }

    @Override
    public void println(PrintStream out) {
        getBase().println(out);
    }

    @Override
    public void println(Path path) {
        getBase().println(path);
    }

    @Override
    public void println() {
        getBase().println();
    }

    @Override
    public void println(NutsTerminal terminal) {
        getBase().println(terminal);
    }

    @Override
    public void println(File file) {
        getBase().println(file);
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return getBase().configureFirst(commandLine);
    }
}
