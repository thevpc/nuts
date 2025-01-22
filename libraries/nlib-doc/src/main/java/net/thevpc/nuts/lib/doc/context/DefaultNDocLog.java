/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.doc.context;

import java.io.PrintStream;

/**
 *
 * @author thevpc
 */
public class DefaultNDocLog implements NDocLog {
    public static final NDocLog INSTANCE=new DefaultNDocLog();
    
    public DefaultNDocLog() {
    }

    @Override
    public void info(String title, String message) {
        out().println("[info ] "+title + ": " + message);
    }

    protected PrintStream err() {
        return System.err;
    }

    protected PrintStream out() {
        return System.out;
    }

    @Override
    public void error(String title, String message) {
        err().println("[error] "+title + ": " + message);
    }

    @Override
    public void warn(String title, String message) {
        err().println("[warn] "+title + ": " + message);
    }

    @Override
    public void debug(String title, String message) {
        out().println("[debug] "+title + ": " + message);
    }
    
}
