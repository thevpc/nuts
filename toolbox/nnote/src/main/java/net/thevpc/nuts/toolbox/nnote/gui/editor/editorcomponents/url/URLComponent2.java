/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.editor.editorcomponents.url;

import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import net.thevpc.common.swing.GridBagLayoutSupport;
import net.thevpc.echo.Application;
import net.thevpc.nuts.toolbox.nnote.gui.util.AnyDocumentListener;

/**
 *
 * @author vpc
 */
public class URLComponent2 extends JPanel {

    private JTextField textField = new JTextField();
    private JButton show;
    private Application app;
    private List<UrlChangedListener> listeners = new ArrayList<>();

    public URLComponent2() {
        JButton showPassword = new JButton("...");
        showPassword.addActionListener((e)
                -> {
            onUrlChanged();
        }
        );
        textField.getDocument().addDocumentListener(new AnyDocumentListener() {
            @Override
            public void anyChange(DocumentEvent e) {
                onUrlChanged();
            }
        });
        new GridBagLayoutSupport("[pwd-===][check] ; insets(2)")
                .bind("pwd", textField)
                .bind("check", showPassword)
                .apply(this);
    }

    public void addListener(UrlChangedListener li) {
        listeners.add(li);
    }

    private void onUrlChanged() {
        for (UrlChangedListener listener : listeners) {
            listener.onUrlChange(getContentString());
        }
    }

    public String getContentString() {
        return textField.getText();
    }

    public URLComponent2 setValue(String s) {
        textField.setText(s);
        return this;
    }

    public void uninstall() {
    }

    public void install(Application app) {
    }

    public interface UrlChangedListener {

        void onUrlChange(String newURL);
    }

    public void setEditable(boolean b) {
        textField.setEditable(b);
    }

    public boolean isEditable() {
        return textField.isEditable();
    }

}
