/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 *
 * @author vpc
 */
public interface NutsOutputListFormat {

    public NutsOutputListFormat out(PrintStream out);

    public NutsOutputListFormat setOut(PrintStream out);

    public NutsOutputListFormat out(PrintWriter out);

    public NutsOutputListFormat setOut(PrintWriter out);

    public NutsOutputListFormat session(NutsSession session);

    public NutsOutputListFormat setSession(NutsSession session);

    public NutsOutputFormat getSupportedFormat();
    
    public NutsOutputListFormat setOption(String name,String value);

    public void formatStart();

    public void formatElement(Object object, long index);

    public void formatEnd(long count);
}
