/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.ByteArrayPrintStream;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author thevpc
 */
public abstract class DefaultFormatBase<T extends NutsFormat> extends DefaultFormatBase0<T> implements NutsFormat {

    public DefaultFormatBase(NutsWorkspace ws, String name) {
        super(ws, name);
    }

    @Override
    public PrintWriter getValidPrintWriter(Writer out) {
        checkSession();
        return (out == null) ?
                CoreIOUtils.toPrintWriter(getSession().getTerminal().getOut(), getSession())
                :
                CoreIOUtils.toPrintWriter(out, getSession());
    }

    @Override
    public PrintWriter getValidPrintWriter() {
        return getValidPrintWriter(null);
    }

    @Override
    public PrintStream getValidPrintStream(PrintStream out) {
        checkSession();
        if (out == null) {
            out = getSession().getTerminal().getOut();
        }
        return getSession().getWorkspace().term().setSession(getSession()).prepare(out);
    }

    @Override
    public PrintStream getValidPrintStream() {
        return getValidPrintStream(null);
    }

    @Override
    public NutsString format() {
        checkSession();
        ByteArrayPrintStream out = new ByteArrayPrintStream();
        print(out);
        return isNtf()?
                getSession().getWorkspace().text().parse(out.toString())
                :
                getSession().getWorkspace().text().forPlain(out.toString())
                ;
    }

    @Override
    public void print() {
        checkSession();
        print(getSession().getTerminal());
    }

    @Override
    public void println() {
        checkSession();
        println(getSession().getTerminal());
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
        checkSession();
        if (out == null) {
            PrintStream pout = getValidPrintStream();
            print(pout);
            pout.flush();
        } else {
            PrintStream pout = CoreIOUtils.toPrintStream(out, getSession());
            print(pout);
            pout.flush();
        }
    }

    @Override
    public void print(OutputStream out) {
        checkSession();
        PrintStream p = CoreIOUtils.toPrintStream(out, getSession());
        if (p == null) {
            p = getValidPrintStream();
        }
        print(p);
        p.flush();
    }

    @Override
    public void print(Path path) {
        checkSession();
                CoreIOUtils.mkdirs(path.getParent());
        try (Writer w = Files.newBufferedWriter(path)) {
            print(w);
        } catch (IOException ex) {
            throw new NutsIOException(getSession(), ex);
        }
    }

    @Override
    public void print(File file) {
        print(file.toPath());
    }

    @Override
    public void print(NutsTerminal terminal) {
        checkSession();
        print(terminal == null ? getSession().getTerminal().out() : terminal.out());
    }

    @Override
    public void println(Writer w) {
        checkSession();
        if (w == null) {
            PrintStream pout = getValidPrintStream();
            println(pout);
            pout.flush();
        } else {
            PrintStream pout = CoreIOUtils.toPrintStream(w, getSession());
            println(pout);
            pout.flush();
        }
    }

    @Override
    public void println(PrintStream out) {
        PrintStream p = getValidPrintStream(out);
        print(out);
        p.println();
        p.flush();
    }

    @Override
    public void println(Path path) {
        checkSession();
        try (Writer w = Files.newBufferedWriter(path)) {
            println(w);
        } catch (IOException ex) {
            throw new NutsIOException(getSession(), ex);
        }
    }

    @Override
    public void println(NutsTerminal terminal) {
        checkSession();
        println(terminal == null ? getSession().getTerminal().out() : terminal.out());
    }

    @Override
    public void println(File file) {
        println(file.toPath());
    }

    @Override
    public String toString() {
        return format().toString();
    }


}
