/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.text.NString;
import net.thevpc.nuts.text.NTexts;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;

/**
 * @author thevpc
 */
public abstract class DefaultFormatBase<T extends NFormat> extends DefaultFormatBase0<T> implements NFormat {

    public DefaultFormatBase(NWorkspace ws, String name) {
        super(ws, name);
    }

    public DefaultFormatBase(NSession session, String name) {
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
    public NPrintStream getValidPrintStream(NPrintStream out) {
        checkSession();
        if (out == null) {
            out = getSession().getTerminal().getOut();
        }
        return out;
    }

    @Override
    public NPrintStream getValidPrintStream() {
        return getValidPrintStream(null);
    }

    @Override
    public NString format() {
        checkSession();
        if (isNtf()) {
            NPrintStream out = NMemoryPrintStream.of(NTerminalMode.FORMATTED, getSession());
            print(out);
            return NTexts.of(getSession()).parse(out.toString());
        } else {
            NPrintStream out = NMemoryPrintStream.of(NTerminalMode.INHERITED, getSession());
            print(out);
            return NTexts.of(getSession()).ofPlain(out.toString());
        }
    }

    @Override
    public String formatPlain() {
        checkSession();
        boolean ntf = isNtf();
        try {
            NPrintStream out = NPrintStream.ofMem(NTerminalMode.INHERITED, getSession());
            print(out);
            return out.toString();
        } finally {
            setNtf(ntf);
        }
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
    public abstract void print(NPrintStream out);

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
            NPrintStream pout = getValidPrintStream();
            print(pout);
            pout.flush();
        } else {
            NPrintStream pout = NPrintStream.of(out, getSession());
            print(pout);
            pout.flush();
        }
    }

    @Override
    public void print(OutputStream out) {
        checkSession();
        NPrintStream p =
                out == null ? getValidPrintStream() :
                        NPrintStream.of(out, getSession());
        print(p);
        p.flush();
    }

    @Override
    public void print(Path path) {
        checkSession();
        print(NPath.of(path, getSession()));
    }

    @Override
    public void print(NPath path) {
        checkSession();
        path.mkParentDirs();
        try (Writer w = path.getWriter()) {
            print(w);
        } catch (IOException ex) {
            throw new NIOException(getSession(), ex);
        }
    }

    @Override
    public void print(File file) {
        print(NPath.of(file, getSession()));
    }

    @Override
    public void print(NSessionTerminal terminal) {
        checkSession();
        print(terminal == null ? getSession().getTerminal().out() : terminal.out());
    }

    @Override
    public void println(Writer w) {
        checkSession();
        if (w == null) {
            NPrintStream pout = getValidPrintStream();
            println(pout);
            pout.flush();
        } else {
            NPrintStream pout = NPrintStream.of(w, getSession());
            println(pout);
            pout.flush();
        }
    }

    @Override
    public void println(NPrintStream out) {
        checkSession();
        NPrintStream p = getValidPrintStream(out);
        print(out);
        p.println();
        p.flush();
    }

    @Override
    public void println(OutputStream out) {
        checkSession();
        if (out == null) {
            NPrintStream pout = getValidPrintStream();
            println(pout);
            pout.flush();
        } else {
            NPrintStream pout = NPrintStream.of(out, getSession());
            println(pout);
            pout.flush();
        }
    }

    @Override
    public void println(Path path) {
        checkSession();
        println(NPath.of(path, getSession()));
    }

    @Override
    public void println(NPath out) {
        checkSession();
        out.mkParentDirs();
        try (Writer w = out.getWriter()) {
            println(w);
        } catch (IOException ex) {
            throw new NIOException(getSession(), ex);
        }
    }

    @Override
    public void println(NSessionTerminal terminal) {
        checkSession();
        println(terminal == null ? getSession().getTerminal().out() : terminal.out());
    }

    @Override
    public void println(File file) {
        println(file.toPath());
    }

    @Override
    public String toString() {
        return formatPlain();
    }

}
