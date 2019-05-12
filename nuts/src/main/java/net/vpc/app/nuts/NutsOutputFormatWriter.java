/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

import java.io.PrintStream;
import java.io.Writer;

/**
 *
 * @author vpc
 */
public interface NutsOutputFormatWriter {

    boolean configure(NutsCommandLine commandLine, boolean skipIgnored);
    
    boolean configureFirst(NutsCommandLine commandLine);

    void write(Writer w);

    void write(PrintStream w);

}
