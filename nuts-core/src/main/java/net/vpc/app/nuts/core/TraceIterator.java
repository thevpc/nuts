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
import net.vpc.app.nuts.NutsUnsupportedOperationException;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.DefaultNutsFindTraceFormatJson;
import net.vpc.app.nuts.core.util.DefaultNutsFindTraceFormatPlain;
import net.vpc.app.nuts.core.util.DefaultNutsFindTraceFormatProps;
import net.vpc.app.nuts.NutsOutputCustomFormat;

/**
 *
 * @author vpc
 */
public class TraceIterator<T> implements Iterator<T> {

    Iterator<T> curr;
    NutsWorkspace ws;
    NutsOutputCustomFormat conv;
    PrintStream out;
    NutsOutputFormat format;
    NutsFindCommand findCommand;
    long count = 0;

    public TraceIterator(Iterator<T> curr, NutsWorkspace ws, PrintStream out, NutsOutputFormat format, NutsOutputCustomFormat conv, NutsFindCommand findCommand, NutsSession session) {
        this.curr = curr;
        this.ws = ws;
        this.out = out;
        this.conv = conv;
        this.format = format;
        this.findCommand = findCommand;
        if (this.conv == null) {
            switch (this.format) {
                case JSON: {
                    this.conv = new DefaultNutsFindTraceFormatJson();
                    break;
                }
                case PROPS: {
                    this.conv = new DefaultNutsFindTraceFormatProps();
                    break;
                }
                case PLAIN: {
                    this.conv = new DefaultNutsFindTraceFormatPlain(findCommand, session);
                    break;
                }
                default: {
                    throw new NutsUnsupportedOperationException("Unsupported " + format);
                }
            }
        }
    }

    @Override
    public boolean hasNext() {
        boolean p = curr.hasNext();
        if (!p) {
            conv.formatEnd(count, out, ws);
        }
        return p;
    }

    @Override
    public T next() {
        T n = curr.next();
        if (count == 0) {
            conv.formatStart(out, ws);
        }
        conv.formatElement(n, count, out, ws);
        count++;
        return n;
    }

}
