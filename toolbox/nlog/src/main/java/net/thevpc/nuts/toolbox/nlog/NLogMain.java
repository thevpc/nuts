/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nlog;

import net.thevpc.nuts.*;

/**
 * @author thevpc
 */
public class NLogMain implements NApplication {
    public static void main(String[] args) {
        new NLogMain().runAndExit(args);
    }

    @Override
    public void run(NApplicationContext applicationContext) {
        applicationContext.processCommandLine(new NLogMainCmdProcessor(applicationContext));
    }

}