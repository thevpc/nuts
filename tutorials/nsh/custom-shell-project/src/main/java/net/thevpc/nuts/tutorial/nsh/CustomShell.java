/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.tutorial.nsh;

import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.toolbox.nsh.jshell.JShell;

/**
 *
 * @author vpc
 */
public class CustomShell implements NApplication {

    public static void main(String[] args) {
        new CustomShell().runAndExit(args);
    }

    @Override
    public void run(NApplicationContext nac) {
        JShell nsh = new JShell(nac);
        nsh.run();
    }

}
