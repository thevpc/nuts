/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.thevpc.nuts.toolbox.nnote.util.OtherUtils;

/**
 *
 * @author vpc
 */
public class URLViewer extends JPanel {

    private JScrollPane scroll;
    private JComponent base;
    private JEditorPane pane;

    public URLViewer() {
        super(new BorderLayout());
        pane = new JEditorPane();
        pane.setEditable(false);
        base = pane;
        scroll = new JScrollPane(base);
        add(scroll);
    }

    public void resetContent() {
        pane.setText("");
    }

    public void load(URL url) {
        if (url == null) {
            pane.setText("");
        }
        InputStream is = null;
        try {
            try {
                is = url.openStream();
                if (is == null) {
                    pane.setText("");
                } else {
                    pane.setText("");
                    pane.setText(new String(OtherUtils.toByteArray(is)));
                }
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } catch (IOException ex) {
            pane.setText("");
            pane.setText(new String(ex.toString()));
        }
    }
}
