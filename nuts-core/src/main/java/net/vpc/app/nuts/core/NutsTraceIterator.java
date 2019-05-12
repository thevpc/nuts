/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.io.PrintStream;
import java.util.Iterator;
import net.vpc.app.nuts.NutsFindCommand;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsOutputListFormat;

/**
 *
 * @author vpc
 */
public class NutsTraceIterator<T> implements Iterator<T> {

    Iterator<T> curr;
    NutsWorkspace ws;
    NutsOutputListFormat listFormat;
    PrintStream out;
    NutsOutputFormat format;
    NutsFindCommand findCommand;
    long count = 0;

    public NutsTraceIterator(Iterator<T> curr, NutsWorkspace ws, PrintStream out, NutsOutputFormat format, NutsOutputListFormat conv, NutsFindCommand findCommand, NutsSession session) {
        this.curr = curr;
        this.ws = ws;
        this.out = out;
        this.listFormat = conv;
        this.format = format;
        this.findCommand = findCommand;
        this.listFormat = conv != null ? conv : ws.formatter().createOutputListFormat(format)
                .setOption("long", String.valueOf(((DefaultNutsFindCommand) findCommand).isLongFormat()));
        this.listFormat.session(session).out(out);
    }

    @Override
    public boolean hasNext() {
        boolean p = curr.hasNext();
        if (!p) {
            listFormat.formatEnd(count);
        }
        return p;
    }

    @Override
    public T next() {
        T n = curr.next();
        if (count == 0) {
            listFormat.formatStart();
        }
        listFormat.formatElement(n, count);
        count++;
        return n;
    }

}
