/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import java.io.PrintStream;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsTraceFormat;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public class DefaultNutsFindTraceFormatPlain implements NutsTraceFormat {

    public static final NutsTraceFormat INSTANCE = new DefaultNutsFindTraceFormatPlain();

    @Override
    public void formatStart(PrintStream out, NutsWorkspace ws) {
    }

    @Override
    public NutsOutputFormat getSupportedFormat() {
        return NutsOutputFormat.PLAIN;
    }

    @Override
    public void formatElement(Object object, long index, PrintStream out, NutsWorkspace ws) {
        if (object instanceof NutsId) {
            out.printf("%N%n", NutsWorkspaceUtils.getIdFormat(ws).toString((NutsId) object));
        } else if (object instanceof NutsDescriptor) {
            out.printf("%N%n", NutsWorkspaceUtils.getIdFormat(ws).toString((NutsId) object));
        } else {
            out.printf("%N%n", object);
        }
        out.flush();
    }

    @Override
    public void formatEnd(long count, PrintStream out, NutsWorkspace ws) {

    }

}
