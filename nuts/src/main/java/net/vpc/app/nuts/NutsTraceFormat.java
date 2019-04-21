/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.io.PrintStream;

/**
 *
 * @author vpc
 */
public interface NutsTraceFormat {

    public NutsOutputFormat getSupportedFormat();
    
    public void formatStart(PrintStream out, NutsWorkspace ws);

    public void formatElement(Object object, long index, PrintStream out, NutsWorkspace ws);

    public void formatEnd(long count, PrintStream out, NutsWorkspace ws);
}
