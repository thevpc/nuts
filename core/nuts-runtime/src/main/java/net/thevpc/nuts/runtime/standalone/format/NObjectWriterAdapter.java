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
package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.*;

/**
 * @author thevpc
 */
public abstract class NObjectWriterAdapter /*extends DefaultFormatBase<NFormat>*/ implements NObjectWriter {

    protected List<String[]> confCmds = new ArrayList<>();
    protected boolean ntf;

    public abstract NFormatAndValue<Object, NObjectWriter> getBase(Object aValue);

    public NFormatAndValue<Object, NObjectWriter> getConfBase(Object aValue) {
        NFormatAndValue<Object, NObjectWriter> b = getBase(aValue);
        for (String[] confCmd : confCmds) {
            b.getFormat().configure(true).configure(true, confCmd);
        }
        b.getFormat().setNtf(isNtf());
        return b;
    }

    @Override
    public String formatPlain(Object aValue) {
        return getConfBase(aValue).getFormat().formatPlain(aValue);
    }

    @Override
    public void print(Object aValue, OutputStream out) {
        getConfBase(aValue).getFormat().print(aValue, out);
    }

    @Override
    public void print(Object aValue, NPath out) {
        getConfBase(aValue).getFormat().print(aValue, out);
    }

    @Override
    public void println(Object aValue, OutputStream out) {
        getConfBase(aValue).getFormat().print(aValue, out);
    }

    @Override
    public void println(Object aValue, NPath out) {
        getConfBase(aValue).getFormat().print(aValue, out);
    }


    @Override
    public boolean isNtf() {
        return ntf;
    }

    @Override
    public NObjectWriter setNtf(boolean ntf) {
        this.ntf = ntf;
        return this;
    }


    @Override
    public NText format(Object aValue) {
        return getConfBase(aValue).getFormat().format(aValue);
    }

    @Override
    public void print(Object aValue) {
        getConfBase(aValue).getFormat().print(aValue);
    }

    @Override
    public void println(Object aValue) {
        getConfBase(aValue).getFormat().println(aValue);
    }

    @Override
    public void print(Object aValue, NPrintStream out) {
        getConfBase(aValue).getFormat().print(aValue, out);
    }

    @Override
    public void print(Object aValue, Writer out) {
        getConfBase(aValue).getFormat().print(aValue, out);
    }

    @Override
    public void print(Object aValue, Path out) {
        getConfBase(aValue).getFormat().print(aValue, out);
    }

    @Override
    public void print(Object aValue, File out) {
        getConfBase(aValue).getFormat().print(aValue, out);
    }

    @Override
    public void print(Object aValue, NTerminal terminal) {
        getConfBase(aValue).getFormat().print(aValue, terminal);
    }

    @Override
    public void println(Object aValue, Writer w) {
        getConfBase(aValue).getFormat().println(aValue, w);
    }

    @Override
    public void println(Object aValue, NPrintStream out) {
        getConfBase(aValue).getFormat().println(aValue, out);
    }

    @Override
    public void println(Object aValue, Path path) {
        getConfBase(aValue).getFormat().println(aValue, path);
    }

    @Override
    public void println(Object aValue, NTerminal terminal) {
        getConfBase(aValue).getFormat().println(aValue, terminal);
    }
    @Override
    public void println(Object aValue, File file) {
        getConfBase(aValue).getFormat().println(aValue, file);
    }

    @Override
    public void write(Object aValue) {
        getConfBase(aValue).getFormat().write(aValue);
    }

    @Override
    public void writeln(Object aValue) {
        getConfBase(aValue).getFormat().writeln(aValue);
    }

    @Override
    public void write(Object aValue, NPrintStream out) {
        getConfBase(aValue).getFormat().write(aValue,out);
    }

    @Override
    public void write(Object aValue, Writer out) {
        getConfBase(aValue).getFormat().write(aValue,out);
    }

    @Override
    public void write(Object aValue, OutputStream out) {
        getConfBase(aValue).getFormat().write(aValue,out);
    }

    @Override
    public void write(Object aValue, Path out) {
        getConfBase(aValue).getFormat().write(aValue,out);
    }

    @Override
    public void write(Object aValue, NPath out) {
        getConfBase(aValue).getFormat().write(aValue,out);
    }

    @Override
    public void write(Object aValue, File out) {
        getConfBase(aValue).getFormat().write(aValue,out);
    }

    @Override
    public void write(Object aValue, NTerminal out) {
        getConfBase(aValue).getFormat().write(aValue,out);
    }

    @Override
    public void writeln(Object aValue, Writer out) {
        getConfBase(aValue).getFormat().writeln(aValue,out);
    }

    @Override
    public void writeln(Object aValue, NPrintStream out) {
        getConfBase(aValue).getFormat().writeln(aValue,out);

    }

    @Override
    public void writeln(Object aValue, OutputStream out) {
        getConfBase(aValue).getFormat().writeln(aValue,out);
    }

    @Override
    public void writeln(Object aValue, Path out) {
        getConfBase(aValue).getFormat().writeln(aValue,out);

    }

    @Override
    public void writeln(Object aValue, NPath out) {
        getConfBase(aValue).getFormat().writeln(aValue,out);

    }

    @Override
    public void writeln(Object aValue, NTerminal out) {
        getConfBase(aValue).getFormat().writeln(aValue,out);

    }

    @Override
    public void writeln(Object aValue, File out) {
        getConfBase(aValue).getFormat().writeln(aValue,out);
    }


    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NOptional<NArg> peek = cmdLine.peek();
        if (peek.isPresent()) {
            NOptional<NArg> n = cmdLine.next();
            confCmds.add(new String[]{n.get().image()});
            return true;
        }
        return false;
    }

    @Override
    public boolean configure(boolean skipUnsupported, NCmdLine cmdLine) {
        String[] a = cmdLine.toStringArray();
        cmdLine.skipAll();
        confCmds.add(a);
        return a.length > 0;
    }

    @Override
    public NObjectWriter configure(boolean skipUnsupported, String... args) {
        confCmds.add(args);
        return this;
    }

    @Override
    public void configureLast(NCmdLine cmdLine) {
        configure(true, cmdLine);
    }
}
