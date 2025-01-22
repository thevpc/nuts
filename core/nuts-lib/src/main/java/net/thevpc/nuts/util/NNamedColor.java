package net.thevpc.nuts.util;

import java.awt.*;

public class NNamedColor {
    private String name;
    private Color color;

    public NNamedColor(String name, Color color) {
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
