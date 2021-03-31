/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import net.thevpc.echo.Application;

/**
 *
 * @author vpc
 */
public class TextAreaComponent extends JPanel implements FormComponent {

    private JTextArea jtf = new JTextArea();
    private AnyDocumentListener listener;

    public TextAreaComponent() {
        super(new BorderLayout());
        add(new JScrollPane(jtf));
        setPreferredSize(new Dimension(200, 100));
        setMinimumSize(new Dimension(100, 100));
    }

    @Override
    public String getContentString() {
        return jtf.getText();
    }

    @Override
    public void setContentString(String s) {
        jtf.setText(s);
    }

    public void uninstall() {
        if (listener != null) {
            jtf.getDocument().removeDocumentListener(listener);
            listener = null;
        }
    }

    public void install(Application app) {
    }

    @Override
    public void setFormChangeListener(Runnable callback) {
        if (listener == null) {
            listener = new AnyDocumentListener() {
                @Override
                public void anyChange(DocumentEvent e) {
                    callback.run();
                }
            };
            jtf.getDocument().addDocumentListener(listener);
        }
    }
}
