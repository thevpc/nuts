/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

/**
 *
 * @author thevpc
 */
public class JShellNodeUnsafeRunnable implements UnsafeRunnable {
    
    private final JShellCommandNode left;
    private final JShellFileContext context;

    public JShellNodeUnsafeRunnable(JShellCommandNode left, JShellFileContext context) {
        this.left = left;
        this.context = context;
    }

    @Override
    public void run() throws Exception {
        left.eval(context);
    }
    
}
