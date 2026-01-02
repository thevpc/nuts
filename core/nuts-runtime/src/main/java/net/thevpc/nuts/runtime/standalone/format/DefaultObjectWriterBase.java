/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.text.NObjectWriter;
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
public abstract class DefaultObjectWriterBase<T extends NObjectWriter> extends DefaultFormatBase0<T> implements NObjectWriter {

    public DefaultObjectWriterBase(String name) {
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
    public NText format(Object aValue) {
        if (isNtf()) {
            NPrintStream out = NMemoryPrintStream.of(NTerminalMode.FORMATTED);
            print(aValue, out);
            return NText.of(out.toString());
        } else {
            NPrintStream out = NMemoryPrintStream.of(NTerminalMode.INHERITED);
            print(aValue, out);
            return NText.ofPlain(out.toString());
        }
    }

    @Override
    public String formatPlain(Object aValue) {
        boolean ntf = isNtf();
        try {
            NPrintStream out = NPrintStream.ofMem(NTerminalMode.INHERITED);
            print(aValue, out);
            return out.toString();
        } finally {
            setNtf(ntf);
        }
    }

    @Override
    public void print(Object aValue) {
        NSession session=NSession.of();
        print(aValue, session.getTerminal());
    }

    @Override
    public void println(Object aValue) {
        NSession session=NSession.of();
        println(aValue, session.getTerminal());
    }

    @Override
    public abstract void print(Object aValue, NPrintStream out);

    //    @Override
//    public void print(PrintStream out) {
//        PrintWriter p = out == null ? null : new ExtendedFormatAwarePrintWriter(out);
//        print(p);
//        if (p != null) {
//            p.flush();
//        }
//    }
    @Override
    public void print(Object aValue, Writer out) {
        if (out == null) {
            NPrintStream pout = getValidPrintStream();
            print(aValue, pout);
            pout.flush();
        } else {
            NPrintStream pout = NPrintStream.of(out);
            print(aValue, pout);
            pout.flush();
        }
    }

    @Override
    public void print(Object aValue, OutputStream out) {
        NPrintStream p =
                out == null ? getValidPrintStream() :
                        NPrintStream.of(out);
        print(aValue, p);
        p.flush();
    }

    @Override
    public void print(Object aValue, Path path) {
        print(aValue, NPath.of(path));
    }

    @Override
    public void print(Object aValue, NPath path) {
        path.mkParentDirs();
        try (Writer w = path.getWriter()) {
            print(aValue, w);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    @Override
    public void print(Object aValue, File file) {
        print(aValue, NPath.of(file));
    }

    @Override
    public void print(Object aValue, NTerminal terminal) {
        NSession session=NSession.of();
        print(aValue, terminal == null ? session.getTerminal().out() : terminal.out());
    }

    @Override
    public void println(Object aValue, Writer w) {
        if (w == null) {
            NPrintStream pout = getValidPrintStream();
            println(aValue, pout);
            pout.flush();
        } else {
            NPrintStream pout = NPrintStream.of(w);
            println(aValue, pout);
            pout.flush();
        }
    }

    @Override
    public void println(Object aValue, NPrintStream out) {
        NPrintStream p = getValidPrintStream(out);
        print(aValue, out);
        p.println();
        p.flush();
    }

    @Override
    public void println(Object aValue, OutputStream out) {
        if (out == null) {
            NPrintStream pout = getValidPrintStream();
            println(aValue, pout);
            pout.flush();
        } else {
            NPrintStream pout = NPrintStream.of(out);
            println(aValue, pout);
            pout.flush();
        }
    }

    @Override
    public void println(Object aValue, Path path) {
        println(aValue, NPath.of(path));
    }

    @Override
    public void println(Object aValue, NPath out) {
        out.mkParentDirs();
        try (Writer w = out.getWriter()) {
            println(aValue, w);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    @Override
    public void println(Object aValue, NTerminal terminal) {
        NSession session= NSession.of();
        println(aValue, terminal == null ? session.getTerminal().out() : terminal.out());
    }

    @Override
    public void println(Object aValue, File file) {
        println(aValue, file.toPath());
    }

    @Override
    public void write(Object aValue) {
        print(aValue);
    }

    @Override
    public void writeln(Object aValue) {
        println(aValue);
    }

    @Override
    public void write(Object aValue, NPrintStream out) {
        print(aValue,out);
    }

    @Override
    public void write(Object aValue, Writer out) {
        print(aValue,out);
    }

    @Override
    public void write(Object aValue, OutputStream out) {
        print(aValue,out);
    }

    @Override
    public void write(Object aValue, Path out) {
        print(aValue,out);
    }

    @Override
    public void write(Object aValue, NPath out) {
        print(aValue,out);
    }

    @Override
    public void write(Object aValue, File out) {
        print(aValue,out);
    }

    @Override
    public void write(Object aValue, NTerminal terminal) {
        print(aValue,terminal);
    }

    @Override
    public void writeln(Object aValue, Writer out) {
        println(aValue,out);

    }

    @Override
    public void writeln(Object aValue, NPrintStream out) {
        println(aValue,out);
    }

    @Override
    public void writeln(Object aValue, OutputStream out) {
        println(aValue,out);
    }

    @Override
    public void writeln(Object aValue, Path out) {
        println(aValue,out);
    }

    @Override
    public void writeln(Object aValue, NPath out) {
        println(aValue,out);
    }

    @Override
    public void writeln(Object aValue, NTerminal out) {
        println(aValue,out);
    }

    @Override
    public void writeln(Object aValue, File out) {
        println(aValue,out);
    }
}
