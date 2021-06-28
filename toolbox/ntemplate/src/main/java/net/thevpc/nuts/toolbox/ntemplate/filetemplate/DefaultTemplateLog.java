/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ntemplate.filetemplate;

import java.io.PrintStream;

/**
 *
 * @author thevpc
 */
public class DefaultTemplateLog implements TemplateLog {
    public static final TemplateLog INSTANCE=new DefaultTemplateLog();
    
    public DefaultTemplateLog() {
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
    public void debug(String title, String message) {
        out().println("[debug] "+title + ": " + message);
    }
    
}
