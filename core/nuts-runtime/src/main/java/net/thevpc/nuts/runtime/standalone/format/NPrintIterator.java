/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format;

import java.util.Iterator;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NIterableFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.util.NIteratorBase;

/**
 *
 * @author thevpc
 */
public class NPrintIterator<T> extends NIteratorBase<T> {

    Iterator<T> curr;
    NWorkspace ws;
    NIterableFormat listFormat;
    NPrintStream out;
    NFetchDisplayOptions displayOptions;
    long count = 0;

    public NPrintIterator(Iterator<T> curr, NWorkspace ws, NPrintStream out, NFetchDisplayOptions displayOptions) {
        this.curr = curr;
        this.ws = ws;
        this.out = out;
        this.listFormat = NSession.of().getIterableOutput();
        this.displayOptions = displayOptions;
        if (this.listFormat == null) {
            this.listFormat = NElements.of().setContentType(NSession.of().getOutputFormat().orDefault()).iter(out);
        }
        this.listFormat
                .configure(true, displayOptions.toCmdLineOptions())
                ;
    }

    @Override
    public NElement describe() {
        return NEDesc.describeResolveOrDestructAsObject(curr)
                .builder()
                .set("print", NElements.of().ofObject().set("format",listFormat.getOutputFormat().id()).build())
                .build();
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
