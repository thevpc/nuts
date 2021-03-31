/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.tree;

import java.awt.Component;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 *
 * @author vpc
 */
class SecureJFileChooserImpl extends JFileChooser {
    
    private JCheckBox secureChekbox = new JCheckBox("secure file");
    private final NNoteDocumentTree outer;

    public SecureJFileChooserImpl(final NNoteDocumentTree outer) {
        this.outer = outer;
        JPanel panel1 = (JPanel) this.getComponent(3);
        JPanel panel2 = (JPanel) panel1.getComponent(3);
        Component c1 = panel2.getComponent(0);
        Component c2 = panel2.getComponent(1);
        panel2.removeAll();
        panel2.add(Box.createHorizontalGlue());
        panel2.add(secureChekbox);
        panel2.add(c1);
        panel2.add(c2);
    }

    public JCheckBox getSecureCheckbox() {
        return secureChekbox;
    }
    
}
