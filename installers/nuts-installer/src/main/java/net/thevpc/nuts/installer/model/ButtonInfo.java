package net.thevpc.nuts.installer.model;

import javax.swing.*;
import java.awt.*;

public class ButtonInfo {
    public String text;
    public String html;
    public VerInfo verInfo;
    public Color bg;
    public Color bg2;
    public ImageIcon iconNonSelected;
    public ImageIcon iconSelected;

    public static ButtonInfo of(JComponent component) {
        return (ButtonInfo) component.getClientProperty("ButtonInfo");
    }

    public ButtonInfo(String text, String html, Color bg, Color bg2, ImageIcon iconNonSelected, ImageIcon iconSelected) {
        this.text = text;
        this.html = html;
        this.bg = bg;
        this.bg2 = bg2;
        this.iconNonSelected = iconNonSelected;
        this.iconSelected = iconSelected;
    }

    public void bind(JToggleButton s) {
        s.putClientProperty("ButtonInfo", this);
        applyButtonInfo(s);
    }

    public void applyButtonInfo(JToggleButton s) {
        s.setForeground(Color.BLACK);
        s.setBackground(this.bg);
        s.setIcon(this.iconNonSelected);
        s.setSelectedIcon(this.iconSelected);
        s.setText(this.text);
    }
}
