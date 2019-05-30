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
import net.vpc.app.nuts.NutsIncrementalFormat;

/**
 *
 * @author vpc
 */
public class NutsTraceIterator<T> implements Iterator<T> {

    Iterator<T> curr;
    NutsWorkspace ws;
    NutsIncrementalFormat listFormat;
    PrintStream out;
    NutsOutputFormat format;
    NutsFetchDisplayOptions displayOptions;
    long count = 0;

    public NutsTraceIterator(Iterator<T> curr, NutsWorkspace ws, PrintStream out, NutsOutputFormat format, NutsIncrementalFormat conv, NutsFetchDisplayOptions displayOptions, NutsSession session) {
        this.curr = curr;
        this.ws = ws;
        this.out = out;
        this.listFormat = conv;
        this.format = format;
        this.displayOptions = displayOptions;
        if (conv == null) {
            this.listFormat = ws.formatter().createIncrementalFormat(format);
            this.listFormat.configure(ws.parser().parseCommand(displayOptions.toCommandLineOptions()), true);
        }
        this.listFormat.session(session).out(out);
    }

    @Override
    public boolean hasNext() {
        boolean p = curr.hasNext();
        if (!p) {
            listFormat.formatComplete(count);
        }
        return p;
    }

    @Override
    public T next() {
        T n = curr.next();
        if (count == 0) {
            listFormat.formatStart();
        }
        listFormat.formatNext(n, count);
        count++;
        return n;
    }

}
