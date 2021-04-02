/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import net.thevpc.echo.Application;

/**
 *
 * @author vpc
 */
public class ComboboxComponent extends JPanel implements FormComponent {

    private JComboBox cb = new JComboBox();
    private Runnable callback;
    private boolean editable = true;

    public ComboboxComponent() {
        super(new BorderLayout());
        add(cb);

        cb.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                callOnValueChanged();
            }
        });

    }

    private void callOnValueChanged() {
        if (callback != null) {
            callback.run();
        }
    }

    public void setSelectValues(List<String> values) {
        cb.setModel(new DefaultComboBoxModel(values.toArray()));
    }

    @Override
    public String getContentString() {
        return (String) cb.getSelectedItem();
    }

    @Override
    public void setContentString(String s) {
        cb.setSelectedItem(s);
    }

    public void uninstall() {
        callback = null;
    }

    public void install(Application app) {
    }

    @Override
    public void setFormChangeListener(Runnable callback) {
        this.callback = callback;
    }

    @Override
    public void setEditable(boolean b) {
        this.editable = b;
        cb.setEnabled(b);
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

}
