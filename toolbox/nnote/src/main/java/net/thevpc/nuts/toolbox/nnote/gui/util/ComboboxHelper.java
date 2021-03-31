/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.gui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import net.thevpc.common.swing.ExtendedComboBoxModel;
import net.thevpc.common.swing.NamedValue;
import net.thevpc.echo.Application;

/**
 *
 * @author vpc
 */
public class ComboboxHelper {

    public static JComboBox createCombobox(Application app, NamedValue... values) {
        JComboBox c = new JComboBox(new ExtendedComboBoxModel(values));
        c.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setIcon(null);
                } else {
                    NamedValue nv = (NamedValue) value;
                    if (nv.isGroup()) {
                        JLabel label = new JLabel(nv.getName());
                        Font f = label.getFont();
                        Color fc = label.getForeground();
                        Color bc = label.getBackground();
                        label.setOpaque(true);
                        label.setBackground(fc);
                        label.setForeground(bc);
                        label.setFont(f.deriveFont(f.getStyle() | Font.BOLD | Font.ITALIC));
                        return label;
                        //                        super.getListCellRendererComponent(list, nv.name, index, false, false);
                        //                        setIcon(null);
                    } else {
                        super.getListCellRendererComponent(list, nv.getName(), index, isSelected, cellHasFocus);
                        if (value instanceof NamedValue) {
                            String icon = ((NamedValue) value).getIcon();
                            setIcon((icon != null && icon.length() > 0) ? app.iconSet().icon(icon).get() : null);
                        } else {
                            setIcon(null);
                        }
                    }
                }
                return this;
            }
        });
        return c;
    }

}
