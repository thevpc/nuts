/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import java.io.PrintStream;
import java.util.Iterator;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsIterableOutput;

/**
 *
 * @author vpc
 */
public class NutsTraceIterator<T> implements Iterator<T> {

    Iterator<T> curr;
    NutsWorkspace ws;
    NutsIterableOutput listFormat;
    PrintStream out;
    NutsFetchDisplayOptions displayOptions;
    long count = 0;

    public NutsTraceIterator(Iterator<T> curr, NutsWorkspace ws, PrintStream out, NutsFetchDisplayOptions displayOptions, NutsSession session) {
        this.curr = curr;
        this.ws = ws;
        this.out = out;
        this.listFormat = session.getIterableOutput();
        this.displayOptions = displayOptions;
        if (this.listFormat == null) {
            this.listFormat = ws.iter();
        }
        this.listFormat
                .session(session)
                .configure(true, displayOptions.toCommandLineOptions())
                .out(out);
    }

    @Override
    public boolean hasNext() {
        boolean p = curr.hasNext();
        if (!p) {
            listFormat.complete();
        }
        return p;
    }

    @Override
    public T next() {
        T n = curr.next();
        if (count == 0) {
            listFormat.start();
        }
        listFormat.next(n);
        count++;
        return n;
    }

}
