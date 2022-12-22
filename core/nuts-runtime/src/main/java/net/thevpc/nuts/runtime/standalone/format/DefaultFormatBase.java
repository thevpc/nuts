/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.text.NutsTexts;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;

/**
 * @author thevpc
 */
public abstract class DefaultFormatBase<T extends NutsFormat> extends DefaultFormatBase0<T> implements NutsFormat {

    public DefaultFormatBase(NutsWorkspace ws, String name) {
        super(ws, name);
    }

    public DefaultFormatBase(NutsSession session, String name) {
        super(session, name);
    }

//    @Override
//    public PrintWriter getValidPrintWriter(Writer out) {
//        checkSession();
//        if(out == null){
//            return
//        }
//        return (out == null) ?
//                CoreIOUtils.toPrintWriter(getSession().getTerminal().getOut(), getSession())
//                :
//                CoreIOUtils.toPrintWriter(out, getSession());
//    }

//    @Override
//    public PrintWriter getValidPrintWriter() {
//        return getValidPrintWriter(null);
//    }

    @Override
    public NutsPrintStream getValidPrintStream(NutsPrintStream out) {
        checkSession();
        if (out == null) {
            out = getSession().getTerminal().getOut();
        }
        return out;
    }

    @Override
    public NutsPrintStream getValidPrintStream() {
        return getValidPrintStream(null);
    }

    @Override
    public NutsString format() {
        checkSession();
        NutsPrintStream out = NutsMemoryPrintStream.of(getSession());
        print(out);
        return isNtf() ?
                NutsTexts.of(getSession()).parse(out.toString())
                :
                NutsTexts.of(getSession()).ofPlain(out.toString())
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
    public abstract void print(NutsPrintStream out);

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
            NutsPrintStream pout = getValidPrintStream();
            print(pout);
            pout.flush();
        } else {
            NutsPrintStream pout = NutsPrintStream.of(out, getSession());
            print(pout);
            pout.flush();
        }
    }

    @Override
    public void print(OutputStream out) {
        checkSession();
        NutsPrintStream p =
                out == null ? getValidPrintStream() :
                        NutsPrintStream.of(out, getSession());
        print(p);
        p.flush();
    }

    @Override
    public void print(Path path) {
        checkSession();
        print(NutsPath.of(path, getSession()));
    }

    @Override
    public void print(NutsPath path) {
        checkSession();
        path.mkParentDirs();
        try (Writer w = path.getWriter()) {
            print(w);
        } catch (IOException ex) {
            throw new NutsIOException(getSession(), ex);
        }
    }

    @Override
    public void print(File file) {
        print(NutsPath.of(file,getSession()));
    }

    @Override
    public void print(NutsSessionTerminal terminal) {
        checkSession();
        print(terminal == null ? getSession().getTerminal().out() : terminal.out());
    }

    @Override
    public void println(Writer w) {
        checkSession();
        if (w == null) {
            NutsPrintStream pout = getValidPrintStream();
            println(pout);
            pout.flush();
        } else {
            NutsPrintStream pout = NutsPrintStream.of(w, getSession());
            println(pout);
            pout.flush();
        }
    }

    @Override
    public void println(NutsPrintStream out) {
        checkSession();
        NutsPrintStream p = getValidPrintStream(out);
        print(out);
        p.println();
        p.flush();
    }

    @Override
    public void println(OutputStream out) {
        checkSession();
        if (out == null) {
            NutsPrintStream pout = getValidPrintStream();
            println(pout);
            pout.flush();
        } else {
            NutsPrintStream pout = NutsPrintStream.of(out, getSession());
            println(pout);
            pout.flush();
        }
    }

    @Override
    public void println(Path path) {
        checkSession();
        println(NutsPath.of(path,getSession()));
    }

    @Override
    public void println(NutsPath out) {
        checkSession();
        out.mkParentDirs();
        try (Writer w = out.getWriter()) {
            println(w);
        } catch (IOException ex) {
            throw new NutsIOException(getSession(), ex);
        }
    }

    @Override
    public void println(NutsSessionTerminal terminal) {
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

    @Override
    public void configureLast(NutsCommandLine commandLine) {
        if (!configureFirst(commandLine)) {
            commandLine.throwUnexpectedArgument();
        }
    }

}
