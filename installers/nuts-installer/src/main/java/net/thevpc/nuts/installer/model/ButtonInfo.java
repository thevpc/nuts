package net.thevpc.nuts.installer.model;

import net.thevpc.nuts.installer.model.VerInfo;

import javax.swing.*;
import java.awt.*;

public class ButtonInfo {
    public String text;
    public String html;
    public VerInfo verInfo;
    public Color bg;
    public Color bg2;

    public ButtonInfo(String text, String html, Color bg, Color bg2) {
        this.text = text;
        this.html = html;
        this.bg = bg;
        this.bg2 = bg2;
    }

    private void apply(JToggleButton jtb) {
        JToggleButton a = new JToggleButton(text);
        a.setBackground(bg);
    }
}
