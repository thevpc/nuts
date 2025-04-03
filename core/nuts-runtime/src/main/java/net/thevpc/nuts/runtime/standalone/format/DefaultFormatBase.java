/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.text.NText;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;

/**
 * @author thevpc
 */
public abstract class DefaultFormatBase<T extends NFormat> extends DefaultFormatBase0<T> implements NFormat {

    public DefaultFormatBase(String name) {
        super(name);
    }
    
    @Override
    public NPrintStream getValidPrintStream(NPrintStream out) {
        if (out == null) {
            out = NOut.out();
        }
        return out;
    }

    @Override
    public NPrintStream getValidPrintStream() {
        return getValidPrintStream(null);
    }

    @Override
    public NText format() {
        if (isNtf()) {
            NPrintStream out = NMemoryPrintStream.of(NTerminalMode.FORMATTED);
            print(out);
            return NText.of(out.toString());
        } else {
            NPrintStream out = NMemoryPrintStream.of(NTerminalMode.INHERITED);
            print(out);
            return NText.ofPlain(out.toString());
        }
    }

    @Override
    public String formatPlain() {
        boolean ntf = isNtf();
        try {
            NPrintStream out = NPrintStream.ofMem(NTerminalMode.INHERITED);
            print(out);
            return out.toString();
        } finally {
            setNtf(ntf);
        }
    }

    @Override
    public void print() {
        NSession session=NSession.of();
        print(session.getTerminal());
    }

    @Override
    public void println() {
        NSession session=NSession.of();
        println(session.getTerminal());
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
        if (out == null) {
            NPrintStream pout = getValidPrintStream();
            print(pout);
            pout.flush();
        } else {
            NPrintStream pout = NPrintStream.of(out);
            print(pout);
            pout.flush();
        }
    }

    @Override
    public void print(OutputStream out) {
        NPrintStream p =
                out == null ? getValidPrintStream() :
                        NPrintStream.of(out);
        print(p);
        p.flush();
    }

    @Override
    public void print(Path path) {
        print(NPath.of(path));
    }

    @Override
    public void print(NPath path) {
        path.mkParentDirs();
        try (Writer w = path.getWriter()) {
            print(w);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    @Override
    public void print(File file) {
        print(NPath.of(file));
    }

    @Override
    public void print(NTerminal terminal) {
        NSession session=NSession.of();
        print(terminal == null ? session.getTerminal().out() : terminal.out());
    }

    @Override
    public void println(Writer w) {
        if (w == null) {
            NPrintStream pout = getValidPrintStream();
            println(pout);
            pout.flush();
        } else {
            NPrintStream pout = NPrintStream.of(w);
            println(pout);
            pout.flush();
        }
    }

    @Override
    public void println(NPrintStream out) {
        NPrintStream p = getValidPrintStream(out);
        print(out);
        p.println();
        p.flush();
    }

    @Override
    public void println(OutputStream out) {
        if (out == null) {
            NPrintStream pout = getValidPrintStream();
            println(pout);
            pout.flush();
        } else {
            NPrintStream pout = NPrintStream.of(out);
            println(pout);
            pout.flush();
        }
    }

    @Override
    public void println(Path path) {
        println(NPath.of(path));
    }

    @Override
    public void println(NPath out) {
        out.mkParentDirs();
        try (Writer w = out.getWriter()) {
            println(w);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    @Override
    public void println(NTerminal terminal) {
        NSession session=NSession.of();
        println(terminal == null ? session.getTerminal().out() : terminal.out());
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
