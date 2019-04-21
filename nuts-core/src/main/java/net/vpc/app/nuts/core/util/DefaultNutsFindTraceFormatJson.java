/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import java.io.PrintStream;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsTraceFormat;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public class DefaultNutsFindTraceFormatJson implements NutsTraceFormat {

    CanonicalBuilder canonicalBuilder;

    public DefaultNutsFindTraceFormatJson() {
    }

    @Override
    public NutsOutputFormat getSupportedFormat() {
        return NutsOutputFormat.JSON;
    }
    

    @Override
    public void formatStart(PrintStream out, NutsWorkspace ws) {
        out.println("[");
    }

    @Override
    public void formatElement(Object object, long index, PrintStream out, NutsWorkspace ws) {
        if (index > 0) {
            out.print(", ");
        }
        out.printf("%N%n", ws.io().toJsonString(object, true));
        out.flush();
    }

    @Override
    public void formatEnd(long count, PrintStream out, NutsWorkspace ws) {
        out.println("]");
    }

}
