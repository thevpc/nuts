/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import java.io.PrintStream;
import java.io.PrintWriter;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsOutputFormatWriter;

/**
 *
 * @author vpc
 */
public abstract class NutsOutputFormatWriterBase implements NutsOutputFormatWriter {

    @Override
    public final boolean configure(NutsCommandLine commandLine, boolean skipIgnored) {
        boolean conf = false;
        while (commandLine.hasNext()) {
            if (!configure(commandLine, false)) {
                if (skipIgnored) {
                    commandLine.skip();
                } else {
                    commandLine.unexpectedArgument();
                }
            } else {
                conf = true;
            }
        }
        return conf;
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return false;
    }

    @Override
    public void write(PrintStream w) {
        PrintWriter pw = new PrintWriter(w);
        write(pw);
    }
}
