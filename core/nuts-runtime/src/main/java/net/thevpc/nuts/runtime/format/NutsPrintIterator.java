/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.format;

import java.io.PrintStream;
import java.util.Iterator;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsIterableOutput;

/**
 *
 * @author thevpc
 */
public class NutsPrintIterator<T> implements Iterator<T> {

    Iterator<T> curr;
    NutsWorkspace ws;
    NutsIterableOutput listFormat;
    PrintStream out;
    NutsFetchDisplayOptions displayOptions;
    long count = 0;

    public NutsPrintIterator(Iterator<T> curr, NutsWorkspace ws, PrintStream out, NutsFetchDisplayOptions displayOptions, NutsSession session) {
        this.curr = curr;
        this.ws = ws;
        this.out = out;
        this.listFormat = session.getIterableOutput();
        this.displayOptions = displayOptions;
        if (this.listFormat == null) {
            this.listFormat = ws.formats().iter();
        }
        this.listFormat
                .setSession(session)
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
