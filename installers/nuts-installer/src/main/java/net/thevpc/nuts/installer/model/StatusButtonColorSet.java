package net.thevpc.nuts.installer.model;

import java.awt.*;

public class StatusButtonColorSet {
    public final ButtonColorSet normal;
    public final ButtonColorSet selected;
    public final ButtonColorSet disabled;
    public final ButtonColorSet hover;

    public StatusButtonColorSet(ButtonColorSet normal, ButtonColorSet selected, ButtonColorSet disabled, ButtonColorSet hover) {
        this.normal = normal;
        this.selected = selected;
        this.disabled = disabled;
        this.hover = hover;
    }
}
