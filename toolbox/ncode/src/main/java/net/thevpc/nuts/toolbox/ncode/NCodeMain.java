/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ncode;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;

/**
 * @author thevpc
 */
public class NCodeMain implements NApplication {
    public static void main(String[] args) {
        new NCodeMain().runAndExit(args);
    }

    @Override
    public void run() {
        NSession session = NSession.of().get();
        NApp.of().processCmdLine(new NCodeMainCmdProcessor(session));
    }

}
