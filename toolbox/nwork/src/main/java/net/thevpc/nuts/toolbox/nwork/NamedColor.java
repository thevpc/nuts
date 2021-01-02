package net.thevpc.nuts.toolbox.nwork;

import java.awt.*;

public class NamedColor {
    private String name;
    private Color color;

    public NamedColor(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }
}
