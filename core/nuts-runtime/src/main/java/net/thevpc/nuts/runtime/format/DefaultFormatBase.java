/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.format;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import net.thevpc.nuts.NutsFormat;
import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsTerminal;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.util.io.ByteArrayPrintStream;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;

/**
 * @author vpc
 */
public abstract class DefaultFormatBase<T extends NutsFormat> extends DefaultFormatBase0<T> implements NutsFormat {

    public DefaultFormatBase(NutsWorkspace ws, String name) {
        super(ws, name);
    }

    @Override
    public PrintWriter getValidPrintWriter(Writer out) {
        return (out == null) ?
                CoreIOUtils.toPrintWriter(getValidSession().getTerminal().getOut(), getWorkspace())
                :
                CoreIOUtils.toPrintWriter(out, getWorkspace());
    }

    @Override
    public PrintWriter getValidPrintWriter() {
        return getValidPrintWriter(null);
    }

    @Override
    public PrintStream getValidPrintStream(PrintStream out) {
        if (out == null) {
            out = getValidSession().getTerminal().getOut();
        }
        return getWorkspace().io().term().getTerminalFormat().prepare(out);
    }

    @Override
    public PrintStream getValidPrintStream() {
        return getValidPrintStream(null);
    }

    @Override
    public String format() {
        ByteArrayPrintStream out = new ByteArrayPrintStream();
        print(out);
        return out.toString();
    }

    @Override
    public void print() {
        print(getValidSession().getTerminal());
    }

    @Override
    public void println() {
        println(getValidSession().getTerminal());
    }

    @Override
    public void print(NutsTerminal terminal) {
        print(terminal == null ? getValidSession().getTerminal().out() : terminal.out());
    }

    @Override
    public void println(NutsTerminal terminal) {
        println(terminal == null ? getValidSession().getTerminal().out() : terminal.out());
    }

    @Override
    public void print(File file) {
        print(file.toPath());
    }

    @Override
    public void println(File file) {
        println(file.toPath());
    }

    @Override
    public void print(Path path) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (IOException ex) {
            throw new NutsIOException(getWorkspace(),ex);
        }
        try (Writer w = Files.newBufferedWriter(path)) {
            print(w);
        } catch (IOException ex) {
            throw new NutsIOException(getWorkspace(),ex);
        }
    }

    @Override
    public void println(Path path) {
        try (Writer w = Files.newBufferedWriter(path)) {
            println(w);
        } catch (IOException ex) {
            throw new NutsIOException(getWorkspace(),ex);
        }
    }

    @Override
    public abstract void print(PrintStream out);

    //    @Override
//    public void print(PrintStream out) {
//        PrintWriter p = out == null ? null : new ExtendedFormatAwarePrintWriter(out);
//        print(p);
//        if (p != null) {
//            p.flush();
//        }
//    }
    @Override
    public void print(Writer out) {
        if (out == null) {
            PrintStream pout = getValidPrintStream();
            print(pout);
            pout.flush();
        } else {
            PrintStream pout = CoreIOUtils.toPrintStream(out, getWorkspace());
            print(pout);
            pout.flush();
        }
    }

    @Override
    public void print(OutputStream out) {
        PrintStream p = CoreIOUtils.toPrintStream(out, getWorkspace());
        if (p == null) {
            p = getValidPrintStream();
        }
        print(p);
        p.flush();
    }

    @Override
    public void println(PrintStream out) {
        PrintStream p = getValidPrintStream(out);
        print(out);
        p.println();
        p.flush();
    }

    @Override
    public void println(Writer w) {
        if (w == null) {
            PrintStream pout = getValidPrintStream();
            println(pout);
            pout.flush();
        } else {
            PrintStream pout = CoreIOUtils.toPrintStream(w, getWorkspace());
            println(pout);
            pout.flush();
        }
    }

    @Override
    public String toString() {
        return format();
    }


}
