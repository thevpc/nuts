/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format;

import java.util.Iterator;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.iter.IterInfoNode;
import net.thevpc.nuts.runtime.bundles.iter.IterInfoNodeAware2Base;
//import net.thevpc.nuts.NutsIterableOutput;

/**
 *
 * @author thevpc
 */
public class NutsPrintIterator<T> extends IterInfoNodeAware2Base<T> {

    Iterator<T> curr;
    NutsWorkspace ws;
    NutsIterableFormat listFormat;
    NutsPrintStream out;
    NutsFetchDisplayOptions displayOptions;
    long count = 0;

    public NutsPrintIterator(Iterator<T> curr, NutsWorkspace ws, NutsPrintStream out, NutsFetchDisplayOptions displayOptions, NutsSession session) {
        this.curr = curr;
        this.ws = ws;
        this.out = out;
        this.listFormat = session.getIterableOutput();
        this.displayOptions = displayOptions;
        if (this.listFormat == null) {
            this.listFormat = NutsElements.of(session).setContentType(session.getOutputFormat()).iter(out);
        }
        this.listFormat
                .configure(true, displayOptions.toCommandLineOptions())
                ;
    }

    @Override
    public IterInfoNode info(NutsSession session) {
        return info("Print",
                IterInfoNode.resolveOrNull("base", curr, session),
                IterInfoNode.resolveOrString("format", listFormat.getOutputFormat().toString(), session)
        );
    }

    @Override
    public boolean hasNext() {
        boolean p = curr.hasNext();
        if (!p) {
            listFormat.complete(count);
        }
        return p;
    }

    @Override
    public T next() {
        T n = curr.next();
        if (count == 0) {
            listFormat.start();
        }
        listFormat.next(n,count);
        count++;
        return n;
    }

}
